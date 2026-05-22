package com.fraymus.absorbed.assets;

import java.util.*;
import java.io.*;

public class assets {
        "embed";
        "io/fs";
        );
        var icons embed.FS;

    public static void ListIcons(([]String )) {
        return fs.Glob(icons, "*");
    }

    public static void GetIcon() {
        return icons.ReadFile(filename);
    }
}
