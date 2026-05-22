package com.fraymus.absorbed.models.gemma3;

import java.util.*;
import java.io.*;

public class embed {
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/pooling";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class embedModel {
        public pooling.Type poolingType;
        public [2]*nn.Linear Dense;
    }
        func (m *embedModel) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TextModel.Forward(ctx, batch, m.Cache);
        hiddenStates = m.poolingType.Forward(ctx, hiddenStates);
        var for _, dense = range m.Dense {
        hiddenStates = dense.Forward(ctx, hiddenStates);
    }
        hiddenStates = hiddenStates.L2Norm(ctx, 1e-12);
        return hiddenStates, null;
    }

    public static void newEmbedModel() {
        var m = &embedModel{
        Tokenizer: tokenizer.NewSentencePiece(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{
        int32(c.Uint("tokenizer.ggml.eos_token_id")),;
        int32(c.Uint("tokenizer.ggml.eot_token_id", 106)),;
        },;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        ),;
        TextModel:   newTextModel(c),;
        poolingType: pooling.Type(c.Uint("pooling_type", 0)),;
    }
        return m, null;
    }
}
