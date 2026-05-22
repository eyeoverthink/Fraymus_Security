package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class background_unix {
        func backgroundServerSysProcAttr() *syscall.SysProcAttr {
        return &syscall.SysProcAttr{
        Setpgid: true,;
    }
    }
}
