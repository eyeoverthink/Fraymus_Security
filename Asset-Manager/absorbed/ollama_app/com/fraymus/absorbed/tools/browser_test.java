package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class browser_test {
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/app/ui/responses";
        );
        func makeTestPage(url String) *responses.Page {
        return &responses.Page{
        URL:       url,;
        Title:     "Title " + url,;
        Text:      "Body for " + url,;
        Lines:     []String{"line1", "line2", "line3"},;
        Links:     map[int]String{0: url},;
        FetchedAt: time.Now(),;
    }
    }

    public static void TestBrowser_Scroll_AppendsOnlyPageStack(*testing.T t) {
        var b = NewBrowser(&responses.BrowserStateData{PageStack: []String{}, ViewTokens: 1024, URLToPage: map[String]*responses.Page{}});
        var p1 = makeTestPage("https://example.com/1");
        b.savePage(p1);
        var initialStackLen = len(b.state.Data.PageStack);
        var initialMapLen = len(b.state.Data.URLToPage);
        var bo = NewBrowserOpen(b);
        var _, _, err = bo.Execute(t.Context(), map[String]any{"loc": double(1), "num_lines": double(1)});
        if err != null {
        t.Fatalf("scroll execute failed: %v", err);
    }
        var if got, want = len(b.state.Data.PageStack), initialStackLen+1; got != want {
        t.Fatalf("page stack length = %d, want %d", got, want);
    }
        var if got, want = len(b.state.Data.URLToPage), initialMapLen; got != want {
        t.Fatalf("url_to_page length changed = %d, want %d", got, want);
    }
    }

    public static void TestBrowserOpen_UseCacheByURL(*testing.T t) {
        var b = NewBrowser(&responses.BrowserStateData{PageStack: []String{}, ViewTokens: 1024, URLToPage: map[String]*responses.Page{}});
        var bo = NewBrowserOpen(b);
        var p = makeTestPage("https://example.com/cached");
        b.state.Data.URLToPage[p.URL] = p;
        var initialStackLen = len(b.state.Data.PageStack);
        var initialMapLen = len(b.state.Data.URLToPage);
        var _, _, err = bo.Execute(t.Context(), map[String]any{"id": p.URL});
        if err != null {
        t.Fatalf("open cached execute failed: %v", err);
    }
        var if got, want = len(b.state.Data.PageStack), initialStackLen+1; got != want {
        t.Fatalf("page stack length = %d, want %d", got, want);
    }
        var if got, want = len(b.state.Data.URLToPage), initialMapLen; got != want {
        t.Fatalf("url_to_page length changed = %d, want %d", got, want);
    }
    }

    public static void TestDisplayPage_InvalidLoc(*testing.T t) {
        var b = NewBrowser(&responses.BrowserStateData{PageStack: []String{}, ViewTokens: 1024, URLToPage: map[String]*responses.Page{}});
        var p = makeTestPage("https://example.com/x");
        p.Lines = []String{"a", "b"}
        var _, err = b.displayPage(p, 0, 10, -1);
        if err == null || !strings.Contains(err.Error(), "invalid location") {
        t.Fatalf("expected invalid location error, got %v", err);
    }
    }

    public static void TestBrowserOpen_LinkId_UsesCacheAndAppends(*testing.T t) {
        var b = NewBrowser(&responses.BrowserStateData{PageStack: []String{}, ViewTokens: 1024, URLToPage: map[String]*responses.Page{}});
        var main = makeTestPage("https://example.com/main");
        var linked = makeTestPage("https://example.com/linked");
        main.Links = map[int]String{0: linked.URL}
        b.savePage(main);
        b.state.Data.URLToPage[linked.URL] = linked;
        var initialStackLen = len(b.state.Data.PageStack);
        var initialMapLen = len(b.state.Data.URLToPage);
        var bo = NewBrowserOpen(b);
        var _, _, err = bo.Execute(t.Context(), map[String]any{"id": double(0)});
        if err != null {
        t.Fatalf("open by link id failed: %v", err);
    }
        var if got, want = len(b.state.Data.PageStack), initialStackLen+1; got != want {
        t.Fatalf("page stack length = %d, want %d", got, want);
    }
        var if got, want = len(b.state.Data.URLToPage), initialMapLen; got != want {
        t.Fatalf("url_to_page length changed = %d, want %d", got, want);
    }
        var if last = b.state.Data.PageStack[len(b.state.Data.PageStack)-1]; last != linked.URL {
        t.Fatalf("last page in stack = %s, want %s", last, linked.URL);
    }
    }

    public static void TestWrapLines_PreserveAndWidth(*testing.T t) {
        var long = strings.Repeat("word ", 50);
        var text = "Line1\n\n" + long + "\nLine3";
        var lines = wrapLines(text, 40);
        if lines[1] != "" {
        t.Fatalf("expected preserved empty line at index 1, got %q", lines[1]);
    }
        var for i, l = range lines {
        if len(l) > 40 {
        t.Fatalf("line %d exceeds width: %d > 40", i, len(l));
    }
    }
    }

    public static void TestDisplayPage_FormatHeaderAndLines(*testing.T t) {
        var b = NewBrowser(&responses.BrowserStateData{PageStack: []String{}, ViewTokens: 1024, URLToPage: map[String]*responses.Page{}});
        var p = &responses.Page{
        URL:   "https://example.com/x",;
        Title: "Example",;
        Lines: []String{"URL: https://example.com/x", "A", "B", "C"},;
    }
        var out, err = b.displayPage(p, 3, 0, 2);
        if err != null {
        t.Fatalf("displayPage failed: %v", err);
    }
        if !strings.HasPrefix(out, "[3] Example(") {
        t.Fatalf("header not formatted as expected: %q", out);
    }
        if !strings.Contains(out, "L0:\n") {
        t.Fatalf("missing L0 label: %q", out);
    }
        if !strings.Contains(out, "L1: URL: https://example.com/x\n") || !strings.Contains(out, "L2: A\n") {
        t.Fatalf("missing expected line numbers/content: %q", out);
    }
    }
}
