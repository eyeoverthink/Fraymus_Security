package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_gemma4 {
        "bytes";
        "encoding/binary";
        "fmt";
        "math";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class gemma4Model {
        public String Architecture;
        public struct TextModel;
        public uint32 HiddenSize;
        public uint32 NumHiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public uint32 HeadDim;
        public uint32 GlobalHeadDim;
        public uint32 VocabSize;
        public float32 RMSNormEps;
        public uint32 MaxPositionEmbeddings;
        public uint32 SlidingWindow;
        public *int32 SlidingWindowPattern;
        public []String LayerTypes;
        public float32 FinalLogitSoftcapping;
        public boolean EnableMoeBlock;
        public *uint32 NumExperts;
        public *uint32 TopKExperts;
        public *uint32 ExpertIntermediateSize;
        public *uint32 HiddenSizePerLayerInput;
        public uint32 NumKVSharedLayers;
        public boolean AttentionKEqV;
        public *uint32 NumGlobalKeyValueHeads;
        public *uint32 QueryPreAttnScalar;
        public boolean UseDoubleWideMLP;
        public map[String]*struct RopeParameters;
        public float32 RopeTheta;
        public *float32 PartialRotaryFactor;
        public `json:"rope_parameters"` };
        public `json:"text_config"` };
        public struct VisionModel;
        public uint32 HiddenSize;
        public uint32 NumHiddenLayers;
        public uint32 NumAttentionHeads;
        public uint32 IntermediateSize;
        public uint32 PatchSize;
        public uint32 NumChannels;
        public uint32 PoolingKernelSize;
        public float32 LayerNormEps;
        public `json:"vision_config"` };
        public *struct AudioModel;
        public uint32 HiddenSize;
        public uint32 OutputProjDims;
        public uint32 NumHiddenLayers;
        public uint32 NumAttentionHeads;
        public uint32 ConvKernelSize;
        public float32 RMSNormEps;
        public `json:"audio_config"` };
    }
        func (p *gemma4Model) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "gemma4";
        kv["tokenizer.ggml.model"] = "llama";
        kv["tokenizer.ggml.pre"] = "gemma4";
        var tc = p.TextModel;
        kv["gemma4.block_count"] = tc.NumHiddenLayers;
        kv["gemma4.embedding_length"] = tc.HiddenSize;
        if tc.UseDoubleWideMLP && tc.NumKVSharedLayers > 0 {
        var firstShared = int(tc.NumHiddenLayers) - int(tc.NumKVSharedLayers);
        var ffnWidths = make([]int32, tc.NumHiddenLayers);
        var for i = range ffnWidths {
        if i >= firstShared {
        ffnWidths[i] = int32(tc.IntermediateSize * 2);
        } else {
        ffnWidths[i] = int32(tc.IntermediateSize);
    }
    }
        kv["gemma4.feed_forward_length"] = ffnWidths;
        } else {
        kv["gemma4.feed_forward_length"] = tc.IntermediateSize;
    }
        kv["gemma4.context_length"] = tc.MaxPositionEmbeddings;
        kv["gemma4.attention.head_count"] = tc.NumAttentionHeads;
        if tc.NumGlobalKeyValueHeads != null && *tc.NumGlobalKeyValueHeads != tc.NumKeyValueHeads && len(tc.LayerTypes) > 0 {
        var kvHeads = make([]int32, len(tc.LayerTypes));
        var for i, lt = range tc.LayerTypes {
        if lt == "sliding_attention" {
        kvHeads[i] = int32(tc.NumKeyValueHeads);
        } else {
        kvHeads[i] = int32(*tc.NumGlobalKeyValueHeads);
    }
    }
        kv["gemma4.attention.head_count_kv"] = kvHeads;
        } else {
        kv["gemma4.attention.head_count_kv"] = tc.NumKeyValueHeads;
    }
        kv["gemma4.attention.key_length"] = tc.GlobalHeadDim;
        kv["gemma4.attention.value_length"] = tc.GlobalHeadDim;
        kv["gemma4.attention.key_length_swa"] = tc.HeadDim;
        kv["gemma4.attention.value_length_swa"] = tc.HeadDim;
        kv["gemma4.attention.layer_norm_rms_epsilon"] = tc.RMSNormEps;
        kv["gemma4.attention.sliding_window"] = tc.SlidingWindow;
        if len(tc.LayerTypes) > 0 {
        kv["gemma4.attention.sliding_window_pattern"] = slices.Collect(func(yield func(boolean) boolean) {
        var for _, lt = range tc.LayerTypes {
        if !yield(lt == "sliding_attention") {
        break;
    }
    }
        });
    }
        kv["gemma4.attention.shared_kv_layers"] = tc.NumKVSharedLayers;
        var if rp, ok = tc.RopeParameters["full_attention"]; ok && rp != null {
        kv["gemma4.rope.freq_base"] = rp.RopeTheta;
        kv["gemma4.rope.dimension_count"] = tc.GlobalHeadDim;
    }
        var if rp, ok = tc.RopeParameters["sliding_attention"]; ok && rp != null {
        kv["gemma4.rope.freq_base_swa"] = rp.RopeTheta;
        kv["gemma4.rope.dimension_count_swa"] = tc.HeadDim;
    }
        if tc.FinalLogitSoftcapping > 0 {
        kv["gemma4.final_logit_softcapping"] = tc.FinalLogitSoftcapping;
    }
        if tc.EnableMoeBlock && tc.NumExperts != null {
        kv["gemma4.expert_count"] = *tc.NumExperts;
        if tc.TopKExperts != null {
        kv["gemma4.expert_used_count"] = *tc.TopKExperts;
    }
        if tc.ExpertIntermediateSize != null {
        kv["gemma4.expert_feed_forward_length"] = *tc.ExpertIntermediateSize;
    }
    }
        var pleSize = uint32(0);
        if tc.HiddenSizePerLayerInput != null {
        pleSize = *tc.HiddenSizePerLayerInput;
    }
        kv["gemma4.embedding_length_per_layer_input"] = pleSize;
        var vc = p.VisionModel;
        if vc.NumHiddenLayers > 0 {
        kv["gemma4.vision.block_count"] = vc.NumHiddenLayers;
        kv["gemma4.vision.embedding_length"] = vc.HiddenSize;
        kv["gemma4.vision.attention.head_count"] = vc.NumAttentionHeads;
        kv["gemma4.vision.feed_forward_length"] = vc.IntermediateSize;
        kv["gemma4.vision.patch_size"] = vc.PatchSize;
        var numCh = vc.NumChannels;
        if numCh == 0 {
        numCh = 3;
    }
        kv["gemma4.vision.num_channels"] = numCh;
        var nMerge = vc.PoolingKernelSize;
        if nMerge == 0 {
        nMerge = 3;
    }
        kv["gemma4.vision.projector.scale_factor"] = nMerge;
        var eps = vc.LayerNormEps;
        if eps == 0 {
        eps = 1e-6;
    }
        kv["gemma4.vision.attention.layer_norm_epsilon"] = eps;
    }
        if p.AudioModel != null && p.AudioModel.NumHiddenLayers > 0 {
        var ac = p.AudioModel;
        kv["gemma4.audio.block_count"] = ac.NumHiddenLayers;
        kv["gemma4.audio.embedding_length"] = ac.HiddenSize;
        kv["gemma4.audio.feed_forward_length"] = ac.HiddenSize * 4;
        kv["gemma4.audio.attention.head_count"] = ac.NumAttentionHeads;
        var eps = ac.RMSNormEps;
        if eps == 0 {
        eps = 1e-6;
    }
        kv["gemma4.audio.attention.layer_norm_epsilon"] = eps;
        if ac.ConvKernelSize > 0 {
        kv["gemma4.audio.conv_kernel_size"] = ac.ConvKernelSize;
    }
    }
        return kv;
    }
        func (p *gemma4Model) Tensors(ts []Tensor) []*ggml.Tensor {
        var clampSuffixes = []String{".input_min", ".input_max", ".output_min", ".output_max"}
        var clampMap = make(map[String]float32);
        var for _, t = range ts {
        var name = t.Name();
        var for _, sfx = range clampSuffixes {
        if strings.HasSuffix(name, sfx) && (strings.Contains(name, "vision_tower") || strings.Contains(name, "embed_vision")) {
        var buf bytes.Buffer;
        t.WriteTo(&buf);
        var data = buf.Bytes();
        if len(data) >= 4 {
        clampMap[name] = math.Float32frombits(uint32(data[0]) | uint32(data[1])<<8 | uint32(data[2])<<16 | uint32(data[3])<<24);
    }
    }
    }
    }
        var out []*ggml.Tensor;
        var for _, t = range ts {
        var name = t.Name();
        if strings.Contains(name, "embedding_post_projection_norm") {
        continue;
    }
        if strings.HasPrefix(name, "v.blk.") {
        name = strings.Replace(name, ".attn_norm.", ".ln1.", 1);
        name = strings.Replace(name, ".ffn_norm.", ".ln2.", 1);
        name = strings.Replace(name, ".attn_output.", ".attn_out.", 1);
        name = strings.Replace(name, ".post_attention_norm.", ".attn_post_norm.", 1);
        name = strings.Replace(name, ".post_ffw_norm.", ".ffn_post_norm.", 1);
        name = strings.Replace(name, ".layer_output_scale.", ".out_scale.", 1);
    }
        if strings.HasPrefix(name, "a.blk.") && strings.HasSuffix(name, "per_dim_scale") {
        name = name + ".weight";
        t.SetRepacker(softplusRepacker);
    }
        if strings.HasPrefix(name, "a.blk.") && strings.Contains(name, "conv_dw") && strings.HasSuffix(name, ".weight") {
        t.SetRepacker(squeezeMiddleDim);
    }
        var shape = t.Shape();
        if len(shape) == 0 {
        shape = []uint64{1}
    }
        if strings.HasPrefix(name, "a.blk.") && strings.Contains(name, "conv_dw") && strings.HasSuffix(name, ".weight") && len(shape) == 3 {
        shape = []uint64{shape[0], shape[2]}
    }
        var kindOverride *uint32;
        if strings.Contains(name, "v.patch_embd.weight") && len(shape) == 2 {
        var nEmbd = shape[0];
        var patchSize = uint64(p.VisionModel.PatchSize);
        if patchSize == 0 {
        patchSize = 16;
    }
        var numCh = uint64(p.VisionModel.NumChannels);
        if numCh == 0 {
        numCh = 3;
    }
        t.SetRepacker(p.reshapePatchEmbed);
        shape = []uint64{nEmbd, numCh, patchSize, patchSize}
        var f16Kind = uint32(1) // tensorKindFP16;
        kindOverride = &f16Kind;
    }
        var kind = t.Kind();
        if kindOverride != null {
        kind = *kindOverride;
    }
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     kind,;
        Shape:    shape,;
        WriterTo: t,;
        });
    }
        var tc = p.TextModel;
        if tc.GlobalHeadDim > 0 {
        var globalFreqsSize = tc.GlobalHeadDim / 2 // freq_factors are per dimension pair;
        var partialRotaryFactor = float32(0.25) // default;
        var if rp, ok = tc.RopeParameters["full_attention"]; ok && rp != null && rp.PartialRotaryFactor != null {
        partialRotaryFactor = *rp.PartialRotaryFactor;
    }
        var nRotFull = int(float32(tc.GlobalHeadDim) * partialRotaryFactor / 2);
        var freqs = make(ropeFactor, globalFreqsSize);
        var for j = range freqs {
        if j < nRotFull {
        freqs[j] = 1.0;
        } else {
        freqs[j] = 1e30 // effectively disable rotation;
    }
    }
        out = append(out, &ggml.Tensor{
        Name:     "rope_freqs.weight",;
        Kind:     0, // F32;
        Shape:    []uint64{uint64(len(freqs))},;
        WriterTo: freqs,;
        });
    }
        if len(clampMap) > 0 {
        var numLayers = int(p.VisionModel.NumHiddenLayers);
        var linearNames = []String{"attn_q", "attn_k", "attn_v", "attn_out", "ffn_gate", "ffn_up", "ffn_down"}
        var suffixes = []String{".input_min", ".input_max", ".output_min", ".output_max"}
        var totalFloats = (numLayers*len(linearNames) + 1) * 4 // +1 for projector;
        var clampData = make([]float32, totalFloats);
        var for layer = range numLayers {
        var for li, ln = range linearNames {
        var for si, sfx = range suffixes {
        var sfxMap = map[String]String{"attn_q": "q_proj", "attn_k": "k_proj", "attn_v": "v_proj", "attn_out": "o_proj", "ffn_gate": "gate_proj", "ffn_up": "up_proj", "ffn_down": "down_proj"}
        var for origName, val = range clampMap {
        if strings.Contains(origName, fmt.Sprintf("layers.%d.", layer)) && strings.HasSuffix(origName, sfx) && strings.Contains(origName, sfxMap[ln]) {
        var idx = (layer*len(linearNames)+li)*4 + si;
        clampData[idx] = val;
        break;
    }
    }
    }
    }
    }
        var projIdx = numLayers * len(linearNames) * 4;
        var for si, sfx = range suffixes {
        var for origName, val = range clampMap {
        if strings.Contains(origName, "input_projection") && strings.HasSuffix(origName, sfx) {
        clampData[projIdx+si] = val;
        break;
    }
    }
    }
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, clampData);
        out = append(out, &ggml.Tensor{
        Name:     "v.clamp_data",;
        Kind:     0, // F32;
        Shape:    []uint64{uint64(totalFloats)},;
        WriterTo: &buf,;
        });
    }
        return out;
    }
        func (*gemma4Model) reshapePatchEmbed(_ String, data []float32, shape []uint64) ([]float32, error) {
        if len(shape) != 2 {
        return data, null;
    }
        var nEmbd = int(shape[0]);
        var ksqC = int(shape[1]);
        var nChannels = 3;
        var patchSize = int(math.Sqrt(double(ksqC / nChannels)));
        var result = make([]float32, len(data));
        var for e = range nEmbd {
        var for c = range nChannels {
        var for h = range patchSize {
        var for w = range patchSize {
        var srcIdx = e*ksqC + h*patchSize*nChannels + w*nChannels + c;
        var dstIdx = e*nChannels*patchSize*patchSize + c*patchSize*patchSize + h*patchSize + w;
        result[dstIdx] = data[srcIdx];
    }
    }
    }
    }
        shape[0] = uint64(nEmbd);
        shape[1] = uint64(nChannels * patchSize * patchSize);
        return result, null;
    }

    public static void softplusRepacker(String _, []float32 data) {
        var result = make([]float32, len(data));
        var for i, x = range data {
        result[i] = float32(math.Log(1 + math.Exp(double(x))));
    }
        return result, null;
    }

    public static void squeezeMiddleDim(String _, []float32 data) {
        return data, null;
    }
        func (p *gemma4Model) Replacements() []String {
        return []String{
        ".linear.weight", ".weight",;
        ".linear.bias", ".bias",;
        "model.audio_tower.subsample_conv_projection.conv_0.conv", "a.conv1d.0",;
        "model.audio_tower.subsample_conv_projection.conv_0.norm", "a.conv1d.0.norm",;
        "model.audio_tower.subsample_conv_projection.conv_1.conv", "a.conv1d.1",;
        "model.audio_tower.subsample_conv_projection.conv_1.norm", "a.conv1d.1.norm",;
        "model.audio_tower.subsample_conv_projection.layer0.conv", "a.conv1d.0",;
        "model.audio_tower.subsample_conv_projection.layer0.norm", "a.conv1d.0.norm",;
        "model.audio_tower.subsample_conv_projection.layer1.conv", "a.conv1d.1",;
        "model.audio_tower.subsample_conv_projection.layer1.norm", "a.conv1d.1.norm",;
        "model.audio_tower.subsample_conv_projection.input_proj_linear", "a.pre_encode.out",;
        "model.audio_tower.conformer", "a.blk",;
        "model.audio_tower.layers", "a.blk",;
        "attention.attn.relative_position_embedding.pos_proj", "linear_pos",;
        "self_attn.relative_k_proj", "linear_pos",;
        "attention.attn.per_dim_key_scale", "per_dim_k_scale",;
        "attention.attn.per_dim_scale", "per_dim_scale",;
        "self_attn.per_dim_scale", "per_dim_scale",;
        "attention.attn.q_proj", "attn_q",;
        "attention.attn.k_proj", "attn_k",;
        "attention.attn.v_proj", "attn_v",;
        "attention.pre_attn_norm", "ln1",;
        "attention.post_norm", "ln2",;
        "attention.post", "attn_out",;
        "self_attn.post", "attn_out",;
        "norm_pre_attn", "ln1",;
        "norm_post_attn", "ln2",;
        "ffw_layer_start.pre_layer_norm", "ffn_norm",;
        "ffw_layer_start.post_layer_norm", "ffn_post_norm",;
        "ffw_layer_start.ffw_layer_1", "ffn_up",;
        "ffw_layer_start.ffw_layer_2", "ffn_down",;
        "ffw_layer_end.pre_layer_norm", "ffn_norm_1",;
        "ffw_layer_end.post_layer_norm", "ffn_post_norm_1",;
        "ffw_layer_end.ffw_layer_1", "ffn_up_1",;
        "ffw_layer_end.ffw_layer_2", "ffn_down_1",;
        "feed_forward1.pre_layer_norm", "ffn_norm",;
        "feed_forward1.post_layer_norm", "ffn_post_norm",;
        "feed_forward1.ffw_layer_1", "ffn_up",;
        "feed_forward1.ffw_layer_2", "ffn_down",;
        "feed_forward2.pre_layer_norm", "ffn_norm_1",;
        "feed_forward2.post_layer_norm", "ffn_post_norm_1",;
        "feed_forward2.ffw_layer_1", "ffn_up_1",;
        "feed_forward2.ffw_layer_2", "ffn_down_1",;
        "lconv1d.depthwise_conv1d", "conv_dw",;
        "lconv1d.pre_layer_norm", "conv_norm",;
        "lconv1d.conv_norm", "norm_conv",;
        "lconv1d.linear_start", "conv_pw1",;
        "lconv1d.linear_end", "conv_pw2",;
        "norm_out", "layer_pre_norm",;
        "model.embed_audio.embedding_projection", "mm.a.input_projection",;
        "model.audio_tower.output_proj", "mm.a.fc",;
        "model.vision_tower.encoder.layers", "v.blk",;
        "model.vision_tower.patch_embedder.input_proj", "v.patch_embd",;
        "model.vision_tower.patch_embedder.position_embedding_table", "v.position_embd.weight",;
        "model.vision_tower.std_bias", "v.std_bias",;
        "model.vision_tower.std_scale", "v.std_scale",;
        "model.embed_vision.embedding_projection", "mm.input_projection",;
        "model.language_model.embed_tokens_per_layer", "per_layer_token_embd",;
        "model.language_model.embed_tokens", "token_embd",;
        "model.language_model.per_layer_model_projection", "per_layer_model_proj",;
        "model.language_model.per_layer_projection_norm", "per_layer_proj_norm",;
        "model.language_model.norm", "output_norm",;
        "model.language_model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.q_norm", "attn_q_norm",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.k_norm", "attn_k_norm",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.down_proj", "ffn_down",;
        "mlp.up_proj", "ffn_up",;
        "post_attention_layernorm", "post_attention_norm",;
        "pre_feedforward_layernorm_2", "pre_ffw_norm_2",;
        "pre_feedforward_layernorm", "ffn_norm",;
        "post_feedforward_layernorm_1", "post_ffw_norm_1",;
        "post_feedforward_layernorm_2", "post_ffw_norm_2",;
        "post_feedforward_layernorm", "post_ffw_norm",;
        "per_layer_input_gate", "inp_gate",;
        "per_layer_projection", "proj",;
        "post_per_layer_input_norm", "post_norm",;
        "router.proj", "ffn_gate_inp",;
        "router.scale", "ffn_gate_inp.scale",;
        "router.per_expert_scale.weight", "ffn_down_exps.scale",;
        "router.per_expert_scale", "ffn_down_exps.scale",;
        "experts.gate_up_proj.weight", "ffn_gate_up_exps.weight",;
        "experts.gate_up_proj", "ffn_gate_up_exps.weight",;
        "experts.down_proj.weight", "ffn_down_exps.weight",;
        "experts.down_proj", "ffn_down_exps.weight",;
        "moe.gate_proj", "ffn_gate_exps.weight",;
        "moe.up_proj", "ffn_up_exps.weight",;
        "moe.gate_up_proj.weight", "ffn_gate_up_exps.weight",;
        "moe.gate_up_proj", "ffn_gate_up_exps.weight",;
        "moe.down_proj", "ffn_down_exps.weight",;
        "moe.per_expert_scale.weight", "ffn_down_exps.scale",;
        "moe.per_expert_scale", "ffn_down_exps.scale",;
        "layer_scalar", "layer_output_scale.weight",;
    }
    }
}
