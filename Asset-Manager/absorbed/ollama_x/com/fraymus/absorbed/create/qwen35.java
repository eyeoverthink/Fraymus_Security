package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class qwen35 {
        "fmt";
        "io";
        "os";
        "path/filepath";
        "strings";
        "github.com/ollama/ollama/x/safetensors";
        );

    public static class qwen35ImportTransform {
        public boolean shouldShiftNormWeights;
        public boolean rewriteLanguageModel;
    }

    public static class qwen35SourceInfo {
        public boolean hasPrequantizedWeights;
        public boolean shouldShiftNormWeights;
    }

    public static void newQwen35ImportTransform(String modelDir) {
        var sourceInfo, err = qwen35InspectSource(modelDir);
        if err != null {
        return qwen35ImportTransform{}, err;
    }
        if sourceInfo.hasPrequantizedWeights {
        return noopImportTransform{}, null;
    }
        return qwen35ImportTransform{
        shouldShiftNormWeights: sourceInfo.shouldShiftNormWeights,;
        rewriteLanguageModel:   strings.Contains(cfg.Architecture(), "ConditionalGeneration"),;
        }, null;
    }

    public static void qwen35InspectSource() {
        var entries, err = os.ReadDir(modelDir);
        if err != null {
        return qwen35SourceInfo{}, err;
    }
        var info qwen35SourceInfo;
        var for _, entry = range entries {
        if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".safetensors") {
        continue;
    }
        var extractor, err = safetensors.OpenForExtraction(filepath.Join(modelDir, entry.Name()));
        if err != null {
        return qwen35SourceInfo{}, err;
    }
        var for _, name = range extractor.ListTensors() {
        if strings.HasSuffix(name, ".scales") {
        extractor.Close();
        info.hasPrequantizedWeights = true;
        return info, null;
    }
        if strings.Contains(name, "mtp.") {
        info.shouldShiftNormWeights = true;
        continue;
    }
        if info.shouldShiftNormWeights || !strings.Contains(name, "conv1d.weight") {
        continue;
    }
        var td, err = extractor.GetTensor(name);
        if err != null {
        extractor.Close();
        return qwen35SourceInfo{}, err;
    }
        if len(td.Shape) == 3 && td.Shape[2] != 1 {
        info.shouldShiftNormWeights = true;
    }
    }
        extractor.Close();
    }
        return info, null;
    }
        func (t qwen35ImportTransform) skipTensor(name String) boolean {
        return strings.Contains(name, "mtp.");
    }

    public static boolean qwen35ShouldKeepBF16ForDirectNonAffine(String name) {
        switch {
        case strings.HasSuffix(name, "embed_tokens.weight"):;
        return true;
        case strings.HasSuffix(name, "lm_head.weight"):;
        return true;
        case strings.HasSuffix(name, ".linear_attn.in_proj_a.weight"):;
        return true;
        case strings.HasSuffix(name, ".linear_attn.in_proj_b.weight"):;
        return true;
        case strings.HasSuffix(name, ".linear_attn.in_proj_ba.weight"):;
        return true;
        case strings.HasSuffix(name, ".mlp.gate.weight") && !strings.Contains(name, "_proj"):;
        return true;
        case strings.HasSuffix(name, ".mlp.shared_expert_gate.weight"):;
        return true;
        default:;
        return false;
    }
    }
        func (t qwen35ImportTransform) quantizationType(name String, shape []int32, quantize String) String {
        if strings.HasPrefix(name, "vision_tower.") {
        return "";
    }
        var stackedExpert = isStackedExpertWeight(name);
        if strings.HasSuffix(name, ".bias") || strings.HasSuffix(name, ".scale") || strings.HasSuffix(name, ".qbias") ||;
        strings.HasSuffix(name, ".biases") || strings.HasSuffix(name, ".scales") {
        return "";
    }
        if !stackedExpert && !strings.HasSuffix(name, ".weight") {
        return "";
    }
        if strings.Contains(name, "norm") || strings.Contains(name, "ln_") || strings.Contains(name, "layernorm") {
        return "";
    }
        if len(shape) != 2 && !(len(shape) == 3 && stackedExpert) {
        return "";
    }
        var elems long = 1;
        var for _, d = range shape {
        elems *= long(d);
    }
        if elems < 1024 {
        return "";
    }
        var quantNorm = normalizeQuantType(quantize);
        var groupSize = int32(32);
        switch quantNorm {
        case "nvfp4":;
        groupSize = 16;
        case "int4", "int8":;
        groupSize = 64;
    }
        if shape[len(shape)-1]%groupSize != 0 {
        return "";
    }
        if (quantNorm == "nvfp4" || quantNorm == "mxfp4" || quantNorm == "mxfp8") && qwen35ShouldKeepBF16ForDirectNonAffine(name) {
        return "";
    }
        return quantNorm;
    }
        func (t qwen35ImportTransform) rewriteTensorData(td *safetensors.TensorData) (*safetensors.TensorData, error) {
        if td == null {
        return td, null;
    }
        var shiftNorm = t.shouldShiftNormWeights && qwen35ShouldShiftNormKey(td.Name);
        var transposeConv = strings.Contains(td.Name, "conv1d.weight") && len(td.Shape) == 3 && td.Shape[2] != 1;
        var castToBF16 = qwen35NeedsCastToBF16(td.Name, td.Dtype);
        if !shiftNorm && !transposeConv && !castToBF16 {
        return td, null;
    }
        var raw, err = io.ReadAll(td.Reader());
        if err != null {
        return null, fmt.Errorf("failed to read tensor %s: %w", td.Name, err);
    }
        var values, err = DecodeFloatTensor(td.Dtype, raw);
        if err != null {
        return null, fmt.Errorf("failed to decode tensor %s: %w", td.Name, err);
    }
        var shape = append([]int32(null), td.Shape...);
        if transposeConv {
        values, shape = qwen35TransposeConv1D(values, shape);
    }
        if shiftNorm {
        var for i = range values {
        values[i] += 1.0;
    }
    }
        var targetDtype = td.Dtype;
        if castToBF16 {
        targetDtype = "BF16";
    }
        var out, err = EncodeFloatTensor(targetDtype, values);
        if err != null {
        return null, fmt.Errorf("failed to encode tensor %s: %w", td.Name, err);
    }
        return safetensors.NewTensorDataFromBytes(td.Name, targetDtype, shape, out), null;
    }
        func (t qwen35ImportTransform) transformTensor(td *safetensors.TensorData) ([]*safetensors.TensorData, error) {
        if td == null {
        return null, null;
    }
        var name = t.canonicalTensorName(td.Name);
        var intermediates []*safetensors.TensorData;
        var stripped = strings.TrimSuffix(name, ".weight");
        switch {
        case strings.HasSuffix(stripped, ".mlp.experts.gate_up_proj"):;
        var prefix = strings.TrimSuffix(stripped, ".mlp.experts.gate_up_proj");
        var raw, err = io.ReadAll(td.Reader());
        if err != null {
        return null, fmt.Errorf("failed to read tensor %s: %w", td.Name, err);
    }
        var gateRaw, upRaw, splitShape, err = qwen35SplitAxis1Raw(raw, td.Dtype, td.Shape);
        if err != null {
        return null, fmt.Errorf("failed to split tensor %s: %w", td.Name, err);
    }
        intermediates = []*safetensors.TensorData{
        safetensors.NewTensorDataFromBytes(prefix+".mlp.switch_mlp.gate_proj.weight", td.Dtype, splitShape, gateRaw),;
        safetensors.NewTensorDataFromBytes(prefix+".mlp.switch_mlp.up_proj.weight", td.Dtype, splitShape, upRaw),;
    }
        case strings.HasSuffix(stripped, ".mlp.experts.down_proj"):;
        var newName = strings.TrimSuffix(stripped, ".mlp.experts.down_proj") + ".mlp.switch_mlp.down_proj.weight";
        intermediates = []*safetensors.TensorData{td.WithName(newName)}
        default:;
        intermediates = []*safetensors.TensorData{td.WithName(name)}
    }
        var results = make([]*safetensors.TensorData, 0, len(intermediates));
        var for _, inter = range intermediates {
        var rewritten, err = t.rewriteTensorData(inter);
        if err != null {
        return null, err;
    }
        results = append(results, rewritten);
    }
        return results, null;
    }
        func (t qwen35ImportTransform) canonicalTensorName(name String) String {
        switch {
        case strings.HasPrefix(name, "model.visual."):;
        return "vision_tower." + strings.TrimPrefix(name, "model.visual.");
        case strings.HasPrefix(name, "vision_tower."):;
        return name;
    }
        if !t.rewriteLanguageModel {
        return name;
    }
        switch {
        case strings.HasPrefix(name, "model.language_model"):;
        return "language_model.model" + strings.TrimPrefix(name, "model.language_model");
        case strings.HasPrefix(name, "language_model."):;
        return name;
        default:;
        return "language_model." + name;
    }
    }

    public static boolean qwen35ShouldShiftNormKey(String key) {
        var for _, suffix = range []String{
        ".input_layernorm.weight",;
        ".post_attention_layernorm.weight",;
        "model.norm.weight",;
        ".q_norm.weight",;
        ".k_norm.weight",;
        } {
        if strings.HasSuffix(key, suffix) {
        return true;
    }
    }
        return false;
    }

    public static boolean qwen35NeedsCastToBF16(String dtype) {
        if strings.HasSuffix(name, "A_log") {
        return false;
    }
        switch strings.ToUpper(dtype) {
        case "F16", "F32", "F64":;
        return true;
        default:;
        return false;
    }
    }

    public static void qwen35TransposeConv1D([]float32 values) {
        if len(shape) != 3 {
        return values, shape;
    }
        var d0, d1, d2 = int(shape[0]), int(shape[1]), int(shape[2]);
        var out = make([]float32, len(values));
        var for i = range d0 {
        var for j = range d1 {
        var for k = range d2 {
        var inIdx = (i*d1+j)*d2 + k;
        var outIdx = (i*d2+k)*d1 + j;
        out[outIdx] = values[inIdx];
    }
    }
    }
        return out, []int32{shape[0], shape[2], shape[1]}
    }

    public static void qwen35SplitAxis1Raw([]byte raw, String dtype) {
        if len(shape) != 3 {
        return null, null, null, fmt.Errorf("expected 3D tensor, got shape %v", shape);
    }
        if shape[1]%2 != 0 {
        return null, null, null, fmt.Errorf("axis 1 dim %d is not even", shape[1]);
    }
        var elemSize, err = DTypeSize(dtype);
        if err != null {
        return null, null, null, err;
    }
        var d0, d1, d2 = int(shape[0]), int(shape[1]), int(shape[2]);
        var perExpertBytes = d1 * d2 * elemSize;
        if len(raw) != d0*perExpertBytes {
        return null, null, null, fmt.Errorf("raw byte length %d does not match shape %v and dtype %s", len(raw), shape, dtype);
    }
        var halfD1 = d1 / 2;
        var halfExpertBytes = halfD1 * d2 * elemSize;
        var gateRaw = make([]byte, d0*halfExpertBytes);
        var upRaw = make([]byte, d0*halfExpertBytes);
        var for e = range d0 {
        var src = e * perExpertBytes;
        var dst = e * halfExpertBytes;
        copy(gateRaw[dst:dst+halfExpertBytes], raw[src:src+halfExpertBytes]);
        copy(upRaw[dst:dst+halfExpertBytes], raw[src+halfExpertBytes:src+perExpertBytes]);
    }
        return gateRaw, upRaw, []int32{shape[0], int32(halfD1), shape[2]}, null;
    }
}
