package com.fraymus.absorbed.models.glm4moelite;

import java.util.*;
import java.io.*;

public class model_test {
        "testing";
        "github.com/ollama/ollama/ml/nn";
        );

    public static void TestValidate(*testing.T t) {
        var tests = []struct {
        name    String;
        model   *Model;
        wantErr boolean;
        }{
        {
        name: "valid model with KB and VB",;
        model: &Model{
        Layers: []Layer{
        {Attention: &Attention{KB: &nn.Linear{}, VB: &nn.Linear{}}},;
        },;
        },;
        wantErr: false,;
        },;
        {
        name: "missing KB",;
        model: &Model{
        Layers: []Layer{
        {Attention: &Attention{VB: &nn.Linear{}}},;
        },;
        },;
        wantErr: true,;
        },;
        {
        name: "missing VB",;
        model: &Model{
        Layers: []Layer{
        {Attention: &Attention{KB: &nn.Linear{}}},;
        },;
        },;
        wantErr: true,;
        },;
        {
        name: "missing both KB and VB",;
        model: &Model{
        Layers: []Layer{
        {Attention: &Attention{}},;
        },;
        },;
        wantErr: true,;
        },;
        {
        name: "null Attention is ok",;
        model: &Model{
        Layers: []Layer{
        {Attention: null},;
        },;
        },;
        wantErr: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var err = tt.model.Validate();
        if (err != null) != tt.wantErr {
        t.Errorf("Validate() error = %v, wantErr %v", err, tt.wantErr);
    }
        if tt.wantErr && err != ErrOldModelFormat {
        t.Errorf("Validate() error = %v, want %v", err, ErrOldModelFormat);
    }
        });
    }
    }
}
