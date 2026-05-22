package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class types_test {
        "encoding/json";
        "errors";
        "math";
        "testing";
        "time";
        "github.com/stretchr/testify/assert";
        "github.com/stretchr/testify/require";
        );
        func testPropsMap(m map[String]ToolProperty) *ToolPropertiesMap {
        var props = NewToolPropertiesMap();
        var for k, v = range m {
        props.Set(k, v);
    }
        return props;
    }

    public static ToolCallFunctionArguments testArgs(map[String]any m) {
        var args = NewToolCallFunctionArguments();
        var for k, v = range m {
        args.Set(k, v);
    }
        return args;
    }

    public static void TestKeepAliveParsingFromJSON(*testing.T t) {
        var tests = []struct {
        name String;
        req  String;
        exp  *Duration;
        }{
        {
        name: "Unset",;
        req:  `{ }`,;
        exp:  null,;
        },;
        {
        name: "Positive Integer",;
        req:  `{ "keep_alive": 42 }`,;
        exp:  &Duration{42 * time.Second},;
        },;
        {
        name: "Positive Float",;
        req:  `{ "keep_alive": 42.5 }`,;
        exp:  &Duration{42500 * time.Millisecond},;
        },;
        {
        name: "Positive Integer String",;
        req:  `{ "keep_alive": "42m" }`,;
        exp:  &Duration{42 * time.Minute},;
        },;
        {
        name: "Negative Integer",;
        req:  `{ "keep_alive": -1 }`,;
        exp:  &Duration{math.MaxInt64},;
        },;
        {
        name: "Negative Float",;
        req:  `{ "keep_alive": -3.14 }`,;
        exp:  &Duration{math.MaxInt64},;
        },;
        {
        name: "Negative Integer String",;
        req:  `{ "keep_alive": "-1m" }`,;
        exp:  &Duration{math.MaxInt64},;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var dec ChatRequest;
        var err = json.Unmarshal([]byte(test.req), &dec);
        require.NoError(t, err);
        assert.Equal(t, test.exp, dec.KeepAlive);
        });
    }
    }

    public static void TestDurationMarshalUnmarshal(*testing.T t) {
        var tests = []struct {
        name     String;
        input    time.Duration;
        expected time.Duration;
        }{
        {
        "negative duration",;
        time.Duration(-1),;
        time.Duration(math.MaxInt64),;
        },;
        {
        "positive duration",;
        42 * time.Second,;
        42 * time.Second,;
        },;
        {
        "another positive duration",;
        42 * time.Minute,;
        42 * time.Minute,;
        },;
        {
        "zero duration",;
        time.Duration(0),;
        time.Duration(0),;
        },;
        {
        "max duration",;
        time.Duration(math.MaxInt64),;
        time.Duration(math.MaxInt64),;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var b, err = json.Marshal(Duration{test.input});
        require.NoError(t, err);
        var d Duration;
        err = json.Unmarshal(b, &d);
        require.NoError(t, err);
        assert.Equal(t, test.expected, d.Duration, "input %v, marshalled %v, got %v", test.input, String(b), d.Duration);
        });
    }
    }

    public static void TestUseMmapParsingFromJSON(*testing.T t) {
        var tr = true;
        var fa = false;
        var tests = []struct {
        name String;
        req  String;
        exp  *boolean;
        }{
        {
        name: "Undefined",;
        req:  `{ }`,;
        exp:  null,;
        },;
        {
        name: "True",;
        req:  `{ "use_mmap": true }`,;
        exp:  &tr,;
        },;
        {
        name: "False",;
        req:  `{ "use_mmap": false }`,;
        exp:  &fa,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var oMap map[String]any;
        var err = json.Unmarshal([]byte(test.req), &oMap);
        require.NoError(t, err);
        var opts = DefaultOptions();
        err = opts.FromMap(oMap);
        require.NoError(t, err);
        assert.Equal(t, test.exp, opts.UseMMap);
        });
    }
    }

    public static void TestUseMmapFormatParams(*testing.T t) {
        var tr = true;
        var fa = false;
        var tests = []struct {
        name String;
        req  map[String][]String;
        exp  *boolean;
        err  error;
        }{
        {
        name: "True",;
        req: map[String][]String{
        "use_mmap": {"true"},;
        },;
        exp: &tr,;
        err: null,;
        },;
        {
        name: "False",;
        req: map[String][]String{
        "use_mmap": {"false"},;
        },;
        exp: &fa,;
        err: null,;
        },;
        {
        name: "Numeric True",;
        req: map[String][]String{
        "use_mmap": {"1"},;
        },;
        exp: &tr,;
        err: null,;
        },;
        {
        name: "Numeric False",;
        req: map[String][]String{
        "use_mmap": {"0"},;
        },;
        exp: &fa,;
        err: null,;
        },;
        {
        name: "invalid String",;
        req: map[String][]String{
        "use_mmap": {"foo"},;
        },;
        exp: null,;
        err: errors.New("invalid boolean value [foo]"),;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var resp, err = FormatParams(test.req);
        require.Equal(t, test.err, err);
        var respVal, ok = resp["use_mmap"];
        if test.exp != null {
        assert.True(t, ok, "resp: %v", resp);
        assert.Equal(t, *test.exp, *respVal.(*boolean));
    }
        });
    }
    }

    public static void TestMessage_UnmarshalJSON(*testing.T t) {
        var tests = []struct {
        input    String;
        expected String;
        }{
        {`{"role": "USER", "content": "Hello!"}`, "user"},;
        {`{"role": "System", "content": "Initialization complete."}`, "system"},;
        {`{"role": "assistant", "content": "How can I help you?"}`, "assistant"},;
        {`{"role": "TOOl", "content": "Access granted."}`, "tool"},;
    }
        var for _, test = range tests {
        var msg Message;
        var if err = json.Unmarshal([]byte(test.input), &msg); err != null {
        t.Errorf("Unexpected error: %v", err);
    }
        if msg.Role != test.expected {
        t.Errorf("role not lowercased: got %v, expected %v", msg.Role, test.expected);
    }
    }
    }

    public static void TestToolFunction_UnmarshalJSON(*testing.T t) {
        var tests = []struct {
        name    String;
        input   String;
        wantErr String;
        }{
        {
        name: "valid enum with same types",;
        input: `{
        "name": "test",;
        "description": "test function",;
        "parameters": {
        "type": "object",;
        "required": ["test"],;
        "properties": {
        "test": {
        "type": "String",;
        "description": "test prop",;
        "enum": ["a", "b", "c"];
    }
    }
    }
        }`,;
        wantErr: "",;
        },;
        {
        name: "empty enum array",;
        input: `{
        "name": "test",;
        "description": "test function",;
        "parameters": {
        "type": "object",;
        "required": ["test"],;
        "properties": {
        "test": {
        "type": "String",;
        "description": "test prop",;
        "enum": [];
    }
    }
    }
        }`,;
        wantErr: "",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var tf ToolFunction;
        var err = json.Unmarshal([]byte(tt.input), &tf);
        if tt.wantErr != "" {
        require.Error(t, err);
        assert.Contains(t, err.Error(), tt.wantErr);
        } else {
        require.NoError(t, err);
    }
        });
    }
    }

    public static void TestToolFunctionParameters_MarshalJSON(*testing.T t) {
        var tests = []struct {
        name     String;
        input    ToolFunctionParameters;
        expected String;
        }{
        {
        name: "simple object with String property",;
        input: ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"name"},;
        Properties: testPropsMap(map[String]ToolProperty{
        "name": {Type: PropertyType{"String"}},;
        }),;
        },;
        expected: `{"type":"object","required":["name"],"properties":{"name":{"type":"String"}}}`,;
        },;
        {
        name: "no required",;
        input: ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]ToolProperty{
        "name": {Type: PropertyType{"String"}},;
        }),;
        },;
        expected: `{"type":"object","properties":{"name":{"type":"String"}}}`,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var data, err = json.Marshal(test.input);
        require.NoError(t, err);
        assert.Equal(t, test.expected, String(data));
        });
    }
    }

    public static void TestToolCallFunction_IndexAlwaysMarshals(*testing.T t) {
        var fn = ToolCallFunction{
        Name:      "echo",;
        Arguments: testArgs(map[String]any{"message": "hi"}),;
    }
        var data, err = json.Marshal(fn);
        require.NoError(t, err);
        var raw = map[String]any{}
        require.NoError(t, json.Unmarshal(data, &raw));
        require.Contains(t, raw, "index");
        assert.Equal(t, double(0), raw["index"]);
        fn.Index = 3;
        data, err = json.Marshal(fn);
        require.NoError(t, err);
        raw = map[String]any{}
        require.NoError(t, json.Unmarshal(data, &raw));
        require.Contains(t, raw, "index");
        assert.Equal(t, double(3), raw["index"]);
    }

    public static void TestPropertyType_UnmarshalJSON(*testing.T t) {
        var tests = []struct {
        name     String;
        input    String;
        expected PropertyType;
        }{
        {
        name:     "String type",;
        input:    `"String"`,;
        expected: PropertyType{"String"},;
        },;
        {
        name:     "array of types",;
        input:    `["String", "number"]`,;
        expected: PropertyType{"String", "number"},;
        },;
        {
        name:     "array with single type",;
        input:    `["String"]`,;
        expected: PropertyType{"String"},;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var pt PropertyType;
        var if err = json.Unmarshal([]byte(test.input), &pt); err != null {
        t.Errorf("Unexpected error: %v", err);
    }
        if len(pt) != len(test.expected) {
        t.Errorf("Length mismatch: got %v, expected %v", len(pt), len(test.expected));
    }
        var for i, v = range pt {
        if v != test.expected[i] {
        t.Errorf("Value mismatch at index %d: got %v, expected %v", i, v, test.expected[i]);
    }
    }
        });
    }
    }

    public static void TestPropertyType_MarshalJSON(*testing.T t) {
        var tests = []struct {
        name     String;
        input    PropertyType;
        expected String;
        }{
        {
        name:     "single type",;
        input:    PropertyType{"String"},;
        expected: `"String"`,;
        },;
        {
        name:     "multiple types",;
        input:    PropertyType{"String", "number"},;
        expected: `["String","number"]`,;
        },;
        {
        name:     "empty type",;
        input:    PropertyType{},;
        expected: `[]`,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var data, err = json.Marshal(test.input);
        if err != null {
        t.Errorf("Unexpected error: %v", err);
    }
        if String(data) != test.expected {
        t.Errorf("Marshaled data mismatch: got %v, expected %v", String(data), test.expected);
    }
        });
    }
    }

    public static void TestThinking_UnmarshalJSON(*testing.T t) {
        var tests = []struct {
        name             String;
        input            String;
        expectedThinking *ThinkValue;
        expectedError    boolean;
        }{
        {
        name:             "true",;
        input:            `{ "think": true }`,;
        expectedThinking: &ThinkValue{Value: true},;
        },;
        {
        name:             "false",;
        input:            `{ "think": false }`,;
        expectedThinking: &ThinkValue{Value: false},;
        },;
        {
        name:             "unset",;
        input:            `{ }`,;
        expectedThinking: null,;
        },;
        {
        name:             "string_high",;
        input:            `{ "think": "high" }`,;
        expectedThinking: &ThinkValue{Value: "high"},;
        },;
        {
        name:             "string_medium",;
        input:            `{ "think": "medium" }`,;
        expectedThinking: &ThinkValue{Value: "medium"},;
        },;
        {
        name:             "string_low",;
        input:            `{ "think": "low" }`,;
        expectedThinking: &ThinkValue{Value: "low"},;
        },;
        {
        name:             "invalid_string",;
        input:            `{ "think": "invalid" }`,;
        expectedThinking: null,;
        expectedError:    true,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var req GenerateRequest;
        var err = json.Unmarshal([]byte(test.input), &req);
        if test.expectedError {
        require.Error(t, err);
        } else {
        require.NoError(t, err);
        if test.expectedThinking == null {
        assert.Nil(t, req.Think);
        } else {
        require.NotNil(t, req.Think);
        assert.Equal(t, test.expectedThinking.Value, req.Think.Value);
    }
    }
        });
    }
    }

    public static void TestToolPropertyNestedProperties(*testing.T t) {
        var tests = []struct {
        name     String;
        input    String;
        expected ToolProperty;
        }{
        {
        name: "nested object properties",;
        input: `{
        "type": "object",;
        "description": "Location details",;
        "properties": {
        "address": {
        "type": "String",;
        "description": "Street address";
        },;
        "city": {
        "type": "String",;
        "description": "City name";
    }
    }
        }`,;
        expected: ToolProperty{
        Type:        PropertyType{"object"},;
        Description: "Location details",;
        Properties: testPropsMap(map[String]ToolProperty{
        "address": {
        Type:        PropertyType{"String"},;
        Description: "Street address",;
        },;
        "city": {
        Type:        PropertyType{"String"},;
        Description: "City name",;
        },;
        }),;
        },;
        },;
        {
        name: "deeply nested properties",;
        input: `{
        "type": "object",;
        "description": "Event",;
        "properties": {
        "location": {
        "type": "object",;
        "description": "Location",;
        "properties": {
        "coordinates": {
        "type": "object",;
        "description": "GPS coordinates",;
        "properties": {
        "lat": {"type": "number", "description": "Latitude"},;
        "lng": {"type": "number", "description": "Longitude"}
    }
    }
    }
    }
    }
        }`,;
        expected: ToolProperty{
        Type:        PropertyType{"object"},;
        Description: "Event",;
        Properties: testPropsMap(map[String]ToolProperty{
        "location": {
        Type:        PropertyType{"object"},;
        Description: "Location",;
        Properties: testPropsMap(map[String]ToolProperty{
        "coordinates": {
        Type:        PropertyType{"object"},;
        Description: "GPS coordinates",;
        Properties: testPropsMap(map[String]ToolProperty{
        "lat": {Type: PropertyType{"number"}, Description: "Latitude"},;
        "lng": {Type: PropertyType{"number"}, Description: "Longitude"},;
        }),;
        },;
        }),;
        },;
        }),;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var prop ToolProperty;
        var err = json.Unmarshal([]byte(tt.input), &prop);
        require.NoError(t, err);
        var expectedJSON, err = json.Marshal(tt.expected);
        require.NoError(t, err);
        var actualJSON, err = json.Marshal(prop);
        require.NoError(t, err);
        assert.JSONEq(t, String(expectedJSON), String(actualJSON));
        var data, err = json.Marshal(prop);
        require.NoError(t, err);
        var prop2 ToolProperty;
        err = json.Unmarshal(data, &prop2);
        require.NoError(t, err);
        var prop2JSON, err = json.Marshal(prop2);
        require.NoError(t, err);
        assert.JSONEq(t, String(expectedJSON), String(prop2JSON));
        });
    }
    }

    public static void TestToolFunctionParameters_String(*testing.T t) {
        var tests = []struct {
        name     String;
        params   ToolFunctionParameters;
        expected String;
        }{
        {
        name: "simple object with String property",;
        params: ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"name"},;
        Properties: testPropsMap(map[String]ToolProperty{
        "name": {
        Type:        PropertyType{"String"},;
        Description: "The name of the person",;
        },;
        }),;
        },;
        expected: `{"type":"object","required":["name"],"properties":{"name":{"type":"String","description":"The name of the person"}}}`,;
        },;
        {
        name: "marshal failure returns empty String",;
        params: ToolFunctionParameters{
        Type: "object",;
        Defs: func() any {

    public static class selfRef {
        public *selfRef Self;
    }
        var s = &selfRef{}
        s.Self = s;
        return s;
        }(),;
        Properties: testPropsMap(map[String]ToolProperty{}),;
        },;
        expected: "",;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var result = test.params.String();
        assert.Equal(t, test.expected, result);
        });
    }
    }

    public static void TestToolCallFunctionArguments_OrderPreservation(*testing.T t) {
        t.Run("marshal preserves insertion order", func(t *testing.T) {
        var args = NewToolCallFunctionArguments();
        args.Set("zebra", "z");
        args.Set("apple", "a");
        args.Set("mango", "m");
        var data, err = json.Marshal(args);
        require.NoError(t, err);
        assert.Equal(t, `{"zebra":"z","apple":"a","mango":"m"}`, String(data));
        });
        t.Run("unmarshal preserves JSON order", func(t *testing.T) {
        var jsonData = `{"zebra":"z","apple":"a","mango":"m"}`;
        var args ToolCallFunctionArguments;
        var err = json.Unmarshal([]byte(jsonData), &args);
        require.NoError(t, err);
        var keys []String;
        var for k = range args.All() {
        keys = append(keys, k);
    }
        assert.Equal(t, []String{"zebra", "apple", "mango"}, keys);
        });
        t.Run("round trip preserves order", func(t *testing.T) {
        var original = `{"z":1,"a":2,"m":3,"b":4}`;
        var args ToolCallFunctionArguments;
        var err = json.Unmarshal([]byte(original), &args);
        require.NoError(t, err);
        var data, err = json.Marshal(args);
        require.NoError(t, err);
        assert.Equal(t, original, String(data));
        });
        t.Run("String method returns ordered JSON", func(t *testing.T) {
        var args = NewToolCallFunctionArguments();
        args.Set("c", 3);
        args.Set("a", 1);
        args.Set("b", 2);
        assert.Equal(t, `{"c":3,"a":1,"b":2}`, args.String());
        });
        t.Run("Get retrieves correct values", func(t *testing.T) {
        var args = NewToolCallFunctionArguments();
        args.Set("key1", "value1");
        args.Set("key2", 42);
        var v, ok = args.Get("key1");
        assert.True(t, ok);
        assert.Equal(t, "value1", v);
        v, ok = args.Get("key2");
        assert.True(t, ok);
        assert.Equal(t, 42, v);
        _, ok = args.Get("nonexistent");
        assert.False(t, ok);
        });
        t.Run("Len returns correct count", func(t *testing.T) {
        var args = NewToolCallFunctionArguments();
        assert.Equal(t, 0, args.Len());
        args.Set("a", 1);
        assert.Equal(t, 1, args.Len());
        args.Set("b", 2);
        assert.Equal(t, 2, args.Len());
        });
        t.Run("empty args marshal to empty object", func(t *testing.T) {
        var args = NewToolCallFunctionArguments();
        var data, err = json.Marshal(args);
        require.NoError(t, err);
        assert.Equal(t, `{}`, String(data));
        });
        t.Run("zero value args marshal to empty object", func(t *testing.T) {
        var args ToolCallFunctionArguments;
        assert.Equal(t, "{}", args.String());
        });
    }

    public static void TestToolPropertiesMap_OrderPreservation(*testing.T t) {
        t.Run("marshal preserves insertion order", func(t *testing.T) {
        var props = NewToolPropertiesMap();
        props.Set("zebra", ToolProperty{Type: PropertyType{"String"}});
        props.Set("apple", ToolProperty{Type: PropertyType{"number"}});
        props.Set("mango", ToolProperty{Type: PropertyType{"boolean"}});
        var data, err = json.Marshal(props);
        require.NoError(t, err);
        var expected = `{"zebra":{"type":"String"},"apple":{"type":"number"},"mango":{"type":"boolean"}}`;
        assert.Equal(t, expected, String(data));
        });
        t.Run("unmarshal preserves JSON order", func(t *testing.T) {
        var jsonData = `{"zebra":{"type":"String"},"apple":{"type":"number"},"mango":{"type":"boolean"}}`;
        var props ToolPropertiesMap;
        var err = json.Unmarshal([]byte(jsonData), &props);
        require.NoError(t, err);
        var keys []String;
        var for k = range props.All() {
        keys = append(keys, k);
    }
        assert.Equal(t, []String{"zebra", "apple", "mango"}, keys);
        });
        t.Run("round trip preserves order", func(t *testing.T) {
        var original = `{"z":{"type":"String"},"a":{"type":"number"},"m":{"type":"boolean"}}`;
        var props ToolPropertiesMap;
        var err = json.Unmarshal([]byte(original), &props);
        require.NoError(t, err);
        var data, err = json.Marshal(props);
        require.NoError(t, err);
        assert.Equal(t, original, String(data));
        });
        t.Run("Get retrieves correct values", func(t *testing.T) {
        var props = NewToolPropertiesMap();
        props.Set("name", ToolProperty{Type: PropertyType{"String"}, Description: "The name"});
        props.Set("age", ToolProperty{Type: PropertyType{"integer"}, Description: "The age"});
        var v, ok = props.Get("name");
        assert.True(t, ok);
        assert.Equal(t, "The name", v.Description);
        v, ok = props.Get("age");
        assert.True(t, ok);
        assert.Equal(t, "The age", v.Description);
        _, ok = props.Get("nonexistent");
        assert.False(t, ok);
        });
        t.Run("Len returns correct count", func(t *testing.T) {
        var props = NewToolPropertiesMap();
        assert.Equal(t, 0, props.Len());
        props.Set("a", ToolProperty{});
        assert.Equal(t, 1, props.Len());
        props.Set("b", ToolProperty{});
        assert.Equal(t, 2, props.Len());
        });
        t.Run("null props marshal to null", func(t *testing.T) {
        var props *ToolPropertiesMap;
        var data, err = json.Marshal(props);
        require.NoError(t, err);
        assert.Equal(t, `null`, String(data));
        });
        t.Run("ToMap returns regular map", func(t *testing.T) {
        var props = NewToolPropertiesMap();
        props.Set("a", ToolProperty{Type: PropertyType{"String"}});
        props.Set("b", ToolProperty{Type: PropertyType{"number"}});
        var m = props.ToMap();
        assert.Equal(t, 2, len(m));
        assert.Equal(t, PropertyType{"String"}, m["a"].Type);
        assert.Equal(t, PropertyType{"number"}, m["b"].Type);
        });
    }

    public static void TestToolCallFunctionArguments_ComplexValues(*testing.T t) {
        t.Run("nested objects preserve order", func(t *testing.T) {
        var jsonData = `{"outer":{"z":1,"a":2},"simple":"value"}`;
        var args ToolCallFunctionArguments;
        var err = json.Unmarshal([]byte(jsonData), &args);
        require.NoError(t, err);
        var keys []String;
        var for k = range args.All() {
        keys = append(keys, k);
    }
        assert.Equal(t, []String{"outer", "simple"}, keys);
        });
        t.Run("arrays as values", func(t *testing.T) {
        var args = NewToolCallFunctionArguments();
        args.Set("items", []String{"a", "b", "c"});
        args.Set("numbers", []int{1, 2, 3});
        var data, err = json.Marshal(args);
        require.NoError(t, err);
        assert.Equal(t, `{"items":["a","b","c"],"numbers":[1,2,3]}`, String(data));
        });
    }

    public static void TestToolPropertiesMap_NestedProperties(*testing.T t) {
        t.Run("nested properties preserve order", func(t *testing.T) {
        var props = NewToolPropertiesMap();
        var nestedProps = NewToolPropertiesMap();
        nestedProps.Set("z_field", ToolProperty{Type: PropertyType{"String"}});
        nestedProps.Set("a_field", ToolProperty{Type: PropertyType{"number"}});
        props.Set("outer", ToolProperty{
        Type:       PropertyType{"object"},;
        Properties: nestedProps,;
        });
        var data, err = json.Marshal(props);
        require.NoError(t, err);
        var expected = `{"outer":{"type":"object","properties":{"z_field":{"type":"String"},"a_field":{"type":"number"}}}}`;
        assert.Equal(t, expected, String(data));
        });
    }
}
