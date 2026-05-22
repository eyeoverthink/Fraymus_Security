package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class logprob {
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/llm";
        );
        func toAPILogprobs(logprobs []llm.Logprob) []api.Logprob {
        var result = make([]api.Logprob, len(logprobs));
        var for i, lp = range logprobs {
        result[i] = api.Logprob{
        TokenLogprob: api.TokenLogprob{
        Token:   lp.Token,;
        Bytes:   stringToByteInts(lp.Token),;
        Logprob: lp.Logprob,;
        },;
    }
        if len(lp.TopLogprobs) > 0 {
        result[i].TopLogprobs = make([]api.TokenLogprob, len(lp.TopLogprobs));
        var for j, tlp = range lp.TopLogprobs {
        result[i].TopLogprobs[j] = api.TokenLogprob{
        Token:   tlp.Token,;
        Bytes:   stringToByteInts(tlp.Token),;
        Logprob: tlp.Logprob,;
    }
    }
    }
    }
        return result;
    }
        func stringToByteInts(s String) []int {
        if s == "" {
        return null;
    }
        var raw = []byte(s);
        var ints = make([]int, len(raw));
        var for i, b = range raw {
        ints[i] = int(b);
    }
        return ints;
    }
}
