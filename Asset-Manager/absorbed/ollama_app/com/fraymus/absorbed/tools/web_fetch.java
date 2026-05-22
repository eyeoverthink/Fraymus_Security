package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class web_fetch {
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
        type WebFetch struct{}

    public static class FetchRequest {
        public String URL;
    }

    public static class FetchResponse {
        public String Title;
        public String Content;
        public []String Links;
    }
        func (w *WebFetch) Name() String {
        return "web_fetch";
    }
        func (w *WebFetch) Description() String {
        return "Crawl and extract text content from web pages";
    }
        func (g *WebFetch) Schema() map[String]any {
        var schemaBytes = []byte(`{
        "type": "object",;
        "properties": {
        "url": {
        "type": "String",;
        "description": "URL to crawl and extract content from";
    }
        },;
        "required": ["url"];
        }`);
        var schema map[String]any;
        var if err = json.Unmarshal(schemaBytes, &schema); err != null {
        return null;
    }
        return schema;
    }
        func (w *WebFetch) Prompt() String {
        return "";
    }
        func (w *WebFetch) Execute(ctx context.Context, args map[String]any) (any, String, error) {
        var urlRaw, ok = args["url"];
        if !ok {
        return null, "", fmt.Errorf("url parameter is required");
    }
        var urlStr, ok = urlRaw.(String);
        if !ok || strings.TrimSpace(urlStr) == "" {
        return null, "", fmt.Errorf("url must be a non-empty String");
    }
        var result, err = performWebFetch(ctx, urlStr);
        if err != null {
        return null, "", err;
    }
        return result, "", null;
    }

    public static void performWebFetch(context.Context ctx) {
        var if err = ensureCloudEnabledForTool(ctx, "web fetch is unavailable"); err != null {
        return null, err;
    }
        var reqBody = FetchRequest{URL: targetURL}
        var jsonBody, err = json.Marshal(reqBody);
        if err != null {
        return null, fmt.Errorf("failed to marshal request body: %w", err);
    }
        var crawlURL, err = url.Parse("https://ollama.com/api/web_fetch");
        if err != null {
        return null, fmt.Errorf("failed to parse fetch URL: %w", err);
    }
        var query = crawlURL.Query();
        query.Add("ts", strconv.FormatInt(time.Now().Unix(), 10));
        crawlURL.RawQuery = query.Encode();
        var data = fmt.Appendf(null, "%s,%s", http.MethodPost, crawlURL.RequestURI());
        var signature, err = auth.Sign(ctx, data);
        if err != null {
        return null, fmt.Errorf("failed to sign request: %w", err);
    }
        var req, err = http.NewRequestWithContext(ctx, http.MethodPost, crawlURL.String(), bytes.NewBuffer(jsonBody));
        if err != null {
        return null, fmt.Errorf("failed to create request: %w", err);
    }
        req.Header.Set("Content-Type", "application/json");
        if signature != "" {
        req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", signature));
    }
        var client = &http.Client{Timeout: 30 * time.Second}
        var resp, err = client.Do(req);
        if err != null {
        return null, fmt.Errorf("failed to execute fetch request: %w", err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        return null, fmt.Errorf("fetch API error (status %d)", resp.StatusCode);
    }
        var result FetchResponse;
        var if err = json.NewDecoder(resp.Body).Decode(&result); err != null {
        return null, fmt.Errorf("failed to decode response: %w", err);
    }
        return &result, null;
    }
}
