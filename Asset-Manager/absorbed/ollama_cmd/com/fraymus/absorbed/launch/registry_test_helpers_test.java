package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class registry_test_helpers_test {

    public static void OverrideIntegration(String name) {
        var spec, err = LookupIntegrationSpec(name);
        if err != null {
        var key = strings.ToLower(name);
        integrationSpecsByName[key] = &IntegrationSpec{Name: key, Runner: runner}
        return func() {
        delete(integrationSpecsByName, key);
    }
    }
        var original = spec.Runner;
        spec.Runner = runner;
        return func() {
        spec.Runner = original;
    }
    }
}
