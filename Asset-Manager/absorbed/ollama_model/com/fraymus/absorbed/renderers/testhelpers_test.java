package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class testhelpers_test {
        "encoding/json";
        "github.com/ollama/ollama/api";
        );
        func args(s String) api.ToolCallFunctionArguments {
        var result api.ToolCallFunctionArguments;
        var if err = json.Unmarshal([]byte(s), &result); err != null {
        panic("invalid JSON in args(): " + err.Error());
    }
        return result;
    }
        func propsMap(s String) *api.ToolPropertiesMap {
        var result api.ToolPropertiesMap;
        var if err = json.Unmarshal([]byte(s), &result); err != null {
        panic("invalid JSON in propsMap(): " + err.Error());
    }
        return &result;
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

    public static class orderedArg {
        public String Key;
        public any Value;
    }
        func testArgsOrdered(pairs []orderedArg) api.ToolCallFunctionArguments {
        var args = api.NewToolCallFunctionArguments();
        var for _, p = range pairs {
        args.Set(p.Key, p.Value);
    }
        return args;
    }

    public static class orderedProp {
        public String Key;
        public api.ToolProperty Value;
    }
        func testPropsOrdered(pairs []orderedProp) *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        var for _, p = range pairs {
        props.Set(p.Key, p.Value);
    }
        return props;
    }
}
