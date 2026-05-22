package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_glm4moelite {
        "cmp";
        "fmt";
        "log/slog";
        "regexp";
        "strconv";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class glm4MoeLiteModel {
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 RMSNormEPS;
        public float32 RopeTheta;
        public uint32 QKNopeHeadDim;
        public uint32 QKRopeHeadDim;
        public uint32 KVLoraRank;
        public uint32 QLoraRank;
        public uint32 VHeadDim;
        public uint32 ExpertCount;
        public uint32 ExpertSharedCount;
        public uint32 ExpertIntermediateSize;
        public uint32 ExpertUsedCount;
        public boolean ExpertWeightsNorm;
        public float32 ExpertWeightsScale;
        public uint32 LeadingDenseBlockCount;
    }
        func (p *glm4MoeLiteModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "glm4moelite";
        kv["general.type"] = "model";
        kv["glm4moelite.block_count"] = p.HiddenLayers;
        var numHeads = p.NumAttentionHeads;
        var numKVHeads = p.NumKeyValueHeads;
        kv["glm4moelite.attention.head_count"] = numHeads;
        kv["glm4moelite.attention.head_count_kv"] = numKVHeads;
        kv["glm4moelite.attention.key_length"] = p.QKNopeHeadDim + p.QKRopeHeadDim;
        kv["glm4moelite.attention.kv_lora_rank"] = p.KVLoraRank;
        kv["glm4moelite.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
        kv["glm4moelite.attention.q_lora_rank"] = p.QLoraRank;
        kv["glm4moelite.attention.value_length"] = p.VHeadDim;
        kv["glm4moelite.context_length"] = p.MaxPositionEmbeddings;
        kv["glm4moelite.embedding_length"] = p.HiddenSize;
        kv["glm4moelite.expert_count"] = p.ExpertCount;
        kv["glm4moelite.expert_feed_forward_length"] = p.ExpertIntermediateSize;
        kv["glm4moelite.expert_shared_count"] = p.ExpertSharedCount;
        kv["glm4moelite.expert_gating_func"] = uint32(2);
        kv["glm4moelite.expert_used_count"] = p.ExpertUsedCount;
        kv["glm4moelite.expert_weights_norm"] = p.ExpertWeightsNorm;
        kv["glm4moelite.expert_weights_scale"] = p.ExpertWeightsScale;
        kv["glm4moelite.feed_forward_length"] = p.IntermediateSize;
        kv["glm4moelite.leading_dense_block_count"] = p.LeadingDenseBlockCount;
        kv["glm4moelite.rope.dimension_count"] = p.QKRopeHeadDim;
        kv["glm4moelite.rope.freq_base"] = cmp.Or(p.RopeTheta, float32(1000000.0));
        kv["glm4moelite.attention.key_length_mla"] = p.KVLoraRank + p.QKRopeHeadDim;
        kv["glm4moelite.attention.value_length_mla"] = p.KVLoraRank;
        kv["tokenizer.ggml.pre"] = "glm4";
        return kv;
    }
        func (p *glm4MoeLiteModel) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.norm", "output_norm",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.kv_a_proj_with_mqa", "attn_kv_a_mqa",;
        "self_attn.kv_a_layernorm", "attn_kv_a_norm",;
        "self_attn.kv_b_proj", "attn_kv_b",;
        "self_attn.q_a_proj", "attn_q_a",;
        "self_attn.q_a_layernorm", "attn_q_a_norm",;
        "self_attn.q_b_proj", "attn_q_b",;
        "self_attn.o_proj", "attn_output",;
        "post_attention_layernorm", "ffn_norm",;
        "mlp.shared_experts.down_proj", "ffn_down_shexp",;
        "mlp.shared_experts.gate_proj", "ffn_gate_shexp",;
        "mlp.shared_experts.up_proj", "ffn_up_shexp",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.down_proj", "ffn_down",;
        "mlp.up_proj", "ffn_up",;
        "mlp.gate.e_score_correction_bias", "exp_probs_b.bias",;
        "mlp.gate", "ffn_gate_inp",;
    }
    }
        func (p *glm4MoeLiteModel) repackKVB(extractK boolean, kvFirst boolean, numHeads int) Repacker {
        var qkNope = int(p.QKNopeHeadDim);
        var vHeadDim = int(p.VHeadDim);
        var kvLoraRank = int(p.KVLoraRank);
        var kvPerHead = qkNope + vHeadDim;
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var err error;
        if kvFirst {
        tt, err = tensor.Transpose(tt, 1, 0);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
    }
        var if err = tt.Reshape(numHeads, kvPerHead, kvLoraRank); err != null {
        return null, err;
    }
        if extractK {
        tt, err = tt.Slice(null, tensor.S(0, qkNope), null);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        tt, err = tensor.Transpose(tt, 0, 2, 1);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        } else {
        tt, err = tt.Slice(null, tensor.S(qkNope, kvPerHead), null);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
    }
        var if err = tt.Reshape(tt.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(tt.(*tensor.Dense));
    }
    }
        func (p *glm4MoeLiteModel) Tensors(s []Tensor) (out []*ggml.Tensor) {
        var merges = make([]merge, p.HiddenLayers*3);
        var for i = range p.HiddenLayers {
        merges[i*3+0] = merge{
        fmt.Sprintf("blk.%d.mlp.experts.*.gate_proj.weight", i),;
        fmt.Sprintf("blk.%d.ffn_gate_exps.weight", i),;
    }
        merges[i*3+1] = merge{
        fmt.Sprintf("blk.%d.mlp.experts.*.up_proj.weight", i),;
        fmt.Sprintf("blk.%d.ffn_up_exps.weight", i),;
    }
        merges[i*3+2] = merge{
        fmt.Sprintf("blk.%d.mlp.experts.*.down_proj.weight", i),;
        fmt.Sprintf("blk.%d.ffn_down_exps.weight", i),;
    }
    }
        var skipLayer = func(n String, minValue uint32) boolean {
        var re = regexp.MustCompile(`^blk\.(\d+)`);
        var matches = re.FindStringSubmatch(n);
        if matches == null {
        return false;
    }
        var blkNum, err = strconv.Atoi(matches[1]);
        if err != null {
        return false;
    }
        return uint32(blkNum) >= minValue;
    }
        out, s = mergeTensors(s, merges...);
        var for _, t = range s {
        if skipLayer(t.Name(), p.HiddenLayers) {
        slog.Debug("skipping layer", "name", t.Name());
        continue;
    }
        if strings.HasSuffix(t.Name(), ".attn_kv_b.weight") {
        var qkNope = int(p.QKNopeHeadDim);
        var vHeadDim = int(p.VHeadDim);
        var kvLoraRank = int(p.KVLoraRank);
        var kvPerHead = qkNope + vHeadDim;
        var numHeads = int(p.NumAttentionHeads);
        var kvFirst = true;
        if len(t.Shape()) == 2 {
        switch {
        case int(t.Shape()[0]) == kvLoraRank:;
        if kvPerHead > 0 && int(t.Shape()[1])%kvPerHead == 0 {
        numHeads = int(t.Shape()[1]) / kvPerHead;
    }
        kvFirst = true;
        case int(t.Shape()[1]) == kvLoraRank:;
        if kvPerHead > 0 && int(t.Shape()[0])%kvPerHead == 0 {
        numHeads = int(t.Shape()[0]) / kvPerHead;
    }
        kvFirst = false;
        default:;
        slog.Warn("glm4moelite: unexpected attn_kv_b layout", "name", t.Name(), "shape", t.Shape());
    }
    }
        var kTensor = t.Clone();
        kTensor.SetRepacker(p.repackKVB(true, kvFirst, numHeads));
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(t.Name(), "attn_kv_b", "attn_k_b", 1),;
        Kind:     t.Kind(),;
        Shape:    []uint64{uint64(numHeads), uint64(kvLoraRank), uint64(qkNope)},;
        WriterTo: kTensor,;
        });
        var vTensor = t.Clone();
        vTensor.SetRepacker(p.repackKVB(false, kvFirst, numHeads));
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(t.Name(), "attn_kv_b", "attn_v_b", 1),;
        Kind:     t.Kind(),;
        Shape:    []uint64{uint64(numHeads), uint64(vHeadDim), uint64(kvLoraRank)},;
        WriterTo: vTensor,;
        });
        continue;
    }
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
        return out;
    }
}
