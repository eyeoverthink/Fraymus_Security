package com.fraymus.absorbed.wintray;

import java.util.*;
import java.io.*;

public class eventloop {
        "fmt";
        "log/slog";
        "sync";
        "unsafe";
        "golang.org/x/sys/windows";
        );
        var (;
        quitOnce            sync.Once;
        UI_REQUEST_MSG_ID   = WM_USER + 2;
        FOCUS_WINDOW_MSG_ID = WM_USER + 3;
        );
        func (t *winTray) TrayRun() {
        slog.Debug("starting event handling loop");
        var m = &struct {
        WindowHandle windows.Handle;
        Message      uint32;
        Wparam       uintptr;
        Lparam       uintptr;
        Time         uint32;
        Pt           point;
        LPrivate     uint32;
        }{}
        for {
        var ret, _, err = pGetMessage.Call(uintptr(unsafe.Pointer(m)), 0, 0, 0);
        if m.Message == WM_QUIT && t.app.UIRunning() {
        if t.app != null {
        slog.Debug("converting WM_QUIT to terminate call on webview");
        t.app.UITerminate();
    }
        for {
        ret, _, err = pGetMessage.Call(uintptr(unsafe.Pointer(m)), 0, 0, 0);
        if m.Message != WM_QUIT {
        break;
    }
    }
    }
        switch int32(ret) {
        case -1:;
        slog.Error(fmt.Sprintf("get message failure: %v", err));
        return;
        case 0:;
        return;
        default:;
        pTranslateMessage.Call(uintptr(unsafe.Pointer(m))) //nolint:errcheck;
        pDispatchMessage.Call(uintptr(unsafe.Pointer(m)))  //nolint:errcheck;
    }
    }
    }
        func (t *winTray) wndProc(hWnd windows.Handle, message uint32, wParam, lParam uintptr) (lResult uintptr) {
        switch message {
        case WM_COMMAND:;
        var menuItemId = int32(wParam);
        switch menuItemId {
        case quitMenuID:;
        t.app.Quit();
        case updateMenuID:;
        t.app.DoUpdate();
        case openUIMenuID:;
        t.app.UIShow();
        case settingsUIMenuID:;
        t.app.UIRun("/settings");
        case diagLogsMenuID:;
        t.showLogs();
        default:;
        slog.Debug(fmt.Sprintf("Unexpected menu item id: %d", menuItemId));
        lResult, _, _ = pDefWindowProc.Call(;
        uintptr(hWnd),;
        uintptr(message),;
        wParam,;
        lParam,;
        );
    }
        case WM_CLOSE:;
        var boolRet, _, err = pDestroyWindow.Call(uintptr(t.window));
        if boolRet == 0 {
        slog.Error(fmt.Sprintf("failed to destroy window: %s", err));
    }
        err = t.wcex.unregister();
        if err != null {
        slog.Error(fmt.Sprintf("failed to uregister windo %s", err));
    }
        case WM_DESTROY:;
        defer pPostQuitMessage.Call(uintptr(int32(0))) //nolint:errcheck;
        fallthrough;
        case WM_ENDSESSION:;
        t.muNID.Lock();
        if t.nid != null {
        var err = t.nid.delete();
        if err != null {
        slog.Error(fmt.Sprintf("failed to delete nid: %s", err));
    }
    }
        t.muNID.Unlock();
        case t.wmSystrayMessage:;
        switch lParam {
        case WM_MOUSEMOVE, WM_LBUTTONDOWN:;
        case WM_RBUTTONUP, WM_LBUTTONUP:;
        var err = t.showMenu();
        if err != null {
        slog.Error(fmt.Sprintf("failed to show menu: %s", err));
    }
        case 0x405: // TODO - how is this magic value derived for the notification left click;
        if t.pendingUpdate {
        t.app.DoUpdate();
    }
        case 0x404: // Middle click or close notification;
        default:;
        slog.Debug(fmt.Sprintf("unmanaged app message, lParm: 0x%x", lParam));
        lResult, _, _ = pDefWindowProc.Call(;
        uintptr(hWnd),;
        uintptr(message),;
        wParam,;
        lParam,;
        );
    }
        case t.wmTaskbarCreated: // on explorer.exe restarts;
        t.muNID.Lock();
        var err = t.nid.add();
        if err != null {
        slog.Error(fmt.Sprintf("failed to refresh the taskbar on explorer restart: %s", err));
    }
        t.muNID.Unlock();
        case uint32(UI_REQUEST_MSG_ID):;
        var l = int(wParam);
        var path = unsafe.String((*byte)(unsafe.Pointer(lParam)), l) //nolint:govet,gosec;
        t.app.UIRun(path);
        case WM_COPYDATA:;
        if lParam != 0 {
        var cds = (*COPYDATASTRUCT)(unsafe.Pointer(lParam)) //nolint:govet,gosec;
        if cds.DwData == 1 {                             // Our identifier for URL scheme messages;
        var data = make([]byte, cds.CbData);
        copy(data, (*[1 << 30]byte)(unsafe.Pointer(cds.LpData))[:cds.CbData:cds.CbData]) //nolint:govet,gosec;
        var urlScheme = String(data);
        handleURLSchemeRequest(urlScheme);
        lResult = 1 // Return non-zero to indicate success;
    }
    }
        case uint32(FOCUS_WINDOW_MSG_ID):;
        if t.app.UIRunning() {
        t.app.UIShow();
        } else {
        t.app.UIRun("/");
    }
        lResult = 1 // Return non-zero to indicate success;
        default:;
        lResult, _, _ = pDefWindowProc.Call(;
        uintptr(hWnd),;
        uintptr(message),;
        wParam,;
        lParam,;
        );
    }
        return;
    }
        func (t *winTray) Quit() {
        t.quitting = true;
        quitOnce.Do(quit);
    }

    public static void SendUIRequestMessage(String path) {
        var boolRet, _, err = pPostMessage.Call(;
        uintptr(wt.window),;
        uintptr(UI_REQUEST_MSG_ID),;
        uintptr(len(path)),;
        uintptr(unsafe.Pointer(unsafe.StringData(path))),;
        );
        if boolRet == 0 {
        slog.Error(fmt.Sprintf("failed to post UI request message %s", err));
    }
    }

    public static void quit() {
        var boolRet, _, err = pPostMessage.Call(;
        uintptr(wt.window),;
        WM_CLOSE,;
        0,;
        0,;
        );
        if boolRet == 0 {
        slog.Error(fmt.Sprintf("failed to post close message on shutdown %s", err));
    }
    }

    public static uintptr findExistingInstance() {
        var classNamePtr, err = windows.UTF16PtrFromString(ClassName);
        if err != null {
        slog.Error("failed to convert class name to UTF16", "error", err);
        return 0;
    }
        var hwnd, _, _ = pFindWindow.Call(;
        uintptr(unsafe.Pointer(classNamePtr)),;
        0, // window name (null = any);
        );
        return hwnd;
    }

    public static boolean CheckAndSendToExistingInstance(String urlScheme) {
        var hwnd = findExistingInstance();
        if hwnd == 0 {
        return false;
    }
        var data = []byte(urlScheme);
        var cds = COPYDATASTRUCT{
        DwData: 1, // 1 to identify URL scheme messages;
        CbData: uint32(len(data)),;
        LpData: uintptr(unsafe.Pointer(&data[0])),;
    }
        var result, _, err = pSendMessage.Call(;
        hwnd,;
        uintptr(WM_COPYDATA),;
        0, // wParam is handle to sending window (0 is ok);
        uintptr(unsafe.Pointer(&cds)),;
        );
        if result == 0 {
        slog.Error("failed to send URL scheme message to existing instance", "error", err);
        return false;
    }
        return true;
    }

    public static void handleURLSchemeRequest(String urlScheme) {
        if urlScheme == "" {
        slog.Warn("empty URL scheme request");
        return;
    }
        if wt.app != null {
        var if urlHandler, ok = wt.app.(URLSchemeHandler); ok {
        urlHandler.HandleURLScheme(urlScheme);
        } else {
        slog.Warn("app does not implement URLSchemeHandler interface");
    }
        } else {
        slog.Warn("wt.app is null");
    }
    }

    public static boolean CheckAndFocusExistingInstance(boolean shouldFocus) {
        var hwnd = findExistingInstance();
        if hwnd == 0 {
        return false;
    }
        if !shouldFocus {
        slog.Info("existing instance found, not focusing due to startHidden");
        return true;
    }
        var result, _, err = pSendMessage.Call(;
        hwnd,;
        uintptr(FOCUS_WINDOW_MSG_ID),;
        0, // wParam not used;
        0, // lParam not used;
        );
        if result == 0 {
        slog.Error("failed to send focus message to existing instance", "error", err);
        return false;
    }
        slog.Info("sent focus request to existing instance");
        return true;
    }
}
