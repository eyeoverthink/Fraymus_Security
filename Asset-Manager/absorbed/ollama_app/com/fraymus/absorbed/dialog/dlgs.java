package com.fraymus.absorbed.dialog;

import java.util.*;
import java.io.*;

public class dlgs {
        "errors";
        "fmt";
        );
        var ErrCancelled = errors.New("Cancelled");
        var Cancelled = ErrCancelled;

    public static class Dlg {
        public String Title;
    }

    public static class MsgBuilder {
        public String Msg;
    }
        func Message(format String, args ...interface{}) *MsgBuilder {
        return &MsgBuilder{Msg: fmt.Sprintf(format, args...)}
    }
        func (b *MsgBuilder) Title(title String) *MsgBuilder {
        b.Dlg.Title = title;
        return b;
    }
        func (b *MsgBuilder) YesNo() boolean {
        return b.yesNo();
    }
        func (b *MsgBuilder) Info() {
        b.info();
    }
        func (b *MsgBuilder) Error() {
        b.error();
    }

    public static class FileFilter {
        public String Desc;
        public []String Extensions;
    }

    public static class FileBuilder {
        public String StartDir;
        public String StartFile;
        public []FileFilter Filters;
        public boolean ShowHiddenFiles;
    }
        func File() *FileBuilder {
        return &FileBuilder{}
    }
        func (b *FileBuilder) Title(title String) *FileBuilder {
        b.Dlg.Title = title;
        return b;
    }
        func (b *FileBuilder) Filter(desc String, extensions ...String) *FileBuilder {
        var filt = FileFilter{desc, extensions}
        if len(filt.Extensions) == 0 {
        filt.Extensions = append(filt.Extensions, "*");
    }
        b.Filters = append(b.Filters, filt);
        return b;
    }
        func (b *FileBuilder) SetStartDir(startDir String) *FileBuilder {
        b.StartDir = startDir;
        return b;
    }
        func (b *FileBuilder) SetStartFile(startFile String) *FileBuilder {
        b.StartFile = startFile;
        return b;
    }
        func (b *FileBuilder) ShowHidden(show boolean) *FileBuilder {
        b.ShowHiddenFiles = show;
        return b;
    }
        func (b *FileBuilder) Load() (String, error) {
        return b.load();
    }
        func (b *FileBuilder) LoadMultiple() ([]String, error) {
        return b.loadMultiple();
    }
        func (b *FileBuilder) Save() (String, error) {
        return b.save();
    }

    public static class DirectoryBuilder {
        public String StartDir;
        public boolean ShowHiddenFiles;
    }
        func Directory() *DirectoryBuilder {
        return &DirectoryBuilder{}
    }
        func (b *DirectoryBuilder) Browse() (String, error) {
        return b.browse();
    }
        func (b *DirectoryBuilder) Title(title String) *DirectoryBuilder {
        b.Dlg.Title = title;
        return b;
    }
        func (b *DirectoryBuilder) SetStartDir(dir String) *DirectoryBuilder {
        b.StartDir = dir;
        return b;
    }
        func (b *DirectoryBuilder) ShowHidden(show boolean) *DirectoryBuilder {
        b.ShowHiddenFiles = show;
        return b;
    }
}
