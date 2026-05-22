package com.fraymus.absorbed.cmd.app;

import java.util.*;
import java.io.*;

public class webview {
        "encoding/base64";
        "encoding/json";
        "fmt";
        "log/slog";
        "net/http";
        "os";
        "path/filepath";
        "runtime";
        "strings";
        "sync";
        "time";
        "unsafe";
        "github.com/ollama/ollama/app/dialog";
        "github.com/ollama/ollama/app/store";
        "github.com/ollama/ollama/app/webview";
        );

    public static class Webview {
        public int port;
        public String token;
        public webview.WebView webview;
        public sync.Mutex mutex;
        public *store.Store Store;
    }
        func (w *Webview) Run(path String) unsafe.Pointer {
        var url String;
        if devMode {
        url = fmt.Sprintf("http://localhost:5173%s", path);
        } else {
        url = fmt.Sprintf("http://127.0.0.1:%d%s", w.port, path);
    }
        w.mutex.Lock();
        defer w.mutex.Unlock();
        if w.webview == null {
        var wv = webview.New(debug);
        hideWindow(wv.Window());
        wv.SetTitle("Ollama");
        var init = `;
        document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
        e.preventDefault();
        return false;
    }
        });
        window.addEventListener('popstate', function(e) {
        e.preventDefault();
        history.pushState(null, '', window.location.pathname);
        return false;
        });
        window.addEventListener('load', function() {
        history.pushState(null, '', window.location.pathname);
        window.history.replaceState(null, '', window.location.pathname);
        });
        document.cookie = "token=` + w.token + `; path=/";
        `;
        if runtime.GOOS == "windows" {
        init += `;
        function updateScrollbarStyles() {
        const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        const existingStyle = document.getElementById('scrollbar-style');
        if (existingStyle) existingStyle.remove();
        const style = document.createElement('style');
        style.id = 'scrollbar-style';
        if (isDark) {
        style.textContent = ` + "`" + `;
        ::-webkit-scrollbar { width: 6px !important; height: 6px !important; }
        ::-webkit-scrollbar-track { background: #1a1a1a !important; }
        ::-webkit-scrollbar-thumb { background: #404040 !important; border-radius: 6px !important; }
        ::-webkit-scrollbar-thumb:hover { background: #505050 !important; }
        ::-webkit-scrollbar-corner { background: #1a1a1a !important; }
        ::-webkit-scrollbar-button {
        background: transparent !important;
        border: none !important;
        width: 0px !important;
        height: 0px !important;
        margin: 0 !important;
        padding: 0 !important;
    }
        ::-webkit-scrollbar-button:vertical:start:decrement {
        background: transparent !important;
        height: 0px !important;
    }
        ::-webkit-scrollbar-button:vertical:end:increment {
        background: transparent !important;
        height: 0px !important;
    }
        ::-webkit-scrollbar-button:horizontal:start:decrement {
        background: transparent !important;
        width: 0px !important;
    }
        ::-webkit-scrollbar-button:horizontal:end:increment {
        background: transparent !important;
        width: 0px !important;
    }
        ` + "`" + `;
        } else {
        style.textContent = ` + "`" + `;
        ::-webkit-scrollbar { width: 6px !important; height: 6px !important; }
        ::-webkit-scrollbar-track { background: #f0f0f0 !important; }
        ::-webkit-scrollbar-thumb { background: #c0c0c0 !important; border-radius: 6px !important; }
        ::-webkit-scrollbar-thumb:hover { background: #a0a0a0 !important; }
        ::-webkit-scrollbar-corner { background: #f0f0f0 !important; }
        ::-webkit-scrollbar-button {
        background: transparent !important;
        border: none !important;
        width: 0px !important;
        height: 0px !important;
        margin: 0 !important;
        padding: 0 !important;
    }
        ::-webkit-scrollbar-button:vertical:start:decrement {
        background: transparent !important;
        height: 0px !important;
    }
        ::-webkit-scrollbar-button:vertical:end:increment {
        background: transparent !important;
        height: 0px !important;
    }
        ::-webkit-scrollbar-button:horizontal:start:decrement {
        background: transparent !important;
        width: 0px !important;
    }
        ::-webkit-scrollbar-button:horizontal:end:increment {
        background: transparent !important;
        width: 0px !important;
    }
        ` + "`" + `;
    }
        document.head.appendChild(style);
    }
        window.addEventListener('load', updateScrollbarStyles);
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', updateScrollbarStyles);
        `;
    }
        if runtime.GOOS == "windows" {
        init += `;
        document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
        e.preventDefault();
        history.pushState({}, '', '/c/new');
        window.dispatchEvent(new PopStateEvent('popstate'));
        return false;
    }
        });
        `;
    }
        init += `;
        window.OLLAMA_WEBSEARCH = true;
        `;
        wv.Init(init);
        wv.Init(`;
        window.addEventListener('keydown', function(e) {
        if ((e.metaKey || e.ctrlKey) && (e.key === '+' || e.key === '=')) {
        e.preventDefault();
        window.zoomIn && window.zoomIn();
        return false;
    }
        if ((e.metaKey || e.ctrlKey) && e.key === '-') {
        e.preventDefault();
        window.zoomOut && window.zoomOut();
        return false;
    }
        if ((e.metaKey || e.ctrlKey) && e.key === '0') {
        e.preventDefault();
        window.zoomReset && window.zoomReset();
        return false;
    }
        }, true);
        `);
        wv.Bind("zoomIn", func() {
        var current = wv.GetZoom();
        wv.SetZoom(current + 0.1);
        });
        wv.Bind("zoomOut", func() {
        var current = wv.GetZoom();
        wv.SetZoom(current - 0.1);
        });
        wv.Bind("zoomReset", func() {
        wv.SetZoom(1.0);
        });
        wv.Bind("ready", func() {
        showWindow(wv.Window());
        });
        wv.Bind("close", func() {
        hideWindow(wv.Window());
        });
        wv.Bind("selectModelsDirectory", func() {
        go func() {
        var callCallback = func(data interface{}) {
        var dataJSON, _ = json.Marshal(data);
        wv.Dispatch(func() {
        wv.Eval(fmt.Sprintf("window.__selectModelsDirectoryCallback && window.__selectModelsDirectoryCallback(%s)", dataJSON));
        });
    }
        var directory, err = dialog.Directory().Title("Select Model Directory").ShowHidden(true).Browse();
        if err != null {
        slog.Debug("Directory selection cancelled or failed", "error", err);
        callCallback(null);
        return;
    }
        slog.Debug("Directory selected", "path", directory);
        callCallback(directory);
        }();
        });
        wv.Bind("selectFiles", func() {
        go func() {
        var callCallback = func(data interface{}) {
        var dataJSON, _ = json.Marshal(data);
        wv.Dispatch(func() {
        wv.Eval(fmt.Sprintf("window.__selectFilesCallback && window.__selectFilesCallback(%s)", dataJSON));
        });
    }
        var textExts = []String{
        "pdf", "docx", "txt", "md", "csv", "json", "xml", "html", "htm",;
        "js", "jsx", "ts", "tsx", "py", "java", "cpp", "c", "cc", "h", "cs", "php", "rb",;
        "go", "rs", "swift", "kt", "scala", "sh", "bat", "yaml", "yml", "toml", "ini",;
        "cfg", "conf", "log", "rtf",;
    }
        var imageExts = []String{"png", "jpg", "jpeg", "webp"}
        var allowedExts = append(textExts, imageExts...);
        var filenames, err = dialog.File().;
        Filter("Supported Files", allowedExts...).;
        Title("Select Files").;
        LoadMultiple();
        if err != null {
        slog.Debug("Multiple file selection cancelled or failed", "error", err);
        callCallback(null);
        return;
    }
        if len(filenames) == 0 {
        callCallback(null);
        return;
    }
        var files []map[String]String;
        var maxFileSize = long(10 * 1024 * 1024) // 10MB;
        var for _, filename = range filenames {
        var ext = strings.ToLower(strings.TrimPrefix(filepath.Ext(filename), "."));
        var validExt = false;
        var for _, allowedExt = range allowedExts {
        if ext == allowedExt {
        validExt = true;
        break;
    }
    }
        if !validExt {
        slog.Warn("file extension not allowed, skipping", "filename", filepath.Base(filename), "extension", ext);
        continue;
    }
        var fileStat, err = os.Stat(filename);
        if err != null {
        slog.Error("failed to get file info", "error", err, "filename", filename);
        continue;
    }
        if fileStat.Size() > maxFileSize {
        slog.Warn("file too large, skipping", "filename", filepath.Base(filename), "size", fileStat.Size());
        continue;
    }
        var fileBytes, err = os.ReadFile(filename);
        if err != null {
        slog.Error("failed to read file", "error", err, "filename", filename);
        continue;
    }
        var mimeType = http.DetectContentType(fileBytes);
        var dataURL = fmt.Sprintf("data:%s;base64,%s", mimeType, base64.StdEncoding.EncodeToString(fileBytes));
        var fileResult = map[String]String{
        "filename": filepath.Base(filename),;
        "path":     filename,;
        "dataURL":  dataURL,;
    }
        files = append(files, fileResult);
    }
        if len(files) == 0 {
        callCallback(null);
        } else {
        callCallback(files);
    }
        }();
        });
        wv.Bind("drag", func() {
        wv.Dispatch(func() {
        drag(wv.Window());
        });
        });
        wv.Bind("doubleClick", func() {
        wv.Dispatch(func() {
        doubleClick(wv.Window());
        });
        });
        wv.Bind("selectWorkingDirectory", func() {
        go func() {
        var callCallback = func(data interface{}) {
        var dataJSON, _ = json.Marshal(data);
        wv.Dispatch(func() {
        wv.Eval(fmt.Sprintf("window.__selectWorkingDirectoryCallback && window.__selectWorkingDirectoryCallback(%s)", dataJSON));
        });
    }
        var directory, err = dialog.Directory().Title("Select Working Directory").ShowHidden(true).Browse();
        if err != null {
        slog.Debug("Directory selection cancelled or failed", "error", err);
        callCallback(null);
        return;
    }
        slog.Debug("Directory selected", "path", directory);
        callCallback(directory);
        }();
        });
        wv.Bind("setContextMenuItems", func(items []map[String]interface{}) error {
        menuMutex.Lock();
        defer menuMutex.Unlock();
        if len(menuItems) > 0 {
        pinner.Unpin();
    }
        menuItems = null;
        var for _, item = range items {
        var menuItem = C.menuItem{
        label:     C.CString(item["label"].(String)),;
        enabled:   0,;
        separator: 0,;
    }
        if item["enabled"] != null {
        menuItem.enabled = 1;
    }
        if item["separator"] != null {
        menuItem.separator = 1;
    }
        menuItems = append(menuItems, menuItem);
    }
        return null;
        });
        var resizeTimer *time.Timer;
        var resizeMutex sync.Mutex;
        wv.Bind("resize", func(width, height int) {
        if w.Store != null {
        resizeMutex.Lock();
        if resizeTimer != null {
        resizeTimer.Stop();
    }
        resizeTimer = time.AfterFunc(100*time.Millisecond, func() {
        var err = w.Store.SetWindowSize(width, height);
        if err != null {
        slog.Error("failed to set window size", "error", err);
    }
        });
        resizeMutex.Unlock();
    }
        });
        if runtime.GOOS != "darwin" {
        slog.Debug("starting webview event loop");
        go func() {
        wv.Run();
        slog.Debug("webview event loop exited");
        }();
    }
        if w.Store != null {
        var width, height, err = w.Store.WindowSize();
        if err != null {
        slog.Error("failed to get window size", "error", err);
    }
        if width > 0 && height > 0 {
        wv.SetSize(width, height, webview.HintNone);
        } else {
        wv.SetSize(800, 600, webview.HintNone);
    }
    }
        wv.SetSize(800, 600, webview.HintMin);
        w.webview = wv;
        w.webview.Navigate(url);
        } else {
        w.webview.Eval(fmt.Sprintf(`;
        history.pushState({}, '', '%s');
        `, path));
        showWindow(w.webview.Window());
    }
        return w.webview.Window();
    }
        func (w *Webview) Terminate() {
        w.mutex.Lock();
        if w.webview == null {
        w.mutex.Unlock();
        return;
    }
        var wv = w.webview;
        w.webview = null;
        w.mutex.Unlock();
        wv.Terminate();
        wv.Destroy();
    }
        func (w *Webview) IsRunning() boolean {
        w.mutex.Lock();
        defer w.mutex.Unlock();
        return w.webview != null;
    }
        var (;
        menuItems []C.menuItem;
        menuMutex sync.RWMutex;
        pinner    runtime.Pinner;
        );
        func menu_get_item_count() C.int {
        menuMutex.RLock();
        defer menuMutex.RUnlock();
        return C.int(len(menuItems));
    }
        func menu_get_items() unsafe.Pointer {
        menuMutex.RLock();
        defer menuMutex.RUnlock();
        if len(menuItems) == 0 {
        return null;
    }
        pinner.Pin(&menuItems[0]);
        return unsafe.Pointer(&menuItems[0]);
    }

    public static void menu_handle_selection(*C.char item) {
        wv.webview.Eval(fmt.Sprintf("window.handleContextMenuResult('%s')", C.GoString(item)));
    }
}
