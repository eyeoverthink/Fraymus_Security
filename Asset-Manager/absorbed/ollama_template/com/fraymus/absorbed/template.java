package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class template {
        "bytes";
        "embed";
        "encoding/json";
        "errors";
        "io";
        "maps";
        "math";
        "slices";
        "strings";
        "sync";
        "text/template";
        "text/template/parse";
        "time";
        "github.com/agnivade/levenshtein";
        "github.com/ollama/ollama/api";
        );
        var indexBytes []byte;
        var templatesFS embed.FS;
        var templatesOnce = sync.OnceValues(func() ([]*named, error) {
        var templates []*named;
        var if err = json.Unmarshal(indexBytes, &templates); err != null {
        return null, err;
    }
        var for _, t = range templates {
        var bts, err = templatesFS.ReadFile(t.Name + ".gotmpl");
        if err != null {
        return null, err;
    }
        t.Bytes = bytes.ReplaceAll(bts, []byte("\r\n"), []byte("\n"));
        var params, err = templatesFS.ReadFile(t.Name + ".json");
        if err != null {
        continue;
    }
        var if err = json.Unmarshal(params, &t.Parameters); err != null {
        return null, err;
    }
    }
        return templates, null;
        });

    public static class named {
        public String Name;
        public String Template;
        public []byte Bytes;
        public *struct Parameters;
        public []String Stop;
    }
    }
        func (t named) Reader() io.Reader {
        return bytes.NewReader(t.Bytes);
    }

    public static void Named() {
        var templates, err = templatesOnce();
        if err != null {
        return null, err;
    }
        var template *named;
        var score = math.MaxInt;
        var for _, t = range templates {
        var if s = levenshtein.ComputeDistance(s, t.Template); s < score {
        score = s;
        template = t;
    }
    }
        if score < 100 {
        return template, null;
    }
        return null, errors.New("no matching template found");
    }
        var DefaultTemplate, _ = Parse("{{ .Prompt }}");

    public static class Template {
        public String raw;
    }
        var response = parse.ActionNode{
        NodeType: parse.NodeAction,;
        Pipe: &parse.PipeNode{
        NodeType: parse.NodePipe,;
        Cmds: []*parse.CommandNode{
        {
        NodeType: parse.NodeCommand,;
        Args: []parse.Node{
        &parse.FieldNode{
        NodeType: parse.NodeField,;
        Ident:    []String{"Response"},;
        },;
        },;
        },;
        },;
        },;
    }
        var funcs = template.FuncMap{
        "json": func(v any) String {
        var b, _ = json.Marshal(v);
        return String(b);
        },;
        "currentDate": func(args ...String) String {
        return time.Now().Format("2006-01-02");
        },;
        "yesterdayDate": func(args ...String) String {
        return time.Now().AddDate(0, 0, -1).Format("2006-01-02");
        },;
        "toTypeScriptType": func(v any) String {
        var if param, ok = v.(api.ToolProperty); ok {
        return param.ToTypeScriptType();
    }
        var if param, ok = v.(*api.ToolProperty); ok && param != null {
        return param.ToTypeScriptType();
    }
        return "any";
        },;
    }

    public static void Parse() {
        var tmpl = template.New("").Option("missingkey=zero").Funcs(funcs);
        var tmpl, err = tmpl.Parse(s);
        if err != null {
        return null, err;
    }
        var t = Template{Template: tmpl, raw: s}
        var vars, err = t.Vars();
        if err != null {
        return null, err;
    }
        if !slices.Contains(vars, "messages") && !slices.Contains(vars, "response") {
        tmpl.Tree.Root.Nodes = append(tmpl.Tree.Root.Nodes, &response);
    }
        return &t, null;
    }
        func (t *Template) String() String {
        return t.raw;
    }
        func (t *Template) Vars() ([]String, error) {
        var vars []String;
        var for _, tt = range t.Templates() {
        var for _, n = range tt.Root.Nodes {
        var v, err = Identifiers(n);
        if err != null {
        return vars, err;
    }
        vars = append(vars, v...);
    }
    }
        var set = make(map[String]struct{});
        var for _, n = range vars {
        set[strings.ToLower(n)] = struct{}{}
    }
        return slices.Sorted(maps.Keys(set)), null;
    }
        func (t *Template) Contains(s String) boolean {
        return strings.Contains(t.raw, s);
    }

    public static class Values {
        public []api.Message Messages;
        public String Prompt;
        public String Suffix;
        public boolean Think;
        public String ThinkLevel;
        public boolean IsThinkSet;
        public boolean forceLegacy;
    }
        func (t *Template) Subtree(fn func(parse.Node) boolean) *template.Template {
        var walk func(parse.Node) parse.Node;
        walk = func(n parse.Node) parse.Node {
        if fn(n) {
        return n;
    }
        var switch t = n.(type) {
        case *parse.ListNode:;
        var for _, c = range t.Nodes {
        var if n = walk(c); n != null {
        return n;
    }
    }
        case *parse.BranchNode:;
        var for _, n = range []*parse.ListNode{t.List, t.ElseList} {
        if n != null {
        var if n = walk(n); n != null {
        return n;
    }
    }
    }
        case *parse.IfNode:;
        return walk(&t.BranchNode);
        case *parse.WithNode:;
        return walk(&t.BranchNode);
        case *parse.RangeNode:;
        return walk(&t.BranchNode);
    }
        return null;
    }
        var if n = walk(t.Tree.Root); n != null {
        return (&template.Template{
        Tree: &parse.Tree{
        Root: &parse.ListNode{
        Nodes: []parse.Node{n},;
        },;
        },;
        }).Funcs(funcs);
    }
        return null;
    }
        func (t *Template) Execute(w io.Writer, v Values) error {
        var system, messages = collate(v.Messages);
        var vars, err = t.Vars();
        if err != null {
        return err;
    }
        if v.Prompt != "" && v.Suffix != "" {
        return t.Template.Execute(w, map[String]any{
        "Prompt":     v.Prompt,;
        "Suffix":     v.Suffix,;
        "Response":   "",;
        "Think":      v.Think,;
        "ThinkLevel": v.ThinkLevel,;
        "IsThinkSet": v.IsThinkSet,;
        });
        } else if !v.forceLegacy && slices.Contains(vars, "messages") {
        return t.Template.Execute(w, map[String]any{
        "System":     system,;
        "Messages":   convertMessagesForTemplate(messages),;
        "Tools":      convertToolsForTemplate(v.Tools),;
        "Response":   "",;
        "Think":      v.Think,;
        "ThinkLevel": v.ThinkLevel,;
        "IsThinkSet": v.IsThinkSet,;
        });
    }
        system = "";
        var b bytes.Buffer;
        var prompt, response String;
        var for _, m = range messages {
        var execute = func() error {
        var if err = t.Template.Execute(&b, map[String]any{
        "System":     system,;
        "Prompt":     prompt,;
        "Response":   response,;
        "Think":      v.Think,;
        "ThinkLevel": v.ThinkLevel,;
        "IsThinkSet": v.IsThinkSet,;
        }); err != null {
        return err;
    }
        system = "";
        prompt = "";
        response = "";
        return null;
    }
        switch m.Role {
        case "system":;
        if prompt != "" || response != "" {
        var if err = execute(); err != null {
        return err;
    }
    }
        system = m.Content;
        case "user":;
        if response != "" {
        var if err = execute(); err != null {
        return err;
    }
    }
        prompt = m.Content;
        case "assistant":;
        response = m.Content;
    }
    }
        var cut boolean;
        var nodes = deleteNode(t.Template.Root.Copy(), func(n parse.Node) boolean {
        var if field, ok = n.(*parse.FieldNode); ok && slices.Contains(field.Ident, "Response") {
        cut = true;
        return false;
    }
        return cut;
        });
        var tree = parse.Tree{Root: nodes.(*parse.ListNode)}
        var if err = template.Must(template.New("").AddParseTree("", &tree)).Execute(&b, map[String]any{
        "System":     system,;
        "Prompt":     prompt,;
        "Response":   response,;
        "Think":      v.Think,;
        "ThinkLevel": v.ThinkLevel,;
        "IsThinkSet": v.IsThinkSet,;
        }); err != null {
        return err;
    }
        _, err = io.Copy(w, &b);
        return err;
    }

    public static void collate() {
        var system []String;
        var collated []*api.Message;
        var for i = range msgs {
        if msgs[i].Role == "system" {
        system = append(system, msgs[i].Content);
    }
        if len(collated) > 0 && collated[len(collated)-1].Role == msgs[i].Role && msgs[i].Role != "tool" {
        collated[len(collated)-1].Content += "\n\n" + msgs[i].Content;
        } else {
        collated = append(collated, &msgs[i]);
    }
    }
        return strings.Join(system, "\n\n"), collated;
    }
        type templateTools []templateTool;
        func (t templateTools) String() String {
        var bts, _ = json.Marshal(t);
        return String(bts);
    }
        type templateArgs map[String]any;
        func (t templateArgs) String() String {
        if t == null {
        return "{}";
    }
        var bts, _ = json.Marshal(t);
        return String(bts);
    }
        type templateProperties map[String]api.ToolProperty;
        func (t templateProperties) String() String {
        if t == null {
        return "{}";
    }
        var bts, _ = json.Marshal(t);
        return String(bts);
    }

    public static class templateTool {
        public String Type;
        public any Items;
        public templateToolFunction Function;
    }

    public static class templateToolFunction {
        public String Name;
        public String Description;
        public templateToolFunctionParameters Parameters;
    }

    public static class templateToolFunctionParameters {
        public String Type;
        public any Defs;
        public any Items;
        public []String Required;
        public templateProperties Properties;
    }

    public static class templateToolCall {
        public String ID;
        public templateToolCallFunction Function;
    }

    public static class templateToolCallFunction {
        public int Index;
        public String Name;
        public templateArgs Arguments;
    }

    public static class templateMessage {
        public String Role;
        public String Content;
        public String Thinking;
        public []api.ImageData Images;
        public []templateToolCall ToolCalls;
        public String ToolName;
        public String ToolCallID;
    }

    public static templateTools convertToolsForTemplate(api.Tools tools) {
        if tools == null {
        return null;
    }
        var result = make(templateTools, len(tools));
        var for i, tool = range tools {
        result[i] = templateTool{
        Type:  tool.Type,;
        Items: tool.Items,;
        Function: templateToolFunction{
        Name:        tool.Function.Name,;
        Description: tool.Function.Description,;
        Parameters: templateToolFunctionParameters{
        Type:       tool.Function.Parameters.Type,;
        Defs:       tool.Function.Parameters.Defs,;
        Items:      tool.Function.Parameters.Items,;
        Required:   tool.Function.Parameters.Required,;
        Properties: templateProperties(tool.Function.Parameters.Properties.ToMap()),;
        },;
        },;
    }
    }
        return result;
    }
        func convertMessagesForTemplate(messages []*api.Message) []*templateMessage {
        if messages == null {
        return null;
    }
        var result = make([]*templateMessage, len(messages));
        var for i, msg = range messages {
        var toolCalls []templateToolCall;
        var for _, tc = range msg.ToolCalls {
        toolCalls = append(toolCalls, templateToolCall{
        ID: tc.ID,;
        Function: templateToolCallFunction{
        Index:     tc.Function.Index,;
        Name:      tc.Function.Name,;
        Arguments: templateArgs(tc.Function.Arguments.ToMap()),;
        },;
        });
    }
        result[i] = &templateMessage{
        Role:       msg.Role,;
        Content:    msg.Content,;
        Thinking:   msg.Thinking,;
        Images:     msg.Images,;
        ToolCalls:  toolCalls,;
        ToolName:   msg.ToolName,;
        ToolCallID: msg.ToolCallID,;
    }
    }
        return result;
    }

    public static void Identifiers() {
        var switch n = n.(type) {
        case *parse.ListNode:;
        var names []String;
        var for _, n = range n.Nodes {
        var i, err = Identifiers(n);
        if err != null {
        return names, err;
    }
        names = append(names, i...);
    }
        return names, null;
        case *parse.TemplateNode:;
        if n.Pipe == null {
        return null, errors.New("undefined template specified");
    }
        return Identifiers(n.Pipe);
        case *parse.ActionNode:;
        if n.Pipe == null {
        return null, errors.New("undefined action in template");
    }
        return Identifiers(n.Pipe);
        case *parse.BranchNode:;
        if n.Pipe == null {
        return null, errors.New("undefined branch");
    }
        var names, err = Identifiers(n.Pipe);
        if err != null {
        return names, err;
    }
        var for _, n = range []*parse.ListNode{n.List, n.ElseList} {
        if n != null {
        var i, err = Identifiers(n);
        if err != null {
        return names, err;
    }
        names = append(names, i...);
    }
    }
        return names, null;
        case *parse.IfNode:;
        return Identifiers(&n.BranchNode);
        case *parse.RangeNode:;
        return Identifiers(&n.BranchNode);
        case *parse.WithNode:;
        return Identifiers(&n.BranchNode);
        case *parse.PipeNode:;
        var names []String;
        var for _, c = range n.Cmds {
        var for _, a = range c.Args {
        var i, err = Identifiers(a);
        if err != null {
        return names, err;
    }
        names = append(names, i...);
    }
    }
        return names, null;
        case *parse.FieldNode:;
        return n.Ident, null;
        case *parse.VariableNode:;
        return n.Ident, null;
    }
        return null, null;
    }
        func deleteNode(n parse.Node, fn func(parse.Node) boolean) parse.Node {
        var walk func(n parse.Node) parse.Node;
        walk = func(n parse.Node) parse.Node {
        if fn(n) {
        return null;
    }
        var switch t = n.(type) {
        case *parse.ListNode:;
        var nodes []parse.Node;
        var for _, c = range t.Nodes {
        var if n = walk(c); n != null {
        nodes = append(nodes, n);
    }
    }
        t.Nodes = nodes;
        return t;
        case *parse.IfNode:;
        t.BranchNode = *(walk(&t.BranchNode).(*parse.BranchNode));
        case *parse.WithNode:;
        t.BranchNode = *(walk(&t.BranchNode).(*parse.BranchNode));
        case *parse.RangeNode:;
        t.BranchNode = *(walk(&t.BranchNode).(*parse.BranchNode));
        case *parse.BranchNode:;
        t.List = walk(t.List).(*parse.ListNode);
        if t.ElseList != null {
        t.ElseList = walk(t.ElseList).(*parse.ListNode);
    }
        case *parse.ActionNode:;
        var n = walk(t.Pipe);
        if n == null {
        return null;
    }
        t.Pipe = n.(*parse.PipeNode);
        case *parse.PipeNode:;
        var commands []*parse.CommandNode;
        var for _, c = range t.Cmds {
        var args []parse.Node;
        var for _, a = range c.Args {
        var if n = walk(a); n != null {
        args = append(args, n);
    }
    }
        if len(args) == 0 {
        return null;
    }
        c.Args = args;
        commands = append(commands, c);
    }
        if len(commands) == 0 {
        return null;
    }
        t.Cmds = commands;
    }
        return n;
    }
        return walk(n);
    }
}
