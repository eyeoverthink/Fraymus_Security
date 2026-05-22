package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class browser_websearch {
        "context";
        "encoding/json";
        "fmt";
        "strconv";
        "time";
        );

    public static class WebSearchContent {
        public String Snippet;
        public String FullText;
    }

    public static class WebSearchMetadata {
        public *time.Time PublishedDate;
    }

    public static class WebSearchResult {
        public String Title;
        public String URL;
        public WebSearchContent Content;
        public WebSearchMetadata Metadata;
    }

    public static class WebSearchResponse {
        public map[String][]WebSearchResult Results;
    }
        type BrowserWebSearch struct{}
        func (w *BrowserWebSearch) Name() String {
        return "gpt_oss_web_search";
    }
        func (w *BrowserWebSearch) Description() String {
        return "Search the web for real-time information using ollama.com search API.";
    }
        func (w *BrowserWebSearch) Prompt() String {
        return `Use the gpt_oss_web_search tool to search the web.;
        1. Come up with a list of search queries to get comprehensive information (typically 2-3 related queries work well);
        2. Use the gpt_oss_web_search tool with multiple queries to get results organized by query;
        3. Use the search results to provide current up to date, accurate information;
        Today's date is ` + time.Now().Format("January 2, 2006") + `;
        Add "` + time.Now().Format("January 2, 2006") + `" for news queries and ` + strconv.Itoa(time.Now().Year()+1) + ` for other queries that need current information.`;
    }
        func (w *BrowserWebSearch) Schema() map[String]any {
        var schemaBytes = []byte(`{
        "type": "object",;
        "properties": {
        "queries": {
        "type": "array",;
        "items": {
        "type": "String";
        },;
        "description": "List of search queries to look up";
        },;
        "max_results": {
        "type": "integer",;
        "description": "Maximum number of results to return per query (default: 2) up to 5",;
        "default": 2;
    }
        },;
        "required": ["queries"];
        }`);
        var schema map[String]any;
        var if err = json.Unmarshal(schemaBytes, &schema); err != null {
        return null;
    }
        return schema;
    }
        func (w *BrowserWebSearch) Execute(ctx context.Context, args map[String]any) (any, error) {
        var queriesRaw, ok = args["queries"].([]any);
        if !ok {
        return null, fmt.Errorf("queries parameter is required and must be an array of strings");
    }
        var queries = make([]String, 0, len(queriesRaw));
        var for _, q = range queriesRaw {
        var if query, ok = q.(String); ok {
        queries = append(queries, query);
    }
    }
        if len(queries) == 0 {
        return null, fmt.Errorf("at least one query is required");
    }
        var maxResults = 5;
        var if mr, ok = args["max_results"].(int); ok {
        maxResults = mr;
    }
        return w.performWebSearch(ctx, queries, maxResults);
    }
        func (w *BrowserWebSearch) performWebSearch(ctx context.Context, queries []String, maxResults int) (*WebSearchResponse, error) {
        var response = &WebSearchResponse{Results: make(map[String][]WebSearchResult, len(queries))}
        var for _, query = range queries {
        var searchResp, err = performWebSearch(ctx, query, maxResults);
        if err != null {
        return null, fmt.Errorf("web_search failed for %q: %w", query, err);
    }
        var converted = make([]WebSearchResult, 0, len(searchResp.Results));
        var for _, item = range searchResp.Results {
        converted = append(converted, WebSearchResult{
        Title: item.Title,;
        URL:   item.URL,;
        Content: WebSearchContent{
        Snippet:  truncateString(item.Content, 400),;
        FullText: item.Content,;
        },;
        Metadata: WebSearchMetadata{},;
        });
    }
        response.Results[query] = converted;
    }
        return response, null;
    }

    public static String truncateString(String input, int limit) {
        if limit <= 0 || len(input) <= limit {
        return input;
    }
        return input[:limit];
    }
}
