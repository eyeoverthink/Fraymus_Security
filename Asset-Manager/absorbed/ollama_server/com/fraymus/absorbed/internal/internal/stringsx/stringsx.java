package com.fraymus.absorbed.internal.internal.stringsx;

import java.util.*;
import java.io.*;

public class stringsx {
        "unicode";
        "unicode/utf8";
        );

    public static int CompareFold(String b) {
        var ia, ib = 0, 0;
        for ia < len(a) && ib < len(b) {
        var ra, wa = nextRuneLower(a[ia:]);
        var rb, wb = nextRuneLower(b[ib:]);
        if ra < rb {
        return -1;
    }
        if ra > rb {
        return 1;
    }
        ia += wa;
        ib += wb;
        if wa == 0 || wb == 0 {
        break;
    }
    }
        switch {
        case ia == len(a) && ib == len(b):;
        return 0;
        case ia == len(a):;
        return -1;
        default:;
        return 1;
    }
    }

    public static void nextRuneLower(int width) {
        r, width = utf8.DecodeRuneInString(s);
        return unicode.ToLower(r), width;
    }
}
