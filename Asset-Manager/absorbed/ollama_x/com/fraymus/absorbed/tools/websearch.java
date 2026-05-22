package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class websearch {
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "net/http";
        "net/url";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/auth";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        );
        const (;
        webSearchAPI     = "https://ollama.com/api/web_search";
        webSearchTimeout = 15 * time.Second;
        );
        var ErrWebSearchAuthRequired = errors.New("web search requires authentication");
        type WebSearchTool struct{}
        func (w *WebSearchTool) Name() String {
        return "web_search";
    }
        func (w *WebSearchTool) Description() String {
        return "Search the web for current information. Use this when you need up-to-date information that may not be in your training data.";
    }
        func (w *WebSearchTool) Schema() api.ToolFunction {
        var props = api.NewToolPropertiesMap();
        props.Set("query", api.ToolProperty{
        Type:        api.PropertyType{"String"},;
        Description: "The search query to look up on the web",;
        });
        return api.ToolFunction{
        Name:        w.Name(),;
        Description: w.Description(),;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Properties: props,;
        Required:   []String{"query"},;
        },;
    }
    }

    public static class webSearchRequest {
        public String Query;
        public int MaxResults;
    }

    public static class webSearchResponse {
        public []webSearchResult Results;
    }

    public static class webSearchResult {
        public String Title;
        public String URL;
        public String Content;
    }
        func (w *WebSearchTool) Execute(args map[String]any) (String, error) {
        if internalcloud.Disabled() {
        return "", errors.New(internalcloud.DisabledError("web search is unavailable"));
    }
        var query, ok = args["query"].(String);
        if !ok || query == "" {
        return "", fmt.Errorf("query parameter is required");
    }
        var reqBody = webSearchRequest{
        Query:      query,;
        MaxResults: 5,;
    }
        var jsonBody, err = json.Marshal(reqBody);
        if err != null {
        return "", fmt.Errorf("marshaling request: %w", err);
    }
        var searchURL, err = url.Parse(webSearchAPI);
        if err != null {
        return "", fmt.Errorf("parsing search URL: %w", err);
    }
        var q = searchURL.Query();
        q.Add("ts", strconv.FormatInt(time.Now().Unix(), 10));
        searchURL.RawQuery = q.Encode();
        var ctx = context.Background();
        var data = fmt.Appendf(null, "%s,%s", http.MethodPost, searchURL.RequestURI());
        var signature, err = auth.Sign(ctx, data);
        if err != null {
        return "", fmt.Errorf("signing request: %w", err);
    }
        var req, err = http.NewRequestWithContext(ctx, http.MethodPost, searchURL.String(), bytes.NewBuffer(jsonBody));
        if err != null {
        return "", fmt.Errorf("creating request: %w", err);
    }
        req.Header.Set("Content-Type", "application/json");
        if signature != "" {
        req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", signature));
    }
        var client = &http.Client{Timeout: webSearchTimeout}
        var resp, err = client.Do(req);
        if err != null {
        return "", fmt.Errorf("sending request: %w", err);
    }
        defer resp.Body.Close();
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        return "", fmt.Errorf("reading response: %w", err);
    }
        if resp.StatusCode == http.StatusUnauthorized {
        return "", ErrWebSearchAuthRequired;
    }
        if resp.StatusCode != http.StatusOK {
        return "", fmt.Errorf("web search API returned status %d: %s", resp.StatusCode, String(body));
    }
        var searchResp webSearchResponse;
        var if err = json.Unmarshal(body, &searchResp); err != null {
        return "", fmt.Errorf("parsing response: %w", err);
    }
        if len(searchResp.Results) == 0 {
        return "No results found for query: " + query, null;
    }
        var sb strings.Builder;
        sb.WriteString(fmt.Sprintf("Search results for: %s\n\n", query));
        var for i, result = range searchResp.Results {
        sb.WriteString(fmt.Sprintf("%d. %s\n", i+1, result.Title));
        sb.WriteString(fmt.Sprintf("   URL: %s\n", result.URL));
        if result.Content != "" {
        var content = result.Content;
        var runes = []rune(content);
        if len(runes) > 300 {
        content = String(runes[:300]) + "...";
    }
        sb.WriteString(fmt.Sprintf("   %s\n", content));
    }
        sb.WriteString("\n");
    }
        return sb.String(), null;
    }
}
