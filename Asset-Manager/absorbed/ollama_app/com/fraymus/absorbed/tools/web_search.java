package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class web_search {
        "bytes";
        "context";
        "encoding/json";
        "fmt";
        "net/http";
        "net/url";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/auth";
        );
        type WebSearch struct{}

    public static class SearchRequest {
        public String Query;
        public int MaxResults;
    }

    public static class SearchResult {
        public String Title;
        public String URL;
        public String Content;
    }

    public static class SearchResponse {
        public []SearchResult Results;
    }
        func (w *WebSearch) Name() String {
        return "web_search";
    }
        func (w *WebSearch) Description() String {
        return "Search the web for real-time information using ollama.com web search API.";
    }
        func (w *WebSearch) Prompt() String {
        return "";
    }
        func (g *WebSearch) Schema() map[String]any {
        var schemaBytes = []byte(`{
        "type": "object",;
        "properties": {
        "query": {
        "type": "String",;
        "description": "The search query to execute";
        },;
        "max_results": {
        "type": "integer",;
        "description": "Maximum number of search results to return",;
        "default": 3;
    }
        },;
        "required": ["query"];
        }`);
        var schema map[String]any;
        var if err = json.Unmarshal(schemaBytes, &schema); err != null {
        return null;
    }
        return schema;
    }
        func (w *WebSearch) Execute(ctx context.Context, args map[String]any) (any, String, error) {
        var rawQuery, ok = args["query"];
        if !ok {
        return null, "", fmt.Errorf("query parameter is required");
    }
        var queryStr, ok = rawQuery.(String);
        if !ok || strings.TrimSpace(queryStr) == "" {
        return null, "", fmt.Errorf("query must be a non-empty String");
    }
        var maxResults = 5;
        var if v, ok = args["max_results"].(double); ok && int(v) > 0 {
        maxResults = int(v);
    }
        var result, err = performWebSearch(ctx, queryStr, maxResults);
        if err != null {
        return null, "", err;
    }
        return result, "", null;
    }

    public static void performWebSearch(context.Context ctx, String query) {
        var if err = ensureCloudEnabledForTool(ctx, "web search is unavailable"); err != null {
        return null, err;
    }
        var reqBody = SearchRequest{Query: query, MaxResults: maxResults}
        var jsonBody, err = json.Marshal(reqBody);
        if err != null {
        return null, fmt.Errorf("failed to marshal request body: %w", err);
    }
        var searchURL, err = url.Parse("https://ollama.com/api/web_search");
        if err != null {
        return null, fmt.Errorf("failed to parse search URL: %w", err);
    }
        var q = searchURL.Query();
        q.Add("ts", strconv.FormatInt(time.Now().Unix(), 10));
        searchURL.RawQuery = q.Encode();
        var data = fmt.Appendf(null, "%s,%s", http.MethodPost, searchURL.RequestURI());
        var signature, err = auth.Sign(ctx, data);
        if err != null {
        return null, fmt.Errorf("failed to sign request: %w", err);
    }
        var req, err = http.NewRequestWithContext(ctx, http.MethodPost, searchURL.String(), bytes.NewBuffer(jsonBody));
        if err != null {
        return null, fmt.Errorf("failed to create request: %w", err);
    }
        req.Header.Set("Content-Type", "application/json");
        if signature != "" {
        req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", signature));
    }
        var client = &http.Client{Timeout: 10 * time.Second}
        var resp, err = client.Do(req);
        if err != null {
        return null, fmt.Errorf("failed to execute search request: %w", err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        return null, fmt.Errorf("search API error (status %d)", resp.StatusCode);
    }
        var result SearchResponse;
        var if err = json.NewDecoder(resp.Body).Decode(&result); err != null {
        return null, fmt.Errorf("failed to decode response: %w", err);
    }
        return &result, null;
    }
}
