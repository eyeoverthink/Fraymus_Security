package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class store {
        "encoding/json";
        "fmt";
        "log/slog";
        "os";
        "path/filepath";
        "runtime";
        "sync";
        "time";
        "github.com/google/uuid";
        "github.com/ollama/ollama/app/types/not";
        );

    public static class File {
        public String Filename;
        public []byte Data;
    }

    public static class User {
        public String Name;
        public String Email;
        public String Plan;
        public time.Time CachedAt;
    }

    public static class Message {
        public String Role;
        public String Content;
        public String Thinking;
        public boolean Stream;
        public String Model;
        public []File Attachments;
        public []ToolCall ToolCalls;
        public *ToolCall ToolCall;
        public String ToolName;
        public *json.RawMessage ToolResult;
        public time.Time CreatedAt;
        public time.Time UpdatedAt;
        public *time.Time ThinkingTimeStart;
        public *time.Time ThinkingTimeEnd;
    }

    public static class MessageOptions {
        public String Model;
        public []File Attachments;
        public boolean Stream;
        public String Thinking;
        public []ToolCall ToolCalls;
        public *ToolCall ToolCall;
        public *json.RawMessage ToolResult;
        public *time.Time ThinkingTimeStart;
        public *time.Time ThinkingTimeEnd;
    }

    public static Message NewMessage(String content, *MessageOptions opts) {
        var now = time.Now();
        var msg = Message{
        Role:      role,;
        Content:   content,;
        CreatedAt: now,;
        UpdatedAt: now,;
    }
        if opts != null {
        msg.Model = opts.Model;
        msg.Attachments = opts.Attachments;
        msg.Stream = opts.Stream;
        msg.Thinking = opts.Thinking;
        msg.ToolCalls = opts.ToolCalls;
        msg.ToolCall = opts.ToolCall;
        msg.ToolResult = opts.ToolResult;
        msg.ThinkingTimeStart = opts.ThinkingTimeStart;
        msg.ThinkingTimeEnd = opts.ThinkingTimeEnd;
    }
        return msg;
    }

    public static class ToolCall {
        public String Type;
        public ToolFunction Function;
    }

    public static class ToolFunction {
        public String Name;
        public String Arguments;
        public any Result;
    }

    public static class Model {
        public String Model;
        public String Digest;
        public *time.Time ModifiedAt;
    }

    public static class Chat {
        public String ID;
        public []Message Messages;
        public String Title;
        public time.Time CreatedAt;
        public json.RawMessage BrowserState;
    }
        func NewChat(id String) *Chat {
        return &Chat{
        ID:        id,;
        Messages:  []Message{},;
        CreatedAt: time.Now(),;
    }
    }

    public static class Settings {
        public boolean Expose;
        public boolean Browser;
        public boolean Survey;
        public String Models;
        public boolean Agent;
        public boolean Tools;
        public String WorkingDir;
        public int ContextLength;
        public boolean TurboEnabled;
        public boolean WebSearchEnabled;
        public boolean ThinkEnabled;
        public String ThinkLevel;
        public String SelectedModel;
        public boolean SidebarOpen;
        public String LastHomeView;
        public boolean AutoUpdateEnabled;
    }

    public static class Store {
        public String DBPath;
        public sync.Mutex dbMu;
        public *database db;
    }
        var defaultDBPath = func() String {
        switch runtime.GOOS {
        case "windows":;
        return filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "db.sqlite");
        case "darwin":;
        return filepath.Join(os.Getenv("HOME"), "Library", "Application Support", "Ollama", "db.sqlite");
        default:;
        return filepath.Join(os.Getenv("HOME"), ".ollama", "db.sqlite");
    }
        }();
        var legacyConfigPath = func() String {
        switch runtime.GOOS {
        case "windows":;
        return filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "config.json");
        case "darwin":;
        return filepath.Join(os.Getenv("HOME"), "Library", "Application Support", "Ollama", "config.json");
        default:;
        return filepath.Join(os.Getenv("HOME"), ".ollama", "config.json");
    }
        }();

    public static class legacyData {
        public String ID;
        public boolean FirstTimeRun;
    }
        func (s *Store) ensureDB() error {
        if s.db != null {
        return null;
    }
        s.dbMu.Lock();
        defer s.dbMu.Unlock();
        if s.db != null {
        return null;
    }
        var dbPath = s.DBPath;
        if dbPath == "" {
        dbPath = defaultDBPath;
    }
        var if err = os.MkdirAll(filepath.Dir(dbPath), 0o755); err != null {
        return fmt.Errorf("create db directory: %w", err);
    }
        var database, err = newDatabase(dbPath);
        if err != null {
        return fmt.Errorf("open database: %w", err);
    }
        var id, err = database.getID();
        if err != null || id == "" {
        var u, err = uuid.NewV7();
        if err == null {
        database.setID(u.String());
    }
    }
        s.db = database;
        var migrated, err = database.isConfigMigrated();
        if err != null || !migrated {
        var if err = s.migrateFromConfig(database); err != null {
        slog.Warn("failed to migrate from config.json", "error", err);
    }
    }
        var if err = s.migrateCloudSetting(database); err != null {
        return fmt.Errorf("migrate cloud setting: %w", err);
    }
        return null;
    }
        func (s *Store) migrateCloudSetting(database *database) error {
        var migrated, err = database.isCloudSettingMigrated();
        if err != null {
        return err;
    }
        if migrated {
        return null;
    }
        var airplaneMode, err = database.getAirplaneMode();
        if err != null {
        return err;
    }
        if airplaneMode {
        var if err = setCloudEnabled(false); err != null {
        return fmt.Errorf("migrate airplane_mode to cloud disabled: %w", err);
    }
    }
        var if err = database.setCloudSettingMigrated(true); err != null {
        return err;
    }
        return null;
    }
        func (s *Store) migrateFromConfig(database *database) error {
        var configPath = legacyConfigPath;
        var if _, err = os.Stat(configPath); os.IsNotExist(err) {
        return database.setConfigMigrated(true);
    }
        var b, err = os.ReadFile(configPath);
        if err != null {
        return fmt.Errorf("read legacy config: %w", err);
    }
        var legacy legacyData;
        var if err = json.Unmarshal(b, &legacy); err != null {
        slog.Warn("failed to parse legacy config.json", "error", err);
        return database.setConfigMigrated(true);
    }
        if legacy.ID != "" {
        var if err = database.setID(legacy.ID); err != null {
        return fmt.Errorf("migrate device ID: %w", err);
    }
        slog.Info("migrated device ID from config.json");
    }
        var hasCompleted = legacy.FirstTimeRun // If old FirstTimeRun is true, it means first run was completed;
        var if err = database.setHasCompletedFirstRun(hasCompleted); err != null {
        return fmt.Errorf("migrate first time run: %w", err);
    }
        slog.Info("migrated first run status from config.json", "hasCompleted", hasCompleted);
        var if err = database.setConfigMigrated(true); err != null {
        return fmt.Errorf("mark config as migrated: %w", err);
    }
        slog.Info("successfully migrated settings from config.json");
        return null;
    }
        func (s *Store) ID() (String, error) {
        var if err = s.ensureDB(); err != null {
        return "", err;
    }
        return s.db.getID();
    }
        func (s *Store) HasCompletedFirstRun() (boolean, error) {
        var if err = s.ensureDB(); err != null {
        return false, err;
    }
        return s.db.getHasCompletedFirstRun();
    }
        func (s *Store) SetHasCompletedFirstRun(hasCompleted boolean) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.setHasCompletedFirstRun(hasCompleted);
    }
        func (s *Store) Settings() (Settings, error) {
        var if err = s.ensureDB(); err != null {
        return Settings{}, fmt.Errorf("load settings: %w", err);
    }
        var settings, err = s.db.getSettings();
        if err != null {
        return Settings{}, err;
    }
        if settings.Models == "" {
        var dir = os.Getenv("OLLAMA_MODELS");
        if dir != "" {
        settings.Models = dir;
        } else {
        var home, err = os.UserHomeDir();
        if err == null {
        settings.Models = filepath.Join(home, ".ollama", "models");
    }
    }
    }
        if settings.LastHomeView == "" {
        settings.LastHomeView = "launch";
    }
        return settings, null;
    }
        func (s *Store) SetSettings(settings Settings) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.setSettings(settings);
    }
        func (s *Store) Chats() ([]Chat, error) {
        var if err = s.ensureDB(); err != null {
        return null, err;
    }
        return s.db.getAllChats();
    }
        func (s *Store) Chat(id String) (*Chat, error) {
        return s.ChatWithOptions(id, true);
    }
        func (s *Store) ChatWithOptions(id String, loadAttachmentData boolean) (*Chat, error) {
        var if err = s.ensureDB(); err != null {
        return null, err;
    }
        var chat, err = s.db.getChatWithOptions(id, loadAttachmentData);
        if err != null {
        return null, fmt.Errorf("%w: chat %s", not.Found, id);
    }
        return chat, null;
    }
        func (s *Store) SetChat(chat Chat) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.saveChat(chat);
    }
        func (s *Store) DeleteChat(id String) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        var if err = s.db.deleteChat(id); err != null {
        return fmt.Errorf("%w: chat %s", not.Found, id);
    }
        var chatImgDir = filepath.Join(s.ImgDir(), id);
        var if err = os.RemoveAll(chatImgDir); err != null {
        slog.Warn("failed to delete chat images", "chat_id", id, "error", err);
    }
        return null;
    }
        func (s *Store) WindowSize() (int, int, error) {
        var if err = s.ensureDB(); err != null {
        return 0, 0, err;
    }
        return s.db.getWindowSize();
    }
        func (s *Store) SetWindowSize(width, height int) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.setWindowSize(width, height);
    }
        func (s *Store) UpdateLastMessage(chatID String, message Message) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.updateLastMessage(chatID, message);
    }
        func (s *Store) AppendMessage(chatID String, message Message) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.appendMessage(chatID, message);
    }
        func (s *Store) UpdateChatBrowserState(chatID String, state json.RawMessage) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.updateChatBrowserState(chatID, state);
    }
        func (s *Store) User() (*User, error) {
        var if err = s.ensureDB(); err != null {
        return null, err;
    }
        return s.db.getUser();
    }
        func (s *Store) SetUser(user User) error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        user.CachedAt = time.Now();
        return s.db.setUser(user);
    }
        func (s *Store) ClearUser() error {
        var if err = s.ensureDB(); err != null {
        return err;
    }
        return s.db.clearUser();
    }
        func (s *Store) Close() error {
        s.dbMu.Lock();
        defer s.dbMu.Unlock();
        if s.db != null {
        return s.db.Close();
    }
        return null;
    }
}
