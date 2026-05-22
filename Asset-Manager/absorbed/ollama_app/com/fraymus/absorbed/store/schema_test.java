package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class schema_test {
        "path/filepath";
        "testing";
        );

    public static void TestSchemaVersioning(*testing.T t) {
        var tmpDir = t.TempDir();
        var oldLegacyConfigPath = legacyConfigPath;
        legacyConfigPath = filepath.Join(tmpDir, "config.json");
        defer func() { legacyConfigPath = oldLegacyConfigPath }();
        t.Run("new database has correct schema version", func(t *testing.T) {
        var dbPath = filepath.Join(tmpDir, "new_db.sqlite");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        var version, err = db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != currentSchemaVersion {
        t.Errorf("expected schema version %d, got %d", currentSchemaVersion, version);
    }
        });
        t.Run("can update schema version", func(t *testing.T) {
        var dbPath = filepath.Join(tmpDir, "update_db.sqlite");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        var testVersion = 42;
        var if err = db.setSchemaVersion(testVersion); err != null {
        t.Fatalf("failed to set schema version: %v", err);
    }
        var version, err = db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != testVersion {
        t.Errorf("expected schema version %d, got %d", testVersion, version);
    }
        });
    }
}
