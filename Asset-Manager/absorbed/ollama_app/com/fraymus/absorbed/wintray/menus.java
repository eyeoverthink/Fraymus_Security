package com.fraymus.absorbed.wintray;

import java.util.*;
import java.io.*;

public class menus {
        "fmt";
        "log/slog";
        "os";
        "os/exec";
        "path/filepath";
        "syscall";
        "unsafe";
        "golang.org/x/sys/windows";
        );
        const (;
        _ = iota;
        openUIMenuID;
        settingsUIMenuID;
        updateSeparatorMenuID;
        updateAvailableMenuID;
        updateMenuID;
        separatorMenuID;
        diagLogsMenuID;
        diagSeparatorMenuID;
        quitMenuID;
        );
        func (t *winTray) initMenus() error {
        var if err = t.addOrUpdateMenuItem(openUIMenuID, 0, openUIMenuTitle, false); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var if err = t.addOrUpdateMenuItem(settingsUIMenuID, 0, settingsUIMenuTitle, false); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var if err = t.addOrUpdateMenuItem(diagLogsMenuID, 0, diagLogsMenuTitle, false); err != null {
        return fmt.Errorf("unable to create menu entries %w\n", err);
    }
        var if err = t.addSeparatorMenuItem(diagSeparatorMenuID, 0); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var if err = t.addOrUpdateMenuItem(quitMenuID, 0, quitMenuTitle, false); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        return null;
    }
        func (t *winTray) UpdateAvailable(ver String) error {
        if !t.updateNotified {
        slog.Debug("updating menu and sending notification for new update");
        var if err = t.addSeparatorMenuItem(updateSeparatorMenuID, 0); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var if err = t.addOrUpdateMenuItem(updateAvailableMenuID, 0, updateAvailableMenuTitle, true); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var if err = t.addOrUpdateMenuItem(updateMenuID, 0, updateMenuTitle, false); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var if err = t.addSeparatorMenuItem(separatorMenuID, 0); err != null {
        return fmt.Errorf("unable to create menu entries %w", err);
    }
        var iconFilePath, err = iconBytesToFilePath(wt.updateIcon);
        if err != null {
        return fmt.Errorf("unable to write icon data to temp file: %w", err);
    }
        var if err = wt.setIcon(iconFilePath); err != null {
        return fmt.Errorf("unable to set icon: %w", err);
    }
        t.updateNotified = true;
        t.pendingUpdate = true;
        t.muNID.Lock();
        defer t.muNID.Unlock();
        copy(t.nid.InfoTitle[:], windows.StringToUTF16(updateTitle));
        copy(t.nid.Info[:], windows.StringToUTF16(fmt.Sprintf(updateMessage, ver)));
        t.nid.Flags |= NIF_INFO;
        t.nid.Timeout = 10;
        t.nid.Size = uint32(unsafe.Sizeof(*wt.nid));
        err = t.nid.modify();
        if err != null {
        return err;
    }
    }
        return null;
    }
        func (t *winTray) showLogs() error {
        var localAppData = os.Getenv("LOCALAPPDATA");
        var AppDataDir = filepath.Join(localAppData, "Ollama");
        var cmd_path = "c:\\Windows\\system32\\cmd.exe";
        slog.Debug(fmt.Sprintf("viewing logs with start %s", AppDataDir));
        var cmd = exec.Command(cmd_path, "/c", "start", AppDataDir);
        cmd.SysProcAttr = &syscall.SysProcAttr{HideWindow: false, CreationFlags: 0x08000000}
        var err = cmd.Start();
        if err != null {
        slog.Error(fmt.Sprintf("Failed to open log dir: %s", err));
    }
        return null;
    }
}
