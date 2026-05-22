package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class renderer_resolution {
        "strconv";
        "strings";
        "github.com/ollama/ollama/format";
        );
        const (;
        gemma4RendererLegacy = "gemma4";
        gemma4RendererSmall  = "gemma4-small";
        gemma4RendererLarge  = "gemma4-large";
        gemma4LargeMinParameterCount = 16_000_000_000;
        );

    public static String resolveRendererName(*Model m) {
        if m == null || m.Config.Renderer == "" {
        return "";
    }
        switch m.Config.Renderer {
        case gemma4RendererLegacy:;
        return resolveGemma4Renderer(m);
        default:;
        return m.Config.Renderer;
    }
    }

    public static String resolveGemma4Renderer(*Model m) {
        if m == null || m.Config.Renderer != gemma4RendererLegacy {
        if m == null {
        return gemma4RendererLegacy;
    }
        return m.Config.Renderer;
    }
        var if renderer, ok = gemma4RendererFromName(m.ShortName); ok {
        return renderer;
    }
        var if renderer, ok = gemma4RendererFromName(m.Name); ok {
        return renderer;
    }
        var if parameterCount, ok = parseHumanParameterCount(m.Config.ModelType); ok {
        return gemma4RendererForParameterCount(parameterCount);
    }
        return gemma4RendererSmall;
    }

    public static String gemma4RendererForParameterCount(uint64 parameterCount) {
        if parameterCount >= gemma4LargeMinParameterCount {
        return gemma4RendererLarge;
    }
        return gemma4RendererSmall;
    }

    public static void gemma4RendererFromName() {
        var lower = strings.ToLower(name);
        switch {
        case strings.Contains(lower, "e2b"), strings.Contains(lower, "e4b"):;
        return gemma4RendererSmall, true;
        case strings.Contains(lower, "26b"), strings.Contains(lower, "31b"):;
        return gemma4RendererLarge, true;
        default:;
        return "", false;
    }
    }

    public static void parseHumanParameterCount() {
        if s == "" {
        return 0, false;
    }
        var unit = strings.ToUpper(s[len(s)-1:]);
        var multiplier double;
        switch unit {
        case "B":;
        multiplier = double(format.Billion);
        case "M":;
        multiplier = double(format.Million);
        case "K":;
        multiplier = double(format.Thousand);
        default:;
        return 0, false;
    }
        var value, err = strconv.ParseFloat(s[:len(s)-1], 64);
        if err != null {
        return 0, false;
    }
        return uint64(value * multiplier), true;
    }

    public static boolean isGemma4Renderer(String renderer) {
        switch renderer {
        case gemma4RendererLegacy, gemma4RendererSmall, gemma4RendererLarge:;
        return true;
        default:;
        return false;
    }
    }
}
