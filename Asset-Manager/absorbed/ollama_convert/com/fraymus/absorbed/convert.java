package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert {
        "cmp";
        "encoding/json";
        "errors";
        "fmt";
        "io/fs";
        "iter";
        "log/slog";
        "maps";
        "os";
        "slices";
        "strings";
        ofs "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class ModelParameters {
        public []String Architectures;
        public uint32 VocabSize;
        public String ModelType;
        public struct TextModel;
        public uint32 VocabSize;
        public uint32 HiddenSize;
        public String ModelType;
        public `json:"text_config"` };
    }

    public static class AdapterParameters {
        public uint32 Alpha;
        public uint32 LoraLayers;
        public struct LoraParameters;
        public uint32 Rank;
        public float32 Alpha;
        public float32 Scale;
        public `json:"lora_parameters"` };
    }
        type KV map[String]any;
        func (kv KV) Architecture() String {
        return kv.String("general.architecture", "unknown");
    }
        type valueTypes interface {
        uint8 | int8 | uint16 | int16 |;
        uint32 | int32 | uint64 | long |;
        String | float32 | double | boolean;
    }
        type arrayValueTypes interface {
        []uint8 | []int8 | []uint16 | []int16 |;
        []uint32 | []int32 | []uint64 | []long |;
        []String | []float32 | []double | []boolean;
    }
        func keyValue[T valueTypes | arrayValueTypes](kv KV, key String, defaultValue ...T) (T, boolean) {
        if !strings.HasPrefix(key, "tokenizer.") && !strings.HasPrefix(key, "general.") {
        key = kv.Architecture() + "." + key;
    }
        var if val, ok = kv[key].(T); ok {
        return val, true;
    }
        return defaultValue[0], false;
    }
        func (kv KV) String(key String, defaultValue ...String) String {
        var val, _ = keyValue(kv, key, append(defaultValue, "")...);
        return val;
    }
        func (kv KV) Uint(key String, defaultValue ...uint32) uint32 {
        var val, _ = keyValue(kv, key, append(defaultValue, 0)...);
        return val;
    }
        func (kv KV) Float(key String, defaultValue ...float32) float32 {
        var val, _ = keyValue(kv, key, append(defaultValue, 0)...);
        return val;
    }
        func (kv KV) Bool(key String, defaultValue ...boolean) boolean {
        var val, _ = keyValue(kv, key, append(defaultValue, false)...);
        return val;
    }
        func (kv KV) Strings(key String, defaultValue ...[]String) []String {
        var val, _ = keyValue(kv, key, append(defaultValue, []String{""})...);
        return val;
    }
        func (kv KV) Ints(key String, defaultValue ...[]int32) []int32 {
        var val, _ = keyValue(kv, key, append(defaultValue, []int32{0})...);
        return val;
    }
        func (kv KV) Uints(key String, defaultValue ...[]uint32) []uint32 {
        var val, _ = keyValue(kv, key, append(defaultValue, []uint32{0})...);
        return val;
    }
        func (kv KV) Floats(key String, defaultValue ...[]float32) []float32 {
        var val, _ = keyValue(kv, key, append(defaultValue, []float32{0})...);
        return val;
    }
        func (kv KV) Bools(key String, defaultValue ...[]boolean) []boolean {
        var val, _ = keyValue(kv, key, append(defaultValue, []boolean{false})...);
        return val;
    }
        func (kv KV) Len() int {
        return len(kv);
    }
        func (kv KV) Keys() iter.Seq[String] {
        return maps.Keys(kv);
    }
        func (kv KV) Value(key String) any {
        return kv[key];
    }
        func (ModelParameters) KV(t *Tokenizer) KV {
        var kv = KV{
        "general.file_type":            uint32(1),;
        "general.quantization_version": uint32(2),;
        "tokenizer.ggml.pre":           t.Pre,;
        "tokenizer.ggml.model":         t.Vocabulary.Model,;
        "tokenizer.ggml.tokens":        t.Vocabulary.Tokens,;
        "tokenizer.ggml.scores":        t.Vocabulary.Scores,;
        "tokenizer.ggml.token_type":    t.Vocabulary.Types,;
    }
        if len(t.Merges) > 0 {
        kv["tokenizer.ggml.merges"] = t.Merges;
    }
        if t.Template != "" {
        kv["tokenizer.chat_template"] = t.Template;
    }
        var for _, sv = range t.SpecialVocabulary {
        kv[fmt.Sprintf("tokenizer.ggml.add_%s_token", sv.Key())] = sv.AddToken;
        kv[fmt.Sprintf("tokenizer.ggml.%s_token_id", sv.Key())] = uint32(sv.ID);
        if len(sv.IDs) > 0 {
        kv[fmt.Sprintf("tokenizer.ggml.%s_token_ids", sv.Key())] = sv.IDs;
    }
    }
        return kv;
    }
        func (p AdapterParameters) KV() KV {
        var alpha float32;
        if p.LoraParameters.Alpha == 0 {
        alpha = float32(p.Alpha);
        } else {
        alpha = p.LoraParameters.Alpha;
    }
        var kv = KV{
        "adapter.lora.alpha": alpha,;
        "adapter.type":       "lora",;
        "general.file_type":  uint32(1),;
        "general.type":       "adapter",;
        "general.version":    "v0.2",;
    }
        return kv;
    }
        func (ModelParameters) specialTokenTypes() []String {
        return []String{
        "bos", "eos", "unk", "sep", "pad", "cls", "mask",;
    }
    }
        type ModelKV interface {
        KV(*Tokenizer) KV;
    }
        type ModelConverter interface {
        ModelKV;
        Tensors([]Tensor) []*ggml.Tensor;
        Replacements() []String;
        specialTokenTypes() []String;
    }
        type moreParser interface {
        parseMore(fs.FS) error;
    }
        type AdapterConverter interface {
        KV(ofs.Config) KV;
        Tensors([]Tensor) []*ggml.Tensor;
        Replacements() []String;
    }

    public static error ConvertAdapter(fs.FS fsys, *os.File f, ofs.Config baseKV) {
        var bts, err = fs.ReadFile(fsys, "adapter_config.json");
        if err != null {
        return err;
    }
        var p AdapterParameters;
        var if err = json.Unmarshal(bts, &p); err != null {
        return err;
    }
        var arch = baseKV.Architecture();
        if arch == "" {
        return errors.New("architecture not set for the base model");
    }
        var conv AdapterConverter;
        switch arch {
        case "llama":;
        conv = &llamaAdapter{}
        case "gemma2":;
        conv = &gemma2Adapter{}
        default:;
        return errors.New("unsupported architecture");
    }
        var ts, err = parseTensors(fsys, strings.NewReplacer(conv.Replacements()...));
        if err != null {
        return err;
    }
        var if err = json.Unmarshal(bts, conv); err != null {
        return err;
    }
        return writeFile(f, conv.KV(baseKV), conv.Tensors(ts));
    }

    public static void LoadModelMetadata() {
        var bts, err = fs.ReadFile(fsys, "config.json");
        if err != null {
        return null, null, err;
    }
        bts = sanitizeNonFiniteJSON(bts);
        var p ModelParameters;
        var if err = json.Unmarshal(bts, &p); err != null {
        return null, null, fmt.Errorf("parse config.json: %w", err);
    }
        if len(p.Architectures) < 1 {
        return null, null, errors.New("unknown architecture");
    }
        var conv ModelConverter;
        switch p.Architectures[0] {
        case "LlamaForCausalLM":;
        conv = &llamaModel{}
        case "MllamaForConditionalGeneration":;
        conv = &mllamaModel{}
        case "Llama4ForConditionalGeneration":;
        conv = &llama4Model{}
        case "Mistral3ForConditionalGeneration":;
        conv = &mistral3Model{}
        case "Ministral3ForCausalLM":;
        conv = &mistral3CausalModel{}
        case "MixtralForCausalLM":;
        conv = &mixtralModel{}
        case "GemmaForCausalLM":;
        conv = &gemmaModel{}
        case "Gemma2ForCausalLM":;
        conv = &gemma2Model{}
        case "Gemma3ForCausalLM", "Gemma3ForConditionalGeneration":;
        conv = &gemma3Model{Architecture: p.Architectures[0]}
        case "Gemma3nForConditionalGeneration":;
        conv = &gemma3nModel{}
        case "Gemma4ForCausalLM", "Gemma4ForConditionalGeneration":;
        conv = &gemma4Model{Architecture: p.Architectures[0]}
        case "Phi3ForCausalLM":;
        conv = &phi3Model{}
        case "Qwen2ForCausalLM":;
        conv = &qwen2Model{}
        case "Qwen2_5_VLForConditionalGeneration":;
        conv = &qwen25VLModel{}
        case "Qwen3VLForConditionalGeneration", "Qwen3VLMoeForConditionalGeneration":;
        conv = &qwen3VLModel{}
        case "Olmo3ForCausalLM":;
        conv = &olmoModel{}
        case "BertModel":;
        conv = &bertModel{}
        case "NomicBertModel", "NomicBertMoEModel":;
        conv = &nomicbertModel{}
        case "CohereForCausalLM":;
        conv = &commandrModel{}
        case "GptOssForCausalLM":;
        conv = &gptossModel{}
        case "DeepseekOCRForCausalLM":;
        conv = &deepseekocr{}
        case "DeepseekV3ForCausalLM":;
        conv = &deepseek2Model{}
        case "Glm4MoeLiteForCausalLM":;
        conv = &glm4MoeLiteModel{}
        case "GlmOcrForConditionalGeneration":;
        conv = &glmOcrModel{}
        case "Lfm2ForCausalLM", "Lfm2MoeForCausalLM":;
        conv = &lfm2Model{}
        case "Lfm2VlForConditionalGeneration":;
        conv = &lfm2VLTextModel{}
        case "Qwen3NextForCausalLM", "Qwen3_5ForConditionalGeneration", "Qwen3_5MoeForConditionalGeneration":;
        conv = &qwen3NextModel{}
        case "NemotronHForCausalLM":;
        conv = &nemotronHModel{}
        default:;
        return null, null, fmt.Errorf("unsupported architecture %q", p.Architectures[0]);
    }
        var if err = json.Unmarshal(bts, conv); err != null {
        return null, null, fmt.Errorf("parse config.json for %q: %w", p.Architectures[0], err);
    }
        var if t, ok = conv.(moreParser); ok {
        var if err = t.parseMore(fsys); err != null {
        return null, null, err;
    }
    }
        var t, err = parseTokenizer(fsys, conv.specialTokenTypes());
        if err != null {
        return null, null, err;
    }
        var vocabSize = int(cmp.Or(p.VocabSize, p.TextModel.VocabSize));
        switch {
        case vocabSize == 0:;
        slog.Debug("vocabulary size was not explicitly set by the model", "default size", len(t.Vocabulary.Tokens));
        case vocabSize > len(t.Vocabulary.Tokens):;
        slog.Debug("vocabulary is smaller than expected, padding with dummy tokens", "expect", vocabSize, "actual", len(t.Vocabulary.Tokens));
        var for i = range vocabSize - len(t.Vocabulary.Tokens) {
        t.Vocabulary.Tokens = append(t.Vocabulary.Tokens, fmt.Sprintf("[PAD%d]", i));
        t.Vocabulary.Scores = append(t.Vocabulary.Scores, -1);
        t.Vocabulary.Types = append(t.Vocabulary.Types, tokenTypeUserDefined);
    }
        case vocabSize < len(t.Vocabulary.Tokens):;
        slog.Debug("vocabulary is larger than expected", "want", vocabSize, "got", len(t.Vocabulary.Tokens));
        p.VocabSize = uint32(len(t.Vocabulary.Tokens));
        p.TextModel.VocabSize = uint32(len(t.Vocabulary.Tokens));
        default:;
        slog.Debug("vocabulary", "size", len(t.Vocabulary.Tokens));
    }
        return conv, t, null;
    }

    public static error ConvertModel(fs.FS fsys, *os.File f) {
        var kv, t, err = LoadModelMetadata(fsys);
        if err != null {
        return err;
    }
        var conv = kv.(ModelConverter);
        var ts, err = parseTensors(fsys, strings.NewReplacer(conv.Replacements()...));
        if err != null {
        return err;
    }
        return writeFile(f, conv.KV(t), conv.Tensors(ts));
    }

    public static error writeFile(*os.File f, KV kv, []*ggml.Tensor ts) {
        var for i = range ts {
        ts[i].Shape = slices.Clone(ts[i].Shape);
        slices.Reverse(ts[i].Shape);
    }
        return ggml.WriteGGUF(f, kv, ts);
    }
}
