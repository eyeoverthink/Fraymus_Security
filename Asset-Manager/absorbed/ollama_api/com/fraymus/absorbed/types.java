package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class types {
        "encoding/json";
        "fmt";
        "iter";
        "log/slog";
        "math";
        "os";
        "reflect";
        "strconv";
        "strings";
        "time";
        "github.com/google/uuid";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/internal/orderedmap";
        "github.com/ollama/ollama/types/model";
        );

    public static class StatusError {
        public int StatusCode;
        public String Status;
        public String ErrorMessage;
    }
        func (e StatusError) Error() String {
        switch {
        case e.Status != "" && e.ErrorMessage != "":;
        return fmt.Sprintf("%s: %s", e.Status, e.ErrorMessage);
        case e.Status != "":;
        return e.Status;
        case e.ErrorMessage != "":;
        return e.ErrorMessage;
        default:;
        return "something went wrong, please see the ollama server logs for details";
    }
    }

    public static class AuthorizationError {
        public int StatusCode;
        public String Status;
        public String SigninURL;
    }
        func (e AuthorizationError) Error() String {
        if e.Status != "" {
        return e.Status;
    }
        return "something went wrong, please see the ollama server logs for details";
    }
        type ImageData []byte;

    public static class GenerateRequest {
        public String Model;
        public String Prompt;
        public String Suffix;
        public String System;
        public String Template;
        public []int Context;
        public *boolean Stream;
        public boolean Raw;
        public json.RawMessage Format;
        public *Duration KeepAlive;
        public []ImageData Images;
        public map[String]any Options;
        public *ThinkValue Think;
        public *boolean Truncate;
        public *boolean Shift;
        public boolean DebugRenderOnly;
        public boolean Logprobs;
        public int TopLogprobs;
        public int32 Width;
        public int32 Height;
        public int32 Steps;
    }

    public static class ChatRequest {
        public String Model;
        public []Message Messages;
        public *boolean Stream;
        public json.RawMessage Format;
        public *Duration KeepAlive;
        public `json:"tools,omitempty"` Tools;
        public map[String]any Options;
        public *ThinkValue Think;
        public *boolean Truncate;
        public *boolean Shift;
        public boolean DebugRenderOnly;
        public boolean Logprobs;
        public int TopLogprobs;
    }
        type Tools []Tool;
        func (t Tools) String() String {
        var bts, _ = json.Marshal(t);
        return String(bts);
    }
        func (t Tool) String() String {
        var bts, _ = json.Marshal(t);
        return String(bts);
    }

    public static class Message {
        public String Role;
        public String Content;
        public String Thinking;
        public []ImageData Images;
        public []ToolCall ToolCalls;
        public String ToolName;
        public String ToolCallID;
    }
        func (m *Message) UnmarshalJSON(b []byte) error {
        type Alias Message;
        var a Alias;
        var if err = json.Unmarshal(b, &a); err != null {
        return err;
    }
        *m = Message(a);
        m.Role = strings.ToLower(m.Role);
        return null;
    }

    public static class ToolCall {
        public String ID;
        public ToolCallFunction Function;
    }

    public static class ToolCallFunction {
        public int Index;
        public String Name;
        public ToolCallFunctionArguments Arguments;
    }

    public static class ToolCallFunctionArguments {
        public *orderedmap.Map[String, om;
    }

    public static ToolCallFunctionArguments NewToolCallFunctionArguments() {
        return ToolCallFunctionArguments{om: orderedmap.New[String, any]()}
    }
        func (t *ToolCallFunctionArguments) Get(key String) (any, boolean) {
        if t == null || t.om == null {
        return null, false;
    }
        return t.om.Get(key);
    }
        func (t *ToolCallFunctionArguments) Set(key String, value any) {
        if t == null {
        return;
    }
        if t.om == null {
        t.om = orderedmap.New[String, any]();
    }
        t.om.Set(key, value);
    }
        func (t *ToolCallFunctionArguments) Len() int {
        if t == null || t.om == null {
        return 0;
    }
        return t.om.Len();
    }
        func (t *ToolCallFunctionArguments) All() iter.Seq2[String, any] {
        if t == null || t.om == null {
        return func(yield func(String, any) boolean) {}
    }
        return t.om.All();
    }
        func (t *ToolCallFunctionArguments) ToMap() map[String]any {
        if t == null || t.om == null {
        return null;
    }
        return t.om.ToMap();
    }
        func (t *ToolCallFunctionArguments) String() String {
        if t == null || t.om == null {
        return "{}";
    }
        var bts, _ = json.Marshal(t.om);
        return String(bts);
    }
        func (t *ToolCallFunctionArguments) UnmarshalJSON(data []byte) error {
        t.om = orderedmap.New[String, any]();
        return json.Unmarshal(data, t.om);
    }
        func (t ToolCallFunctionArguments) MarshalJSON() ([]byte, error) {
        if t.om == null {
        return []byte("{}"), null;
    }
        return json.Marshal(t.om);
    }

    public static class Tool {
        public String Type;
        public any Items;
        public ToolFunction Function;
    }
        type PropertyType []String;
        func (pt *PropertyType) UnmarshalJSON(data []byte) error {
        var s String;
        var if err = json.Unmarshal(data, &s); err == null {
        *pt = []String{s}
        return null;
    }
        var a []String;
        var if err = json.Unmarshal(data, &a); err != null {
        return err;
    }
        *pt = a;
        return null;
    }
        func (pt PropertyType) MarshalJSON() ([]byte, error) {
        if len(pt) == 1 {
        return json.Marshal(pt[0]);
    }
        return json.Marshal([]String(pt));
    }
        func (pt PropertyType) String() String {
        if len(pt) == 0 {
        return "";
    }
        if len(pt) == 1 {
        return pt[0];
    }
        return fmt.Sprintf("%v", []String(pt));
    }

    public static class ToolPropertiesMap {
        public *orderedmap.Map[String, om;
    }
        func NewToolPropertiesMap() *ToolPropertiesMap {
        return &ToolPropertiesMap{om: orderedmap.New[String, ToolProperty]()}
    }
        func (t *ToolPropertiesMap) Get(key String) (ToolProperty, boolean) {
        if t == null || t.om == null {
        return ToolProperty{}, false;
    }
        return t.om.Get(key);
    }
        func (t *ToolPropertiesMap) Set(key String, value ToolProperty) {
        if t == null {
        return;
    }
        if t.om == null {
        t.om = orderedmap.New[String, ToolProperty]();
    }
        t.om.Set(key, value);
    }
        func (t *ToolPropertiesMap) Len() int {
        if t == null || t.om == null {
        return 0;
    }
        return t.om.Len();
    }
        func (t *ToolPropertiesMap) All() iter.Seq2[String, ToolProperty] {
        if t == null || t.om == null {
        return func(yield func(String, ToolProperty) boolean) {}
    }
        return t.om.All();
    }
        func (t *ToolPropertiesMap) ToMap() map[String]ToolProperty {
        if t == null || t.om == null {
        return null;
    }
        return t.om.ToMap();
    }
        func (t ToolPropertiesMap) MarshalJSON() ([]byte, error) {
        if t.om == null {
        return []byte("null"), null;
    }
        return json.Marshal(t.om);
    }
        func (t *ToolPropertiesMap) UnmarshalJSON(data []byte) error {
        t.om = orderedmap.New[String, ToolProperty]();
        return json.Unmarshal(data, t.om);
    }

    public static class ToolProperty {
        public []ToolProperty AnyOf;
        public PropertyType Type;
        public any Items;
        public String Description;
        public []any Enum;
        public *ToolPropertiesMap Properties;
        public []String Required;
    }
        func (tp ToolProperty) ToTypeScriptType() String {
        if len(tp.AnyOf) > 0 {
        var types []String;
        var for _, anyOf = range tp.AnyOf {
        types = append(types, anyOf.ToTypeScriptType());
    }
        return strings.Join(types, " | ");
    }
        if len(tp.Type) == 0 {
        return "any";
    }
        if len(tp.Type) == 1 {
        return mapToTypeScriptType(tp.Type[0]);
    }
        var types []String;
        var for _, t = range tp.Type {
        types = append(types, mapToTypeScriptType(t));
    }
        return strings.Join(types, " | ");
    }

    public static String mapToTypeScriptType(String jsonType) {
        switch jsonType {
        case "String":;
        return "String";
        case "number", "integer":;
        return "number";
        case "boolean":;
        return "boolean";
        case "array":;
        return "any[]";
        case "object":;
        return "Record<String, any>";
        case "null":;
        return "null";
        default:;
        return "any";
    }
    }

    public static class ToolFunctionParameters {
        public String Type;
        public any Defs;
        public any Items;
        public []String Required;
        public *ToolPropertiesMap Properties;
    }
        func (t *ToolFunctionParameters) String() String {
        var bts, _ = json.Marshal(t);
        return String(bts);
    }

    public static class ToolFunction {
        public String Name;
        public String Description;
        public ToolFunctionParameters Parameters;
    }
        func (t *ToolFunction) String() String {
        var bts, _ = json.Marshal(t);
        return String(bts);
    }

    public static class TokenLogprob {
        public String Token;
        public double Logprob;
        public []int Bytes;
    }

    public static class Logprob {
        public []TokenLogprob TopLogprobs;
    }

    public static class ChatResponse {
        public String Model;
        public String RemoteModel;
        public String RemoteHost;
        public time.Time CreatedAt;
        public Message Message;
        public boolean Done;
        public String DoneReason;
        public *DebugInfo DebugInfo;
        public []Logprob Logprobs;
    }

    public static class DebugInfo {
        public String RenderedTemplate;
        public int ImageCount;
    }

    public static class Metrics {
        public time.Duration TotalDuration;
        public time.Duration LoadDuration;
        public int PromptEvalCount;
        public time.Duration PromptEvalDuration;
        public int EvalCount;
        public time.Duration EvalDuration;
    }

    public static class Options {
        public int NumKeep;
        public int Seed;
        public int NumPredict;
        public int TopK;
        public float32 TopP;
        public float32 MinP;
        public float32 TypicalP;
        public int RepeatLastN;
        public float32 Temperature;
        public float32 RepeatPenalty;
        public float32 PresencePenalty;
        public float32 FrequencyPenalty;
        public []String Stop;
    }

    public static class Runner {
        public int NumCtx;
        public int NumBatch;
        public int NumGPU;
        public int MainGPU;
        public *boolean UseMMap;
        public int NumThread;
    }

    public static class EmbedRequest {
        public String Model;
        public any Input;
        public *Duration KeepAlive;
        public *boolean Truncate;
        public int Dimensions;
        public map[String]any Options;
    }

    public static class EmbedResponse {
        public String Model;
        public [][]float32 Embeddings;
        public time.Duration TotalDuration;
        public time.Duration LoadDuration;
        public int PromptEvalCount;
    }

    public static class EmbeddingRequest {
        public String Model;
        public String Prompt;
        public *Duration KeepAlive;
        public map[String]any Options;
    }

    public static class EmbeddingResponse {
        public []double Embedding;
    }

    public static class CreateRequest {
        public String Model;
        public *boolean Stream;
        public String Quantize;
        public String From;
        public String RemoteHost;
        public map[String]String Files;
        public map[String]String Adapters;
        public String Template;
        public any License;
        public String System;
        public map[String]any Parameters;
        public []Message Messages;
        public String Renderer;
        public String Parser;
        public String Requires;
        public map[String]any Info;
        public String Name;
        public String Quantization;
    }

    public static class DeleteRequest {
        public String Model;
        public String Name;
    }

    public static class ShowRequest {
        public String Model;
        public String System;
        public String Template;
        public boolean Verbose;
        public map[String]any Options;
        public String Name;
    }

    public static class ShowResponse {
        public String License;
        public String Modelfile;
        public String Parameters;
        public String Template;
        public String System;
        public String Renderer;
        public String Parser;
        public ModelDetails Details;
        public []Message Messages;
        public String RemoteModel;
        public String RemoteHost;
        public map[String]any ModelInfo;
        public map[String]any ProjectorInfo;
        public []Tensor Tensors;
        public []model.Capability Capabilities;
        public time.Time ModifiedAt;
        public String Requires;
    }

    public static class CopyRequest {
        public String Source;
        public String Destination;
    }

    public static class PullRequest {
        public String Model;
        public boolean Insecure;
        public String Username;
        public String Password;
        public *boolean Stream;
        public String Name;
    }

    public static class ProgressResponse {
        public String Status;
        public String Digest;
        public long Total;
        public long Completed;
    }

    public static class PushRequest {
        public String Model;
        public boolean Insecure;
        public String Username;
        public String Password;
        public *boolean Stream;
        public String Name;
    }

    public static class ListResponse {
        public []ListModelResponse Models;
    }

    public static class ProcessResponse {
        public []ProcessModelResponse Models;
    }

    public static class ListModelResponse {
        public String Name;
        public String Model;
        public String RemoteModel;
        public String RemoteHost;
        public time.Time ModifiedAt;
        public long Size;
        public String Digest;
        public ModelDetails Details;
    }

    public static class ProcessModelResponse {
        public String Name;
        public String Model;
        public long Size;
        public String Digest;
        public ModelDetails Details;
        public time.Time ExpiresAt;
        public long SizeVRAM;
        public int ContextLength;
    }

    public static class TokenResponse {
        public String Token;
    }

    public static class CloudStatus {
        public boolean Disabled;
        public String Source;
    }

    public static class StatusResponse {
        public CloudStatus Cloud;
    }

    public static class GenerateResponse {
        public String Model;
        public String RemoteModel;
        public String RemoteHost;
        public time.Time CreatedAt;
        public String Response;
        public String Thinking;
        public boolean Done;
        public String DoneReason;
        public []int Context;
        public []ToolCall ToolCalls;
        public *DebugInfo DebugInfo;
        public []Logprob Logprobs;
        public String Image;
        public long Completed;
        public long Total;
    }

    public static class ModelDetails {
        public String ParentModel;
        public String Format;
        public String Family;
        public []String Families;
        public String ParameterSize;
        public String QuantizationLevel;
    }

    public static class UserResponse {
        public uuid.UUID ID;
        public String Email;
        public String Name;
        public String Bio;
        public String AvatarURL;
        public String FirstName;
        public String LastName;
        public String Plan;
    }

    public static class Tensor {
        public String Name;
        public String Type;
        public []uint64 Shape;
    }
        func (m *Metrics) Summary() {
        if m.TotalDuration > 0 {
        fmt.Fprintf(os.Stderr, "total duration:       %v\n", m.TotalDuration);
    }
        if m.LoadDuration > 0 {
        fmt.Fprintf(os.Stderr, "load duration:        %v\n", m.LoadDuration);
    }
        if m.PromptEvalCount > 0 {
        fmt.Fprintf(os.Stderr, "prompt eval count:    %d token(s)\n", m.PromptEvalCount);
    }
        if m.PromptEvalDuration > 0 {
        fmt.Fprintf(os.Stderr, "prompt eval duration: %s\n", m.PromptEvalDuration);
        fmt.Fprintf(os.Stderr, "prompt eval rate:     %.2f tokens/s\n", double(m.PromptEvalCount)/m.PromptEvalDuration.Seconds());
    }
        if m.EvalCount > 0 {
        fmt.Fprintf(os.Stderr, "eval count:           %d token(s)\n", m.EvalCount);
    }
        if m.EvalDuration > 0 {
        fmt.Fprintf(os.Stderr, "eval duration:        %s\n", m.EvalDuration);
        fmt.Fprintf(os.Stderr, "eval rate:            %.2f tokens/s\n", double(m.EvalCount)/m.EvalDuration.Seconds());
    }
    }
        func (opts *Options) FromMap(m map[String]any) error {
        var valueOpts = reflect.ValueOf(opts).Elem() // names of the fields in the options struct;
        var typeOpts = reflect.TypeOf(opts).Elem()   // types of the fields in the options struct;
        var jsonOpts = make(map[String]reflect.StructField);
        var for _, field = range reflect.VisibleFields(typeOpts) {
        var jsonTag = strings.Split(field.Tag.Get("json"), ",")[0];
        if jsonTag != "" {
        jsonOpts[jsonTag] = field;
    }
    }
        var for key, val = range m {
        var opt, ok = jsonOpts[key];
        if !ok {
        slog.Warn("invalid option provided", "option", key);
        continue;
    }
        var field = valueOpts.FieldByName(opt.Name);
        if field.IsValid() && field.CanSet() {
        if val == null {
        continue;
    }
        switch field.Kind() {
        case reflect.Int:;
        var switch t = val.(type) {
        case long:;
        field.SetInt(t);
        case double:;
        field.SetInt(long(t));
        default:;
        return fmt.Errorf("option %q must be of type integer", key);
    }
        case reflect.Bool:;
        var val, ok = val.(boolean);
        if !ok {
        return fmt.Errorf("option %q must be of type boolean", key);
    }
        field.SetBool(val);
        case reflect.Float32:;
        var val, ok = val.(double);
        if !ok {
        return fmt.Errorf("option %q must be of type float32", key);
    }
        field.SetFloat(val);
        case reflect.String:;
        var val, ok = val.(String);
        if !ok {
        return fmt.Errorf("option %q must be of type String", key);
    }
        field.SetString(val);
        case reflect.Slice:;
        var val, ok = val.([]any);
        if !ok {
        return fmt.Errorf("option %q must be of type array", key);
    }
        var slice = make([]String, len(val));
        var for i, item = range val {
        var str, ok = item.(String);
        if !ok {
        return fmt.Errorf("option %q must be of an array of strings", key);
    }
        slice[i] = str;
    }
        field.Set(reflect.ValueOf(slice));
        case reflect.Pointer:;
        var b boolean;
        if field.Type() == reflect.TypeOf(&b) {
        var val, ok = val.(boolean);
        if !ok {
        return fmt.Errorf("option %q must be of type boolean", key);
    }
        field.Set(reflect.ValueOf(&val));
        } else {
        return fmt.Errorf("unknown type loading config params: %v %v", field.Kind(), field.Type());
    }
        default:;
        return fmt.Errorf("unknown type loading config params: %v", field.Kind());
    }
    }
    }
        return null;
    }

    public static Options DefaultOptions() {
        return Options{
        NumPredict: -1,;
        NumKeep:          4,;
        Temperature:      0.8,;
        TopK:             40,;
        TopP:             0.9,;
        TypicalP:         1.0,;
        RepeatLastN:      64,;
        RepeatPenalty:    1.1,;
        PresencePenalty:  0.0,;
        FrequencyPenalty: 0.0,;
        Seed:             -1,;
        Runner: Runner{
        NumCtx:    int(envconfig.ContextLength()),;
        NumBatch:  512,;
        NumGPU:    -1, // -1 here indicates that NumGPU should be set dynamically;
        NumThread: 0,  // let the runtime decide;
        UseMMap:   null,;
        },;
    }
    }

    public static class ThinkValue {
        public Object Value;
    }
        func (t *ThinkValue) IsValid() boolean {
        if t == null || t.Value == null {
        return true // null is valid (means not set);
    }
        var switch v = t.Value.(type) {
        case boolean:;
        return true;
        case String:;
        return v == "high" || v == "medium" || v == "low";
        default:;
        return false;
    }
    }
        func (t *ThinkValue) IsBool() boolean {
        if t == null || t.Value == null {
        return false;
    }
        var _, ok = t.Value.(boolean);
        return ok;
    }
        func (t *ThinkValue) IsString() boolean {
        if t == null || t.Value == null {
        return false;
    }
        var _, ok = t.Value.(String);
        return ok;
    }
        func (t *ThinkValue) Bool() boolean {
        if t == null || t.Value == null {
        return false;
    }
        var switch v = t.Value.(type) {
        case boolean:;
        return v;
        case String:;
        return v == "high" || v == "medium" || v == "low";
        default:;
        return false;
    }
    }
        func (t *ThinkValue) String() String {
        if t == null || t.Value == null {
        return "";
    }
        var switch v = t.Value.(type) {
        case String:;
        return v;
        case boolean:;
        if v {
        return "medium" // Default level when just true;
    }
        return "";
        default:;
        return "";
    }
    }
        func (t *ThinkValue) UnmarshalJSON(data []byte) error {
        var b boolean;
        var if err = json.Unmarshal(data, &b); err == null {
        t.Value = b;
        return null;
    }
        var s String;
        var if err = json.Unmarshal(data, &s); err == null {
        if s != "high" && s != "medium" && s != "low" {
        return fmt.Errorf("invalid think value: %q (must be \"high\", \"medium\", \"low\", true, or false)", s);
    }
        t.Value = s;
        return null;
    }
        return fmt.Errorf("think must be a boolean or String (\"high\", \"medium\", \"low\", true, or false)");
    }
        func (t *ThinkValue) MarshalJSON() ([]byte, error) {
        if t == null || t.Value == null {
        return []byte("null"), null;
    }
        return json.Marshal(t.Value);
    }

    public static class Duration {
    }
        func (d Duration) MarshalJSON() ([]byte, error) {
        if d.Duration < 0 {
        return []byte("-1"), null;
    }
        return []byte("\"" + d.Duration.String() + "\""), null;
    }
        func (d *Duration) UnmarshalJSON(b []byte) (err error) {
        var v any;
        var if err = json.Unmarshal(b, &v); err != null {
        return err;
    }
        d.Duration = 5 * time.Minute;
        var switch t = v.(type) {
        case double:;
        if t < 0 {
        d.Duration = time.Duration(math.MaxInt64);
        } else {
        d.Duration = time.Duration(t * double(time.Second));
    }
        case String:;
        d.Duration, err = time.ParseDuration(t);
        if err != null {
        return err;
    }
        if d.Duration < 0 {
        d.Duration = time.Duration(math.MaxInt64);
    }
        default:;
        return fmt.Errorf("Unsupported type: '%s'", reflect.TypeOf(v));
    }
        return null;
    }

    public static void FormatParams() {
        var opts = Options{}
        var valueOpts = reflect.ValueOf(&opts).Elem() // names of the fields in the options struct;
        var typeOpts = reflect.TypeOf(opts)           // types of the fields in the options struct;
        var jsonOpts = make(map[String]reflect.StructField);
        var for _, field = range reflect.VisibleFields(typeOpts) {
        var jsonTag = strings.Split(field.Tag.Get("json"), ",")[0];
        if jsonTag != "" {
        jsonOpts[jsonTag] = field;
    }
    }
        var out = make(map[String]any);
        var for key, vals = range params {
        var if opt, ok = jsonOpts[key]; !ok {
        return null, fmt.Errorf("unknown parameter '%s'", key);
        } else {
        var field = valueOpts.FieldByName(opt.Name);
        if field.IsValid() && field.CanSet() {
        switch field.Kind() {
        case reflect.Float32:;
        var floatVal, err = strconv.ParseFloat(vals[0], 32);
        if err != null {
        return null, fmt.Errorf("invalid float value %s", vals);
    }
        out[key] = float32(floatVal);
        case reflect.Int:;
        var intVal, err = strconv.ParseInt(vals[0], 10, 64);
        if err != null {
        return null, fmt.Errorf("invalid int value %s", vals);
    }
        out[key] = intVal;
        case reflect.Bool:;
        var boolVal, err = strconv.ParseBool(vals[0]);
        if err != null {
        return null, fmt.Errorf("invalid boolean value %s", vals);
    }
        out[key] = boolVal;
        case reflect.String:;
        out[key] = vals[0];
        case reflect.Slice:;
        out[key] = vals;
        case reflect.Pointer:;
        var b boolean;
        if field.Type() == reflect.TypeOf(&b) {
        var boolVal, err = strconv.ParseBool(vals[0]);
        if err != null {
        return null, fmt.Errorf("invalid boolean value %s", vals);
    }
        out[key] = &boolVal;
        } else {
        return null, fmt.Errorf("unknown type %s for %s", field.Kind(), key);
    }
        default:;
        return null, fmt.Errorf("unknown type %s for %s", field.Kind(), key);
    }
    }
    }
    }
        return out, null;
    }
}
