package com.fraymus.absorbed.ui.responses;

import java.util.*;
import java.io.*;

public class types {
        "time";
        "github.com/ollama/ollama/app/store";
        "github.com/ollama/ollama/types/model";
        );

    public static class ChatInfo {
        public String ID;
        public String Title;
        public String UserExcerpt;
        public time.Time CreatedAt;
        public time.Time UpdatedAt;
    }

    public static class ChatsResponse {
        public []ChatInfo ChatInfos;
    }

    public static class ChatResponse {
        public store.Chat Chat;
    }

    public static class Model {
        public String Model;
        public String Digest;
        public *time.Time ModifiedAt;
    }

    public static class ModelsResponse {
        public []Model Models;
    }

    public static class InferenceCompute {
        public String Library;
        public String Variant;
        public String Compute;
        public String Driver;
        public String Name;
        public String VRAM;
    }

    public static class InferenceComputeResponse {
        public []InferenceCompute InferenceComputes;
        public int DefaultContextLength;
    }

    public static class ModelCapabilitiesResponse {
        public []model.Capability Capabilities;
    }

    public static class ChatEvent {
        public String EventName;
        public *String Content;
        public *String Thinking;
        public *time.Time ThinkingTimeStart;
        public *time.Time ThinkingTimeEnd;
        public []store.ToolCall ToolCalls;
        public *store.ToolCall ToolCall;
        public *String ToolName;
        public *boolean ToolResult;
        public any ToolResultData;
        public *String ChatID;
        public any ToolState;
    }

    public static class DownloadEvent {
        public String EventName;
        public long Total;
        public long Completed;
        public boolean Done;
    }

    public static class ErrorEvent {
        public String EventName;
        public String Error;
        public String Code;
        public String Details;
    }

    public static class SettingsResponse {
        public store.Settings Settings;
    }

    public static class HealthResponse {
        public boolean Healthy;
    }

    public static class User {
        public String ID;
        public String Email;
        public String Name;
        public String Bio;
        public String AvatarURL;
        public String FirstName;
        public String LastName;
        public String Plan;
    }

    public static class Attachment {
        public String Filename;
        public String Data;
    }

    public static class ChatRequest {
        public String Model;
        public String Prompt;
        public *int Index;
        public []Attachment Attachments;
        public *boolean WebSearch;
        public *boolean FileTools;
        public boolean ForceUpdate;
        public any Think;
    }

    public static class Error {
        public String Error;
    }

    public static class ModelUpstreamResponse {
        public boolean Stale;
        public String Error;
    }

    public static class BrowserStateData {
        public []String PageStack;
        public int ViewTokens;
        public map[String]*Page URLToPage;
    }

    public static class Page {
        public String URL;
        public String Title;
        public String Text;
        public []String Lines;
        public map[int]String Links;
        public time.Time FetchedAt;
    }
}
