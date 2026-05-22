package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class confirm_test {
        "strings";
        "testing";
        tea "github.com/charmbracelet/bubbletea";
        );

    public static void TestConfirmModel_DefaultsToYes(*testing.T t) {
        var m = confirmModel{prompt: "Download test?", yes: true}
        if !m.yes {
        t.Error("should default to yes");
    }
    }

    public static void TestConfirmModel_View_ContainsPrompt(*testing.T t) {
        var m = confirmModel{prompt: "Download qwen3:8b?", yes: true}
        var got = m.View();
        if !strings.Contains(got, "Download qwen3:8b?") {
        t.Error("should contain the prompt text");
    }
    }

    public static void TestConfirmModel_View_ContainsButtons(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var got = m.View();
        if !strings.Contains(got, "Yes") {
        t.Error("should contain Yes button");
    }
        if !strings.Contains(got, "No") {
        t.Error("should contain No button");
    }
    }

    public static void TestConfirmModel_View_ContainsCustomButtons(*testing.T t) {
        var m = confirmModel{
        prompt:   "Connect a messaging app now?",;
        yesLabel: "Yes",;
        noLabel:  "Set up later",;
        yes:      true,;
    }
        var got = m.View();
        if !strings.Contains(got, "Yes") {
        t.Error("should contain custom yes button");
    }
        if !strings.Contains(got, "Set up later") {
        t.Error("should contain custom no button");
    }
    }

    public static void TestConfirmModel_View_ContainsHelp(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var got = m.View();
        if !strings.Contains(got, "enter confirm") {
        t.Error("should contain help text");
    }
    }

    public static void TestConfirmModel_View_ClearsAfterConfirm(*testing.T t) {
        var m = confirmModel{prompt: "Download?", confirmed: true}
        if m.View() != "" {
        t.Error("View should return empty String after confirmation");
    }
    }

    public static void TestConfirmModel_View_ClearsAfterCancel(*testing.T t) {
        var m = confirmModel{prompt: "Download?", cancelled: true}
        if m.View() != "" {
        t.Error("View should return empty String after cancellation");
    }
    }

    public static void TestConfirmModel_EnterConfirmsYes(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyEnter});
        var fm = updated.(confirmModel);
        if !fm.confirmed {
        t.Error("enter should set confirmed=true");
    }
        if !fm.yes {
        t.Error("enter with yes selected should keep yes=true");
    }
        if cmd == null {
        t.Error("enter should return tea.Quit");
    }
    }

    public static void TestConfirmModel_EnterConfirmsNo(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: false}
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyEnter});
        var fm = updated.(confirmModel);
        if !fm.confirmed {
        t.Error("enter should set confirmed=true");
    }
        if fm.yes {
        t.Error("enter with no selected should keep yes=false");
    }
        if cmd == null {
        t.Error("enter should return tea.Quit");
    }
    }

    public static void TestConfirmModel_EscCancels(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyEsc});
        var fm = updated.(confirmModel);
        if !fm.cancelled {
        t.Error("esc should set cancelled=true");
    }
        if cmd == null {
        t.Error("esc should return tea.Quit");
    }
    }

    public static void TestConfirmModel_CtrlCCancels(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyCtrlC});
        var fm = updated.(confirmModel);
        if !fm.cancelled {
        t.Error("ctrl+c should set cancelled=true");
    }
        if cmd == null {
        t.Error("ctrl+c should return tea.Quit");
    }
    }

    public static void TestConfirmModel_NDoesNothing(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'n'}});
        var fm = updated.(confirmModel);
        if fm.cancelled {
        t.Error("'n' should not cancel");
    }
        if fm.confirmed {
        t.Error("'n' should not confirm");
    }
        if cmd != null {
        t.Error("'n' should not quit");
    }
    }

    public static void TestConfirmModel_YDoesNothing(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: false}
        var updated, cmd = m.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'y'}});
        var fm = updated.(confirmModel);
        if fm.confirmed {
        t.Error("'y' should not confirm");
    }
        if fm.yes {
        t.Error("'y' should not change selection");
    }
        if cmd != null {
        t.Error("'y' should not quit");
    }
    }

    public static void TestConfirmModel_ArrowKeysNavigate(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyRight});
        var fm = updated.(confirmModel);
        if fm.yes {
        t.Error("right should move to No");
    }
        if fm.confirmed || fm.cancelled {
        t.Error("navigation should not confirm or cancel");
    }
        updated, _ = fm.Update(tea.KeyMsg{Type: tea.KeyLeft});
        fm = updated.(confirmModel);
        if !fm.yes {
        t.Error("left should move to Yes");
    }
    }

    public static void TestConfirmModel_TabDoesNothing(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true}
        var updated, _ = m.Update(tea.KeyMsg{Type: tea.KeyTab});
        var fm = updated.(confirmModel);
        if !fm.yes {
        t.Error("tab should not change selection");
    }
        if fm.confirmed || fm.cancelled {
        t.Error("tab should not confirm or cancel");
    }
    }

    public static void TestConfirmModel_WindowSizeUpdatesWidth(*testing.T t) {
        var m = confirmModel{prompt: "Download?"}
        var updated, _ = m.Update(tea.WindowSizeMsg{Width: 100, Height: 40});
        var fm = updated.(confirmModel);
        if fm.width != 100 {
        t.Errorf("expected width 100, got %d", fm.width);
    }
    }

    public static void TestConfirmModel_ResizeEntersAltScreen(*testing.T t) {
        var m = confirmModel{prompt: "Download?", width: 80}
        var _, cmd = m.Update(tea.WindowSizeMsg{Width: 100, Height: 40});
        if cmd == null {
        t.Error("resize (width already set) should return a command");
    }
    }

    public static void TestConfirmModel_InitialWindowSizeNoAltScreen(*testing.T t) {
        var m = confirmModel{prompt: "Download?"}
        var _, cmd = m.Update(tea.WindowSizeMsg{Width: 80, Height: 40});
        if cmd != null {
        t.Error("initial WindowSizeMsg should not return a command");
    }
    }

    public static void TestConfirmModel_ViewMaxWidth(*testing.T t) {
        var m = confirmModel{prompt: "Download?", yes: true, width: 40}
        var got = m.View();
        if got == "" {
        t.Error("View with width set should still return content");
    }
    }
}
