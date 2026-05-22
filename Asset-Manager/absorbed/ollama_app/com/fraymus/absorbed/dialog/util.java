package com.fraymus.absorbed.dialog;

import java.util.*;
import java.io.*;

public class util {

    public static String firstOf(...String args) {
        var for _, arg = range args {
        if arg != "" {
        return arg;
    }
    }
        return "";
    }
}
