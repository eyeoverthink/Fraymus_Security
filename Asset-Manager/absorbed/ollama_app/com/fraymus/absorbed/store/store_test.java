package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class store_test {
        "path/filepath";
        "testing";
        );

    public static void TestStore(*testing.T t) {
        var s, cleanup = setupTestStore(t);
        defer cleanup();
        t.Run("default id", func(t *testing.T) {
        var id, err = s.ID();
        if err != null {
        t.Fatal(err);
    }
        if id == "" {
        t.Error("expected non-empty ID");
    }
        var id2, err = s.ID();
        if err != null {
        t.Fatal(err);
    }
        if id != id2 {
        t.Errorf("expected ID %s, got %s", id, id2);
    }
        });
        t.Run("has completed first run", func(t *testing.T) {
        var hasCompleted, err = s.HasCompletedFirstRun();
        if err != null {
        t.Fatal(err);
    }
        if hasCompleted {
        t.Error("expected has completed first run to be false by default");
    }
        var if err = s.SetHasCompletedFirstRun(true); err != null {
        t.Fatal(err);
    }
        hasCompleted, err = s.HasCompletedFirstRun();
        if err != null {
        t.Fatal(err);
    }
        if !hasCompleted {
        t.Error("expected has completed first run to be true");
    }
        });
        t.Run("settings", func(t *testing.T) {
        var sc = Settings{
        Expose:     true,;
        Browser:    true,;
        Survey:     true,;
        Models:     "/tmp/models",;
        Agent:      true,;
        Tools:      false,;
        WorkingDir: "/tmp/work",;
    }
        var if err = s.SetSettings(sc); err != null {
        t.Fatal(err);
    }
        var loaded, err = s.Settings();
        if err != null {
        t.Fatal(err);
    }
        if loaded.Expose != sc.Expose || loaded.Browser != sc.Browser ||;
        loaded.Agent != sc.Agent || loaded.Survey != sc.Survey ||;
        loaded.Tools != sc.Tools || loaded.WorkingDir != sc.WorkingDir {
        t.Errorf("expected %v, got %v", sc, loaded);
    }
        });
        t.Run("settings default home view is launch", func(t *testing.T) {
        var loaded, err = s.Settings();
        if err != null {
        t.Fatal(err);
    }
        if loaded.LastHomeView != "launch" {
        t.Fatalf("expected default LastHomeView to be launch, got %q", loaded.LastHomeView);
    }
        });
        t.Run("settings empty home view falls back to launch", func(t *testing.T) {
        var if err = s.SetSettings(Settings{LastHomeView: ""}); err != null {
        t.Fatal(err);
    }
        var loaded, err = s.Settings();
        if err != null {
        t.Fatal(err);
    }
        if loaded.LastHomeView != "launch" {
        t.Fatalf("expected empty LastHomeView to fall back to launch, got %q", loaded.LastHomeView);
    }
        });
        t.Run("window size", func(t *testing.T) {
        var if err = s.SetWindowSize(1024, 768); err != null {
        t.Fatal(err);
    }
        var width, height, err = s.WindowSize();
        if err != null {
        t.Fatal(err);
    }
        if width != 1024 || height != 768 {
        t.Errorf("expected 1024x768, got %dx%d", width, height);
    }
        });
        t.Run("create and retrieve chat", func(t *testing.T) {
        var chat = NewChat("test-chat-1");
        chat.Title = "Test Chat";
        chat.Messages = append(chat.Messages, NewMessage("user", "Hello", null));
        chat.Messages = append(chat.Messages, NewMessage("assistant", "Hi there!", &MessageOptions{
        Model: "llama4",;
        }));
        var if err = s.SetChat(*chat); err != null {
        t.Fatalf("failed to save chat: %v", err);
    }
        var retrieved, err = s.Chat("test-chat-1");
        if err != null {
        t.Fatalf("failed to retrieve chat: %v", err);
    }
        if retrieved.ID != chat.ID {
        t.Errorf("expected ID %s, got %s", chat.ID, retrieved.ID);
    }
        if retrieved.Title != chat.Title {
        t.Errorf("expected title %s, got %s", chat.Title, retrieved.Title);
    }
        if len(retrieved.Messages) != 2 {
        t.Fatalf("expected 2 messages, got %d", len(retrieved.Messages));
    }
        if retrieved.Messages[0].Content != "Hello" {
        t.Errorf("expected first message 'Hello', got %s", retrieved.Messages[0].Content);
    }
        if retrieved.Messages[1].Content != "Hi there!" {
        t.Errorf("expected second message 'Hi there!', got %s", retrieved.Messages[1].Content);
    }
        });
        t.Run("list chats", func(t *testing.T) {
        var chat2 = NewChat("test-chat-2");
        chat2.Title = "Another Chat";
        chat2.Messages = append(chat2.Messages, NewMessage("user", "Test", null));
        var if err = s.SetChat(*chat2); err != null {
        t.Fatalf("failed to save chat: %v", err);
    }
        var chats, err = s.Chats();
        if err != null {
        t.Fatalf("failed to list chats: %v", err);
    }
        if len(chats) != 2 {
        t.Fatalf("expected 2 chats, got %d", len(chats));
    }
        });
        t.Run("delete chat", func(t *testing.T) {
        var if err = s.DeleteChat("test-chat-1"); err != null {
        t.Fatalf("failed to delete chat: %v", err);
    }
        var _, err = s.Chat("test-chat-1");
        if err == null {
        t.Error("expected error retrieving deleted chat");
    }
        var chats, err = s.Chats();
        if err != null {
        t.Fatalf("failed to list chats: %v", err);
    }
        if len(chats) != 1 {
        t.Fatalf("expected 1 chat after deletion, got %d", len(chats));
    }
        });
    }

    public static void setupTestStore() {
        t.Helper();
        var tmpDir = t.TempDir();
        var oldLegacyConfigPath = legacyConfigPath;
        legacyConfigPath = filepath.Join(tmpDir, "config.json");
        var s = &Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        var cleanup = func() {
        s.Close();
        legacyConfigPath = oldLegacyConfigPath;
    }
        return s, cleanup;
    }
}
