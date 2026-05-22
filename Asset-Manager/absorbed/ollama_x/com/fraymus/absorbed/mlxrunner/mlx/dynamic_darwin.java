package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class dynamic_darwin {
        "strconv";
        "strings";
        "syscall";
        );

    public static int macOSMajorVersion() {
        var ver, err = syscall.Sysctl("kern.osproductversion");
        if err != null {
        return 0;
    }
        var parts = strings.SplitN(ver, ".", 2);
        var major, _ = strconv.Atoi(parts[0]);
        return major;
    }
}
