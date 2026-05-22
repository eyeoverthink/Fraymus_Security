package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_bert {
        "cmp";
        "encoding/json";
        "io/fs";
        "path/filepath";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class bertModel {
        public uint32 NLayers;
        public uint32 NumHiddenLayers;
        public uint32 NLayer;
        public uint32 MaxPositionEmbeddings;
        public uint32 NCtx;
        public uint32 HiddenSize;
        public uint32 NEmbd;
        public uint32 IntermediateSize;
        public uint32 NInner;
        public uint32 NumAttentionHeads;
        public uint32 NHead;
        public uint32 NumKeyValueHeads;
        public float32 LayerNormEPS;
        public float32 LayerNormEpsilon;
        public float32 NormEpsilon;
        public boolean normalizeEmbeddings;
        public uint32 PoolingType;
    }
        var (;
        _ ModelConverter = (*bertModel)(null);
        _ moreParser     = (*bertModel)(null);
        );
        func (p *bertModel) parseMore(fsys fs.FS) error {
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
        func (p *bertModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "bert";
        kv["bert.attention.causal"] = false;
        kv["bert.pooling_type"] = p.PoolingType;
        kv["bert.normalize_embeddings"] = p.normalizeEmbeddings;
        kv["bert.block_count"] = cmp.Or(p.NLayers, p.NumHiddenLayers, p.NLayer);
        var if contextLength = cmp.Or(p.MaxPositionEmbeddings, p.NCtx); contextLength > 0 {
        kv["bert.context_length"] = contextLength;
    }
        var if embeddingLength = cmp.Or(p.HiddenSize, p.NEmbd); embeddingLength > 0 {
        kv["bert.embedding_length"] = cmp.Or(p.HiddenSize, p.NEmbd);
    }
        var if feedForwardLength = cmp.Or(p.IntermediateSize, p.NInner); feedForwardLength > 0 {
        kv["bert.feed_forward_length"] = cmp.Or(p.IntermediateSize, p.NInner);
    }
        var if headCount = cmp.Or(p.NumAttentionHeads, p.NHead); headCount > 0 {
        kv["bert.attention.head_count"] = cmp.Or(p.NumAttentionHeads, p.NHead);
    }
        var if layerNormEpsilon = cmp.Or(p.LayerNormEPS, p.LayerNormEpsilon, p.NormEpsilon); layerNormEpsilon > 0 {
        kv["bert.attention.layer_norm_epsilon"] = layerNormEpsilon;
    }
        kv["tokenizer.ggml.model"] = "bert";
        kv["tokenizer.ggml.token_type_count"] = uint32(2);
        var for i, e = range t.Tokens {
        if strings.HasPrefix(e, "[") && strings.HasSuffix(e, "]") {
        } else if strings.HasPrefix(e, "##") {
        t.Tokens[i] = e[2:];
        } else {
        t.Tokens[i] = "\u2581" + e;
    }
    }
        kv["tokenizer.ggml.tokens"] = t.Tokens;
        return kv;
    }
        func (p *bertModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
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
        func (bertModel) Replacements() []String {
        return []String{
        "encoder.layer", "blk",;
        "encoder.layers", "blk",;
        "embeddings.word_embeddings", "token_embd",;
        "embeddings.token_type_embeddings", "token_types",;
        "embeddings.LayerNorm", "token_embd_norm",;
        "embeddings.position_embeddings", "position_embd",;
        "attention.self.query", "attn_q",;
        "attention.self.key", "attn_k",;
        "attention.self.value", "attn_v",;
        "attention.output.dense", "attn_output",;
        "attention.output.LayerNorm", "attn_output_norm",;
        "intermediate.dense", "ffn_up",;
        "output.dense", "ffn_down",;
        "output.LayerNorm", "layer_output_norm",;
    }
    }
}
