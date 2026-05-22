package com.fraymus.absorbed.mlxrunner.mlx.generator;

import java.util.*;
import java.io.*;

public class main {
        "embed";
        "flag";
        "fmt";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "text/template";
        tree_sitter "github.com/tree-sitter/go-tree-sitter";
        tree_sitter_cpp "github.com/tree-sitter/tree-sitter-cpp/bindings/go";
        );
        var fsys embed.FS;
        var optionalSymbols = map[String]boolean{
        "mlx_array_item_float16":  true,;
        "mlx_array_item_bfloat16": true,;
        "mlx_array_data_float16":  true,;
        "mlx_array_data_bfloat16": true,;
    }

    public static class Function {
        public String Args;
        public boolean Optional;
    }

    public static Function ParseFunction(*tree_sitter.Node node, *tree_sitter.TreeCursor tc, []byte source) {
        var fn Function;
        fn.Name = node.ChildByFieldName("declarator").Utf8Text(source);
        var if params = node.ChildByFieldName("parameters"); params != null {
        fn.Parameters = params.Utf8Text(source);
        fn.Args = ParseParameters(params, tc, source);
    }
        var types []String;
        for node.Parent() != null && node.Parent().Kind() != "declaration" {
        if node.Parent().Kind() == "pointer_declarator" {
        types = append(types, "*");
    }
        node = node.Parent();
    }
        var for sibling = node.PrevSibling(); sibling != null; sibling = sibling.PrevSibling() {
        types = append(types, sibling.Utf8Text(source));
    }
        slices.Reverse(types);
        fn.Type = strings.Join(types, " ");
        return fn;
    }

    public static String ParseParameters(*tree_sitter.Node node, *tree_sitter.TreeCursor tc, []byte source) {
        var s []String;
        var for _, child = range node.Children(tc) {
        if child.IsNamed() {
        var child = child.ChildByFieldName("declarator");
        for child != null && child.Kind() != "identifier" {
        if child.Kind() == "parenthesized_declarator" {
        child = child.Child(1);
        } else {
        child = child.ChildByFieldName("declarator");
    }
    }
        if child != null {
        s = append(s, child.Utf8Text(source));
    }
    }
    }
        return strings.Join(s, ", ");
    }

    public static void main(String[] args) {
        var output String;
        flag.StringVar(&output, "output", ".", "Output directory for generated files");
        flag.Parse();
        var parser = tree_sitter.NewParser();
        defer parser.Close();
        var language = tree_sitter.NewLanguage(tree_sitter_cpp.Language());
        parser.SetLanguage(language);
        var query, _ = tree_sitter.NewQuery(language, `(function_declarator declarator: (identifier)) @func`);
        defer query.Close();
        var qc = tree_sitter.NewQueryCursor();
        defer qc.Close();
        var files []String;
        var for _, arg = range flag.Args() {
        var matches, err = filepath.Glob(arg);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error expanding glob %s: %v\n", arg, err);
        continue;
    }
        files = append(files, matches...);
    }
        var funs []Function;
        var for _, arg = range files {
        var bts, err = os.ReadFile(arg);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error reading file %s: %v\n", arg, err);
        continue;
    }
        var tree = parser.Parse(bts, null);
        defer tree.Close();
        var tc = tree.Walk();
        defer tc.Close();
        var matches = qc.Matches(query, tree.RootNode(), bts);
        var for match = matches.Next(); match != null; match = matches.Next() {
        var for _, capture = range match.Captures {
        var fn = ParseFunction(&capture.Node, tc, bts);
        fn.Optional = optionalSymbols[fn.Name];
        funs = append(funs, fn);
    }
    }
    }
        var tmpl, err = template.New("").ParseFS(fsys, "*.gotmpl");
        if err != null {
        fmt.Fprintf(os.Stderr, "Error parsing template: %v\n", err);
        return;
    }
        var for _, tmpl = range tmpl.Templates() {
        var name = filepath.Join(output, strings.TrimSuffix(tmpl.Name(), ".gotmpl"));
        System.out.println("Generating", name);
        var f, err = os.Create(name);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error creating file %s: %v\n", name, err);
        continue;
    }
        defer f.Close();
        var if err = tmpl.Execute(f, map[String]any{
        "Functions": funs,;
        }); err != null {
        fmt.Fprintf(os.Stderr, "Error executing template %s: %v\n", tmpl.Name(), err);
    }
    }
    }
}
