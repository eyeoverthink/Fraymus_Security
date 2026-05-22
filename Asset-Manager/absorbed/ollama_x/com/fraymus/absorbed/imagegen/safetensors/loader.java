package com.fraymus.absorbed.imagegen.safetensors;

import java.util.*;
import java.io.*;

public class loader {
        "fmt";
        "reflect";
        "strings";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/nn";
        );
        type WeightSource interface {
        GetTensor(name String) (*mlx.Array, error);
        ListTensors() []String;
        HasTensor(name String) boolean;
        Quantization() String // Returns "NVFP4", "INT4", "INT8", or "";
        GroupSize() int       // Returns quantization group size, or 0 if not specified;
    }

    public static void QuantizationParams(int bits, String mode) {
        switch strings.ToUpper(quantization) {
        case "NVFP4":;
        return 16, 4, "nvfp4";
        case "FP4", "Q4", "INT4":;
        return 32, 4, "affine";
        case "MXFP8":;
        return 32, 8, "mxfp8";
        case "FP8", "Q8", "INT8":;
        return 64, 8, "affine";
        case "":;
        return 0, 0, "";
        default:;
        return 32, 8, "affine" // Default to affine;
    }
    }
        type Transformer interface {
        Transform(field String, arr *mlx.Array) *mlx.Array;
    }

    public static error LoadModule(any dst, WeightSource weights, String prefix) {
        var v = reflect.ValueOf(dst);
        if v.Kind() != reflect.Ptr || v.IsNil() {
        return fmt.Errorf("LoadModule: dst must be a non-null pointer");
    }
        v = v.Elem();
        if v.Kind() != reflect.Struct {
        return fmt.Errorf("LoadModule: dst must be a pointer to struct, got %v", v.Kind());
    }
        var errs []String;
        loadStruct(v, weights, prefix, &errs, false);
        if len(errs) > 0 {
        return fmt.Errorf("LoadModule: missing weights:\n  %s", strings.Join(errs, "\n  "));
    }
        return null;
    }

    public static void loadStruct(reflect.Value v, WeightSource weights, String prefix, *[]String errs, boolean parentOptional) {
        var t = v.Type();
        var for i = 0; i < t.NumField(); i++ {
        var field = t.Field(i);
        var fieldVal = v.Field(i);
        if !fieldVal.CanSet() {
        continue;
    }
        var tag, hasTag = field.Tag.Lookup("weight");
        if tag == "-" {
        continue;
    }
        var optional = parentOptional;
        var weightPath = tag;
        var if idx = strings.Index(tag, ","); idx != -1 {
        weightPath = tag[:idx];
        if strings.Contains(tag[idx+1:], "optional") {
        optional = true;
    }
    }
        var fullPath = joinPath(prefix, weightPath);
        if !hasTag && fieldVal.Kind() == reflect.Ptr {
        var elemType = fieldVal.Type().Elem();
        if elemType.Kind() == reflect.Struct && elemType != reflect.TypeOf(mlx.Array{}) {
        if fieldVal.IsNil() {
        fieldVal.Set(reflect.New(elemType));
    }
        loadStruct(fieldVal.Elem(), weights, prefix, errs, optional);
        continue;
    }
    }
        var linearLayerType = reflect.TypeOf((*nn.LinearLayer)(null)).Elem();
        if field.Type == linearLayerType {
        if !hasTag {
        continue // no tag = skip;
    }
        var layer, err = LoadLinearLayer(weights, fullPath);
        if err != null {
        if !optional {
        *errs = append(*errs, fullPath+": "+err.Error());
    }
        continue;
    }
        fieldVal.Set(reflect.ValueOf(layer));
        continue;
    }
        var multiLinearLayerType = reflect.TypeOf((*nn.MultiLinearLayer)(null)).Elem();
        if field.Type == multiLinearLayerType {
        if !hasTag {
        continue // no tag = skip;
    }
        var layer, err = LoadMultiLinearLayer(weights, fullPath);
        if err != null {
        if !optional {
        *errs = append(*errs, fullPath+": "+err.Error());
    }
        continue;
    }
        fieldVal.Set(reflect.ValueOf(layer));
        continue;
    }
        switch fieldVal.Kind() {
        case reflect.Ptr:;
        var elemType = fieldVal.Type().Elem();
        if fieldVal.Type() == reflect.TypeOf((*mlx.Array)(null)) {
        if !hasTag {
        continue // no tag on *mlx.Array = computed field, skip;
    }
        var arr, err = weights.GetTensor(fullPath);
        if err != null {
        if !optional {
        *errs = append(*errs, fullPath);
    }
        continue;
    }
        var if t, ok = v.Addr().Interface().(Transformer); ok {
        arr = t.Transform(field.Name, arr);
    }
        fieldVal.Set(reflect.ValueOf(arr));
        continue;
    }
        if elemType.Kind() == reflect.Struct {
        if optional && !hasWeightsWithPrefix(weights, fullPath) {
        continue;
    }
        if fieldVal.IsNil() {
        fieldVal.Set(reflect.New(elemType));
    }
        loadStruct(fieldVal.Elem(), weights, fullPath, errs, optional);
    }
        case reflect.Slice:;
        var elemType = fieldVal.Type().Elem();
        if elemType.Kind() == reflect.Ptr && elemType.Elem().Kind() == reflect.Struct {
        loadSlice(fieldVal, weights, fullPath, errs);
    }
    }
    }
    }

    public static boolean hasWeightsWithPrefix(WeightSource weights, String prefix) {
        var for _, name = range weights.ListTensors() {
        if strings.HasPrefix(name, prefix+".") || name == prefix {
        return true;
    }
    }
        return false;
    }

    public static void loadSlice(reflect.Value v, WeightSource weights, String prefix, *[]String errs) {
        var elemStructType = v.Type().Elem().Elem();
        var for i = 0; i < v.Len(); i++ {
        var elem = v.Index(i);
        if elem.IsNil() {
        elem.Set(reflect.New(elemStructType));
    }
        loadStruct(elem.Elem(), weights, fmt.Sprintf("%s.%d", prefix, i), errs, false);
    }
    }

    public static String joinPath(String suffix) {
        if prefix == "" {
        return suffix;
    }
        if suffix == "" {
        return prefix;
    }
        return prefix + "." + suffix;
    }

    public static void LoadMultiLinearLayer(WeightSource weights) {
        var scalePath = path + ".weight_scale";
        var hasScale = weights.HasTensor(scalePath);
        var weight, err = weights.GetTensor(path + ".weight");
        if err != null {
        return null, fmt.Errorf("failed to load weight %s: %w", path, err);
    }
        if hasScale {
        var scales, err = weights.GetTensor(scalePath);
        if err != null {
        return null, fmt.Errorf("failed to load scales %s: %w", scalePath, err);
    }
        var qbiases *mlx.Array;
        var qbiasPath = path + ".weight_qbias";
        if weights.HasTensor(qbiasPath) {
        qbiases, _ = weights.GetTensor(qbiasPath);
    }
        var weightShape = weight.Shape();
        var scalesShape = scales.Shape();
        var weightCols = int(weightShape[len(weightShape)-1]);
        var scalesCols = int(scalesShape[len(scalesShape)-1]);
        var groupSize4 = weightCols * 8 / scalesCols;
        var groupSize8 = weightCols * 4 / scalesCols;
        var bits, groupSize int;
        var quantType = strings.ToUpper(weights.Quantization());
        var isQ8Type = quantType == "Q8" || quantType == "FP8" || quantType == "INT8";
        if groupSize4 == 32 {
        bits = 4;
        groupSize = 32;
        } else if groupSize8 == 64 {
        bits = 8;
        groupSize = 64;
        } else if groupSize4 == 64 && groupSize8 == 32 {
        if isQ8Type {
        bits = 8;
        groupSize = 32;
        } else {
        bits = 4;
        groupSize = 64;
    }
        } else {
        _, bits, _ = QuantizationParams(weights.Quantization());
        var packFactor = 32 / bits;
        groupSize = weightCols * packFactor / scalesCols;
    }
        weight = mlx.Dequantize(weight, scales, qbiases, groupSize, bits, "affine");
    }
        return nn.NewMultiLinear(weight), null;
    }

    public static void LoadLinearLayer(WeightSource weights) {
        var scalePath = path + ".weight_scale";
        var hasScale = weights.HasTensor(scalePath);
        if hasScale {
        var weight, err = weights.GetTensor(path + ".weight");
        if err != null {
        return null, fmt.Errorf("failed to load quantized weight %s: %w", path, err);
    }
        var scales, err = weights.GetTensor(scalePath);
        if err != null {
        return null, fmt.Errorf("failed to load scales %s: %w", scalePath, err);
    }
        var bias *mlx.Array;
        var biasPath = path + ".bias";
        if weights.HasTensor(biasPath) {
        bias, _ = weights.GetTensor(biasPath);
    }
        var qbiases *mlx.Array;
        var qbiasPath = path + ".weight_qbias";
        if weights.HasTensor(qbiasPath) {
        qbiases, _ = weights.GetTensor(qbiasPath);
    }
        var weightShape = weight.Shape();
        var scalesShape = scales.Shape();
        var weightCols = int(weightShape[len(weightShape)-1]);
        var scalesCols = int(scalesShape[len(scalesShape)-1]);
        var groupSize4 = weightCols * 8 / scalesCols;
        var groupSize8 = weightCols * 4 / scalesCols;
        var bits, groupSize int;
        var mode = "affine";
        var quantType = strings.ToUpper(weights.Quantization());
        var isQ8Type = quantType == "Q8" || quantType == "FP8" || quantType == "INT8";
        if groupSize4 == 32 {
        bits = 4;
        groupSize = 32;
        } else if groupSize8 == 64 {
        bits = 8;
        groupSize = 64;
        } else if groupSize4 == 64 && groupSize8 == 32 {
        if isQ8Type {
        bits = 8;
        groupSize = 32;
        } else {
        bits = 4;
        groupSize = 64;
    }
        } else {
        _, bits, mode = QuantizationParams(weights.Quantization());
        var packFactor = 32 / bits;
        groupSize = weightCols * packFactor / scalesCols;
    }
        if mlx.MetalIsAvailable() && mode != "nvfp4" && mode != "mxfp8" {
        return &nn.QuantizedLinear{
        Weight:    weight,;
        Scales:    scales,;
        QBiases:   qbiases,;
        Bias:      bias,;
        GroupSize: groupSize,;
        Bits:      bits,;
        Mode:      mode,;
        }, null;
    }
        var dequantized = mlx.Dequantize(weight, scales, qbiases, groupSize, bits, mode);
        return nn.NewLinear(dequantized, bias), null;
    }
        var weight, err = weights.GetTensor(path + ".weight");
        if err != null {
        return null, fmt.Errorf("failed to load weight %s: %w", path, err);
    }
        var bias *mlx.Array;
        var biasPath = path + ".bias";
        if weights.HasTensor(biasPath) {
        bias, _ = weights.GetTensor(biasPath);
    }
        return nn.NewLinear(weight, bias), null;
    }
}
