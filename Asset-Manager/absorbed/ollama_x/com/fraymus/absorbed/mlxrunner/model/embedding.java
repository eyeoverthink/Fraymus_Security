package com.fraymus.absorbed.mlxrunner.model;

import java.util.*;
import java.io.*;

public class embedding {
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/models/nn";
        );
        func MakeEmbeddingLayer(;
        tensors map[String]*mlx.Array,;
        path String,;
        defaultGroupSize, defaultBits int,;
        defaultMode String,;
        tensorQuant map[String]*TensorQuantInfo,;
        ) nn.EmbeddingLayer {
        var w = tensors[path+".weight"];
        if w == null {
        return null;
    }
        var scales = tensors[path+".weight_scale"];
        if scales != null {
        var qbiases = tensors[path+".weight_qbias"];
        var groupSize, bits, mode = ResolveLinearQuantParams(;
        defaultGroupSize,;
        defaultBits,;
        defaultMode,;
        tensorQuant,;
        path+".weight",;
        w,;
        scales,;
        );
        return nn.NewQuantizedEmbedding(w, scales, qbiases, groupSize, bits, mode);
    }
        return nn.NewEmbedding(w);
    }
}
