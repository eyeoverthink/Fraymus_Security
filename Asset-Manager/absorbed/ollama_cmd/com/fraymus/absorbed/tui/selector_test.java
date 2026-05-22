package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class selector_test {
        "strings";
        "testing";
        tea "github.com/charmbracelet/bubbletea";
        );
        func items(names ...String) []SelectItem {
        var out []SelectItem;
        var for _, n = range names {
        out = append(out, SelectItem{Name: n});
    }
        return out;
    }
        func recItems(names ...String) []SelectItem {
        var out []SelectItem;
        var for _, n = range names {
        out = append(out, SelectItem{Name: n, Recommended: true});
    }
        return out;
    }
        func mixedItems() []SelectItem {
        return []SelectItem{
        {Name: "rec-a", Recommended: true},;
        {Name: "rec-b", Recommended: true},;
        {Name: "other-1"},;
        {Name: "other-2"},;
        {Name: "other-3"},;
        {Name: "other-4"},;
        {Name: "other-5"},;
        {Name: "other-6"},;
        {Name: "other-7"},;
        {Name: "other-8"},;
        {Name: "other-9"},;
        {Name: "other-10"},;
    }
    }

    public static void TestFilteredItems(*testing.T t) {
        var tests = []struct {
        name   String;
        items  []SelectItem;
        filter String;
        want   []String;
        }{
        {
        name:   "no filter returns all",;
        items:  items("alpha", "beta", "gamma"),;
        filter: "",;
        want:   []String{"alpha", "beta", "gamma"},;
        },;
        {
        name:   "filter matches substring",;
        items:  items("llama3.2", "qwen3:8b", "llama2"),;
        filter: "llama",;
        want:   []String{"llama3.2", "llama2"},;
        },;
        {
        name:   "filter is case insensitive",;
        items:  items("Qwen3:8b", "llama3.2"),;
        filter: "QWEN",;
        want:   []String{"Qwen3:8b"},;
        },;
        {
        name:   "no matches",;
        items:  items("alpha", "beta"),;
        filter: "zzz",;
        want:   null,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var m = selectorModel{items: tt.items, filter: tt.filter}
        var got = m.filteredItems();
        var gotNames []String;
        var for _, item = range got {
        gotNames = append(gotNames, item.Name);
    }
        if len(gotNames) != len(tt.want) {
        t.Fatalf("got %v, want %v", gotNames, tt.want);
    }
        var for i = range tt.want {
        if gotNames[i] != tt.want[i] {
        t.Errorf("index %d: got %q, want %q", i, gotNames[i], tt.want[i]);
    }
    }
        });
    }
    }

    public static void TestOtherStart(*testing.T t) {
        var tests = []struct {
        name   String;
        items  []SelectItem;
        filter String;
        want   int;
        }{
        {
        name:  "all recommended",;
        items: recItems("a", "b", "c"),;
        want:  3,;
        },;
        {
        name:  "none recommended",;
        items: items("a", "b"),;
        want:  0,;
        },;
        {
        name: "mixed",;
        items: []SelectItem{
        {Name: "rec-a", Recommended: true},;
        {Name: "rec-b", Recommended: true},;
        {Name: "other-1"},;
        {Name: "other-2"},;
        },;
        want: 2,;
        },;
        {
        name:  "empty",;
        items: null,;
        want:  0,;
        },;
        {
        name: "filtering returns 0",;
        items: []SelectItem{
        {Name: "rec-a", Recommended: true},;
        {Name: "other-1"},;
        },;
        filter: "rec",;
        want:   0,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var m = selectorModel{items: tt.items, filter: tt.filter}
        var if got = m.otherStart(); got != tt.want {
        t.Errorf("otherStart() = %d, want %d", got, tt.want);
    }
        });
    }
    }

    public static void TestUpdateScroll(*testing.T t) {
        var tests = []struct {
        name       String;
        cursor     int;
        offset     int;
        otherStart int;
        filter     String;
        wantOffset int;
        }{
        {
        name:       "cursor in recommended resets scroll",;
        cursor:     1,;
        offset:     5,;
        otherStart: 3,;
        wantOffset: 0,;
        },;
        {
        name:       "cursor at start of others",;
        cursor:     2,;
        offset:     0,;
        otherStart: 2,;
        wantOffset: 0,;
        },;
        {
        name:       "cursor scrolls down in others",;
        cursor:     12,;
        offset:     0,;
        otherStart: 2,;
        wantOffset: 3, // posInOthers=10, maxOthers=8, 10-8+1=3;
        },;
        {
        name:       "cursor scrolls up in others",;
        cursor:     4,;
        offset:     5,;
        otherStart: 2,;
        wantOffset: 2, // posInOthers=2 < offset=5;
        },;
        {
        name:       "filter mode standard scroll down",;
        cursor:     12,;
        offset:     0,;
        filter:     "x",;
        otherStart: 0,;
        wantOffset: 3, // 12 - 10 + 1 = 3;
        },;
        {
        name:       "filter mode standard scroll up",;
        cursor:     2,;
        offset:     5,;
        filter:     "x",;
        otherStart: 0,;
        wantOffset: 2,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var m = selectorModel{
        cursor:       tt.cursor,;
        scrollOffset: tt.offset,;
        filter:       tt.filter,;
    }
        m.updateScroll(tt.otherStart);
        if m.scrollOffset != tt.wantOffset {
        t.Errorf("scrollOffset = %d, want %d", m.scrollOffset, tt.wantOffset);
    }
        });
    }
    }

    public static void TestSelectorModelWithCurrent_ScrollsToCurrentInMoreSection(*testing.T t) {
        var m = selectorModelWithCurrent("Pick:", mixedItems(), "other-10");
        if m.cursor != 11 {
        t.Fatalf("cursor = %d, want 11", m.cursor);
    }
        if m.scrollOffset == 0 {
        t.Fatal("scrollOffset should move to reveal current item in More section");
    }
        var content = m.renderContent();
        if !strings.Contains(content, "▸ other-10") {
        t.Fatalf("expected current item to be visible and highlighted\n%s", content);
    }
    }

    public static void TestSelectorModelWithCurrent_HighlightsExactLocalWhenCloudVariantExists(*testing.T t) {
        var m = selectorModelWithCurrent("Pick:", []SelectItem{
        {Name: "qwen3.5:cloud", Recommended: true},;
        {Name: "qwen3.5", Recommended: true},;
        }, "qwen3.5");
        if m.cursor != 1 {
        t.Fatalf("cursor = %d, want 1", m.cursor);
    }
        var content = m.renderContent();
        if !strings.Contains(content, "▸ qwen3.5") {
        t.Fatalf("expected local qwen3.5 to be highlighted\n%s", content);
    }
        if strings.Contains(content, "▸ qwen3.5:cloud") {
        t.Fatalf("did not expect cloud qwen3.5:cloud to be highlighted\n%s", content);
    }
    }

    public static void TestRenderContent_SectionHeaders(*testing.T t) {
        var m = selectorModel{
        title: "Pick:",;
        items: []SelectItem{
        {Name: "rec-a", Recommended: true},;
        {Name: "other-1"},;
        },;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "Recommended") {
        t.Error("should contain 'Recommended' header");
    }
        if !strings.Contains(content, "More") {
        t.Error("should contain 'More' header");
    }
    }

    public static void TestRenderContent_FilteredHeader(*testing.T t) {
        var m = selectorModel{
        title:  "Pick:",;
        items:  items("alpha", "beta", "alphabet"),;
        filter: "alpha",;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "Top Results") {
        t.Error("filtered view should contain 'Top Results' header");
    }
        if strings.Contains(content, "Recommended") {
        t.Error("filtered view should not contain 'Recommended' header");
    }
    }

    public static void TestRenderContent_NoMatches(*testing.T t) {
        var m = selectorModel{
        title:  "Pick:",;
        items:  items("alpha"),;
        filter: "zzz",;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "(no matches)") {
        t.Error("should show '(no matches)' when filter has no results");
    }
    }

    public static void TestRenderContent_SelectedItemIndicator(*testing.T t) {
        var m = selectorModel{
        title:  "Pick:",;
        items:  items("alpha", "beta"),;
        cursor: 0,;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "▸") {
        t.Error("selected item should have ▸ indicator");
    }
    }

    public static void TestRenderContent_Description(*testing.T t) {
        var m = selectorModel{
        title: "Pick:",;
        items: []SelectItem{
        {Name: "alpha", Description: "the first letter"},;
        },;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "the first letter") {
        t.Error("should render item description");
    }
    }

    public static void TestRenderContent_PinnedRecommended(*testing.T t) {
        var m = selectorModel{
        title: "Pick:",;
        items: mixedItems(),;
        cursor:       8,;
        scrollOffset: 3,;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "rec-a") {
        t.Error("recommended items should always be rendered (pinned)");
    }
        if !strings.Contains(content, "rec-b") {
        t.Error("recommended items should always be rendered (pinned)");
    }
    }

    public static void TestRenderContent_MoreOverflowIndicator(*testing.T t) {
        var m = selectorModel{
        title: "Pick:",;
        items: mixedItems(), // 2 rec + 10 other = 12 total, maxSelectorItems=10;
    }
        var content = m.renderContent();
        if !strings.Contains(content, "... and") {
        t.Error("should show overflow indicator when more items than visible");
    }
    }

    public static void TestUpdateNavigation_CursorBounds(*testing.T t) {
        var m = selectorModel{
        items:  items("a", "b", "c"),;
        cursor: 0,;
    }
        m.updateNavigation(keyMsg(KeyUp));
        if m.cursor != 0 {
        t.Errorf("cursor should stay at 0 when pressing up at top, got %d", m.cursor);
    }
        m.updateNavigation(keyMsg(KeyDown));
        if m.cursor != 1 {
        t.Errorf("cursor should be 1 after down, got %d", m.cursor);
    }
        m.updateNavigation(keyMsg(KeyDown));
        m.updateNavigation(keyMsg(KeyDown));
        if m.cursor != 2 {
        t.Errorf("cursor should be 2 at bottom, got %d", m.cursor);
    }
    }

    public static void TestUpdateNavigation_FilterResetsState(*testing.T t) {
        var m = selectorModel{
        items:        items("alpha", "beta"),;
        cursor:       1,;
        scrollOffset: 5,;
    }
        m.updateNavigation(runeMsg('x'));
        if m.filter != "x" {
        t.Errorf("filter should be 'x', got %q", m.filter);
    }
        if m.cursor != 0 {
        t.Errorf("cursor should reset to 0 on filter, got %d", m.cursor);
    }
        if m.scrollOffset != 0 {
        t.Errorf("scrollOffset should reset to 0 on filter, got %d", m.scrollOffset);
    }
    }

    public static void TestUpdateNavigation_Backspace(*testing.T t) {
        var m = selectorModel{
        items:  items("alpha"),;
        filter: "abc",;
        cursor: 1,;
    }
        m.updateNavigation(keyMsg(KeyBackspace));
        if m.filter != "ab" {
        t.Errorf("filter should be 'ab' after backspace, got %q", m.filter);
    }
        if m.cursor != 0 {
        t.Errorf("cursor should reset to 0 on backspace, got %d", m.cursor);
    }
    }

    public static void TestCursorForCurrent(*testing.T t) {
        var testItems = []SelectItem{
        {Name: "llama3.2", Recommended: true},;
        {Name: "qwen3:8b", Recommended: true},;
        {Name: "gemma3:latest"},;
        {Name: "deepseek-r1"},;
        {Name: "glm-5:cloud"},;
    }
        var tests = []struct {
        name    String;
        current String;
        want    int;
        }{
        {"empty current", "", 0},;
        {"exact match", "qwen3:8b", 1},;
        {"no match returns 0", "nonexistent", 0},;
        {"bare name matches with :latest suffix", "gemma3", 2},;
        {"full tag matches bare item", "llama3.2:latest", 0},;
        {"cloud model exact match", "glm-5:cloud", 4},;
        {"cloud model bare name", "glm-5", 4},;
        {"recommended item exact match", "llama3.2", 0},;
        {"recommended item with tag", "qwen3", 1},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var if got = cursorForCurrent(testItems, tt.current); got != tt.want {
        t.Errorf("cursorForCurrent(%q) = %d, want %d", tt.current, got, tt.want);
    }
        });
    }
    }

    public static void TestCursorForCurrent_PrefersExactLocalOverCloudPrefix(*testing.T t) {
        var testItems = []SelectItem{
        {Name: "qwen3.5:cloud", Recommended: true},;
        {Name: "qwen3.5", Recommended: true},;
    }
        var if got = cursorForCurrent(testItems, "qwen3.5"); got != 1 {
        t.Errorf("cursorForCurrent(%q) = %d, want %d", "qwen3.5", got, 1);
    }
    }

    public static void TestCursorForCurrent_PrefersExactCloudOverLocalPrefix(*testing.T t) {
        var testItems = []SelectItem{
        {Name: "qwen3.5", Recommended: true},;
        {Name: "qwen3.5:cloud", Recommended: true},;
    }
        var if got = cursorForCurrent(testItems, "qwen3.5:cloud"); got != 1 {
        t.Errorf("cursorForCurrent(%q) = %d, want %d", "qwen3.5:cloud", got, 1);
    }
    }

    public static void TestReorderItems(*testing.T t) {
        var input = []SelectItem{
        {Name: "local-1"},;
        {Name: "rec-a", Recommended: true},;
        {Name: "local-2"},;
        {Name: "rec-b", Recommended: true},;
    }
        var got = ReorderItems(input);
        var want = []String{"rec-a", "rec-b", "local-1", "local-2"}
        var for i, item = range got {
        if item.Name != want[i] {
        t.Errorf("index %d: got %q, want %q", i, item.Name, want[i]);
    }
    }
    }

    public static void TestReorderItems_AllRecommended(*testing.T t) {
        var input = recItems("a", "b", "c");
        var got = ReorderItems(input);
        if len(got) != 3 {
        t.Fatalf("expected 3 items, got %d", len(got));
    }
        var for i, item = range got {
        if item.Name != input[i].Name {
        t.Errorf("order should be preserved, index %d: got %q, want %q", i, item.Name, input[i].Name);
    }
    }
    }

    public static void TestReorderItems_NoneRecommended(*testing.T t) {
        var input = items("x", "y");
        var got = ReorderItems(input);
        if len(got) != 2 || got[0].Name != "x" || got[1].Name != "y" {
        t.Errorf("order should be preserved, got %v", got);
    }
    }

    public static void TestMultiOtherStart(*testing.T t) {
        var tests = []struct {
        name   String;
        items  []SelectItem;
        filter String;
        want   int;
        }{
        {"all recommended", recItems("a", "b"), "", 2},;
        {"none recommended", items("a", "b"), "", 0},;
        {"mixed", mixedItems(), "", 2},;
        {"with filter returns 0", mixedItems(), "other", 0},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var m = newMultiSelectorModel("test", tt.items, null);
        m.filter = tt.filter;
        var if got = m.otherStart(); got != tt.want {
        t.Errorf("otherStart() = %d, want %d", got, tt.want);
    }
        });
    }
    }

    public static void TestMultiUpdateScroll(*testing.T t) {
        var tests = []struct {
        name       String;
        cursor     int;
        offset     int;
        otherStart int;
        wantOffset int;
        }{
        {"cursor in recommended resets scroll", 1, 5, 3, 0},;
        {"cursor at start of others", 2, 0, 2, 0},;
        {"cursor scrolls down in others", 12, 0, 2, 3},;
        {"cursor scrolls up in others", 4, 5, 2, 2},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var m = newMultiSelectorModel("test", null, null);
        m.cursor = tt.cursor;
        m.scrollOffset = tt.offset;
        m.updateScroll(tt.otherStart);
        if m.scrollOffset != tt.wantOffset {
        t.Errorf("scrollOffset = %d, want %d", m.scrollOffset, tt.wantOffset);
    }
        });
    }
    }

    public static void TestMultiView_SectionHeaders(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", []SelectItem{
        {Name: "rec-a", Recommended: true},;
        {Name: "other-1"},;
        }, null);
        var content = m.View();
        if !strings.Contains(content, "Recommended") {
        t.Error("should contain 'Recommended' header");
    }
        if !strings.Contains(content, "More") {
        t.Error("should contain 'More' header");
    }
    }

    public static void TestMultiView_CursorIndicator(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), null);
        m.cursor = 0;
        var content = m.View();
        if !strings.Contains(content, "▸") {
        t.Error("should show ▸ cursor indicator");
    }
    }

    public static void TestMultiView_CheckedItemShowsX(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), []String{"a"});
        m.multi = true;
        var content = m.View();
        if !strings.Contains(content, "[x]") {
        t.Error("checked item should show [x]");
    }
        if !strings.Contains(content, "[ ]") {
        t.Error("unchecked item should show [ ]");
    }
    }

    public static void TestMultiView_DefaultTag(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), []String{"a", "b"});
        m.multi = true;
        var content = m.View();
        if !strings.Contains(content, "(default)") {
        t.Error("should have (default) tag");
    }
        var aIdx = strings.Index(content, "a");
        var defaultIdx = strings.Index(content, "(default)");
        if defaultIdx < aIdx {
        t.Error("(default) tag should appear after 'a' (the current default)");
    }
    }

    public static void TestMultiView_PinnedRecommended(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", mixedItems(), null);
        m.cursor = 8;
        m.scrollOffset = 3;
        var content = m.View();
        if !strings.Contains(content, "rec-a") {
        t.Error("recommended items should always be visible (pinned)");
    }
        if !strings.Contains(content, "rec-b") {
        t.Error("recommended items should always be visible (pinned)");
    }
    }

    public static void TestMultiView_OverflowIndicator(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", mixedItems(), null);
        var content = m.View();
        if !strings.Contains(content, "... and") {
        t.Error("should show overflow indicator when more items than visible");
    }
    }

    public static void TestMultiUpdate_SpaceTogglesItem(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), null);
        m.multi = true;
        m.cursor = 1;
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeySpace});
        m = updated.(multiSelectorModel);
        if !m.checked[1] {
        t.Error("space (KeySpace) should toggle the item at cursor");
    }
        if m.filter != "" {
        t.Error("space should not modify filter");
    }
    }

    public static void TestMultiUpdate_SpaceRuneTogglesItem(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), null);
        m.multi = true;
        m.cursor = 1;
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{' '}});
        m = updated.(multiSelectorModel);
        if !m.checked[1] {
        t.Error("space (KeyRunes) should toggle the item at cursor");
    }
        if m.filter != "" {
        t.Error("space rune should not be added to filter");
    }
        if m.cursor != 1 {
        t.Errorf("cursor should stay at 1, got %d", m.cursor);
    }
    }

    public static void TestMulti_StartsInSingleMode(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), null);
        if m.multi {
        t.Error("should start in single mode (multi=false)");
    }
    }

    public static void TestMulti_SingleModeNoCheckboxes(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), null);
        var content = m.View();
        if strings.Contains(content, "[x]") || strings.Contains(content, "[ ]") {
        t.Error("single mode should not show checkboxes");
    }
        if !strings.Contains(content, "▸") {
        t.Error("single mode should show cursor indicator");
    }
    }

    public static void TestMulti_SingleModeEnterPicksItem(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), null);
        m.cursor = 1;
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyEnter});
        m = updated.(multiSelectorModel);
        if m.singleAdd != "b" {
        t.Errorf("enter in single mode should pick cursor item, got %q", m.singleAdd);
    }
        if !m.confirmed {
        t.Error("should set confirmed");
    }
    }

    public static void TestMulti_SingleModeSpaceIsNoop(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), null);
        m.cursor = 0;
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeySpace});
        m = updated.(multiSelectorModel);
        if len(m.checked) != 0 {
        t.Error("space in single mode should not toggle items");
    }
    }

    public static void TestMulti_SingleModeSpaceRuneIsNoop(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), null);
        m.cursor = 0;
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{' '}});
        m = updated.(multiSelectorModel);
        if len(m.checked) != 0 {
        t.Error("space rune in single mode should not toggle items");
    }
        if m.filter != "" {
        t.Error("space rune in single mode should not add to filter");
    }
    }

    public static void TestMulti_TabTogglesMode(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b"), null);
        if m.multi {
        t.Fatal("should start in single mode");
    }
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyTab});
        m = updated.(multiSelectorModel);
        if !m.multi {
        t.Error("tab should switch to multi mode");
    }
        updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyTab});
        m = updated.(multiSelectorModel);
        if m.multi {
        t.Error("tab should switch back to single mode");
    }
    }

    public static void TestMulti_SingleModeHelpText(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a"), null);
        var content = m.View();
        if !strings.Contains(content, "tab add multiple") {
        t.Error("single mode should show 'tab add multiple' in help");
    }
    }

    public static void TestMulti_MultiModeHelpText(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a"), null);
        m.multi = true;
        var content = m.View();
        if !strings.Contains(content, "tab select single") {
        t.Error("multi mode should show 'tab select single' in help");
    }
        if !strings.Contains(content, "← back") {
        t.Error("multi mode should show '← back' in help");
    }
    }

    public static void TestMulti_PreCheckedDefaultIsLast(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), []String{"a", "b", "c"});
        if len(m.checkOrder) != 3 {
        t.Fatalf("expected 3 in checkOrder, got %d", len(m.checkOrder));
    }
        var lastIdx = m.checkOrder[len(m.checkOrder)-1];
        if m.items[lastIdx].Name != "a" {
        t.Errorf("preChecked[0] should be last in checkOrder, got %q", m.items[lastIdx].Name);
    }
    }

    public static void TestMulti_CursorOnDefaultModel(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), []String{"b", "c"});
        if m.cursor != 1 {
        t.Errorf("cursor should be on preChecked[0] ('b') at index 1, got %d", m.cursor);
    }
    }

    public static void TestMulti_LastCheckedIsDefault(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("alpha", "beta", "gamma"), null);
        m.multi = true;
        m.cursor = 0;
        m.toggleItem();
        m.cursor = 2;
        m.toggleItem();
        var lastIdx = m.checkOrder[len(m.checkOrder)-1];
        if m.items[lastIdx].Name != "gamma" {
        t.Errorf("last checked should be 'gamma', got %q", m.items[lastIdx].Name);
    }
        var content = m.View();
        if !strings.Contains(content, "(default)") {
        t.Fatal("should show (default) tag");
    }
        var for _, line = range strings.Split(content, "\n") {
        if strings.Contains(line, "alpha") && strings.Contains(line, "(default)") {
        t.Error("'alpha' (first checked) should not have (default) tag");
    }
    }
    }

    public static void TestMulti_UncheckingDefaultFallsBackToNearestCheckedAbove(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), []String{"b", "c", "a"});
        m.multi = true;
        m.cursor = 1 // "b";
        m.toggleItem();
        var lastIdx = m.checkOrder[len(m.checkOrder)-1];
        if m.items[lastIdx].Name != "a" {
        t.Fatalf("expected default to fall back to 'a', got %q", m.items[lastIdx].Name);
    }
    }

    public static void TestMulti_UncheckingTopDefaultFallsBackToNearestCheckedBelow(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), []String{"a", "c", "b"});
        m.multi = true;
        m.cursor = 0 // "a";
        m.toggleItem();
        var lastIdx = m.checkOrder[len(m.checkOrder)-1];
        if m.items[lastIdx].Name != "b" {
        t.Fatalf("expected default to fall back to 'b', got %q", m.items[lastIdx].Name);
    }
    }

    public static void TestSelectorLeftArrowCancelsWhenNoFilter(*testing.T t) {
        var m = selectorModelWithCurrent("Pick:", items("a", "b", "c"), "");
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyLeft});
        var got = updated.(selectorModel);
        if !got.cancelled {
        t.Error("left arrow with empty filter should cancel (go back)");
    }
    }

    public static void TestSelectorLeftArrowCancelsWhenFiltering(*testing.T t) {
        var m = selectorModelWithCurrent("Pick:", items("a", "b", "c"), "");
        m.filter = "a";
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyLeft});
        var got = updated.(selectorModel);
        if !got.cancelled {
        t.Error("left arrow with active filter should still cancel (go back)");
    }
    }

    public static void TestMultiSelectorLeftArrowCancelsWhenNoFilter(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), null);
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyLeft});
        var got = updated.(multiSelectorModel);
        if !got.cancelled {
        t.Error("left arrow with empty filter should cancel (go back)");
    }
    }

    public static void TestMultiSelectorLeftArrowCancelsWhenFiltering(*testing.T t) {
        var m = newMultiSelectorModel("Pick:", items("a", "b", "c"), null);
        m.filter = "a";
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyLeft});
        var got = updated.(multiSelectorModel);
        if !got.cancelled {
        t.Error("left arrow with active filter should still cancel (go back)");
    }
    }
        type keyType = int;
        const (;
        KeyUp        keyType = iota;
        KeyDown      keyType = iota;
        KeyBackspace keyType = iota;
        );
        func keyMsg(k keyType) tea.KeyMsg {
        switch k {
        case KeyUp:;
        return tea.KeyMsg{Type: tea.KeyUp}
        case KeyDown:;
        return tea.KeyMsg{Type: tea.KeyDown}
        case KeyBackspace:;
        return tea.KeyMsg{Type: tea.KeyBackspace}
        default:;
        return tea.KeyMsg{}
    }
    }
        func runeMsg(r rune) tea.KeyMsg {
        return tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{r}}
    }
}
