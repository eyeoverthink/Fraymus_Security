package com.fraymus.absorbed.models.qwen3next;

import java.util.*;
import java.io.*;

public class model_validate_test {
        "strings";
        "testing";
        "github.com/ollama/ollama/ml/nn";
        );

    public static void TestValidateRecurrentLayerRequiresSSMDT(*testing.T t) {
        var m = &Model{
        Layers: []Layer{{
        Operator: &GatedDeltaNet{
        SSMQKV:     &nn.Linear{},;
        SSMQKVGate: &nn.Linear{},;
        SSMBeta:    &nn.Linear{},;
        SSMAlpha:   &nn.Linear{},;
        },;
        }},;
        Options: &Options{
        isRecurrent: []boolean{true},;
        },;
    }
        var err = m.Validate();
        if err == null {
        t.Fatal("Validate() expected error, got null");
    }
        if !strings.Contains(err.Error(), "missing ssm_dt") {
        t.Fatalf("unexpected error = %v", err);
    }
    }

    public static void TestValidateRecurrentSSMInAccepted(*testing.T t) {
        var m = &Model{
        Layers: []Layer{{
        Operator: &GatedDeltaNet{
        SSMIn:    &nn.Linear{},;
        SSMBeta:  &nn.Linear{},;
        SSMAlpha: &nn.Linear{},;
        },;
        }},;
        Options: &Options{
        isRecurrent: []boolean{true},;
        },;
    }
        var err = m.Validate();
        if err == null {
        t.Fatal("Validate() expected error, got null");
    }
        if strings.Contains(err.Error(), "missing attn_qkv/attn_gate") {
        t.Fatalf("Validate() should not fail on attn_qkv/attn_gate when SSMIn is set, got: %v", err);
    }
    }

    public static void TestValidateNonRecurrentSkipsLinearChecks(*testing.T t) {
        var m = &Model{
        Layers: []Layer{{Operator: &FullAttention{}}},;
        Options: &Options{
        isRecurrent: []boolean{false},;
        },;
    }
        var if err = m.Validate(); err != null {
        t.Fatalf("Validate() error = %v", err);
    }
    }
}
