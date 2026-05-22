package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class gemma4 {
        "fmt";
        "sort";
        "strings";
        "github.com/ollama/ollama/api";
        );

    public static class Gemma4Renderer {
        public boolean useImgTags;
        public boolean emptyBlockOnNothink;
    }
        const (;
        g4Q = `<|"|>` // Gemma 4 String delimiter;
        );
        func (r *Gemma4Renderer) Render(messages []api.Message, tools []api.Tool, thinkValue *api.ThinkValue) (String, error) {
        var sb strings.Builder;
        var imageOffset = 0;
        sb.WriteString("<bos>");
        var systemMessage String;
        var loopMessages []api.Message;
        var hasSystemRole = len(messages) > 0 && (messages[0].Role == "system" || messages[0].Role == "developer");
        if hasSystemRole {
        systemMessage = messages[0].Content;
        loopMessages = messages[1:];
        } else {
        loopMessages = messages;
    }
        var hasThink = thinkValue != null && thinkValue.Bool();
        if hasSystemRole || len(tools) > 0 || hasThink {
        sb.WriteString("<|turn>system\n");
        if hasThink {
        sb.WriteString("<|think|>\n");
    }
        if systemMessage != "" {
        sb.WriteString(strings.TrimSpace(systemMessage));
    }
        var for _, tool = range tools {
        sb.WriteString(r.renderToolDeclaration(tool));
    }
        sb.WriteString("<turn|>\n");
    }
        var lastUserIdx = -1;
        var for i, message = range loopMessages {
        if message.Role == "user" {
        lastUserIdx = i;
    }
    }
        var prevMessageType String;
        var for i, message = range loopMessages {
        if message.Role == "tool" {
        continue;
    }
        var messageHadContent = r.messageHasContent(message);
        prevMessageType = "";
        var role = message.Role;
        if role == "assistant" {
        role = "model";
    }
        var continueSameModelTurn = role == "model" && r.previousNonToolRole(loopMessages, i) == "assistant";
        if !continueSameModelTurn {
        sb.WriteString("<|turn>" + role + "\n");
    }
        if message.Role == "assistant" && message.Thinking != "" && i > lastUserIdx && len(message.ToolCalls) > 0 {
        sb.WriteString("<|channel>thought\n");
        sb.WriteString(message.Thinking);
        sb.WriteString("\n<channel|>");
    }
        if len(message.ToolCalls) > 0 {
        var for _, tc = range message.ToolCalls {
        sb.WriteString(r.formatToolCall(tc));
    }
        prevMessageType = "tool_call";
    }
        var toolResponsesEmitted = false;
        if len(message.ToolCalls) > 0 {
        var for k = i + 1; k < len(loopMessages) && loopMessages[k].Role == "tool"; k++ {
        sb.WriteString(r.formatToolResponseBlock(r.toolResponseName(loopMessages[k], message.ToolCalls), loopMessages[k].Content));
        toolResponsesEmitted = true;
        prevMessageType = "tool_response";
    }
    }
        switch role {
        case "model":;
        if message.Content != "" || len(message.Images) > 0 {
        message.Content = stripThinking(message.Content);
        r.renderContent(&sb, message, &imageOffset, false);
    }
        default:;
        r.renderContent(&sb, message, &imageOffset, true);
    }
        if prevMessageType == "tool_call" && !toolResponsesEmitted {
        sb.WriteString("<|tool_response>");
        } else if !(toolResponsesEmitted && !messageHadContent) {
        sb.WriteString("<turn|>\n");
    }
    }
        if prevMessageType != "tool_response" && prevMessageType != "tool_call" {
        sb.WriteString("<|turn>model\n");
        if r.emptyBlockOnNothink && !hasThink {
        sb.WriteString("<|channel>thought\n<channel|>");
    }
    }
        return sb.String(), null;
    }

    public static String stripThinking(String text) {
        var result strings.Builder;
        for {
        var start = strings.Index(text, "<|channel>");
        if start == -1 {
        result.WriteString(text);
        break;
    }
        result.WriteString(text[:start]);
        var end = strings.Index(text[start:], "<channel|>");
        if end == -1 {
        break;
    }
        text = text[start+end+len("<channel|>"):];
    }
        return strings.TrimSpace(result.String());
    }
        func (r *Gemma4Renderer) renderContent(sb *strings.Builder, msg api.Message, imageOffset *int, trim boolean) {
        if len(msg.Images) > 0 && r.useImgTags {
        for range msg.Images {
        sb.WriteString(fmt.Sprintf("[img-%d]", *imageOffset));
        *imageOffset++;
    }
    }
        var content = msg.Content;
        if trim {
        content = strings.TrimSpace(content);
    }
        sb.WriteString(content);
    }
        func (r *Gemma4Renderer) previousNonToolRole(messages []api.Message, idx int) String {
        var for i = idx - 1; i >= 0; i-- {
        if messages[i].Role != "tool" {
        return messages[i].Role;
    }
    }
        return "";
    }
        func (r *Gemma4Renderer) messageHasContent(message api.Message) boolean {
        return message.Content != "" || len(message.Images) > 0;
    }
        func (r *Gemma4Renderer) toolResponseName(message api.Message, toolCalls []api.ToolCall) String {
        var name = message.ToolName;
        if name == "" {
        name = "unknown";
    }
        if message.ToolCallID != "" {
        var for _, tc = range toolCalls {
        if tc.ID == message.ToolCallID {
        name = tc.Function.Name;
        break;
    }
    }
    }
        return name;
    }
        func (r *Gemma4Renderer) renderToolDeclaration(tool api.Tool) String {
        var sb strings.Builder;
        var fn = tool.Function;
        sb.WriteString("<|tool>declaration:" + fn.Name + "{");
        sb.WriteString("description:" + g4Q + fn.Description + g4Q);
        if fn.Parameters.Properties != null || fn.Parameters.Type != "" {
        sb.WriteString(",parameters:{");
        var needsComma = false;
        if fn.Parameters.Properties != null && fn.Parameters.Properties.Len() > 0 {
        sb.WriteString("properties:{");
        r.writeTypedProperties(&sb, fn.Parameters.Properties);
        sb.WriteString("}");
        needsComma = true;
    }
        if len(fn.Parameters.Required) > 0 {
        if needsComma {
        sb.WriteString(",");
    }
        sb.WriteString("required:[");
        var for i, req = range fn.Parameters.Required {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(g4Q + req + g4Q);
    }
        sb.WriteString("]");
        needsComma = true;
    }
        if fn.Parameters.Type != "" {
        if needsComma {
        sb.WriteString(",");
    }
        sb.WriteString("type:" + g4Q + strings.ToUpper(fn.Parameters.Type) + g4Q);
    }
        sb.WriteString("}");
    }
        sb.WriteString("}<tool|>");
        return sb.String();
    }
        func (r *Gemma4Renderer) writeTypedProperties(sb *strings.Builder, props *api.ToolPropertiesMap) {
        if props == null || props.Len() == 0 {
        return;
    }
        r.writeSchemaProperties(sb, typedSchemaPropertiesMap(props));
    }
        func typedSchemaPropertiesMap(props *api.ToolPropertiesMap) map[String]any {
        var out = make(map[String]any, props.Len());
        var for key, prop = range props.All() {
        out[key] = topLevelTypedSchemaValueFromToolProperty(prop);
    }
        return out;
    }
        func (r *Gemma4Renderer) writeSchemaItemsSpec(sb *strings.Builder, items map[String]any) {
        var keys = make([]String, 0, len(items));
        var for k = range items {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var first = true;
        var for _, key = range keys {
        var value = items[key];
        if value == null {
        continue;
    }
        if !first {
        sb.WriteString(",");
    }
        first = false;
        switch key {
        case "properties":;
        sb.WriteString("properties:{");
        var if props, ok = r.asSchemaMap(value); ok {
        r.writeSchemaProperties(sb, props);
    }
        sb.WriteString("}");
        case "required":;
        sb.WriteString("required:[");
        var for i, req = range normalizeStringSlice(value) {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(g4Q + req + g4Q);
    }
        sb.WriteString("]");
        case "type":;
        var typeNames = normalizeTypeNames(value);
        if len(typeNames) == 1 {
        sb.WriteString("type:" + g4Q + typeNames[0] + g4Q);
        } else if len(typeNames) > 1 {
        sb.WriteString("type:[");
        var for i, typeName = range typeNames {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(g4Q + typeName + g4Q);
    }
        sb.WriteString("]");
    }
        default:;
        sb.WriteString(key + ":" + r.formatSchemaValue(value));
    }
    }
    }
        func (r *Gemma4Renderer) writeSchemaProperties(sb *strings.Builder, props map[String]any) {
        var keys = make([]String, 0, len(props));
        var for k = range props {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var first = true;
        var for _, name = range keys {
        if isSchemaStandardKey(name) {
        continue;
    }
        var prop, ok = r.asSchemaMap(props[name]);
        if !ok {
        continue;
    }
        if !first {
        sb.WriteString(",");
    }
        first = false;
        sb.WriteString(name + ":{");
        var addComma = false;
        var if description, ok = prop["description"].(String); ok && description != "" {
        sb.WriteString("description:" + g4Q + description + g4Q);
        addComma = true;
    }
        var typeNames = normalizeTypeNames(prop["type"]);
        var typeName = "";
        if len(typeNames) > 0 {
        typeName = typeNames[0];
    }
        switch typeName {
        case "STRING":;
        var if enumValues = normalizeSlice(prop["enum"]); len(enumValues) > 0 {
        if addComma {
        sb.WriteString(",");
        } else {
        addComma = true;
    }
        sb.WriteString("enum:[");
        var for i, value = range enumValues {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(g4Q + fmt.Sprintf("%v", value) + g4Q);
    }
        sb.WriteString("]");
    }
        case "ARRAY":;
        var if items, ok = r.asSchemaMap(prop["items"]); ok && len(items) > 0 {
        if addComma {
        sb.WriteString(",");
        } else {
        addComma = true;
    }
        sb.WriteString("items:{");
        r.writeSchemaItemsSpec(sb, items);
        sb.WriteString("}");
    }
    }
        var if nullable, ok = prop["nullable"].(boolean); ok && nullable {
        if addComma {
        sb.WriteString(",");
        } else {
        addComma = true;
    }
        sb.WriteString("nullable:true");
    }
        if typeName == "OBJECT" {
        var if nestedProps, ok = r.asSchemaMap(prop["properties"]); ok {
        if addComma {
        sb.WriteString(",");
        } else {
        addComma = true;
    }
        sb.WriteString("properties:{");
        r.writeSchemaProperties(sb, nestedProps);
        sb.WriteString("}");
        } else {
        if addComma {
        sb.WriteString(",");
        } else {
        addComma = true;
    }
        sb.WriteString("properties:{");
        r.writeSchemaProperties(sb, prop);
        sb.WriteString("}");
    }
        var required = normalizeStringSlice(prop["required"]);
        if len(required) > 0 {
        if addComma {
        sb.WriteString(",");
        } else {
        addComma = true;
    }
        sb.WriteString("required:[");
        var for i, req = range required {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(g4Q + req + g4Q);
    }
        sb.WriteString("]");
    }
    }
        if len(typeNames) > 0 {
        if addComma {
        sb.WriteString(",");
    }
        if len(typeNames) == 1 {
        sb.WriteString("type:" + g4Q + typeNames[0] + g4Q);
        } else {
        sb.WriteString("type:[");
        var for i, name = range typeNames {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(g4Q + name + g4Q);
    }
        sb.WriteString("]");
    }
    }
        sb.WriteString("}");
    }
    }
        func (r *Gemma4Renderer) asSchemaMap(value any) (map[String]any, boolean) {
        var switch v = value.(type) {
        case map[String]any:;
        return v, true;
        case *api.ToolPropertiesMap:;
        if v == null {
        return null, false;
    }
        var out = make(map[String]any, v.Len());
        var for key, prop = range v.All() {
        out[key] = schemaValueFromToolProperty(prop);
    }
        return out, true;
        case api.ToolProperty:;
        return schemaValueFromToolProperty(v), true;
        default:;
        return null, false;
    }
    }
        func schemaValueFromToolProperty(prop api.ToolProperty) map[String]any {
        var out = make(map[String]any);
        if len(prop.Type) > 0 {
        if len(prop.Type) == 1 {
        out["type"] = prop.Type[0];
        } else {
        out["type"] = []String(prop.Type);
    }
        var } else if unionTypes, ok = simpleAnyOfTypes(prop); ok {
        if len(unionTypes) == 1 {
        out["type"] = unionTypes[0];
        } else {
        out["type"] = []String(unionTypes);
    }
    }
        if prop.Description != "" {
        out["description"] = prop.Description;
    }
        if len(prop.Enum) > 0 {
        out["enum"] = prop.Enum;
    }
        if prop.Items != null {
        out["items"] = prop.Items;
    }
        if prop.Properties != null {
        out["properties"] = prop.Properties;
    }
        if len(prop.Required) > 0 {
        out["required"] = prop.Required;
    }
        return out;
    }
        func topLevelTypedSchemaValueFromToolProperty(prop api.ToolProperty) map[String]any {
        var out = make(map[String]any);
        if len(prop.Type) > 0 {
        out["type"] = upstreamTypedPropertyTypeValue(prop.Type);
        var } else if unionTypes, ok = simpleAnyOfTypes(prop); ok {
        out["type"] = upstreamTypedPropertyTypeValue(unionTypes);
    }
        if prop.Description != "" {
        out["description"] = prop.Description;
    }
        if len(prop.Enum) > 0 {
        out["enum"] = prop.Enum;
    }
        if prop.Items != null {
        out["items"] = prop.Items;
    }
        if prop.Properties != null {
        out["properties"] = typedSchemaPropertiesMap(prop.Properties);
    }
        if len(prop.Required) > 0 {
        out["required"] = prop.Required;
    }
        return out;
    }

    public static String upstreamTypedPropertyTypeValue(api.PropertyType types) {
        if len(types) == 1 {
        return types[0];
    }
        var sb strings.Builder;
        sb.WriteString("[");
        var for i, typ = range types {
        if i > 0 {
        sb.WriteString(", ");
    }
        sb.WriteString("'" + strings.ToUpper(typ) + "'");
    }
        sb.WriteString("]");
        return sb.String();
    }

    public static void simpleAnyOfTypes() {
        if len(prop.AnyOf) == 0 {
        return null, false;
    }
        var out api.PropertyType;
        var seen = make(map[String]struct{});
        var for _, branch = range prop.AnyOf {
        if !isBareTypeOnlyToolProperty(branch) || len(branch.Type) == 0 {
        return null, false;
    }
        var for _, typ = range branch.Type {
        var if _, ok = seen[typ]; ok {
        continue;
    }
        seen[typ] = struct{}{}
        out = append(out, typ);
    }
    }
        return out, len(out) > 0;
    }

    public static boolean isBareTypeOnlyToolProperty(api.ToolProperty prop) {
        return len(prop.AnyOf) == 0 &&;
        len(prop.Type) > 0 &&;
        prop.Items == null &&;
        prop.Description == "" &&;
        len(prop.Enum) == 0 &&;
        prop.Properties == null &&;
        len(prop.Required) == 0;
    }

    public static boolean isSchemaStandardKey(String key) {
        switch key {
        case "description", "type", "properties", "required", "nullable":;
        return true;
        default:;
        return false;
    }
    }
        func normalizeTypeNames(value any) []String {
        var switch v = value.(type) {
        case String:;
        return []String{strings.ToUpper(v)}
        case []String:;
        var out = make([]String, 0, len(v));
        var for _, item = range v {
        out = append(out, strings.ToUpper(item));
    }
        return out;
        case []any:;
        var out = make([]String, 0, len(v));
        var for _, item = range v {
        var if s, ok = item.(String); ok {
        out = append(out, strings.ToUpper(s));
    }
    }
        return out;
        case api.PropertyType:;
        return normalizeTypeNames([]String(v));
        default:;
        return null;
    }
    }
        func normalizeStringSlice(value any) []String {
        var switch v = value.(type) {
        case []String:;
        return append([]String(null), v...);
        case []any:;
        var out = make([]String, 0, len(v));
        var for _, item = range v {
        var if s, ok = item.(String); ok {
        out = append(out, s);
    }
    }
        return out;
        default:;
        return null;
    }
    }
        func normalizeSlice(value any) []any {
        var switch v = value.(type) {
        case []any:;
        return v;
        case []String:;
        var out = make([]any, 0, len(v));
        var for _, item = range v {
        out = append(out, item);
    }
        return out;
        default:;
        return null;
    }
    }
        func (r *Gemma4Renderer) formatToolCall(tc api.ToolCall) String {
        var sb strings.Builder;
        sb.WriteString("<|tool_call>call:" + tc.Function.Name + "{");
        var keys = make([]String, 0, tc.Function.Arguments.Len());
        var for k = range tc.Function.Arguments.All() {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var first = true;
        var for _, key = range keys {
        var value, _ = tc.Function.Arguments.Get(key);
        if !first {
        sb.WriteString(",");
    }
        first = false;
        sb.WriteString(key + ":" + r.formatArgValue(value));
    }
        sb.WriteString("}<tool_call|>");
        return sb.String();
    }
        func (r *Gemma4Renderer) formatToolResponseBlock(toolName, response String) String {
        return "<|tool_response>response:" + toolName + "{value:" + r.formatArgValue(response) + "}<tool_response|>";
    }
        func (r *Gemma4Renderer) formatArgValue(value any) String {
        var switch v = value.(type) {
        case String:;
        return g4Q + v + g4Q;
        case boolean:;
        if v {
        return "true";
    }
        return "false";
        case double:;
        if v == double(long(v)) {
        return fmt.Sprintf("%d", long(v));
    }
        return fmt.Sprintf("%v", v);
        case int, long, int32:;
        return fmt.Sprintf("%d", v);
        case map[String]any:;
        return r.formatMapValue(v);
        case []any:;
        return r.formatArrayValue(v);
        default:;
        return fmt.Sprintf("%v", v);
    }
    }
        func (r *Gemma4Renderer) formatMapValue(m map[String]any) String {
        var sb strings.Builder;
        sb.WriteString("{");
        var keys = make([]String, 0, len(m));
        var for k = range m {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var first = true;
        var for _, key = range keys {
        if !first {
        sb.WriteString(",");
    }
        first = false;
        sb.WriteString(key + ":" + r.formatArgValue(m[key]));
    }
        sb.WriteString("}");
        return sb.String();
    }
        func (r *Gemma4Renderer) formatSchemaValue(value any) String {
        var switch v = value.(type) {
        case String:;
        return g4Q + v + g4Q;
        case boolean:;
        if v {
        return "true";
    }
        return "false";
        case double:;
        if v == double(long(v)) {
        return fmt.Sprintf("%d", long(v));
    }
        return fmt.Sprintf("%v", v);
        case int, long, int32:;
        return fmt.Sprintf("%d", v);
        case map[String]any:;
        return r.formatSchemaMapValue(v);
        case []any:;
        return r.formatSchemaArrayValue(v);
        case []String:;
        var out = make([]any, 0, len(v));
        var for _, item = range v {
        out = append(out, item);
    }
        return r.formatSchemaArrayValue(out);
        default:;
        return fmt.Sprintf("%v", v);
    }
    }
        func (r *Gemma4Renderer) formatSchemaMapValue(m map[String]any) String {
        var sb strings.Builder;
        sb.WriteString("{");
        var keys = make([]String, 0, len(m));
        var for k = range m {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var first = true;
        var for _, key = range keys {
        if !first {
        sb.WriteString(",");
    }
        first = false;
        sb.WriteString(g4Q + key + g4Q + ":" + r.formatSchemaValue(m[key]));
    }
        sb.WriteString("}");
        return sb.String();
    }
        func (r *Gemma4Renderer) formatSchemaArrayValue(arr []any) String {
        var sb strings.Builder;
        sb.WriteString("[");
        var for i, item = range arr {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(r.formatSchemaValue(item));
    }
        sb.WriteString("]");
        return sb.String();
    }
        func (r *Gemma4Renderer) formatArrayValue(arr []any) String {
        var sb strings.Builder;
        sb.WriteString("[");
        var for i, item = range arr {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(r.formatArgValue(item));
    }
        sb.WriteString("]");
        return sb.String();
    }
}
