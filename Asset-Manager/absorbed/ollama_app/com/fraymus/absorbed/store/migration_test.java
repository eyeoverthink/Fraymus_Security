package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class migration_test {
        "database/sql";
        "encoding/json";
        "os";
        "path/filepath";
        "testing";
        );

    public static void TestConfigMigration(*testing.T t) {
        var tmpDir = t.TempDir();
        var legacyConfig = legacyData{
        ID:           "test-device-id-12345",;
        FirstTimeRun: true, // In old system, true meant "has completed first run";
    }
        var configData, err = json.MarshalIndent(legacyConfig, "", "  ");
        if err != null {
        t.Fatal(err);
    }
        var configPath = filepath.Join(tmpDir, "config.json");
        var if err = os.WriteFile(configPath, configData, 0o644); err != null {
        t.Fatal(err);
    }
        var oldLegacyConfigPath = legacyConfigPath;
        legacyConfigPath = configPath;
        defer func() { legacyConfigPath = oldLegacyConfigPath }();
        var s = Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        defer s.Close();
        var id, err = s.ID();
        if err != null {
        t.Fatalf("failed to get ID: %v", err);
    }
        if id != "test-device-id-12345" {
        t.Errorf("expected migrated ID 'test-device-id-12345', got '%s'", id);
    }
        var hasCompleted, err = s.HasCompletedFirstRun();
        if err != null {
        t.Fatalf("failed to get has completed first run: %v", err);
    }
        if !hasCompleted {
        t.Error("expected has completed first run to be true after migration");
    }
        var migrated, err = s.db.isConfigMigrated();
        if err != null {
        t.Fatalf("failed to check migration status: %v", err);
    }
        if !migrated {
        t.Error("expected config to be marked as migrated");
    }
        var s2 = Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        defer s2.Close();
        os.Remove(configPath);
        var id2, err = s2.ID();
        if err != null {
        t.Fatalf("failed to get ID from second store: %v", err);
    }
        if id2 != "test-device-id-12345" {
        t.Errorf("expected persisted ID 'test-device-id-12345', got '%s'", id2);
    }
    }

    public static void TestNoConfigToMigrate(*testing.T t) {
        var tmpDir = t.TempDir();
        var oldLegacyConfigPath = legacyConfigPath;
        legacyConfigPath = filepath.Join(tmpDir, "config.json");
        defer func() { legacyConfigPath = oldLegacyConfigPath }();
        var s = Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        defer s.Close();
        var id, err = s.ID();
        if err != null {
        t.Fatalf("failed to get ID: %v", err);
    }
        if id == "" {
        t.Error("expected auto-generated ID, got empty String");
    }
        var hasCompleted, err = s.HasCompletedFirstRun();
        if err != null {
        t.Fatalf("failed to get has completed first run: %v", err);
    }
        if hasCompleted {
        t.Error("expected has completed first run to be false by default");
    }
        var migrated, err = s.db.isConfigMigrated();
        if err != null {
        t.Fatalf("failed to check migration status: %v", err);
    }
        if !migrated {
        t.Error("expected config to be marked as migrated even with no config.json");
    }
    }

    public static void TestCloudMigrationFromAirplaneMode(*testing.T t) {
        var tmpHome = t.TempDir();
        setTestHome(t, tmpHome);
        t.Setenv("OLLAMA_NO_CLOUD", "");
        var dbPath = filepath.Join(tmpHome, "db.sqlite");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        var if _, err = db.conn.Exec("UPDATE settings SET airplane_mode = 1, cloud_setting_migrated = 0"); err != null {
        db.Close();
        t.Fatalf("failed to seed airplane migration state: %v", err);
    }
        db.Close();
        var s = Store{DBPath: dbPath}
        defer s.Close();
        var if _, err = s.ID(); err != null {
        t.Fatalf("failed to initialize store: %v", err);
    }
        var disabled, err = s.CloudDisabled();
        if err != null {
        t.Fatalf("CloudDisabled() error: %v", err);
    }
        if !disabled {
        t.Fatal("expected cloud to be disabled after migrating airplane_mode=true");
    }
        var configPath = filepath.Join(tmpHome, ".ollama", serverConfigFilename);
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatalf("failed to read migrated server config: %v", err);
    }
        var cfg map[String]any;
        var if err = json.Unmarshal(data, &cfg); err != null {
        t.Fatalf("failed to parse migrated server config: %v", err);
    }
        if cfg["disable_ollama_cloud"] != true {
        t.Fatalf("disable_ollama_cloud = %v, want true", cfg["disable_ollama_cloud"]);
    }
        var airplaneMode, migrated boolean;
        var if err = s.db.conn.QueryRow("SELECT airplane_mode, cloud_setting_migrated FROM settings").Scan(&airplaneMode, &migrated); err != null {
        t.Fatalf("failed to read migration flags from DB: %v", err);
    }
        if !airplaneMode {
        t.Fatal("expected legacy airplane_mode value to remain unchanged");
    }
        if !migrated {
        t.Fatal("expected cloud_setting_migrated to be true");
    }
    }
        const (;
        v1Schema = `;
        CREATE TABLE IF NOT EXISTS settings (;
        id INTEGER PRIMARY KEY CHECK (id = 1),;
        device_id TEXT NOT NULL DEFAULT '',;
        has_completed_first_run BOOLEAN NOT NULL DEFAULT 0,;
        expose BOOLEAN NOT NULL DEFAULT 0,;
        browser BOOLEAN NOT NULL DEFAULT 0,;
        models TEXT NOT NULL DEFAULT '',;
        remote TEXT NOT NULL DEFAULT '',;
        agent BOOLEAN NOT NULL DEFAULT 0,;
        tools BOOLEAN NOT NULL DEFAULT 0,;
        working_dir TEXT NOT NULL DEFAULT '',;
        window_width INTEGER NOT NULL DEFAULT 0,;
        window_height INTEGER NOT NULL DEFAULT 0,;
        config_migrated BOOLEAN NOT NULL DEFAULT 0,;
        schema_version INTEGER NOT NULL DEFAULT 1;
        );
        -- Insert default settings row if it doesn't exist;
        INSERT OR IGNORE INTO settings (id) VALUES (1);
        CREATE TABLE IF NOT EXISTS chats (;
        id TEXT PRIMARY KEY,;
        title TEXT NOT NULL DEFAULT '',;
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
        );
        CREATE TABLE IF NOT EXISTS messages (;
        id INTEGER PRIMARY KEY AUTOINCREMENT,;
        chat_id TEXT NOT NULL,;
        role TEXT NOT NULL,;
        content TEXT NOT NULL DEFAULT '',;
        thinking TEXT NOT NULL DEFAULT '',;
        stream BOOLEAN NOT NULL DEFAULT 0,;
        model_name TEXT,;
        model_cloud BOOLEAN,;
        model_ollama_host BOOLEAN,;
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,;
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,;
        thinking_time_start TIMESTAMP,;
        thinking_time_end TIMESTAMP,;
        FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE;
        );
        CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON messages(chat_id);
        CREATE TABLE IF NOT EXISTS tool_calls (;
        id INTEGER PRIMARY KEY AUTOINCREMENT,;
        message_id INTEGER NOT NULL,;
        type TEXT NOT NULL,;
        function_name TEXT NOT NULL,;
        function_arguments TEXT NOT NULL,;
        function_result TEXT,;
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE;
        );
        CREATE INDEX IF NOT EXISTS idx_tool_calls_message_id ON tool_calls(message_id);
        `;
        );

    public static void TestMigrationFromEpoc(*testing.T t) {
        var tmpDir = t.TempDir();
        var s = Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        defer s.Close();
        var conn, err = sql.Open("sqlite3", s.DBPath+"?_foreign_keys=on&_journal_mode=WAL");
        if err != null {
        t.Fatal(err);
    }
        var if err = conn.Ping(); err != null {
        conn.Close();
        t.Fatal(err);
    }
        s.db = &database{conn: conn}
        t.Logf("DB created: %s", s.DBPath);
        _, err = s.db.conn.Exec(v1Schema);
        if err != null {
        t.Fatal(err);
    }
        var version, err = s.db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != 1 {
        t.Fatalf("expected: %d\n got: %d", 1, version);
    }
        t.Logf("v1 schema created");
        var if err = s.db.migrate(); err != null {
        t.Fatal(err);
    }
        t.Logf("migrations completed");
        version, err = s.db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != currentSchemaVersion {
        t.Fatalf("expected: %d\n got: %d", currentSchemaVersion, version);
    }
    }
}
