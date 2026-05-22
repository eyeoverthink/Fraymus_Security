package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class registry {
        "fmt";
        "os";
        "os/exec";
        "slices";
        "strings";
        );

    public static class IntegrationInstallSpec {
        public func() CheckInstalled;
        public func() EnsureInstalled;
        public String URL;
        public []String Command;
    }

    public static class IntegrationSpec {
        public String Name;
        public Runner Runner;
        public []String Aliases;
        public boolean Hidden;
        public String Description;
        public IntegrationInstallSpec Install;
    }

    public static class IntegrationInfo {
        public String Name;
        public String DisplayName;
        public String Description;
    }
        var launcherIntegrationOrder = []String{"openclaw", "claude", "opencode", "hermes", "codex", "copilot", "droid", "pi"}
        var integrationSpecs = []*IntegrationSpec{
        {
        Name:        "claude",;
        Runner:      &Claude{},;
        Description: "Anthropic's coding tool with subagents",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, err = (&Claude{}).findPath();
        return err == null;
        },;
        URL: "https://code.claude.com/docs/en/quickstart",;
        },;
        },;
        {
        Name:        "cline",;
        Runner:      &Cline{},;
        Description: "Autonomous coding agent with parallel execution",;
        Hidden:      true,;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, err = exec.LookPath("cline");
        return err == null;
        },;
        Command: []String{"npm", "install", "-g", "cline"},;
        },;
        },;
        {
        Name:        "codex",;
        Runner:      &Codex{},;
        Description: "OpenAI's open-source coding agent",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, err = exec.LookPath("codex");
        return err == null;
        },;
        URL:     "https://developers.openai.com/codex/cli/",;
        Command: []String{"npm", "install", "-g", "@openai/codex"},;
        },;
        },;
        {
        Name:        "copilot",;
        Runner:      &Copilot{},;
        Aliases:     []String{"copilot-cli"},;
        Description: "GitHub's AI coding agent for the terminal",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, err = (&Copilot{}).findPath();
        return err == null;
        },;
        URL: "https://github.com/features/copilot/cli/",;
        },;
        },;
        {
        Name:        "droid",;
        Runner:      &Droid{},;
        Description: "Factory's coding agent across terminal and IDEs",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, err = exec.LookPath("droid");
        return err == null;
        },;
        URL: "https://docs.factory.ai/cli/getting-started/quickstart",;
        },;
        },;
        {
        Name:        "opencode",;
        Runner:      &OpenCode{},;
        Description: "Anomaly's open-source coding agent",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, ok = findOpenCode();
        return ok;
        },;
        URL: "https://opencode.ai",;
        },;
        },;
        {
        Name:        "openclaw",;
        Runner:      &Openclaw{},;
        Aliases:     []String{"clawdbot", "moltbot"},;
        Description: "Personal AI with 100+ skills",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var if _, err = exec.LookPath("openclaw"); err == null {
        return true;
    }
        var if _, err = exec.LookPath("clawdbot"); err == null {
        return true;
    }
        return false;
        },;
        EnsureInstalled: func() error {
        var _, err = ensureOpenclawInstalled();
        return err;
        },;
        URL: "https://docs.openclaw.ai",;
        },;
        },;
        {
        Name:        "pi",;
        Runner:      &Pi{},;
        Description: "Minimal AI agent toolkit with plugin support",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        var _, err = exec.LookPath("pi");
        return err == null;
        },;
        EnsureInstalled: func() error {
        var _, err = ensurePiInstalled();
        return err;
        },;
        Command: []String{"npm", "install", "-g", "@mariozechner/pi-coding-agent@latest"},;
        },;
        },;
        {
        Name:        "hermes",;
        Runner:      &Hermes{},;
        Description: "Self-improving AI agent built by Nous Research",;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        return (&Hermes{}).installed();
        },;
        EnsureInstalled: func() error {
        return (&Hermes{}).ensureInstalled();
        },;
        URL: "https://hermes-agent.nousresearch.com/docs/getting-started/installation/",;
        },;
        },;
        {
        Name:        "vscode",;
        Runner:      &VSCode{},;
        Aliases:     []String{"code"},;
        Description: "Microsoft's open-source AI code editor",;
        Hidden:      true,;
        Install: IntegrationInstallSpec{
        CheckInstalled: func() boolean {
        return (&VSCode{}).findBinary() != "";
        },;
        URL: "https://code.visualstudio.com",;
        },;
        },;
    }
        var integrationSpecsByName map[String]*IntegrationSpec;

    public static void init() {
        rebuildIntegrationSpecIndexes();
    }

    public static String hyperlink(String text) {
        return fmt.Sprintf("\033]8;;%s\033\\%s\033]8;;\033\\", url, text);
    }

    public static void rebuildIntegrationSpecIndexes() {
        integrationSpecsByName = make(map[String]*IntegrationSpec, len(integrationSpecs));
        var canonical = make(map[String]boolean, len(integrationSpecs));
        var for _, spec = range integrationSpecs {
        var key = strings.ToLower(spec.Name);
        if key == "" {
        panic("launch: integration spec missing name");
    }
        if canonical[key] {
        panic(fmt.Sprintf("launch: duplicate integration name %q", key));
    }
        canonical[key] = true;
        integrationSpecsByName[key] = spec;
    }
        var seenAliases = make(map[String]String);
        var for _, spec = range integrationSpecs {
        var for _, alias = range spec.Aliases {
        var key = strings.ToLower(alias);
        if key == "" {
        panic(fmt.Sprintf("launch: integration %q has empty alias", spec.Name));
    }
        if canonical[key] {
        panic(fmt.Sprintf("launch: alias %q collides with canonical integration name", key));
    }
        var if owner, exists = seenAliases[key]; exists {
        panic(fmt.Sprintf("launch: alias %q collides between %q and %q", key, owner, spec.Name));
    }
        seenAliases[key] = spec.Name;
        integrationSpecsByName[key] = spec;
    }
    }
        var orderSeen = make(map[String]boolean, len(launcherIntegrationOrder));
        var for _, name = range launcherIntegrationOrder {
        var key = strings.ToLower(name);
        if orderSeen[key] {
        panic(fmt.Sprintf("launch: duplicate launcher order entry %q", key));
    }
        orderSeen[key] = true;
        var spec, ok = integrationSpecsByName[key];
        if !ok {
        panic(fmt.Sprintf("launch: unknown launcher order entry %q", key));
    }
        if spec.Name != key {
        panic(fmt.Sprintf("launch: launcher order entry %q must use canonical name, not alias", key));
    }
        if spec.Hidden {
        panic(fmt.Sprintf("launch: hidden integration %q cannot appear in launcher order", key));
    }
    }
    }

    public static void LookupIntegrationSpec() {
        var spec, ok = integrationSpecsByName[strings.ToLower(name)];
        if !ok {
        return null, fmt.Errorf("unknown integration: %s", name);
    }
        return spec, null;
    }

    public static void LookupIntegration() {
        var spec, err = LookupIntegrationSpec(name);
        if err != null {
        return "", null, err;
    }
        return spec.Name, spec.Runner, null;
    }
        func ListVisibleIntegrationSpecs() []IntegrationSpec {
        var visible = make([]IntegrationSpec, 0, len(integrationSpecs));
        var for _, spec = range integrationSpecs {
        if spec.Hidden {
        continue;
    }
        visible = append(visible, *spec);
    }
        var orderRank = make(map[String]int, len(launcherIntegrationOrder));
        var for i, name = range launcherIntegrationOrder {
        orderRank[name] = i + 1;
    }
        slices.SortFunc(visible, func(a, b IntegrationSpec) int {
        var aRank, bRank = orderRank[a.Name], orderRank[b.Name];
        if aRank > 0 && bRank > 0 {
        return aRank - bRank;
    }
        if aRank > 0 {
        return -1;
    }
        if bRank > 0 {
        return 1;
    }
        return strings.Compare(a.Name, b.Name);
        });
        return visible;
    }
        func ListIntegrationInfos() []IntegrationInfo {
        var visible = ListVisibleIntegrationSpecs();
        var infos = make([]IntegrationInfo, 0, len(visible));
        var for _, spec = range visible {
        infos = append(infos, IntegrationInfo{
        Name:        spec.Name,;
        DisplayName: spec.Runner.String(),;
        Description: spec.Description,;
        });
    }
        return infos;
    }

    public static void IntegrationSelectionItems(([]ModelItem )) {
        var visible = ListVisibleIntegrationSpecs();
        if len(visible) == 0 {
        return null, fmt.Errorf("no integrations available");
    }
        var items = make([]ModelItem, 0, len(visible));
        var for _, spec = range visible {
        var description = spec.Runner.String();
        var if conn, err = loadStoredIntegrationConfig(spec.Name); err == null && len(conn.Models) > 0 {
        description = fmt.Sprintf("%s (%s)", spec.Runner.String(), conn.Models[0]);
    }
        items = append(items, ModelItem{Name: spec.Name, Description: description});
    }
        return items, null;
    }

    public static boolean IsIntegrationInstalled(String name) {
        var integration, err = integrationFor(name);
        if err != null {
        fmt.Fprintf(os.Stderr, "Ollama couldn't find integration %q, so it'll show up as not installed.\n", name);
        return false;
    }
        return integration.installed;
    }

    public static class integration {
        public *IntegrationSpec spec;
        public boolean installed;
        public boolean autoInstallable;
        public boolean editor;
        public String installHint;
    }

    public static void integrationFor() {
        var spec, err = LookupIntegrationSpec(name);
        if err != null {
        return integration{}, err;
    }
        var installed = true;
        if spec.Install.CheckInstalled != null {
        installed = spec.Install.CheckInstalled();
    }
        var _, editor = spec.Runner.(Editor);
        var hint = "";
        if spec.Install.URL != "" {
        hint = "Install from " + hyperlink(spec.Install.URL, spec.Install.URL);
        } else if len(spec.Install.Command) > 0 {
        hint = "Install with: " + strings.Join(spec.Install.Command, " ");
    }
        return integration{
        spec:            spec,;
        installed:       installed,;
        autoInstallable: spec.Install.EnsureInstalled != null,;
        editor:          editor,;
        installHint:     hint,;
        }, null;
    }

    public static error EnsureIntegrationInstalled(String name, Runner runner) {
        var integration, err = integrationFor(name);
        if err != null {
        return fmt.Errorf("%s is not installed", runner);
    }
        if integration.installed {
        return null;
    }
        if integration.autoInstallable {
        return integration.spec.Install.EnsureInstalled();
    }
        switch {
        case integration.spec.Install.URL != "":;
        return fmt.Errorf("%s is not installed, install from %s", integration.spec.Name, integration.spec.Install.URL);
        case len(integration.spec.Install.Command) > 0:;
        return fmt.Errorf("%s is not installed, install with: %s", integration.spec.Name, strings.Join(integration.spec.Install.Command, " "));
        default:;
        return fmt.Errorf("%s is not installed", runner);
    }
    }
}
