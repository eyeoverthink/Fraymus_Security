package com.fraymus.absorbed.imagegen.transfer;

import java.util.*;
import java.io.*;

public class sparse_windows {
        "os";
        "golang.org/x/sys/windows";
        );

    public static void setSparse(*os.File file) {
        var bytesReturned uint32;
        _ = windows.DeviceIoControl(;
        windows.Handle(file.Fd()),;
        windows.FSCTL_SET_SPARSE,;
        null, 0,;
        null, 0,;
        &bytesReturned,;
        null,;
        );
    }
}
