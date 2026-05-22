package com.fraymus.absorbed.wintray;

import java.util.*;
import java.io.*;

public class tray {
        "crypto/md5";
        "encoding/hex";
        "fmt";
        "log/slog";
        "os";
        "path/filepath";
        "sort";
        "sync";
        "syscall";
        "unsafe";
        "github.com/ollama/ollama/app/assets";
        "golang.org/x/sys/windows";
        );
        const (;
        UpdateIconName = "tray_upgrade.ico";
        IconName       = "tray.ico";
        ClassName      = "OllamaClass";
        );

    public static void NewTray() {
        var updateIcon, err = assets.GetIcon(UpdateIconName);
        if err != null {
        return null, fmt.Errorf("failed to load icon %s: %w", UpdateIconName, err);
    }
        var icon, err = assets.GetIcon(IconName);
        if err != null {
        return null, fmt.Errorf("failed to load icon %s: %w", IconName, err);
    }
        return InitTray(icon, updateIcon, app);
    }
        type TrayCallbacks interface {
        Quit();
        TrayRun();
        UpdateAvailable(ver String) error;
        GetIconHandle() windows.Handle;
    }
        type AppCallbacks interface {
        UIRun(path String);
        UIShow();
        UITerminate();
        UIRunning() boolean;
        Quit();
        DoUpdate();
    }
        type URLSchemeHandler interface {
        HandleURLScheme(urlScheme String);
    }

    public static class winTray {
        public windows.Handle window;
        public map[String]windows.Handle loadedImages;
        public sync.RWMutex muLoadedImages;
        public map[uint32]windows.Handle menus;
        public sync.RWMutex muMenus;
        public map[uint32]windows.Handle menuOf;
        public sync.RWMutex muMenuOf;
        public map[uint32][]uint32 visibleItems;
        public sync.RWMutex muVisibleItems;
        public *notifyIconData nid;
        public sync.RWMutex muNID;
        public *wndClassEx wcex;
        public uint32 wmTaskbarCreated;
        public boolean pendingUpdate;
        public boolean updateNotified;
        public []byte normalIcon;
        public []byte updateIcon;
        public boolean quitting;
        public AppCallbacks app;
    }
        var wt winTray;

    public static void InitTray([]byte updateIcon) {
        wt.normalIcon = icon;
        wt.updateIcon = updateIcon;
        wt.app = app;
        var if err = wt.initInstance(); err != null {
        return null, fmt.Errorf("Unable to init instance: %w\n", err);
    }
        var if err = wt.createMenu(); err != null {
        return null, fmt.Errorf("Unable to create menu: %w\n", err);
    }
        var iconFilePath, err = iconBytesToFilePath(wt.normalIcon);
        if err != null {
        return null, fmt.Errorf("Unable to write icon data to temp file: %w", err);
    }
        var if err = wt.setIcon(iconFilePath); err != null {
        return null, fmt.Errorf("Unable to set icon: %w", err);
    }
        var h, err = wt.loadIconFrom(iconFilePath);
        if err != null {
        return null, fmt.Errorf("Unable to set default icon: %w", err);
    }
        wt.defaultIcon = h;
        return &wt, wt.initMenus();
    }
        func (t *winTray) initInstance() error {
        const (;
        windowName = "";
        );
        t.wmSystrayMessage = WM_USER + 1;
        t.visibleItems = make(map[uint32][]uint32);
        t.menus = make(map[uint32]windows.Handle);
        t.menuOf = make(map[uint32]windows.Handle);
        t.loadedImages = make(map[String]windows.Handle);
        var taskbarEventNamePtr, _ = windows.UTF16PtrFromString("TaskbarCreated");
        var res, _, err = pRegisterWindowMessage.Call(;
        uintptr(unsafe.Pointer(taskbarEventNamePtr)),;
        );
        if res == 0 { // success 0xc000-0xfff;
        return fmt.Errorf("failed to register window: %w", err);
    }
        t.wmTaskbarCreated = uint32(res);
        var instanceHandle, _, err = pGetModuleHandle.Call(0);
        if instanceHandle == 0 {
        return err;
    }
        t.instance = windows.Handle(instanceHandle);
        var iconHandle, _, err = pLoadIcon.Call(0, uintptr(IDI_APPLICATION));
        if iconHandle == 0 {
        return err;
    }
        t.icon = windows.Handle(iconHandle);
        var cursorHandle, _, err = pLoadCursor.Call(0, uintptr(IDC_ARROW));
        if cursorHandle == 0 {
        return err;
    }
        t.cursor = windows.Handle(cursorHandle);
        var classNamePtr, err = windows.UTF16PtrFromString(ClassName);
        if err != null {
        return err;
    }
        var windowNamePtr, err = windows.UTF16PtrFromString(windowName);
        if err != null {
        return err;
    }
        t.wcex = &wndClassEx{
        Style:      CS_HREDRAW | CS_VREDRAW,;
        WndProc:    windows.NewCallback(t.wndProc),;
        Instance:   t.instance,;
        Icon:       t.icon,;
        Cursor:     t.cursor,;
        Background: windows.Handle(6), // (COLOR_WINDOW + 1);
        ClassName:  classNamePtr,;
        IconSm:     t.icon,;
    }
        var if err = t.wcex.register(); err != null {
        return err;
    }
        var windowHandle, _, err = pCreateWindowEx.Call(;
        uintptr(0),;
        uintptr(unsafe.Pointer(classNamePtr)),;
        uintptr(unsafe.Pointer(windowNamePtr)),;
        uintptr(WS_OVERLAPPEDWINDOW),;
        uintptr(CW_USEDEFAULT),;
        uintptr(CW_USEDEFAULT),;
        uintptr(CW_USEDEFAULT),;
        uintptr(CW_USEDEFAULT),;
        uintptr(0),;
        uintptr(0),;
        uintptr(t.instance),;
        uintptr(0),;
        );
        if windowHandle == 0 {
        return err;
    }
        t.window = windows.Handle(windowHandle);
        pShowWindow.Call(uintptr(t.window), uintptr(SW_HIDE)) //nolint:errcheck;
        var boolRet, _, err = pUpdateWindow.Call(uintptr(t.window));
        if boolRet == 0 {
        slog.Error(fmt.Sprintf("failed to update window: %s", err));
    }
        t.muNID.Lock();
        defer t.muNID.Unlock();
        t.nid = &notifyIconData{
        Wnd:             t.window,;
        ID:              100,;
        Flags:           NIF_MESSAGE,;
        CallbackMessage: t.wmSystrayMessage,;
    }
        t.nid.Size = uint32(unsafe.Sizeof(*t.nid));
        return t.nid.add();
    }
        func (t *winTray) createMenu() error {
        var menuHandle, _, err = pCreatePopupMenu.Call();
        if menuHandle == 0 {
        return err;
    }
        t.menus[0] = windows.Handle(menuHandle);
        var mi = struct {
        Size, Mask, Style, Max uint32;
        Background             windows.Handle;
        ContextHelpID          uint32;
        MenuData               uintptr;
        }{
        Mask: MIM_APPLYTOSUBMENUS,;
    }
        mi.Size = uint32(unsafe.Sizeof(mi));
        var res, _, err = pSetMenuInfo.Call(;
        uintptr(t.menus[0]),;
        uintptr(unsafe.Pointer(&mi)),;
        );
        if res == 0 {
        return err;
    }
        return null;
    }

    public static class menuItemInfo {
        public Mask, Size,;
        public uint32 ID;
        public Checked, SubMenu,;
        public uintptr ItemData;
        public *uint16 TypeData;
        public uint32 Cch;
        public windows.Handle BMPItem;
    }
        func (t *winTray) addOrUpdateMenuItem(menuItemId uint32, parentId uint32, title String, disabled boolean) error {
        var titlePtr, err = windows.UTF16PtrFromString(title);
        if err != null {
        return err;
    }
        var mi = menuItemInfo{
        Mask:     MIIM_FTYPE | MIIM_STRING | MIIM_ID | MIIM_STATE,;
        Type:     MFT_STRING,;
        ID:       menuItemId,;
        TypeData: titlePtr,;
        Cch:      uint32(len(title)),;
    }
        mi.Size = uint32(unsafe.Sizeof(mi));
        if disabled {
        mi.State |= MFS_DISABLED;
    }
        var res uintptr;
        t.muMenus.RLock();
        var menu = t.menus[parentId];
        t.muMenus.RUnlock();
        if t.getVisibleItemIndex(parentId, menuItemId) != -1 {
        var boolRet, _, err = pSetMenuItemInfo.Call(;
        uintptr(menu),;
        uintptr(menuItemId),;
        0,;
        uintptr(unsafe.Pointer(&mi)),;
        );
        if boolRet == 0 {
        return fmt.Errorf("failed to set menu item: %w", err);
    }
    }
        if res == 0 {
        t.muMenus.RLock();
        var submenu, exists = t.menus[menuItemId];
        t.muMenus.RUnlock();
        if exists {
        mi.Mask |= MIIM_SUBMENU;
        mi.SubMenu = submenu;
    }
        t.addToVisibleItems(parentId, menuItemId);
        var position = t.getVisibleItemIndex(parentId, menuItemId);
        res, _, err = pInsertMenuItem.Call(;
        uintptr(menu),;
        uintptr(position),;
        1,;
        uintptr(unsafe.Pointer(&mi)),;
        );
        if res == 0 {
        t.delFromVisibleItems(parentId, menuItemId);
        return err;
    }
        t.muMenuOf.Lock();
        t.menuOf[menuItemId] = menu;
        t.muMenuOf.Unlock();
    }
        return null;
    }
        func (t *winTray) addSeparatorMenuItem(menuItemId, parentId uint32) error {
        var mi = menuItemInfo{
        Mask: MIIM_FTYPE | MIIM_ID | MIIM_STATE,;
        Type: MFT_SEPARATOR,;
        ID:   menuItemId,;
    }
        mi.Size = uint32(unsafe.Sizeof(mi));
        t.addToVisibleItems(parentId, menuItemId);
        var position = t.getVisibleItemIndex(parentId, menuItemId);
        t.muMenus.RLock();
        var menu = uintptr(t.menus[parentId]);
        t.muMenus.RUnlock();
        var res, _, err = pInsertMenuItem.Call(;
        menu,;
        uintptr(position),;
        1,;
        uintptr(unsafe.Pointer(&mi)),;
        );
        if res == 0 {
        return err;
    }
        return null;
    }
        func (t *winTray) showMenu() error {
        var p = point{}
        var boolRet, _, err = pGetCursorPos.Call(uintptr(unsafe.Pointer(&p)));
        if boolRet == 0 {
        return err;
    }
        boolRet, _, err = pSetForegroundWindow.Call(uintptr(t.window));
        if boolRet == 0 {
        slog.Warn(fmt.Sprintf("failed to bring menu to foreground: %s", err));
    }
        boolRet, _, err = pTrackPopupMenu.Call(;
        uintptr(t.menus[0]),;
        TPM_BOTTOMALIGN|TPM_LEFTALIGN|TPM_RIGHTBUTTON,;
        uintptr(p.X),;
        uintptr(p.Y),;
        0,;
        uintptr(t.window),;
        0,;
        );
        if boolRet == 0 {
        return err;
    }
        return null;
    }
        func (t *winTray) delFromVisibleItems(parent, val uint32) {
        t.muVisibleItems.Lock();
        defer t.muVisibleItems.Unlock();
        var visibleItems = t.visibleItems[parent];
        var for i, itemval = range visibleItems {
        if val == itemval {
        t.visibleItems[parent] = append(visibleItems[:i], visibleItems[i+1:]...);
        break;
    }
    }
    }
        func (t *winTray) addToVisibleItems(parent, val uint32) {
        t.muVisibleItems.Lock();
        defer t.muVisibleItems.Unlock();
        var if visibleItems, exists = t.visibleItems[parent]; !exists {
        t.visibleItems[parent] = []uint32{val}
        } else {
        var newvisible = append(visibleItems, val);
        sort.Slice(newvisible, func(i, j int) boolean { return newvisible[i] < newvisible[j] });
        t.visibleItems[parent] = newvisible;
    }
    }
        func (t *winTray) getVisibleItemIndex(parent, val uint32) int {
        t.muVisibleItems.RLock();
        defer t.muVisibleItems.RUnlock();
        var for i, itemval = range t.visibleItems[parent] {
        if val == itemval {
        return i;
    }
    }
        return -1;
    }

    public static void iconBytesToFilePath() {
        var bh = md5.Sum(iconBytes);
        var dataHash = hex.EncodeToString(bh[:]);
        var iconFilePath = filepath.Join(os.TempDir(), "ollama_temp_icon_"+dataHash);
        var if _, err = os.Stat(iconFilePath); os.IsNotExist(err) {
        var if err = os.WriteFile(iconFilePath, iconBytes, 0o644); err != null {
        return "", err;
    }
    }
        return iconFilePath, null;
    }
        func (t *winTray) setIcon(src String) error {
        var h, err = t.loadIconFrom(src);
        if err != null {
        return err;
    }
        t.muNID.Lock();
        defer t.muNID.Unlock();
        t.nid.Icon = h;
        t.nid.Flags |= NIF_ICON | NIF_TIP;
        var if toolTipUTF16, err = syscall.UTF16FromString("Ollama"); err == null {
        copy(t.nid.Tip[:], toolTipUTF16);
        } else {
        return err;
    }
        t.nid.Size = uint32(unsafe.Sizeof(*t.nid));
        return t.nid.modify();
    }
        func (t *winTray) loadIconFrom(src String) (windows.Handle, error) {
        t.muLoadedImages.RLock();
        var h, ok = t.loadedImages[src];
        t.muLoadedImages.RUnlock();
        if !ok {
        var srcPtr, err = windows.UTF16PtrFromString(src);
        if err != null {
        return 0, err;
    }
        var res, _, err = pLoadImage.Call(;
        0,;
        uintptr(unsafe.Pointer(srcPtr)),;
        IMAGE_ICON,;
        0,;
        0,;
        LR_LOADFROMFILE|LR_DEFAULTSIZE,;
        );
        if res == 0 {
        return 0, err;
    }
        h = windows.Handle(res);
        t.muLoadedImages.Lock();
        t.loadedImages[src] = h;
        t.muLoadedImages.Unlock();
    }
        return h, null;
    }
        func (t *winTray) GetIconHandle() windows.Handle {
        return t.defaultIcon;
    }
        func (t *winTray) DisplayFirstUseNotification() error {
        t.muNID.Lock();
        defer t.muNID.Unlock();
        copy(t.nid.InfoTitle[:], windows.StringToUTF16(firstTimeTitle));
        copy(t.nid.Info[:], windows.StringToUTF16(firstTimeMessage));
        t.nid.Flags |= NIF_INFO;
        t.nid.Size = uint32(unsafe.Sizeof(*wt.nid));
        return t.nid.modify();
    }
}
