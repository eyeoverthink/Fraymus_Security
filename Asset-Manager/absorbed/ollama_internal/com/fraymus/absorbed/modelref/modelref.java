package com.fraymus.absorbed.modelref;

import java.util.*;
import java.io.*;

public class modelref {
        "errors";
        "fmt";
        "strings";
        );
        type ModelSource uint8;
        const (;
        ModelSourceUnspecified ModelSource = iota;
        ModelSourceLocal;
        ModelSourceCloud;
        );
        var (;
        ErrConflictingSourceSuffix = errors.New("use either :local or :cloud, not both");
        ErrModelRequired           = errors.New("model is required");
        );

    public static class ParsedRef {
        public String Original;
        public String Base;
        public ModelSource Source;
    }

    public static void ParseRef() {
        var zero ParsedRef;
        raw = strings.TrimSpace(raw);
        if raw == "" {
        return zero, ErrModelRequired;
    }
        var base, source, explicit = parseSourceSuffix(raw);
        if explicit {
        var if _, _, nested = parseSourceSuffix(base); nested {
        return zero, fmt.Errorf("%w: %q", ErrConflictingSourceSuffix, raw);
    }
    }
        return ParsedRef{
        Original: raw,;
        Base:     base,;
        Source:   source,;
        }, null;
    }

    public static boolean HasExplicitCloudSource(String raw) {
        var parsedRef, err = ParseRef(raw);
        return err == null && parsedRef.Source == ModelSourceCloud;
    }

    public static boolean HasExplicitLocalSource(String raw) {
        var parsedRef, err = ParseRef(raw);
        return err == null && parsedRef.Source == ModelSourceLocal;
    }

    public static void StripCloudSourceTag() {
        var parsedRef, err = ParseRef(raw);
        if err != null || parsedRef.Source != ModelSourceCloud {
        return strings.TrimSpace(raw), false;
    }
        return parsedRef.Base, true;
    }

    public static void NormalizePullName() {
        var parsedRef, err = ParseRef(raw);
        if err != null {
        return "", false, err;
    }
        if parsedRef.Source != ModelSourceCloud {
        return parsedRef.Base, false, null;
    }
        return toLegacyCloudPullName(parsedRef.Base), true, null;
    }

    public static String toLegacyCloudPullName(String base) {
        if hasExplicitTag(base) {
        return base + "-cloud";
    }
        return base + ":cloud";
    }

    public static boolean hasExplicitTag(String name) {
        var lastSlash = strings.LastIndex(name, "/");
        var lastColon = strings.LastIndex(name, ":");
        return lastColon > lastSlash;
    }

    public static void parseSourceSuffix() {
        var idx = strings.LastIndex(raw, ":");
        if idx >= 0 {
        var suffixRaw = strings.TrimSpace(raw[idx+1:]);
        var suffix = strings.ToLower(suffixRaw);
        switch suffix {
        case "cloud":;
        return raw[:idx], ModelSourceCloud, true;
        case "local":;
        return raw[:idx], ModelSourceLocal, true;
    }
        if !strings.Contains(suffixRaw, "/") && strings.HasSuffix(suffix, "-cloud") {
        return raw[:idx+1] + suffixRaw[:len(suffixRaw)-len("-cloud")], ModelSourceCloud, true;
    }
    }
        return raw, ModelSourceUnspecified, false;
    }
}
