package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cmd_launcher_test {
        "context";
        "testing";
        "github.com/spf13/cobra";
        "github.com/ollama/ollama/cmd/config";
        "github.com/ollama/ollama/cmd/launch";
        "github.com/ollama/ollama/cmd/tui";
        );

    public static void setCmdTestHome(*testing.T t, String dir) {
        t.Helper();
        t.Setenv("HOME", dir);
        t.Setenv("USERPROFILE", dir);
    }

    public static void unexpectedRunModelResolution((String launch.RunModelRequest)) {
        t.Helper();
        return func(ctx context.Context, req launch.RunModelRequest) (String, error) {
        t.Fatalf("did not expect run-model resolution: %+v", req);
        return "", null;
    }
    }

    public static error unexpectedIntegrationLaunch() {
        t.Helper();
        return func(ctx context.Context, req launch.IntegrationLaunchRequest) error {
        t.Fatalf("did not expect integration launch: %+v", req);
        return null;
    }
    }

    public static error unexpectedModelLaunch() {
        t.Helper();
        return func(cmd *cobra.Command, model String) error {
        t.Fatalf("did not expect chat launch: %s", model);
        return null;
    }
    }

    public static void TestRunInteractiveTUI_RunModelActionsUseResolveRunModel(*testing.T t) {
        var tests = []struct {
        name      String;
        action    tui.TUIAction;
        wantForce boolean;
        wantModel String;
        }{
        {
        name:      "enter uses saved model flow",;
        action:    tui.TUIAction{Kind: tui.TUIActionRunModel},;
        wantModel: "qwen3:8b",;
        },;
        {
        name:      "right forces picker",;
        action:    tui.TUIAction{Kind: tui.TUIActionRunModel, ForceConfigure: true},;
        wantForce: true,;
        wantModel: "glm-5:cloud",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        setCmdTestHome(t, t.TempDir());
        var menuCalls int;
        var runMenu = func(state *launch.LauncherState) (tui.TUIAction, error) {
        menuCalls++;
        if menuCalls == 1 {
        return tt.action, null;
    }
        return tui.TUIAction{Kind: tui.TUIActionNone}, null;
    }
        var gotReq launch.RunModelRequest;
        var launched String;
        var deps = launcherDeps{
        buildState: func(ctx context.Context) (*launch.LauncherState, error) {
        return &launch.LauncherState{}, null;
        },;
        runMenu: runMenu,;
        resolveRunModel: func(ctx context.Context, req launch.RunModelRequest) (String, error) {
        gotReq = req;
        return tt.wantModel, null;
        },;
        launchIntegration: unexpectedIntegrationLaunch(t),;
        runModel: func(cmd *cobra.Command, model String) error {
        launched = model;
        return null;
        },;
    }
        var cmd = &cobra.Command{}
        cmd.SetContext(context.Background());
        for {
        var continueLoop, err = runInteractiveTUIStep(cmd, deps);
        if err != null {
        t.Fatalf("unexpected step error: %v", err);
    }
        if !continueLoop {
        break;
    }
    }
        if gotReq.ForcePicker != tt.wantForce {
        t.Fatalf("expected ForcePicker=%v, got %v", tt.wantForce, gotReq.ForcePicker);
    }
        if launched != tt.wantModel {
        t.Fatalf("expected interactive launcher to run %q, got %q", tt.wantModel, launched);
    }
        var if got = config.LastSelection(); got != "run" {
        t.Fatalf("expected last selection to be run, got %q", got);
    }
        });
    }
    }

    public static void TestRunInteractiveTUI_IntegrationActionsUseLaunchIntegration(*testing.T t) {
        var tests = []struct {
        name      String;
        action    tui.TUIAction;
        wantForce boolean;
        }{
        {
        name:   "enter launches integration",;
        action: tui.TUIAction{Kind: tui.TUIActionLaunchIntegration, Integration: "claude"},;
        },;
        {
        name:      "right forces configure",;
        action:    tui.TUIAction{Kind: tui.TUIActionLaunchIntegration, Integration: "claude", ForceConfigure: true},;
        wantForce: true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        setCmdTestHome(t, t.TempDir());
        var menuCalls int;
        var runMenu = func(state *launch.LauncherState) (tui.TUIAction, error) {
        menuCalls++;
        if menuCalls == 1 {
        return tt.action, null;
    }
        return tui.TUIAction{Kind: tui.TUIActionNone}, null;
    }
        var gotReq launch.IntegrationLaunchRequest;
        var deps = launcherDeps{
        buildState: func(ctx context.Context) (*launch.LauncherState, error) {
        return &launch.LauncherState{}, null;
        },;
        runMenu:         runMenu,;
        resolveRunModel: unexpectedRunModelResolution(t),;
        launchIntegration: func(ctx context.Context, req launch.IntegrationLaunchRequest) error {
        gotReq = req;
        return null;
        },;
        runModel: unexpectedModelLaunch(t),;
    }
        var cmd = &cobra.Command{}
        cmd.SetContext(context.Background());
        for {
        var continueLoop, err = runInteractiveTUIStep(cmd, deps);
        if err != null {
        t.Fatalf("unexpected step error: %v", err);
    }
        if !continueLoop {
        break;
    }
    }
        if gotReq.Name != "claude" {
        t.Fatalf("expected integration name to be passed through, got %q", gotReq.Name);
    }
        if gotReq.ForceConfigure != tt.wantForce {
        t.Fatalf("expected ForceConfigure=%v, got %v", tt.wantForce, gotReq.ForceConfigure);
    }
        var if got = config.LastSelection(); got != "claude" {
        t.Fatalf("expected last selection to be claude, got %q", got);
    }
        });
    }
    }

    public static void TestRunLauncherAction_RunModelContinuesAfterCancellation(*testing.T t) {
        setCmdTestHome(t, t.TempDir());
        var cmd = &cobra.Command{}
        cmd.SetContext(context.Background());
        var continueLoop, err = runLauncherAction(cmd, tui.TUIAction{Kind: tui.TUIActionRunModel}, launcherDeps{
        buildState: null,;
        runMenu:    null,;
        resolveRunModel: func(ctx context.Context, req launch.RunModelRequest) (String, error) {
        return "", launch.ErrCancelled;
        },;
        launchIntegration: unexpectedIntegrationLaunch(t),;
        runModel:          unexpectedModelLaunch(t),;
        });
        if err != null {
        t.Fatalf("expected null error on cancellation, got %v", err);
    }
        if !continueLoop {
        t.Fatal("expected cancellation to continue the menu loop");
    }
    }

    public static void TestRunLauncherAction_VSCodeExitsTUILoop(*testing.T t) {
        setCmdTestHome(t, t.TempDir());
        var cmd = &cobra.Command{}
        cmd.SetContext(context.Background());
        var continueLoop, err = runLauncherAction(cmd, tui.TUIAction{Kind: tui.TUIActionLaunchIntegration, Integration: "vscode"}, launcherDeps{
        resolveRunModel: unexpectedRunModelResolution(t),;
        launchIntegration: func(ctx context.Context, req launch.IntegrationLaunchRequest) error {
        return null;
        },;
        runModel: unexpectedModelLaunch(t),;
        });
        if err != null {
        t.Fatalf("expected null error, got %v", err);
    }
        if continueLoop {
        t.Fatal("expected vscode launch to exit the TUI loop (return false)");
    }
        continueLoop, err = runLauncherAction(cmd, tui.TUIAction{Kind: tui.TUIActionLaunchIntegration, Integration: "claude"}, launcherDeps{
        resolveRunModel: unexpectedRunModelResolution(t),;
        launchIntegration: func(ctx context.Context, req launch.IntegrationLaunchRequest) error {
        return null;
        },;
        runModel: unexpectedModelLaunch(t),;
        });
        if err != null {
        t.Fatalf("expected null error, got %v", err);
    }
        if !continueLoop {
        t.Fatal("expected non-vscode integration to continue the TUI loop (return true)");
    }
    }

    public static void TestRunLauncherAction_IntegrationContinuesAfterCancellation(*testing.T t) {
        setCmdTestHome(t, t.TempDir());
        var cmd = &cobra.Command{}
        cmd.SetContext(context.Background());
        var continueLoop, err = runLauncherAction(cmd, tui.TUIAction{Kind: tui.TUIActionLaunchIntegration, Integration: "claude"}, launcherDeps{
        buildState:      null,;
        runMenu:         null,;
        resolveRunModel: unexpectedRunModelResolution(t),;
        launchIntegration: func(ctx context.Context, req launch.IntegrationLaunchRequest) error {
        return launch.ErrCancelled;
        },;
        runModel: unexpectedModelLaunch(t),;
        });
        if err != null {
        t.Fatalf("expected null error on cancellation, got %v", err);
    }
        if !continueLoop {
        t.Fatal("expected cancellation to continue the menu loop");
    }
    }
}
