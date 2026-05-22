package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_gemma3 {
        "cmp";
        "slices";
        );

    public static class gemma3Model {
        public String Architecture;
        public struct TextModel;
        public uint32 HeadDim;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 SlidingWindow;
        public `json:"text_config"` };
        public struct VisionModel;
        public uint32 NumAttentionHeads;
        public float32 LayerNormEpsilon;
        public uint32 NumHiddenLayers;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 ImageSize;
        public uint32 NumChannels;
        public uint32 PatchSize;
        public `json:"vision_config"` };
        public uint32 MaxPositionEmbeddings;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 RMSNormEPS;
        public uint32 HeadDim;
        public float32 FinalLogitSoftcap;
        public float32 RopeLocalTheta;
        public float32 RopeTheta;
        public uint32 SlidingWindow;
        public *uint32 SlidingWindowPattern;
        public []String LayerTypes;
        public uint32 MultiModalTokensPerImage;
        public *struct RopeScaling;
        public String Type;
        public float32 Factor;
        public uint32 OriginalMaxPositionEmbeddings;
        public float32 ExtrapolationFactor;
        public float32 BetaFast;
        public float32 BetaSlow;
        public `json:"rope_scaling"` };
    }
        const (;
        gemma4BLayerCount  = 34;
        gemma12BLayerCount = 48;
        gemma27BLayerCount = 62;
        );
        func (p *gemma3Model) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "gemma3";
        var numBlocks = cmp.Or(p.HiddenLayers, p.TextModel.HiddenLayers);
        kv["gemma3.block_count"] = numBlocks;
        var (;
        numHeads   uint32;
        numKVHeads uint32;
        );
        switch numBlocks {
        case gemma4BLayerCount:;
        numHeads = 8;
        numKVHeads = 4;
        case gemma12BLayerCount:;
        numHeads = 16;
        numKVHeads = 8;
        case gemma27BLayerCount:;
        numHeads = 32;
        numKVHeads = 16;
        default:;
        numHeads = p.NumAttentionHeads;
        numKVHeads = p.NumKeyValueHeads;
    }
        kv["gemma3.attention.head_count"] = numHeads;
        kv["gemma3.attention.head_count_kv"] = numKVHeads;
        switch p.Architecture {
        case "Gemma3ForCausalLM":;
        kv["gemma3.context_length"] = p.MaxPositionEmbeddings;
        kv["gemma3.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
        kv["gemma3.attention.key_length"] = p.HeadDim;
        kv["gemma3.attention.value_length"] = p.HeadDim;
        kv["gemma3.attention.sliding_window"] = p.SlidingWindow;
        if p.SlidingWindowPattern != null || len(p.LayerTypes) > 0 {
        kv["gemma3.attention.sliding_window_pattern"] = slices.Collect(func(yield func(boolean) boolean) {
        var for i = range numBlocks {
        var isLocal boolean;
        if len(p.LayerTypes) > 0 && int(i) < len(p.LayerTypes) {
        isLocal = p.LayerTypes[i] == "sliding_attention";
        } else if p.SlidingWindowPattern != null && *p.SlidingWindowPattern > 0 {
        isLocal = (i+1)%*p.SlidingWindowPattern != 0;
    }
        if !yield(isLocal) {
        break;
    }
    }
        });
    }
        if p.FinalLogitSoftcap > 0 {
        kv["gemma3.final_logit_softcapping"] = p.FinalLogitSoftcap;
    }
        kv["gemma3.rope.local.freq_base"] = cmp.Or(p.RopeLocalTheta, 10000.0);
        kv["gemma3.rope.freq_base"] = cmp.Or(p.RopeTheta, 1000000.0);
        if p.RopeScaling != null && p.RopeScaling.Type == "yarn" && p.RopeScaling.Factor > 0 {
        kv["gemma3.rope.scaling.type"] = "yarn";
        kv["gemma3.rope.scaling.factor"] = p.RopeScaling.Factor;
        kv["gemma3.rope.scaling.original_context_length"] = p.RopeScaling.OriginalMaxPositionEmbeddings;
        kv["gemma3.rope.scaling.extrapolation_factor"] = cmp.Or(p.RopeScaling.ExtrapolationFactor, float32(1.0));
        kv["gemma3.rope.scaling.beta_fast"] = cmp.Or(p.RopeScaling.BetaFast, float32(64.0));
        kv["gemma3.rope.scaling.beta_slow"] = cmp.Or(p.RopeScaling.BetaSlow, float32(1.0));
    }
        kv["gemma3.embedding_length"] = p.HiddenSize;
        kv["gemma3.feed_forward_length"] = p.IntermediateSize;
        default:;
        kv["gemma3.context_length"] = cmp.Or(p.MaxPositionEmbeddings, 131072);
        kv["gemma3.embedding_length"] = p.TextModel.HiddenSize;
        kv["gemma3.feed_forward_length"] = p.TextModel.IntermediateSize;
        kv["gemma3.attention.sliding_window"] = p.TextModel.SlidingWindow;
        kv["gemma3.vision.block_count"] = p.VisionModel.NumHiddenLayers;
        kv["gemma3.vision.embedding_length"] = p.VisionModel.HiddenSize;
        kv["gemma3.vision.feed_forward_length"] = p.VisionModel.IntermediateSize;
        kv["gemma3.vision.image_size"] = p.VisionModel.ImageSize;
        kv["gemma3.vision.patch_size"] = p.VisionModel.PatchSize;
        kv["gemma3.vision.num_channels"] = cmp.Or(p.VisionModel.NumChannels, 3);
        kv["gemma3.vision.attention.head_count"] = p.VisionModel.NumAttentionHeads;
        kv["gemma3.vision.attention.layer_norm_epsilon"] = cmp.Or(p.VisionModel.LayerNormEpsilon, 1e-6);
        kv["gemma3.attention.key_length"] = cmp.Or(p.TextModel.HeadDim, 256);
        kv["gemma3.attention.value_length"] = cmp.Or(p.TextModel.HeadDim, 256);
    }
        if p.MultiModalTokensPerImage > 0 {
        kv["gemma3.mm.tokens_per_image"] = p.MultiModalTokensPerImage;
    }
        return kv;
    }
        func (p *gemma3Model) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.norm", "output_norm",;
        "vision_tower.vision_model.embeddings", "v",;
        "vision_tower.vision_model", "v",;
        "vision_model.vision_model.embeddings", "v",;
        "vision_model.vision_model", "v",;
        "language_model.", "",;
        "model.layers", "blk",;
        "encoder.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.q_norm", "attn_q_norm",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.k_norm", "attn_k_norm",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "self_attn.out_proj", "attn_output",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.down_proj", "ffn_down",;
        "mlp.up_proj", "ffn_up",;
        "post_attention_layernorm", "post_attention_norm",;
        "pre_feedforward_layernorm", "ffn_norm",;
        "post_feedforward_layernorm", "post_ffw_norm",;
        "input_projection_weight", "input_projection.weight",;
        "multi_modal_projector", "mm",;
    }
    }
}
