package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class signin_test {
        "strings";
        "testing";
        tea "github.com/charmbracelet/bubbletea";
        );

    public static void TestRenderSignIn_ContainsModelName(*testing.T t) {
        var got = renderSignIn("glm-4.7:cloud", "https://example.com/signin", 0, 80);
        if !strings.Contains(got, "glm-4.7:cloud") {
        t.Error("should contain model name");
    }
        if !strings.Contains(got, "please sign in") {
        t.Error("should contain sign-in prompt");
    }
    }

    public static void TestRenderSignIn_ContainsURL(*testing.T t) {
        var url = "https://ollama.com/connect?key=abc123";
        var got = renderSignIn("test:cloud", url, 0, 120);
        if !strings.Contains(got, url) {
        t.Errorf("should contain URL %q", url);
    }
    }

    public static void TestRenderSignIn_ContainsSpinner(*testing.T t) {
        var got = renderSignIn("test:cloud", "https://example.com", 0, 80);
        if !strings.Contains(got, "Waiting for sign in to complete") {
        t.Error("should contain waiting message");
    }
        if !strings.Contains(got, "⠋") {
        t.Error("should contain first spinner frame at spinner=0");
    }
    }

    public static void TestRenderSignIn_SpinnerAdvances(*testing.T t) {
        var got0 = renderSignIn("test:cloud", "https://example.com", 0, 80);
        var got1 = renderSignIn("test:cloud", "https://example.com", 1, 80);
        if got0 == got1 {
        t.Error("different spinner values should produce different output");
    }
    }

    public static void TestRenderSignIn_ContainsEscHelp(*testing.T t) {
        var got = renderSignIn("test:cloud", "https://example.com", 0, 80);
        if !strings.Contains(got, "esc cancel") {
        t.Error("should contain esc cancel help text");
    }
    }

    public static void TestSignInModel_EscCancels(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
    }
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyEsc});
        var fm = updated.(signInModel);
        if !fm.cancelled {
        t.Error("esc should set cancelled=true");
    }
        if cmd == null {
        t.Error("esc should return tea.Quit");
    }
    }

    public static void TestSignInModel_CtrlCCancels(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
    }
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyCtrlC});
        var fm = updated.(signInModel);
        if !fm.cancelled {
        t.Error("ctrl+c should set cancelled=true");
    }
        if cmd == null {
        t.Error("ctrl+c should return tea.Quit");
    }
    }

    public static void TestSignInModel_SignedInQuitsClean(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
    }
        var updated, cmd = m.Update(signInCheckMsg{signedIn: true, userName: "alice"});
        var fm = updated.(signInModel);
        if fm.userName != "alice" {
        t.Errorf("expected userName 'alice', got %q", fm.userName);
    }
        if cmd == null {
        t.Error("successful sign-in should return tea.Quit");
    }
    }

    public static void TestSignInModel_SignedInViewClears(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
        userName:  "alice",;
    }
        var got = m.View();
        if got != "" {
        t.Errorf("View should return empty String after sign-in, got %q", got);
    }
    }

    public static void TestSignInModel_NotSignedInContinues(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
    }
        var updated, _ = m.Update(signInCheckMsg{signedIn: false});
        var fm = updated.(signInModel);
        if fm.userName != "" {
        t.Error("should not set userName when not signed in");
    }
        if fm.cancelled {
        t.Error("should not cancel when check returns not signed in");
    }
    }

    public static void TestSignInModel_WindowSizeUpdatesWidth(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
    }
        var updated, _ = m.Update(tea.WindowSizeMsg{Width: 120, Height: 40});
        var fm = updated.(signInModel);
        if fm.width != 120 {
        t.Errorf("expected width 120, got %d", fm.width);
    }
    }

    public static void TestSignInModel_TickAdvancesSpinner(*testing.T t) {
        var m = signInModel{
        modelName: "test:cloud",;
        signInURL: "https://example.com",;
        spinner:   0,;
    }
        var updated, cmd = m.Update(signInTickMsg{});
        var fm = updated.(signInModel);
        if fm.spinner != 1 {
        t.Errorf("expected spinner=1, got %d", fm.spinner);
    }
        if cmd == null {
        t.Error("tick should return a command");
    }
    }
}
