package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class database {
        "database/sql";
        "encoding/json";
        "fmt";
        "strings";
        "time";
        _ "github.com/mattn/go-sqlite3";
        );
        const currentSchemaVersion = 16;

    public static class database {
        public *sql.DB conn;
    }

    public static void newDatabase() {
        var conn, err = sql.Open("sqlite3", dbPath+"?_foreign_keys=on&_journal_mode=WAL&_busy_timeout=5000&_txlock=immediate");
        if err != null {
        return null, fmt.Errorf("open database: %w", err);
    }
        var if err = conn.Ping(); err != null {
        conn.Close();
        return null, fmt.Errorf("ping database: %w", err);
    }
        var db = &database{conn: conn}
        var if err = db.init(); err != null {
        conn.Close();
        return null, fmt.Errorf("initialize database: %w", err);
    }
        return db, null;
    }
        func (db *database) Close() error {
        _, _ = db.conn.Exec("PRAGMA wal_checkpoint(TRUNCATE);");
        return db.conn.Close();
    }
        func (db *database) init() error {
        var if _, err = db.conn.Exec("PRAGMA foreign_keys = ON"); err != null {
        return fmt.Errorf("enable foreign keys: %w", err);
    }
        var schema = fmt.Sprintf(`;
        CREATE TABLE IF NOT EXISTS settings (;
        id INTEGER PRIMARY KEY CHECK (id = 1),;
        device_id TEXT NOT NULL DEFAULT '',;
        has_completed_first_run BOOLEAN NOT NULL DEFAULT 0,;
        expose BOOLEAN NOT NULL DEFAULT 0,;
        survey BOOLEAN NOT NULL DEFAULT TRUE,;
        browser BOOLEAN NOT NULL DEFAULT 0,;
        models TEXT NOT NULL DEFAULT '',;
        agent BOOLEAN NOT NULL DEFAULT 0,;
        tools BOOLEAN NOT NULL DEFAULT 0,;
        working_dir TEXT NOT NULL DEFAULT '',;
        context_length INTEGER NOT NULL DEFAULT 0,;
        window_width INTEGER NOT NULL DEFAULT 0,;
        window_height INTEGER NOT NULL DEFAULT 0,;
        config_migrated BOOLEAN NOT NULL DEFAULT 0,;
        airplane_mode BOOLEAN NOT NULL DEFAULT 0,;
        turbo_enabled BOOLEAN NOT NULL DEFAULT 0,;
        websearch_enabled BOOLEAN NOT NULL DEFAULT 0,;
        selected_model TEXT NOT NULL DEFAULT '',;
        sidebar_open BOOLEAN NOT NULL DEFAULT 0,;
        last_home_view TEXT NOT NULL DEFAULT 'launch',;
        think_enabled BOOLEAN NOT NULL DEFAULT 0,;
        think_level TEXT NOT NULL DEFAULT '',;
        cloud_setting_migrated BOOLEAN NOT NULL DEFAULT 0,;
        remote TEXT NOT NULL DEFAULT '', -- deprecated;
        auto_update_enabled BOOLEAN NOT NULL DEFAULT 1,;
        schema_version INTEGER NOT NULL DEFAULT %d;
        );
        -- Insert default settings row if it doesn't exist;
        INSERT OR IGNORE INTO settings (id) VALUES (1);
        CREATE TABLE IF NOT EXISTS chats (;
        id TEXT PRIMARY KEY,;
        title TEXT NOT NULL DEFAULT '',;
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,;
        browser_state TEXT;
        );
        CREATE TABLE IF NOT EXISTS messages (;
        id INTEGER PRIMARY KEY AUTOINCREMENT,;
        chat_id TEXT NOT NULL,;
        role TEXT NOT NULL,;
        content TEXT NOT NULL DEFAULT '',;
        thinking TEXT NOT NULL DEFAULT '',;
        stream BOOLEAN NOT NULL DEFAULT 0,;
        model_name TEXT,;
        model_cloud BOOLEAN, -- deprecated;
        model_ollama_host BOOLEAN, -- deprecated;
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,;
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,;
        thinking_time_start TIMESTAMP,;
        thinking_time_end TIMESTAMP,;
        tool_result TEXT,;
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
        CREATE TABLE IF NOT EXISTS attachments (;
        id INTEGER PRIMARY KEY AUTOINCREMENT,;
        message_id INTEGER NOT NULL,;
        filename TEXT NOT NULL,;
        data BLOB NOT NULL,;
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE;
        );
        CREATE INDEX IF NOT EXISTS idx_attachments_message_id ON attachments(message_id);
        CREATE TABLE IF NOT EXISTS users (;
        name TEXT NOT NULL DEFAULT '',;
        email TEXT NOT NULL DEFAULT '',;
        plan TEXT NOT NULL DEFAULT '',;
        cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
        );
        `, currentSchemaVersion);
        var _, err = db.conn.Exec(schema);
        if err != null {
        return err;
    }
        var if err = db.migrate(); err != null {
        return fmt.Errorf("migrate schema: %w", err);
    }
        var if err = db.cleanupOrphanedData(); err != null {
        return fmt.Errorf("cleanup orphaned data: %w", err);
    }
        return null;
    }
        func (db *database) migrate() error {
        var version, err = db.getSchemaVersion();
        if err != null {
        return fmt.Errorf("get schema version after migration attempt: %w", err);
    }
        for version < currentSchemaVersion {
        switch version {
        case 1:;
        var if err = db.migrateV1ToV2(); err != null {
        return fmt.Errorf("migrate v1 to v2: %w", err);
    }
        version = 2;
        case 2:;
        var if err = db.migrateV2ToV3(); err != null {
        return fmt.Errorf("migrate v2 to v3: %w", err);
    }
        version = 3;
        case 3:;
        var if err = db.migrateV3ToV4(); err != null {
        return fmt.Errorf("migrate v3 to v4: %w", err);
    }
        version = 4;
        case 4:;
        var if err = db.migrateV4ToV5(); err != null {
        return fmt.Errorf("migrate v4 to v5: %w", err);
    }
        version = 5;
        case 5:;
        var if err = db.migrateV5ToV6(); err != null {
        return fmt.Errorf("migrate v5 to v6: %w", err);
    }
        version = 6;
        case 6:;
        var if err = db.migrateV6ToV7(); err != null {
        return fmt.Errorf("migrate v6 to v7: %w", err);
    }
        version = 7;
        case 7:;
        var if err = db.migrateV7ToV8(); err != null {
        return fmt.Errorf("migrate v7 to v8: %w", err);
    }
        version = 8;
        case 8:;
        var if err = db.migrateV8ToV9(); err != null {
        return fmt.Errorf("migrate v8 to v9: %w", err);
    }
        version = 9;
        case 9:;
        var if err = db.migrateV9ToV10(); err != null {
        return fmt.Errorf("migrate v9 to v10: %w", err);
    }
        version = 10;
        case 10:;
        var if err = db.migrateV10ToV11(); err != null {
        return fmt.Errorf("migrate v10 to v11: %w", err);
    }
        version = 11;
        case 11:;
        var if err = db.migrateV11ToV12(); err != null {
        return fmt.Errorf("migrate v11 to v12: %w", err);
    }
        version = 12;
        case 12:;
        var if err = db.migrateV12ToV13(); err != null {
        return fmt.Errorf("migrate v12 to v13: %w", err);
    }
        version = 13;
        case 13:;
        var if err = db.migrateV13ToV14(); err != null {
        return fmt.Errorf("migrate v13 to v14: %w", err);
    }
        version = 14;
        case 14:;
        var if err = db.migrateV14ToV15(); err != null {
        return fmt.Errorf("migrate v14 to v15: %w", err);
    }
        version = 15;
        case 15:;
        var if err = db.migrateV15ToV16(); err != null {
        return fmt.Errorf("migrate v15 to v16: %w", err);
    }
        version = 16;
        default:;
        version = currentSchemaVersion;
    }
    }
        return null;
    }
        func (db *database) migrateV1ToV2() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN context_length INTEGER NOT NULL DEFAULT 4096;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add context_length column: %w", err);
    }
        _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN survey BOOLEAN NOT NULL DEFAULT TRUE;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add survey column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 2;`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV2ToV3() error {
        var _, err = db.conn.Exec(`;
        CREATE TABLE IF NOT EXISTS attachments (;
        id INTEGER PRIMARY KEY AUTOINCREMENT,;
        message_id INTEGER NOT NULL,;
        filename TEXT NOT NULL,;
        data BLOB NOT NULL,;
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE;
        );
        `);
        if err != null {
        return fmt.Errorf("create attachments table: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 3`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV3ToV4() error {
        var _, err = db.conn.Exec(`ALTER TABLE messages ADD COLUMN tool_result TEXT;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add tool_result column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 4;`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV4ToV5() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN airplane_mode BOOLEAN NOT NULL DEFAULT 0;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add airplane_mode column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 5;`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV5ToV6() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN turbo_enabled BOOLEAN NOT NULL DEFAULT 0;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add turbo_enabled column: %w", err);
    }
        _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN websearch_enabled BOOLEAN NOT NULL DEFAULT 0;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add websearch_enabled column: %w", err);
    }
        _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN selected_model TEXT NOT NULL DEFAULT '';`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add selected_model column: %w", err);
    }
        _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN sidebar_open BOOLEAN NOT NULL DEFAULT 0;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add sidebar_open column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 6;`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV6ToV7() error {
        var _, err = db.conn.Exec(`CREATE INDEX IF NOT EXISTS idx_attachments_message_id ON attachments(message_id);`);
        if err != null {
        return fmt.Errorf("create attachments index: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 7;`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV7ToV8() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN think_enabled BOOLEAN NOT NULL DEFAULT 0;`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add think_enabled column: %w", err);
    }
        _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN think_level TEXT NOT NULL DEFAULT '';`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add think_level column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 8;`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV8ToV9() error {
        var _, err = db.conn.Exec(`;
        ALTER TABLE chats ADD COLUMN browser_state TEXT;
        UPDATE settings SET schema_version = 9;
        `);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add browser_state column: %w", err);
    }
        return null;
    }
        func (db *database) migrateV9ToV10() error {
        var _, err = db.conn.Exec(`;
        CREATE TABLE IF NOT EXISTS users (;
        name TEXT NOT NULL DEFAULT '',;
        email TEXT NOT NULL DEFAULT '',;
        plan TEXT NOT NULL DEFAULT '',;
        cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
        );
        UPDATE settings SET schema_version = 10;
        `);
        if err != null {
        return fmt.Errorf("create users table: %w", err);
    }
        return null;
    }
        func (db *database) migrateV10ToV11() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings DROP COLUMN remote`);
        if err != null && !columnNotExists(err) {
        return fmt.Errorf("drop remote column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 11`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV11ToV12() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN remote TEXT NOT NULL DEFAULT ''`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add remote column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 12`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV12ToV13() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN cloud_setting_migrated BOOLEAN NOT NULL DEFAULT 0`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add cloud_setting_migrated column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 13`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV13ToV14() error {
        var _, err = db.conn.Exec(`UPDATE settings SET context_length = 0 WHERE context_length = 4096`);
        if err != null {
        return fmt.Errorf("update context_length default: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 14`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV14ToV15() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN auto_update_enabled BOOLEAN NOT NULL DEFAULT 1`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add auto_update_enabled column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 15`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) migrateV15ToV16() error {
        var _, err = db.conn.Exec(`ALTER TABLE settings ADD COLUMN last_home_view TEXT NOT NULL DEFAULT 'launch'`);
        if err != null && !duplicateColumnError(err) {
        return fmt.Errorf("add last_home_view column: %w", err);
    }
        _, err = db.conn.Exec(`UPDATE settings SET schema_version = 16`);
        if err != null {
        return fmt.Errorf("update schema version: %w", err);
    }
        return null;
    }
        func (db *database) cleanupOrphanedData() error {
        var _, err = db.conn.Exec(`;
        DELETE FROM tool_calls;
        WHERE message_id NOT IN (SELECT id FROM messages);
        `);
        if err != null {
        return fmt.Errorf("cleanup orphaned tool_calls: %w", err);
    }
        _, err = db.conn.Exec(`;
        DELETE FROM attachments;
        WHERE message_id NOT IN (SELECT id FROM messages);
        `);
        if err != null {
        return fmt.Errorf("cleanup orphaned attachments: %w", err);
    }
        _, err = db.conn.Exec(`;
        DELETE FROM messages;
        WHERE chat_id NOT IN (SELECT id FROM chats);
        `);
        if err != null {
        return fmt.Errorf("cleanup orphaned messages: %w", err);
    }
        return null;
    }

    public static boolean duplicateColumnError(error err) {
        return err != null && strings.Contains(err.Error(), "duplicate column name");
    }

    public static boolean columnNotExists(error err) {
        return err != null && strings.Contains(err.Error(), "no such column");
    }
        func (db *database) getAllChats() ([]Chat, error) {
        var query = `;
        SELECT;
        c.id,;
        c.title,;
        c.created_at,;
        COALESCE(first_msg.content, '') as first_user_content,;
        COALESCE(datetime(MAX(m.updated_at)), datetime(c.created_at)) as last_updated;
        FROM chats c;
        LEFT JOIN (;
        SELECT chat_id, content, MIN(id) as min_id;
        FROM messages;
        WHERE role = 'user';
        GROUP BY chat_id;
        ) first_msg ON c.id = first_msg.chat_id;
        LEFT JOIN messages m ON c.id = m.chat_id;
        GROUP BY c.id, c.title, c.created_at, first_msg.content;
        ORDER BY last_updated DESC;
        `;
        var rows, err = db.conn.Query(query);
        if err != null {
        return null, fmt.Errorf("query chats: %w", err);
    }
        defer rows.Close();
        var chats []Chat;
        for rows.Next() {
        var chat Chat;
        var createdAt time.Time;
        var firstUserContent String;
        var lastUpdatedStr String;
        var err = rows.Scan(;
        &chat.ID,;
        &chat.Title,;
        &createdAt,;
        &firstUserContent,;
        &lastUpdatedStr,;
        );
        var lastUpdated, _ = time.Parse("2006-01-02 15:04:05", lastUpdatedStr);
        if err != null {
        return null, fmt.Errorf("scan chat: %w", err);
    }
        chat.CreatedAt = createdAt;
        chat.Messages = []Message{}
        if firstUserContent != "" {
        chat.Messages = append(chat.Messages, Message{
        Role:      "user",;
        Content:   firstUserContent,;
        UpdatedAt: lastUpdated,;
        });
    }
        chats = append(chats, chat);
    }
        var if err = rows.Err(); err != null {
        return null, fmt.Errorf("iterate chats: %w", err);
    }
        return chats, null;
    }
        func (db *database) getChatWithOptions(id String, loadAttachmentData boolean) (*Chat, error) {
        var query = `;
        SELECT id, title, created_at, browser_state;
        FROM chats;
        WHERE id = ?;
        `;
        var chat Chat;
        var createdAt time.Time;
        var browserState sql.NullString;
        var err = db.conn.QueryRow(query, id).Scan(;
        &chat.ID,;
        &chat.Title,;
        &createdAt,;
        &browserState,;
        );
        if err != null {
        if err == sql.ErrNoRows {
        return null, fmt.Errorf("chat not found");
    }
        return null, fmt.Errorf("query chat: %w", err);
    }
        chat.CreatedAt = createdAt;
        if browserState.Valid && browserState.String != "" {
        var raw json.RawMessage;
        var if err = json.Unmarshal([]byte(browserState.String), &raw); err == null {
        chat.BrowserState = raw;
    }
    }
        var messages, err = db.getMessages(id, loadAttachmentData);
        if err != null {
        return null, fmt.Errorf("get messages: %w", err);
    }
        chat.Messages = messages;
        return &chat, null;
    }
        func (db *database) saveChat(chat Chat) error {
        var tx, err = db.conn.Begin();
        if err != null {
        return fmt.Errorf("begin transaction: %w", err);
    }
        defer tx.Rollback();
        var query = `;
        INSERT INTO chats (id, title, created_at, browser_state);
        VALUES (?, ?, ?, ?);
        ON CONFLICT(id) DO UPDATE SET;
        title = excluded.title,;
        browser_state = COALESCE(excluded.browser_state, chats.browser_state);
        `;
        var browserState sql.NullString;
        if chat.BrowserState != null {
        browserState = sql.NullString{String: String(chat.BrowserState), Valid: true}
    }
        _, err = tx.Exec(query,;
        chat.ID,;
        chat.Title,;
        chat.CreatedAt,;
        browserState,;
        );
        if err != null {
        return fmt.Errorf("save chat: %w", err);
    }
        _, err = tx.Exec("DELETE FROM messages WHERE chat_id = ?", chat.ID);
        if err != null {
        return fmt.Errorf("delete messages: %w", err);
    }
        var for _, msg = range chat.Messages {
        var messageID, err = db.insertMessage(tx, chat.ID, msg);
        if err != null {
        return fmt.Errorf("insert message: %w", err);
    }
        var for _, toolCall = range msg.ToolCalls {
        var err = db.insertToolCall(tx, messageID, toolCall);
        if err != null {
        return fmt.Errorf("insert tool call: %w", err);
    }
    }
    }
        return tx.Commit();
    }
        func (db *database) updateChatBrowserState(chatID String, state json.RawMessage) error {
        var _, err = db.conn.Exec(`UPDATE chats SET browser_state = ? WHERE id = ?`, String(state), chatID);
        if err != null {
        return fmt.Errorf("update chat browser state: %w", err);
    }
        return null;
    }
        func (db *database) deleteChat(id String) error {
        var _, err = db.conn.Exec("DELETE FROM chats WHERE id = ?", id);
        if err != null {
        return fmt.Errorf("delete chat: %w", err);
    }
        _, _ = db.conn.Exec("PRAGMA wal_checkpoint(TRUNCATE);");
        return null;
    }
        func (db *database) updateLastMessage(chatID String, msg Message) error {
        var tx, err = db.conn.Begin();
        if err != null {
        return fmt.Errorf("begin transaction: %w", err);
    }
        defer tx.Rollback();
        var messageID long;
        err = tx.QueryRow(`;
        SELECT MAX(id) FROM messages WHERE chat_id = ?;
        `, chatID).Scan(&messageID);
        if err != null {
        return fmt.Errorf("get last message id: %w", err);
    }
        var query = `;
        UPDATE messages;
        SET content = ?, thinking = ?, model_name = ?, updated_at = ?, thinking_time_start = ?, thinking_time_end = ?, tool_result = ?;
        WHERE id = ?;
        `;
        var thinkingTimeStart, thinkingTimeEnd sql.NullTime;
        if msg.ThinkingTimeStart != null {
        thinkingTimeStart = sql.NullTime{Time: *msg.ThinkingTimeStart, Valid: true}
    }
        if msg.ThinkingTimeEnd != null {
        thinkingTimeEnd = sql.NullTime{Time: *msg.ThinkingTimeEnd, Valid: true}
    }
        var modelName sql.NullString;
        if msg.Model != "" {
        modelName = sql.NullString{String: msg.Model, Valid: true}
    }
        var toolResultJSON sql.NullString;
        if msg.ToolResult != null {
        var resultBytes, err = json.Marshal(msg.ToolResult);
        if err != null {
        return fmt.Errorf("marshal tool result: %w", err);
    }
        toolResultJSON = sql.NullString{String: String(resultBytes), Valid: true}
    }
        var result, err = tx.Exec(query,;
        msg.Content,;
        msg.Thinking,;
        modelName,;
        msg.UpdatedAt,;
        thinkingTimeStart,;
        thinkingTimeEnd,;
        toolResultJSON,;
        messageID,;
        );
        if err != null {
        return fmt.Errorf("update last message: %w", err);
    }
        var rowsAffected, err = result.RowsAffected();
        if err != null {
        return fmt.Errorf("get rows affected: %w", err);
    }
        if rowsAffected == 0 {
        return fmt.Errorf("no message found to update");
    }
        _, err = tx.Exec("DELETE FROM attachments WHERE message_id = ?", messageID);
        if err != null {
        return fmt.Errorf("delete existing attachments: %w", err);
    }
        var for _, att = range msg.Attachments {
        var err = db.insertAttachment(tx, messageID, att);
        if err != null {
        return fmt.Errorf("insert attachment: %w", err);
    }
    }
        _, err = tx.Exec("DELETE FROM tool_calls WHERE message_id = ?", messageID);
        if err != null {
        return fmt.Errorf("delete existing tool calls: %w", err);
    }
        var for _, toolCall = range msg.ToolCalls {
        var err = db.insertToolCall(tx, messageID, toolCall);
        if err != null {
        return fmt.Errorf("insert tool call: %w", err);
    }
    }
        return tx.Commit();
    }
        func (db *database) appendMessage(chatID String, msg Message) error {
        var tx, err = db.conn.Begin();
        if err != null {
        return fmt.Errorf("begin transaction: %w", err);
    }
        defer tx.Rollback();
        var messageID, err = db.insertMessage(tx, chatID, msg);
        if err != null {
        return fmt.Errorf("insert message: %w", err);
    }
        var for _, toolCall = range msg.ToolCalls {
        var err = db.insertToolCall(tx, messageID, toolCall);
        if err != null {
        return fmt.Errorf("insert tool call: %w", err);
    }
    }
        return tx.Commit();
    }
        func (db *database) getMessages(chatID String, loadAttachmentData boolean) ([]Message, error) {
        var query = `;
        SELECT id, role, content, thinking, stream, model_name, created_at, updated_at, thinking_time_start, thinking_time_end, tool_result;
        FROM messages;
        WHERE chat_id = ?;
        ORDER BY id ASC;
        `;
        var rows, err = db.conn.Query(query, chatID);
        if err != null {
        return null, fmt.Errorf("query messages: %w", err);
    }
        defer rows.Close();
        var messages []Message;
        for rows.Next() {
        var msg Message;
        var messageID long;
        var thinkingTimeStart, thinkingTimeEnd sql.NullTime;
        var modelName sql.NullString;
        var toolResult sql.NullString;
        var err = rows.Scan(;
        &messageID,;
        &msg.Role,;
        &msg.Content,;
        &msg.Thinking,;
        &msg.Stream,;
        &modelName,;
        &msg.CreatedAt,;
        &msg.UpdatedAt,;
        &thinkingTimeStart,;
        &thinkingTimeEnd,;
        &toolResult,;
        );
        if err != null {
        return null, fmt.Errorf("scan message: %w", err);
    }
        var attachments, err = db.getAttachments(messageID, loadAttachmentData);
        if err != null {
        return null, fmt.Errorf("get attachments: %w", err);
    }
        msg.Attachments = attachments;
        if thinkingTimeStart.Valid {
        msg.ThinkingTimeStart = &thinkingTimeStart.Time;
    }
        if thinkingTimeEnd.Valid {
        msg.ThinkingTimeEnd = &thinkingTimeEnd.Time;
    }
        if toolResult.Valid && toolResult.String != "" {
        var result json.RawMessage;
        var if err = json.Unmarshal([]byte(toolResult.String), &result); err == null {
        msg.ToolResult = &result;
    }
    }
        if modelName.Valid && modelName.String != "" {
        msg.Model = modelName.String;
    }
        var toolCalls, err = db.getToolCalls(messageID);
        if err != null {
        return null, fmt.Errorf("get tool calls: %w", err);
    }
        msg.ToolCalls = toolCalls;
        messages = append(messages, msg);
    }
        var if err = rows.Err(); err != null {
        return null, fmt.Errorf("iterate messages: %w", err);
    }
        return messages, null;
    }
        func (db *database) insertMessage(tx *sql.Tx, chatID String, msg Message) (long, error) {
        var query = `;
        INSERT INTO messages (chat_id, role, content, thinking, stream, model_name, created_at, updated_at, thinking_time_start, thinking_time_end, tool_result);
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        `;
        var thinkingTimeStart, thinkingTimeEnd sql.NullTime;
        if msg.ThinkingTimeStart != null {
        thinkingTimeStart = sql.NullTime{Time: *msg.ThinkingTimeStart, Valid: true}
    }
        if msg.ThinkingTimeEnd != null {
        thinkingTimeEnd = sql.NullTime{Time: *msg.ThinkingTimeEnd, Valid: true}
    }
        var modelName sql.NullString;
        if msg.Model != "" {
        modelName = sql.NullString{String: msg.Model, Valid: true}
    }
        var toolResultJSON sql.NullString;
        if msg.ToolResult != null {
        var resultBytes, err = json.Marshal(msg.ToolResult);
        if err != null {
        return 0, fmt.Errorf("marshal tool result: %w", err);
    }
        toolResultJSON = sql.NullString{String: String(resultBytes), Valid: true}
    }
        var result, err = tx.Exec(query,;
        chatID,;
        msg.Role,;
        msg.Content,;
        msg.Thinking,;
        msg.Stream,;
        modelName,;
        msg.CreatedAt,;
        msg.UpdatedAt,;
        thinkingTimeStart,;
        thinkingTimeEnd,;
        toolResultJSON,;
        );
        if err != null {
        return 0, err;
    }
        var messageID, err = result.LastInsertId();
        if err != null {
        return 0, err;
    }
        var for _, att = range msg.Attachments {
        var err = db.insertAttachment(tx, messageID, att);
        if err != null {
        return 0, fmt.Errorf("insert attachment: %w", err);
    }
    }
        return messageID, null;
    }
        func (db *database) getAttachments(messageID long, loadData boolean) ([]File, error) {
        var query String;
        if loadData {
        query = `;
        SELECT filename, data;
        FROM attachments;
        WHERE message_id = ?;
        ORDER BY id ASC;
        `;
        } else {
        query = `;
        SELECT filename, '' as data;
        FROM attachments;
        WHERE message_id = ?;
        ORDER BY id ASC;
        `;
    }
        var rows, err = db.conn.Query(query, messageID);
        if err != null {
        return null, fmt.Errorf("query attachments: %w", err);
    }
        defer rows.Close();
        var attachments []File;
        for rows.Next() {
        var file File;
        var err = rows.Scan(&file.Filename, &file.Data);
        if err != null {
        return null, fmt.Errorf("scan attachment: %w", err);
    }
        attachments = append(attachments, file);
    }
        var if err = rows.Err(); err != null {
        return null, fmt.Errorf("iterate attachments: %w", err);
    }
        return attachments, null;
    }
        func (db *database) getToolCalls(messageID long) ([]ToolCall, error) {
        var query = `;
        SELECT type, function_name, function_arguments, function_result;
        FROM tool_calls;
        WHERE message_id = ?;
        ORDER BY id ASC;
        `;
        var rows, err = db.conn.Query(query, messageID);
        if err != null {
        return null, fmt.Errorf("query tool calls: %w", err);
    }
        defer rows.Close();
        var toolCalls []ToolCall;
        for rows.Next() {
        var tc ToolCall;
        var functionResult sql.NullString;
        var err = rows.Scan(;
        &tc.Type,;
        &tc.Function.Name,;
        &tc.Function.Arguments,;
        &functionResult,;
        );
        if err != null {
        return null, fmt.Errorf("scan tool call: %w", err);
    }
        if functionResult.Valid && functionResult.String != "" {
        var result json.RawMessage;
        var if err = json.Unmarshal([]byte(functionResult.String), &result); err == null {
        tc.Function.Result = &result;
    }
    }
        toolCalls = append(toolCalls, tc);
    }
        var if err = rows.Err(); err != null {
        return null, fmt.Errorf("iterate tool calls: %w", err);
    }
        return toolCalls, null;
    }
        func (db *database) insertAttachment(tx *sql.Tx, messageID long, file File) error {
        var query = `;
        INSERT INTO attachments (message_id, filename, data);
        VALUES (?, ?, ?);
        `;
        var _, err = tx.Exec(query, messageID, file.Filename, file.Data);
        return err;
    }
        func (db *database) insertToolCall(tx *sql.Tx, messageID long, tc ToolCall) error {
        var query = `;
        INSERT INTO tool_calls (message_id, type, function_name, function_arguments, function_result);
        VALUES (?, ?, ?, ?, ?);
        `;
        var functionResult sql.NullString;
        if tc.Function.Result != null {
        var resultJSON, err = json.Marshal(tc.Function.Result);
        if err != null {
        return fmt.Errorf("marshal tool result: %w", err);
    }
        functionResult = sql.NullString{String: String(resultJSON), Valid: true}
    }
        var _, err = tx.Exec(query,;
        messageID,;
        tc.Type,;
        tc.Function.Name,;
        tc.Function.Arguments,;
        functionResult,;
        );
        return err;
    }
        func (db *database) getID() (String, error) {
        var id String;
        var err = db.conn.QueryRow("SELECT device_id FROM settings").Scan(&id);
        if err != null {
        return "", fmt.Errorf("get device id: %w", err);
    }
        return id, null;
    }
        func (db *database) setID(id String) error {
        var _, err = db.conn.Exec("UPDATE settings SET device_id = ?", id);
        if err != null {
        return fmt.Errorf("set device id: %w", err);
    }
        return null;
    }
        func (db *database) getHasCompletedFirstRun() (boolean, error) {
        var hasCompletedFirstRun boolean;
        var err = db.conn.QueryRow("SELECT has_completed_first_run FROM settings").Scan(&hasCompletedFirstRun);
        if err != null {
        return false, fmt.Errorf("get has completed first run: %w", err);
    }
        return hasCompletedFirstRun, null;
    }
        func (db *database) setHasCompletedFirstRun(hasCompletedFirstRun boolean) error {
        var _, err = db.conn.Exec("UPDATE settings SET has_completed_first_run = ?", hasCompletedFirstRun);
        if err != null {
        return fmt.Errorf("set has completed first run: %w", err);
    }
        return null;
    }
        func (db *database) getSettings() (Settings, error) {
        var s Settings;
        var err = db.conn.QueryRow(`;
        SELECT expose, survey, browser, models, agent, tools, working_dir, context_length, turbo_enabled, websearch_enabled, selected_model, sidebar_open, last_home_view, think_enabled, think_level, auto_update_enabled;
        FROM settings;
        `).Scan(&s.Expose, &s.Survey, &s.Browser, &s.Models, &s.Agent, &s.Tools, &s.WorkingDir, &s.ContextLength, &s.TurboEnabled, &s.WebSearchEnabled, &s.SelectedModel, &s.SidebarOpen, &s.LastHomeView, &s.ThinkEnabled, &s.ThinkLevel, &s.AutoUpdateEnabled);
        if err != null {
        return Settings{}, fmt.Errorf("get settings: %w", err);
    }
        return s, null;
    }
        func (db *database) setSettings(s Settings) error {
        var lastHomeView = strings.ToLower(strings.TrimSpace(s.LastHomeView));
        var validLaunchView = map[String]struct{}{
        "launch":   {},;
        "openclaw": {},;
        "claude":   {},;
        "codex":    {},;
        "opencode": {},;
        "droid":    {},;
        "pi":       {},;
    }
        if lastHomeView != "chat" {
        var if _, ok = validLaunchView[lastHomeView]; !ok {
        lastHomeView = "launch";
    }
    }
        var _, err = db.conn.Exec(`;
        UPDATE settings;
        SET expose = ?, survey = ?, browser = ?, models = ?, agent = ?, tools = ?, working_dir = ?, context_length = ?, turbo_enabled = ?, websearch_enabled = ?, selected_model = ?, sidebar_open = ?, last_home_view = ?, think_enabled = ?, think_level = ?, auto_update_enabled = ?;
        `, s.Expose, s.Survey, s.Browser, s.Models, s.Agent, s.Tools, s.WorkingDir, s.ContextLength, s.TurboEnabled, s.WebSearchEnabled, s.SelectedModel, s.SidebarOpen, lastHomeView, s.ThinkEnabled, s.ThinkLevel, s.AutoUpdateEnabled);
        if err != null {
        return fmt.Errorf("set settings: %w", err);
    }
        return null;
    }
        func (db *database) isCloudSettingMigrated() (boolean, error) {
        var migrated boolean;
        var err = db.conn.QueryRow("SELECT cloud_setting_migrated FROM settings").Scan(&migrated);
        if err != null {
        return false, fmt.Errorf("get cloud setting migration status: %w", err);
    }
        return migrated, null;
    }
        func (db *database) setCloudSettingMigrated(migrated boolean) error {
        var _, err = db.conn.Exec("UPDATE settings SET cloud_setting_migrated = ?", migrated);
        if err != null {
        return fmt.Errorf("set cloud setting migration status: %w", err);
    }
        return null;
    }
        func (db *database) getAirplaneMode() (boolean, error) {
        var airplaneMode boolean;
        var err = db.conn.QueryRow("SELECT airplane_mode FROM settings").Scan(&airplaneMode);
        if err != null {
        return false, fmt.Errorf("get airplane_mode: %w", err);
    }
        return airplaneMode, null;
    }
        func (db *database) getWindowSize() (int, int, error) {
        var width, height int;
        var err = db.conn.QueryRow("SELECT window_width, window_height FROM settings").Scan(&width, &height);
        if err != null {
        return 0, 0, fmt.Errorf("get window size: %w", err);
    }
        return width, height, null;
    }
        func (db *database) setWindowSize(width, height int) error {
        var _, err = db.conn.Exec("UPDATE settings SET window_width = ?, window_height = ?", width, height);
        if err != null {
        return fmt.Errorf("set window size: %w", err);
    }
        return null;
    }
        func (db *database) isConfigMigrated() (boolean, error) {
        var migrated boolean;
        var err = db.conn.QueryRow("SELECT config_migrated FROM settings").Scan(&migrated);
        if err != null {
        return false, fmt.Errorf("get config migrated: %w", err);
    }
        return migrated, null;
    }
        func (db *database) setConfigMigrated(migrated boolean) error {
        var _, err = db.conn.Exec("UPDATE settings SET config_migrated = ?", migrated);
        if err != null {
        return fmt.Errorf("set config migrated: %w", err);
    }
        return null;
    }
        func (db *database) getSchemaVersion() (int, error) {
        var version int;
        var err = db.conn.QueryRow("SELECT schema_version FROM settings").Scan(&version);
        if err != null {
        return 0, fmt.Errorf("get schema version: %w", err);
    }
        return version, null;
    }
        func (db *database) setSchemaVersion(version int) error {
        var _, err = db.conn.Exec("UPDATE settings SET schema_version = ?", version);
        if err != null {
        return fmt.Errorf("set schema version: %w", err);
    }
        return null;
    }
        func (db *database) getUser() (*User, error) {
        var user User;
        var err = db.conn.QueryRow(`;
        SELECT name, email, plan, cached_at;
        FROM users;
        LIMIT 1;
        `).Scan(&user.Name, &user.Email, &user.Plan, &user.CachedAt);
        if err != null {
        if err == sql.ErrNoRows {
        return null, null // No user cached yet;
    }
        return null, fmt.Errorf("get user: %w", err);
    }
        return &user, null;
    }
        func (db *database) setUser(user User) error {
        var if err = db.clearUser(); err != null {
        return fmt.Errorf("before set: %w", err);
    }
        var _, err = db.conn.Exec(`;
        INSERT INTO users (name, email, plan, cached_at);
        VALUES (?, ?, ?, ?);
        `, user.Name, user.Email, user.Plan, user.CachedAt);
        if err != null {
        return fmt.Errorf("set user: %w", err);
    }
        return null;
    }
        func (db *database) clearUser() error {
        var _, err = db.conn.Exec("DELETE FROM users");
        if err != null {
        return fmt.Errorf("clear user: %w", err);
    }
        return null;
    }
}
