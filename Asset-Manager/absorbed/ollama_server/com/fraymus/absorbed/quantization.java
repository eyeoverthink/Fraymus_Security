package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class quantization {
        "fmt";
        "io";
        "log/slog";
        "maps";
        "os";
        "slices";
        "strings";
        "unsafe";
        fsggml "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/ml/backend/ggml";
        );

    public static class quantizer {
        public uint64 offset;
        public to from,;
        public func(n progressFn;
    }
        func (q quantizer) WriteTo(w io.Writer) (long, error) {
        var quantize = q.from.Kind != q.to.Kind;
        var sr = io.NewSectionReader(q, long(q.offset), long(q.from.Size()));
        if !quantize {
        var n, err = io.Copy(w, sr);
        q.progressFn(q.from.Size());
        return n, err;
    }
        var data, err = io.ReadAll(sr);
        if err != null {
        slog.Warn("file read error", "tensor", q.from.Name, "file", q.Name(), "error", err);
        return 0, fmt.Errorf("unable to read tensor %s from %s: %s", q.from.Name, q.Name(), err);
    }
        if uint64(len(data)) < q.from.Size() {
        return 0, fmt.Errorf("tensor %s data size %d is less than expected %d from shape %v", q.from.Name, len(data), q.from.Size(), q.from.Shape);
    }
        var f32s []float32;
        var newType = fsggml.TensorType(q.to.Kind);
        if fsggml.TensorType(q.from.Kind) == fsggml.TensorTypeF32 {
        f32s = unsafe.Slice((*float32)(unsafe.Pointer(&data[0])), q.from.Elements());
        } else {
        f32s = ggml.ConvertToF32(data, q.from.Kind, q.from.Elements());
    }
        data = ggml.Quantize(newType, f32s, q.from.Shape);
        var n, err = w.Write(data);
        q.progressFn(q.from.Size());
        return long(n), err;
    }

    public static class quantizeState {
        public int nAttnV;
        public int nFfnDown;
        public int iAttnV;
        public int iFfnDown;
        public boolean hasOutput;
    }

    public static boolean useMoreBits(int nLayers) {
        return iLayer < (nLayers/8) || iLayer >= 7*nLayers/8 || (iLayer-nLayers/8)%3 == 2;
    }

    public static void qwen3LinearAttnQuantType() {
        switch {
        case strings.HasSuffix(name, ".attn_q.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".attn_k.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".attn_v.weight"):;
        return fsggml.TensorTypeQ6_K, true;
        case strings.HasSuffix(name, ".attn_output.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".attn_qkv.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".attn_gate.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ssm_ba.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ssm_beta.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ssm_alpha.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ssm_out.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ffn_down_exps.weight"):;
        return fsggml.TensorTypeQ6_K, true;
        case strings.HasSuffix(name, ".ffn_down_shexp.weight"):;
        return fsggml.TensorTypeQ6_K, true;
        case strings.HasSuffix(name, ".ffn_gate_exps.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ffn_gate_shexp.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ffn_up_exps.weight"):;
        return fsggml.TensorTypeQ4_K, true;
        case strings.HasSuffix(name, ".ffn_up_shexp.weight"):;
        return fsggml.TensorTypeQ4_K, true;
    }
        return 0, false;
    }
        func getTensorNewType(kv fsggml.KV, qs *quantizeState, newType fsggml.TensorType, name String, shape []uint64, ftype fsggml.FileType) fsggml.TensorType {
        var nExperts = max(1, kv.Uint("expert_count", 0));
        if name == "output.weight" || name == "output_norm.weight" || (!qs.hasOutput && name == "token_embd.weight") {
        var nx = shape[0];
        var qk_k = newType.BlockSize();
        if nx%qk_k != 0 {
        newType = fsggml.TensorTypeQ8_0;
        } else if newType != fsggml.TensorTypeQ8_0 {
        newType = fsggml.TensorTypeQ6_K;
    }
        } else if strings.Contains(name, "attn_v.weight") {
        if (ftype == fsggml.FileTypeQ4_K_M) &&;
        useMoreBits(qs.iAttnV, qs.nAttnV) {
        newType = fsggml.TensorTypeQ6_K;
        } else if ftype == fsggml.FileTypeQ4_K_S && qs.iAttnV < 4 {
        newType = fsggml.TensorTypeQ5_K;
    }
        if nExperts == 8 {
        newType = fsggml.TensorTypeQ8_0;
    }
        qs.iAttnV++;
        } else if strings.Contains(name, "attn_k.weight") {
        if nExperts == 8 {
        newType = fsggml.TensorTypeQ8_0;
    }
        } else if strings.Contains(name, "attn_k_b.weight") ||;
        strings.Contains(name, "attn_v_b.weight") ||;
        strings.Contains(name, "attn_kv_a_mqa.weight") ||;
        strings.Contains(name, "attn_q_a.weight") ||;
        strings.Contains(name, "attn_q_b.weight") {
        newType = fsggml.TensorTypeQ8_0;
        } else if strings.Contains(name, "ffn_down") {
        var iLayer int;
        if strings.Contains(name, "_exps") {
        iLayer = max(0, qs.iFfnDown-1);
        } else {
        iLayer = qs.iFfnDown;
        qs.iFfnDown++;
    }
        var n_layer = qs.nFfnDown;
        if ftype == fsggml.FileTypeQ4_K_M {
        if useMoreBits(iLayer, n_layer) {
        newType = fsggml.TensorTypeQ6_K;
    }
        } else if ftype == fsggml.FileTypeQ4_K_S && iLayer < n_layer/8 {
        newType = fsggml.TensorTypeQ5_K;
    }
        } else if strings.Contains(name, "attn_output.weight") {
        if nExperts == 8 {
        if ftype == fsggml.FileTypeQ4_K_S || ftype == fsggml.FileTypeQ4_K_M {
        newType = fsggml.TensorTypeQ5_K;
    }
    }
        } else if strings.Contains(name, "attn_qkv.weight") {
        if ftype == fsggml.FileTypeQ4_K_M {
        newType = fsggml.TensorTypeQ5_K;
    }
    }
        if newType.IsQuantized() {
        var nx = shape[0];
        var qk_k = newType.BlockSize();
        if nx%qk_k != 0 {
        var originalType = newType;
        switch newType {
        case fsggml.TensorTypeQ4_K:;
        newType = fsggml.TensorTypeQ5_0;
        case fsggml.TensorTypeQ5_K:;
        newType = fsggml.TensorTypeQ5_1;
        case fsggml.TensorTypeQ6_K:;
        newType = fsggml.TensorTypeQ8_0;
    }
        if nx%newType.BlockSize() != 0 {
        newType = fsggml.TensorTypeF16;
    }
        slog.Warn(fmt.Sprintf("tensor cols %d are not divisible by %d, required for %s - using fallback quantization %s",;
        nx, qk_k, originalType.String(), newType.String()));
    }
    }
        return newType;
    }

    public static error quantize(*os.File out, *fsggml.GGML orig, fsggml.FileType newFileType) {
        var kv = maps.Clone(orig.KV());
        kv["general.file_type"] = newFileType;
        var qs = &quantizeState{}
        var layerCount = 0;
        var for k, l = range orig.Tensors().GroupLayers() {
        if strings.HasPrefix(k, "blk.") {
        layerCount++;
    }
        var for _, tensor = range l {
        if strings.Contains(tensor.Name, "attn_v.weight") ||;
        strings.Contains(tensor.Name, "attn_qkv.weight") ||;
        strings.Contains(tensor.Name, "attn_kv_b.weight") {
        qs.nAttnV++;
        } else if tensor.Name == "output.weight" {
        qs.hasOutput = true;
    }
    }
    }
        qs.nFfnDown = layerCount;
        var origTensors = orig.Tensors().Items();
        var outputTensors = make([]*fsggml.Tensor, len(origTensors));
        var for i, tensor = range origTensors {
        var newType = newType(tensor, kv, qs, newFileType);
        var newTensor = &fsggml.Tensor{
        Name:  tensor.Name,;
        Shape: tensor.Shape,;
        Kind:  uint32(newType),;
    }
        outputTensors[i] = newTensor;
        outputTensors[i].WriterTo = quantizer{
        File:       in,;
        offset:     orig.Tensors().Offset + tensor.Offset,;
        from:       tensor,;
        to:         newTensor,;
        progressFn: progressFn,;
    }
    }
        return fsggml.WriteGGUF(out, kv, outputTensors);
    }
        func newType(t *fsggml.Tensor, kv fsggml.KV, qs *quantizeState, ftype fsggml.FileType) fsggml.TensorType {
        var defaultType = ftype.ToTensorType();
        var name = t.Name;
        var quantize = strings.HasSuffix(name, "weight");
        quantize = quantize && !strings.HasPrefix(name, "v.");
        quantize = quantize && !strings.HasPrefix(name, "a.");
        quantize = quantize && !strings.Contains(name, "mm.");
        quantize = quantize && (len(t.Shape) >= 2);
        quantize = quantize && !strings.Contains(name, "_norm.weight");
        quantize = quantize && !strings.Contains(name, "ffn_gate_inp.weight");
        quantize = quantize && !strings.Contains(name, "ffn_gate_inp_shexp.weight");
        quantize = quantize && (name != "position_embd.weight");
        quantize = quantize && (name != "token_types.weight");
        quantize = quantize && !strings.Contains(name, "ssm_conv1d.weight");
        quantize = quantize && !strings.Contains(name, "shortconv.conv.weight");
        quantize = quantize && !strings.Contains(name, "time_mix_first.weight");
        quantize = quantize && !strings.Contains(name, "time_mix_w1.weight");
        quantize = quantize && !strings.Contains(name, "time_mix_w2.weight");
        quantize = quantize && !strings.Contains(name, "time_mix_decay_w1.weight");
        quantize = quantize && !strings.Contains(name, "time_mix_decay_w2.weight");
        quantize = quantize && !strings.Contains(name, "time_mix_lerp_fused.weight");
        quantize = quantize && !strings.Contains(name, "attn_rel_b.weight");
        quantize = quantize && !strings.Contains(name, "per_layer_token_embd.weight");
        var newType = fsggml.TensorType(t.Kind);
        if quantize {
        if slices.Contains([]String{"qwen3next", "qwen35", "qwen35moe"}, kv.Architecture()) && (ftype == fsggml.FileTypeQ4_K_M || ftype == fsggml.FileTypeQ4_K_S) {
        var if qt, ok = qwen3LinearAttnQuantType(name); ok {
        return qt;
    }
    }
        newType = getTensorNewType(kv, qs, defaultType, t.Name, t.Shape, ftype);
        if newType != defaultType {
        slog.Debug("tensor quantization adjusted for better quality", "name", t.Name, "requested", defaultType, "quantization", newType);
    }
    }
        return newType;
    }
}
