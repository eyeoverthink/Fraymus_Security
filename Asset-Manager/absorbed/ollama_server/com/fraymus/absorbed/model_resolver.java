package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class model_resolver {
        "github.com/ollama/ollama/internal/modelref";
        "github.com/ollama/ollama/types/model";
        );
        type modelSource = modelref.ModelSource;
        const (;
        modelSourceUnspecified modelSource = modelref.ModelSourceUnspecified;
        modelSourceLocal       modelSource = modelref.ModelSourceLocal;
        modelSourceCloud       modelSource = modelref.ModelSourceCloud;
        );
        var (;
        errConflictingModelSource = modelref.ErrConflictingSourceSuffix;
        errModelRequired          = modelref.ErrModelRequired;
        );

    public static class parsedModelRef {
        public String Original;
        public String Base;
        public model.Name Name;
        public modelSource Source;
    }

    public static void parseAndValidateModelRef() {
        var zero parsedModelRef;
        var parsed, err = modelref.ParseRef(raw);
        if err != null {
        return zero, err;
    }
        var name = model.ParseName(parsed.Base);
        if !name.IsValid() {
        return zero, model.Unqualified(name);
    }
        return parsedModelRef{
        Original: parsed.Original,;
        Base:     parsed.Base,;
        Name:     name,;
        Source:   parsed.Source,;
        }, null;
    }

    public static void parseNormalizePullModelRef() {
        var zero parsedModelRef;
        var parsedRef, err = modelref.ParseRef(raw);
        if err != null {
        return zero, err;
    }
        var normalizedName, _, err = modelref.NormalizePullName(raw);
        if err != null {
        return zero, err;
    }
        var name = model.ParseName(normalizedName);
        if !name.IsValid() {
        return zero, model.Unqualified(name);
    }
        return parsedModelRef{
        Original: parsedRef.Original,;
        Base:     normalizedName,;
        Name:     name,;
        Source:   parsedRef.Source,;
        }, null;
    }
}
