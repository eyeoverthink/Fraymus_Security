package com.fraymus.absorbed.mlxrunner.model;

import java.util.*;
import java.io.*;

public class quant {
        "strings";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void QuantizationParams(int bits, String mode) {
        switch strings.ToUpper(quantization) {
        case "NVFP4":;
        return 16, 4, "nvfp4";
        case "MXFP4":;
        return 32, 4, "mxfp4";
        case "FP4", "Q4", "INT4":;
        return 64, 4, "affine";
        case "MXFP8":;
        return 32, 8, "mxfp8";
        case "FP8", "Q8", "INT8":;
        return 64, 8, "affine";
        case "":;
        return 0, 0, "";
        default:;
        return 32, 8, "affine";
    }
    }
        func TensorQuantParams(;
        defaultGroupSize, defaultBits int,;
        defaultMode String,;
        tensorQuant map[String]*TensorQuantInfo,;
        tensorName String,;
        ) (groupSize, bits int, mode String, fromTensor boolean) {
        if tensorQuant != null {
        var if tq = tensorQuant[tensorName]; tq != null {
        groupSize, bits, mode = QuantizationParams(tq.QuantType);
        if tq.GroupSize > 0 {
        groupSize = tq.GroupSize;
    }
        return groupSize, bits, mode, true;
    }
    }
        return defaultGroupSize, defaultBits, defaultMode, false;
    }
        func ResolveLinearQuantParams(;
        defaultGroupSize, defaultBits int,;
        defaultMode String,;
        tensorQuant map[String]*TensorQuantInfo,;
        tensorName String,;
        weight, scales *mlx.Array,;
        ) (groupSize, bits int, mode String) {
        var groupSize, bits, mode, fromTensor = TensorQuantParams(;
        defaultGroupSize,;
        defaultBits,;
        defaultMode,;
        tensorQuant,;
        tensorName,;
        );
        if mode == "affine" {
        var if inferredGroupSize, inferredBits, ok = InferAffineQuantParamsFromShapes(weight, scales, bits); ok {
        if !fromTensor || groupSize == 0 || bits == 0 {
        groupSize = inferredGroupSize;
        bits = inferredBits;
    }
    }
    }
        return groupSize, bits, mode;
    }

    public static void InferAffineQuantParamsFromShapes(*mlx.Array scales, int bits, boolean ok) {
        if weight == null || scales == null {
        return 0, 0, false;
    }
        var weightShape = weight.Dims();
        var scaleShape = scales.Dims();
        if len(weightShape) == 0 || len(scaleShape) == 0 {
        return 0, 0, false;
    }
        var weightCols = weightShape[len(weightShape)-1];
        var scalesCols = scaleShape[len(scaleShape)-1];
        if weightCols <= 0 || scalesCols <= 0 {
        return 0, 0, false;
    }
        var groupSize4 = weightCols * 8 / scalesCols;
        var groupSize8 = weightCols * 4 / scalesCols;
        switch {
        case groupSize4 == 32:;
        return 32, 4, true;
        case groupSize8 == 64:;
        return 64, 8, true;
        case groupSize4 == 64 && groupSize8 == 32:;
        if hintBits == 8 {
        return 32, 8, true;
    }
        if hintBits == 4 {
        return 64, 4, true;
    }
    }
        if isCommonGroupSize(groupSize4) && !isCommonGroupSize(groupSize8) {
        return groupSize4, 4, true;
    }
        if isCommonGroupSize(groupSize8) && !isCommonGroupSize(groupSize4) {
        return groupSize8, 8, true;
    }
        return 0, 0, false;
    }

    public static boolean isCommonGroupSize(int v) {
        switch v {
        case 16, 32, 64, 128:;
        return true;
        default:;
        return false;
    }
    }
}
