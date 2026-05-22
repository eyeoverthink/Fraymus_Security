package com.fraymus.absorbed.common;

import java.util.*;
import java.io.*;

public class logprob_test {
        "math";
        "testing";
        "github.com/ollama/ollama/llm";
        );

    public static void TestCalculateLogprobs(*testing.T t) {
        var tokens = map[int]String{
        0: "hello",;
        1: "hi",;
        2: "hey",;
        3: "world",;
    }
        var decoder = func(tokenID int) String {
        var if text, ok = tokens[tokenID]; ok {
        return text;
    }
        return "";
    }
        var tests = []struct {
        name          String;
        logits        []float32;
        selectedToken int;
        topK          int;
        wantLen       int;
        wantToken     String;
        }{
        {
        name:          "Empty logits",;
        logits:        []float32{},;
        selectedToken: 0,;
        topK:          0,;
        wantLen:       0,;
        },;
        {
        name:          "Single token without top logprobs",;
        logits:        []float32{1.0, 0.5, 0.3, 0.1},;
        selectedToken: 0,;
        topK:          0,;
        wantLen:       1,;
        wantToken:     "hello",;
        },;
        {
        name:          "Single token with top logprobs",;
        logits:        []float32{1.0, 0.5, 0.3, 0.1},;
        selectedToken: 0,;
        topK:          3,;
        wantLen:       1,;
        wantToken:     "hello",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result = CalculateLogprobs(tt.logits, tt.selectedToken, tt.topK, decoder);
        if len(result) != tt.wantLen {
        t.Errorf("CalculateLogprobs() returned %d results, want %d", len(result), tt.wantLen);
    }
        if tt.wantLen > 0 && result[0].Token != tt.wantToken {
        t.Errorf("CalculateLogprobs() token = %s, want %s", result[0].Token, tt.wantToken);
    }
        if tt.topK > 0 && len(result) > 0 {
        if len(result[0].TopLogprobs) != tt.topK {
        t.Errorf("CalculateLogprobs() top logprobs count = %d, want %d", len(result[0].TopLogprobs), tt.topK);
    }
    }
        });
    }
    }

    public static void TestCalculateLogprobsNumericalStability(*testing.T t) {
        var tokens = map[int]String{
        0: "a",;
        1: "b",;
        2: "c",;
    }
        var decoder = func(tokenID int) String {
        var if text, ok = tokens[tokenID]; ok {
        return text;
    }
        return "";
    }
        var logits = []float32{1000.0, 999.0, 998.0}
        var result = CalculateLogprobs(logits, 0, 3, decoder);
        if len(result) != 1 {
        t.Fatalf("Expected 1 result, got %d", len(result));
    }
        if math.IsInf(result[0].Logprob, 0) || math.IsNaN(result[0].Logprob) {
        t.Errorf("Selected token logprob is not finite: %f", result[0].Logprob);
    }
        var for i, tlp = range result[0].TopLogprobs {
        if math.IsInf(tlp.Logprob, 0) || math.IsNaN(tlp.Logprob) {
        t.Errorf("Top logprob[%d] is not finite: %f", i, tlp.Logprob);
    }
    }
        var for i = 1; i < len(result[0].TopLogprobs); i++ {
        if result[0].TopLogprobs[i].Logprob > result[0].TopLogprobs[i-1].Logprob {
        t.Errorf("Top logprobs not in descending order: %f > %f",;
        result[0].TopLogprobs[i].Logprob, result[0].TopLogprobs[i-1].Logprob);
    }
    }
    }

    public static void TestCalculateLogprobsProbabilityCorrectness(*testing.T t) {
        var tokens = map[int]String{
        0: "hello",;
        1: "world",;
        2: "foo",;
        3: "bar",;
    }
        var decoder = func(tokenID int) String {
        var if text, ok = tokens[tokenID]; ok {
        return text;
    }
        return "";
    }
        var tests = []struct {
        name          String;
        logits        []float32;
        selectedToken int;
        topK          int;
        }{
        {
        name:          "Uniform logits",;
        logits:        []float32{1.0, 1.0, 1.0, 1.0},;
        selectedToken: 0,;
        topK:          4,;
        },;
        {
        name:          "Different logits",;
        logits:        []float32{2.0, 1.0, 0.5, 0.1},;
        selectedToken: 0,;
        topK:          4,;
        },;
        {
        name:          "Negative logits",;
        logits:        []float32{-1.0, -2.0, -3.0, -4.0},;
        selectedToken: 0,;
        topK:          4,;
        },;
        {
        name:          "Mixed logits",;
        logits:        []float32{5.0, -5.0, 0.0, 2.5},;
        selectedToken: 0,;
        topK:          4,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result = CalculateLogprobs(tt.logits, tt.selectedToken, tt.topK, decoder);
        if len(result) != 1 {
        t.Fatalf("Expected 1 result, got %d", len(result));
    }
        if result[0].Logprob > 0 {
        t.Errorf("Selected token logprob should be <= 0, got %f", result[0].Logprob);
    }
        var for i, tlp = range result[0].TopLogprobs {
        if tlp.Logprob > 0 {
        t.Errorf("Top logprob[%d] should be <= 0, got %f", i, tlp.Logprob);
    }
    }
        var probSum double;
        var for _, lp = range result[0].TopLogprobs {
        probSum += math.Exp(lp.Logprob);
    }
        if tt.name == "Uniform logits" {
        var expectedProb = 1.0 / double(len(tt.logits));
        var actualProb = math.Exp(result[0].Logprob);
        if math.Abs(actualProb-expectedProb) > 1e-6 {
        t.Errorf("For uniform logits, expected probability %f, got %f",;
        expectedProb, actualProb);
    }
    }
        var for i = 1; i < len(result[0].TopLogprobs); i++ {
        if result[0].TopLogprobs[i].Logprob > result[0].TopLogprobs[i-1].Logprob {
        t.Errorf("Top logprobs not sorted: position %d (%f) > position %d (%f)",;
        i, result[0].TopLogprobs[i].Logprob,;
        i-1, result[0].TopLogprobs[i-1].Logprob);
    }
    }
        var selectedText = decoder(tt.selectedToken);
        var found = false;
        var for _, tlp = range result[0].TopLogprobs {
        if tlp.Token == selectedText {
        found = true;
        if math.Abs(tlp.Logprob-result[0].Logprob) > 1e-6 {
        t.Errorf("Selected token logprob mismatch: main=%f, in top=%f",;
        result[0].Logprob, tlp.Logprob);
    }
        break;
    }
    }
        if !found {
        t.Errorf("Selected token %q not found in top logprobs", selectedText);
    }
        });
    }
    }

    public static void TestCalculateLogprobsSoftmaxCorrectness(*testing.T t) {
        var decoder = func(tokenID int) String {
        return String(rune('A' + tokenID));
    }
        var tests = []struct {
        name   String;
        logits []float32;
        }{
        {
        name:   "Small vocabulary",;
        logits: []float32{1.0, 2.0, 3.0},;
        },;
        {
        name:   "Large differences",;
        logits: []float32{10.0, 0.0, -10.0},;
        },;
        {
        name:   "All equal",;
        logits: []float32{5.0, 5.0, 5.0, 5.0, 5.0},;
        },;
        {
        name:   "Very large values",;
        logits: []float32{500.0, 499.0, 498.0},;
        },;
        {
        name:   "Very small values",;
        logits: []float32{-500.0, -499.0, -498.0},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var totalProb double;
        var for i = range tt.logits {
        var result = CalculateLogprobs(tt.logits, i, 0, decoder);
        if len(result) != 1 {
        t.Fatalf("Expected 1 result, got %d", len(result));
    }
        var prob = math.Exp(result[0].Logprob);
        totalProb += prob;
        if prob < 0 || prob > 1 {
        t.Errorf("Token %d probability %f is out of range [0, 1]", i, prob);
    }
    }
        if math.Abs(totalProb-1.0) > 1e-5 {
        t.Errorf("Total probability sum is %f, expected 1.0", totalProb);
    }
        });
    }
    }

    public static void TestCalculateLogprobsSelectedTokenCorrectness(*testing.T t) {
        var decoder = func(tokenID int) String {
        return String(rune('A' + tokenID));
    }
        var logits = []float32{3.0, 1.0, 2.0, 0.5}
        var maxLogitIndex = 0;
        var maxLogitValue = logits[0];
        var for i, logit = range logits[1:] {
        if logit > maxLogitValue {
        maxLogitValue = logit;
        maxLogitIndex = i + 1;
    }
    }
        var maxProb double;
        var maxProbIndex int;
        var for i = range logits {
        var result = CalculateLogprobs(logits, i, 0, decoder);
        var prob = math.Exp(result[0].Logprob);
        if prob > maxProb {
        maxProb = prob;
        maxProbIndex = i;
    }
        var expectedToken = decoder(i);
        if result[0].Token != expectedToken {
        t.Errorf("Token %d: expected token %q, got %q", i, expectedToken, result[0].Token);
    }
    }
        if maxProbIndex != maxLogitIndex {
        t.Errorf("Token with highest probability (%d) doesn't match token with highest logit (%d)",;
        maxProbIndex, maxLogitIndex);
    }
    }

    public static void TestCalculateLogprobsTopKOrdering(*testing.T t) {
        var tokens = map[int]String{
        0: "first",;
        1: "second",;
        2: "third",;
        3: "fourth",;
        4: "fifth",;
    }
        var decoder = func(tokenID int) String {
        return tokens[tokenID];
    }
        var logits = []float32{2.0, 5.0, 1.0, 4.0, 3.0}
        var expectedOrder = []String{"second", "fourth", "fifth", "first", "third"}
        var result = CalculateLogprobs(logits, 0, 5, decoder);
        if len(result) != 1 {
        t.Fatalf("Expected 1 result, got %d", len(result));
    }
        if len(result[0].TopLogprobs) != 5 {
        t.Fatalf("Expected 5 top logprobs, got %d", len(result[0].TopLogprobs));
    }
        var for i, tlp = range result[0].TopLogprobs {
        if tlp.Token != expectedOrder[i] {
        t.Errorf("Position %d: expected token %q, got %q", i, expectedOrder[i], tlp.Token);
    }
    }
        var for i = 1; i < len(result[0].TopLogprobs); i++ {
        if result[0].TopLogprobs[i].Logprob > result[0].TopLogprobs[i-1].Logprob {
        t.Errorf("Probabilities not in descending order at position %d: %f > %f",;
        i, result[0].TopLogprobs[i].Logprob, result[0].TopLogprobs[i-1].Logprob);
    }
    }
    }

    public static void TestLogprobsWithStopSequences(*testing.T t) {
        var tests = []struct {
        name              String;
        pendingResponses  []String;
        pendingLogprobs   []llm.Logprob;
        stop              String;
        expectedResponses []String;
        expectedLogprobs  int;
        }{
        {
        name:             "Single token stop",;
        pendingResponses: []String{"Hello", " world", "!"},;
        pendingLogprobs: []llm.Logprob{
        {TokenLogprob: llm.TokenLogprob{Token: "Hello", Logprob: -0.1}},;
        {TokenLogprob: llm.TokenLogprob{Token: " world", Logprob: -0.2}},;
        {TokenLogprob: llm.TokenLogprob{Token: "!", Logprob: -0.3}},;
        },;
        stop:              "!",;
        expectedResponses: []String{"Hello", " world"},;
        expectedLogprobs:  2,;
        },;
        {
        name:             "Multi-token stop sequence",;
        pendingResponses: []String{"Hello", " ", "there", "STOP"},;
        pendingLogprobs: []llm.Logprob{
        {TokenLogprob: llm.TokenLogprob{Token: "Hello", Logprob: -0.1}},;
        {TokenLogprob: llm.TokenLogprob{Token: " ", Logprob: -0.2}},;
        {TokenLogprob: llm.TokenLogprob{Token: "there", Logprob: -0.3}},;
        {TokenLogprob: llm.TokenLogprob{Token: "STOP", Logprob: -0.4}},;
        },;
        stop:              "STOP",;
        expectedResponses: []String{"Hello", " ", "there"},;
        expectedLogprobs:  3,;
        },;
        {
        name:             "Partial token stop",;
        pendingResponses: []String{"Hello", " the", "re!"},;
        pendingLogprobs: []llm.Logprob{
        {TokenLogprob: llm.TokenLogprob{Token: "Hello", Logprob: -0.1}},;
        {TokenLogprob: llm.TokenLogprob{Token: " the", Logprob: -0.2}},;
        {TokenLogprob: llm.TokenLogprob{Token: "re!", Logprob: -0.3}},;
        },;
        stop:              "there!",;
        expectedResponses: []String{"Hello", " "},;
        expectedLogprobs:  2,;
        },;
        {
        name:             "Stop at beginning of last token",;
        pendingResponses: []String{"Hello", " world", "END"},;
        pendingLogprobs: []llm.Logprob{
        {TokenLogprob: llm.TokenLogprob{Token: "Hello", Logprob: -0.1}},;
        {TokenLogprob: llm.TokenLogprob{Token: " world", Logprob: -0.2}},;
        {TokenLogprob: llm.TokenLogprob{Token: "END", Logprob: -0.3}},;
        },;
        stop:              "END",;
        expectedResponses: []String{"Hello", " world"},;
        expectedLogprobs:  2,;
        },;
        {
        name:             "Multi-token stop across tokens",;
        pendingResponses: []String{"Text", " ", "with", " ", "stop", " ", "word"},;
        pendingLogprobs: []llm.Logprob{
        {TokenLogprob: llm.TokenLogprob{Token: "Text", Logprob: -0.1}},;
        {TokenLogprob: llm.TokenLogprob{Token: " ", Logprob: -0.2}},;
        {TokenLogprob: llm.TokenLogprob{Token: "with", Logprob: -0.3}},;
        {TokenLogprob: llm.TokenLogprob{Token: " ", Logprob: -0.4}},;
        {TokenLogprob: llm.TokenLogprob{Token: "stop", Logprob: -0.5}},;
        {TokenLogprob: llm.TokenLogprob{Token: " ", Logprob: -0.6}},;
        {TokenLogprob: llm.TokenLogprob{Token: "word", Logprob: -0.7}},;
        },;
        stop:              "stop word",;
        expectedResponses: []String{"Text", " ", "with", " "},;
        expectedLogprobs:  4,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var origLen = len(tt.pendingResponses);
        var responses, tokenTruncated = TruncateStop(tt.pendingResponses, tt.stop);
        var newLen = len(responses);
        var logprobs = make([]llm.Logprob, len(tt.pendingLogprobs));
        copy(logprobs, tt.pendingLogprobs);
        var origLogprobsLen = len(logprobs);
        var numTokensRemoved = origLen - newLen;
        var newLogprobsLen = origLogprobsLen - numTokensRemoved;
        if newLogprobsLen < 0 {
        newLogprobsLen = 0;
    }
        logprobs = logprobs[:newLogprobsLen];
        if len(responses) != len(tt.expectedResponses) {
        t.Errorf("Expected %d responses, got %d", len(tt.expectedResponses), len(responses));
    }
        if len(logprobs) != tt.expectedLogprobs {
        t.Errorf("Expected %d logprobs after truncation, got %d", tt.expectedLogprobs, len(logprobs));
    }
        if len(logprobs) != len(responses) {
        t.Errorf("Logprobs count (%d) doesn't match responses count (%d)", len(logprobs), len(responses));
    }
        var checkLen = len(logprobs);
        if tokenTruncated && checkLen > 0 {
        checkLen-- // Skip checking the last token when it was partially truncated;
    }
        var for i = range checkLen {
        if i < len(responses) && logprobs[i].Token != responses[i] {
        t.Errorf("Logprob[%d] token %q doesn't match response[%d] %q",;
        i, logprobs[i].Token, i, responses[i]);
    }
    }
        });
    }
    }
}
