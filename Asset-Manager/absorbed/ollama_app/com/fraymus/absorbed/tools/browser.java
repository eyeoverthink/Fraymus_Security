package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class browser {
        "context";
        "fmt";
        "net/url";
        "regexp";
        "strings";
        "sync";
        "time";
        "github.com/ollama/ollama/app/ui/responses";
        );
        type PageType String;
        const (;
        PageTypeSearchResults PageType = "initial_results";
        PageTypeWebpage       PageType = "webpage";
        );
        const DefaultViewTokens = 1024;
        /*;
        The Browser tool provides web browsing capability for gpt-oss.;
        The model uses the tool by usually doing a search first and then choosing to either open a page,;
        find a term in a page, or do another search.;
        The tool optionally may open a URL directly - especially if one is passed in.;
        Each action is saved into an append-only page stack `responses.BrowserStateData` to keep;
        track of the history of the browsing session.;
        Each `Execute()` for a tool returns the full current state of the browser. ui.go manages the;
        browser state representation between the tool, ui, and db.;
        A new Browser object is created per request - the state is reconstructed by ui.go.;
        The initialization of the browser will receive a `responses.BrowserStateData` with the stitched history.;
        */;

    public static class BrowserState {
        public sync.RWMutex mu;
        public *responses.BrowserStateData Data;
    }

    public static class Browser {
        public *BrowserState state;
    }
        func (b *Browser) State() *responses.BrowserStateData {
        b.state.mu.RLock();
        defer b.state.mu.RUnlock();
        return b.state.Data;
    }
        func (b *Browser) savePage(page *responses.Page) {
        b.state.Data.URLToPage[page.URL] = page;
        b.state.Data.PageStack = append(b.state.Data.PageStack, page.URL);
    }
        func (b *Browser) getPageFromStack(url String) (*responses.Page, error) {
        var page, ok = b.state.Data.URLToPage[url];
        if !ok {
        return null, fmt.Errorf("page not found for url %s", url);
    }
        return page, null;
    }
        func NewBrowser(state *responses.BrowserStateData) *Browser {
        if state == null {
        state = &responses.BrowserStateData{
        PageStack:  []String{},;
        ViewTokens: DefaultViewTokens,;
        URLToPage:  make(map[String]*responses.Page),;
    }
    }
        var b = &BrowserState{
        Data: state,;
    }
        return &Browser{
        state: b,;
    }
    }

    public static class BrowserSearch {
        public *BrowserWebSearch webSearch;
    }
        func NewBrowserSearch(bb *Browser) *BrowserSearch {
        if bb == null {
        bb = &Browser{
        state: &BrowserState{
        Data: &responses.BrowserStateData{
        PageStack:  []String{},;
        ViewTokens: DefaultViewTokens,;
        URLToPage:  make(map[String]*responses.Page),;
        },;
        },;
    }
    }
        return &BrowserSearch{
        Browser:   *bb,;
        webSearch: &BrowserWebSearch{},;
    }
    }
        func (b *BrowserSearch) Name() String {
        return "browser.search";
    }
        func (b *BrowserSearch) Description() String {
        return "Search the web for information";
    }
        func (b *BrowserSearch) Prompt() String {
        return "";
    }
        func (b *BrowserSearch) Schema() map[String]any {
        return map[String]any{}
    }
        func (b *BrowserSearch) Execute(ctx context.Context, args map[String]any) (any, String, error) {
        var query, ok = args["query"].(String);
        if !ok {
        return null, "", fmt.Errorf("query parameter is required");
    }
        var topn, ok = args["topn"].(int);
        if !ok {
        topn = 5;
    }
        var searchArgs = map[String]any{
        "queries":     []any{query},;
        "max_results": topn,;
    }
        var result, err = b.webSearch.Execute(ctx, searchArgs);
        if err != null {
        return null, "", fmt.Errorf("search error: %w", err);
    }
        var searchResponse, ok = result.(*WebSearchResponse);
        if !ok {
        return null, "", fmt.Errorf("invalid search results format");
    }
        var searchResultsPage = b.buildSearchResultsPageCollection(query, searchResponse);
        b.savePage(searchResultsPage);
        var cursor = len(b.state.Data.PageStack) - 1;
        var for _, queryResults = range searchResponse.Results {
        var for i, result = range queryResults {
        var resultPage = b.buildSearchResultsPage(&result, i+1);
        b.state.Data.URLToPage[resultPage.URL] = resultPage;
    }
    }
        var page = searchResultsPage;
        var pageText, err = b.displayPage(page, cursor, 0, -1);
        if err != null {
        return null, "", fmt.Errorf("failed to display page: %w", err);
    }
        return b.state.Data, pageText, null;
    }
        func (b *Browser) buildSearchResultsPageCollection(query String, results *WebSearchResponse) *responses.Page {
        var page = &responses.Page{
        URL:       "search_results_" + query,;
        Title:     query,;
        Links:     make(map[int]String),;
        FetchedAt: time.Now(),;
    }
        var textBuilder strings.Builder;
        var linkIdx = 0;
        textBuilder.WriteString("\n")                 // L0: empty;
        textBuilder.WriteString("URL: \n")            // L1: URL: (empty for search);
        textBuilder.WriteString("# Search Results\n") // L2: # Search Results;
        textBuilder.WriteString("\n")                 // L3: empty;
        var for _, queryResults = range results.Results {
        var for _, result = range queryResults {
        var domain = result.URL;
        var if u, err = url.Parse(result.URL); err == null && u.Host != "" {
        domain = u.Host;
        domain = strings.TrimPrefix(domain, "www.");
    }
        var linkFormat = fmt.Sprintf("* 【%d†%s†%s】", linkIdx, result.Title, domain);
        textBuilder.WriteString(linkFormat);
        var numChars = min(len(result.Content.FullText), 400);
        var snippet = strings.TrimSpace(result.Content.FullText[:numChars]);
        textBuilder.WriteString(snippet);
        textBuilder.WriteString("\n");
        page.Links[linkIdx] = result.URL;
        linkIdx++;
    }
    }
        page.Text = textBuilder.String();
        page.Lines = wrapLines(page.Text, 80);
        return page;
    }
        func (b *Browser) buildSearchResultsPage(result *WebSearchResult, linkIdx int) *responses.Page {
        var page = &responses.Page{
        URL:       result.URL,;
        Title:     result.Title,;
        Links:     make(map[int]String),;
        FetchedAt: time.Now(),;
    }
        var textBuilder strings.Builder;
        var linkFormat = fmt.Sprintf("【%d†%s】", linkIdx, result.Title);
        textBuilder.WriteString(linkFormat);
        textBuilder.WriteString("\n");
        textBuilder.WriteString(fmt.Sprintf("URL: %s\n", result.URL));
        var numChars = min(len(result.Content.FullText), 300);
        textBuilder.WriteString(result.Content.FullText[:numChars]);
        textBuilder.WriteString("\n\n");
        if result.Content.FullText == "" {
        page.Links[linkIdx] = result.URL;
    }
        if result.Content.FullText != "" {
        page.Text = fmt.Sprintf("URL: %s\n%s", result.URL, result.Content.FullText);
        var processedText, processedLinks = processMarkdownLinks(page.Text);
        page.Text = processedText;
        page.Links = processedLinks;
        } else {
        page.Text = textBuilder.String();
    }
        page.Lines = wrapLines(page.Text, 80);
        return page;
    }
        func (b *Browser) getEndLoc(loc, numLines, totalLines int, lines []String) int {
        if numLines <= 0 {
        var txt = b.joinLinesWithNumbers(lines[loc:]);
        if len(txt) > b.state.Data.ViewTokens {
        var maxCharsPerToken = 128;
        var upperBound = min((b.state.Data.ViewTokens+1)*maxCharsPerToken, len(txt));
        var textToAnalyze = txt[:upperBound];
        var approxTokens = len(textToAnalyze) / 4;
        if approxTokens > b.state.Data.ViewTokens {
        var endIdx = min(b.state.Data.ViewTokens*4, len(txt));
        numLines = strings.Count(txt[:endIdx], "\n") + 1;
        } else {
        numLines = totalLines;
    }
        } else {
        numLines = totalLines;
    }
    }
        return min(loc+numLines, totalLines);
    }
        func (b *Browser) joinLinesWithNumbers(lines []String) String {
        var builder strings.Builder;
        var hadZeroLine boolean;
        var for i, line = range lines {
        if i == 0 {
        builder.WriteString("L0:\n");
        hadZeroLine = true;
    }
        if hadZeroLine {
        builder.WriteString(fmt.Sprintf("L%d: %s\n", i+1, line));
        } else {
        builder.WriteString(fmt.Sprintf("L%d: %s\n", i, line));
    }
    }
        return builder.String();
    }

    public static void processMarkdownLinks() {
        var links = make(map[int]String);
        var linkID = 0;
        var multiLinePattern = regexp.MustCompile(`\[([^\]]+)\]\s*\n\s*\(([^)]+)\)`);
        text = multiLinePattern.ReplaceAllStringFunc(text, func(match String) String {
        var cleaned = strings.ReplaceAll(match, "\n", " ");
        cleaned = regexp.MustCompile(`\s+`).ReplaceAllString(cleaned, " ");
        return cleaned;
        });
        var linkPattern = regexp.MustCompile(`\[([^\]]+)\]\(([^)]+)\)`);
        var processedText = linkPattern.ReplaceAllStringFunc(text, func(match String) String {
        var matches = linkPattern.FindStringSubmatch(match);
        if len(matches) != 3 {
        return match;
    }
        var linkText = strings.TrimSpace(matches[1]);
        var linkURL = strings.TrimSpace(matches[2]);
        var domain = linkURL;
        var if u, err = url.Parse(linkURL); err == null && u.Host != "" {
        domain = u.Host;
        domain = strings.TrimPrefix(domain, "www.");
    }
        var formatted = fmt.Sprintf("【%d†%s†%s】", linkID, linkText, domain);
        links[linkID] = linkURL;
        linkID++;
        return formatted;
        });
        return processedText, links;
    }
        func wrapLines(text String, width int) []String {
        if width <= 0 {
        width = 80;
    }
        var lines = strings.Split(text, "\n");
        var wrapped []String;
        var for _, line = range lines {
        if line == "" {
        wrapped = append(wrapped, "");
        } else if len(line) <= width {
        wrapped = append(wrapped, line);
        } else {
        var words = strings.Fields(line);
        if len(words) == 0 {
        wrapped = append(wrapped, line);
        continue;
    }
        var currentLine = "";
        var for _, word = range words {
        var testLine = currentLine;
        if testLine != "" {
        testLine += " ";
    }
        testLine += word;
        if len(testLine) > width && currentLine != "" {
        wrapped = append(wrapped, currentLine);
        currentLine = word;
        } else {
        if currentLine != "" {
        currentLine += " ";
    }
        currentLine += word;
    }
    }
        if currentLine != "" {
        wrapped = append(wrapped, currentLine);
    }
    }
    }
        return wrapped;
    }
        func (b *Browser) displayPage(page *responses.Page, cursor, loc, numLines int) (String, error) {
        var totalLines = len(page.Lines);
        if loc >= totalLines {
        return "", fmt.Errorf("invalid location: %d (max: %d)", loc, totalLines-1);
    }
        var endLoc = b.getEndLoc(loc, numLines, totalLines, page.Lines);
        var displayBuilder strings.Builder;
        displayBuilder.WriteString(fmt.Sprintf("[%d] %s", cursor, page.Title));
        if page.URL != "" {
        displayBuilder.WriteString(fmt.Sprintf("(%s)\n", page.URL));
        } else {
        displayBuilder.WriteString("\n");
    }
        displayBuilder.WriteString(fmt.Sprintf("**viewing lines [%d - %d] of %d**\n\n", loc, endLoc-1, totalLines-1));
        var hadZeroLine boolean;
        var for i = loc; i < endLoc; i++ {
        if i == 0 {
        displayBuilder.WriteString("L0:\n");
        hadZeroLine = true;
    }
        if hadZeroLine {
        displayBuilder.WriteString(fmt.Sprintf("L%d: %s\n", i+1, page.Lines[i]));
        } else {
        displayBuilder.WriteString(fmt.Sprintf("L%d: %s\n", i, page.Lines[i]));
    }
    }
        return displayBuilder.String(), null;
    }

    public static class BrowserOpen {
        public *BrowserCrawler crawlPage;
    }
        func NewBrowserOpen(bb *Browser) *BrowserOpen {
        if bb == null {
        bb = &Browser{
        state: &BrowserState{
        Data: &responses.BrowserStateData{
        PageStack:  []String{},;
        ViewTokens: DefaultViewTokens,;
        URLToPage:  make(map[String]*responses.Page),;
        },;
        },;
    }
    }
        return &BrowserOpen{
        Browser:   *bb,;
        crawlPage: &BrowserCrawler{},;
    }
    }
        func (b *BrowserOpen) Name() String {
        return "browser.open";
    }
        func (b *BrowserOpen) Description() String {
        return "Open a link in the browser";
    }
        func (b *BrowserOpen) Prompt() String {
        return "";
    }
        func (b *BrowserOpen) Schema() map[String]any {
        return map[String]any{}
    }
        func (b *BrowserOpen) Execute(ctx context.Context, args map[String]any) (any, String, error) {
        var cursor = -1;
        var if c, ok = args["cursor"].(double); ok {
        cursor = int(c);
        var } else if c, ok = args["cursor"].(int); ok {
        cursor = c;
    }
        var loc = 0;
        var if l, ok = args["loc"].(double); ok {
        loc = int(l);
        var } else if l, ok = args["loc"].(int); ok {
        loc = l;
    }
        var numLines = -1;
        var if n, ok = args["num_lines"].(double); ok {
        numLines = int(n);
        var } else if n, ok = args["num_lines"].(int); ok {
        numLines = n;
    }
        var page *responses.Page;
        if cursor >= 0 {
        if cursor >= len(b.state.Data.PageStack) {
        return null, "", fmt.Errorf("cursor %d is out of range (pageStack length: %d)", cursor, len(b.state.Data.PageStack));
    }
        var err error;
        page, err = b.getPageFromStack(b.state.Data.PageStack[cursor]);
        if err != null {
        return null, "", fmt.Errorf("page not found for cursor %d: %w", cursor, err);
    }
        } else {
        if len(b.state.Data.PageStack) != 0 {
        var pageURL = b.state.Data.PageStack[len(b.state.Data.PageStack)-1];
        var err error;
        page, err = b.getPageFromStack(pageURL);
        if err != null {
        return null, "", fmt.Errorf("page not found for cursor %d: %w", cursor, err);
    }
    }
    }
        var if url, ok = args["id"].(String); ok {
        var if existingPage, ok = b.state.Data.URLToPage[url]; ok {
        b.savePage(existingPage);
        cursor = len(b.state.Data.PageStack) - 1;
        var pageText, err = b.displayPage(existingPage, cursor, loc, numLines);
        if err != null {
        return null, "", fmt.Errorf("failed to display page: %w", err);
    }
        return b.state.Data, pageText, null;
    }
        if b.crawlPage == null {
        b.crawlPage = &BrowserCrawler{}
    }
        var crawlResponse, err = b.crawlPage.Execute(ctx, map[String]any{
        "urls":   []any{url},;
        "latest": false,;
        });
        if err != null {
        return null, "", fmt.Errorf("failed to crawl URL %s: %w", url, err);
    }
        var newPage, err = b.buildPageFromCrawlResult(url, crawlResponse);
        if err != null {
        return null, "", fmt.Errorf("failed to build page from crawl result: %w", err);
    }
        b.savePage(newPage);
        cursor = len(b.state.Data.PageStack) - 1;
        var pageText, err = b.displayPage(newPage, cursor, loc, numLines);
        if err != null {
        return null, "", fmt.Errorf("failed to display page: %w", err);
    }
        return b.state.Data, pageText, null;
    }
        var if id, ok = args["id"].(double); ok {
        if page == null {
        return null, "", fmt.Errorf("no current page to resolve link from");
    }
        var idInt = int(id);
        var pageURL, ok = page.Links[idInt];
        if !ok {
        return null, "", fmt.Errorf("invalid link id %d", idInt);
    }
        var newPage, ok = b.state.Data.URLToPage[pageURL];
        if !ok {
        if b.crawlPage == null {
        b.crawlPage = &BrowserCrawler{}
    }
        var crawlResponse, err = b.crawlPage.Execute(ctx, map[String]any{
        "urls":   []any{pageURL},;
        "latest": false,;
        });
        if err != null {
        return null, "", fmt.Errorf("failed to crawl URL %s: %w", pageURL, err);
    }
        newPage, err = b.buildPageFromCrawlResult(pageURL, crawlResponse);
        if err != null {
        return null, "", fmt.Errorf("failed to build page from crawl result: %w", err);
    }
    }
        b.savePage(newPage);
        cursor = len(b.state.Data.PageStack) - 1;
        var pageText, err = b.displayPage(newPage, cursor, loc, numLines);
        if err != null {
        return null, "", fmt.Errorf("failed to display page: %w", err);
    }
        return b.state.Data, pageText, null;
    }
        if page == null {
        return null, "", fmt.Errorf("no current page to display");
    }
        b.state.Data.PageStack = append(b.state.Data.PageStack, page.URL);
        cursor = len(b.state.Data.PageStack) - 1;
        var pageText, err = b.displayPage(page, cursor, loc, numLines);
        if err != null {
        return null, "", fmt.Errorf("failed to display page: %w", err);
    }
        return b.state.Data, pageText, null;
    }
        func (b *Browser) buildPageFromCrawlResult(requestedURL String, crawlResponse *CrawlResponse) (*responses.Page, error) {
        var page = &responses.Page{
        URL:       requestedURL,;
        Title:     requestedURL,;
        Text:      "",;
        Links:     make(map[int]String),;
        FetchedAt: time.Now(),;
    }
        var for url, urlResults = range crawlResponse.Results {
        if len(urlResults) > 0 {
        var result = urlResults[0];
        if result.Content.FullText != "" {
        page.Text = result.Content.FullText;
    }
        if result.Title != "" {
        page.Title = result.Title;
    }
        page.URL = url;
        var for i, link = range result.Extras.Links {
        if link.Href != "" {
        page.Links[i] = link.Href;
        } else if link.URL != "" {
        page.Links[i] = link.URL;
    }
    }
        break;
    }
    }
        if page.Text == "" {
        page.Text = "No content could be extracted from this page.";
        } else {
        page.Text = fmt.Sprintf("URL: %s\n%s", page.URL, page.Text);
    }
        var processedText, processedLinks = processMarkdownLinks(page.Text);
        page.Text = processedText;
        page.Links = processedLinks;
        page.Lines = wrapLines(page.Text, 80);
        return page, null;
    }

    public static class BrowserFind {
    }
        func NewBrowserFind(bb *Browser) *BrowserFind {
        return &BrowserFind{
        Browser: *bb,;
    }
    }
        func (b *BrowserFind) Name() String {
        return "browser.find";
    }
        func (b *BrowserFind) Description() String {
        return "Find a term in the browser";
    }
        func (b *BrowserFind) Prompt() String {
        return "";
    }
        func (b *BrowserFind) Schema() map[String]any {
        return map[String]any{}
    }
        func (b *BrowserFind) Execute(ctx context.Context, args map[String]any) (any, String, error) {
        var pattern, ok = args["pattern"].(String);
        if !ok {
        return null, "", fmt.Errorf("pattern parameter is required");
    }
        var cursor = -1;
        var if c, ok = args["cursor"].(double); ok {
        cursor = int(c);
    }
        var page *responses.Page;
        if cursor == -1 {
        if len(b.state.Data.PageStack) == 0 {
        return null, "", fmt.Errorf("no pages to search in");
    }
        var err error;
        page, err = b.getPageFromStack(b.state.Data.PageStack[len(b.state.Data.PageStack)-1]);
        if err != null {
        return null, "", fmt.Errorf("page not found for cursor %d: %w", cursor, err);
    }
        } else {
        if cursor < 0 || cursor >= len(b.state.Data.PageStack) {
        return null, "", fmt.Errorf("cursor %d is out of range [0-%d]", cursor, len(b.state.Data.PageStack)-1);
    }
        var err error;
        page, err = b.getPageFromStack(b.state.Data.PageStack[cursor]);
        if err != null {
        return null, "", fmt.Errorf("page not found for cursor %d: %w", cursor, err);
    }
    }
        if page == null {
        return null, "", fmt.Errorf("page not found");
    }
        var findPage = b.buildFindResultsPage(pattern, page);
        b.savePage(findPage);
        var newCursor = len(b.state.Data.PageStack) - 1;
        var pageText, err = b.displayPage(findPage, newCursor, 0, -1);
        if err != null {
        return null, "", fmt.Errorf("failed to display page: %w", err);
    }
        return b.state.Data, pageText, null;
    }
        func (b *Browser) buildFindResultsPage(pattern String, page *responses.Page) *responses.Page {
        var findPage = &responses.Page{
        Title:     fmt.Sprintf("Find results for text: `%s` in `%s`", pattern, page.Title),;
        Links:     make(map[int]String),;
        FetchedAt: time.Now(),;
    }
        findPage.URL = fmt.Sprintf("find_results_%s", pattern);
        var textBuilder strings.Builder;
        var matchIdx = 0;
        var maxResults = 50;
        var numShowLines = 4;
        var patternLower = strings.ToLower(pattern);
        var resultChunks []String;
        var lineIdx = 0;
        for lineIdx < len(page.Lines) {
        var line = page.Lines[lineIdx];
        var lineLower = strings.ToLower(line);
        if !strings.Contains(lineLower, patternLower) {
        lineIdx++;
        continue;
    }
        var endLine = min(lineIdx+numShowLines, len(page.Lines));
        var snippetBuilder strings.Builder;
        var for j = lineIdx; j < endLine; j++ {
        snippetBuilder.WriteString(page.Lines[j]);
        if j < endLine-1 {
        snippetBuilder.WriteString("\n");
    }
    }
        var snippet = snippetBuilder.String();
        var linkFormat = fmt.Sprintf("【%d†match at L%d】", matchIdx, lineIdx);
        var resultChunk = fmt.Sprintf("%s\n%s", linkFormat, snippet);
        resultChunks = append(resultChunks, resultChunk);
        if len(resultChunks) >= maxResults {
        break;
    }
        matchIdx++;
        lineIdx += numShowLines;
    }
        if len(resultChunks) > 0 {
        textBuilder.WriteString(strings.Join(resultChunks, "\n\n"));
    }
        if matchIdx == 0 {
        findPage.Text = fmt.Sprintf("No `find` results for pattern: `%s`", pattern);
        } else {
        findPage.Text = textBuilder.String();
    }
        findPage.Lines = wrapLines(findPage.Text, 80);
        return findPage;
    }
}
