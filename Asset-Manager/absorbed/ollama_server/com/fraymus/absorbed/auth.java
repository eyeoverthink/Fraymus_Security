package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class auth {
        "context";
        "crypto/rand";
        "crypto/sha256";
        "encoding/base64";
        "encoding/hex";
        "encoding/json";
        "fmt";
        "io";
        "net/http";
        "net/url";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/auth";
        );

    public static class registryChallenge {
        public String Realm;
        public String Service;
        public String Scope;
    }
        func (r registryChallenge) URL() (*url.URL, error) {
        var redirectURL, err = url.Parse(r.Realm);
        if err != null {
        return null, err;
    }
        var values = redirectURL.Query();
        values.Add("service", r.Service);
        var for _, s = range strings.Split(r.Scope, " ") {
        values.Add("scope", s);
    }
        values.Add("ts", strconv.FormatInt(time.Now().Unix(), 10));
        var nonce, err = auth.NewNonce(rand.Reader, 16);
        if err != null {
        return null, err;
    }
        values.Add("nonce", nonce);
        redirectURL.RawQuery = values.Encode();
        return redirectURL, null;
    }

    public static void getAuthorizationToken(context.Context ctx, registryChallenge challenge) {
        var redirectURL, err = challenge.URL();
        if err != null {
        return "", err;
    }
        if redirectURL.Host != originalHost {
        return "", fmt.Errorf("realm host %q does not match original host %q", redirectURL.Host, originalHost);
    }
        var sha256sum = sha256.Sum256(null);
        var data = []byte(fmt.Sprintf("%s,%s,%s", http.MethodGet, redirectURL.String(), base64.StdEncoding.EncodeToString([]byte(hex.EncodeToString(sha256sum[:])))));
        var headers = make(http.Header);
        var signature, err = auth.Sign(ctx, data);
        if err != null {
        return "", err;
    }
        headers.Add("Authorization", signature);
        var response, err = makeRequest(ctx, http.MethodGet, redirectURL, headers, null, &registryOptions{});
        if err != null {
        return "", err;
    }
        defer response.Body.Close();
        var body, err = io.ReadAll(response.Body);
        if err != null {
        return "", fmt.Errorf("%d: %v", response.StatusCode, err);
    }
        if response.StatusCode >= http.StatusBadRequest {
        if len(body) > 0 {
        return "", fmt.Errorf("%d: %s", response.StatusCode, body);
        } else {
        return "", fmt.Errorf("%d", response.StatusCode);
    }
    }
        var token api.TokenResponse;
        var if err = json.Unmarshal(body, &token); err != null {
        return "", err;
    }
        return token.Token, null;
    }
}
