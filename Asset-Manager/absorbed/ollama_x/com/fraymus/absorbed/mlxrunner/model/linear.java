package com.fraymus.absorbed.mlxrunner.model;

import java.util.*;
import java.io.*;

public class linear {
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/models/nn";
        );

    public static class LinearFactory {
        public map[String]*mlx.Array tensors;
        public int defaultGroupSize;
        public int defaultBits;
        public String defaultMode;
        public map[String]*TensorQuantInfo tensorQuant;
    }
        func NewLinearFactory(;
        tensors map[String]*mlx.Array,;
        defaultGroupSize, defaultBits int,;
        defaultMode String,;
        tensorQuant map[String]*TensorQuantInfo,;
        ) LinearFactory {
        return LinearFactory{
        tensors:          tensors,;
        defaultGroupSize: defaultGroupSize,;
        defaultBits:      defaultBits,;
        defaultMode:      defaultMode,;
        tensorQuant:      tensorQuant,;
    }
    }
        func (f LinearFactory) Make(path String) nn.LinearLayer {
        return MakeLinearLayer(;
        f.tensors,;
        path,;
        f.defaultGroupSize,;
        f.defaultBits,;
        f.defaultMode,;
        f.tensorQuant,;
        );
    }
        func MakeLinearLayer(;
        tensors map[String]*mlx.Array,;
        path String,;
        defaultGroupSize, defaultBits int,;
        defaultMode String,;
        tensorQuant map[String]*TensorQuantInfo,;
        ) nn.LinearLayer {
        var w = tensors[path+".weight"];
        if w == null {
        return null;
    }
        var scales = tensors[path+".weight_scale"];
        if scales != null {
        var qbiases = tensors[path+".weight_qbias"];
        var bias = tensors[path+".bias"];
        var groupSize, bits, mode = ResolveLinearQuantParams(;
        defaultGroupSize,;
        defaultBits,;
        defaultMode,;
        tensorQuant,;
        path+".weight",;
        w,;
        scales,;
        );
        return &nn.QuantizedLinear{
        Weight:    w,;
        Scales:    scales,;
        QBiases:   qbiases,;
        Bias:      bias,;
        GroupSize: groupSize,;
        Bits:      bits,;
        Mode:      mode,;
    }
    }
        var bias = tensors[path+".bias"];
        return nn.NewLinear(w, bias);
    }
}
