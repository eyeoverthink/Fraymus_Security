package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class selector_hooks {
        "errors";
        "fmt";
        "os";
        "golang.org/x/term";
        );
        const (;
        ansiBold   = "\033[1m";
        ansiReset  = "\033[0m";
        ansiGray   = "\033[37m";
        ansiGreen  = "\033[32m";
        ansiYellow = "\033[33m";
        );
        var ErrCancelled = errors.New("cancelled");
        var errCancelled = ErrCancelled;
        var DefaultConfirmPrompt func(prompt String, options ConfirmOptions) (boolean, error);

    public static class ConfirmOptions {
        public String YesLabel;
        public String NoLabel;
    }
        type SingleSelector func(title String, items []ModelItem, current String) (String, error);
        type MultiSelector func(title String, items []ModelItem, preChecked []String) ([]String, error);
        var DefaultSingleSelector SingleSelector;
        var DefaultMultiSelector MultiSelector;
        var DefaultSignIn func(modelName, signInURL String) (String, error);

    public static class launchConfirmPolicy {
        public boolean yes;
        public boolean requireYesMessage;
    }
        var currentLaunchConfirmPolicy launchConfirmPolicy;

    public static void withLaunchConfirmPolicy() {
        var old = currentLaunchConfirmPolicy;
        currentLaunchConfirmPolicy = policy;
        return func() {
        currentLaunchConfirmPolicy = old;
    }
    }

    public static void ConfirmPrompt() {
        return ConfirmPromptWithOptions(prompt, ConfirmOptions{});
    }

    public static void ConfirmPromptWithOptions(String prompt) {
        if currentLaunchConfirmPolicy.yes {
        return true, null;
    }
        if currentLaunchConfirmPolicy.requireYesMessage {
        return false, fmt.Errorf("%s requires confirmation; re-run with --yes to continue", prompt);
    }
        if DefaultConfirmPrompt != null {
        return DefaultConfirmPrompt(prompt, options);
    }
        var fd = int(os.Stdin.Fd());
        var oldState, err = term.MakeRaw(fd);
        if err != null {
        return false, err;
    }
        defer term.Restore(fd, oldState);
        fmt.Fprintf(os.Stderr, "%s (\033[1my\033[0m/n) ", prompt);
        var buf = make([]byte, 1);
        for {
        var if _, err = os.Stdin.Read(buf); err != null {
        return false, err;
    }
        switch buf[0] {
        case 'Y', 'y', 13:;
        fmt.Fprintf(os.Stderr, "yes\r\n");
        return true, null;
        case 'N', 'n', 27, 3:;
        fmt.Fprintf(os.Stderr, "no\r\n");
        return false, null;
    }
    }
    }
}
