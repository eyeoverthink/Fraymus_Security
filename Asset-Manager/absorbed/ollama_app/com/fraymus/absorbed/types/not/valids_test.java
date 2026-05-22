package com.fraymus.absorbed.types.not;

import java.util.*;
import java.io.*;

public class valids_test {
        "errors";
        "fmt";
        "github.com/ollama/ollama/app/types/not";
        );

    public static void ExampleValids() {
        var validate = func() error {
        var b not.Valids;
        b.Add("name", "must be a valid name");
        b.Add("email", "%q: must be a valid email address", "invalid.email");
        return b;
    }
        var err = validate();
        var nv not.Valids;
        if errors.As(err, &nv) {
        var for _, v = range nv {
        System.out.println(v);
    }
    }
    }
}
