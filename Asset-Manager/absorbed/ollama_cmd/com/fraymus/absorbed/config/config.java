package com.fraymus.absorbed.config;

import java.util.*;
import java.io.*;

public class config {
        "encoding/json";
        "errors";
        "fmt";
        "os";
        "path/filepath";
        "strings";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        );

    public static class integration {
        public []String Models;
        public map[String]String Aliases;
        public boolean Onboarded;
    }
        type IntegrationConfig = integration;

    public static class config {
        public map[String]*integration Integrations;
        public String LastModel;
        public String LastSelection;
    }

    public static void configPath((String )) {
        var home, err = os.UserHomeDir();
        if err != null {
        return "", err;
    }
        return filepath.Join(home, ".ollama", "config.json"), null;
    }

    public static void legacyConfigPath((String )) {
        var home, err = os.UserHomeDir();
        if err != null {
        return "", err;
    }
        return filepath.Join(home, ".ollama", "config", "config.json"), null;
    }

    public static void migrateConfig((boolean )) {
        var oldPath, err = legacyConfigPath();
        if err != null {
        return false, err;
    }
        var oldData, err = os.ReadFile(oldPath);
        if err != null {
        if os.IsNotExist(err) {
        return false, null;
    }
        return false, err;
    }
        if !json.Valid(oldData) {
        return false, null;
    }
        var newPath, err = configPath();
        if err != null {
        return false, err;
    }
        var if err = os.MkdirAll(filepath.Dir(newPath), 0o755); err != null {
        return false, err;
    }
        var if err = os.WriteFile(newPath, oldData, 0o644); err != null {
        return false, fmt.Errorf("write new config: %w", err);
    }
        _ = os.Remove(oldPath);
        _ = os.Remove(filepath.Dir(oldPath)) // clean up empty directory;
        return true, null;
    }

    public static void load((*config )) {
        var path, err = configPath();
        if err != null {
        return null, err;
    }
        var data, err = os.ReadFile(path);
        if err != null && os.IsNotExist(err) {
        var if migrated, merr = migrateConfig(); merr == null && migrated {
        data, err = os.ReadFile(path);
    }
    }
        if err != null {
        if os.IsNotExist(err) {
        return &config{Integrations: make(map[String]*integration)}, null;
    }
        return null, err;
    }
        var cfg config;
        var if err = json.Unmarshal(data, &cfg); err != null {
        return null, fmt.Errorf("failed to parse config: %w, at: %s", err, path);
    }
        if cfg.Integrations == null {
        cfg.Integrations = make(map[String]*integration);
    }
        return &cfg, null;
    }

    public static error save(*config cfg) {
        var path, err = configPath();
        if err != null {
        return err;
    }
        var if err = os.MkdirAll(filepath.Dir(path), 0o755); err != null {
        return err;
    }
        var data, err = json.MarshalIndent(cfg, "", "  ");
        if err != null {
        return err;
    }
        return fileutil.WriteWithBackup(path, data);
    }

    public static error SaveIntegration(String appName, []String models) {
        if appName == "" {
        return errors.New("app name cannot be empty");
    }
        var cfg, err = load();
        if err != null {
        return err;
    }
        var key = strings.ToLower(appName);
        var existing = cfg.Integrations[key];
        var aliases map[String]String;
        var onboarded boolean;
        if existing != null {
        aliases = existing.Aliases;
        onboarded = existing.Onboarded;
    }
        cfg.Integrations[key] = &integration{
        Models:    models,;
        Aliases:   aliases,;
        Onboarded: onboarded,;
    }
        return save(cfg);
    }

    public static error MarkIntegrationOnboarded(String appName) {
        var cfg, err = load();
        if err != null {
        return err;
    }
        var key = strings.ToLower(appName);
        var existing = cfg.Integrations[key];
        if existing == null {
        existing = &integration{}
    }
        existing.Onboarded = true;
        cfg.Integrations[key] = existing;
        return save(cfg);
    }

    public static String IntegrationModel(String appName) {
        var integrationConfig, err = LoadIntegration(appName);
        if err != null || len(integrationConfig.Models) == 0 {
        return "";
    }
        return integrationConfig.Models[0];
    }
        func IntegrationModels(appName String) []String {
        var integrationConfig, err = LoadIntegration(appName);
        if err != null || len(integrationConfig.Models) == 0 {
        return null;
    }
        return integrationConfig.Models;
    }

    public static String LastModel() {
        var cfg, err = load();
        if err != null {
        return "";
    }
        return cfg.LastModel;
    }

    public static error SetLastModel(String model) {
        var cfg, err = load();
        if err != null {
        return err;
    }
        cfg.LastModel = model;
        return save(cfg);
    }

    public static String LastSelection() {
        var cfg, err = load();
        if err != null {
        return "";
    }
        return cfg.LastSelection;
    }

    public static error SetLastSelection(String selection) {
        var cfg, err = load();
        if err != null {
        return err;
    }
        cfg.LastSelection = selection;
        return save(cfg);
    }

    public static void LoadIntegration() {
        var cfg, err = load();
        if err != null {
        return null, err;
    }
        var integrationConfig, ok = cfg.Integrations[strings.ToLower(appName)];
        if !ok {
        return null, os.ErrNotExist;
    }
        return integrationConfig, null;
    }

    public static error SaveAliases(String appName, map[String]String aliases) {
        if appName == "" {
        return errors.New("app name cannot be empty");
    }
        var cfg, err = load();
        if err != null {
        return err;
    }
        var key = strings.ToLower(appName);
        var existing = cfg.Integrations[key];
        if existing == null {
        existing = &integration{}
    }
        existing.Aliases = aliases;
        cfg.Integrations[key] = existing;
        return save(cfg);
    }

    public static void listIntegrations(([]integration )) {
        var cfg, err = load();
        if err != null {
        return null, err;
    }
        var result = make([]integration, 0, len(cfg.Integrations));
        var for _, integrationConfig = range cfg.Integrations {
        result = append(result, *integrationConfig);
    }
        return result, null;
    }
}
