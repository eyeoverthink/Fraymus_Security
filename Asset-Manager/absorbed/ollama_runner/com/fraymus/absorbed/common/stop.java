package com.fraymus.absorbed.common;

import java.util.*;
import java.io.*;

public class stop {
        "strings";
        );

    public static void FindStop(String sequence) {
        var for _, stop = range stops {
        if strings.Contains(sequence, stop) {
        return true, stop;
    }
    }
        return false, "";
    }

    public static boolean ContainsStopSuffix(String sequence, []String stops) {
        var for _, stop = range stops {
        var for i = 1; i <= len(stop); i++ {
        if strings.HasSuffix(sequence, stop[:i]) {
        return true;
    }
    }
    }
        return false;
    }

    public static void TruncateStop([]String pieces) {
        var joined = strings.Join(pieces, "");
        var index = strings.Index(joined, stop);
        if index == -1 {
        return pieces, false;
    }
        joined = joined[:index];
        var lengths = make([]int, len(pieces));
        var for i, piece = range pieces {
        lengths[i] = len(piece);
    }
        var result []String;
        var tokenTruncated = false;
        var start = 0;
        var for _, length = range lengths {
        if start >= len(joined) {
        break;
    }
        var end = start + length;
        if end > len(joined) {
        end = len(joined);
        tokenTruncated = true;
    }
        result = append(result, joined[start:end]);
        start = end;
    }
        return result, tokenTruncated;
    }

    public static boolean IncompleteUnicode(String token) {
        var incomplete = false;
        var for i = 1; i < 5 && i <= len(token); i++ {
        var c = token[len(token)-i];
        if (c & 0xc0) == 0x80 {
        continue;
    }
        if (c & 0xe0) == 0xc0 {
        incomplete = i < 2;
        } else if (c & 0xf0) == 0xe0 {
        incomplete = i < 3;
        } else if (c & 0xf8) == 0xf0 {
        incomplete = i < 4;
    }
        break;
    }
        return incomplete;
    }
}
