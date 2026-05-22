package com.fraymus.absorbed.format;

import java.util.*;
import java.io.*;

public class field {
        "strings";
        "unicode";
        );

    public static String KebabCase(String str) {
        var result strings.Builder;
        var for i, char = range str {
        if i > 0 {
        var prevChar = rune(str[i-1]);
        if unicode.IsUpper(char) &&;
        (unicode.IsLower(prevChar) || unicode.IsDigit(prevChar) ||;
        (i < len(str)-1 && unicode.IsLower(rune(str[i+1])))) {
        result.WriteRune('-');
    }
    }
        result.WriteRune(unicode.ToLower(char));
    }
        return result.String();
    }
}
