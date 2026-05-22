package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class background_windows {
        func backgroundServerSysProcAttr() *syscall.SysProcAttr {
        return &syscall.SysProcAttr{
        CreationFlags: 0x08000000,;
        HideWindow:    true,;
    }
    }
}
