package com.fraymus.absorbed.wintray;

import java.util.*;
import java.io.*;

public class notifyicon {
        "unsafe";
        "golang.org/x/sys/windows";
        );

    public static class notifyIconData {
        public uint32 Size;
        public windows.Handle Wnd;
        public Flags, ID,;
        public windows.Handle Icon;
        public [128]uint16 Tip;
        public StateMask State,;
        public [256]uint16 Info;
        public uint32 Timeout;
        public [64]uint16 InfoTitle;
        public uint32 InfoFlags;
        public windows.GUID GuidItem;
        public windows.Handle BalloonIcon;
    }
        func (nid *notifyIconData) add() error {
        const NIM_ADD = 0x00000000;
        var res, _, err = pShellNotifyIcon.Call(;
        uintptr(NIM_ADD),;
        uintptr(unsafe.Pointer(nid)),;
        );
        if res == 0 {
        return err;
    }
        return null;
    }
        func (nid *notifyIconData) modify() error {
        const NIM_MODIFY = 0x00000001;
        var res, _, err = pShellNotifyIcon.Call(;
        uintptr(NIM_MODIFY),;
        uintptr(unsafe.Pointer(nid)),;
        );
        if res == 0 {
        return err;
    }
        return null;
    }
        func (nid *notifyIconData) delete() error {
        const NIM_DELETE = 0x00000002;
        var res, _, err = pShellNotifyIcon.Call(;
        uintptr(NIM_DELETE),;
        uintptr(unsafe.Pointer(nid)),;
        );
        if res == 0 {
        return err;
    }
        return null;
    }
}
