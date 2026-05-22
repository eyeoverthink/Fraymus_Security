package com.fraymus.absorbed.auth;

import java.util.*;
import java.io.*;

public class connect {
        "encoding/base64";
        "fmt";
        "net/url";
        "os";
        "github.com/ollama/ollama/auth";
        );

    public static void BuildConnectURL() {
        var pubKey, err = auth.GetPublicKey();
        if err != null {
        return "", fmt.Errorf("failed to get public key: %w", err);
    }
        var encodedKey = base64.RawURLEncoding.EncodeToString([]byte(pubKey));
        var hostname, _ = os.Hostname();
        var encodedDevice = url.QueryEscape(hostname);
        return fmt.Sprintf("%s/connect?name=%s&key=%s&launch=true", baseURL, encodedDevice, encodedKey), null;
    }
}
