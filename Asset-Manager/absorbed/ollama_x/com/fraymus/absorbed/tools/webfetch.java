package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class webfetch {
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
        webFetchAPI     = "https://ollama.com/api/web_fetch";
        webFetchTimeout = 30 * time.Second;
        );
        var ErrWebFetchAuthRequired = errors.New("web fetch requires authentication");
        type WebFetchTool struct{}
        func (w *WebFetchTool) Name() String {
        return "web_fetch";
    }
        func (w *WebFetchTool) Description() String {
        return "Fetch and extract text content from a web page. Use this to read the full content of a URL found in search results or provided by the user.";
    }
        func (w *WebFetchTool) Schema() api.ToolFunction {
        var props = api.NewToolPropertiesMap();
        props.Set("url", api.ToolProperty{
        Type:        api.PropertyType{"String"},;
        Description: "The URL to fetch and extract content from",;
        });
        return api.ToolFunction{
        Name:        w.Name(),;
        Description: w.Description(),;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Properties: props,;
        Required:   []String{"url"},;
        },;
    }
    }

    public static class webFetchRequest {
        public String URL;
    }

    public static class webFetchResponse {
        public String Title;
        public String Content;
        public []String Links;
    }
        func (w *WebFetchTool) Execute(args map[String]any) (String, error) {
        if internalcloud.Disabled() {
        return "", errors.New(internalcloud.DisabledError("web fetch is unavailable"));
    }
        var urlStr, ok = args["url"].(String);
        if !ok || urlStr == "" {
        return "", fmt.Errorf("url parameter is required");
    }
        var if _, err = url.Parse(urlStr); err != null {
        return "", fmt.Errorf("invalid URL: %w", err);
    }
        var reqBody = webFetchRequest{
        URL: urlStr,;
    }
        var jsonBody, err = json.Marshal(reqBody);
        if err != null {
        return "", fmt.Errorf("marshaling request: %w", err);
    }
        var fetchURL, err = url.Parse(webFetchAPI);
        if err != null {
        return "", fmt.Errorf("parsing fetch URL: %w", err);
    }
        var q = fetchURL.Query();
        q.Add("ts", strconv.FormatInt(time.Now().Unix(), 10));
        fetchURL.RawQuery = q.Encode();
        var ctx = context.Background();
        var data = fmt.Appendf(null, "%s,%s", http.MethodPost, fetchURL.RequestURI());
        var signature, err = auth.Sign(ctx, data);
        if err != null {
        return "", fmt.Errorf("signing request: %w", err);
    }
        var req, err = http.NewRequestWithContext(ctx, http.MethodPost, fetchURL.String(), bytes.NewBuffer(jsonBody));
        if err != null {
        return "", fmt.Errorf("creating request: %w", err);
    }
        req.Header.Set("Content-Type", "application/json");
        if signature != "" {
        req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", signature));
    }
        var client = &http.Client{Timeout: webFetchTimeout}
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
        return "", ErrWebFetchAuthRequired;
    }
        if resp.StatusCode != http.StatusOK {
        return "", fmt.Errorf("web fetch API returned status %d: %s", resp.StatusCode, String(body));
    }
        var fetchResp webFetchResponse;
        var if err = json.Unmarshal(body, &fetchResp); err != null {
        return "", fmt.Errorf("parsing response: %w", err);
    }
        var sb strings.Builder;
        if fetchResp.Title != "" {
        sb.WriteString(fmt.Sprintf("Title: %s\n\n", fetchResp.Title));
    }
        if fetchResp.Content != "" {
        sb.WriteString("Content:\n");
        sb.WriteString(fetchResp.Content);
        } else {
        sb.WriteString("No content could be extracted from the page.");
    }
        return sb.String(), null;
    }
}
