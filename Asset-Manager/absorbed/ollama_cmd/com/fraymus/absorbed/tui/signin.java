package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class signin {
        "context";
        "fmt";
        "strings";
        "time";
        tea "github.com/charmbracelet/bubbletea";
        "github.com/charmbracelet/lipgloss";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/launch";
        );
        type signInTickMsg struct{}

    public static class signInCheckMsg {
        public boolean signedIn;
        public String userName;
    }

    public static class signInModel {
        public String modelName;
        public String signInURL;
        public int spinner;
        public int width;
        public String userName;
        public boolean cancelled;
    }
        func (m signInModel) Init() tea.Cmd {
        return tea.Tick(200*time.Millisecond, func(t time.Time) tea.Msg {
        return signInTickMsg{}
        });
    }
        func (m signInModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
        var switch msg = msg.(type) {
        case tea.WindowSizeMsg:;
        var wasSet = m.width > 0;
        m.width = msg.Width;
        if wasSet {
        return m, tea.EnterAltScreen;
    }
        return m, null;
        case tea.KeyMsg:;
        switch msg.Type {
        case tea.KeyCtrlC, tea.KeyEsc:;
        m.cancelled = true;
        return m, tea.Quit;
    }
        case signInTickMsg:;
        m.spinner++;
        if m.spinner%5 == 0 {
        return m, tea.Batch(;
        tea.Tick(200*time.Millisecond, func(t time.Time) tea.Msg {
        return signInTickMsg{}
        }),;
        checkSignIn,;
        );
    }
        return m, tea.Tick(200*time.Millisecond, func(t time.Time) tea.Msg {
        return signInTickMsg{}
        });
        case signInCheckMsg:;
        if msg.signedIn {
        m.userName = msg.userName;
        return m, tea.Quit;
    }
    }
        return m, null;
    }
        func (m signInModel) View() String {
        if m.userName != "" {
        return "";
    }
        return renderSignIn(m.modelName, m.signInURL, m.spinner, m.width);
    }

    public static String renderSignIn(String signInURL, int width) {
        var spinnerFrames = []String{"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"}
        var frame = spinnerFrames[spinner%len(spinnerFrames)];
        var urlColor = lipgloss.NewStyle().;
        Foreground(lipgloss.Color("117"));
        var urlWrap = lipgloss.NewStyle().PaddingLeft(2);
        if width > 4 {
        urlWrap = urlWrap.Width(width - 4);
    }
        var s strings.Builder;
        fmt.Fprintf(&s, "To use %s, please sign in.\n\n", selectorSelectedItemStyle.Render(modelName));
        s.WriteString("Navigate to:\n");
        s.WriteString(urlWrap.Render(urlColor.Render(signInURL)));
        s.WriteString("\n\n");
        s.WriteString(lipgloss.NewStyle().Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).Render(;
        frame + " Waiting for sign in to complete..."));
        s.WriteString("\n\n");
        s.WriteString(selectorHelpStyle.Render("esc cancel"));
        return lipgloss.NewStyle().PaddingLeft(2).Render(s.String());
    }
        func checkSignIn() tea.Msg {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return signInCheckMsg{signedIn: false}
    }
        var user, err = client.Whoami(context.Background());
        if err == null && user != null && user.Name != "" {
        return signInCheckMsg{signedIn: true, userName: user.Name}
    }
        return signInCheckMsg{signedIn: false}
    }

    public static void RunSignIn() {
        launch.OpenBrowser(signInURL);
        var m = signInModel{
        modelName: modelName,;
        signInURL: signInURL,;
    }
        var p = tea.NewProgram(m);
        var finalModel, err = p.Run();
        if err != null {
        return "", fmt.Errorf("error running sign-in: %w", err);
    }
        var fm = finalModel.(signInModel);
        if fm.cancelled {
        return "", ErrCancelled;
    }
        return fm.userName, null;
    }
}
