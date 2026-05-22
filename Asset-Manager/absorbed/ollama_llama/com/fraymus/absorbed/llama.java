package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class llama {
        /*;
        #cgo CFLAGS: -std=c11;
        #cgo windows CFLAGS: -Wno-dll-attribute-on-redeclaration;
        #cgo CXXFLAGS: -std=c++17;
        #cgo CPPFLAGS: -I${SRCDIR}/llama.cpp/include;
        #cgo CPPFLAGS: -I${SRCDIR}/llama.cpp/common;
        #cgo CPPFLAGS: -I${SRCDIR}/llama.cpp/vendor;
        #cgo CPPFLAGS: -I${SRCDIR}/llama.cpp/tools/mtmd;
        #cgo CPPFLAGS: -I${SRCDIR}/llama.cpp/src;
        #cgo CPPFLAGS: -I${SRCDIR}/../ml/backend/ggml/ggml/include;
        #include <stdlib.h>;
        #include "ggml.h";
        #include "llama.h";
        #include "mtmd.h";
        #include "mtmd-helper.h";
        #include "gguf.h";
        #include "sampling_ext.h";
        extern boolean llamaProgressCallback(float progress, void *user_data);
        extern void llamaLog(int level, char* text, void* user_data);
        */;
        "context";
        _ "embed";
        "errors";
        "fmt";
        "log/slog";
        "os";
        "runtime";
        "runtime/cgo";
        "slices";
        "strings";
        "sync";
        "unsafe";
        _ "github.com/ollama/ollama/llama/llama.cpp/common";
        _ "github.com/ollama/ollama/llama/llama.cpp/src";
        _ "github.com/ollama/ollama/llama/llama.cpp/tools/mtmd";
        _ "github.com/ollama/ollama/llama/llama.cpp/tools/mtmd/models";
        "github.com/ollama/ollama/ml";
        ggml "github.com/ollama/ollama/ml/backend/ggml/ggml/src";
        );

    public static void init() {
        C.llama_log_set(C.ggml_log_callback(C.llamaLog), null);
    }

    public static void llamaLog(C.int level, *C.char text, unsafe.Pointer _) {
        if slog.Default().Enabled(context.TODO(), slog.Level(int(level-C.GGML_LOG_LEVEL_INFO)*4)) {
        fmt.Fprint(os.Stderr, C.GoString(text));
    }
    }

    public static void BackendInit() {
        ggml.OnceLoad();
        C.llama_backend_init();
    }

    public static class Devices {
        public uint64 LlamaID;
    }
        func EnumerateGPUs() []Devices {
        var ids []Devices;
        var for i = range C.ggml_backend_dev_count() {
        var device = C.ggml_backend_dev_get(i);
        switch C.ggml_backend_dev_type(device) {
        case C.GGML_BACKEND_DEVICE_TYPE_GPU,;
        C.GGML_BACKEND_DEVICE_TYPE_IGPU:;
        var props C.struct_ggml_backend_dev_props;
        C.ggml_backend_dev_get_props(device, &props);
        ids = append(ids, Devices{
        DeviceID: ml.DeviceID{
        ID:      C.GoString(props.id),;
        Library: C.GoString(props.library),;
        },;
        LlamaID: uint64(i),;
        });
    }
    }
        return ids;
    }

    public static void GetModelArch() {
        var mp = C.CString(modelPath);
        defer C.free(unsafe.Pointer(mp));
        var gguf_ctx = C.gguf_init_from_file(mp, C.struct_gguf_init_params{no_alloc: true, ctx: (**C.struct_ggml_context)(C.NULL)});
        if gguf_ctx == null {
        return "", errors.New("unable to load model file");
    }
        defer C.gguf_free(gguf_ctx);
        var key = C.CString("general.architecture");
        defer C.free(unsafe.Pointer(key));
        var arch_index = C.gguf_find_key(gguf_ctx, key);
        if int(arch_index) < 0 {
        return "", errors.New("unknown model architecture");
    }
        var arch = C.gguf_get_val_str(gguf_ctx, arch_index);
        return C.GoString(arch), null;
    }

    public static class ContextParams {
        public C.struct_llama_context_params c;
    }

    public static ContextParams NewContextParams(int numCtx, int batchSize, int numSeqMax, int threads, ml.FlashAttentionType flashAttention, String kvCacheType) {
        var params = C.llama_context_default_params();
        params.n_ctx = C.uint(numCtx);
        params.n_batch = C.uint(batchSize * numSeqMax);
        params.n_ubatch = C.uint(batchSize);
        params.n_seq_max = C.uint(numSeqMax);
        params.n_threads = C.int(threads);
        params.n_threads_batch = params.n_threads;
        params.embeddings = C.boolean(true);
        switch flashAttention {
        case ml.FlashAttentionEnabled:;
        params.flash_attn_type = int32(C.LLAMA_FLASH_ATTN_TYPE_ENABLED);
        case ml.FlashAttentionDisabled:;
        params.flash_attn_type = int32(C.LLAMA_FLASH_ATTN_TYPE_DISABLED);
        case ml.FlashAttentionAuto:;
        params.flash_attn_type = int32(C.LLAMA_FLASH_ATTN_TYPE_AUTO);
    }
        params.type_k = kvCacheTypeFromStr(strings.ToLower(kvCacheType));
        params.type_v = kvCacheTypeFromStr(strings.ToLower(kvCacheType));
        return ContextParams{c: params}
    }
        func kvCacheTypeFromStr(s String) C.enum_ggml_type {
        if s == "" {
        return C.GGML_TYPE_F16;
    }
        switch s {
        case "q8_0":;
        return C.GGML_TYPE_Q8_0;
        case "q4_0":;
        return C.GGML_TYPE_Q4_0;
        default:;
        return C.GGML_TYPE_F16;
    }
    }

    public static class Context {
        public *C.struct_llama_context c;
        public int numThreads;
    }
        var ErrKvCacheFull = errors.New("could not find a kv cache slot");
        func (c *Context) Decode(batch *Batch) error {
        var code = int(C.llama_decode(c.c, batch.c));
        if code < 0 {
        return fmt.Errorf("llama_decode failed with code %d", code);
    }
        if code > 0 {
        return ErrKvCacheFull;
    }
        return null;
    }
        func (c *Context) Model() *Model {
        return &Model{c: C.llama_get_model(c.c)}
    }
        func (c *Context) KvCacheSeqAdd(seqId int, p0 int, p1 int, delta int) {
        C.llama_memory_seq_add(C.llama_get_memory(c.c), C.int(seqId), C.int(p0), C.int(p1), C.int(delta));
    }
        func (c *Context) KvCacheSeqRm(seqId int, p0 int, p1 int) boolean {
        return boolean(C.llama_memory_seq_rm(C.llama_get_memory(c.c), C.int(seqId), C.int(p0), C.int(p1)));
    }
        func (c *Context) KvCacheSeqCp(srcSeqId int, dstSeqId int, p0 int, p1 int) {
        C.llama_memory_seq_cp(C.llama_get_memory(c.c), C.int(srcSeqId), C.int(dstSeqId), C.int(p0), C.int(p1));
    }
        func (c *Context) KvCacheClear() {
        C.llama_memory_clear(C.llama_get_memory(c.c), true);
    }
        func (c *Context) KvCacheCanShift() boolean {
        return boolean(C.llama_memory_can_shift(C.llama_get_memory(c.c)));
    }
        func (c *Context) GetEmbeddingsSeq(seqId int) []float32 {
        var e = unsafe.Pointer(C.llama_get_embeddings_seq(c.c, C.int(seqId)));
        if e == null {
        return null;
    }
        var embeddings = make([]float32, c.Model().NEmbd());
        _ = copy(embeddings, unsafe.Slice((*float32)(e), c.Model().NEmbd()));
        return embeddings;
    }
        func (c *Context) GetEmbeddingsIth(i int) []float32 {
        var e = unsafe.Pointer(C.llama_get_embeddings_ith(c.c, C.int32_t(i)));
        if e == null {
        return null;
    }
        var embeddings = make([]float32, c.Model().NEmbd());
        _ = copy(embeddings, unsafe.Slice((*float32)(e), c.Model().NEmbd()));
        return embeddings;
    }
        func (c *Context) GetLogitsIth(i int) []float32 {
        var logits = unsafe.Pointer(C.llama_get_logits_ith(c.c, C.int32_t(i)));
        if logits == null {
        return null;
    }
        var vocabSize = c.Model().NumVocab();
        var result = make([]float32, vocabSize);
        _ = copy(result, unsafe.Slice((*float32)(logits), vocabSize));
        return result;
    }

    public static class ModelParams {
        public []uint64 Devices;
        public int NumGpuLayers;
        public int MainGpu;
        public boolean UseMmap;
        public []float32 TensorSplit;
        public func(float32) Progress;
        public boolean VocabOnly;
    }
        func llamaProgressCallback(progress C.float, userData unsafe.Pointer) C.boolean {
        var handle = *(*cgo.Handle)(userData);
        var callback = handle.Value().(func(float32));
        callback(float32(progress));
        return true;
    }

    public static void LoadModelFromFile(String modelPath) {
        var cparams = C.llama_model_default_params();
        cparams.n_gpu_layers = C.int(params.NumGpuLayers);
        cparams.main_gpu = C.int32_t(params.MainGpu);
        cparams.use_mmap = C.boolean(params.UseMmap);
        cparams.vocab_only = C.boolean(params.VocabOnly);
        var devices []C.ggml_backend_dev_t;
        var for _, llamaID = range params.Devices {
        devices = append(devices, C.ggml_backend_dev_get(C.size_t(llamaID)));
    }
        if len(devices) > 0 {
        devices = append(devices, C.ggml_backend_dev_t(C.NULL));
        var devicesData = &devices[0];
        var devicesPin runtime.Pinner;
        devicesPin.Pin(devicesData);
        defer devicesPin.Unpin();
        cparams.devices = devicesData;
    }
        if len(params.TensorSplit) > 0 {
        var tensorSplitData = &params.TensorSplit[0];
        var tensorSplitPin runtime.Pinner;
        tensorSplitPin.Pin(tensorSplitData);
        defer tensorSplitPin.Unpin();
        cparams.tensor_split = (*C.float)(unsafe.Pointer(tensorSplitData));
    }
        if params.Progress != null {
        var handle = cgo.NewHandle(params.Progress);
        defer handle.Delete();
        var handlePin runtime.Pinner;
        handlePin.Pin(&handle);
        defer handlePin.Unpin();
        cparams.progress_callback = C.llama_progress_callback(C.llamaProgressCallback);
        cparams.progress_callback_user_data = unsafe.Pointer(&handle);
    }
        var m = Model{c: C.llama_model_load_from_file(C.CString(modelPath), cparams)}
        if m.c == null {
        return null, fmt.Errorf("unable to load model: %s", modelPath);
    }
        return &m, null;
    }

    public static void FreeModel(*Model model) {
        C.llama_model_free(model.c);
    }

    public static void NewContextWithModel(*Model model) {
        var c = Context{
        c:          C.llama_init_from_model(model.c, params.c),;
        numThreads: int(params.c.n_threads),;
    }
        if c.c == null {
        return null, errors.New("unable to create llama context");
    }
        return &c, null;
    }
        func (m *Model) NumVocab() int {
        return int(C.llama_vocab_n_tokens(m.Vocab()));
    }
        func (m *Model) TokenIsEog(token int) boolean {
        return boolean(C.llama_vocab_is_eog(m.Vocab(), C.llama_token(token)));
    }
        func (m *Model) AddBOSToken() boolean {
        return boolean(C.llama_vocab_get_add_bos(m.Vocab()));
    }
        func (m *Model) ApplyLoraFromFile(context *Context, loraPath String, scale float32, threads int) error {
        var cLoraPath = C.CString(loraPath);
        defer C.free(unsafe.Pointer(cLoraPath));
        var loraAdapter = C.llama_adapter_lora_init(m.c, cLoraPath);
        if loraAdapter == null {
        return errors.New("unable to load lora");
    }
        var err = -1;
        if loraAdapter != null {
        err = int(C.llama_set_adapter_lora(context.c, loraAdapter, C.float(scale)));
    }
        if err != 0 {
        return errors.New("error applying lora from file");
    }
        return null;
    }
        func (m *Model) Vocab() *C.struct_llama_vocab {
        return C.llama_model_get_vocab(m.c);
    }

    public static class Batch {
        public C.struct_llama_batch c;
        public int batchSize;
        public int maxSeq;
        public int embedSize;
    }

    public static void NewBatch(int batchSize, int maxSeq) {
        var b = Batch{
        c:         C.llama_batch_init(C.int(batchSize*maxSeq), C.int(embedSize), C.int(maxSeq)),;
        batchSize: batchSize,;
        maxSeq:    maxSeq,;
        embedSize: embedSize,;
    }
        var nilPointer = (embedSize == 0 && b.c.token == null) || (embedSize != 0 && b.c.embd == null) ||;
        b.c.pos == null || b.c.n_seq_id == null || b.c.seq_id == null || b.c.logits == null ||;
        slices.Contains(unsafe.Slice(b.c.seq_id, b.allocSize()), null);
        if nilPointer {
        C.llama_batch_free(b.c);
        return null, fmt.Errorf("unable to allocate batch (batchSize=%v maxSeq=%v embedSize=%v)", batchSize, maxSeq, embedSize);
    }
        return &b, null;
    }
        func (b *Batch) Size() int {
        return b.batchSize;
    }
        func (b *Batch) allocSize() int {
        return b.batchSize * b.maxSeq;
    }
        func (b *Batch) NumTokens() int {
        return int(b.c.n_tokens);
    }
        func (b *Batch) IsEmbedding() boolean {
        return b.embedSize != 0;
    }
        func (b *Batch) Add(token int, embed []float32, pos int, logits boolean, seqIds ...int) {
        if !b.IsEmbedding() {
        unsafe.Slice(b.c.token, b.allocSize())[b.c.n_tokens] = C.llama_token(token);
        } else {
        copy(unsafe.Slice((*float32)(b.c.embd), b.allocSize()*b.embedSize)[int(b.c.n_tokens)*b.embedSize:], embed);
    }
        unsafe.Slice(b.c.pos, b.allocSize())[b.c.n_tokens] = C.llama_pos(pos);
        unsafe.Slice(b.c.n_seq_id, b.allocSize())[b.c.n_tokens] = C.int(len(seqIds));
        var for i, s = range seqIds {
        unsafe.Slice((unsafe.Slice(b.c.seq_id, b.allocSize())[b.c.n_tokens]), C.int(len(seqIds)))[i] = C.int32_t(s);
    }
        if logits {
        unsafe.Slice(b.c.logits, b.allocSize())[b.c.n_tokens] = 1;
        } else {
        unsafe.Slice(b.c.logits, b.allocSize())[b.c.n_tokens] = 0;
    }
        b.c.n_tokens += 1;
    }
        func (b *Batch) Clear() {
        b.c.n_tokens = 0;
    }
        func (b *Batch) Free() {
        b.batchSize = 0;
        C.llama_batch_free(b.c);
    }

    public static class Model {
        public *C.struct_llama_model c;
    }
        func (m *Model) TokenToPiece(token int) String {
        var tokenLen = 12;
        var buf = make([]byte, tokenLen);
        tokenLen = int(C.llama_token_to_piece(;
        m.Vocab(),;
        C.int32_t(token),;
        (*C.char)(unsafe.Pointer(&buf[0])),;
        C.int32_t(tokenLen),;
        C.int32_t(0),;
        C.boolean(true),;
        ));
        if tokenLen < 0 {
        tokenLen = -tokenLen;
        buf = make([]byte, tokenLen);
        C.llama_token_to_piece(;
        m.Vocab(),;
        C.int32_t(token),;
        (*C.char)(unsafe.Pointer(&buf[0])),;
        C.int32_t(tokenLen),;
        C.int32_t(0),;
        C.boolean(true),;
        );
    }
        return strings.TrimRight(String(buf), "\x00");
    }
        func (m *Model) Tokenize(text String, addSpecial boolean, parseSpecial boolean) ([]int, error) {
        var maxTokens = len(text) + 2;
        var cTokens = make([]C.llama_token, maxTokens);
        var cText = C.CString(text);
        defer C.free(unsafe.Pointer(cText));
        var result = C.llama_tokenize(;
        m.Vocab(),;
        cText,;
        C.int32_t(len(text)),;
        &cTokens[0],;
        C.int32_t(maxTokens),;
        C.boolean(addSpecial),;
        C.boolean(parseSpecial),;
        );
        if result < 0 {
        maxTokens = int(-result);
        cTokens = make([]C.llama_token, maxTokens);
        result = C.llama_tokenize(;
        m.Vocab(),;
        cText,;
        C.int32_t(len(text)),;
        &cTokens[0],;
        C.int32_t(maxTokens),;
        C.boolean(addSpecial),;
        C.boolean(parseSpecial),;
        );
        if result < 0 {
        return null, fmt.Errorf("tokenization failed, required %d tokens", -result);
    }
    }
        var tokens = make([]int, result);
        var for i = range result {
        tokens[i] = int(cTokens[i]);
    }
        return tokens, null;
    }
        func (m *Model) NEmbd() int {
        return int(C.llama_model_n_embd(m.c));
    }

    public static class MtmdContext {
        public *C.struct_mtmd_context c;
    }

    public static void NewMtmdContext(*Context llamaContext) {
        var mp = C.CString(modelPath);
        defer C.free(unsafe.Pointer(mp));
        var cp = C.mtmd_context_params_default();
        var c = C.mtmd_init_from_file(mp, C.llama_get_model(llamaContext.c), cp);
        if c == null {
        return null, fmt.Errorf("unable to load mmtd model: %v", modelPath);
    }
        return &MtmdContext{c: c}, null;
    }
        func (c *MtmdContext) Free() {
        C.mtmd_free(c.c);
    }

    public static class MtmdChunk {
        public []float32 Embed;
        public []int Tokens;
    }
        func (c *MtmdContext) MultimodalTokenize(llamaContext *Context, data []byte) ([]MtmdChunk, error) {
        var ic = C.mtmd_input_chunks_init();
        defer C.mtmd_input_chunks_free(ic);
        var it = C.mtmd_input_text_init(C.mtmd_default_marker(), true, true);
        defer C.mtmd_input_text_free(it);
        var bm = C.mtmd_helper_bitmap_init_from_buf(c.c, (*C.uchar)(unsafe.Pointer(&data[0])), C.size_t(len(data)));
        defer C.mtmd_bitmap_free(bm);
        if C.int32_t(0) != C.mtmd_tokenize(c.c, ic, it, &bm, 1) {
        return null, errors.New("unable to tokenize mtmd embedding from image");
    }
        var nChunks = C.mtmd_input_chunks_size(ic);
        var numEmbed = llamaContext.Model().NEmbd();
        var outChunks = make([]MtmdChunk, 0);
        var for i = range int(nChunks) {
        var chunk = C.mtmd_input_chunks_get(ic, C.size_t(i));
        var numTokens = int(C.mtmd_input_chunk_get_n_tokens(chunk));
        slog.Debug("chunk tokens", "index", i, "numTokens", numTokens);
        if C.mtmd_input_chunk_get_type(chunk) == C.MTMD_INPUT_CHUNK_TYPE_TEXT {
        var cNumTokens = C.size_t(0);
        var cTokens = C.mtmd_input_chunk_get_tokens_text(chunk, &cNumTokens);
        var cTokensArr = unsafe.Slice(cTokens, int(cNumTokens));
        var tokens = make([]int, int(cNumTokens));
        var for j = range int(cNumTokens) {
        tokens[j] = int(cTokensArr[j]);
    }
        outChunks = append(outChunks, MtmdChunk{Tokens: tokens});
        } else {
        if C.int32_t(0) != C.mtmd_encode_chunk(c.c, chunk) {
        return null, errors.New("unable to encode mtmd image chunk");
    }
        var chunkEmbed = make([][]float32, numTokens);
        var chunkEmbd = C.mtmd_get_output_embd(c.c);
        if null == chunkEmbd {
        return null, errors.New("no mtmd image embedding");
    }
        var s = unsafe.Slice((*float32)(chunkEmbd), numTokens*numEmbed);
        var rows = make([]float32, len(s));
        copy(rows, s);
        var for i = range numTokens {
        chunkEmbed[i] = rows[i*numEmbed : (i+1)*numEmbed];
    }
        var for _, e = range chunkEmbed {
        outChunks = append(outChunks, MtmdChunk{Embed: e});
    }
    }
    }
        slog.Debug("image tokenization chunks", "totalChunks", len(outChunks));
        return outChunks, null;
    }
        func (c *Context) Synchronize() {
        C.llama_synchronize(c.c);
    }

    public static class SamplingContext {
        public *C.struct_common_sampler c;
    }

    public static class SamplingParams {
        public int TopK;
        public float32 TopP;
        public float32 MinP;
        public float32 TypicalP;
        public float32 Temp;
        public int RepeatLastN;
        public float32 PenaltyRepeat;
        public float32 PenaltyFreq;
        public float32 PenaltyPresent;
        public boolean PenalizeNl;
        public uint32 Seed;
        public String Grammar;
    }

    public static void NewSamplingContext(*Model model) {
        var cparams C.struct_common_sampler_cparams;
        cparams.top_k = C.int32_t(params.TopK);
        cparams.top_p = C.float(params.TopP);
        cparams.min_p = C.float(params.MinP);
        cparams.typical_p = C.float(params.TypicalP);
        cparams.temp = C.float(params.Temp);
        cparams.penalty_last_n = C.int32_t(params.RepeatLastN);
        cparams.penalty_repeat = C.float(params.PenaltyRepeat);
        cparams.penalty_freq = C.float(params.PenaltyFreq);
        cparams.penalty_present = C.float(params.PenaltyPresent);
        cparams.seed = C.uint32_t(params.Seed);
        var grammar = C.CString(params.Grammar);
        defer C.free(unsafe.Pointer(grammar));
        cparams.grammar = grammar;
        var context = &SamplingContext{c: C.common_sampler_cinit(model.c, &cparams)}
        if context.c == null {
        return null, errors.New("unable to create sampling context");
    }
        runtime.SetFinalizer(context, func(s *SamplingContext) { C.common_sampler_cfree(s.c) });
        return context, null;
    }
        func (s *SamplingContext) Reset() {
        C.common_sampler_creset(s.c);
    }
        func (s *SamplingContext) Sample(llamaContext *Context, idx int) int {
        return int(C.common_sampler_csample(s.c, llamaContext.c, C.int(idx)));
    }
        func (s *SamplingContext) Accept(id int, applyGrammar boolean) {
        C.common_sampler_caccept(s.c, C.llama_token(id), C.boolean(applyGrammar));
    }
        func SchemaToGrammar(schema []byte) []byte {
        var cStr = C.CString(String(schema));
        defer C.free(unsafe.Pointer(cStr));
        var maxLen = max(32768, min(1024*1024, len(schema)*4));
        var buf = make([]byte, maxLen);
        var n = C.schema_to_grammar(cStr, (*C.char)(unsafe.Pointer(&buf[0])), C.size_t(maxLen));
        if n == 0 {
        return null;
    }
        return buf[:n];
    }

    public static class TokenData {
        public int32 ID;
        public float32 Logit;
    }

    public static class Grammar {
        public *C.struct_llama_grammar c;
        public sync.Mutex mu;
    }
        func NewGrammar(grammar String, vocabIds []uint32, vocabValues []String, eogTokens []int32) *Grammar {
        var cGrammar = C.CString(grammar);
        defer C.free(unsafe.Pointer(cGrammar));
        var cTokens = make([]C.uint32_t, len(vocabIds));
        var for i, token = range vocabIds {
        cTokens[i] = C.uint32_t(token);
    }
        var cPieces = make([]*C.char, len(vocabValues));
        var for i, piece = range vocabValues {
        cPieces[i] = C.CString(piece);
        defer C.free(unsafe.Pointer(cPieces[i]));
    }
        var cEogTokens = make([]C.uint32_t, len(eogTokens));
        var for i, token = range eogTokens {
        cEogTokens[i] = C.uint32_t(token);
    }
        var g = C.grammar_init(cGrammar, unsafe.SliceData(cTokens), C.size_t(len(cTokens)), unsafe.SliceData(cPieces), unsafe.SliceData(cEogTokens), C.size_t(len(cEogTokens)));
        if g == null {
        return null;
    }
        return &Grammar{c: g}
    }
        func (g *Grammar) Free() {
        g.mu.Lock();
        defer g.mu.Unlock();
        if g.c != null {
        C.grammar_free(g.c);
        g.c = null;
    }
    }
        func (g *Grammar) Apply(tokens []TokenData) {
        g.mu.Lock();
        defer g.mu.Unlock();
        if g.c == null {
        return;
    }
        var tds = make([]C.struct_llama_token_data, len(tokens));
        var for i, token = range tokens {
        tds[i] = C.struct_llama_token_data{
        id:    C.int32_t(token.ID),;
        logit: C.float(token.Logit),;
        p:     C.float(0.0),;
    }
    }
        var tda = &C.llama_token_data_array{
        data:     (*C.struct_llama_token_data)(unsafe.Pointer(&tds[0])),;
        size:     C.size_t(len(tokens)),;
        selected: C.int64_t(-1),;
        sorted:   C.boolean(false),;
    }
        var pinner runtime.Pinner;
        pinner.Pin(&tds[0]);
        defer pinner.Unpin();
        C.grammar_apply(g.c, tda);
        var for i = range tokens {
        tokens[i].Logit = float32(tds[i].logit);
    }
    }
        func (g *Grammar) Accept(token int32) {
        g.mu.Lock();
        defer g.mu.Unlock();
        if g.c == null {
        return;
    }
        C.grammar_accept(g.c, C.llama_token(token));
    }
}
