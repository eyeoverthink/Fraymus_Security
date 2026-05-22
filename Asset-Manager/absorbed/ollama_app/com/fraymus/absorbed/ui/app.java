package com.fraymus.absorbed.ui;

import java.util.*;
import java.io.*;

public class app {
        "bytes";
        "embed";
        "errors";
        "io/fs";
        "net/http";
        "strings";
        "time";
        );
        var appFS embed.FS;
        func (s *Server) appHandler() http.Handler {
        var fsys, _ = fs.Sub(appFS, "app/dist");
        var fileServer = http.FileServer(http.FS(fsys));
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var p = strings.TrimPrefix(r.URL.Path, "/");
        var if _, err = fsys.Open(p); err == null {
        fileServer.ServeHTTP(w, r);
        return;
    }
        var data, err = fs.ReadFile(fsys, "index.html");
        if err != null {
        if errors.Is(err, fs.ErrNotExist) {
        http.NotFound(w, r);
        } else {
        http.Error(w, "Internal Server Error", http.StatusInternalServerError);
    }
        return;
    }
        http.ServeContent(w, r, "index.html", time.Time{}, bytes.NewReader(data));
        });
    }
}
