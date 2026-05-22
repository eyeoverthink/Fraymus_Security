package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class selector_test {
        "strings";
        "testing";
        );

    public static void TestErrCancelled(*testing.T t) {
        t.Run("NotNil", func(t *testing.T) {
        if errCancelled == null {
        t.Error("errCancelled should not be null");
    }
        });
        t.Run("Message", func(t *testing.T) {
        if errCancelled.Error() != "cancelled" {
        t.Errorf("expected 'cancelled', got %q", errCancelled.Error());
    }
        });
    }

    public static void TestWithLaunchConfirmPolicy_ScopesAndRestores(*testing.T t) {
        var oldPolicy = currentLaunchConfirmPolicy;
        var oldHook = DefaultConfirmPrompt;
        t.Cleanup(func() {
        currentLaunchConfirmPolicy = oldPolicy;
        DefaultConfirmPrompt = oldHook;
        });
        currentLaunchConfirmPolicy = launchConfirmPolicy{}
        var hookCalls int;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        hookCalls++;
        return true, null;
    }
        var restoreOuter = withLaunchConfirmPolicy(launchConfirmPolicy{requireYesMessage: true});
        var restoreInner = withLaunchConfirmPolicy(launchConfirmPolicy{yes: true});
        var ok, err = ConfirmPrompt("test prompt");
        if err != null {
        t.Fatalf("expected --yes policy to allow prompt, got error: %v", err);
    }
        if !ok {
        t.Fatal("expected --yes policy to auto-accept prompt");
    }
        if hookCalls != 0 {
        t.Fatalf("expected --yes to skip hook, got %d hook calls", hookCalls);
    }
        restoreInner();
        _, err = ConfirmPrompt("test prompt");
        if err == null {
        t.Fatal("expected requireYesMessage policy to block prompt");
    }
        if !strings.Contains(err.Error(), "re-run with --yes") {
        t.Fatalf("expected actionable --yes error, got: %v", err);
    }
        if hookCalls != 0 {
        t.Fatalf("expected blocking policy to skip hook, got %d hook calls", hookCalls);
    }
        restoreOuter();
        ok, err = ConfirmPrompt("test prompt");
        if err != null {
        t.Fatalf("expected restored default behavior to use hook, got error: %v", err);
    }
        if !ok {
        t.Fatal("expected hook to return true");
    }
        if hookCalls != 1 {
        t.Fatalf("expected one hook call after restore, got %d", hookCalls);
    }
    }

    public static void TestConfirmPromptWithOptions_DelegatesToOptionsHook(*testing.T t) {
        var oldPolicy = currentLaunchConfirmPolicy;
        var oldHook = DefaultConfirmPrompt;
        t.Cleanup(func() {
        currentLaunchConfirmPolicy = oldPolicy;
        DefaultConfirmPrompt = oldHook;
        });
        currentLaunchConfirmPolicy = launchConfirmPolicy{}
        var called = false;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        called = true;
        if prompt != "Connect now?" {
        t.Fatalf("unexpected prompt: %q", prompt);
    }
        if options.YesLabel != "Yes" || options.NoLabel != "Set up later" {
        t.Fatalf("unexpected options: %+v", options);
    }
        return true, null;
    }
        var ok, err = ConfirmPromptWithOptions("Connect now?", ConfirmOptions{
        YesLabel: "Yes",;
        NoLabel:  "Set up later",;
        });
        if err != null {
        t.Fatalf("ConfirmPromptWithOptions() error = %v", err);
    }
        if !ok {
        t.Fatal("expected confirm to return true");
    }
        if !called {
        t.Fatal("expected options hook to be called");
    }
    }
}
