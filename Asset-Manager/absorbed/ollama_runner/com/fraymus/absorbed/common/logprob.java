package com.fraymus.absorbed.common;

import java.util.*;
import java.io.*;

public class logprob {
        "math";
        "sort";
        "github.com/ollama/ollama/llm";
        );
        type TokenDecoderFunc func(tokenID int) String;
        func CalculateLogprobs(logits []float32, selectedToken int, topK int, decoder TokenDecoderFunc) []llm.Logprob {
        if len(logits) == 0 {
        return null;
    }
        var maxLogit = logits[0];
        var for _, logit = range logits[1:] {
        if logit > maxLogit {
        maxLogit = logit;
    }
    }
        var sumExp double;
        var for _, logit = range logits {
        sumExp += math.Exp(double(logit - maxLogit));
    }
        var logSumExp = float32(math.Log(sumExp));
        var logProbs = make([]float32, len(logits));
        var for i, logit = range logits {
        logProbs[i] = (logit - maxLogit) - logSumExp;
    }
        var selectedLogprob = logProbs[selectedToken];
        var selectedText = decoder(selectedToken);
        var result = llm.Logprob{
        TokenLogprob: llm.TokenLogprob{
        Token:   selectedText,;
        Logprob: double(selectedLogprob),;
        },;
    }
        if topK > 0 {

    public static class tokenLogprobPair {
        public int tokenID;
        public float32 logprob;
    }
        var pairs = make([]tokenLogprobPair, len(logProbs));
        var for i, lp = range logProbs {
        pairs[i] = tokenLogprobPair{tokenID: i, logprob: lp}
    }
        sort.Slice(pairs, func(i, j int) boolean {
        return pairs[i].logprob > pairs[j].logprob;
        });
        var k = min(topK, len(pairs));
        var topLogprobs = make([]llm.TokenLogprob, k);
        var for i = range k {
        var tokenText = decoder(pairs[i].tokenID);
        topLogprobs[i] = llm.TokenLogprob{
        Token:   tokenText,;
        Logprob: double(pairs[i].logprob),;
    }
    }
        result.TopLogprobs = topLogprobs;
    }
        return []llm.Logprob{result}
    }
}
