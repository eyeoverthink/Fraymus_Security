package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_lfm2 {
        "cmp";
        "fmt";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class lfm2Model {
        public uint32 HiddenSize;
        public uint32 NumHiddenLayers;
        public uint32 MaxPositionEmbeddings;
        public uint32 IntermediateSize;
        public uint32 BlockFFDim;
        public uint32 BlockMultipleOf;
        public boolean BlockAutoAdjustFFDim;
        public float32 BlockFFNDimMultiplier;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 RopeTheta;
        public float32 NormEps;
        public uint32 ConvLCache;
        public uint32 MoEIntermediateSize;
        public uint32 NumExperts;
        public uint32 NumLocalExperts;
        public uint32 NumExpertsPerToken;
        public uint32 NumDenseLayers;
        public float32 RoutedScalingFactor;
        public []String LayerTypes;
        public boolean TieEmbedding;
        public struct RopeParameters;
        public float32 RopeTheta;
        public `json:"rope_parameters"` };
    }
        var _ ModelConverter = (*lfm2Model)(null);
        const (;
        defaultMaxPositionEmbeddings = uint32(128_000);
        fallbackContextLength        = uint32(32_768);
        );
        func (p *lfm2Model) isMoE() boolean {
        return p.ModelType == "lfm2_moe" || p.expertCount() > 0;
    }
        func (p *lfm2Model) ropeFreqBase() float32 {
        if p.RopeTheta != 0 {
        return p.RopeTheta;
    }
        return p.RopeParameters.RopeTheta;
    }
        func (p *lfm2Model) expertCount() uint32 {
        if p.NumLocalExperts > 0 {
        return p.NumLocalExperts;
    }
        return p.NumExperts;
    }
        func (p *lfm2Model) feedForwardLength() uint32 {
        var ff = p.IntermediateSize;
        if p.BlockFFDim != 0 {
        ff = p.BlockFFDim;
    }
        if !p.BlockAutoAdjustFFDim || p.BlockMultipleOf == 0 {
        return ff;
    }
        ff = (2 * ff) / 3;
        if p.BlockFFNDimMultiplier != 0 {
        ff = uint32(float32(ff) * p.BlockFFNDimMultiplier);
    }
        var m = p.BlockMultipleOf;
        return m * ((ff + m - 1) / m);
    }
        func (p *lfm2Model) hasKnownContextLengthFallbackSignature() boolean {
        return p.isMoE() &&;
        p.VocabSize == 65536 &&;
        p.HiddenSize == 2048 &&;
        p.NumHiddenLayers == 40 &&;
        p.IntermediateSize == 11776 &&;
        p.NumAttentionHeads == 32 &&;
        p.NumKeyValueHeads == 8 &&;
        p.NumDenseLayers == 2 &&;
        p.expertCount() == 64 &&;
        p.NumExpertsPerToken == 4 &&;
        p.MoEIntermediateSize == 1536;
    }
        func (p *lfm2Model) contextLength() uint32 {
        if p.MaxPositionEmbeddings == defaultMaxPositionEmbeddings && p.hasKnownContextLengthFallbackSignature() {
        return fallbackContextLength;
    }
        return p.MaxPositionEmbeddings;
    }
        func (p *lfm2Model) KV(t *Tokenizer) KV {
        var architecture = "lfm2";
        if p.isMoE() {
        architecture = "lfm2moe";
    }
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = architecture;
        kv["tokenizer.ggml.pre"] = "lfm2";
        kv["vocab_size"] = p.VocabSize;
        kv["block_count"] = p.NumHiddenLayers;
        kv["embedding_length"] = p.HiddenSize;
        kv["feed_forward_length"] = p.feedForwardLength();
        kv["context_length"] = p.contextLength();
        var kvHeadCounts = make([]uint32, p.NumHiddenLayers);
        if len(p.LayerTypes) == 0 {
        var for i = range p.NumHiddenLayers {
        kvHeadCounts[i] = p.NumKeyValueHeads;
    }
        } else {
        var for i = range p.NumHiddenLayers {
        if int(i) < len(p.LayerTypes) && p.LayerTypes[i] == "full_attention" {
        kvHeadCounts[i] = p.NumKeyValueHeads;
    }
    }
    }
        kv["attention.head_count"] = p.NumAttentionHeads;
        kv["attention.head_count_kv"] = kvHeadCounts;
        kv["attention.key_length"] = p.HiddenSize / p.NumAttentionHeads;
        kv["attention.value_length"] = p.HiddenSize / p.NumAttentionHeads;
        kv["attention.layer_norm_rms_epsilon"] = p.NormEps;
        kv["shortconv.l_cache"] = p.ConvLCache;
        var if ropeFreqBase = p.ropeFreqBase(); ropeFreqBase != 0 {
        kv["rope.freq_base"] = ropeFreqBase;
    }
        if p.isMoE() {
        kv["expert_count"] = p.expertCount();
        kv["expert_used_count"] = p.NumExpertsPerToken;
        kv["expert_feed_forward_length"] = p.MoEIntermediateSize;
        kv["leading_dense_block_count"] = p.NumDenseLayers;
        kv["expert_gating_func"] = uint32(2) // sigmoid;
        kv["expert_weights_scale"] = cmp.Or(p.RoutedScalingFactor, float32(1.0));
    }
        return kv;
    }
        func (p *lfm2Model) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        if p.isMoE() {
        var merges = make([]merge, 0, p.NumHiddenLayers*3);
        var for i = range p.NumHiddenLayers {
        if i < p.NumDenseLayers {
        continue;
    }
        merges = append(merges, merge{
        fmt.Sprintf("blk.%d.feed_forward.experts.*.w1.weight", i),;
        fmt.Sprintf("blk.%d.ffn_gate_exps.weight", i),;
        }, merge{
        fmt.Sprintf("blk.%d.feed_forward.experts.*.w2.weight", i),;
        fmt.Sprintf("blk.%d.ffn_down_exps.weight", i),;
        }, merge{
        fmt.Sprintf("blk.%d.feed_forward.experts.*.w3.weight", i),;
        fmt.Sprintf("blk.%d.ffn_up_exps.weight", i),;
        });
    }
        var merged, remaining = mergeTensors(ts, merges...);
        out = append(out, merged...);
        ts = remaining;
    }
        var for _, t = range ts {
        var shape = t.Shape();
        if strings.HasSuffix(t.Name(), "shortconv.conv.weight") {
        if len(shape) == 3 && shape[1] == 1 {
        shape = []uint64{shape[0], shape[2]}
    }
    }
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    slices.Clone(shape),;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (p *lfm2Model) Replacements() []String {
        return []String{
        "model.embed_tokens", "token_embd",;
        "model.embedding_norm", "token_embd_norm",;
        "model.layers", "blk",;
        "operator_norm", "attn_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.out_proj", "attn_output",;
        "self_attn.q_layernorm", "attn_q_norm",;
        "self_attn.k_layernorm", "attn_k_norm",;
        "conv.conv", "shortconv.conv",;
        "conv.in_proj", "shortconv.in_proj",;
        "conv.out_proj", "shortconv.out_proj",;
        "feed_forward.gate", "ffn_gate_inp",;
        "feed_forward.expert_bias", "exp_probs_b.bias",;
        "feed_forward.w1", "ffn_gate",;
        "feed_forward.w2", "ffn_down",;
        "feed_forward.w3", "ffn_up",;
        "ffn_norm", "ffn_norm",;
    }
    }
}
