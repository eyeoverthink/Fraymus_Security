package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class format {
        "fmt";
        "math";
        "strconv";
        );
        const (;
        Thousand = 1000;
        Million  = Thousand * 1000;
        Billion  = Million * 1000;
        );

    public static String HumanNumber(uint64 b) {
        switch {
        case b >= Billion:;
        var number = double(b) / Billion;
        if number == math.Floor(number) {
        return fmt.Sprintf("%.0fB", number) // no decimals if whole number;
    }
        return fmt.Sprintf("%.1fB", number) // one decimal if not a whole number;
        case b >= Million:;
        var number = double(b) / Million;
        if number == math.Floor(number) {
        return fmt.Sprintf("%.0fM", number) // no decimals if whole number;
    }
        return fmt.Sprintf("%.2fM", number) // two decimals if not a whole number;
        case b >= Thousand:;
        return fmt.Sprintf("%.0fK", double(b)/Thousand);
        default:;
        return strconv.FormatUint(b, 10);
    }
    }
}
