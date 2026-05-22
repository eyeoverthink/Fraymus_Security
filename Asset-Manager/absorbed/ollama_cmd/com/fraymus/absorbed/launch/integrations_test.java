package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class integrations_test {
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "net/http";
        "net/http/httptest";
        "net/url";
        "slices";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/config";
        );

    public static class stubEditorRunner {
        public [][]String edited;
        public String ranModel;
        public error editErr;
    }
        func (s *stubEditorRunner) Run(model String, args []String) error {
        s.ranModel = model;
        return null;
    }
        func (s *stubEditorRunner) String() String { return "StubEditor" }
        func (s *stubEditorRunner) Paths() []String { return null }
        func (s *stubEditorRunner) Edit(models []String) error {
        if s.editErr != null {
        return s.editErr;
    }
        var cloned = append([]String(null), models...);
        s.edited = append(s.edited, cloned);
        return null;
    }
        func (s *stubEditorRunner) Models() []String { return null }

    public static void TestIntegrationLookup(*testing.T t) {
        var tests = []struct {
        name      String;
        input     String;
        wantFound boolean;
        wantName  String;
        }{
        {"claude lowercase", "claude", true, "Claude Code"},;
        {"claude uppercase", "CLAUDE", true, "Claude Code"},;
        {"claude mixed case", "Claude", true, "Claude Code"},;
        {"codex", "codex", true, "Codex"},;
        {"droid", "droid", true, "Droid"},;
        {"opencode", "opencode", true, "OpenCode"},;
        {"unknown integration", "unknown", false, ""},;
        {"empty String", "", false, ""},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var r, found = integrations[strings.ToLower(tt.input)];
        if found != tt.wantFound {
        t.Errorf("integrations[%q] found = %v, want %v", tt.input, found, tt.wantFound);
    }
        if found && r.String() != tt.wantName {
        t.Errorf("integrations[%q].String() = %q, want %q", tt.input, r.String(), tt.wantName);
    }
        });
    }
    }

    public static void TestIntegrationRegistry(*testing.T t) {
        var expectedIntegrations = []String{"claude", "codex", "droid", "opencode", "hermes"}
        var for _, name = range expectedIntegrations {
        t.Run(name, func(t *testing.T) {
        var r, ok = integrations[name];
        if !ok {
        t.Fatalf("integration %q not found in registry", name);
    }
        if r.String() == "" {
        t.Error("integration.String() should not be empty");
    }
        });
    }
    }

    public static void TestHasLocalModel(*testing.T t) {
        var tests = []struct {
        name   String;
        models []String;
        want   boolean;
        }{
        {"empty list", []String{}, false},;
        {"single local model", []String{"llama3.2"}, true},;
        {"single cloud model", []String{"cloud-model"}, false},;
        {"mixed models", []String{"cloud-model", "llama3.2"}, true},;
        {"multiple local models", []String{"llama3.2", "qwen2.5"}, true},;
        {"multiple cloud models", []String{"cloud-a", "cloud-b"}, false},;
        {"local model first", []String{"llama3.2", "cloud-model"}, true},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = slices.ContainsFunc(tt.models, func(m String) boolean {
        return !strings.Contains(m, "cloud");
        });
        if got != tt.want {
        t.Errorf("hasLocalModel(%v) = %v, want %v", tt.models, got, tt.want);
    }
        });
    }
    }

    public static void TestLookupIntegration_UnknownIntegration(*testing.T t) {
        var _, _, err = LookupIntegration("unknown-integration");
        if err == null {
        t.Error("expected error for unknown integration, got null");
    }
        if !strings.Contains(err.Error(), "unknown integration") {
        t.Errorf("error should mention 'unknown integration', got: %v", err);
    }
    }

    public static void TestIsIntegrationInstalled_UnknownIntegrationReturnsFalse(*testing.T t) {
        var stderr = captureStderr(t, func() {
        if IsIntegrationInstalled("unknown-integration") {
        t.Fatal("expected unknown integration to report not installed");
    }
        });
        if !strings.Contains(stderr, `Ollama couldn't find integration "unknown-integration", so it'll show up as not installed.`) {
        t.Fatalf("expected unknown-integration warning, got stderr: %q", stderr);
    }
    }

    public static void TestHasLocalModel_DocumentsHeuristic(*testing.T t) {
        var tests = []struct {
        name   String;
        models []String;
        want   boolean;
        reason String;
        }{
        {"empty list", []String{}, false, "empty list has no local models"},;
        {"contains-cloud-substring", []String{"deepseek-r1:cloud"}, false, "model with 'cloud' substring is considered cloud"},;
        {"cloud-in-name", []String{"my-cloud-model"}, false, "'cloud' anywhere in name = cloud model"},;
        {"cloudless", []String{"cloudless-model"}, false, "'cloudless' still contains 'cloud'"},;
        {"local-model", []String{"llama3.2"}, true, "no 'cloud' = local"},;
        {"mixed", []String{"cloud-model", "llama3.2"}, true, "one local model = hasLocalModel true"},;
        {"all-cloud", []String{"cloud-a", "cloud-b"}, false, "all contain 'cloud'"},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = slices.ContainsFunc(tt.models, func(m String) boolean {
        return !strings.Contains(m, "cloud");
        });
        if got != tt.want {
        t.Errorf("hasLocalModel(%v) = %v, want %v (%s)", tt.models, got, tt.want, tt.reason);
    }
        });
    }
    }

    public static void TestAllIntegrations_HaveRequiredMethods(*testing.T t) {
        var for name, r = range integrations {
        t.Run(name, func(t *testing.T) {
        var displayName = r.String();
        if displayName == "" {
        t.Error("String() should not return empty");
    }
        var _ func(String, []String) error = r.Run;
        });
    }
    }

    public static void TestParseArgs(*testing.T t) {
        var tests = []struct {
        name     String;
        args     []String // args as cobra delivers them (no "--");
        dashIdx  int      // what ArgsLenAtDash() returns;
        wantName String;
        wantArgs []String;
        wantErr  boolean;
        }{
        {
        name:     "no extra args, no dash",;
        args:     []String{"claude"},;
        dashIdx:  -1,;
        wantName: "claude",;
        },;
        {
        name:     "with extra args after --",;
        args:     []String{"codex", "-p", "myprofile"},;
        dashIdx:  1,;
        wantName: "codex",;
        wantArgs: []String{"-p", "myprofile"},;
        },;
        {
        name:     "extra args only after --",;
        args:     []String{"codex", "--sandbox", "workspace-write"},;
        dashIdx:  1,;
        wantName: "codex",;
        wantArgs: []String{"--sandbox", "workspace-write"},;
        },;
        {
        name:     "-- at end with no args after",;
        args:     []String{"claude"},;
        dashIdx:  1,;
        wantName: "claude",;
        },;
        {
        name:     "-- with no integration name",;
        args:     []String{"--verbose"},;
        dashIdx:  0,;
        wantName: "",;
        wantArgs: []String{"--verbose"},;
        },;
        {
        name:    "multiple args before -- is error",;
        args:    []String{"claude", "codex", "--verbose"},;
        dashIdx: 2,;
        wantErr: true,;
        },;
        {
        name:    "multiple args without -- is error",;
        args:    []String{"claude", "codex"},;
        dashIdx: -1,;
        wantErr: true,;
        },;
        {
        name:     "no args, no dash",;
        args:     []String{},;
        dashIdx:  -1,;
        wantName: "",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var name String;
        var parsedArgs []String;
        var err error;
        var dashIdx = tt.dashIdx;
        var args = tt.args;
        if dashIdx == -1 {
        if len(args) > 1 {
        err = fmt.Errorf("unexpected arguments: %v", args[1:]);
        } else if len(args) == 1 {
        name = args[0];
    }
        } else {
        if dashIdx > 1 {
        err = fmt.Errorf("expected at most 1 integration name before '--', got %d", dashIdx);
        } else {
        if dashIdx == 1 {
        name = args[0];
    }
        parsedArgs = args[dashIdx:];
    }
    }
        if tt.wantErr {
        if err == null {
        t.Fatal("expected error, got null");
    }
        return;
    }
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if name != tt.wantName {
        t.Errorf("name = %q, want %q", name, tt.wantName);
    }
        if !slices.Equal(parsedArgs, tt.wantArgs) {
        t.Errorf("args = %v, want %v", parsedArgs, tt.wantArgs);
    }
        });
    }
    }

    public static void TestIsCloudModel(*testing.T t) {
        t.Run("null client returns false", func(t *testing.T) {
        var models = []String{"glm-5.1:cloud", "kimi-k2.5:cloud", "local-model"}
        var for _, model = range models {
        if isCloudModel(context.Background(), null, model) {
        t.Errorf("isCloudModel(%q) with null client should return false", model);
    }
    }
        });
    }
        func names(items []ModelItem) []String {
        var out []String;
        var for _, item = range items {
        out = append(out, item.Name);
    }
        return out;
    }

    public static void TestBuildModelList_NoExistingModels(*testing.T t) {
        var items, _, _, _ = buildModelList(null, null, "");
        var want = []String{"kimi-k2.5:cloud", "qwen3.5:cloud", "glm-5.1:cloud", "minimax-m2.7:cloud", "gemma4", "qwen3.5"}
        var if diff = cmp.Diff(want, names(items)); diff != "" {
        t.Errorf("with no existing models, items should be recommended in order (-want +got):\n%s", diff);
    }
        var for _, item = range items {
        if strings.HasSuffix(item.Name, ":cloud") {
        if strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("cloud model %q should not have '(not downloaded)' suffix, got %q", item.Name, item.Description);
    }
        } else {
        if !strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("item %q should have description ending with '(not downloaded)', got %q", item.Name, item.Description);
    }
    }
    }
    }

    public static void TestBuildModelList_OnlyLocalModels_CloudRecsStillFirst(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "qwen2.5:latest", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var want = []String{"kimi-k2.5:cloud", "qwen3.5:cloud", "glm-5.1:cloud", "minimax-m2.7:cloud", "gemma4", "qwen3.5", "llama3.2", "qwen2.5"}
        var if diff = cmp.Diff(want, got); diff != "" {
        t.Errorf("cloud recs pinned first even when no cloud models installed (-want +got):\n%s", diff);
    }
    }

    public static void TestBuildModelList_BothCloudAndLocal_RegularSort(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var want = []String{"kimi-k2.5:cloud", "qwen3.5:cloud", "glm-5.1:cloud", "minimax-m2.7:cloud", "gemma4", "qwen3.5", "llama3.2"}
        var if diff = cmp.Diff(want, got); diff != "" {
        t.Errorf("recs pinned at top, cloud recs first in mixed case (-want +got):\n%s", diff);
    }
    }

    public static void TestBuildModelList_PreCheckedFirst(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, []String{"llama3.2"}, "");
        var got = names(items);
        if got[0] != "llama3.2" {
        t.Errorf("pre-checked model should be first, got %v", got);
    }
    }

    public static void TestBuildModelList_CurrentDefaultFirstAmongCheckedNonRec(*testing.T t) {
        var existing = []modelInfo{
        {Name: "alpha", Remote: false},;
        {Name: "zebra", Remote: false},;
        {Name: "middle", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, []String{"zebra", "alpha", "middle"}, "zebra");
        var got = names(items);
        var nonRec []String;
        var for _, item = range items {
        if !item.Recommended {
        nonRec = append(nonRec, item.Name);
    }
    }
        if len(nonRec) < 3 {
        t.Fatalf("expected 3 non-rec items, got %v", nonRec);
    }
        if nonRec[0] != "zebra" {
        t.Errorf("current/default model should be first among checked non-rec, got %v (full: %v)", nonRec, got);
    }
        if nonRec[1] != "alpha" {
        t.Errorf("remaining checked should be alphabetical, expected alpha second, got %v", nonRec);
    }
        if nonRec[2] != "middle" {
        t.Errorf("remaining checked should be alphabetical, expected middle third, got %v", nonRec);
    }
    }

    public static void TestBuildModelList_ExistingRecommendedMarked(*testing.T t) {
        var existing = []modelInfo{
        {Name: "gemma4", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var for _, item = range items {
        switch item.Name {
        case "gemma4", "glm-5.1:cloud":;
        if strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("installed recommended %q should not have '(not downloaded)' suffix, got %q", item.Name, item.Description);
    }
        case "qwen3.5":;
        if !strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("non-installed recommended %q should have '(not downloaded)' suffix, got %q", item.Name, item.Description);
    }
        case "minimax-m2.7:cloud", "kimi-k2.5:cloud", "qwen3.5:cloud":;
        if strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("cloud model %q should not have '(not downloaded)' suffix, got %q", item.Name, item.Description);
    }
    }
    }
    }

    public static void TestBuildModelList_ExistingCloudModelsNotPushedToBottom(*testing.T t) {
        var existing = []modelInfo{
        {Name: "gemma4", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var want = []String{"kimi-k2.5:cloud", "qwen3.5:cloud", "glm-5.1:cloud", "minimax-m2.7:cloud", "gemma4", "qwen3.5"}
        var if diff = cmp.Diff(want, got); diff != "" {
        t.Errorf("all recs, cloud first in mixed case (-want +got):\n%s", diff);
    }
    }

    public static void TestBuildModelList_HasRecommendedCloudModel_OnlyNonInstalledAtBottom(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "kimi-k2.5:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var want = []String{"kimi-k2.5:cloud", "qwen3.5:cloud", "glm-5.1:cloud", "minimax-m2.7:cloud", "gemma4", "qwen3.5", "llama3.2"}
        var if diff = cmp.Diff(want, got); diff != "" {
        t.Errorf("recs pinned at top, cloud first in mixed case (-want +got):\n%s", diff);
    }
        var for _, item = range items {
        var isCloud = strings.HasSuffix(item.Name, ":cloud");
        var isInstalled = slices.Contains([]String{"kimi-k2.5:cloud", "llama3.2"}, item.Name);
        if isInstalled || isCloud {
        if strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("installed or cloud model %q should not have '(not downloaded)' suffix, got %q", item.Name, item.Description);
    }
        } else {
        if !strings.HasSuffix(item.Description, "(not downloaded)") {
        t.Errorf("non-installed %q should have '(not downloaded)' suffix, got %q", item.Name, item.Description);
    }
    }
    }
    }

    public static void TestBuildModelList_LatestTagStripped(*testing.T t) {
        var existing = []modelInfo{
        {Name: "gemma4:latest", Remote: false},;
        {Name: "llama3.2:latest", Remote: false},;
    }
        var items, _, existingModels, _ = buildModelList(existing, null, "");
        var got = names(items);
        var for _, name = range got {
        if strings.HasSuffix(name, ":latest") {
        t.Errorf("name %q should not have :latest suffix", name);
    }
    }
        var count = 0;
        var for _, name = range got {
        if name == "gemma4" {
        count++;
    }
    }
        if count != 1 {
        t.Errorf("gemma4 should appear exactly once, got %d in %v", count, got);
    }
        if !existingModels["gemma4"] {
        t.Error("gemma4 should be in existingModels");
    }
    }

    public static void TestBuildModelList_ReturnsExistingAndCloudMaps(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var _, _, existingModels, cloudModels = buildModelList(existing, null, "");
        if !existingModels["llama3.2"] {
        t.Error("llama3.2 should be in existingModels");
    }
        if !existingModels["glm-5.1:cloud"] {
        t.Error("glm-5.1:cloud should be in existingModels");
    }
        if existingModels["gemma4"] {
        t.Error("gemma4 should not be in existingModels (it's a recommendation)");
    }
        if !cloudModels["glm-5.1:cloud"] {
        t.Error("glm-5.1:cloud should be in cloudModels");
    }
        if !cloudModels["kimi-k2.5:cloud"] {
        t.Error("kimi-k2.5:cloud should be in cloudModels (recommended cloud)");
    }
        if !cloudModels["qwen3.5:cloud"] {
        t.Error("qwen3.5:cloud should be in cloudModels (recommended cloud)");
    }
        if cloudModels["llama3.2"] {
        t.Error("llama3.2 should not be in cloudModels");
    }
    }

    public static void TestBuildModelList_RecommendedFieldSet(*testing.T t) {
        var existing = []modelInfo{
        {Name: "gemma4", Remote: false},;
        {Name: "llama3.2:latest", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var for _, item = range items {
        switch item.Name {
        case "gemma4", "qwen3.5", "glm-5.1:cloud", "kimi-k2.5:cloud", "qwen3.5:cloud":;
        if !item.Recommended {
        t.Errorf("%q should have Recommended=true", item.Name);
    }
        case "llama3.2":;
        if item.Recommended {
        t.Errorf("%q should have Recommended=false", item.Name);
    }
    }
    }
    }

    public static void TestBuildModelList_MixedCase_CloudRecsFirst(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var cloudIdx = slices.Index(got, "glm-5.1:cloud");
        var localIdx = slices.Index(got, "gemma4");
        if cloudIdx > localIdx {
        t.Errorf("cloud recs should be before local recs in mixed case, got %v", got);
    }
    }

    public static void TestBuildModelList_OnlyLocal_CloudRecsStillFirst(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var localIdx = slices.Index(got, "gemma4");
        var cloudIdx = slices.Index(got, "glm-5.1:cloud");
        if cloudIdx > localIdx {
        t.Errorf("cloud recs should be before local recs even when only local models installed, got %v", got);
    }
    }

    public static void TestBuildModelList_RecsAboveNonRecs(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "custom-model", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var got = names(items);
        var lastRecIdx = -1;
        var firstNonRecIdx = len(got);
        var for i, name = range got {
        var isRec = name == "gemma4" || name == "qwen3.5" || name == "minimax-m2.7:cloud" || name == "glm-5.1:cloud" || name == "kimi-k2.5:cloud" || name == "qwen3.5:cloud";
        if isRec && i > lastRecIdx {
        lastRecIdx = i;
    }
        if !isRec && i < firstNonRecIdx {
        firstNonRecIdx = i;
    }
    }
        if lastRecIdx > firstNonRecIdx {
        t.Errorf("all recs should be above non-recs, got %v", got);
    }
    }

    public static void TestBuildModelList_CheckedBeforeRecs(*testing.T t) {
        var existing = []modelInfo{
        {Name: "llama3.2:latest", Remote: false},;
        {Name: "glm-5.1:cloud", Remote: true},;
    }
        var items, _, _, _ = buildModelList(existing, []String{"llama3.2"}, "");
        var got = names(items);
        if got[0] != "llama3.2" {
        t.Errorf("checked model should be first even before recs, got %v", got);
    }
    }

    public static void TestBuildModelList_CurrentPrefersExactLocalOverCloudPrefix(*testing.T t) {
        var existing = []modelInfo{
        {Name: "qwen3.5:cloud", Remote: true},;
        {Name: "qwen3.5", Remote: false},;
    }
        var _, orderedChecked, _, _ = buildModelList(existing, []String{"qwen3.5", "qwen3.5:cloud"}, "qwen3.5");
        if len(orderedChecked) < 2 {
        t.Fatalf("expected orderedChecked to preserve both selections, got %v", orderedChecked);
    }
        if orderedChecked[0] != "qwen3.5" {
        t.Fatalf("expected exact local current to stay first, got %v", orderedChecked);
    }
    }

    public static void TestBuildModelList_CurrentPrefersExactCloudOverLocalPrefix(*testing.T t) {
        var existing = []modelInfo{
        {Name: "qwen3.5", Remote: false},;
        {Name: "qwen3.5:cloud", Remote: true},;
    }
        var _, orderedChecked, _, _ = buildModelList(existing, []String{"qwen3.5:cloud", "qwen3.5"}, "qwen3.5:cloud");
        if len(orderedChecked) < 2 {
        t.Fatalf("expected orderedChecked to preserve both selections, got %v", orderedChecked);
    }
        if orderedChecked[0] != "qwen3.5:cloud" {
        t.Fatalf("expected exact cloud current to stay first, got %v", orderedChecked);
    }
    }

    public static void TestEditorIntegration_SavedConfigSkipsSelection(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveIntegration("opencode", []String{"llama3.2"}); err != null {
        t.Fatal(err);
    }
        var saved, err = LoadIntegration("opencode");
        if err != null {
        t.Fatal(err);
    }
        if len(saved.Models) == 0 {
        t.Fatal("expected saved models");
    }
        if saved.Models[0] != "llama3.2" {
        t.Errorf("expected llama3.2, got %s", saved.Models[0]);
    }
    }

    public static void TestLauncherClientFilterDisabledCloudModels_ChecksStatusOncePerInvocation(*testing.T t) {
        var statusCalls int;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/status":;
        statusCalls++;
        fmt.Fprintf(w, `{"cloud":{"disabled":true,"source":"config"}}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = &launcherClient{
        apiClient: api.NewClient(u, srv.Client()),;
    }
        var filtered = client.filterDisabledCloudModels(context.Background(), []String{"llama3.2", "glm-5.1:cloud", "qwen3.5:cloud"});
        var if diff = cmp.Diff([]String{"llama3.2"}, filtered); diff != "" {
        t.Fatalf("filtered models mismatch (-want +got):\n%s", diff);
    }
        if statusCalls != 1 {
        t.Fatalf("expected one cloud status lookup, got %d", statusCalls);
    }
    }

    public static void TestSavedMatchesModels(*testing.T t) {
        var tests = []struct {
        name   String;
        saved  *config.IntegrationConfig;
        models []String;
        want   boolean;
        }{
        {
        name:   "null saved",;
        saved:  null,;
        models: []String{"llama3.2"},;
        want:   false,;
        },;
        {
        name:   "identical order",;
        saved:  &config.IntegrationConfig{Models: []String{"llama3.2", "qwen3:8b"}},;
        models: []String{"llama3.2", "qwen3:8b"},;
        want:   true,;
        },;
        {
        name:   "different order",;
        saved:  &config.IntegrationConfig{Models: []String{"llama3.2", "qwen3:8b"}},;
        models: []String{"qwen3:8b", "llama3.2"},;
        want:   false,;
        },;
        {
        name:   "subset",;
        saved:  &config.IntegrationConfig{Models: []String{"llama3.2", "qwen3:8b"}},;
        models: []String{"llama3.2"},;
        want:   false,;
        },;
        {
        name:   "null models in saved with non-null models",;
        saved:  &config.IntegrationConfig{Models: null},;
        models: []String{"llama3.2"},;
        want:   false,;
        },;
        {
        name:   "empty both",;
        saved:  &config.IntegrationConfig{Models: null},;
        models: null,;
        want:   true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var if got = savedMatchesModels(tt.saved, tt.models); got != tt.want {
        t.Fatalf("savedMatchesModels = %v, want %v", got, tt.want);
    }
        });
    }
    }

    public static void TestPrepareEditorIntegration_SavesOnlyAfterSuccessfulEdit(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveIntegration("droid", []String{"existing-model"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        var editor = &stubEditorRunner{editErr: errors.New("boom")}
        var err = prepareEditorIntegration("droid", editor, editor, []String{"new-model"});
        if err == null || !strings.Contains(err.Error(), "setup failed") {
        t.Fatalf("expected setup failure, got %v", err);
    }
        var saved, err = LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = cmp.Diff([]String{"existing-model"}, saved.Models); diff != "" {
        t.Fatalf("saved models mismatch (-want +got):\n%s", diff);
    }
    }

    public static void TestShowOrPull_ModelExists(*testing.T t) {
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" {
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"model":"test-model"}`);
        return;
    }
        w.WriteHeader(http.StatusNotFound);
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "test-model", missingModelPromptPull, false);
        if err != null {
        t.Errorf("showOrPull should return null when model exists, got: %v", err);
    }
    }

    public static void TestShowOrPullWithPolicy_ModelExists(*testing.T t) {
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" {
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"model":"test-model"}`);
        return;
    }
        w.WriteHeader(http.StatusNotFound);
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "test-model", missingModelFail, false);
        if err != null {
        t.Errorf("showOrPullWithPolicy should return null when model exists, got: %v", err);
    }
    }

    public static void TestShowOrPullWithPolicy_ModelNotFound_FailDoesNotPromptOrPull(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatal("confirm prompt should not be called with fail policy");
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "missing-model", missingModelFail, false);
        if err == null {
        t.Fatal("expected fail policy to return an error for missing model");
    }
        if !strings.Contains(err.Error(), "ollama pull missing-model") {
        t.Fatalf("expected actionable pull guidance, got: %v", err);
    }
        if pullCalled {
        t.Fatal("expected pull not to be called with fail policy");
    }
    }

    public static void TestShowOrPullWithPolicy_ModelNotFound_PromptPolicyPulls(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if !strings.Contains(prompt, "missing-model") {
        t.Fatalf("expected prompt to mention missing model, got %q", prompt);
    }
        return true, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "missing-model", missingModelPromptPull, false);
        if err != null {
        t.Fatalf("expected prompt policy to pull and succeed, got %v", err);
    }
        if !pullCalled {
        t.Fatal("expected pull to be called with prompt policy");
    }
    }

    public static void TestShowOrPullWithPolicy_ModelNotFound_AutoPullPolicyPullsWithoutPrompt(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("confirm prompt should not be called with auto-pull policy: %q", prompt);
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "missing-model", missingModelAutoPull, false);
        if err != null {
        t.Fatalf("expected auto-pull policy to pull and succeed, got %v", err);
    }
        if !pullCalled {
        t.Fatal("expected pull to be called with auto-pull policy");
    }
    }

    public static void TestShowOrPullWithPolicy_CloudModelNotFound_FailsEarlyForAllPolicies(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatal("confirm prompt should not be called for explicit cloud models");
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var for _, policy = range []missingModelPolicy{missingModelPromptPull, missingModelAutoPull, missingModelFail} {
        t.Run(fmt.Sprintf("policy=%d", policy), func(t *testing.T) {
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "glm-5.1:cloud", policy, true);
        if err == null {
        t.Fatalf("expected cloud model not-found error for policy %d", policy);
    }
        if !strings.Contains(err.Error(), `model "glm-5.1:cloud" not found`) {
        t.Fatalf("expected not-found error for policy %d, got %v", policy, err);
    }
        if pullCalled {
        t.Fatalf("expected pull not to be called for cloud model with policy %d", policy);
    }
        });
    }
    }

    public static void TestShowOrPullWithPolicy_CloudModelDisabled_FailsWithCloudDisabledError(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatal("confirm prompt should not be called for explicit cloud models");
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var for _, policy = range []missingModelPolicy{missingModelPromptPull, missingModelAutoPull, missingModelFail} {
        t.Run(fmt.Sprintf("policy=%d", policy), func(t *testing.T) {
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/status":;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"cloud":{"disabled":true,"source":"config"}}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "glm-5.1:cloud", policy, true);
        if err == null {
        t.Fatalf("expected cloud disabled error for policy %d", policy);
    }
        if !strings.Contains(err.Error(), "remote inference is unavailable") {
        t.Fatalf("expected cloud disabled error for policy %d, got %v", policy, err);
    }
        if pullCalled {
        t.Fatalf("expected pull not to be called for cloud model with policy %d", policy);
    }
        });
    }
    }

    public static void TestShowOrPull_ModelNotFound_NoTerminal(*testing.T t) {
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "missing-model", missingModelPromptPull, false);
        if err == null {
        t.Error("showOrPull should return error when model not found and no terminal available");
    }
    }

    public static void TestShowOrPull_ShowCalledWithCorrectModel(*testing.T t) {
        var receivedModel String;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" {
        var req api.ShowRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err == null {
        receivedModel = req.Model;
    }
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"model":"%s"}`, receivedModel);
        return;
    }
        w.WriteHeader(http.StatusNotFound);
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        _ = showOrPullWithPolicy(context.Background(), client, "qwen3.5", missingModelPromptPull, false);
        if receivedModel != "qwen3.5" {
        t.Errorf("expected Show to be called with %q, got %q", "qwen3.5", receivedModel);
    }
    }

    public static void TestShowOrPull_ModelNotFound_ConfirmYes_Pulls(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if !strings.Contains(prompt, "missing-model") {
        t.Errorf("expected prompt to contain model name, got %q", prompt);
    }
        return true, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "missing-model", missingModelPromptPull, false);
        if err != null {
        t.Errorf("ShowOrPull should succeed after pull, got: %v", err);
    }
        if !pullCalled {
        t.Error("expected pull to be called when user confirms download");
    }
    }

    public static void TestShowOrPull_ModelNotFound_ConfirmNo_Cancelled(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return false, ErrCancelled;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        t.Error("pull should not be called when user declines");
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "missing-model", missingModelPromptPull, false);
        if err == null {
        t.Error("ShowOrPull should return error when user declines");
    }
    }

    public static void TestShowOrPull_CloudModel_NotFoundDoesNotPull(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Error("confirm prompt should not be called for cloud models");
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "glm-5.1:cloud", missingModelPromptPull, true);
        if err == null {
        t.Error("ShowOrPull should return not-found error for cloud model");
    }
        if !strings.Contains(err.Error(), `model "glm-5.1:cloud" not found`) {
        t.Errorf("expected cloud model not-found error, got: %v", err);
    }
        if pullCalled {
        t.Error("expected pull not to be called for cloud model");
    }
    }

    public static void TestShowOrPull_CloudLegacySuffix_NotFoundDoesNotPull(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Error("confirm prompt should not be called for cloud models");
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = showOrPullWithPolicy(context.Background(), client, "gpt-oss:20b-cloud", missingModelPromptPull, true);
        if err == null {
        t.Error("ShowOrPull should return not-found error for cloud model");
    }
        if !strings.Contains(err.Error(), `model "gpt-oss:20b-cloud" not found`) {
        t.Errorf("expected cloud model not-found error, got: %v", err);
    }
        if pullCalled {
        t.Error("expected pull not to be called for cloud model");
    }
    }

    public static void TestConfirmPrompt_DelegatesToHook(*testing.T t) {
        var oldHook = DefaultConfirmPrompt;
        var hookCalled boolean;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        hookCalled = true;
        if prompt != "test prompt?" {
        t.Errorf("expected prompt %q, got %q", "test prompt?", prompt);
    }
        return true, null;
    }
        defer func() { DefaultConfirmPrompt = oldHook }();
        var ok, err = ConfirmPrompt("test prompt?");
        if err != null {
        t.Errorf("unexpected error: %v", err);
    }
        if !ok {
        t.Error("expected true from hook");
    }
        if !hookCalled {
        t.Error("expected DefaultConfirmPrompt hook to be called");
    }
    }

    public static void TestEnsureAuth_NoCloudModels(*testing.T t) {
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        t.Error("no API calls expected when no cloud models selected");
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = ensureAuth(context.Background(), client, map[String]boolean{}, []String{"local-model"});
        if err != null {
        t.Errorf("ensureAuth should return null for non-cloud models, got: %v", err);
    }
    }

    public static void TestEnsureAuth_CloudModelFilteredCorrectly(*testing.T t) {
        var whoamiCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/me" {
        whoamiCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprintf(w, `{"name":"testuser"}`);
        return;
    }
        w.WriteHeader(http.StatusNotFound);
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var cloudModels = map[String]boolean{"cloud-model:cloud": true}
        var selected = []String{"cloud-model:cloud", "local-model"}
        var err = ensureAuth(context.Background(), client, cloudModels, selected);
        if err != null {
        t.Errorf("ensureAuth should succeed when user is authenticated, got: %v", err);
    }
        if !whoamiCalled {
        t.Error("expected whoami to be called for cloud model");
    }
    }

    public static void TestEnsureAuth_SkipsWhenNoCloudSelected(*testing.T t) {
        var whoamiCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/me" {
        whoamiCalled = true;
    }
        w.WriteHeader(http.StatusOK);
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var cloudModels = map[String]boolean{"cloud-model:cloud": true}
        var selected = []String{"local-model"}
        var err = ensureAuth(context.Background(), client, cloudModels, selected);
        if err != null {
        t.Errorf("expected null error, got: %v", err);
    }
        if whoamiCalled {
        t.Error("whoami should not be called when no cloud models are selected");
    }
    }

    public static void TestEnsureAuth_PreservesCancelledSignInHook(*testing.T t) {
        var oldSignIn = DefaultSignIn;
        DefaultSignIn = func(modelName, signInURL String) (String, error) {
        return "", ErrCancelled;
    }
        defer func() { DefaultSignIn = oldSignIn }();
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"not found"}`);
        case "/api/me":;
        w.WriteHeader(http.StatusUnauthorized);
        fmt.Fprintf(w, `{"error":"unauthorized","signin_url":"https://example.com/signin"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = ensureAuth(context.Background(), client, map[String]boolean{"cloud-model:cloud": true}, []String{"cloud-model:cloud"});
        if !errors.Is(err, ErrCancelled) {
        t.Fatalf("expected ErrCancelled, got %v", err);
    }
    }

    public static void TestEnsureAuth_DeclinedFallbackReturnsCancelled(*testing.T t) {
        var oldConfirm = DefaultConfirmPrompt;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return false, null;
    }
        defer func() { DefaultConfirmPrompt = oldConfirm }();
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprintf(w, `{"error":"not found"}`);
        case "/api/me":;
        w.WriteHeader(http.StatusUnauthorized);
        fmt.Fprintf(w, `{"error":"unauthorized","signin_url":"https://example.com/signin"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        var u, _ = url.Parse(srv.URL);
        var client = api.NewClient(u, srv.Client());
        var err = ensureAuth(context.Background(), client, map[String]boolean{"cloud-model:cloud": true}, []String{"cloud-model:cloud"});
        if !errors.Is(err, ErrCancelled) {
        t.Fatalf("expected ErrCancelled, got %v", err);
    }
    }

    public static void TestHyperlink(*testing.T t) {
        var tests = []struct {
        name     String;
        url      String;
        text     String;
        wantURL  String;
        wantText String;
        }{
        {
        name:     "basic link",;
        url:      "https://example.com",;
        text:     "click here",;
        wantURL:  "https://example.com",;
        wantText: "click here",;
        },;
        {
        name:     "url with path",;
        url:      "https://example.com/docs/install",;
        text:     "install docs",;
        wantURL:  "https://example.com/docs/install",;
        wantText: "install docs",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = hyperlink(tt.url, tt.text);
        if !strings.Contains(got, "\033]8;;") {
        t.Error("should contain OSC 8 open sequence");
    }
        if !strings.Contains(got, tt.wantURL) {
        t.Errorf("should contain URL %q", tt.wantURL);
    }
        if !strings.Contains(got, tt.wantText) {
        t.Errorf("should contain text %q", tt.wantText);
    }
        var wantSuffix = "\033]8;;\033\\";
        if !strings.HasSuffix(got, wantSuffix) {
        t.Error("should end with OSC 8 close sequence");
    }
        });
    }
    }

    public static void TestIntegration_InstallHint(*testing.T t) {
        var tests = []struct {
        name      String;
        input     String;
        wantEmpty boolean;
        wantURL   String;
        }{
        {
        name:    "claude has hint",;
        input:   "claude",;
        wantURL: "https://code.claude.com/docs/en/quickstart",;
        },;
        {
        name:    "codex has hint",;
        input:   "codex",;
        wantURL: "https://developers.openai.com/codex/cli/",;
        },;
        {
        name:    "openclaw has hint",;
        input:   "openclaw",;
        wantURL: "https://docs.openclaw.ai",;
        },;
        {
        name:      "unknown has no hint",;
        input:     "unknown",;
        wantEmpty: true,;
        },;
        {
        name:      "empty name has no hint",;
        input:     "",;
        wantEmpty: true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = "";
        var integration, err = integrationFor(tt.input);
        if err == null {
        got = integration.installHint;
    }
        if tt.wantEmpty {
        if got != "" {
        t.Errorf("expected empty hint, got %q", got);
    }
        return;
    }
        if !strings.Contains(got, "Install from") {
        t.Errorf("hint should start with 'Install from', got %q", got);
    }
        if !strings.Contains(got, tt.wantURL) {
        t.Errorf("hint should contain URL %q, got %q", tt.wantURL, got);
    }
        if !strings.Contains(got, "\033]8;;") {
        t.Error("hint URL should be wrapped in OSC 8 hyperlink");
    }
        });
    }
    }

    public static void TestListIntegrationInfos(*testing.T t) {
        var infos = ListIntegrationInfos();
        t.Run("excludes aliases", func(t *testing.T) {
        var for _, info = range infos {
        if integrationAliases[info.Name] {
        t.Errorf("alias %q should not appear in ListIntegrationInfos", info.Name);
    }
    }
        });
        t.Run("follows launcher order", func(t *testing.T) {
        var got = make([]String, 0, len(infos));
        var for _, info = range infos {
        got = append(got, info.Name);
    }
        var if diff = compareStrings(got, integrationOrder); diff != "" {
        t.Fatalf("launcher integration order mismatch: %s", diff);
    }
        });
        t.Run("all fields populated", func(t *testing.T) {
        var for _, info = range infos {
        if info.Name == "" {
        t.Error("Name should not be empty");
    }
        if info.DisplayName == "" {
        t.Errorf("DisplayName for %q should not be empty", info.Name);
    }
    }
        });
        t.Run("includes known integrations", func(t *testing.T) {
        var known = map[String]boolean{"claude": false, "codex": false, "opencode": false}
        var for _, info = range infos {
        var if _, ok = known[info.Name]; ok {
        known[info.Name] = true;
    }
    }
        var for name, found = range known {
        if !found {
        t.Errorf("expected %q in ListIntegrationInfos", name);
    }
    }
        });
        t.Run("includes hermes", func(t *testing.T) {
        var for _, info = range infos {
        if info.Name == "hermes" {
        return;
    }
    }
        t.Fatal("expected hermes to be included in ListIntegrationInfos");
        });
        t.Run("hermes still resolves explicitly", func(t *testing.T) {
        var name, runner, err = LookupIntegration("hermes");
        if err != null {
        t.Fatalf("expected explicit hermes integration lookup to work, got %v", err);
    }
        if name != "hermes" {
        t.Fatalf("expected canonical name hermes, got %q", name);
    }
        if runner.String() == "" {
        t.Fatal("expected hermes integration runner to be present");
    }
        });
    }

    public static void TestBuildModelList_Descriptions(*testing.T t) {
        t.Run("installed recommended has base description", func(t *testing.T) {
        var existing = []modelInfo{
        {Name: "qwen3.5", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var for _, item = range items {
        if item.Name == "qwen3.5" {
        if strings.HasSuffix(item.Description, "install?") {
        t.Errorf("installed model should not have 'install?' suffix, got %q", item.Description);
    }
        if item.Description == "" {
        t.Error("installed recommended model should have a description");
    }
        return;
    }
    }
        t.Error("qwen3.5 not found in items");
        });
        t.Run("not-installed local rec has VRAM in description", func(t *testing.T) {
        var items, _, _, _ = buildModelList(null, null, "");
        var for _, item = range items {
        if item.Name == "qwen3.5" {
        if !strings.Contains(item.Description, "~11GB") {
        t.Errorf("not-installed qwen3.5 should show VRAM hint, got %q", item.Description);
    }
        return;
    }
    }
        t.Error("qwen3.5 not found in items");
        });
        t.Run("installed local rec omits VRAM", func(t *testing.T) {
        var existing = []modelInfo{
        {Name: "qwen3.5", Remote: false},;
    }
        var items, _, _, _ = buildModelList(existing, null, "");
        var for _, item = range items {
        if item.Name == "qwen3.5" {
        if strings.Contains(item.Description, "~11GB") {
        t.Errorf("installed qwen3.5 should not show VRAM hint, got %q", item.Description);
    }
        return;
    }
    }
        t.Error("qwen3.5 not found in items");
        });
    }

    public static void TestIntegration_Editor(*testing.T t) {
        var tests = []struct {
        name String;
        want boolean;
        }{
        {"droid", true},;
        {"opencode", true},;
        {"openclaw", true},;
        {"claude", false},;
        {"codex", false},;
        {"nonexistent", false},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = false;
        var integration, err = integrationFor(tt.name);
        if err == null {
        got = integration.editor;
    }
        if got != tt.want {
        t.Errorf("integrationFor(%q).editor = %v, want %v", tt.name, got, tt.want);
    }
        });
    }
    }

    public static void TestIntegration_AutoInstallable(*testing.T t) {
        var tests = []struct {
        name String;
        want boolean;
        }{
        {"openclaw", true},;
        {"pi", true},;
        {"hermes", true},;
        {"claude", false},;
        {"codex", false},;
        {"opencode", false},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = false;
        var integration, err = integrationFor(tt.name);
        if err == null {
        got = integration.autoInstallable;
    }
        if got != tt.want {
        t.Errorf("integrationFor(%q).autoInstallable = %v, want %v", tt.name, got, tt.want);
    }
        });
    }
    }

    public static void TestIntegrationModels(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Run("returns null when not configured", func(t *testing.T) {
        var if got = IntegrationModels("droid"); got != null {
        t.Errorf("expected null, got %v", got);
    }
        });
        t.Run("returns all saved models", func(t *testing.T) {
        var if err = SaveIntegration("droid", []String{"llama3.2", "qwen3.5"}); err != null {
        t.Fatal(err);
    }
        var got = IntegrationModels("droid");
        var want = []String{"llama3.2", "qwen3.5"}
        var if diff = cmp.Diff(want, got); diff != "" {
        t.Errorf("IntegrationModels mismatch (-want +got):\n%s", diff);
    }
        });
    }
}
