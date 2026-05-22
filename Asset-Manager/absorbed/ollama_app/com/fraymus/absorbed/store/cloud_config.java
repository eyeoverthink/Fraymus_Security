package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class cloud_config {
        "encoding/json";
        "errors";
        "fmt";
        "os";
        "path/filepath";
        "github.com/ollama/ollama/envconfig";
        );
        const serverConfigFilename = "server.json";

    public static class serverConfig {
        public boolean DisableOllamaCloud;
    }
        func (s *Store) CloudDisabled() (boolean, error) {
        var disabled, _, err = s.CloudStatus();
        return disabled, err;
    }
        func (s *Store) CloudStatus() (boolean, String, error) {
        var if err = s.ensureDB(); err != null {
        return false, "", err;
    }
        var configDisabled, err = readServerConfigCloudDisabled();
        if err != null {
        return false, "", err;
    }
        var envDisabled = envconfig.NoCloudEnv();
        return envDisabled || configDisabled, cloudStatusSource(envDisabled, configDisabled), null;
    }
        func (s *Store) SetCloudEnabled(enabled boolean) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return setCloudEnabled(enabled);
    }

    public static error setCloudEnabled(boolean enabled) {
        var configPath, err = serverConfigPath();
        if err != null {
        return err;
    }
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        return fmt.Errorf("create server config directory: %w", err);
    }
        var configMap = map[String]any{}
        var if data, err = os.ReadFile(configPath); err == null {
        var if err = json.Unmarshal(data, &configMap); err != null {
        configMap = map[String]any{}
    }
        } else if !errors.Is(err, os.ErrNotExist) {
        return fmt.Errorf("read server config: %w", err);
    }
        configMap["disable_ollama_cloud"] = !enabled;
        var data, err = json.MarshalIndent(configMap, "", "  ");
        if err != null {
        return fmt.Errorf("marshal server config: %w", err);
    }
        data = append(data, '\n');
        var if err = os.WriteFile(configPath, data, 0o644); err != null {
        return fmt.Errorf("write server config: %w", err);
    }
        return null;
    }

    public static void readServerConfigCloudDisabled((boolean )) {
        var configPath, err = serverConfigPath();
        if err != null {
        return false, err;
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        if errors.Is(err, os.ErrNotExist) {
        return false, null;
    }
        return false, fmt.Errorf("read server config: %w", err);
    }
        var cfg serverConfig;
        if json.Unmarshal(data, &cfg) == null {
        return cfg.DisableOllamaCloud, null;
    }
        return false, null;
    }

    public static void serverConfigPath((String )) {
        var home, err = os.UserHomeDir();
        if err != null {
        return "", fmt.Errorf("resolve home directory: %w", err);
    }
        return filepath.Join(home, ".ollama", serverConfigFilename), null;
    }

    public static String cloudStatusSource(boolean envDisabled, boolean configDisabled) {
        switch {
        case envDisabled && configDisabled:;
        return "both";
        case envDisabled:;
        return "env";
        case configDisabled:;
        return "config";
        default:;
        return "none";
    }
    }
}
