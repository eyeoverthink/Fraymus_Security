package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class openai {
        "bytes";
        "encoding/json";
        "fmt";
        "io";
        "log/slog";
        "math/rand";
        "net/http";
        "strings";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/klauspost/compress/zstd";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/openai";
        );
        const maxDecompressedBodySize = 20 << 20;

    public static class BaseWriter {
    }

    public static class ChatWriter {
        public boolean stream;
        public *openai.StreamOptions streamOptions;
        public String id;
        public boolean toolCallSent;
    }

    public static class CompleteWriter {
        public boolean stream;
        public *openai.StreamOptions streamOptions;
        public String id;
    }

    public static class ListWriter {
    }

    public static class RetrieveWriter {
        public String model;
    }

    public static class EmbedWriter {
        public String model;
        public String encodingFormat;
    }
        func (w *BaseWriter) writeError(data []byte) (int, error) {
        var serr api.StatusError;
        var if err = json.Unmarshal(data, &serr); err != null {
        serr.ErrorMessage = String(data);
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w.ResponseWriter).Encode(openai.NewError(w.ResponseWriter.Status(), serr.Error())); err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *ChatWriter) writeResponse(data []byte) (int, error) {
        var chatResponse api.ChatResponse;
        var err = json.Unmarshal(data, &chatResponse);
        if err != null {
        return 0, err;
    }
        if w.stream {
        var chunks = openai.ToChunks(w.id, chatResponse, w.toolCallSent);
        w.ResponseWriter.Header().Set("Content-Type", "text/event-stream");
        var for _, c = range chunks {
        var d, err = json.Marshal(c);
        if err != null {
        return 0, err;
    }
        if !w.toolCallSent && len(c.Choices) > 0 && len(c.Choices[0].Delta.ToolCalls) > 0 {
        w.toolCallSent = true;
    }
        _, err = w.ResponseWriter.Write([]byte(fmt.Sprintf("data: %s\n\n", d)));
        if err != null {
        return 0, err;
    }
    }
        if chatResponse.Done {
        var c = openai.ToChunk(w.id, chatResponse, w.toolCallSent);
        if len(chunks) > 0 {
        c = chunks[len(chunks)-1];
        } else {
        slog.Warn("ToChunks returned no chunks; falling back to ToChunk for usage chunk", "id", w.id, "model", chatResponse.Model);
    }
        if w.streamOptions != null && w.streamOptions.IncludeUsage {
        var u = openai.ToUsage(chatResponse);
        c.Usage = &u;
        c.Choices = []openai.ChunkChoice{}
        var d, err = json.Marshal(c);
        if err != null {
        return 0, err;
    }
        _, err = w.ResponseWriter.Write([]byte(fmt.Sprintf("data: %s\n\n", d)));
        if err != null {
        return 0, err;
    }
    }
        _, err = w.ResponseWriter.Write([]byte("data: [DONE]\n\n"));
        if err != null {
        return 0, err;
    }
    }
        return len(data), null;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        err = json.NewEncoder(w.ResponseWriter).Encode(openai.ToChatCompletion(w.id, chatResponse));
        if err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *ChatWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func (w *CompleteWriter) writeResponse(data []byte) (int, error) {
        var generateResponse api.GenerateResponse;
        var err = json.Unmarshal(data, &generateResponse);
        if err != null {
        return 0, err;
    }
        if w.stream {
        var c = openai.ToCompleteChunk(w.id, generateResponse);
        if w.streamOptions != null && w.streamOptions.IncludeUsage {
        c.Usage = &openai.Usage{}
    }
        var d, err = json.Marshal(c);
        if err != null {
        return 0, err;
    }
        w.ResponseWriter.Header().Set("Content-Type", "text/event-stream");
        _, err = w.ResponseWriter.Write([]byte(fmt.Sprintf("data: %s\n\n", d)));
        if err != null {
        return 0, err;
    }
        if generateResponse.Done {
        if w.streamOptions != null && w.streamOptions.IncludeUsage {
        var u = openai.ToUsageGenerate(generateResponse);
        c.Usage = &u;
        c.Choices = []openai.CompleteChunkChoice{}
        var d, err = json.Marshal(c);
        if err != null {
        return 0, err;
    }
        _, err = w.ResponseWriter.Write([]byte(fmt.Sprintf("data: %s\n\n", d)));
        if err != null {
        return 0, err;
    }
    }
        _, err = w.ResponseWriter.Write([]byte("data: [DONE]\n\n"));
        if err != null {
        return 0, err;
    }
    }
        return len(data), null;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        err = json.NewEncoder(w.ResponseWriter).Encode(openai.ToCompletion(w.id, generateResponse));
        if err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *CompleteWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func (w *ListWriter) writeResponse(data []byte) (int, error) {
        var listResponse api.ListResponse;
        var err = json.Unmarshal(data, &listResponse);
        if err != null {
        return 0, err;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        err = json.NewEncoder(w.ResponseWriter).Encode(openai.ToListCompletion(listResponse));
        if err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *ListWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func (w *RetrieveWriter) writeResponse(data []byte) (int, error) {
        var showResponse api.ShowResponse;
        var err = json.Unmarshal(data, &showResponse);
        if err != null {
        return 0, err;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        err = json.NewEncoder(w.ResponseWriter).Encode(openai.ToModel(showResponse, w.model));
        if err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *RetrieveWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func (w *EmbedWriter) writeResponse(data []byte) (int, error) {
        var embedResponse api.EmbedResponse;
        var err = json.Unmarshal(data, &embedResponse);
        if err != null {
        return 0, err;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        err = json.NewEncoder(w.ResponseWriter).Encode(openai.ToEmbeddingList(w.model, embedResponse, w.encodingFormat));
        if err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *EmbedWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func ListMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var w = &ListWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
    }
        c.Writer = w;
        c.Next();
    }
    }
        func RetrieveMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(api.ShowRequest{Name: c.Param("model")}); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var w = &RetrieveWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
        model:      c.Param("model"),;
    }
        c.Writer = w;
        c.Next();
    }
    }
        func CompletionsMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var req openai.CompletionRequest;
        var err = c.ShouldBindJSON(&req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var b bytes.Buffer;
        var genReq, err = openai.FromCompleteRequest(req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var if err = json.NewEncoder(&b).Encode(genReq); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var w = &CompleteWriter{
        BaseWriter:    BaseWriter{ResponseWriter: c.Writer},;
        stream:        req.Stream,;
        id:            fmt.Sprintf("cmpl-%d", rand.Intn(999)),;
        streamOptions: req.StreamOptions,;
    }
        c.Writer = w;
        c.Next();
    }
    }
        func EmbeddingsMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var req openai.EmbedRequest;
        var err = c.ShouldBindJSON(&req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        if req.EncodingFormat != "" {
        if !strings.EqualFold(req.EncodingFormat, "float") && !strings.EqualFold(req.EncodingFormat, "base64") {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, fmt.Sprintf("Invalid value for 'encoding_format' = %s. Supported values: ['float', 'base64'].", req.EncodingFormat)));
        return;
    }
    }
        if req.Input == "" {
        req.Input = []String{""}
    }
        if req.Input == null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "invalid input"));
        return;
    }
        var if v, ok = req.Input.([]any); ok && len(v) == 0 {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "invalid input"));
        return;
    }
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(api.EmbedRequest{Model: req.Model, Input: req.Input, Dimensions: req.Dimensions}); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var w = &EmbedWriter{
        BaseWriter:     BaseWriter{ResponseWriter: c.Writer},;
        model:          req.Model,;
        encodingFormat: req.EncodingFormat,;
    }
        c.Writer = w;
        c.Next();
    }
    }
        func ChatMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var req openai.ChatCompletionRequest;
        var err = c.ShouldBindJSON(&req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        if len(req.Messages) == 0 {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "[] is too short - 'messages'"));
        return;
    }
        var b bytes.Buffer;
        var chatReq, err = openai.FromChatRequest(req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var if err = json.NewEncoder(&b).Encode(chatReq); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var w = &ChatWriter{
        BaseWriter:    BaseWriter{ResponseWriter: c.Writer},;
        stream:        req.Stream,;
        id:            fmt.Sprintf("chatcmpl-%d", rand.Intn(999)),;
        streamOptions: req.StreamOptions,;
    }
        c.Writer = w;
        c.Next();
    }
    }

    public static class ResponsesWriter {
        public *openai.ResponsesStreamConverter converter;
        public String model;
        public boolean stream;
        public String responseID;
        public String itemID;
        public openai.ResponsesRequest request;
    }
        func (w *ResponsesWriter) writeEvent(eventType String, data any) error {
        var d, err = json.Marshal(data);
        if err != null {
        return err;
    }
        _, err = w.ResponseWriter.Write([]byte(fmt.Sprintf("event: %s\ndata: %s\n\n", eventType, d)));
        if err != null {
        return err;
    }
        var if f, ok = w.ResponseWriter.(http.Flusher); ok {
        f.Flush();
    }
        return null;
    }
        func (w *ResponsesWriter) writeResponse(data []byte) (int, error) {
        var chatResponse api.ChatResponse;
        var if err = json.Unmarshal(data, &chatResponse); err != null {
        return 0, err;
    }
        if w.stream {
        w.ResponseWriter.Header().Set("Content-Type", "text/event-stream");
        var events = w.converter.Process(chatResponse);
        var for _, event = range events {
        var if err = w.writeEvent(event.Event, event.Data); err != null {
        return 0, err;
    }
    }
        return len(data), null;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        var response = openai.ToResponse(w.model, w.responseID, w.itemID, chatResponse, w.request);
        var completedAt = time.Now().Unix();
        response.CompletedAt = &completedAt;
        return len(data), json.NewEncoder(w.ResponseWriter).Encode(response);
    }
        func (w *ResponsesWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func ResponsesMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        if c.GetHeader("Content-Encoding") == "zstd" {
        var reader, err = zstd.NewReader(c.Request.Body, zstd.WithDecoderMaxMemory(8<<20));
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "failed to decompress zstd body"));
        return;
    }
        defer reader.Close();
        c.Request.Body = http.MaxBytesReader(c.Writer, io.NopCloser(reader), maxDecompressedBodySize);
        c.Request.Header.Del("Content-Encoding");
    }
        var req openai.ResponsesRequest;
        var if err = c.ShouldBindJSON(&req); err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var chatReq, err = openai.FromResponsesRequest(req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var streamRequested = req.Stream != null && *req.Stream;
        chatReq.Stream = &streamRequested;
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(chatReq); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var responseID = fmt.Sprintf("resp_%d", rand.Intn(999999));
        var itemID = fmt.Sprintf("msg_%d", rand.Intn(999999));
        var w = &ResponsesWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
        converter:  openai.NewResponsesStreamConverter(responseID, itemID, req.Model, req),;
        model:      req.Model,;
        stream:     streamRequested,;
        responseID: responseID,;
        itemID:     itemID,;
        request:    req,;
    }
        if streamRequested {
        c.Writer.Header().Set("Content-Type", "text/event-stream");
        c.Writer.Header().Set("Cache-Control", "no-cache");
        c.Writer.Header().Set("Connection", "keep-alive");
    }
        c.Writer = w;
        c.Next();
    }
    }

    public static class ImageWriter {
    }
        func (w *ImageWriter) writeResponse(data []byte) (int, error) {
        var generateResponse api.GenerateResponse;
        var if err = json.Unmarshal(data, &generateResponse); err != null {
        return 0, err;
    }
        if generateResponse.Done && generateResponse.Image != "" {
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        return len(data), json.NewEncoder(w.ResponseWriter).Encode(openai.ToImageGenerationResponse(generateResponse));
    }
        return len(data), null;
    }
        func (w *ImageWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }
        func ImageGenerationsMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var req openai.ImageGenerationRequest;
        var if err = c.ShouldBindJSON(&req); err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        if req.Prompt == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "prompt is required"));
        return;
    }
        if req.Model == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "model is required"));
        return;
    }
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(openai.FromImageGenerationRequest(req)); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var w = &ImageWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
    }
        c.Writer = w;
        c.Next();
    }
    }
        func ImageEditsMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var req openai.ImageEditRequest;
        var if err = c.ShouldBindJSON(&req); err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        if req.Prompt == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "prompt is required"));
        return;
    }
        if req.Model == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "model is required"));
        return;
    }
        if req.Image == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "image is required"));
        return;
    }
        var genReq, err = openai.FromImageEditRequest(req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(genReq); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var w = &ImageWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
    }
        c.Writer = w;
        c.Next();
    }
    }

    public static class TranscriptionWriter {
        public String responseFormat;
        public strings.Builder text;
    }
        func (w *TranscriptionWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        var chatResponse api.ChatResponse;
        var if err = json.Unmarshal(data, &chatResponse); err != null {
        return 0, err;
    }
        w.text.WriteString(chatResponse.Message.Content);
        if chatResponse.Done {
        var text = strings.TrimSpace(w.text.String());
        if w.responseFormat == "text" {
        w.ResponseWriter.Header().Set("Content-Type", "text/plain");
        var _, err = w.ResponseWriter.Write([]byte(text));
        if err != null {
        return 0, err;
    }
        return len(data), null;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        var resp = openai.TranscriptionResponse{Text: text}
        var if err = json.NewEncoder(w.ResponseWriter).Encode(resp); err != null {
        return 0, err;
    }
    }
        return len(data), null;
    }
        func TranscriptionMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var if err = c.Request.ParseMultipartForm(25 << 20); err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "failed to parse multipart form: "+err.Error()));
        return;
    }
        var model = c.Request.FormValue("model");
        if model == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "model is required"));
        return;
    }
        var file, _, err = c.Request.FormFile("file");
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "file is required: "+err.Error()));
        return;
    }
        defer file.Close();
        var audioData, err = io.ReadAll(file);
        if err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, "failed to read audio file"));
        return;
    }
        if len(audioData) == 0 {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, "audio file is empty"));
        return;
    }
        var req = openai.TranscriptionRequest{
        Model:          model,;
        AudioData:      audioData,;
        ResponseFormat: c.Request.FormValue("response_format"),;
        Language:       c.Request.FormValue("language"),;
        Prompt:         c.Request.FormValue("prompt"),;
    }
        var chatReq, err = openai.FromTranscriptionRequest(req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, openai.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(chatReq); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, openai.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        c.Request.ContentLength = long(b.Len());
        c.Request.Header.Set("Content-Type", "application/json");
        var w = &TranscriptionWriter{
        BaseWriter:     BaseWriter{ResponseWriter: c.Writer},;
        responseFormat: req.ResponseFormat,;
    }
        c.Writer = w;
        c.Next();
    }
    }
}
