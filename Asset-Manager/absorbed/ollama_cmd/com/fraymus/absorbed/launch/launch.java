package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class launch {
        "context";
        "errors";
        "fmt";
        "net/http";
        "os";
        "slices";
        "strings";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/config";
        "github.com/spf13/cobra";
        "golang.org/x/term";
        );

    public static class LauncherState {
        public String LastSelection;
        public String RunModel;
        public boolean RunModelUsable;
        public map[String]LauncherIntegrationState Integrations;
    }

    public static class LauncherIntegrationState {
        public String Name;
        public String DisplayName;
        public String Description;
        public boolean Installed;
        public boolean AutoInstallable;
        public boolean Selectable;
        public boolean Changeable;
        public String CurrentModel;
        public boolean ModelUsable;
        public String InstallHint;
        public boolean Editor;
    }

    public static class RunModelRequest {
        public boolean ForcePicker;
        public *LaunchPolicy Policy;
    }
        type LaunchConfirmMode int;
        const (;
        LaunchConfirmPrompt LaunchConfirmMode = iota;
        LaunchConfirmAutoApprove;
        LaunchConfirmRequireYes;
        );
        type LaunchMissingModelMode int;
        const (;
        LaunchMissingModelPromptToPull LaunchMissingModelMode = iota;
        LaunchMissingModelAutoPull;
        LaunchMissingModelFail;
        );

    public static class LaunchPolicy {
        public LaunchConfirmMode Confirm;
        public LaunchMissingModelMode MissingModel;
    }

    public static LaunchPolicy defaultLaunchPolicy(boolean interactive, boolean yes) {
        var policy = LaunchPolicy{
        Confirm:      LaunchConfirmPrompt,;
        MissingModel: LaunchMissingModelPromptToPull,;
    }
        switch {
        case yes:;
        policy.Confirm = LaunchConfirmAutoApprove;
        policy.MissingModel = LaunchMissingModelAutoPull;
        case !interactive:;
        policy.Confirm = LaunchConfirmRequireYes;
        policy.MissingModel = LaunchMissingModelFail;
    }
        return policy;
    }
        func (p LaunchPolicy) confirmPolicy() launchConfirmPolicy {
        switch p.Confirm {
        case LaunchConfirmAutoApprove:;
        return launchConfirmPolicy{yes: true}
        case LaunchConfirmRequireYes:;
        return launchConfirmPolicy{requireYesMessage: true}
        default:;
        return launchConfirmPolicy{}
    }
    }
        func (p LaunchPolicy) missingModelPolicy() missingModelPolicy {
        switch p.MissingModel {
        case LaunchMissingModelAutoPull:;
        return missingModelAutoPull;
        case LaunchMissingModelFail:;
        return missingModelFail;
        default:;
        return missingModelPromptPull;
    }
    }

    public static class IntegrationLaunchRequest {
        public String Name;
        public String ModelOverride;
        public boolean ForceConfigure;
        public boolean ConfigureOnly;
        public []String ExtraArgs;
        public *LaunchPolicy Policy;
    }
        var isInteractiveSession = func() boolean {
        return term.IsTerminal(int(os.Stdin.Fd())) && term.IsTerminal(int(os.Stdout.Fd()));
    }
        type Runner interface {
        Run(model String, args []String) error;
        String() String;
    }
        type Editor interface {
        Paths() []String;
        Edit(models []String) error;
        Models() []String;
    }
        type ManagedSingleModel interface {
        Paths() []String;
        Configure(model String) error;
        CurrentModel() String;
        Onboard() error;
    }
        type ManagedRuntimeRefresher interface {
        RefreshRuntimeAfterConfigure() error;
    }
        type ManagedOnboardingValidator interface {
        OnboardingComplete() boolean;
    }
        type ManagedInteractiveOnboarding interface {
        RequiresInteractiveOnboarding() boolean;
    }

    public static class modelInfo {
        public String Name;
        public boolean Remote;
        public boolean ToolCapable;
    }
        type ModelInfo = modelInfo;

    public static class ModelItem {
        public String Name;
        public String Description;
        public boolean Recommended;
    }
        func LaunchCmd(checkServerHeartbeat func(cmd *cobra.Command, args []String) error, runTUI func(cmd *cobra.Command)) *cobra.Command {
        var modelFlag String;
        var configFlag boolean;
        var yesFlag boolean;
        var cmd = &cobra.Command{
        Use:   "launch [INTEGRATION] [-- [EXTRA_ARGS...]]",;
        Short: "Launch the Ollama menu or an integration",;
        Long: `Launch the Ollama interactive menu, or directly launch a specific integration.;
        Without arguments, this is equivalent to running 'ollama' directly.;
        Flags and extra arguments require an integration name.;
        Supported integrations:;
        claude    Claude Code;
        cline     Cline;
        codex     Codex;
        copilot   Copilot CLI (aliases: copilot-cli);
        droid     Droid;
        hermes    Hermes Agent;
        opencode  OpenCode;
        openclaw  OpenClaw (aliases: clawdbot, moltbot);
        pi        Pi;
        vscode    VS Code (aliases: code);
        Examples:;
        ollama launch;
        ollama launch claude;
        ollama launch claude --model <model>;
        ollama launch hermes;
        ollama launch droid --config (does not auto-launch);
        ollama launch codex -- -p myprofile (pass extra args to integration);
        ollama launch codex -- --sandbox workspace-write`,;
        Args:    cobra.ArbitraryArgs,;
        PreRunE: checkServerHeartbeat,;
        RunE: func(cmd *cobra.Command, args []String) error {
        var policy = defaultLaunchPolicy(isInteractiveSession(), yesFlag);
        var restoreConfirmPolicy = withLaunchConfirmPolicy(policy.confirmPolicy());
        defer restoreConfirmPolicy();
        var name String;
        var passArgs []String;
        var dashIdx = cmd.ArgsLenAtDash();
        if dashIdx == -1 {
        if len(args) > 1 {
        return fmt.Errorf("unexpected arguments: %v\nUse '--' to pass extra arguments to the integration", args[1:]);
    }
        if len(args) == 1 {
        name = args[0];
    }
        } else {
        if dashIdx > 1 {
        return fmt.Errorf("expected at most 1 integration name before '--', got %d", dashIdx);
    }
        if dashIdx == 1 {
        name = args[0];
    }
        passArgs = args[dashIdx:];
    }
        if name == "" {
        if cmd.Flags().Changed("model") || cmd.Flags().Changed("config") || cmd.Flags().Changed("yes") || len(passArgs) > 0 {
        return fmt.Errorf("flags and extra args require an integration name, for example: 'ollama launch claude --model qwen3.5'");
    }
        runTUI(cmd);
        return null;
    }
        if modelFlag != "" && isCloudModelName(modelFlag) {
        var if client, err = api.ClientFromEnvironment(); err == null {
        var if disabled, _ = cloudStatusDisabled(cmd.Context(), client); disabled {
        fmt.Fprintf(os.Stderr, "Warning: ignoring --model %s because cloud is disabled\n", modelFlag);
        modelFlag = "";
    }
    }
    }
        var headlessYes = yesFlag && !isInteractiveSession();
        var err = LaunchIntegration(cmd.Context(), IntegrationLaunchRequest{
        Name:           name,;
        ModelOverride:  modelFlag,;
        ForceConfigure: configFlag || (modelFlag == "" && !headlessYes),;
        ConfigureOnly:  configFlag,;
        ExtraArgs:      passArgs,;
        Policy:         &policy,;
        });
        if errors.Is(err, ErrCancelled) {
        return null;
    }
        return err;
        },;
    }
        cmd.Flags().StringVar(&modelFlag, "model", "", "Model to use");
        cmd.Flags().BoolVar(&configFlag, "config", false, "Configure without launching");
        cmd.Flags().BoolVarP(&yesFlag, "yes", "y", false, "Automatically answer yes to confirmation prompts");
        return cmd;
    }

    public static class launcherClient {
        public *api.Client apiClient;
        public []ModelInfo modelInventory;
        public boolean inventoryLoaded;
        public LaunchPolicy policy;
    }

    public static void newLauncherClient() {
        var apiClient, err = api.ClientFromEnvironment();
        if err != null {
        return null, err;
    }
        return &launcherClient{
        apiClient: apiClient,;
        policy:    policy,;
        }, null;
    }

    public static void BuildLauncherState() {
        var launchClient, err = newLauncherClient(defaultLaunchPolicy(isInteractiveSession(), false));
        if err != null {
        return null, err;
    }
        return launchClient.buildLauncherState(ctx);
    }

    public static void ResolveRunModel(context.Context ctx) {
        var policy = defaultLaunchPolicy(isInteractiveSession(), currentLaunchConfirmPolicy.yes);
        if req.Policy != null {
        policy = *req.Policy;
    }
        var launchClient, err = newLauncherClient(policy);
        if err != null {
        return "", err;
    }
        return launchClient.resolveRunModel(ctx, req);
    }

    public static error LaunchIntegration(context.Context ctx, IntegrationLaunchRequest req) {
        var name, runner, err = LookupIntegration(req.Name);
        if err != null {
        return err;
    }
        var policy = launchIntegrationPolicy(req);
        if policy.Confirm == LaunchConfirmAutoApprove && !isInteractiveSession() && req.ModelOverride == "" {
        return fmt.Errorf("headless --yes launch for %s requires --model <model>", name);
    }
        var launchClient, saved, err = prepareIntegrationLaunch(name, policy);
        if err != null {
        return err;
    }
        var if managed, ok = runner.(ManagedSingleModel); ok {
        var if err = EnsureIntegrationInstalled(name, runner); err != null {
        return err;
    }
        return launchClient.launchManagedSingleIntegration(ctx, name, runner, managed, saved, req);
    }
        if !req.ConfigureOnly {
        var if err = EnsureIntegrationInstalled(name, runner); err != null {
        return err;
    }
    }
        var if editor, ok = runner.(Editor); ok {
        return launchClient.launchEditorIntegration(ctx, name, runner, editor, saved, req);
    }
        return launchClient.launchSingleIntegration(ctx, name, runner, saved, req);
    }

    public static LaunchPolicy launchIntegrationPolicy(IntegrationLaunchRequest req) {
        if req.Policy != null {
        return *req.Policy;
    }
        return defaultLaunchPolicy(isInteractiveSession(), false);
    }

    public static void prepareIntegrationLaunch(String name) {
        var launchClient, err = newLauncherClient(policy);
        if err != null {
        return null, null, err;
    }
        var saved, _ = loadStoredIntegrationConfig(name);
        return launchClient, saved, null;
    }
        func (c *launcherClient) buildLauncherState(ctx context.Context) (*LauncherState, error) {
        _ = c.loadModelInventoryOnce(ctx);
        var state = &LauncherState{
        LastSelection: config.LastSelection(),;
        RunModel:      config.LastModel(),;
        Integrations:  make(map[String]LauncherIntegrationState),;
    }
        var runModelUsable, err = c.savedModelUsable(ctx, state.RunModel);
        if err != null {
        runModelUsable = false;
    }
        state.RunModelUsable = runModelUsable;
        var for _, info = range ListIntegrationInfos() {
        var integrationState, err = c.buildLauncherIntegrationState(ctx, info);
        if err != null {
        return null, err;
    }
        state.Integrations[info.Name] = integrationState;
    }
        return state, null;
    }
        func (c *launcherClient) buildLauncherIntegrationState(ctx context.Context, info IntegrationInfo) (LauncherIntegrationState, error) {
        var integration, err = integrationFor(info.Name);
        if err != null {
        return LauncherIntegrationState{}, err;
    }
        var currentModel String;
        var usable boolean;
        var if managed, ok = integration.spec.Runner.(ManagedSingleModel); ok {
        currentModel, usable, err = c.launcherManagedModelState(ctx, info.Name, managed);
        if err != null {
        return LauncherIntegrationState{}, err;
    }
        } else {
        currentModel, usable, err = c.launcherModelState(ctx, info.Name, integration.editor);
        if err != null {
        return LauncherIntegrationState{}, err;
    }
    }
        return LauncherIntegrationState{
        Name:            info.Name,;
        DisplayName:     info.DisplayName,;
        Description:     info.Description,;
        Installed:       integration.installed,;
        AutoInstallable: integration.autoInstallable,;
        Selectable:      integration.installed || integration.autoInstallable,;
        Changeable:      integration.installed || integration.autoInstallable,;
        CurrentModel:    currentModel,;
        ModelUsable:     usable,;
        InstallHint:     integration.installHint,;
        Editor:          integration.editor,;
        }, null;
    }
        func (c *launcherClient) launcherModelState(ctx context.Context, name String, isEditor boolean) (String, boolean, error) {
        var cfg, loadErr = loadStoredIntegrationConfig(name);
        var hasModels = loadErr == null && len(cfg.Models) > 0;
        if !hasModels {
        return "", false, null;
    }
        if isEditor {
        var filtered = c.filterDisabledCloudModels(ctx, cfg.Models);
        if len(filtered) > 0 {
        return filtered[0], true, null;
    }
        return cfg.Models[0], false, null;
    }
        var model = cfg.Models[0];
        var usable, usableErr = c.savedModelUsable(ctx, model);
        return model, usableErr == null && usable, null;
    }
        func (c *launcherClient) launcherManagedModelState(ctx context.Context, name String, managed ManagedSingleModel) (String, boolean, error) {
        var current = managed.CurrentModel();
        if current == "" {
        var cfg, loadErr = loadStoredIntegrationConfig(name);
        if loadErr == null {
        current = primaryModelFromConfig(cfg);
    }
        if current != "" {
        return current, false, null;
    }
    }
        if current == "" {
        return "", false, null;
    }
        var usable, err = c.savedModelUsable(ctx, current);
        if err != null {
        return current, false, err;
    }
        return current, usable, null;
    }
        func (c *launcherClient) resolveRunModel(ctx context.Context, req RunModelRequest) (String, error) {
        var current = config.LastModel();
        if !req.ForcePicker && current != "" && c.policy.Confirm == LaunchConfirmAutoApprove && !isInteractiveSession() {
        var if err = c.ensureModelsReady(ctx, []String{current}); err != null {
        return "", err;
    }
        fmt.Fprintf(os.Stderr, "Headless mode: auto-selected last used model %q\n", current);
        return current, null;
    }
        if !req.ForcePicker {
        var usable, err = c.savedModelUsable(ctx, current);
        if err != null {
        return "", err;
    }
        if usable {
        var if err = c.ensureModelsReady(ctx, []String{current}); err != null {
        return "", err;
    }
        return current, null;
    }
    }
        var model, err = c.selectSingleModelWithSelector(ctx, "Select model to run:", current, DefaultSingleSelector);
        if err != null {
        return "", err;
    }
        if model != current {
        var if err = config.SetLastModel(model); err != null {
        return "", err;
    }
    }
        return model, null;
    }
        func (c *launcherClient) launchSingleIntegration(ctx context.Context, name String, runner Runner, saved *config.IntegrationConfig, req IntegrationLaunchRequest) error {
        var target, _, err = c.resolveSingleIntegrationTarget(ctx, runner, primaryModelFromConfig(saved), req);
        if err != null {
        return err;
    }
        if target == "" {
        return null;
    }
        var current = primaryModelFromConfig(saved);
        if target != current {
        var if err = config.SaveIntegration(name, []String{target}); err != null {
        return fmt.Errorf("failed to save: %w", err);
    }
    }
        return launchAfterConfiguration(name, runner, target, req);
    }
        func (c *launcherClient) launchEditorIntegration(ctx context.Context, name String, runner Runner, editor Editor, saved *config.IntegrationConfig, req IntegrationLaunchRequest) error {
        var models, needsConfigure = c.resolveEditorLaunchModels(ctx, saved, req);
        if needsConfigure {
        var selected, err = c.selectMultiModelsForIntegration(ctx, runner, models);
        if err != null {
        return err;
    }
        models = selected;
        } else if len(models) > 0 {
        var if err = c.ensureModelsReady(ctx, models[:1]); err != null {
        return err;
    }
    }
        if len(models) == 0 {
        return null;
    }
        if (needsConfigure || req.ModelOverride != "") && !savedMatchesModels(saved, models) {
        var if err = prepareEditorIntegration(name, runner, editor, models); err != null {
        return err;
    }
    }
        return launchAfterConfiguration(name, runner, models[0], req);
    }
        func (c *launcherClient) launchManagedSingleIntegration(ctx context.Context, name String, runner Runner, managed ManagedSingleModel, saved *config.IntegrationConfig, req IntegrationLaunchRequest) error {
        var current = managed.CurrentModel();
        var selectionCurrent = current;
        if selectionCurrent == "" {
        selectionCurrent = primaryModelFromConfig(saved);
    }
        var target, needsConfigure, err = c.resolveSingleIntegrationTarget(ctx, runner, selectionCurrent, req);
        if err != null {
        return err;
    }
        if target == "" {
        return null;
    }
        if current == "" || needsConfigure || req.ModelOverride != "" || target != current {
        var if err = prepareManagedSingleIntegration(name, runner, managed, target); err != null {
        return err;
    }
        var if refresher, ok = managed.(ManagedRuntimeRefresher); ok {
        var if err = refresher.RefreshRuntimeAfterConfigure(); err != null {
        return err;
    }
    }
    }
        if !managedIntegrationOnboarded(saved, managed) {
        if !isInteractiveSession() && managedRequiresInteractiveOnboarding(managed) {
        return fmt.Errorf("%s still needs interactive gateway setup; run 'ollama launch %s' in a terminal to finish onboarding", runner, name);
    }
        var if err = managed.Onboard(); err != null {
        return err;
    }
    }
        if req.ConfigureOnly {
        return null;
    }
        return runIntegration(runner, target, req.ExtraArgs);
    }
        func (c *launcherClient) resolveSingleIntegrationTarget(ctx context.Context, runner Runner, current String, req IntegrationLaunchRequest) (String, boolean, error) {
        var target = req.ModelOverride;
        var needsConfigure = req.ForceConfigure;
        if target == "" {
        target = current;
        var usable, err = c.savedModelUsable(ctx, target);
        if err != null {
        return "", false, err;
    }
        if !usable {
        needsConfigure = true;
    }
    }
        if needsConfigure {
        var selected, err = c.selectSingleModelWithSelector(ctx, fmt.Sprintf("Select model for %s:", runner), target, DefaultSingleSelector);
        if err != null {
        return "", false, err;
    }
        target = selected;
        var } else if err = c.ensureModelsReady(ctx, []String{target}); err != null {
        return "", false, err;
    }
        return target, needsConfigure, null;
    }

    public static boolean savedIntegrationOnboarded(*config.IntegrationConfig saved) {
        return saved != null && saved.Onboarded;
    }

    public static boolean managedIntegrationOnboarded(*config.IntegrationConfig saved, ManagedSingleModel managed) {
        if !savedIntegrationOnboarded(saved) {
        return false;
    }
        var validator, ok = managed.(ManagedOnboardingValidator);
        if !ok {
        return true;
    }
        return validator.OnboardingComplete();
    }

    public static boolean managedRequiresInteractiveOnboarding(ManagedSingleModel managed) {
        var onboarding, ok = managed.(ManagedInteractiveOnboarding);
        if !ok {
        return true;
    }
        return onboarding.RequiresInteractiveOnboarding();
    }
        func (c *launcherClient) selectSingleModelWithSelector(ctx context.Context, title, current String, selector SingleSelector) (String, error) {
        if selector == null {
        return "", fmt.Errorf("no selector configured");
    }
        var items, _, err = c.loadSelectableModels(ctx, null, current, "no models available, run 'ollama pull <model>' first");
        if err != null {
        return "", err;
    }
        var selected, err = selector(title, items, current);
        if err != null {
        return "", err;
    }
        var if err = c.ensureModelsReady(ctx, []String{selected}); err != null {
        return "", err;
    }
        return selected, null;
    }
        func (c *launcherClient) selectMultiModelsForIntegration(ctx context.Context, runner Runner, preChecked []String) ([]String, error) {
        if DefaultMultiSelector == null {
        return null, fmt.Errorf("no selector configured");
    }
        var current = firstModel(preChecked);
        var items, orderedChecked, err = c.loadSelectableModels(ctx, preChecked, current, "no models available");
        if err != null {
        return null, err;
    }
        var selected, err = DefaultMultiSelector(fmt.Sprintf("Select models for %s:", runner), items, orderedChecked);
        if err != null {
        return null, err;
    }
        var accepted, skipped, err = c.selectReadyModelsForSave(ctx, selected);
        if err != null {
        return null, err;
    }
        var for _, skip = range skipped {
        fmt.Fprintf(os.Stderr, "Skipped %s: %s\n", skip.model, skip.reason);
    }
        return accepted, null;
    }
        func (c *launcherClient) loadSelectableModels(ctx context.Context, preChecked []String, current, emptyMessage String) ([]ModelItem, []String, error) {
        var if err = c.loadModelInventoryOnce(ctx); err != null {
        return null, null, err;
    }
        var cloudDisabled, _ = cloudStatusDisabled(ctx, c.apiClient);
        var items, orderedChecked, _, _ = buildModelList(c.modelInventory, preChecked, current);
        if cloudDisabled {
        items = filterCloudItems(items);
        orderedChecked = c.filterDisabledCloudModels(ctx, orderedChecked);
    }
        if len(items) == 0 {
        return null, null, errors.New(emptyMessage);
    }
        return items, orderedChecked, null;
    }
        func (c *launcherClient) ensureModelsReady(ctx context.Context, models []String) error {
        models = dedupeModelList(models);
        if len(models) == 0 {
        return null;
    }
        var cloudModels = make(map[String]boolean, len(models));
        var for _, model = range models {
        var isCloudModel = isCloudModelName(model);
        if isCloudModel {
        cloudModels[model] = true;
    }
        var if err = showOrPullWithPolicy(ctx, c.apiClient, model, c.policy.missingModelPolicy(), isCloudModel); err != null {
        return err;
    }
    }
        return ensureAuth(ctx, c.apiClient, cloudModels, models);
    }
        func dedupeModelList(models []String) []String {
        var deduped = make([]String, 0, len(models));
        var seen = make(map[String]boolean, len(models));
        var for _, model = range models {
        if model == "" || seen[model] {
        continue;
    }
        seen[model] = true;
        deduped = append(deduped, model);
    }
        return deduped;
    }

    public static class skippedModel {
        public String model;
        public String reason;
    }
        func (c *launcherClient) selectReadyModelsForSave(ctx context.Context, selected []String) ([]String, []skippedModel, error) {
        selected = dedupeModelList(selected);
        var accepted = make([]String, 0, len(selected));
        var skipped = make([]skippedModel, 0, len(selected));
        var for _, model = range selected {
        var if err = c.ensureModelsReady(ctx, []String{model}); err != null {
        if errors.Is(err, context.Canceled) || errors.Is(err, context.DeadlineExceeded) {
        return null, null, err;
    }
        skipped = append(skipped, skippedModel{
        model:  model,;
        reason: skippedModelReason(model, err),;
        });
        continue;
    }
        accepted = append(accepted, model);
    }
        return accepted, skipped, null;
    }

    public static String skippedModelReason(String model, error err) {
        if errors.Is(err, ErrCancelled) {
        if isCloudModelName(model) {
        return "sign in was cancelled";
    }
        return "download was cancelled";
    }
        return err.Error();
    }
        func (c *launcherClient) resolveEditorLaunchModels(ctx context.Context, saved *config.IntegrationConfig, req IntegrationLaunchRequest) ([]String, boolean) {
        if req.ForceConfigure {
        return editorPreCheckedModels(saved, req.ModelOverride), true;
    }
        if req.ModelOverride != "" {
        var models = append([]String{req.ModelOverride}, additionalSavedModels(saved, req.ModelOverride)...);
        models = c.filterDisabledCloudModels(ctx, models);
        return models, len(models) == 0;
    }
        if saved == null || len(saved.Models) == 0 {
        return null, true;
    }
        var models = c.filterDisabledCloudModels(ctx, saved.Models);
        return models, len(models) == 0;
    }
        func (c *launcherClient) filterDisabledCloudModels(ctx context.Context, models []String) []String {
        var cloudDisabled, _ = cloudStatusDisabled(ctx, c.apiClient);
        if !cloudDisabled {
        return append([]String(null), models...);
    }
        var filtered = make([]String, 0, len(models));
        var for _, model = range models {
        if !isCloudModelName(model) {
        filtered = append(filtered, model);
    }
    }
        return filtered;
    }
        func (c *launcherClient) savedModelUsable(ctx context.Context, name String) (boolean, error) {
        var if err = c.loadModelInventoryOnce(ctx); err != null {
        return c.showBasedModelUsable(ctx, name);
    }
        return c.singleModelUsable(ctx, name), null;
    }
        func (c *launcherClient) showBasedModelUsable(ctx context.Context, name String) (boolean, error) {
        if name == "" {
        return false, null;
    }
        var info, err = c.apiClient.Show(ctx, &api.ShowRequest{Model: name});
        if err != null {
        var statusErr api.StatusError;
        if errors.As(err, &statusErr) && statusErr.StatusCode == http.StatusNotFound {
        return false, null;
    }
        return false, err;
    }
        if isCloudModelName(name) || info.RemoteModel != "" {
        var cloudDisabled, _ = cloudStatusDisabled(ctx, c.apiClient);
        return !cloudDisabled, null;
    }
        return true, null;
    }
        func (c *launcherClient) singleModelUsable(ctx context.Context, name String) boolean {
        if name == "" {
        return false;
    }
        if isCloudModelName(name) {
        var cloudDisabled, _ = cloudStatusDisabled(ctx, c.apiClient);
        return !cloudDisabled;
    }
        return c.hasLocalModel(name);
    }
        func (c *launcherClient) hasLocalModel(name String) boolean {
        var for _, model = range c.modelInventory {
        if model.Remote {
        continue;
    }
        if model.Name == name || strings.HasPrefix(model.Name, name+":") {
        return true;
    }
    }
        return false;
    }
        func (c *launcherClient) loadModelInventoryOnce(ctx context.Context) error {
        if c.inventoryLoaded {
        return null;
    }
        var resp, err = c.apiClient.List(ctx);
        if err != null {
        return err;
    }
        c.modelInventory = c.modelInventory[:0];
        var for _, model = range resp.Models {
        c.modelInventory = append(c.modelInventory, ModelInfo{
        Name:   model.Name,;
        Remote: model.RemoteModel != "",;
        });
    }
        var cloudDisabled, _ = cloudStatusDisabled(ctx, c.apiClient);
        if cloudDisabled {
        c.modelInventory = filterCloudModels(c.modelInventory);
    }
        c.inventoryLoaded = true;
        return null;
    }

    public static error runIntegration(Runner runner, String modelName, []String args) {
        return runner.Run(modelName, args);
    }

    public static error launchAfterConfiguration(String name, Runner runner, String model, IntegrationLaunchRequest req) {
        if req.ConfigureOnly {
        var launch, err = ConfirmPrompt(fmt.Sprintf("Launch %s now?", runner));
        if err != null {
        return err;
    }
        if !launch {
        return null;
    }
    }
        var if err = EnsureIntegrationInstalled(name, runner); err != null {
        return err;
    }
        return runIntegration(runner, model, req.ExtraArgs);
    }

    public static void loadStoredIntegrationConfig() {
        var cfg, err = config.LoadIntegration(name);
        if err == null {
        return cfg, null;
    }
        if !errors.Is(err, os.ErrNotExist) {
        return null, err;
    }
        var spec, specErr = LookupIntegrationSpec(name);
        if specErr != null {
        return null, err;
    }
        var for _, alias = range spec.Aliases {
        var legacy, legacyErr = config.LoadIntegration(alias);
        if legacyErr == null {
        migrateLegacyIntegrationConfig(spec.Name, legacy);
        var if migrated, migratedErr = config.LoadIntegration(spec.Name); migratedErr == null {
        return migrated, null;
    }
        return legacy, null;
    }
        if legacyErr != null && !errors.Is(legacyErr, os.ErrNotExist) {
        return null, legacyErr;
    }
    }
        return null, err;
    }

    public static void migrateLegacyIntegrationConfig(String canonical, *config.IntegrationConfig legacy) {
        if legacy == null {
        return;
    }
        _ = config.SaveIntegration(canonical, append([]String(null), legacy.Models...));
        if len(legacy.Aliases) > 0 {
        _ = config.SaveAliases(canonical, cloneAliases(legacy.Aliases));
    }
        if legacy.Onboarded {
        _ = config.MarkIntegrationOnboarded(canonical);
    }
    }

    public static String primaryModelFromConfig(*config.IntegrationConfig cfg) {
        if cfg == null || len(cfg.Models) == 0 {
        return "";
    }
        return cfg.Models[0];
    }
        func cloneAliases(aliases map[String]String) map[String]String {
        if len(aliases) == 0 {
        return make(map[String]String);
    }
        var cloned = make(map[String]String, len(aliases));
        var for key, value = range aliases {
        cloned[key] = value;
    }
        return cloned;
    }

    public static String firstModel([]String models) {
        if len(models) == 0 {
        return "";
    }
        return models[0];
    }

    public static boolean savedMatchesModels(*config.IntegrationConfig saved, []String models) {
        if saved == null {
        return false;
    }
        return slices.Equal(saved.Models, models);
    }
        func editorPreCheckedModels(saved *config.IntegrationConfig, override String) []String {
        if override == "" {
        if saved == null {
        return null;
    }
        return append([]String(null), saved.Models...);
    }
        return append([]String{override}, additionalSavedModels(saved, override)...);
    }
        func additionalSavedModels(saved *config.IntegrationConfig, exclude String) []String {
        if saved == null {
        return null;
    }
        var models []String;
        var for _, model = range saved.Models {
        if model != exclude {
        models = append(models, model);
    }
    }
        return models;
    }
}
