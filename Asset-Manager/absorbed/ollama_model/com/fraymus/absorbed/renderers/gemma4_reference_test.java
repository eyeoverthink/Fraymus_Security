package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class gemma4_reference_test {
        "encoding/json";
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "strings";
        "testing";
        "github.com/ollama/ollama/api";
        "github.com/stretchr/testify/assert";
        );
        const (;
        gemma4E2BTemplate = "testdata/gemma4_e2b_chat_template.jinja2";
        gemma431BTemplate = "testdata/gemma4_31b_chat_template.jinja2";
        );
        func bashRefTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "bash",;
        Description: "Run a command",;
        Parameters: api.ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"command"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "command": {Type: api.PropertyType{"String"}, Description: "The command"},;
        }),;
        },;
        },;
        }}
    }
        func bashAndReadRefTools() []api.Tool {
        return []api.Tool{
        bashRefTool()[0],;
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "read",;
        Description: "Read a file",;
        Parameters: api.ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"path"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "path": {Type: api.PropertyType{"String"}, Description: "File path"},;
        }),;
        },;
        },;
        },;
    }
    }
        func weatherTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "city": {Type: api.PropertyType{"String"}, Description: "City"},;
        }),;
        },;
        },;
        }}
    }
        func addTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "add",;
        Description: "Add numbers",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "a": {Type: api.PropertyType{"number"}},;
        "b": {Type: api.PropertyType{"number"}},;
        }),;
        },;
        },;
        }}
    }
        func flagTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "set_flag",;
        Description: "Set a flag",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "enabled": {Type: api.PropertyType{"boolean"}, Description: "Flag value"},;
        }),;
        },;
        },;
        }}
    }
        func modeTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "set_mode",;
        Description: "Set mode",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "mode": {Type: api.PropertyType{"String"}, Description: "The mode", Enum: []any{"fast", "slow"}},;
        }),;
        },;
        },;
        }}
    }
        func bashSmallTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "bash",;
        Description: "Run",;
        Parameters: api.ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"command"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "command": {Type: api.PropertyType{"String"}, Description: "Cmd"},;
        }),;
        },;
        },;
        }}
    }
        func nestedTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "create",;
        Description: "Create item",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "name": {Type: api.PropertyType{"String"}, Description: "Name"},;
        "config": {Type: api.PropertyType{"object"}, Description: "Config", Properties: testPropsMap(map[String]api.ToolProperty{
        "enabled": {Type: api.PropertyType{"boolean"}, Description: "On/off"},;
        })},;
        }),;
        },;
        },;
        }}
    }
        func arrayTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "batch",;
        Description: "Run batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "commands": {Type: api.PropertyType{"array"}, Description: "Commands", Items: map[String]any{"type": "String"}},;
        }),;
        },;
        },;
        }}
    }
        func unionTopLevelTypeTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "maybe_name",;
        Description: "Test nullable union",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "name": {Type: api.PropertyType{"String", "null"}, Description: "Name"},;
        }),;
        },;
        },;
        }}
    }
        func anyOfTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "pick_value",;
        Description: "Pick a value",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "value": {
        AnyOf: []api.ToolProperty{
        {Type: api.PropertyType{"String"}},;
        {Type: api.PropertyType{"number"}},;
        },;
        Description: "Value",;
        },;
        }),;
        },;
        },;
        }}
    }
        func arrayObjectItemsTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "upsert_batch",;
        Description: "Upsert batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "entries": {
        Type:        api.PropertyType{"array"},;
        Description: "Entries",;
        Items: map[String]any{
        "type":     "object",;
        "required": []String{"id"},;
        "properties": map[String]any{
        "id": map[String]any{
        "type":        "String",;
        "description": "ID",;
        },;
        "count": map[String]any{
        "type":        "number",;
        "description": "Count",;
        },;
        },;
        },;
        },;
        }),;
        },;
        },;
        }}
    }
        func arrayUnionItemsTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "maybe_batch",;
        Description: "Maybe batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "values": {
        Type:        api.PropertyType{"array"},;
        Description: "Values",;
        Items: map[String]any{
        "type": []String{"String", "null"},;
        },;
        },;
        }),;
        },;
        },;
        }}
    }
        func arrayNestedItemsTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "plan_batch",;
        Description: "Plan batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "steps": {
        Type:        api.PropertyType{"array"},;
        Description: "Steps",;
        Items: map[String]any{
        "type":     "object",;
        "required": []String{"name"},;
        "properties": map[String]any{
        "name": map[String]any{
        "type":        "String",;
        "description": "Name",;
        },;
        "config": map[String]any{
        "type":        "object",;
        "description": "Config",;
        "required":    []String{"enabled"},;
        "properties": map[String]any{
        "enabled": map[String]any{
        "type":        "boolean",;
        "description": "Enabled",;
        },;
        },;
        },;
        "tags": map[String]any{
        "type":        "array",;
        "description": "Tags",;
        "items": map[String]any{
        "type": "String",;
        },;
        },;
        },;
        },;
        },;
        }),;
        },;
        },;
        }}
    }
        func arrayNullableNestedItemsTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "annotate_batch",;
        Description: "Annotate batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "entries": {
        Type:        api.PropertyType{"array"},;
        Description: "Entries",;
        Items: map[String]any{
        "type":     "object",;
        "required": []String{"note"},;
        "properties": map[String]any{
        "note": map[String]any{
        "type":        "String",;
        "description": "Note",;
        "nullable":    true,;
        },;
        "metadata": map[String]any{
        "type":        "object",;
        "description": "Metadata",;
        "nullable":    true,;
        "properties": map[String]any{
        "tag": map[String]any{
        "type":        "String",;
        "description": "Tag",;
        },;
        },;
        },;
        },;
        },;
        },;
        }),;
        },;
        },;
        }}
    }
        func nestedArrayObjectItemsTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "schedule_jobs",;
        Description: "Schedule jobs",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "jobs": {
        Type:        api.PropertyType{"array"},;
        Description: "Jobs",;
        Items: map[String]any{
        "type":     "object",;
        "required": []String{"tasks"},;
        "properties": map[String]any{
        "tasks": map[String]any{
        "type":        "array",;
        "description": "Tasks",;
        "items": map[String]any{
        "type":     "object",;
        "required": []String{"command"},;
        "properties": map[String]any{
        "command": map[String]any{
        "type":        "String",;
        "description": "Command",;
        },;
        "timeout": map[String]any{
        "type":        "number",;
        "description": "Timeout",;
        },;
        },;
        },;
        },;
        },;
        },;
        },;
        }),;
        },;
        },;
        }}
    }
        func arrayItemsExtraKeysTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "configure_batch",;
        Description: "Configure batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "settings": {
        Type:        api.PropertyType{"array"},;
        Description: "Settings",;
        Items: map[String]any{
        "type":                 "object",;
        "additionalProperties": false,;
        "default": map[String]any{
        "mode": "auto",;
        },;
        "nullable": true,;
        "required": []String{"mode"},;
        "properties": map[String]any{
        "mode": map[String]any{
        "type":        "String",;
        "description": "Mode",;
        },;
        },;
        },;
        },;
        }),;
        },;
        },;
        }}
    }
        func configureTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "configure",;
        Description: "Configure",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "config": {Type: api.PropertyType{"object"}, Description: "Config"},;
        }),;
        },;
        },;
        }}
    }
        func batchArrayTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "batch",;
        Description: "Run batch",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "ids": {Type: api.PropertyType{"array"}, Description: "IDs"},;
        }),;
        },;
        },;
        }}
    }
        func countTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "count",;
        Description: "Count items",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "n": {Type: api.PropertyType{"number"}},;
        }),;
        },;
        },;
        }}
    }
        func enumNoDescTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "set_level",;
        Description: "Set level",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "level": {Type: api.PropertyType{"String"}, Enum: []any{"low", "high"}},;
        }),;
        },;
        },;
        }}
    }
        func nestedRequiredTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "create_user",;
        Description: "Create user",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "profile": {
        Type: api.PropertyType{"object"}, Description: "Profile",;
        Required: []String{"name"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "name": {Type: api.PropertyType{"String"}, Description: "Name"},;
        "age":  {Type: api.PropertyType{"number"}, Description: "Age"},;
        }),;
        },;
        }),;
        },;
        },;
        }}
    }
        func calcTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "calc",;
        Description: "Calculate",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "value": {Type: api.PropertyType{"number"}, Description: "Value"},;
        }),;
        },;
        },;
        }}
    }
        func rawTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "raw",;
        Description: "Raw input",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        },;
        },;
        }}
    }
        func moveTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "move",;
        Description: "Move",;
        Parameters: api.ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"x", "y"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "x": {Type: api.PropertyType{"number"}, Description: "X"},;
        "y": {Type: api.PropertyType{"number"}, Description: "Y"},;
        }),;
        },;
        },;
        }}
    }
        func arrayNoItemsTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "tag",;
        Description: "Tag items",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "tags": {Type: api.PropertyType{"array"}, Description: "Tags"},;
        }),;
        },;
        },;
        }}
    }
        func objectNoDescTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "update",;
        Description: "Update settings",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "settings": {Type: api.PropertyType{"object"}, Properties: testPropsMap(map[String]api.ToolProperty{
        "verbose": {Type: api.PropertyType{"boolean"}, Description: "Verbose mode"},;
        })},;
        }),;
        },;
        },;
        }}
    }
        func searchTool() []api.Tool {
        return []api.Tool{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "search",;
        Description: "Search",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "query":  {Type: api.PropertyType{"String"}, Description: "Search query"},;
        "limit":  {Type: api.PropertyType{"number"}},;
        "offset": {Type: api.PropertyType{"number"}, Description: "Start offset"},;
        }),;
        },;
        },;
        }}
    }
        var (;
        bashSmallDeclRef      = `<|tool>declaration:bash{description:<|"|>Run<|"|>,parameters:{properties:{command:{description:<|"|>Cmd<|"|>,type:<|"|>STRING<|"|>}},required:[<|"|>command<|"|>],type:<|"|>OBJECT<|"|>}}<tool|>`;
        nestedDeclRef         = `<|tool>declaration:create{description:<|"|>Create item<|"|>,parameters:{properties:{config:{description:<|"|>Config<|"|>,properties:{enabled:{description:<|"|>On/off<|"|>,type:<|"|>BOOLEAN<|"|>}},type:<|"|>OBJECT<|"|>},name:{description:<|"|>Name<|"|>,type:<|"|>STRING<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        arrayDeclRef          = `<|tool>declaration:batch{description:<|"|>Run batch<|"|>,parameters:{properties:{commands:{description:<|"|>Commands<|"|>,items:{type:<|"|>STRING<|"|>},type:<|"|>ARRAY<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        bashDeclRef           = `<|tool>declaration:bash{description:<|"|>Run a command<|"|>,parameters:{properties:{command:{description:<|"|>The command<|"|>,type:<|"|>STRING<|"|>}},required:[<|"|>command<|"|>],type:<|"|>OBJECT<|"|>}}<tool|>`;
        readDeclRef           = `<|tool>declaration:read{description:<|"|>Read a file<|"|>,parameters:{properties:{path:{description:<|"|>File path<|"|>,type:<|"|>STRING<|"|>}},required:[<|"|>path<|"|>],type:<|"|>OBJECT<|"|>}}<tool|>`;
        weatherDeclRef        = `<|tool>declaration:get_weather{description:<|"|>Get weather<|"|>,parameters:{properties:{city:{description:<|"|>City<|"|>,type:<|"|>STRING<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        addDeclRef            = `<|tool>declaration:add{description:<|"|>Add numbers<|"|>,parameters:{properties:{a:{type:<|"|>NUMBER<|"|>},b:{type:<|"|>NUMBER<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        flagDeclRef           = `<|tool>declaration:set_flag{description:<|"|>Set a flag<|"|>,parameters:{properties:{enabled:{description:<|"|>Flag value<|"|>,type:<|"|>BOOLEAN<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        modeDeclRef           = `<|tool>declaration:set_mode{description:<|"|>Set mode<|"|>,parameters:{properties:{mode:{description:<|"|>The mode<|"|>,enum:[<|"|>fast<|"|>,<|"|>slow<|"|>],type:<|"|>STRING<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        configureDeclRef      = `<|tool>declaration:configure{description:<|"|>Configure<|"|>,parameters:{properties:{config:{description:<|"|>Config<|"|>,properties:{},type:<|"|>OBJECT<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        batchArrayDeclRef     = `<|tool>declaration:batch{description:<|"|>Run batch<|"|>,parameters:{properties:{ids:{description:<|"|>IDs<|"|>,type:<|"|>ARRAY<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        countDeclRef          = `<|tool>declaration:count{description:<|"|>Count items<|"|>,parameters:{properties:{n:{type:<|"|>NUMBER<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        enumNoDescDeclRef     = `<|tool>declaration:set_level{description:<|"|>Set level<|"|>,parameters:{properties:{level:{enum:[<|"|>low<|"|>,<|"|>high<|"|>],type:<|"|>STRING<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        searchDeclRef         = `<|tool>declaration:search{description:<|"|>Search<|"|>,parameters:{properties:{limit:{type:<|"|>NUMBER<|"|>},offset:{description:<|"|>Start offset<|"|>,type:<|"|>NUMBER<|"|>},query:{description:<|"|>Search query<|"|>,type:<|"|>STRING<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        arrayNoItemsDeclRef   = `<|tool>declaration:tag{description:<|"|>Tag items<|"|>,parameters:{properties:{tags:{description:<|"|>Tags<|"|>,type:<|"|>ARRAY<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        objectNoDescDeclRef   = `<|tool>declaration:update{description:<|"|>Update settings<|"|>,parameters:{properties:{settings:{properties:{verbose:{description:<|"|>Verbose mode<|"|>,type:<|"|>BOOLEAN<|"|>}},type:<|"|>OBJECT<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        nestedRequiredDeclRef = `<|tool>declaration:create_user{description:<|"|>Create user<|"|>,parameters:{properties:{profile:{description:<|"|>Profile<|"|>,properties:{age:{description:<|"|>Age<|"|>,type:<|"|>NUMBER<|"|>},name:{description:<|"|>Name<|"|>,type:<|"|>STRING<|"|>}},required:[<|"|>name<|"|>],type:<|"|>OBJECT<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        calcDeclRef           = `<|tool>declaration:calc{description:<|"|>Calculate<|"|>,parameters:{properties:{value:{description:<|"|>Value<|"|>,type:<|"|>NUMBER<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|>`;
        rawDeclRef            = `<|tool>declaration:raw{description:<|"|>Raw input<|"|>,parameters:{type:<|"|>OBJECT<|"|>}}<tool|>`;
        moveDeclRef           = `<|tool>declaration:move{description:<|"|>Move<|"|>,parameters:{properties:{x:{description:<|"|>X<|"|>,type:<|"|>NUMBER<|"|>},y:{description:<|"|>Y<|"|>,type:<|"|>NUMBER<|"|>}},required:[<|"|>x<|"|>,<|"|>y<|"|>],type:<|"|>OBJECT<|"|>}}<tool|>`;
        );

    public static void TestGemma4RendererMatchesReference(*testing.T t) {
        var q = `<|"|>`;
        var tests = []struct {
        name       String;
        messages   []api.Message;
        tools      []api.Tool;
        think      *api.ThinkValue;
        expected   String;
        skipJinja2 boolean;
        }{
        {
        name:     "user_only",;
        messages: []api.Message{{Role: "user", Content: "Hello"}},;
        expected: "<bos><|turn>user\nHello<turn|>\n<|turn>model\n",;
        },;
        {
        name: "system_user",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "Hi"},;
        },;
        expected: "<bos><|turn>system\nYou are helpful.<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "developer_user",;
        messages: []api.Message{
        {Role: "developer", Content: "You are helpful."},;
        {Role: "user", Content: "Hi"},;
        },;
        expected: "<bos><|turn>system\nYou are helpful.<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "tools_no_system",;
        messages: []api.Message{{Role: "user", Content: "Hi"}},;
        tools:    bashRefTool(),;
        expected: "<bos><|turn>system\n" + bashDeclRef + "<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "system_tools",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "Hi"},;
        },;
        tools:    bashRefTool(),;
        expected: "<bos><|turn>system\nYou are helpful." + bashDeclRef + "<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "thinking_no_system",;
        messages: []api.Message{{Role: "user", Content: "Hi"}},;
        think:    thinkTrue(),;
        expected: "<bos><|turn>system\n<|think|>\n<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "thinking_system",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "Hi"},;
        },;
        think:    thinkTrue(),;
        expected: "<bos><|turn>system\n<|think|>\nYou are helpful.<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "thinking_tools",;
        messages: []api.Message{{Role: "user", Content: "Hi"}},;
        tools:    bashRefTool(),;
        think:    thinkTrue(),;
        expected: "<bos><|turn>system\n<|think|>\n" + bashDeclRef + "<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "thinking_system_tools",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "Hi"},;
        },;
        tools:    bashRefTool(),;
        think:    thinkTrue(),;
        expected: "<bos><|turn>system\n<|think|>\nYou are helpful." + bashDeclRef + "<turn|>\n<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "thinking_explicitly_disabled",;
        messages: []api.Message{{Role: "user", Content: "Hi"}},;
        think:    thinkFalse(),;
        expected: "<bos><|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "multi_turn",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: "Hello!"},;
        {Role: "user", Content: "More"},;
        },;
        expected: "<bos><|turn>system\nYou are helpful.<turn|>\n" +;
        "<|turn>user\nHi<turn|>\n" +;
        "<|turn>model\nHello!<turn|>\n" +;
        "<|turn>user\nMore<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "tool_call_response",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "List files"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{
        Name:      "bash",;
        Arguments: testArgs(map[String]any{"command": "ls"}),;
        },;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "file1.txt\nfile2.txt"},;
        },;
        tools: bashRefTool(),;
        expected: "<bos><|turn>system\nYou are helpful." + bashDeclRef + "<turn|>\n" +;
        "<|turn>user\nList files<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "file1.txt\nfile2.txt" + q + "}<tool_response|>",;
        },;
        {
        name: "tool_call_pending_response",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "List files"},;
        {Role: "assistant", Content: "Let me check.", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{
        Name:      "bash",;
        Arguments: testArgs(map[String]any{"command": "ls"}),;
        },;
        }}},;
        },;
        tools: bashRefTool(),;
        expected: "<bos><|turn>system\nYou are helpful." + bashDeclRef + "<turn|>\n" +;
        "<|turn>user\nList files<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>Let me check.<|tool_response>",;
        },;
        {
        name: "full_round_trip",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "List files"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{
        Name:      "bash",;
        Arguments: testArgs(map[String]any{"command": "ls"}),;
        },;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "file1.txt\nfile2.txt"},;
        {Role: "assistant", Content: "Here are the files."},;
        {Role: "user", Content: "Read file1.txt"},;
        },;
        tools: bashAndReadRefTools(),;
        expected: "<bos><|turn>system\nYou are helpful." + bashDeclRef + readDeclRef + "<turn|>\n" +;
        "<|turn>user\nList files<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "file1.txt\nfile2.txt" + q + "}<tool_response|>" +;
        "Here are the files.<turn|>\n" +;
        "<|turn>user\nRead file1.txt<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "multiple_tool_calls",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "List and read"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{
        {Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})}},;
        {Function: api.ToolCallFunction{Name: "read", Arguments: testArgs(map[String]any{"path": "go.mod"})}},;
        }},;
        {Role: "tool", ToolName: "bash", Content: "file1.txt\nfile2.txt"},;
        {Role: "tool", ToolName: "read", Content: "module example.com/foo"},;
        },;
        tools: bashAndReadRefTools(),;
        expected: "<bos><|turn>system\nYou are helpful." + bashDeclRef + readDeclRef + "<turn|>\n" +;
        "<|turn>user\nList and read<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_call>call:read{path:" + q + "go.mod" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "file1.txt\nfile2.txt" + q + "}<tool_response|>" +;
        "<|tool_response>response:read{value:" + q + "module example.com/foo" + q + "}<tool_response|>",;
        skipJinja2: true,;
        },;
        {
        name: "strip_thinking_history",;
        messages: []api.Message{
        {Role: "user", Content: "What is 2+2?"},;
        {Role: "assistant", Content: "<|channel>thought\nThinking...<channel|>4"},;
        {Role: "user", Content: "And 3+3?"},;
        },;
        expected: "<bos><|turn>user\nWhat is 2+2?<turn|>\n" +;
        "<|turn>model\n4<turn|>\n" +;
        "<|turn>user\nAnd 3+3?<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "assistant_content_with_tool_call",;
        messages: []api.Message{
        {Role: "user", Content: "Weather?"},;
        {Role: "assistant", Content: "Let me check.", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "get_weather", Arguments: testArgs(map[String]any{"city": "Paris"})},;
        }}},;
        {Role: "tool", ToolName: "get_weather", Content: "Sunny"},;
        },;
        tools: weatherTool(),;
        expected: "<bos><|turn>system\n" + weatherDeclRef + "<turn|>\n" +;
        "<|turn>user\nWeather?<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:get_weather{city:" + q + "Paris" + q + "}<tool_call|>" +;
        "<|tool_response>response:get_weather{value:" + q + "Sunny" + q + "}<tool_response|>" +;
        "Let me check.<turn|>\n",;
        },;
        {
        name: "numeric_arguments",;
        messages: []api.Message{
        {Role: "user", Content: "Add"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "add", Arguments: testArgs(map[String]any{"a": double(1), "b": double(2)})},;
        }}},;
        {Role: "tool", ToolName: "add", Content: `{"result": 3}`},;
        },;
        tools: addTool(),;
        expected: "<bos><|turn>system\n" + addDeclRef + "<turn|>\n" +;
        "<|turn>user\nAdd<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:add{a:1,b:2}<tool_call|>" +;
        "<|tool_response>response:add{value:" + q + `{"result": 3}` + q + "}<tool_response|>",;
        },;
        {
        name: "boolean_argument",;
        messages: []api.Message{
        {Role: "user", Content: "Set flag"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "set_flag", Arguments: testArgs(map[String]any{"enabled": true})},;
        }}},;
        {Role: "tool", ToolName: "set_flag", Content: "done"},;
        },;
        tools: flagTool(),;
        expected: "<bos><|turn>system\n" + flagDeclRef + "<turn|>\n" +;
        "<|turn>user\nSet flag<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:set_flag{enabled:true}<tool_call|>" +;
        "<|tool_response>response:set_flag{value:" + q + "done" + q + "}<tool_response|>",;
        },;
        {
        name:     "tool_with_enum",;
        messages: []api.Message{{Role: "user", Content: "Test"}},;
        tools:    modeTool(),;
        expected: "<bos><|turn>system\n" + modeDeclRef + "<turn|>\n" +;
        "<|turn>user\nTest<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "unicode_content",;
        messages: []api.Message{{Role: "user", Content: "こんにちは"}},;
        expected: "<bos><|turn>user\nこんにちは<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "newlines_in_content",;
        messages: []api.Message{{Role: "user", Content: "Line 1\nLine 2\nLine 3"}},;
        expected: "<bos><|turn>user\nLine 1\nLine 2\nLine 3<turn|>\n<|turn>model\n",;
        },;
        {
        name: "json_tool_response_then_user",;
        messages: []api.Message{
        {Role: "user", Content: "Weather?"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "get_weather", Arguments: testArgs(map[String]any{"city": "Tokyo"})},;
        }}},;
        {Role: "tool", ToolName: "get_weather", Content: `{"temperature": 15, "weather": "sunny"}`},;
        {Role: "user", Content: "Thanks!"},;
        },;
        tools: weatherTool(),;
        expected: "<bos><|turn>system\n" + weatherDeclRef + "<turn|>\n" +;
        "<|turn>user\nWeather?<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:get_weather{city:" + q + "Tokyo" + q + "}<tool_call|>" +;
        "<|tool_response>response:get_weather{value:" + q + `{"temperature": 15, "weather": "sunny"}` + q + "}<tool_response|>" +;
        "<|turn>user\nThanks!<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "sorted_args",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"zzz": "last", "aaa": "first", "mmm": "middle"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "ok"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{aaa:" + q + "first" + q + ",mmm:" + q + "middle" + q + ",zzz:" + q + "last" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "ok" + q + "}<tool_response|>",;
        },;
        {
        name:     "user_content_trimmed",;
        messages: []api.Message{{Role: "user", Content: "  hello  "}},;
        expected: "<bos><|turn>user\nhello<turn|>\n<|turn>model\n",;
        },;
        {
        name: "empty_tool_args",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "ok"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "ok" + q + "}<tool_response|>",;
        },;
        {
        name:     "nested_object_tool",;
        messages: []api.Message{{Role: "user", Content: "Create"}},;
        tools:    nestedTool(),;
        expected: "<bos><|turn>system\n" + nestedDeclRef + "<turn|>\n" +;
        "<|turn>user\nCreate<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "array_tool",;
        messages: []api.Message{{Role: "user", Content: "Batch"}},;
        tools:    arrayTool(),;
        expected: "<bos><|turn>system\n" + arrayDeclRef + "<turn|>\n" +;
        "<|turn>user\nBatch<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "typed_property_union_type",;
        messages: []api.Message{{Role: "user", Content: "Hi"}},;
        tools:    unionTopLevelTypeTool(),;
        expected: `<bos><|turn>system;
        <|tool>declaration:maybe_name{description:<|"|>Test nullable union<|"|>,parameters:{properties:{name:{description:<|"|>Name<|"|>,type:<|"|>['STRING', 'NULL']<|"|>}},type:<|"|>OBJECT<|"|>}}<tool|><turn|>;
        <|turn>user;
        Hi<turn|>;
        <|turn>model;
        `,;
        },;
        {
        name: "assistant_whitespace_trimmed",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: "  spaced  "},;
        {Role: "user", Content: "More"},;
        },;
        expected: "<bos><|turn>user\nHi<turn|>\n" +;
        "<|turn>model\nspaced<turn|>\n" +;
        "<|turn>user\nMore<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "three_tool_responses",;
        messages: []api.Message{
        {Role: "user", Content: "Do three things"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{
        {Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "a"})}},;
        {Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "b"})}},;
        {Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "c"})}},;
        }},;
        {Role: "tool", ToolName: "bash", Content: "result-a"},;
        {Role: "tool", ToolName: "bash", Content: "result-b"},;
        {Role: "tool", ToolName: "bash", Content: "result-c"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nDo three things<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "a" + q + "}<tool_call|>" +;
        "<|tool_call>call:bash{command:" + q + "b" + q + "}<tool_call|>" +;
        "<|tool_call>call:bash{command:" + q + "c" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "result-a" + q + "}<tool_response|>" +;
        "<|tool_response>response:bash{value:" + q + "result-b" + q + "}<tool_response|>" +;
        "<|tool_response>response:bash{value:" + q + "result-c" + q + "}<tool_response|>",;
        },;
        {
        name: "tool_calls_no_content",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "files"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "files" + q + "}<tool_response|>",;
        },;
        {
        name: "multiple_thinking_blocks",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: "<|channel>Think1<channel|>Middle<|channel>Think2<channel|>Done"},;
        {Role: "user", Content: "More"},;
        },;
        expected: "<bos><|turn>user\nHi<turn|>\n" +;
        "<|turn>model\nMiddleDone<turn|>\n" +;
        "<|turn>user\nMore<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name:     "property_no_description",;
        messages: []api.Message{{Role: "user", Content: "Count"}},;
        tools:    countTool(),;
        expected: "<bos><|turn>system\n" + countDeclRef + "<turn|>\n" +;
        "<|turn>user\nCount<turn|>\n<|turn>model\n",;
        },;
        {
        name: "system_message_trimmed",;
        messages: []api.Message{
        {Role: "system", Content: "  You are helpful.  "},;
        {Role: "user", Content: "Hi"},;
        },;
        expected: "<bos><|turn>system\nYou are helpful.<turn|>\n" +;
        "<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "nested_map_args",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "configure", Arguments: testArgs(map[String]any{
        "config": map[String]any{"db": map[String]any{"host": "localhost", "port": double(5432)}},;
        })},;
        }}},;
        {Role: "tool", ToolName: "configure", Content: "ok"},;
        },;
        tools: configureTool(),;
        expected: "<bos><|turn>system\n" + configureDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:configure{config:{db:{host:" + q + "localhost" + q + ",port:5432}}}<tool_call|>" +;
        "<|tool_response>response:configure{value:" + q + "ok" + q + "}<tool_response|>",;
        },;
        {
        name: "array_in_args",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "batch", Arguments: testArgs(map[String]any{
        "ids": []any{double(1), double(2), double(3)},;
        })},;
        }}},;
        {Role: "tool", ToolName: "batch", Content: "done"},;
        },;
        tools: batchArrayTool(),;
        expected: "<bos><|turn>system\n" + batchArrayDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:batch{ids:[1,2,3]}<tool_call|>" +;
        "<|tool_response>response:batch{value:" + q + "done" + q + "}<tool_response|>",;
        },;
        {
        name: "mixed_array_args",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "batch", Arguments: testArgs(map[String]any{
        "ids": []any{"a", double(1), true},;
        })},;
        }}},;
        {Role: "tool", ToolName: "batch", Content: "done"},;
        },;
        tools: batchArrayTool(),;
        expected: "<bos><|turn>system\n" + batchArrayDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:batch{ids:[" + q + "a" + q + ",1,true]}<tool_call|>" +;
        "<|tool_response>response:batch{value:" + q + "done" + q + "}<tool_response|>",;
        },;
        {
        name:     "enum_no_description",;
        messages: []api.Message{{Role: "user", Content: "Set"}},;
        tools:    enumNoDescTool(),;
        expected: "<bos><|turn>system\n" + enumNoDescDeclRef + "<turn|>\n" +;
        "<|turn>user\nSet<turn|>\n<|turn>model\n",;
        },;
        {
        name: "system_whitespace_only",;
        messages: []api.Message{
        {Role: "system", Content: "   "},;
        {Role: "user", Content: "Hi"},;
        },;
        expected: "<bos><|turn>system\n<turn|>\n" +;
        "<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name: "empty_assistant_content",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: ""},;
        {Role: "user", Content: "More"},;
        },;
        expected: "<bos><|turn>user\nHi<turn|>\n" +;
        "<|turn>model\n<turn|>\n" +;
        "<|turn>user\nMore<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "map_arg_string_keys",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "configure", Arguments: testArgs(map[String]any{
        "config": map[String]any{"key": "value"},;
        })},;
        }}},;
        {Role: "tool", ToolName: "configure", Content: "ok"},;
        },;
        tools: configureTool(),;
        expected: "<bos><|turn>system\n" + configureDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:configure{config:{key:" + q + "value" + q + "}}<tool_call|>" +;
        "<|tool_response>response:configure{value:" + q + "ok" + q + "}<tool_response|>",;
        },;
        {
        name:     "mixed_desc_no_desc",;
        messages: []api.Message{{Role: "user", Content: "Search"}},;
        tools:    searchTool(),;
        expected: "<bos><|turn>system\n" + searchDeclRef + "<turn|>\n" +;
        "<|turn>user\nSearch<turn|>\n<|turn>model\n",;
        },;
        {
        name: "tool_content_trimmed",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "  result  "},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "  result  " + q + "}<tool_response|>",;
        },;
        {
        name: "empty_system_message",;
        messages: []api.Message{
        {Role: "system", Content: ""},;
        {Role: "user", Content: "Hi"},;
        },;
        expected: "<bos><|turn>system\n<turn|>\n" +;
        "<|turn>user\nHi<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "nested_object_with_required",;
        messages: []api.Message{{Role: "user", Content: "Create"}},;
        tools:    nestedRequiredTool(),;
        expected: "<bos><|turn>system\n" + nestedRequiredDeclRef + "<turn|>\n" +;
        "<|turn>user\nCreate<turn|>\n<|turn>model\n",;
        },;
        {
        name: "float_argument",;
        messages: []api.Message{
        {Role: "user", Content: "Calc"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "calc", Arguments: testArgs(map[String]any{"value": 3.14})},;
        }}},;
        {Role: "tool", ToolName: "calc", Content: "ok"},;
        },;
        tools: calcTool(),;
        expected: "<bos><|turn>system\n" + calcDeclRef + "<turn|>\n" +;
        "<|turn>user\nCalc<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:calc{value:3.14}<tool_call|>" +;
        "<|tool_response>response:calc{value:" + q + "ok" + q + "}<tool_response|>",;
        },;
        {
        name: "thinking_in_last_assistant",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: "<|channel>thinking<channel|>Result"},;
        },;
        expected: "<bos><|turn>user\nHi<turn|>\n" +;
        "<|turn>model\nResult<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "tool_content_multiline_whitespace",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "\n  file1\n  file2\n"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "\n  file1\n  file2\n" + q + "}<tool_response|>",;
        },;
        {
        name:     "tool_params_type_only",;
        messages: []api.Message{{Role: "user", Content: "Raw"}},;
        tools:    rawTool(),;
        expected: "<bos><|turn>system\n" + rawDeclRef + "<turn|>\n" +;
        "<|turn>user\nRaw<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "multiple_required",;
        messages: []api.Message{{Role: "user", Content: "Move"}},;
        tools:    moveTool(),;
        expected: "<bos><|turn>system\n" + moveDeclRef + "<turn|>\n" +;
        "<|turn>user\nMove<turn|>\n<|turn>model\n",;
        },;
        {
        name: "assistant_only_thinking",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: "<|channel>just thinking<channel|>"},;
        {Role: "user", Content: "More"},;
        },;
        expected: "<bos><|turn>user\nHi<turn|>\n" +;
        "<|turn>model\n<turn|>\n" +;
        "<|turn>user\nMore<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "thinking_with_tool_calls",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "List files"},;
        {Role: "assistant", Content: "<|channel>I should use bash<channel|>", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "file1.txt"},;
        {Role: "assistant", Content: "Here are the files."},;
        {Role: "user", Content: "Thanks"},;
        },;
        tools: bashSmallTool(),;
        think: thinkTrue(),;
        expected: "<bos><|turn>system\n<|think|>\nYou are helpful." + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nList files<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "file1.txt" + q + "}<tool_response|>" +;
        "<turn|>\nHere are the files.<turn|>\n" +;
        "<|turn>user\nThanks<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name:     "array_without_items",;
        messages: []api.Message{{Role: "user", Content: "Tag"}},;
        tools:    arrayNoItemsTool(),;
        expected: "<bos><|turn>system\n" + arrayNoItemsDeclRef + "<turn|>\n" +;
        "<|turn>user\nTag<turn|>\n<|turn>model\n",;
        },;
        {
        name:     "object_no_desc_with_properties",;
        messages: []api.Message{{Role: "user", Content: "Update"}},;
        tools:    objectNoDescTool(),;
        expected: "<bos><|turn>system\n" + objectNoDescDeclRef + "<turn|>\n" +;
        "<|turn>user\nUpdate<turn|>\n<|turn>model\n",;
        },;
        {
        name: "chained_tool_calls",;
        messages: []api.Message{
        {Role: "user", Content: "Set up the project"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "mkdir src"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: ""},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "touch src/main.go"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: ""},;
        {Role: "assistant", Content: "Done."},;
        {Role: "user", Content: "Thanks"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nSet up the project<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "mkdir src" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + q + "}<tool_response|>" +;
        "<|tool_call>call:bash{command:" + q + "touch src/main.go" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + q + "}<tool_response|>" +;
        "Done.<turn|>\n" +;
        "<|turn>user\nThanks<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "tool_call_thinking_with_remaining_content",;
        messages: []api.Message{
        {Role: "user", Content: "List files"},;
        {Role: "assistant", Content: "<|channel>I need to check the directory<channel|>Let me list the files.", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "main.go\ngo.mod"},;
        {Role: "user", Content: "OK"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nList files<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "ls" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "main.go\ngo.mod" + q + "}<tool_response|>" +;
        "Let me list the files.<turn|>\n" +;
        "<|turn>user\nOK<turn|>\n" +;
        "<|turn>model\n",;
        },;
        {
        name: "argument_with_newlines",;
        messages: []api.Message{
        {Role: "user", Content: "Run it"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "echo hello\necho world"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "hello\nworld"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nRun it<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + "echo hello\necho world" + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "hello\nworld" + q + "}<tool_response|>",;
        },;
        {
        name: "empty_string_argument",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": ""})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "error"},;
        },;
        tools: bashSmallTool(),;
        expected: "<bos><|turn>system\n" + bashSmallDeclRef + "<turn|>\n" +;
        "<|turn>user\nGo<turn|>\n" +;
        "<|turn>model\n<|tool_call>call:bash{command:" + q + q + "}<tool_call|>" +;
        "<|tool_response>response:bash{value:" + q + "error" + q + "}<tool_response|>",;
        },;
    }
        var verifyJinja2 = os.Getenv("VERIFY_JINJA2") != "";
        if verifyJinja2 {
        var if err = exec.Command("uv", "run", "--with", "jinja2", "python", "-c", "import jinja2").Run(); err != null {
        t.Fatal("VERIFY_JINJA2=1 requires uv and the ability to run uv with jinja2");
    }
        t.Log("VERIFY_JINJA2=1: verifying expected values against Jinja2 template");
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var renderer = &Gemma4Renderer{useImgTags: RenderImgTags}
        var got, err = renderer.Render(tt.messages, tt.tools, tt.think);
        assert.NoError(t, err);
        assert.Equal(t, tt.expected, got);
        if verifyJinja2 && !tt.skipJinja2 {
        var jinja2Output = renderWithJinja2(t, tt.messages, tt.tools, tt.think);
        if jinja2Output != tt.expected || jinja2Output != got {
        fmt.Fprintf(os.Stderr, "\nJINJA2 OUTPUT for %s (copy-paste as expected):\n%q\n\n", tt.name, jinja2Output);
    }
        assert.Equal(t, jinja2Output, tt.expected,;
        "hardcoded expected value doesn't match Jinja2 template output");
        assert.Equal(t, jinja2Output, got,;
        "renderer output doesn't match Jinja2 template output");
    }
        });
    }
    }

    public static void TestGemma4RendererVariantsMatchExpectedGenerationPrompt(*testing.T t) {
        var messages = []api.Message{{Role: "user", Content: "Hello"}}
        var tests = []struct {
        name         String;
        rendererName String;
        expected     String;
        }{
        {
        name:         "legacy_alias",;
        rendererName: "gemma4",;
        expected:     "<bos><|turn>user\nHello<turn|>\n<|turn>model\n",;
        },;
        {
        name:         "small",;
        rendererName: "gemma4-small",;
        expected:     "<bos><|turn>user\nHello<turn|>\n<|turn>model\n",;
        },;
        {
        name:         "large",;
        rendererName: "gemma4-large",;
        expected:     "<bos><|turn>user\nHello<turn|>\n<|turn>model\n<|channel>thought\n<channel|>",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got, err = RenderWithRenderer(tt.rendererName, messages, null, null);
        assert.NoError(t, err);
        assert.Equal(t, tt.expected, got);
        });
    }
    }

    public static void TestGemma4LargeRendererOmitsEmptyThoughtBlockWhenThinkingEnabled(*testing.T t) {
        var got, err = RenderWithRenderer("gemma4-large", []api.Message{{Role: "user", Content: "Hello"}}, null, thinkTrue());
        assert.NoError(t, err);
        assert.Equal(t, "<bos><|turn>system\n<|think|>\n<turn|>\n<|turn>user\nHello<turn|>\n<|turn>model\n", got);
        assert.NotContains(t, got, "<|channel>thought\n<channel|>");
    }

    public static void TestGemma4RendererMatchesJinja2ExpandedParity(*testing.T t) {
        if os.Getenv("VERIFY_JINJA2") == "" {
        t.Skip("set VERIFY_JINJA2=1 to run expanded Jinja2 parity checks");
    }
        var if err = exec.Command("uv", "run", "--with", "jinja2", "python", "-c", "import jinja2").Run(); err != null {
        t.Fatal("VERIFY_JINJA2=1 requires uv and the ability to run uv with jinja2");
    }
        var tests = []struct {
        name     String;
        messages []api.Message;
        tools    []api.Tool;
        think    *api.ThinkValue;
        }{
        {
        name: "adjacent_assistants_continue_same_model_turn",;
        messages: []api.Message{
        {Role: "user", Content: "Start"},;
        {Role: "assistant", Content: "One."},;
        {Role: "assistant", Content: "Two."},;
        {Role: "user", Content: "More"},;
        },;
        },;
        {
        name: "thinking_field_on_pending_tool_call",;
        messages: []api.Message{
        {Role: "user", Content: "List files"},;
        {Role: "assistant", Content: "Let me check.", Thinking: "I should use bash", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        },;
        tools: bashRefTool(),;
        },;
        {
        name: "thinking_field_ignored_before_later_user",;
        messages: []api.Message{
        {Role: "user", Content: "List files"},;
        {Role: "assistant", Content: "Let me check.", Thinking: "I should use bash", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "file1.txt"},;
        {Role: "user", Content: "Thanks"},;
        },;
        tools: bashRefTool(),;
        },;
        {
        name: "tool_response_name_resolved_from_tool_call_id",;
        messages: []api.Message{
        {Role: "user", Content: "List and read"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{
        {
        ID:       "call_bash",;
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        },;
        {
        ID:       "call_read",;
        Function: api.ToolCallFunction{Name: "read", Arguments: testArgs(map[String]any{"path": "go.mod"})},;
        },;
        }},;
        {Role: "tool", ToolCallID: "call_read", Content: "module example.com/foo"},;
        {Role: "tool", ToolCallID: "call_bash", Content: "file1.txt\nfile2.txt"},;
        },;
        tools: bashAndReadRefTools(),;
        },;
        {
        name: "adjacent_assistants_after_tool_response_continue_same_model_turn",;
        messages: []api.Message{
        {Role: "user", Content: "Go"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        {Role: "tool", ToolName: "bash", Content: "file1.txt"},;
        {Role: "assistant", Content: "First."},;
        {Role: "assistant", Content: "Second."},;
        {Role: "user", Content: "Next"},;
        },;
        tools: bashSmallTool(),;
        },;
        {
        name: "thinking_enabled_with_pending_tool_call",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "List files"},;
        {Role: "assistant", Thinking: "Use bash", ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        }}},;
        },;
        tools: bashSmallTool(),;
        think: thinkTrue(),;
        },;
        {
        name: "array_items_object_with_required",;
        messages: []api.Message{
        {Role: "user", Content: "Upsert entries"},;
        },;
        tools: arrayObjectItemsTool(),;
        },;
        {
        name: "array_items_object_with_nested_object_and_array_properties",;
        messages: []api.Message{
        {Role: "user", Content: "Plan steps"},;
        },;
        tools: arrayNestedItemsTool(),;
        },;
        {
        name: "array_items_nested_array_of_objects",;
        messages: []api.Message{
        {Role: "user", Content: "Schedule jobs"},;
        },;
        tools: nestedArrayObjectItemsTool(),;
        },;
        {
        name: "array_items_union_type",;
        messages: []api.Message{
        {Role: "user", Content: "Maybe batch"},;
        },;
        tools: arrayUnionItemsTool(),;
        },;
        {
        name: "array_items_nested_nullable_properties",;
        messages: []api.Message{
        {Role: "user", Content: "Annotate batch"},;
        },;
        tools: arrayNullableNestedItemsTool(),;
        },;
        {
        name: "array_items_extra_keys",;
        messages: []api.Message{
        {Role: "user", Content: "Configure batch"},;
        },;
        tools: arrayItemsExtraKeysTool(),;
        },;
        {
        name: "typed_property_union_type",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        },;
        tools: unionTopLevelTypeTool(),;
        },;
    }
        var variants = []struct {
        name        String;
        renderer    *Gemma4Renderer;
        templateRel String;
        }{
        {
        name:        "small",;
        renderer:    &Gemma4Renderer{useImgTags: RenderImgTags},;
        templateRel: gemma4E2BTemplate,;
        },;
        {
        name:        "large",;
        renderer:    &Gemma4Renderer{useImgTags: RenderImgTags, emptyBlockOnNothink: true},;
        templateRel: gemma431BTemplate,;
        },;
    }
        var for _, variant = range variants {
        t.Run(variant.name, func(t *testing.T) {
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got, err = variant.renderer.Render(tt.messages, tt.tools, tt.think);
        assert.NoError(t, err);
        var jinja2Output = renderWithJinja2Template(t, variant.templateRel, tt.messages, tt.tools, tt.think);
        assert.Equal(t, jinja2Output, got,;
        "renderer output doesn't match Jinja2 template output");
        });
    }
        });
    }
    }

    public static void TestGemma4RendererKnownJinja2Differences(*testing.T t) {
        if os.Getenv("VERIFY_JINJA2") == "" {
        t.Skip("set VERIFY_JINJA2=1 to run Jinja2 difference checks");
    }
        var if err = exec.Command("uv", "run", "--with", "jinja2", "python", "-c", "import jinja2").Run(); err != null {
        t.Fatal("VERIFY_JINJA2=1 requires uv and the ability to run uv with jinja2");
    }
        var tests = []struct {
        name           String;
        messages       []api.Message;
        tools          []api.Tool;
        wantJinjaFrag  String;
        wantRenderFrag String;
        }{
        {
        name: "typed_property_anyof",;
        messages: []api.Message{
        {Role: "user", Content: "Pick"},;
        },;
        tools:          anyOfTool(),;
        wantJinjaFrag:  `value:{description:<|"|>Value<|"|>,type:<|"|><|"|>}`,;
        wantRenderFrag: `value:{description:<|"|>Value<|"|>,type:<|"|>['STRING', 'NUMBER']<|"|>}`,;
        },;
        {
        name: "tool_response_name_not_overridden_without_tool_call_id",;
        messages: []api.Message{
        {Role: "user", Content: "List and read"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        },;
        {
        Function: api.ToolCallFunction{Name: "read", Arguments: testArgs(map[String]any{"path": "go.mod"})},;
        },;
        }},;
        {Role: "tool", ToolName: "bash", Content: "payload"},;
        },;
        tools:          bashAndReadRefTools(),;
        wantJinjaFrag:  `response:read{value:<|"|>payload<|"|>}`,;
        wantRenderFrag: `response:bash{value:<|"|>payload<|"|>}`,;
        },;
        {
        name: "tool_response_without_name_or_id_uses_unknown",;
        messages: []api.Message{
        {Role: "user", Content: "List and read"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        },;
        {
        Function: api.ToolCallFunction{Name: "read", Arguments: testArgs(map[String]any{"path": "go.mod"})},;
        },;
        }},;
        {Role: "tool", Content: "payload"},;
        },;
        tools:          bashAndReadRefTools(),;
        wantJinjaFrag:  `response:read{value:<|"|>payload<|"|>}`,;
        wantRenderFrag: `response:unknown{value:<|"|>payload<|"|>}`,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var renderer = &Gemma4Renderer{useImgTags: RenderImgTags}
        var got, err = renderer.Render(tt.messages, tt.tools, null);
        assert.NoError(t, err);
        var jinja2Output = renderWithJinja2(t, tt.messages, tt.tools, null);
        assert.NotEqual(t, jinja2Output, got, "case no longer differs from Jinja2 output");
        assert.Contains(t, jinja2Output, tt.wantJinjaFrag);
        assert.Contains(t, got, tt.wantRenderFrag);
        });
    }
    }

    public static void TestGemma4RendererNormalizesSimpleAnyOfToTypedUnion(*testing.T t) {
        var renderer = &Gemma4Renderer{useImgTags: RenderImgTags}
        var got, err = renderer.Render([]api.Message{{Role: "user", Content: "Pick"}}, anyOfTool(), null);
        assert.NoError(t, err);
        assert.Contains(t, got, `value:{description:<|"|>Value<|"|>,type:<|"|>['STRING', 'NUMBER']<|"|>}`);
    }

    public static void TestGemma4RendererToolResponseWithoutNameOrIDUsesUnknown(*testing.T t) {
        var renderer = &Gemma4Renderer{useImgTags: RenderImgTags}
        var got, err = renderer.Render([]api.Message{
        {Role: "user", Content: "List and read"},;
        {Role: "assistant", ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{Name: "bash", Arguments: testArgs(map[String]any{"command": "ls"})},;
        },;
        {
        Function: api.ToolCallFunction{Name: "read", Arguments: testArgs(map[String]any{"path": "go.mod"})},;
        },;
        }},;
        {Role: "tool", Content: "payload"},;
        }, bashAndReadRefTools(), null);
        assert.NoError(t, err);
        assert.Contains(t, got, `response:unknown{value:<|"|>payload<|"|>}`);
        assert.NotContains(t, got, `response:read{value:<|"|>payload<|"|>}`);
    }

    public static void TestGemma4SizeTemplateFixturesDifferAtGenerationPrompt(*testing.T t) {
        var e2b, err = os.ReadFile(gemma4E2BTemplate);
        if err != null {
        t.Fatalf("failed to read %s: %v", gemma4E2BTemplate, err);
    }
        var thirtyOneB, err = os.ReadFile(gemma431BTemplate);
        if err != null {
        t.Fatalf("failed to read %s: %v", gemma431BTemplate, err);
    }
        assert.Contains(t, String(e2b), "{{- '<|turn>model\\n' -}}");
        assert.NotContains(t, String(e2b), "{{- '<|channel>thought\\n<channel|>' -}}");
        assert.Contains(t, String(thirtyOneB), "{{- '<|turn>model\\n' -}}");
        assert.Contains(t, String(thirtyOneB), "{{- '<|channel>thought\\n<channel|>' -}}");
    }

    public static String renderWithJinja2(*testing.T t, []api.Message messages, []api.Tool tools, *api.ThinkValue think) {
        return renderWithJinja2Template(t, gemma4E2BTemplate, messages, tools, think);
    }

    public static String renderWithJinja2Template(*testing.T t, String templateRelPath, []api.Message messages, []api.Tool tools, *api.ThinkValue think) {
        t.Helper();
        var templatePath, err = filepath.Abs(templateRelPath);
        if err != null {
        t.Fatalf("failed to get template path: %v", err);
    }

    public static class jinja2ToolCall {
        public String ID;
        public struct Function;
        public String Name;
        public any Arguments;
        public `json:"function"` };
    }

    public static class jinja2Message {
        public String Role;
        public String Content;
        public String Reasoning;
        public []jinja2ToolCall ToolCalls;
        public String Name;
        public String ToolCallID;
    }
        var jMsgs []jinja2Message;
        var for _, m = range messages {
        var jm = jinja2Message{
        Role:       m.Role,;
        Content:    m.Content,;
        Reasoning:  m.Thinking,;
        Name:       m.ToolName,;
        ToolCallID: m.ToolCallID,;
    }
        var for _, tc = range m.ToolCalls {
        var jtc = jinja2ToolCall{}
        jtc.ID = tc.ID;
        jtc.Function.Name = tc.Function.Name;
        var args map[String]any;
        var raw, _ = tc.Function.Arguments.MarshalJSON();
        json.Unmarshal(raw, &args);
        jtc.Function.Arguments = args;
        jm.ToolCalls = append(jm.ToolCalls, jtc);
    }
        jMsgs = append(jMsgs, jm);
    }
        var msgsJSON, err = json.Marshal(jMsgs);
        if err != null {
        t.Fatalf("failed to marshal messages: %v", err);
    }
        var toolsJSON = "None";
        if len(tools) > 0 {
        var b, _ = json.Marshal(tools);
        toolsJSON = String(b);
    }
        var thinking = "False";
        if think != null && think.Bool() {
        thinking = "True";
    }
        var script = fmt.Sprintf(`;
        from jinja2 import Environment;
        tmpl = Environment().from_string(open(%q).read());
        msgs = json.loads(%q);
        tools = json.loads(%q) if %q != "None" else None;
        kwargs = {"messages": msgs, "bos_token": "<bos>", "add_generation_prompt": True}
        if tools:;
        kwargs["tools"] = tools;
        if %s:;
        kwargs["enable_thinking"] = True;
        print(tmpl.render(**kwargs), end="");
        `, templatePath, String(msgsJSON), toolsJSON, toolsJSON, thinking);
        var cmd = exec.Command("uv", "run", "--with", "jinja2", "python", "-c", script);
        var stdout, stderr strings.Builder;
        cmd.Stdout = &stdout;
        cmd.Stderr = &stderr;
        var if err = cmd.Run(); err != null {
        t.Fatalf("uv run failed: %v\nstderr: %s", err, stderr.String());
    }
        return stdout.String();
    }
        func thinkTrue() *api.ThinkValue {
        return &api.ThinkValue{Value: true}
    }
        func thinkFalse() *api.ThinkValue {
        return &api.ThinkValue{Value: false}
    }
}
