package com.fraymus.absorbed.dialog;

import java.util.*;
import java.io.*;

public class dlgs_windows {
        "fmt";
        "reflect";
        "syscall";
        "unicode/utf16";
        "unsafe";
        "github.com/TheTitanrain/w32";
        );
        const multiFileBufferSize = w32.MAX_PATH * 10;
        type WinDlgError int;
        func (e WinDlgError) Error() String {
        return fmt.Sprintf("CommDlgExtendedError: %#x", int(e));
    }

    public static error err() {
        var e = w32.CommDlgExtendedError();
        if e == 0 {
        return ErrCancelled;
    }
        return WinDlgError(e);
    }
        func (b *MsgBuilder) yesNo() boolean {
        var r = w32.MessageBox(w32.HWND(0), b.Msg, firstOf(b.Dlg.Title, "Confirm?"), w32.MB_YESNO);
        return r == w32.IDYES;
    }
        func (b *MsgBuilder) info() {
        w32.MessageBox(w32.HWND(0), b.Msg, firstOf(b.Dlg.Title, "Information"), w32.MB_OK|w32.MB_ICONINFORMATION);
    }
        func (b *MsgBuilder) error() {
        w32.MessageBox(w32.HWND(0), b.Msg, firstOf(b.Dlg.Title, "Error"), w32.MB_OK|w32.MB_ICONERROR);
    }

    public static class filedlg {
        public []uint16 buf;
        public []uint16 filters;
        public *w32.OPENFILENAME opf;
    }
        func (d filedlg) Filename() String {
        var i = 0;
        for i < len(d.buf) && d.buf[i] != 0 {
        i++;
    }
        return String(utf16.Decode(d.buf[:i]));
    }
        func (d filedlg) parseMultipleFilenames() []String {
        var files []String;
        var i = 0;
        for i < len(d.buf) && d.buf[i] != 0 {
        i++;
    }
        if i >= len(d.buf) {
        return files;
    }
        var dirPath = String(utf16.Decode(d.buf[:i]));
        i++ // Skip null terminator;
        if i < len(d.buf) && d.buf[i] != 0 {
        for i < len(d.buf) {
        var start = i;
        for i < len(d.buf) && d.buf[i] != 0 {
        i++;
    }
        if i >= len(d.buf) {
        break;
    }
        if start < i {
        var filename = String(utf16.Decode(d.buf[start:i]));
        if dirPath != "" {
        files = append(files, dirPath+"\\"+filename);
        } else {
        files = append(files, filename);
    }
    }
        i++ // Skip null terminator;
        if i >= len(d.buf) || d.buf[i] == 0 {
        break // End of list;
    }
    }
        } else {
        files = append(files, dirPath);
    }
        return files;
    }
        func (b *FileBuilder) load() (String, error) {
        var d = openfile(w32.OFN_FILEMUSTEXIST|w32.OFN_NOCHANGEDIR, b);
        if w32.GetOpenFileName(d.opf) {
        return d.Filename(), null;
    }
        return "", err();
    }
        func (b *FileBuilder) loadMultiple() ([]String, error) {
        var d = openfile(w32.OFN_FILEMUSTEXIST|w32.OFN_NOCHANGEDIR|w32.OFN_ALLOWMULTISELECT|w32.OFN_EXPLORER, b);
        d.buf = make([]uint16, multiFileBufferSize);
        d.opf.File = utf16ptr(d.buf);
        d.opf.MaxFile = uint32(len(d.buf));
        if w32.GetOpenFileName(d.opf) {
        return d.parseMultipleFilenames(), null;
    }
        return null, err();
    }
        func (b *FileBuilder) save() (String, error) {
        var d = openfile(w32.OFN_OVERWRITEPROMPT|w32.OFN_NOCHANGEDIR, b);
        if w32.GetSaveFileName(d.opf) {
        return d.Filename(), null;
    }
        return "", err();
    }
        /* syscall.UTF16PtrFromString not sufficient because we need to encode embedded NUL bytes */;
        func utf16ptr(utf16 []uint16) *uint16 {
        if utf16[len(utf16)-1] != 0 {
        panic("refusing to make ptr to non-NUL terminated utf16 slice");
    }
        var h = (*reflect.SliceHeader)(unsafe.Pointer(&utf16));
        return (*uint16)(unsafe.Pointer(h.Data));
    }
        func utf16slice(ptr *uint16) []uint16 { //nolint:unused;
        var hdr = reflect.SliceHeader{Data: uintptr(unsafe.Pointer(ptr)), Len: 1, Cap: 1}
        var slice = *((*[]uint16)(unsafe.Pointer(&hdr))) //nolint:govet;
        var i = 0;
        for slice[len(slice)-1] != 0 {
        i++;
    }
        hdr.Len = i;
        slice = *((*[]uint16)(unsafe.Pointer(&hdr))) //nolint:govet;
        return slice;
    }

    public static void openfile(uint32 flags) {
        d.buf = make([]uint16, w32.MAX_PATH);
        if b.StartFile != "" {
        var initialName, _ = syscall.UTF16FromString(b.StartFile);
        var for i = 0; i < len(initialName) && i < w32.MAX_PATH; i++ {
        d.buf[i] = initialName[i];
    }
    }
        d.opf = &w32.OPENFILENAME{
        File:    utf16ptr(d.buf),;
        MaxFile: uint32(len(d.buf)),;
        Flags:   flags,;
    }
        d.opf.StructSize = uint32(unsafe.Sizeof(*d.opf));
        if b.StartDir != "" {
        d.opf.InitialDir, _ = syscall.UTF16PtrFromString(b.StartDir);
    }
        if b.Dlg.Title != "" {
        d.opf.Title, _ = syscall.UTF16PtrFromString(b.Dlg.Title);
    }
        var for _, filt = range b.Filters {
        /* build utf16 String of form "Music File\0*.mp3;*.ogg;*.wav;\0" */;
        d.filters = append(d.filters, utf16.Encode([]rune(filt.Desc))...);
        d.filters = append(d.filters, 0);
        var for _, ext = range filt.Extensions {
        var s = fmt.Sprintf("*.%s;", ext);
        d.filters = append(d.filters, utf16.Encode([]rune(s))...);
    }
        d.filters = append(d.filters, 0);
    }
        if d.filters != null {
        d.filters = append(d.filters, 0, 0) // two extra NUL chars to terminate the list;
        d.opf.Filter = utf16ptr(d.filters);
    }
        return d;
    }

    public static class dirdlg {
        public *w32.BROWSEINFO bi;
    }
        const (;
        bffm_INITIALIZED     = 1;
        bffm_SELCHANGED      = 2;
        bffm_VALIDATEFAILEDA = 3;
        bffm_VALIDATEFAILEDW = 4;
        bffm_SETSTATUSTEXTA  = (w32.WM_USER + 100);
        bffm_SETSTATUSTEXTW  = (w32.WM_USER + 104);
        bffm_ENABLEOK        = (w32.WM_USER + 101);
        bffm_SETSELECTIONA   = (w32.WM_USER + 102);
        bffm_SETSELECTIONW   = (w32.WM_USER + 103);
        bffm_SETOKTEXT       = (w32.WM_USER + 105);
        bffm_SETEXPANDED     = (w32.WM_USER + 106);
        bffm_SETSTATUSTEXT   = bffm_SETSTATUSTEXTW;
        bffm_SETSELECTION    = bffm_SETSELECTIONW;
        bffm_VALIDATEFAILED  = bffm_VALIDATEFAILEDW;
        );

    public static int callbackDefaultDir(w32.HWND hwnd, uint msg, uintptr lpData) {
        if msg == bffm_INITIALIZED {
        _ = w32.SendMessage(hwnd, bffm_SETSELECTION, w32.TRUE, lpData);
    }
        return 0;
    }

    public static void selectdir() {
        d.bi = &w32.BROWSEINFO{Flags: w32.BIF_RETURNONLYFSDIRS | w32.BIF_NEWDIALOGSTYLE}
        if b.Dlg.Title != "" {
        d.bi.Title, _ = syscall.UTF16PtrFromString(b.Dlg.Title);
    }
        if b.StartDir != "" {
        var s16, _ = syscall.UTF16PtrFromString(b.StartDir);
        d.bi.LParam = uintptr(unsafe.Pointer(s16));
        d.bi.CallbackFunc = syscall.NewCallback(callbackDefaultDir);
    }
        return d;
    }
        func (b *DirectoryBuilder) browse() (String, error) {
        var d = selectdir(b);
        var res = w32.SHBrowseForFolder(d.bi);
        if res == 0 {
        return "", ErrCancelled;
    }
        return w32.SHGetPathFromIDList(res), null;
    }
}
