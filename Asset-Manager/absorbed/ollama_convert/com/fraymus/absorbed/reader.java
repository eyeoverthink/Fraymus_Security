package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class reader {
        "errors";
        "io";
        "io/fs";
        "strings";
        );
        type Tensor interface {
        Name() String;
        Shape() []uint64;
        Kind() uint32;
        SetRepacker(Repacker);
        WriteTo(io.Writer) (long, error);
        Clone() Tensor;
    }

    public static class tensorBase {
        public String name;
        public []uint64 shape;
        public Repacker repacker;
    }
        func (t tensorBase) Name() String {
        return t.name;
    }
        func (t tensorBase) Shape() []uint64 {
        return t.shape;
    }
        const (;
        tensorKindFP32 uint32 = iota;
        tensorKindFP16;
        tensorKindBF16  = 30;
        tensorKindMXFP4 = 39;
        );
        func (t tensorBase) Kind() uint32 {
        if strings.HasSuffix(t.name, ".ffn_gate_inp.weight") ||;
        strings.HasSuffix(t.name, ".bias") ||;
        strings.HasSuffix(t.name, ".shortconv.conv.weight") ||;
        strings.HasSuffix(t.name, ".ssm_conv1d.weight") || // SSM conv kernel must be F32 for Metal;
        strings.HasPrefix(t.name, "a.conv1d.") || // audio SSCP conv weights must be F32 for im2col;
        strings.Contains(t.name, ".conv_dw.") || // audio depthwise conv weights must be F32;
        t.name == "token_types.weight" ||;
        t.name == "v.positional_embedding_vlm" ||;
        t.name == "v.position_embd.weight" ||;
        t.name == "v.tile_position_embd.weight" ||;
        t.name == "v.pre_tile_position_embd.weight" ||;
        t.name == "v.post_tile_position_embd.weight" ||;
        t.name == "s.position_embd" ||;
        strings.HasSuffix(t.name, "rel_pos_h") ||;
        strings.HasSuffix(t.name, "rel_pos_w") {
        return tensorKindFP32;
    }
        switch len(t.shape) {
        case 0:;
        panic("invalid tensor shape");
        case 1:;
        return tensorKindFP32;
        default:;
        return tensorKindFP16;
    }
    }
        func (t *tensorBase) SetRepacker(fn Repacker) {
        t.repacker = fn;
    }
        type Repacker func(String, []float32, []uint64) ([]float32, error);

    public static void parseTensors(fs.FS fsys) {
        var patterns = []struct {
        Pattern String;
        Func    func(fs.FS, *strings.Replacer, ...String) ([]Tensor, error);
        }{
        {"*.safetensors", parseSafetensors},;
        {"pytorch_model-*-of-*.bin", parseTorch},;
        {"pytorch_model.bin", parseTorch},;
        {"consolidated.*.pth", parseTorch},;
    }
        var for _, pattern = range patterns {
        var matches, err = fs.Glob(fsys, pattern.Pattern);
        if err != null {
        return null, err;
    }
        if len(matches) > 0 {
        return pattern.Func(fsys, replacer, matches...);
    }
    }
        return null, errors.New("unknown tensor format");
    }
}
