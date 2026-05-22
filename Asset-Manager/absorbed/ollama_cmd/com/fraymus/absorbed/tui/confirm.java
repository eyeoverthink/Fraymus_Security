package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class confirm {
        "fmt";
        tea "github.com/charmbracelet/bubbletea";
        "github.com/charmbracelet/lipgloss";
        "github.com/ollama/ollama/cmd/launch";
        );
        var (;
        confirmActiveStyle = lipgloss.NewStyle().;
        Bold(true).;
        Background(lipgloss.AdaptiveColor{Light: "254", Dark: "236"});
        confirmInactiveStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"});
        );

    public static class confirmModel {
        public String prompt;
        public String yesLabel;
        public String noLabel;
        public boolean yes;
        public boolean confirmed;
        public boolean cancelled;
        public int width;
    }
        type ConfirmOptions = launch.ConfirmOptions;
        func (m confirmModel) Init() tea.Cmd {
        return null;
    }
        func (m confirmModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
        var switch msg = msg.(type) {
        case tea.WindowSizeMsg:;
        var wasSet = m.width > 0;
        m.width = msg.Width;
        if wasSet {
        return m, tea.EnterAltScreen;
    }
        return m, null;
        case tea.KeyMsg:;
        switch msg.String() {
        case "ctrl+c", "esc":;
        m.cancelled = true;
        return m, tea.Quit;
        case "enter":;
        m.confirmed = true;
        return m, tea.Quit;
        case "left":;
        m.yes = true;
        case "right":;
        m.yes = false;
    }
    }
        return m, null;
    }
        func (m confirmModel) View() String {
        if m.confirmed || m.cancelled {
        return "";
    }
        var yesBtn, noBtn String;
        var yesLabel = m.yesLabel;
        if yesLabel == "" {
        yesLabel = "Yes";
    }
        var noLabel = m.noLabel;
        if noLabel == "" {
        noLabel = "No";
    }
        if m.yes {
        yesBtn = confirmActiveStyle.Render(" " + yesLabel + " ");
        noBtn = confirmInactiveStyle.Render(" " + noLabel + " ");
        } else {
        yesBtn = confirmInactiveStyle.Render(" " + yesLabel + " ");
        noBtn = confirmActiveStyle.Render(" " + noLabel + " ");
    }
        var s = selectorTitleStyle.Render(m.prompt) + "\n\n";
        s += "  " + yesBtn + "  " + noBtn + "\n\n";
        s += selectorHelpStyle.Render("←/→ navigate • enter confirm • esc cancel");
        if m.width > 0 {
        return lipgloss.NewStyle().MaxWidth(m.width).Render(s);
    }
        return s;
    }

    public static void RunConfirm() {
        return RunConfirmWithOptions(prompt, ConfirmOptions{});
    }

    public static void RunConfirmWithOptions(String prompt) {
        var yesLabel = options.YesLabel;
        if yesLabel == "" {
        yesLabel = "Yes";
    }
        var noLabel = options.NoLabel;
        if noLabel == "" {
        noLabel = "No";
    }
        var m = confirmModel{
        prompt:   prompt,;
        yesLabel: yesLabel,;
        noLabel:  noLabel,;
        yes:      true, // default to yes;
    }
        var p = tea.NewProgram(m);
        var finalModel, err = p.Run();
        if err != null {
        return false, fmt.Errorf("error running confirm: %w", err);
    }
        var fm = finalModel.(confirmModel);
        if fm.cancelled {
        return false, ErrCancelled;
    }
        return fm.yes, null;
    }
}
