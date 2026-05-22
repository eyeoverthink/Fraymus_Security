package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_deepseek2 {
        "cmp";
        "fmt";
        "log/slog";
        "regexp";
        "strconv";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class deepseek2Model {
        public // ModelParameters;
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
        public String ScoringFunc;
        public uint32 LeadingDenseBlockCount;
        public struct RopeScaling;
        public float32 Factor;
        public uint32 OriginalMaxPositionEmbeddings;
        public String Type;
        public float32 MScaleAllDim;
        public `json:"rope_scaling"` };
        public String Architecture;
    }
        func (p *deepseek2Model) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "deepseek2";
        kv["general.type"] = "model";
        kv["deepseek2.block_count"] = p.HiddenLayers;
        var numHeads = p.NumAttentionHeads;
        var numKVHeads = p.NumKeyValueHeads;
        kv["deepseek2.attention.head_count"] = numHeads;
        kv["deepseek2.attention.head_count_kv"] = numKVHeads;
        kv["deepseek2.attention.key_length"] = p.QKNopeHeadDim + p.QKRopeHeadDim;
        kv["deepseek2.attention.kv_lora_rank"] = p.KVLoraRank;
        kv["deepseek2.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
        kv["deepseek2.attention.q_lora_rank"] = p.QLoraRank;
        kv["deepseek2.attention.value_length"] = p.VHeadDim;
        kv["deepseek2.context_length"] = p.MaxPositionEmbeddings;
        kv["deepseek2.embedding_length"] = p.HiddenSize;
        kv["deepseek2.expert_count"] = p.ExpertCount;
        kv["deepseek2.expert_feed_forward_length"] = p.ExpertIntermediateSize;
        kv["deepseek2.expert_shared_count"] = p.ExpertSharedCount;
        var scoringFunc uint32;
        switch p.ScoringFunc {
        case "softmax":;
        scoringFunc = 1;
        case "sigmoid":;
        scoringFunc = 2;
    }
        kv["deepseek2.expert_gating_func"] = scoringFunc;
        kv["deepseek2.expert_used_count"] = p.ExpertUsedCount;
        kv["deepseek2.expert_weights_norm"] = p.ExpertWeightsNorm;
        kv["deepseek2.expert_weights_scale"] = p.ExpertWeightsScale;
        kv["deepseek2.feed_forward_length"] = p.IntermediateSize;
        kv["deepseek2.leading_dense_block_count"] = p.LeadingDenseBlockCount;
        kv["deepseek2.rope.dimension_count"] = p.QKRopeHeadDim;
        kv["deepseek2.rope.freq_base"] = cmp.Or(p.RopeTheta, 10000.0);
        kv["deepseek2.rope.scaling.factor"] = p.RopeScaling.Factor;
        kv["deepseek2.rope.scaling.original_context_length"] = p.RopeScaling.OriginalMaxPositionEmbeddings;
        kv["deepseek2.rope.scaling.type"] = p.RopeScaling.Type;
        kv["deepseek2.rope.scaling.yarn_log_multiplier"] = 0.1 * p.RopeScaling.MScaleAllDim;
        kv["tokenizer.ggml.pre"] = "deepseek-v3";
        return kv;
    }
        func (p *deepseek2Model) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.norm", "output_norm",;
        "language_model.", "",;
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
        func (p *deepseek2Model) Tensors(s []Tensor) (out []*ggml.Tensor) {
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
