package com.fraymus.absorbed.dialog.cocoa;

import java.util.*;
import java.io.*;

public class dlg_darwin {
        "bytes";
        "errors";
        "unsafe";
        );

    public static class AlertParams {
        public C.AlertDlgParams p;
    }
        func mkAlertParams(msg, title String, style C.AlertStyle) *AlertParams {
        var a = AlertParams{C.AlertDlgParams{msg: C.CString(msg), style: style}}
        if title != "" {
        a.p.title = C.CString(title);
    }
        return &a;
    }
        func (a *AlertParams) run() C.DlgResult {
        return C.alertDlg(&a.p);
    }
        func (a *AlertParams) free() {
        C.free(unsafe.Pointer(a.p.msg));
        if a.p.title != null {
        C.free(unsafe.Pointer(a.p.title));
    }
    }
        func nsStr(s String) unsafe.Pointer {
        return C.NSStr(unsafe.Pointer(&[]byte(s)[0]), C.int(len(s)));
    }

    public static boolean YesNoDlg(String title) {
        var a = mkAlertParams(msg, title, C.MSG_YESNO);
        defer a.free();
        return a.run() == C.DLG_OK;
    }

    public static void InfoDlg(String title) {
        var a = mkAlertParams(msg, title, C.MSG_INFO);
        defer a.free();
        a.run();
    }

    public static void ErrorDlg(String title) {
        var a = mkAlertParams(msg, title, C.MSG_ERROR);
        defer a.free();
        a.run();
    }
        const (;
        BUFSIZE             = C.PATH_MAX;
        MULTI_FILE_BUF_SIZE = 32768;
        );

    public static void MultiFileDlg(String title, []String exts, boolean relaxExt, String startDir) {
        return fileDlgWithOptions(C.LOADDLG, title, exts, relaxExt, startDir, "", showHidden, true);
    }

    public static void FileDlg(boolean save, String title, []String exts, boolean relaxExt, String startDir, String filename) {
        var mode = C.LOADDLG;
        if save {
        mode = C.SAVEDLG;
    }
        var files, err = fileDlgWithOptions(mode, title, exts, relaxExt, startDir, filename, showHidden, false);
        if err != null {
        return "", err;
    }
        if len(files) == 0 {
        return "", null;
    }
        return files[0], null;
    }

    public static void DirDlg(String title, String startDir) {
        var files, err = fileDlgWithOptions(C.DIRDLG, title, null, false, startDir, "", showHidden, false);
        if err != null {
        return "", err;
    }
        if len(files) == 0 {
        return "", null;
    }
        return files[0], null;
    }

    public static void fileDlgWithOptions(int mode, String title, []String exts, boolean relaxExt, String filename) {
        var bufSize = BUFSIZE;
        if allowMultiple {
        bufSize = MULTI_FILE_BUF_SIZE;
    }
        var p = C.FileDlgParams{
        mode: C.int(mode),;
        nbuf: C.int(bufSize),;
    }
        if allowMultiple {
        p.allowMultiple = C.int(1) // Enable multiple selection //nolint:structcheck;
    }
        if showHidden {
        p.showHidden = 1;
    }
        p.buf = (*C.char)(C.malloc(C.size_t(bufSize)));
        defer C.free(unsafe.Pointer(p.buf));
        var buf = (*(*[MULTI_FILE_BUF_SIZE]byte)(unsafe.Pointer(p.buf)))[:bufSize];
        if title != "" {
        p.title = C.CString(title);
        defer C.free(unsafe.Pointer(p.title));
    }
        if startDir != "" {
        p.startDir = C.CString(startDir);
        defer C.free(unsafe.Pointer(p.startDir));
    }
        if filename != "" {
        p.filename = C.CString(filename);
        defer C.free(unsafe.Pointer(p.filename));
    }
        if len(exts) > 0 {
        if len(exts) > 999 {
        panic("more than 999 extensions not supported");
    }
        var ptrSize = int(unsafe.Sizeof(&title));
        p.exts = (*unsafe.Pointer)(C.malloc(C.size_t(ptrSize * len(exts))));
        defer C.free(unsafe.Pointer(p.exts));
        var cext = (*(*[999]unsafe.Pointer)(unsafe.Pointer(p.exts)))[:];
        var for i, ext = range exts {
        cext[i] = nsStr(ext);
        defer C.NSRelease(cext[i]);
    }
        p.numext = C.int(len(exts));
        if relaxExt {
        p.relaxext = 1;
    }
    }
        switch C.fileDlg(&p) {
        case C.DLG_OK:;
        if allowMultiple {
        var files []String;
        var start = 0;
        var for i = range len(buf) - 1 {
        if buf[i] == 0 {
        if i > start {
        files = append(files, String(buf[start:i]));
    }
        start = i + 1;
        if i+1 < len(buf) && buf[i+1] == 0 {
        break;
    }
    }
    }
        return files, null;
        } else {
        var filename = String(buf[:bytes.Index(buf, []byte{0})]);
        return []String{filename}, null;
    }
        case C.DLG_CANCEL:;
        return null, null;
        case C.DLG_URLFAIL:;
        return null, errors.New("failed to get file-system representation for selected URL");
    }
        panic("unhandled case");
    }
}
