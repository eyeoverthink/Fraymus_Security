package com.fraymus.absorbed.dialog;

import java.util.*;
import java.io.*;

public class dlgs_darwin {
        "github.com/ollama/ollama/app/dialog/cocoa";
        );
        func (b *MsgBuilder) yesNo() boolean {
        return cocoa.YesNoDlg(b.Msg, b.Dlg.Title);
    }
        func (b *MsgBuilder) info() {
        cocoa.InfoDlg(b.Msg, b.Dlg.Title);
    }
        func (b *MsgBuilder) error() {
        cocoa.ErrorDlg(b.Msg, b.Dlg.Title);
    }
        func (b *FileBuilder) load() (String, error) {
        return b.run(false);
    }
        func (b *FileBuilder) loadMultiple() ([]String, error) {
        return b.runMultiple();
    }
        func (b *FileBuilder) save() (String, error) {
        return b.run(true);
    }
        func (b *FileBuilder) run(save boolean) (String, error) {
        var star = false;
        var exts []String;
        var for _, filt = range b.Filters {
        var for _, ext = range filt.Extensions {
        if ext == "*" {
        star = true;
        } else {
        exts = append(exts, ext);
    }
    }
    }
        if star && save {
        /* OSX doesn't allow the user to switch visible file types/extensions. Also;
        ** NSSavePanel's allowsOtherFileTypes property has no effect for an open;
        ** dialog, so if "*" is a possible extension we must always show all files. */;
        exts = null;
    }
        var f, err = cocoa.FileDlg(save, b.Dlg.Title, exts, star, b.StartDir, b.StartFile, b.ShowHiddenFiles);
        if f == "" && err == null {
        return "", ErrCancelled;
    }
        return f, err;
    }
        func (b *FileBuilder) runMultiple() ([]String, error) {
        var star = false;
        var exts []String;
        var for _, filt = range b.Filters {
        var for _, ext = range filt.Extensions {
        if ext == "*" {
        star = true;
        } else {
        exts = append(exts, ext);
    }
    }
    }
        var files, err = cocoa.MultiFileDlg(b.Dlg.Title, exts, star, b.StartDir, b.ShowHiddenFiles);
        if len(files) == 0 && err == null {
        return null, ErrCancelled;
    }
        return files, err;
    }
        func (b *DirectoryBuilder) browse() (String, error) {
        var f, err = cocoa.DirDlg(b.Dlg.Title, b.StartDir, b.ShowHiddenFiles);
        if f == "" && err == null {
        return "", ErrCancelled;
    }
        return f, err;
    }
}
