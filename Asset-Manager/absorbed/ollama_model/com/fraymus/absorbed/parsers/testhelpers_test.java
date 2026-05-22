package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class testhelpers_test {
        "encoding/json";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );
        var argsComparer = cmp.Comparer(func(a, b api.ToolCallFunctionArguments) boolean {
        var aMap = a.ToMap();
        var bMap = b.ToMap();
        if len(aMap) != len(bMap) {
        return false;
    }
        var for k, av = range aMap {
        var bv, ok = bMap[k];
        if !ok {
        return false;
    }
        var aJSON, _ = json.Marshal(av);
        var bJSON, _ = json.Marshal(bv);
        if String(aJSON) != String(bJSON) {
        return false;
    }
    }
        return true;
        });
        var propsComparer = cmp.Comparer(func(a, b *api.ToolPropertiesMap) boolean {
        if a == null && b == null {
        return true;
    }
        if a == null || b == null {
        return false;
    }
        var aJSON, _ = json.Marshal(a);
        var bJSON, _ = json.Marshal(b);
        return String(aJSON) == String(bJSON);
        });
        var toolsComparer = cmp.Options{argsComparer, propsComparer}

    public static boolean toolCallEqual(api.ToolCall b) {
        if a.ID != b.ID {
        return false;
    }
        if a.Function.Index != b.Function.Index {
        return false;
    }
        if a.Function.Name != b.Function.Name {
        return false;
    }
        var aMap = a.Function.Arguments.ToMap();
        var bMap = b.Function.Arguments.ToMap();
        if len(aMap) != len(bMap) {
        return false;
    }
        var for k, av = range aMap {
        var bv, ok = bMap[k];
        if !ok {
        return false;
    }
        var aJSON, _ = json.Marshal(av);
        var bJSON, _ = json.Marshal(bv);
        if String(aJSON) != String(bJSON) {
        return false;
    }
    }
        return true;
    }
        func testPropsMap(m map[String]api.ToolProperty) *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        var for k, v = range m {
        props.Set(k, v);
    }
        return props;
    }
        func testArgs(m map[String]any) api.ToolCallFunctionArguments {
        var args = api.NewToolCallFunctionArguments();
        var for k, v = range m {
        args.Set(k, v);
    }
        return args;
    }
        func args(s String) api.ToolCallFunctionArguments {
        var result api.ToolCallFunctionArguments;
        var if err = json.Unmarshal([]byte(s), &result); err != null {
        panic("invalid JSON in args(): " + err.Error());
    }
        return result;
    }
}
