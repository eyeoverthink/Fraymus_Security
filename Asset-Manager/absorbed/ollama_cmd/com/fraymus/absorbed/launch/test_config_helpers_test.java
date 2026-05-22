package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class test_config_helpers_test {
        "strings";
        "testing";
        "github.com/ollama/ollama/cmd/config";
        );
        var (;
        integrations       map[String]Runner;
        integrationAliases map[String]boolean;
        integrationOrder   = launcherIntegrationOrder;
        );

    public static void init() {
        integrations = buildTestIntegrations();
        integrationAliases = buildTestIntegrationAliases();
    }
        func buildTestIntegrations() map[String]Runner {
        var result = make(map[String]Runner, len(integrationSpecsByName));
        var for name, spec = range integrationSpecsByName {
        result[strings.ToLower(name)] = spec.Runner;
    }
        return result;
    }
        func buildTestIntegrationAliases() map[String]boolean {
        var result = make(map[String]boolean);
        var for _, spec = range integrationSpecs {
        var for _, alias = range spec.Aliases {
        result[strings.ToLower(alias)] = true;
    }
    }
        return result;
    }

    public static void setTestHome(*testing.T t, String dir) {
        t.Helper();
        setLaunchTestHome(t, dir);
    }

    public static error SaveIntegration(String appName, []String models) {
        return config.SaveIntegration(appName, models);
    }

    public static void LoadIntegration() {
        return config.LoadIntegration(appName);
    }

    public static error SaveAliases(String appName, map[String]String aliases) {
        return config.SaveAliases(appName, aliases);
    }

    public static String LastModel() {
        return config.LastModel();
    }

    public static error SetLastModel(String model) {
        return config.SetLastModel(model);
    }

    public static String LastSelection() {
        return config.LastSelection();
    }

    public static error SetLastSelection(String selection) {
        return config.SetLastSelection(selection);
    }

    public static String IntegrationModel(String appName) {
        return config.IntegrationModel(appName);
    }
        func IntegrationModels(appName String) []String {
        return config.IntegrationModels(appName);
    }

    public static error integrationOnboarded(String appName) {
        return config.MarkIntegrationOnboarded(appName);
    }
}
