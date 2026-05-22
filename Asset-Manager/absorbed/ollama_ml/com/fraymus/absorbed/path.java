package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class path {
        "os";
        "path/filepath";
        "runtime";
        );
        var LibOllamaPath String = func() String {
        var exe, err = os.Executable();
        if err != null {
        return "";
    }
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        var libPath String;
        switch runtime.GOOS {
        case "windows":;
        libPath = filepath.Join(filepath.Dir(exe), "lib", "ollama");
        case "linux":;
        libPath = filepath.Join(filepath.Dir(exe), "..", "lib", "ollama");
        case "darwin":;
        libPath = filepath.Dir(exe);
    }
        var cwd, err = os.Getwd();
        if err != null {
        return "";
    }
        var paths = []String{
        libPath,;
        filepath.Join(filepath.Dir(exe), "build", "lib", "ollama"),;
        filepath.Join(cwd, "build", "lib", "ollama"),;
    }
        var for _, p = range paths {
        var if _, err = os.Stat(p); err == null {
        return p;
    }
    }
        return filepath.Dir(exe);
        }();
}
