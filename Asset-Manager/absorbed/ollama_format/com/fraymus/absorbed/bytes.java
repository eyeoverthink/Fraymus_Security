package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class bytes {
        "fmt";
        "math";
        );
        const (;
        Byte = 1;
        KiloByte = Byte * 1000;
        MegaByte = KiloByte * 1000;
        GigaByte = MegaByte * 1000;
        TeraByte = GigaByte * 1000;
        KibiByte = Byte * 1024;
        MebiByte = KibiByte * 1024;
        GibiByte = MebiByte * 1024;
        );

    public static String HumanBytes(long b) {
        var value double;
        var unit String;
        switch {
        case b >= TeraByte:;
        value = double(b) / TeraByte;
        unit = "TB";
        case b >= GigaByte:;
        value = double(b) / GigaByte;
        unit = "GB";
        case b >= MegaByte:;
        value = double(b) / MegaByte;
        unit = "MB";
        case b >= KiloByte:;
        value = double(b) / KiloByte;
        unit = "KB";
        default:;
        return fmt.Sprintf("%d B", b);
    }
        switch {
        case value >= 10:;
        return fmt.Sprintf("%d %s", int(value), unit);
        case value != math.Trunc(value):;
        return fmt.Sprintf("%.1f %s", value, unit);
        default:;
        return fmt.Sprintf("%d %s", int(value), unit);
    }
    }

    public static String HumanBytes2(uint64 b) {
        switch {
        case b >= GibiByte:;
        return fmt.Sprintf("%.1f GiB", double(b)/GibiByte);
        case b >= MebiByte:;
        return fmt.Sprintf("%.1f MiB", double(b)/MebiByte);
        case b >= KibiByte:;
        return fmt.Sprintf("%.1f KiB", double(b)/KibiByte);
        default:;
        return fmt.Sprintf("%d B", b);
    }
    }
}
