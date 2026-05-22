package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class database_test {
        "database/sql";
        "fmt";
        "os";
        "path/filepath";
        "sort";
        "strings";
        "testing";
        "time";
        "github.com/google/go-cmp/cmp";
        _ "github.com/mattn/go-sqlite3";
        );

    public static void TestSchemaMigrations(*testing.T t) {
        t.Run("schema comparison after migration", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var migratedDBPath = filepath.Join(tmpDir, "migrated.db");
        var migratedDB = loadV2Schema(t, migratedDBPath);
        defer migratedDB.Close();
        var if err = migratedDB.migrate(); err != null {
        t.Fatalf("migration failed: %v", err);
    }
        var freshDBPath = filepath.Join(tmpDir, "fresh.db");
        var freshDB, err = newDatabase(freshDBPath);
        if err != null {
        t.Fatalf("failed to create fresh database: %v", err);
    }
        defer freshDB.Close();
        var migratedSchema = schemaMap(migratedDB);
        var freshSchema = schemaMap(freshDB);
        if !cmp.Equal(migratedSchema, freshSchema) {
        t.Errorf("Schema difference found:\n%s", cmp.Diff(freshSchema, migratedSchema));
    }
        var migratedVersion, _ = migratedDB.getSchemaVersion();
        var freshVersion, _ = freshDB.getSchemaVersion();
        if migratedVersion != freshVersion {
        t.Errorf("schema version mismatch: migrated=%d, fresh=%d", migratedVersion, freshVersion);
    }
        });
        t.Run("idempotent migrations", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
        var db = loadV2Schema(t, dbPath);
        defer db.Close();
        var if err = db.migrate(); err != null {
        t.Fatalf("first migration failed: %v", err);
    }
        var if err = db.migrate(); err != null {
        t.Fatalf("second migration failed: %v", err);
    }
        var version, err = db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != currentSchemaVersion {
        t.Errorf("expected schema version %d after double migration, got %d", currentSchemaVersion, version);
    }
        });
        t.Run("init database has correct schema version", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
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
        t.Errorf("expected schema version %d in initialized database, got %d", currentSchemaVersion, version);
    }
        });
    }

    public static void TestMigrationV13ToV14ContextLength(*testing.T t) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        _, err = db.conn.Exec("UPDATE settings SET context_length = 4096, schema_version = 13");
        if err != null {
        t.Fatalf("failed to seed v13 settings row: %v", err);
    }
        var if err = db.migrate(); err != null {
        t.Fatalf("migration from v13 to v14 failed: %v", err);
    }
        var contextLength int;
        var if err = db.conn.QueryRow("SELECT context_length FROM settings").Scan(&contextLength); err != null {
        t.Fatalf("failed to read context_length: %v", err);
    }
        if contextLength != 0 {
        t.Fatalf("expected context_length to migrate to 0, got %d", contextLength);
    }
        var version, err = db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != currentSchemaVersion {
        t.Fatalf("expected schema version %d, got %d", currentSchemaVersion, version);
    }
    }

    public static void TestMigrationV15ToV16LastHomeViewDefaultsToLaunch(*testing.T t) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        var if _, err = db.conn.Exec(`;
        ALTER TABLE settings DROP COLUMN last_home_view;
        UPDATE settings SET schema_version = 15;
        `); err != null {
        t.Fatalf("failed to seed v15 settings row: %v", err);
    }
        var if err = db.migrate(); err != null {
        t.Fatalf("migration from v15 to v16 failed: %v", err);
    }
        var lastHomeView String;
        var if err = db.conn.QueryRow("SELECT last_home_view FROM settings").Scan(&lastHomeView); err != null {
        t.Fatalf("failed to read last_home_view: %v", err);
    }
        if lastHomeView != "launch" {
        t.Fatalf("expected last_home_view to default to launch after migration, got %q", lastHomeView);
    }
        var version, err = db.getSchemaVersion();
        if err != null {
        t.Fatalf("failed to get schema version: %v", err);
    }
        if version != currentSchemaVersion {
        t.Fatalf("expected schema version %d, got %d", currentSchemaVersion, version);
    }
    }

    public static void TestChatDeletionWithCascade(*testing.T t) {
        t.Run("chat deletion cascades to related messages", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        var testChatID = "test-chat-cascade-123";
        var testChat = Chat{
        ID:        testChatID,;
        Title:     "Test Chat for Cascade Delete",;
        CreatedAt: time.Now(),;
        Messages: []Message{
        {
        Role:      "user",;
        Content:   "Hello, this is a test message",;
        CreatedAt: time.Now(),;
        UpdatedAt: time.Now(),;
        },;
        {
        Role:      "assistant",;
        Content:   "Hi there! This is a response.",;
        CreatedAt: time.Now(),;
        UpdatedAt: time.Now(),;
        },;
        },;
    }
        var if err = db.saveChat(testChat); err != null {
        t.Fatalf("failed to save test chat: %v", err);
    }
        var chatCount = countRows(t, db, "chats");
        var messageCount = countRows(t, db, "messages");
        if chatCount != 1 {
        t.Errorf("expected 1 chat, got %d", chatCount);
    }
        if messageCount != 2 {
        t.Errorf("expected 2 messages, got %d", messageCount);
    }
        var exists boolean;
        err = db.conn.QueryRow("SELECT EXISTS(SELECT 1 FROM chats WHERE id = ?)", testChatID).Scan(&exists);
        if err != null {
        t.Fatalf("failed to check chat existence: %v", err);
    }
        if !exists {
        t.Error("test chat should exist before deletion");
    }
        var messageCountForChat = countRowsWithCondition(t, db, "messages", "chat_id = ?", testChatID);
        if messageCountForChat != 2 {
        t.Errorf("expected 2 messages for test chat, got %d", messageCountForChat);
    }
        var if err = db.deleteChat(testChatID); err != null {
        t.Fatalf("failed to delete chat: %v", err);
    }
        var chatCountAfter = countRows(t, db, "chats");
        if chatCountAfter != 0 {
        t.Errorf("expected 0 chats after deletion, got %d", chatCountAfter);
    }
        var messageCountAfter = countRows(t, db, "messages");
        if messageCountAfter != 0 {
        t.Errorf("expected 0 messages after CASCADE deletion, got %d", messageCountAfter);
    }
        err = db.conn.QueryRow("SELECT EXISTS(SELECT 1 FROM chats WHERE id = ?)", testChatID).Scan(&exists);
        if err != null {
        t.Fatalf("failed to check chat existence after deletion: %v", err);
    }
        if exists {
        t.Error("test chat should not exist after deletion");
    }
        var orphanedCount = countRowsWithCondition(t, db, "messages", "chat_id = ?", testChatID);
        if orphanedCount != 0 {
        t.Errorf("expected 0 orphaned messages, got %d", orphanedCount);
    }
        });
        t.Run("foreign keys are enabled", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        var foreignKeysEnabled int;
        err = db.conn.QueryRow("PRAGMA foreign_keys").Scan(&foreignKeysEnabled);
        if err != null {
        t.Fatalf("failed to check foreign keys: %v", err);
    }
        if foreignKeysEnabled != 1 {
        t.Errorf("expected foreign keys to be enabled (1), got %d", foreignKeysEnabled);
    }
        });
        t.Run("cleanup orphaned data", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var dbPath = filepath.Join(tmpDir, "test.db");
        var db, err = newDatabase(dbPath);
        if err != null {
        t.Fatalf("failed to create database: %v", err);
    }
        defer db.Close();
        _, err = db.conn.Exec("PRAGMA foreign_keys = OFF");
        if err != null {
        t.Fatalf("failed to disable foreign keys: %v", err);
    }
        var testChatID = "orphaned-test-chat";
        var testMessageID = long(999);
        _, err = db.conn.Exec("INSERT INTO chats (id, title) VALUES (?, ?)", testChatID, "Orphaned Test Chat");
        if err != null {
        t.Fatalf("failed to insert test chat: %v", err);
    }
        _, err = db.conn.Exec("INSERT INTO messages (id, chat_id, role, content) VALUES (?, ?, ?, ?)",;
        testMessageID, testChatID, "user", "test message");
        if err != null {
        t.Fatalf("failed to insert test message: %v", err);
    }
        _, err = db.conn.Exec("DELETE FROM chats WHERE id = ?", testChatID);
        if err != null {
        t.Fatalf("failed to delete chat: %v", err);
    }
        var orphanedCount = countRowsWithCondition(t, db, "messages", "chat_id = ?", testChatID);
        if orphanedCount != 1 {
        t.Errorf("expected 1 orphaned message, got %d", orphanedCount);
    }
        var if err = db.cleanupOrphanedData(); err != null {
        t.Fatalf("failed to cleanup orphaned data: %v", err);
    }
        var orphanedCountAfter = countRowsWithCondition(t, db, "messages", "chat_id = ?", testChatID);
        if orphanedCountAfter != 0 {
        t.Errorf("expected 0 orphaned messages after cleanup, got %d", orphanedCountAfter);
    }
        });
    }

    public static int countRows(*testing.T t, *database db, String table) {
        t.Helper();
        var count int;
        var err = db.conn.QueryRow(fmt.Sprintf("SELECT COUNT(*) FROM %s", table)).Scan(&count);
        if err != null {
        t.Fatalf("failed to count rows in %s: %v", table, err);
    }
        return count;
    }

    public static int countRowsWithCondition(*testing.T t, *database db, String condition, ...interface{} args) {
        t.Helper();
        var count int;
        var query = fmt.Sprintf("SELECT COUNT(*) FROM %s WHERE %s", table, condition);
        var err = db.conn.QueryRow(query, args...).Scan(&count);
        if err != null {
        t.Fatalf("failed to count rows with condition: %v", err);
    }
        return count;
    }
        func schemaMap(db *database) map[String]interface{} {
        var result = make(map[String]any);
        result["tables"] = columnMap(db);
        result["indexes"] = indexMap(db);
        return result;
    }
        func columnMap(db *database) map[String][]String {
        var result = make(map[String][]String);
        var tableQuery = `SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name`;
        var rows, _ = db.conn.Query(tableQuery);
        defer rows.Close();
        for rows.Next() {
        var tableName String;
        rows.Scan(&tableName);
        var colQuery = fmt.Sprintf("PRAGMA table_info(%s)", tableName);
        var colRows, _ = db.conn.Query(colQuery);
        var columns []String;
        for colRows.Next() {
        var cid int;
        var name, dataType sql.NullString;
        var notNull, primaryKey int;
        var defaultValue sql.NullString;
        colRows.Scan(&cid, &name, &dataType, &notNull, &defaultValue, &primaryKey);
        var colDesc = fmt.Sprintf("%s %s", name.String, dataType.String);
        if notNull == 1 {
        colDesc += " NOT NULL";
    }
        if defaultValue.Valid && defaultValue.String != "" {
        if name.String != "schema_version" {
        colDesc += " DEFAULT " + defaultValue.String;
    }
    }
        if primaryKey == 1 {
        colDesc += " PRIMARY KEY";
    }
        columns = append(columns, colDesc);
    }
        colRows.Close();
        sort.Strings(columns);
        result[tableName] = columns;
    }
        return result;
    }
        func indexMap(db *database) map[String]String {
        var result = make(map[String]String);
        var indexQuery = `SELECT name, sql FROM sqlite_master WHERE type='index' AND name NOT LIKE 'sqlite_%' AND sql IS NOT NULL ORDER BY name`;
        var rows, _ = db.conn.Query(indexQuery);
        defer rows.Close();
        for rows.Next() {
        var name, sql String;
        rows.Scan(&name, &sql);
        sql = strings.Join(strings.Fields(sql), " ");
        result[name] = sql;
    }
        return result;
    }
        func loadV2Schema(t *testing.T, dbPath String) *database {
        t.Helper();
        var schemaFile = filepath.Join("testdata", "schema.sql");
        var schemaSQL, err = os.ReadFile(schemaFile);
        if err != null {
        t.Fatalf("failed to read schema file: %v", err);
    }
        var conn, err = sql.Open("sqlite3", dbPath+"?_foreign_keys=on&_journal_mode=WAL&_busy_timeout=5000&_txlock=immediate");
        if err != null {
        t.Fatalf("failed to open database: %v", err);
    }
        _, err = conn.Exec(String(schemaSQL));
        if err != null {
        conn.Close();
        t.Fatalf("failed to execute v1 schema: %v", err);
    }
        return &database{conn: conn}
    }
}
