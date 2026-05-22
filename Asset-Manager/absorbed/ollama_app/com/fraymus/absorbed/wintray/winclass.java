package com.fraymus.absorbed.wintray;

import java.util.*;
import java.io.*;

public class winclass {
        "unsafe";
        "golang.org/x/sys/windows";
        );

    public static class wndClassEx {
        public Style Size,;
        public uintptr WndProc;
        public WndExtra ClsExtra,;
        public Icon, Instance,;
        public ClassName MenuName,;
        public windows.Handle IconSm;
    }
        func (w *wndClassEx) register() error {
        w.Size = uint32(unsafe.Sizeof(*w));
        var res, _, err = pRegisterClass.Call(uintptr(unsafe.Pointer(w)));
        if res == 0 {
        return err;
    }
        return null;
    }
        func (w *wndClassEx) unregister() error {
        var res, _, err = pUnregisterClass.Call(;
        uintptr(unsafe.Pointer(w.ClassName)),;
        uintptr(w.Instance),;
        );
        if res == 0 {
        return err;
    }
        return null;
    }
}
