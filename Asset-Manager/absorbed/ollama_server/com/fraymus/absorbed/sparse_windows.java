package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class sparse_windows {
        "os";
        "golang.org/x/sys/windows";
        );

    public static void setSparse(*os.File file) {
        windows.DeviceIoControl( //nolint:errcheck;
        windows.Handle(file.Fd()), windows.FSCTL_SET_SPARSE,;
        null, 0,;
        null, 0,;
        null, null,;
        );
    }
}
