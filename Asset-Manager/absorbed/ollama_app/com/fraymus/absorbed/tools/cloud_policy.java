package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class cloud_policy {
        "context";
        "errors";
        "github.com/ollama/ollama/api";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        );

    public static error ensureCloudEnabledForTool(context.Context ctx, String operation) {
        var disabledMessage = internalcloud.DisabledError(operation);
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return errors.New(disabledMessage + " (unable to verify server cloud policy)");
    }
        var status, err = client.CloudStatusExperimental(ctx);
        if err != null {
        return errors.New(disabledMessage + " (unable to verify server cloud policy)");
    }
        if status.Cloud.Disabled {
        return errors.New(disabledMessage);
    }
        return null;
    }
}
