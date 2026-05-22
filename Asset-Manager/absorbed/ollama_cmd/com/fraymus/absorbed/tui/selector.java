package com.fraymus.absorbed.tui;

import java.util.*;
import java.io.*;

public class selector {
        "fmt";
        "strings";
        tea "github.com/charmbracelet/bubbletea";
        "github.com/charmbracelet/lipgloss";
        "github.com/ollama/ollama/cmd/launch";
        );
        var (;
        selectorTitleStyle = lipgloss.NewStyle().;
        Bold(true);
        selectorItemStyle = lipgloss.NewStyle().;
        PaddingLeft(4);
        selectorSelectedItemStyle = lipgloss.NewStyle().;
        PaddingLeft(2).;
        Bold(true).;
        Background(lipgloss.AdaptiveColor{Light: "254", Dark: "236"});
        selectorDescStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"});
        selectorDescLineStyle = selectorDescStyle.;
        PaddingLeft(6);
        selectorFilterStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).;
        Italic(true);
        selectorInputStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "235", Dark: "252"});
        selectorDefaultTagStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).;
        Italic(true);
        selectorHelpStyle = lipgloss.NewStyle().;
        Foreground(lipgloss.AdaptiveColor{Light: "244", Dark: "244"});
        selectorMoreStyle = lipgloss.NewStyle().;
        PaddingLeft(6).;
        Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).;
        Italic(true);
        sectionHeaderStyle = lipgloss.NewStyle().;
        PaddingLeft(2).;
        Bold(true).;
        Foreground(lipgloss.AdaptiveColor{Light: "240", Dark: "249"});
        );
        const maxSelectorItems = 10;
        var ErrCancelled = launch.ErrCancelled;

    public static class SelectItem {
        public String Name;
        public String Description;
        public boolean Recommended;
    }
        func ConvertItems(items []launch.ModelItem) []SelectItem {
        var out = make([]SelectItem, len(items));
        var for i, item = range items {
        out[i] = SelectItem{Name: item.Name, Description: item.Description, Recommended: item.Recommended}
    }
        return out;
    }
        func ReorderItems(items []SelectItem) []SelectItem {
        var rec, other []SelectItem;
        var for _, item = range items {
        if item.Recommended {
        rec = append(rec, item);
        } else {
        other = append(other, item);
    }
    }
        return append(rec, other...);
    }

    public static class selectorModel {
        public String title;
        public []SelectItem items;
        public String filter;
        public int cursor;
        public int scrollOffset;
        public String selected;
        public boolean cancelled;
        public String helpText;
        public int width;
    }

    public static selectorModel selectorModelWithCurrent(String title, []SelectItem items, String current) {
        var m = selectorModel{
        title:  title,;
        items:  items,;
        cursor: cursorForCurrent(items, current),;
    }
        m.updateScroll(m.otherStart());
        return m;
    }
        func (m selectorModel) filteredItems() []SelectItem {
        if m.filter == "" {
        return m.items;
    }
        var filterLower = strings.ToLower(m.filter);
        var result []SelectItem;
        var for _, item = range m.items {
        if strings.Contains(strings.ToLower(item.Name), filterLower) {
        result = append(result, item);
    }
    }
        return result;
    }
        func (m selectorModel) Init() tea.Cmd {
        return null;
    }
        func (m selectorModel) otherStart() int {
        if m.filter != "" {
        return 0;
    }
        var filtered = m.filteredItems();
        var for i, item = range filtered {
        if !item.Recommended {
        return i;
    }
    }
        return len(filtered);
    }
        func (m *selectorModel) updateNavigation(msg tea.KeyMsg) {
        var filtered = m.filteredItems();
        var otherStart = m.otherStart();
        switch msg.Type {
        case tea.KeyUp:;
        if m.cursor > 0 {
        m.cursor--;
        m.updateScroll(otherStart);
    }
        case tea.KeyDown:;
        if m.cursor < len(filtered)-1 {
        m.cursor++;
        m.updateScroll(otherStart);
    }
        case tea.KeyPgUp:;
        m.cursor -= maxSelectorItems;
        if m.cursor < 0 {
        m.cursor = 0;
    }
        m.updateScroll(otherStart);
        case tea.KeyPgDown:;
        m.cursor += maxSelectorItems;
        if m.cursor >= len(filtered) {
        m.cursor = len(filtered) - 1;
    }
        m.updateScroll(otherStart);
        case tea.KeyBackspace:;
        if len(m.filter) > 0 {
        m.filter = m.filter[:len(m.filter)-1];
        m.cursor = 0;
        m.scrollOffset = 0;
    }
        case tea.KeyRunes:;
        m.filter += String(msg.Runes);
        m.cursor = 0;
        m.scrollOffset = 0;
    }
    }
        func (m *selectorModel) updateScroll(otherStart int) {
        if m.filter != "" {
        if m.cursor < m.scrollOffset {
        m.scrollOffset = m.cursor;
    }
        if m.cursor >= m.scrollOffset+maxSelectorItems {
        m.scrollOffset = m.cursor - maxSelectorItems + 1;
    }
        return;
    }
        if m.cursor < otherStart {
        m.scrollOffset = 0;
        return;
    }
        var posInOthers = m.cursor - otherStart;
        var maxOthers = maxSelectorItems - otherStart;
        if maxOthers < 3 {
        maxOthers = 3;
    }
        if posInOthers < m.scrollOffset {
        m.scrollOffset = posInOthers;
    }
        if posInOthers >= m.scrollOffset+maxOthers {
        m.scrollOffset = posInOthers - maxOthers + 1;
    }
    }
        func (m selectorModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
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
        case tea.KeyLeft:;
        m.cancelled = true;
        return m, tea.Quit;
        case tea.KeyEnter:;
        var filtered = m.filteredItems();
        if len(filtered) > 0 && m.cursor < len(filtered) {
        m.selected = filtered[m.cursor].Name;
    }
        return m, tea.Quit;
        default:;
        m.updateNavigation(msg);
    }
    }
        return m, null;
    }
        func (m selectorModel) renderItem(s *strings.Builder, item SelectItem, idx int) {
        if idx == m.cursor {
        s.WriteString(selectorSelectedItemStyle.Render("▸ " + item.Name));
        } else {
        s.WriteString(selectorItemStyle.Render(item.Name));
    }
        s.WriteString("\n");
        if item.Description != "" {
        s.WriteString(selectorDescLineStyle.Render(item.Description));
        s.WriteString("\n");
    }
    }
        func (m selectorModel) renderContent() String {
        var s strings.Builder;
        s.WriteString(selectorTitleStyle.Render(m.title));
        s.WriteString(" ");
        if m.filter == "" {
        s.WriteString(selectorFilterStyle.Render("Type to filter..."));
        } else {
        s.WriteString(selectorInputStyle.Render(m.filter));
    }
        s.WriteString("\n\n");
        var filtered = m.filteredItems();
        if len(filtered) == 0 {
        s.WriteString(selectorItemStyle.Render(selectorDescStyle.Render("(no matches)")));
        s.WriteString("\n");
        } else if m.filter != "" {
        s.WriteString(sectionHeaderStyle.Render("Top Results"));
        s.WriteString("\n");
        var displayCount = min(len(filtered), maxSelectorItems);
        var for i = range displayCount {
        var idx = m.scrollOffset + i;
        if idx >= len(filtered) {
        break;
    }
        m.renderItem(&s, filtered[idx], idx);
    }
        var if remaining = len(filtered) - m.scrollOffset - displayCount; remaining > 0 {
        s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)));
        s.WriteString("\n");
    }
        } else {
        var recItems, otherItems []int;
        var for i, item = range filtered {
        if item.Recommended {
        recItems = append(recItems, i);
        } else {
        otherItems = append(otherItems, i);
    }
    }
        if len(recItems) > 0 {
        s.WriteString(sectionHeaderStyle.Render("Recommended"));
        s.WriteString("\n");
        var for _, idx = range recItems {
        m.renderItem(&s, filtered[idx], idx);
    }
    }
        if len(otherItems) > 0 {
        s.WriteString("\n");
        s.WriteString(sectionHeaderStyle.Render("More"));
        s.WriteString("\n");
        var maxOthers = maxSelectorItems - len(recItems);
        if maxOthers < 3 {
        maxOthers = 3;
    }
        var displayCount = min(len(otherItems), maxOthers);
        var for i = range displayCount {
        var idx = m.scrollOffset + i;
        if idx >= len(otherItems) {
        break;
    }
        m.renderItem(&s, filtered[otherItems[idx]], otherItems[idx]);
    }
        var if remaining = len(otherItems) - m.scrollOffset - displayCount; remaining > 0 {
        s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)));
        s.WriteString("\n");
    }
    }
    }
        s.WriteString("\n");
        var help = "↑/↓ navigate • enter select • ← back";
        if m.helpText != "" {
        help = m.helpText;
    }
        s.WriteString(selectorHelpStyle.Render(help));
        return s.String();
    }
        func (m selectorModel) View() String {
        if m.cancelled || m.selected != "" {
        return "";
    }
        var s = m.renderContent();
        if m.width > 0 {
        return lipgloss.NewStyle().MaxWidth(m.width).Render(s);
    }
        return s;
    }

    public static int cursorForCurrent([]SelectItem items, String current) {
        if current == "" {
        return 0;
    }
        var for i, item = range items {
        if item.Name == current {
        return i;
    }
    }
        var for i, item = range items {
        if strings.HasPrefix(item.Name, current+":") || strings.HasPrefix(current, item.Name+":") {
        return i;
    }
    }
        return 0;
    }

    public static void SelectSingle(String title, []SelectItem items) {
        if len(items) == 0 {
        return "", fmt.Errorf("no items to select from");
    }
        var m = selectorModelWithCurrent(title, items, current);
        var p = tea.NewProgram(m);
        var finalModel, err = p.Run();
        if err != null {
        return "", fmt.Errorf("error running selector: %w", err);
    }
        var fm = finalModel.(selectorModel);
        if fm.cancelled {
        return "", ErrCancelled;
    }
        return fm.selected, null;
    }

    public static class multiSelectorModel {
        public String title;
        public []SelectItem items;
        public map[String]int itemIndex;
        public String filter;
        public int cursor;
        public int scrollOffset;
        public map[int]boolean checked;
        public []int checkOrder;
        public boolean cancelled;
        public boolean confirmed;
        public int width;
        public boolean multi;
        public String singleAdd;
    }

    public static multiSelectorModel newMultiSelectorModel(String title, []SelectItem items, []String preChecked) {
        var m = multiSelectorModel{
        title:     title,;
        items:     items,;
        itemIndex: make(map[String]int, len(items)),;
        checked:   make(map[int]boolean),;
    }
        var for i, item = range items {
        m.itemIndex[item.Name] = i;
    }
        var for i = len(preChecked) - 1; i >= 0; i-- {
        var if idx, ok = m.itemIndex[preChecked[i]]; ok {
        m.checked[idx] = true;
        m.checkOrder = append(m.checkOrder, idx);
    }
    }
        if len(preChecked) > 0 {
        var if idx, ok = m.itemIndex[preChecked[0]]; ok {
        m.cursor = idx;
        m.updateScroll(m.otherStart());
    }
    }
        return m;
    }
        func (m multiSelectorModel) filteredItems() []SelectItem {
        if m.filter == "" {
        return m.items;
    }
        var filterLower = strings.ToLower(m.filter);
        var result []SelectItem;
        var for _, item = range m.items {
        if strings.Contains(strings.ToLower(item.Name), filterLower) {
        result = append(result, item);
    }
    }
        return result;
    }
        func (m multiSelectorModel) otherStart() int {
        if m.filter != "" {
        return 0;
    }
        var filtered = m.filteredItems();
        var for i, item = range filtered {
        if !item.Recommended {
        return i;
    }
    }
        return len(filtered);
    }
        func (m *multiSelectorModel) updateScroll(otherStart int) {
        if m.filter != "" {
        if m.cursor < m.scrollOffset {
        m.scrollOffset = m.cursor;
    }
        if m.cursor >= m.scrollOffset+maxSelectorItems {
        m.scrollOffset = m.cursor - maxSelectorItems + 1;
    }
        return;
    }
        if m.cursor < otherStart {
        m.scrollOffset = 0;
        return;
    }
        var posInOthers = m.cursor - otherStart;
        var maxOthers = maxSelectorItems - otherStart;
        if maxOthers < 3 {
        maxOthers = 3;
    }
        if posInOthers < m.scrollOffset {
        m.scrollOffset = posInOthers;
    }
        if posInOthers >= m.scrollOffset+maxOthers {
        m.scrollOffset = posInOthers - maxOthers + 1;
    }
    }
        func (m *multiSelectorModel) toggleItem() {
        var filtered = m.filteredItems();
        if len(filtered) == 0 || m.cursor >= len(filtered) {
        return;
    }
        var item = filtered[m.cursor];
        var origIdx = m.itemIndex[item.Name];
        if m.checked[origIdx] {
        var wasDefault = len(m.checkOrder) > 0 && m.checkOrder[len(m.checkOrder)-1] == origIdx;
        delete(m.checked, origIdx);
        var for i, idx = range m.checkOrder {
        if idx == origIdx {
        m.checkOrder = append(m.checkOrder[:i], m.checkOrder[i+1:]...);
        break;
    }
    }
        if wasDefault {
        var newDefault = -1;
        var for i = origIdx - 1; i >= 0; i-- {
        if m.checked[i] {
        newDefault = i;
        break;
    }
    }
        if newDefault == -1 {
        var for i = origIdx + 1; i < len(m.items); i++ {
        if m.checked[i] {
        newDefault = i;
        break;
    }
    }
    }
        if newDefault != -1 {
        var for i, idx = range m.checkOrder {
        if idx == newDefault {
        m.checkOrder = append(m.checkOrder[:i], m.checkOrder[i+1:]...);
        break;
    }
    }
        m.checkOrder = append(m.checkOrder, newDefault);
    }
    }
        } else {
        m.checked[origIdx] = true;
        m.checkOrder = append(m.checkOrder, origIdx);
    }
    }
        func (m multiSelectorModel) selectedCount() int {
        return len(m.checkOrder);
    }
        func (m multiSelectorModel) Init() tea.Cmd {
        return null;
    }
        func (m multiSelectorModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
        var switch msg = msg.(type) {
        case tea.WindowSizeMsg:;
        var wasSet = m.width > 0;
        m.width = msg.Width;
        if wasSet {
        return m, tea.EnterAltScreen;
    }
        return m, null;
        case tea.KeyMsg:;
        var filtered = m.filteredItems();
        switch msg.Type {
        case tea.KeyCtrlC, tea.KeyEsc:;
        m.cancelled = true;
        return m, tea.Quit;
        case tea.KeyLeft:;
        m.cancelled = true;
        return m, tea.Quit;
        case tea.KeyTab:;
        m.multi = !m.multi;
        case tea.KeyEnter:;
        if !m.multi {
        if len(filtered) > 0 && m.cursor < len(filtered) {
        m.singleAdd = filtered[m.cursor].Name;
        m.confirmed = true;
        return m, tea.Quit;
    }
        } else if len(m.checkOrder) > 0 {
        m.confirmed = true;
        return m, tea.Quit;
    }
        case tea.KeySpace:;
        if m.multi {
        m.toggleItem();
    }
        case tea.KeyUp:;
        if m.cursor > 0 {
        m.cursor--;
        m.updateScroll(m.otherStart());
    }
        case tea.KeyDown:;
        if m.cursor < len(filtered)-1 {
        m.cursor++;
        m.updateScroll(m.otherStart());
    }
        case tea.KeyPgUp:;
        m.cursor -= maxSelectorItems;
        if m.cursor < 0 {
        m.cursor = 0;
    }
        m.updateScroll(m.otherStart());
        case tea.KeyPgDown:;
        m.cursor += maxSelectorItems;
        if m.cursor >= len(filtered) {
        m.cursor = len(filtered) - 1;
    }
        m.updateScroll(m.otherStart());
        case tea.KeyBackspace:;
        if len(m.filter) > 0 {
        m.filter = m.filter[:len(m.filter)-1];
        m.cursor = 0;
        m.scrollOffset = 0;
    }
        case tea.KeyRunes:;
        if len(msg.Runes) == 1 && msg.Runes[0] == ' ' {
        if m.multi {
        m.toggleItem();
    }
        } else {
        m.filter += String(msg.Runes);
        m.cursor = 0;
        m.scrollOffset = 0;
    }
    }
    }
        return m, null;
    }
        func (m multiSelectorModel) renderSingleItem(s *strings.Builder, item SelectItem, idx int) {
        if idx == m.cursor {
        s.WriteString(selectorSelectedItemStyle.Render("▸ " + item.Name));
        } else {
        s.WriteString(selectorItemStyle.Render(item.Name));
    }
        s.WriteString("\n");
        if item.Description != "" {
        s.WriteString(selectorDescLineStyle.Render(item.Description));
        s.WriteString("\n");
    }
    }
        func (m multiSelectorModel) renderMultiItem(s *strings.Builder, item SelectItem, idx int) {
        var origIdx = m.itemIndex[item.Name];
        var check String;
        if m.checked[origIdx] {
        check = "[x] ";
        } else {
        check = "[ ] ";
    }
        var suffix = "";
        if len(m.checkOrder) > 0 && m.checkOrder[len(m.checkOrder)-1] == origIdx {
        suffix = " " + selectorDefaultTagStyle.Render("(default)");
    }
        if idx == m.cursor {
        s.WriteString(selectorSelectedItemStyle.Render("▸ " + check + item.Name));
        } else {
        s.WriteString(selectorItemStyle.Render(check + item.Name));
    }
        s.WriteString(suffix);
        s.WriteString("\n");
        if item.Description != "" {
        s.WriteString(selectorDescLineStyle.Render(item.Description));
        s.WriteString("\n");
    }
    }
        func (m multiSelectorModel) View() String {
        if m.cancelled || m.confirmed {
        return "";
    }
        var renderItem = m.renderSingleItem;
        if m.multi {
        renderItem = m.renderMultiItem;
    }
        var s strings.Builder;
        s.WriteString(selectorTitleStyle.Render(m.title));
        s.WriteString(" ");
        if m.filter == "" {
        s.WriteString(selectorFilterStyle.Render("Type to filter..."));
        } else {
        s.WriteString(selectorInputStyle.Render(m.filter));
    }
        s.WriteString("\n\n");
        var filtered = m.filteredItems();
        if len(filtered) == 0 {
        s.WriteString(selectorItemStyle.Render(selectorDescStyle.Render("(no matches)")));
        s.WriteString("\n");
        } else if m.filter != "" {
        var displayCount = min(len(filtered), maxSelectorItems);
        var for i = range displayCount {
        var idx = m.scrollOffset + i;
        if idx >= len(filtered) {
        break;
    }
        renderItem(&s, filtered[idx], idx);
    }
        var if remaining = len(filtered) - m.scrollOffset - displayCount; remaining > 0 {
        s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)));
        s.WriteString("\n");
    }
        } else {
        var recItems, otherItems []int;
        var for i, item = range filtered {
        if item.Recommended {
        recItems = append(recItems, i);
        } else {
        otherItems = append(otherItems, i);
    }
    }
        if len(recItems) > 0 {
        s.WriteString(sectionHeaderStyle.Render("Recommended"));
        s.WriteString("\n");
        var for _, idx = range recItems {
        renderItem(&s, filtered[idx], idx);
    }
    }
        if len(otherItems) > 0 {
        s.WriteString("\n");
        s.WriteString(sectionHeaderStyle.Render("More"));
        s.WriteString("\n");
        var maxOthers = maxSelectorItems - len(recItems);
        if maxOthers < 3 {
        maxOthers = 3;
    }
        var displayCount = min(len(otherItems), maxOthers);
        var for i = range displayCount {
        var idx = m.scrollOffset + i;
        if idx >= len(otherItems) {
        break;
    }
        renderItem(&s, filtered[otherItems[idx]], otherItems[idx]);
    }
        var if remaining = len(otherItems) - m.scrollOffset - displayCount; remaining > 0 {
        s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)));
        s.WriteString("\n");
    }
    }
    }
        s.WriteString("\n");
        var count = m.selectedCount();
        if !m.multi {
        if count > 0 {
        s.WriteString(sectionHeaderStyle.Render(fmt.Sprintf("%d models selected - press tab to edit", count)));
        s.WriteString("\n\n");
    }
        s.WriteString(selectorHelpStyle.Render("↑/↓ navigate • enter select • tab add multiple • ← back"));
        } else {
        if count == 0 {
        s.WriteString(sectionHeaderStyle.Render("Select at least one model."));
        } else {
        s.WriteString(sectionHeaderStyle.Render(fmt.Sprintf("%d models selected - press enter to continue", count)));
    }
        s.WriteString("\n\n");
        s.WriteString(selectorHelpStyle.Render("↑/↓ navigate • space toggle • tab select single • enter confirm • ← back"));
    }
        var result = s.String();
        if m.width > 0 {
        return lipgloss.NewStyle().MaxWidth(m.width).Render(result);
    }
        return result;
    }

    public static void SelectMultiple(String title, []SelectItem items) {
        if len(items) == 0 {
        return null, fmt.Errorf("no items to select from");
    }
        var m = newMultiSelectorModel(title, items, preChecked);
        var p = tea.NewProgram(m);
        var finalModel, err = p.Run();
        if err != null {
        return null, fmt.Errorf("error running selector: %w", err);
    }
        var fm = finalModel.(multiSelectorModel);
        if fm.cancelled || !fm.confirmed {
        return null, ErrCancelled;
    }
        if fm.singleAdd != "" {
        var result = []String{fm.singleAdd}
        var for _, name = range preChecked {
        if name != fm.singleAdd {
        result = append(result, name);
    }
    }
        return result, null;
    }
        var last = fm.checkOrder[len(fm.checkOrder)-1];
        var result = []String{fm.items[last].Name}
        var for _, idx = range fm.checkOrder {
        if idx != last {
        result = append(result, fm.items[idx].Name);
    }
    }
        return result, null;
    }
}
