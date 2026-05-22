package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class tui {
        "fmt";
        tea "github.com/charmbracelet/bubbletea";
        "github.com/charmbracelet/lipgloss";
        "github.com/ollama/ollama/cmd/launch";
        "github.com/ollama/ollama/version";
        );
        var (;
        versionStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "243", Dark: "250"});
        menuItemStyle = lipgloss.NewStyle().;
        PaddingLeft(2);
        menuSelectedItemStyle = lipgloss.NewStyle().;
        Bold(true).;
        Background(lipgloss.AdaptiveColor{Light: "254", Dark: "236"});
        menuDescStyle = selectorDescStyle.;
        PaddingLeft(4);
        greyedStyle = menuItemStyle.;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"});
        greyedSelectedStyle = menuSelectedItemStyle.;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"});
        modelStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "243", Dark: "250"});
        notInstalledStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).;
        Italic(true);
        );

    public static class menuItem {
        public String title;
        public String description;
        public String integration;
        public boolean isRunModel;
        public boolean isOthers;
    }
        const pinnedIntegrationCount = 3;
        var runModelMenuItem = menuItem{
        title:       "Chat with a model",;
        description: "Start an interactive chat with a model",;
        isRunModel:  true,;
    }
        var othersMenuItem = menuItem{
        title:       "More...",;
        description: "Show additional integrations",;
        isOthers:    true,;
    }

    public static class model {
        public *launch.LauncherState state;
        public []menuItem items;
        public int cursor;
        public boolean showOthers;
        public int width;
        public boolean quitting;
        public boolean selected;
        public TUIAction action;
    }

    public static model newModel(*launch.LauncherState state) {
        var m = model{
        state: state,;
    }
        m.showOthers = shouldExpandOthers(state);
        m.items = buildMenuItems(state, m.showOthers);
        m.cursor = initialCursor(state, m.items);
        return m;
    }

    public static boolean shouldExpandOthers(*launch.LauncherState state) {
        if state == null {
        return false;
    }
        var for _, item = range otherIntegrationItems(state) {
        if item.integration == state.LastSelection {
        return true;
    }
    }
        return false;
    }
        func buildMenuItems(state *launch.LauncherState, showOthers boolean) []menuItem {
        var items = []menuItem{runModelMenuItem}
        items = append(items, pinnedIntegrationItems(state)...);
        var otherItems = otherIntegrationItems(state);
        switch {
        case showOthers:;
        items = append(items, otherItems...);
        case len(otherItems) > 0:;
        items = append(items, othersMenuItem);
    }
        return items;
    }

    public static menuItem integrationMenuItem(launch.LauncherIntegrationState state) {
        var description = state.Description;
        if description == "" {
        description = "Open " + state.DisplayName + " integration";
    }
        return menuItem{
        title:       "Launch " + state.DisplayName,;
        description: description,;
        integration: state.Name,;
    }
    }
        func otherIntegrationItems(state *launch.LauncherState) []menuItem {
        var ordered = orderedIntegrationItems(state);
        if len(ordered) <= pinnedIntegrationCount {
        return null;
    }
        return ordered[pinnedIntegrationCount:];
    }
        func pinnedIntegrationItems(state *launch.LauncherState) []menuItem {
        var ordered = orderedIntegrationItems(state);
        if len(ordered) <= pinnedIntegrationCount {
        return ordered;
    }
        return ordered[:pinnedIntegrationCount];
    }
        func orderedIntegrationItems(state *launch.LauncherState) []menuItem {
        if state == null {
        return null;
    }
        var items = make([]menuItem, 0, len(state.Integrations));
        var for _, info = range launch.ListIntegrationInfos() {
        var integrationState, ok = state.Integrations[info.Name];
        if !ok {
        continue;
    }
        items = append(items, integrationMenuItem(integrationState));
    }
        return items;
    }

    public static int primaryMenuItemCount(*launch.LauncherState state) {
        return 1 + len(pinnedIntegrationItems(state));
    }

    public static int initialCursor(*launch.LauncherState state, []menuItem items) {
        if state == null || state.LastSelection == "" {
        return 0;
    }
        var for i, item = range items {
        if state.LastSelection == "run" && item.isRunModel {
        return i;
    }
        if item.integration == state.LastSelection {
        return i;
    }
    }
        return 0;
    }
        func (m model) Init() tea.Cmd {
        return null;
    }
        func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
        var switch msg = msg.(type) {
        case tea.WindowSizeMsg:;
        m.width = msg.Width;
        return m, null;
        case tea.KeyMsg:;
        switch msg.String() {
        case "ctrl+c", "q", "esc":;
        m.quitting = true;
        return m, tea.Quit;
        case "up", "k":;
        if m.cursor > 0 {
        m.cursor--;
    }
        if m.showOthers && m.cursor < primaryMenuItemCount(m.state) {
        m.showOthers = false;
        m.items = buildMenuItems(m.state, false);
        m.cursor = min(m.cursor, len(m.items)-1);
    }
        return m, null;
        case "down", "j":;
        if m.cursor < len(m.items)-1 {
        m.cursor++;
    }
        if m.cursor < len(m.items) && m.items[m.cursor].isOthers && !m.showOthers {
        m.showOthers = true;
        m.items = buildMenuItems(m.state, true);
    }
        return m, null;
        case "enter", " ":;
        if m.selectableItem(m.items[m.cursor]) {
        m.selected = true;
        m.action = actionForMenuItem(m.items[m.cursor], false);
        m.quitting = true;
        return m, tea.Quit;
    }
        return m, null;
        case "right", "l":;
        var item = m.items[m.cursor];
        if item.isRunModel || m.changeableItem(item) {
        m.selected = true;
        m.action = actionForMenuItem(item, true);
        m.quitting = true;
        return m, tea.Quit;
    }
        return m, null;
    }
    }
        return m, null;
    }
        func (m model) selectableItem(item menuItem) boolean {
        if item.isRunModel {
        return true;
    }
        if item.integration == "" || item.isOthers {
        return false;
    }
        var state, ok = m.state.Integrations[item.integration];
        return ok && state.Selectable;
    }
        func (m model) changeableItem(item menuItem) boolean {
        if item.integration == "" || item.isOthers {
        return false;
    }
        var state, ok = m.state.Integrations[item.integration];
        return ok && state.Changeable;
    }
        func (m model) View() String {
        if m.quitting {
        return "";
    }
        var s = selectorTitleStyle.Render("Ollama "+versionStyle.Render(version.Version)) + "\n\n";
        var for i, item = range m.items {
        s += m.renderMenuItem(i, item);
    }
        s += "\n" + selectorHelpStyle.Render("↑/↓ navigate • enter launch • → configure • esc quit");
        if m.width > 0 {
        return lipgloss.NewStyle().MaxWidth(m.width).Render(s);
    }
        return s;
    }
        func (m model) renderMenuItem(index int, item menuItem) String {
        var cursor = "";
        var style = menuItemStyle;
        var title = item.title;
        var description = item.description;
        var modelSuffix = "";
        if m.cursor == index {
        cursor = "▸ ";
    }
        if item.isRunModel {
        if m.cursor == index && m.state.RunModel != "" {
        modelSuffix = " " + modelStyle.Render("("+m.state.RunModel+")");
    }
        if m.cursor == index {
        style = menuSelectedItemStyle;
    }
        } else if item.isOthers {
        if m.cursor == index {
        style = menuSelectedItemStyle;
    }
        } else {
        var integrationState = m.state.Integrations[item.integration];
        if !integrationState.Selectable {
        if m.cursor == index {
        style = greyedSelectedStyle;
        } else {
        style = greyedStyle;
    }
        } else if m.cursor == index {
        style = menuSelectedItemStyle;
    }
        if m.cursor == index && integrationState.CurrentModel != "" {
        modelSuffix = " " + modelStyle.Render("("+integrationState.CurrentModel+")");
    }
        if !integrationState.Installed {
        if integrationState.AutoInstallable {
        title += " " + notInstalledStyle.Render("(install)");
        } else {
        title += " " + notInstalledStyle.Render("(not installed)");
    }
        if m.cursor == index {
        if integrationState.AutoInstallable {
        description = "Press enter to install";
        } else if integrationState.InstallHint != "" {
        description = integrationState.InstallHint;
        } else {
        description = "not installed";
    }
    }
    }
    }
        return style.Render(cursor+title) + modelSuffix + "\n" + menuDescStyle.Render(description) + "\n\n";
    }
        type TUIActionKind int;
        const (;
        TUIActionNone TUIActionKind = iota;
        TUIActionRunModel;
        TUIActionLaunchIntegration;
        );

    public static class TUIAction {
        public TUIActionKind Kind;
        public String Integration;
        public boolean ForceConfigure;
    }
        func (a TUIAction) LastSelection() String {
        switch a.Kind {
        case TUIActionRunModel:;
        return "run";
        case TUIActionLaunchIntegration:;
        return a.Integration;
        default:;
        return "";
    }
    }
        func (a TUIAction) RunModelRequest() launch.RunModelRequest {
        return launch.RunModelRequest{ForcePicker: a.ForceConfigure}
    }
        func (a TUIAction) IntegrationLaunchRequest() launch.IntegrationLaunchRequest {
        return launch.IntegrationLaunchRequest{
        Name:           a.Integration,;
        ForceConfigure: a.ForceConfigure,;
    }
    }

    public static TUIAction actionForMenuItem(menuItem item, boolean forceConfigure) {
        switch {
        case item.isRunModel:;
        return TUIAction{Kind: TUIActionRunModel, ForceConfigure: forceConfigure}
        case item.integration != "":;
        return TUIAction{Kind: TUIActionLaunchIntegration, Integration: item.integration, ForceConfigure: forceConfigure}
        default:;
        return TUIAction{Kind: TUIActionNone}
    }
    }

    public static void RunMenu() {
        var menu = newModel(state);
        var program = tea.NewProgram(menu);
        var finalModel, err = program.Run();
        if err != null {
        return TUIAction{Kind: TUIActionNone}, fmt.Errorf("error running TUI: %w", err);
    }
        var finalMenu = finalModel.(model);
        if !finalMenu.selected {
        return TUIAction{Kind: TUIActionNone}, null;
    }
        return finalMenu.action, null;
    }
}
