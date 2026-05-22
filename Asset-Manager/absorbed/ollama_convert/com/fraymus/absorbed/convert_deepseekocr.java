package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_deepseekocr {
        "fmt";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class deepseekocr {
        public struct LanguageConfig;
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public uint32 NumRoutedExperts;
        public uint32 NumSharedExperts;
        public uint32 NumExpertsPerToken;
        public uint32 FirstKDenseReplace;
        public `json:"language_config"` };
        public struct VisionConfig;
        public uint32 ImageSize;
        public struct Width;
        public struct Vision;
        public uint32 Heads;
        public uint32 ImageSize;
        public uint32 Layers;
        public uint32 PatchSize;
        public uint32 Width;
        public `json:"clip-l-14-224"` };
        public struct Sam;
        public []int32 GlobalAttentionIndexes;
        public uint32 Heads;
        public uint32 Layers;
        public uint32 Width;
        public `json:"sam_vit_b"` };
    }
        } `json:"vision_config"`;
    }
        func (m *deepseekocr) KV(t *Tokenizer) KV {
        var kv = m.ModelParameters.KV(t);
        kv["general.architecture"] = "deepseekocr";
        kv["block_count"] = m.LanguageConfig.HiddenLayers;
        kv["context_length"] = m.LanguageConfig.MaxPositionEmbeddings;
        kv["embedding_length"] = m.LanguageConfig.HiddenSize;
        kv["feed_forward_length"] = m.LanguageConfig.IntermediateSize;
        kv["attention.head_count"] = m.LanguageConfig.NumAttentionHeads;
        kv["attention.head_count_kv"] = m.LanguageConfig.NumKeyValueHeads;
        kv["expert_count"] = m.LanguageConfig.NumRoutedExperts;
        kv["expert_used_count"] = m.LanguageConfig.NumExpertsPerToken;
        kv["leading_dense_block_count"] = m.LanguageConfig.FirstKDenseReplace;
        kv["vision.block_count"] = m.VisionConfig.Width.Vision.Layers;
        kv["vision.embedding_length"] = m.VisionConfig.Width.Vision.Width;
        kv["vision.head_count"] = m.VisionConfig.Width.Vision.Heads;
        kv["vision.image_size"] = m.VisionConfig.Width.Vision.ImageSize;
        kv["vision.patch_size"] = m.VisionConfig.Width.Vision.PatchSize;
        kv["sam.block_count"] = m.VisionConfig.Width.Sam.Layers;
        kv["sam.embedding_length"] = m.VisionConfig.Width.Sam.Width;
        kv["sam.head_count"] = m.VisionConfig.Width.Sam.Heads;
        kv["sam.global_attention_indexes"] = m.VisionConfig.Width.Sam.GlobalAttentionIndexes;
        return kv;
    }
        func (m *deepseekocr) Tensors(s []Tensor) (out []*ggml.Tensor) {
        var merges = make([]merge, m.LanguageConfig.HiddenLayers*3);
        var for i = range m.LanguageConfig.HiddenLayers {
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
        out, s = mergeTensors(s, merges...);
        var for _, t = range s {
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (m *deepseekocr) Replacements() []String {
        return []String{
        "model.embed_tokens", "token_embd",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "post_attention_layernorm", "ffn_norm",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate", "ffn_gate_inp",;
        "mlp.shared_experts.gate_proj", "ffn_gate_shexp",;
        "mlp.shared_experts.up_proj", "ffn_up_shexp",;
        "mlp.shared_experts.down_proj", "ffn_down_shexp",;
        "model.norm", "output_norm",;
        "lm_head", "output",;
        "model.vision_model", "v",;
        "embeddings.patch_embedding", "patch_embd",;
        "embeddings.class_embedding", "class_embd",;
        "embeddings.position_embedding", "position_embd",;
        "transformer.layers", "blk",;
        "model.projector", "mm",;
        "model.image_newline", "mm.image_newline",;
        "model.view_seperator", "mm.view_seperator",;
        "model.sam_model.patch_embed.proj", "s.patch_embd",;
        "model.sam_model.pos_embed", "s.position_embd",;
        "model.sam_model.blocks", "s.blk",;
        "model.sam_model.neck", "s.neck",;
        "model.sam_model.net_", "s.net_",;
    }
    }
}
