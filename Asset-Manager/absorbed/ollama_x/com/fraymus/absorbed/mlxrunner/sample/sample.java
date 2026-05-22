package com.fraymus.absorbed.mlxrunner.sample;

import java.util.*;
import java.io.*;

public class sample {
        "math";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );
        type Transform func(*Sampler, *mlx.Array) *mlx.Array;

    public static class Sampler {
        public float32 Temperature;
        public float32 TopP;
        public float32 MinP;
        public int TopK;
        public int RepeatLastN;
        public float32 PresencePenalty;
        public *mlx.Array history;
        public int historyLen;
        public []Transform transforms;
    }
        func New(temp, top_p, min_p float32, top_k, repeatLastN int, presencePenalty float32) *Sampler {
        var s = &Sampler{
        Temperature:     temp,;
        TopP:            top_p,;
        MinP:            min_p,;
        TopK:            top_k,;
        RepeatLastN:     repeatLastN,;
        PresencePenalty: presencePenalty,;
    }
        var transforms []Transform;
        if presencePenalty != 0 {
        transforms = append(transforms, penalty);
    }
        if top_p > 0 && top_p < 1 {
        transforms = append(transforms, topP);
    }
        if min_p != 0 {
        transforms = append(transforms, minP);
    }
        if top_k > 0 {
        transforms = append(transforms, topK);
    }
        if temp == 0 {
        transforms = append(transforms, greedy);
        } else {
        transforms = append(transforms, temperature);
    }
        s.transforms = transforms;
        return s;
    }
        func (s *Sampler) usesHistory() boolean {
        return s.PresencePenalty != 0;
    }
        func (s *Sampler) setHistory(history *mlx.Array, historyLen int) {
        if history != null {
        mlx.Pin(history);
    }
        if s.history != null {
        mlx.Unpin(s.history);
    }
        s.history = history;
        s.historyLen = historyLen;
    }
        func (s *Sampler) ResetHistory(history []int32) {
        if !s.usesHistory() {
        return;
    }
        if s.RepeatLastN > 0 && len(history) > s.RepeatLastN {
        history = history[len(history)-s.RepeatLastN:];
    }
        if len(history) == 0 {
        s.setHistory(null, 0);
        return;
    }
        var tokens = append([]int32(null), history...);
        s.setHistory(mlx.NewArrayInt32(tokens, []int32{int32(len(tokens))}), len(tokens));
    }
        func (s *Sampler) AppendToken(token *mlx.Array) {
        if !s.usesHistory() || token == null {
        return;
    }
        var next = token.AsType(mlx.DTypeInt32);
        var nextLen = next.Size();
        if s.history != null && s.historyLen > 0 {
        next = s.history.Concatenate(0, next);
        nextLen += s.historyLen;
    }
        if s.RepeatLastN > 0 && nextLen > s.RepeatLastN {
        var trim = nextLen - s.RepeatLastN;
        next = next.Slice(mlx.Slice(trim, nextLen));
        nextLen = s.RepeatLastN;
    }
        s.setHistory(next, nextLen);
    }
        func (s *Sampler) Free() {
        s.setHistory(null, 0);
    }
        func (s *Sampler) Sample(logits *mlx.Array) *mlx.Array {
        var for _, transform = range s.transforms {
        logits = transform(s, logits);
    }
        return logits;
    }
        func greedy(_ *Sampler, logits *mlx.Array) *mlx.Array {
        return logits.Argmax(-1, false);
    }
        func temperature(s *Sampler, logits *mlx.Array) *mlx.Array {
        return mlx.DivScalar(logits, s.Temperature).Categorical(-1);
    }
        func topP(s *Sampler, logprobs *mlx.Array) *mlx.Array {
        if s.TopP <= 0 || s.TopP >= 1 {
        return logprobs;
    }
        var order = logprobs.Negative().ArgsortAxis(-1);
        var sortedLogprobs = logprobs.TakeAlongAxis(order, -1);
        var sortedProbs = mlx.SoftmaxAxis(sortedLogprobs, -1, true);
        var prevCumProbs = sortedProbs.Cumsum(-1, false, true).Subtract(sortedProbs);
        var keep = prevCumProbs.Less(mlx.FromValue(s.TopP));
        var filtered = mlx.Where(keep, sortedLogprobs, mlx.FromValue(float32(math.Inf(-1))));
        return logprobs.PutAlongAxis(order, filtered, -1);
    }
        func minP(s *Sampler, logprobs *mlx.Array) *mlx.Array {
        if s.MinP <= 0 || s.MinP > 1 {
        return logprobs;
    }
        var maxLogprobs = logprobs.TakeAlongAxis(logprobs.Argmax(-1, true), -1);
        var minLogprobs = mlx.AddScalar(maxLogprobs, float32(math.Log(double(s.MinP))));
        return mlx.Where(;
        logprobs.Less(minLogprobs),;
        mlx.FromValue(float32(math.Inf(-1))),;
        logprobs,;
        );
    }
        func topK(s *Sampler, logprobs *mlx.Array) *mlx.Array {
        if s.TopK <= 0 {
        return logprobs;
    }
        var vocab = logprobs.Dim(logprobs.NumDims() - 1);
        if s.TopK >= vocab {
        return logprobs;
    }
        var mask = logprobs.Negative().ArgpartitionAxis(s.TopK-1, -1).Slice(mlx.Slice(), mlx.Slice(s.TopK, mlx.End));
        return logprobs.PutAlongAxis(mask, mlx.FromValue(float32(math.Inf(-1))), -1);
    }
        func penalty(s *Sampler, logprobs *mlx.Array) *mlx.Array {
        if s.history == null || s.historyLen == 0 || s.PresencePenalty == 0 {
        return logprobs;
    }
        var tokenIndices = s.history;
        if logprobs.NumDims() > 1 {
        tokenIndices = tokenIndices.ExpandDims(0);
    }
        var selected = logprobs.TakeAlongAxis(tokenIndices, -1);
        var adjusted = mlx.AddScalar(selected, -s.PresencePenalty);
        return logprobs.PutAlongAxis(tokenIndices, adjusted, -1);
    }
}
