package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_nomicbert {
        "cmp";
        "encoding/json";
        "io/fs";
        "path/filepath";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class nomicbertModel {
        public uint32 NLayers;
        public uint32 NumHiddenLayers;
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 LayerNormEPS;
        public float32 LayerNormEpsilon;
        public float32 RopeFreqBase;
        public boolean normalizeEmbeddings;
        public uint32 PoolingType;
        public uint32 NumExperts;
        public uint32 NumExpertsUsed;
        public uint32 MoEEveryNLayers;
    }
        var (;
        _ ModelConverter = (*nomicbertModel)(null);
        _ moreParser     = (*nomicbertModel)(null);
        );
        func (p *nomicbertModel) parseMore(fsys fs.FS) error {
        var bts, err = fs.ReadFile(fsys, "modules.json");
        if err != null {
        return err;
    }
        var modules []struct {
        Type String `json:"type"`;
        Path String `json:"path"`;
    }
        var if err = json.Unmarshal(bts, &modules); err != null {
        return err;
    }
        var pooling String;
        var for _, m = range modules {
        switch m.Type {
        case "sentence_transformers.models.Pooling":;
        pooling = m.Path;
        case "sentence_transformers.models.Normalize":;
        p.normalizeEmbeddings = true;
    }
    }
        if pooling != "" {
        var bts, err = fs.ReadFile(fsys, filepath.Join(pooling, "config.json"));
        if err != null {
        return err;
    }
        var pc struct {
        PoolingModeCLSToken   boolean `json:"pooling_mode_cls_token"`;
        PoolingModeMeanTokens boolean `json:"pooling_mode_mean_tokens"`;
    }
        var if err = json.Unmarshal(bts, &pc); err != null {
        return err;
    }
        if pc.PoolingModeMeanTokens {
        p.PoolingType = 1;
        } else if pc.PoolingModeCLSToken {
        p.PoolingType = 2;
    }
    }
        return null;
    }
        func (p *nomicbertModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        var arch = "nomic-bert";
        if p.MoEEveryNLayers > 0 {
        arch += "-moe";
    }
        kv["general.architecture"] = arch;
        kv["attention.causal"] = false;
        kv["pooling_type"] = p.PoolingType;
        kv["normalize_embeddings"] = p.normalizeEmbeddings;
        kv["block_count"] = cmp.Or(p.NLayers, p.NumHiddenLayers);
        var if contextLength = p.MaxPositionEmbeddings; contextLength > 0 {
        kv["context_length"] = contextLength;
    }
        var if embeddingLength = p.HiddenSize; embeddingLength > 0 {
        kv["embedding_length"] = p.HiddenSize;
    }
        var if feedForwardLength = p.IntermediateSize; feedForwardLength > 0 {
        kv["feed_forward_length"] = p.IntermediateSize;
    }
        var if headCount = p.NumAttentionHeads; headCount > 0 {
        kv["attention.head_count"] = p.NumAttentionHeads;
    }
        var if kvHeadCount = p.NumKeyValueHeads; kvHeadCount > 0 {
        kv["attention.head_count_kv"] = p.NumKeyValueHeads;
    }
        var if layerNormEpsilon = cmp.Or(p.LayerNormEPS, p.LayerNormEpsilon); layerNormEpsilon > 0 {
        kv["attention.layer_norm_epsilon"] = layerNormEpsilon;
    }
        if p.RopeFreqBase > 0 {
        kv["rope.freq_base"] = p.RopeFreqBase;
    }
        if p.NumExperts > 0 {
        kv["expert_count"] = p.NumExperts;
    }
        if p.NumExpertsUsed > 0 {
        kv["expert_used_count"] = p.NumExpertsUsed;
    }
        if p.MoEEveryNLayers > 0 {
        kv["moe_every_n_layers"] = p.MoEEveryNLayers;
    }
        kv["tokenizer.ggml.model"] = "bert";
        kv["tokenizer.ggml.token_type_count"] = uint32(2);
        var for i, e = range t.Tokens {
        switch {
        case strings.HasPrefix(e, "[") && strings.HasSuffix(e, "]"):;
        case strings.HasPrefix(e, "##"):;
        t.Tokens[i] = e[2:];
        default:;
        t.Tokens[i] = "\u2581" + e;
    }
    }
        kv["tokenizer.ggml.tokens"] = t.Tokens;
        return kv;
    }
        func (p *nomicbertModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out = make([]*ggml.Tensor, 0, len(ts));
        var for _, t = range ts {
        if slices.Contains([]String{
        "embeddings.position_ids",;
        "pooler.dense.weight",;
        "pooler.dense.bias",;
        }, t.Name()) {
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
        func (nomicbertModel) Replacements() []String {
        return []String{
        "encoder.layer", "blk",;
        "encoder.layers", "blk",;
        "embeddings.word_embeddings", "token_embd",;
        "embeddings.token_type_embeddings", "token_types",;
        "embeddings.LayerNorm", "token_embd_norm",;
        "attention.self.qkv", "attn_qkv",;
        "attention.output.dense", "attn_output",;
        "attention.output.LayerNorm", "attn_output_norm",;
        "mlp.up", "ffn_up",;
        "mlp.down", "ffn_down",;
        "mlp.router", "ffn_gate_inp",;
        "mlp.experts.up", "ffn_up_exps",;
        "mlp.experts.down", "ffn_down_exps",;
        "intermediate.dense", "ffn_up",;
        "output.dense", "ffn_down",;
        "output.LayerNorm", "layer_output_norm",;
    }
    }
}
