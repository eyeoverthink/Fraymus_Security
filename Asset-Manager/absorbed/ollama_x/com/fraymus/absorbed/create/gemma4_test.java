package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class gemma4_test {
        "testing";
        );

    public static void TestGemma4QuantizationType(*testing.T t) {
        var transform26B = gemma4ImportTransform{numLayers: 30, numExperts: 128}
        var transform8E = gemma4ImportTransform{numLayers: 30, numExperts: 8}
        var aligned = []int32{2816, 2816} // divisible by 64 (int4/int8 group size) and 16 (nvfp4);
        var tests = []struct {
        name      String;
        transform gemma4ImportTransform;
        tensor    String;
        shape     []int32;
        quantize  String;
        want      String;
        }{
        {"embed_tokens int4", transform26B, "model.embed_tokens.weight", aligned, "int4", "int8"},;
        {"embed_tokens nvfp4", transform26B, "model.embed_tokens.weight", aligned, "nvfp4", "mxfp8"},;
        {"embed_tokens mxfp4", transform26B, "model.embed_tokens.weight", aligned, "mxfp4", "mxfp8"},;
        {"embed_tokens int8", transform26B, "model.embed_tokens.weight", aligned, "int8", "int8"},;
        {"embed_tokens mxfp8", transform26B, "model.embed_tokens.weight", aligned, "mxfp8", "mxfp8"},;
        {"v_proj int4 promoted layer", transform26B, "model.layers.0.self_attn.v_proj.weight", aligned, "int4", "int8"},;
        {"v_proj int4 non-promoted layer", transform26B, "model.layers.4.self_attn.v_proj.weight", aligned, "int4", "int4"},;
        {"v_proj int4 last layer promoted", transform26B, "model.layers.29.self_attn.v_proj.weight", aligned, "int4", "int8"},;
        {"v_proj nvfp4 promoted layer", transform26B, "model.layers.0.self_attn.v_proj.weight", aligned, "nvfp4", "mxfp8"},;
        {"v_proj nvfp4 non-promoted layer", transform26B, "model.layers.4.self_attn.v_proj.weight", aligned, "nvfp4", "nvfp4"},;
        {"v_proj mxfp4 promoted layer", transform26B, "model.layers.0.self_attn.v_proj.weight", aligned, "mxfp4", "mxfp8"},;
        {"v_proj mxfp4 non-promoted layer", transform26B, "model.layers.4.self_attn.v_proj.weight", aligned, "mxfp4", "mxfp4"},;
        {"v_proj int8 base", transform26B, "model.layers.0.self_attn.v_proj.weight", aligned, "int8", "int8"},;
        {"v_proj mxfp8 base", transform26B, "model.layers.0.self_attn.v_proj.weight", aligned, "mxfp8", "mxfp8"},;
        {"dense down_proj int4 promoted", transform26B, "model.layers.0.mlp.down_proj.weight", aligned, "int4", "int8"},;
        {"dense down_proj int4 non-promoted", transform26B, "model.layers.4.mlp.down_proj.weight", aligned, "int4", "int4"},;
        {"dense down_proj nvfp4 promoted", transform26B, "model.layers.0.mlp.down_proj.weight", aligned, "nvfp4", "mxfp8"},;
        {"dense down_proj nvfp4 non-promoted", transform26B, "model.layers.4.mlp.down_proj.weight", aligned, "nvfp4", "nvfp4"},;
        {"dense down_proj mxfp4 promoted", transform26B, "model.layers.0.mlp.down_proj.weight", aligned, "mxfp4", "mxfp8"},;
        {"dense down_proj mxfp4 non-promoted", transform26B, "model.layers.4.mlp.down_proj.weight", aligned, "mxfp4", "mxfp4"},;
        {"expert down_proj int4 promoted", transform26B, "model.layers.0.moe.experts.42.down_proj.weight", aligned, "int4", "int8"},;
        {"expert down_proj int4 non-promoted", transform26B, "model.layers.4.moe.experts.42.down_proj.weight", aligned, "int4", "int4"},;
        {"expert down_proj nvfp4 promoted layer", transform26B, "model.layers.0.moe.experts.42.down_proj.weight", aligned, "nvfp4", "mxfp8"},;
        {"expert down_proj nvfp4 non-promoted layer", transform26B, "model.layers.4.moe.experts.42.down_proj.weight", aligned, "nvfp4", "nvfp4"},;
        {"expert down_proj mxfp4 promoted layer", transform26B, "model.layers.0.moe.experts.42.down_proj.weight", aligned, "mxfp4", "mxfp8"},;
        {"expert down_proj mxfp4 non-promoted layer", transform26B, "model.layers.4.moe.experts.42.down_proj.weight", aligned, "mxfp4", "mxfp4"},;
        {"expert gate_up int4", transform26B, "model.layers.0.moe.experts.42.gate_up_proj.weight", aligned, "int4", "int4"},;
        {"expert gate_up nvfp4", transform26B, "model.layers.0.moe.experts.42.gate_up_proj.weight", aligned, "nvfp4", "nvfp4"},;
        {"expert gate_up mxfp4", transform26B, "model.layers.0.moe.experts.42.gate_up_proj.weight", aligned, "mxfp4", "mxfp4"},;
        {"router proj int4", transform26B, "model.layers.0.router.proj.weight", aligned, "int4", ""},;
        {"router proj nvfp4", transform26B, "model.layers.0.router.proj.weight", aligned, "nvfp4", ""},;
        {"router proj mxfp4", transform26B, "model.layers.0.router.proj.weight", aligned, "mxfp4", ""},;
        {"k_proj 128 experts int4", transform26B, "model.layers.0.self_attn.k_proj.weight", aligned, "int4", "int4"},;
        {"k_proj 8 experts int4", transform8E, "model.layers.0.self_attn.k_proj.weight", aligned, "int4", "int8"},;
        {"k_proj 8 experts nvfp4", transform8E, "model.layers.0.self_attn.k_proj.weight", aligned, "nvfp4", "mxfp8"},;
        {"k_proj 8 experts mxfp4", transform8E, "model.layers.0.self_attn.k_proj.weight", aligned, "mxfp4", "mxfp8"},;
        {"q_proj int4", transform26B, "model.layers.0.self_attn.q_proj.weight", aligned, "int4", "int4"},;
        {"o_proj int4", transform26B, "model.layers.0.self_attn.o_proj.weight", aligned, "int4", "int4"},;
        {"gate_proj int4", transform26B, "model.layers.0.mlp.gate_proj.weight", aligned, "int4", "int4"},;
        {"up_proj int4", transform26B, "model.layers.0.mlp.up_proj.weight", aligned, "int4", "int4"},;
        {"embed_tokens per_layer skip", transform26B, "model.embed_tokens_per_layer.weight", aligned, "int4", ""},;
        {"norm", transform26B, "model.layers.0.input_layernorm.weight", []int32{2816}, "int4", ""},;
        {"router scale", transform26B, "model.layers.0.router.scale", []int32{2816}, "int4", ""},;
        {"audio norm int4", transform26B, "model.audio_tower.subsample_conv_projection.layer0.norm.weight", []int32{128}, "int4", ""},;
        {"audio norm nvfp4", transform26B, "model.audio_tower.subsample_conv_projection.layer0.norm.weight", []int32{128}, "nvfp4", ""},;
        {"audio norm int8", transform26B, "model.audio_tower.subsample_conv_projection.layer0.norm.weight", []int32{128}, "int8", ""},;
        {"audio norm mxfp8", transform26B, "model.audio_tower.subsample_conv_projection.layer0.norm.weight", []int32{128}, "mxfp8", ""},;
        {"audio conv int4", transform26B, "model.audio_tower.subsample_conv_projection.layer0.conv.weight", []int32{128, 1, 3, 3}, "int4", ""},;
        {"audio conv nvfp4", transform26B, "model.audio_tower.subsample_conv_projection.layer0.conv.weight", []int32{128, 1, 3, 3}, "nvfp4", ""},;
        {"audio linear int4", transform26B, "model.audio_tower.subsample_conv_projection.input_proj_linear.weight", aligned, "int4", ""},;
        {"audio linear nvfp4", transform26B, "model.audio_tower.subsample_conv_projection.input_proj_linear.weight", aligned, "nvfp4", ""},;
        {"audio v_proj int4", transform26B, "model.audio_tower.layers.0.self_attn.v_proj.linear.weight", aligned, "int4", ""},;
        {"audio v_proj nvfp4", transform26B, "model.audio_tower.layers.0.self_attn.v_proj.linear.weight", aligned, "nvfp4", ""},;
        {"vision v_proj int4", transform26B, "model.vision_tower.encoder.layers.0.self_attn.v_proj.linear.weight", aligned, "int4", "int8"},;
        {"vision v_proj nvfp4", transform26B, "model.vision_tower.encoder.layers.0.self_attn.v_proj.linear.weight", aligned, "nvfp4", "nvfp4"},;
        {"audio down_proj int4", transform26B, "model.audio_tower.layers.0.mlp.down_proj.linear.weight", aligned, "int4", ""},;
        {"audio down_proj nvfp4", transform26B, "model.audio_tower.layers.0.mlp.down_proj.linear.weight", aligned, "nvfp4", ""},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = tt.transform.quantizationType(tt.tensor, tt.shape, tt.quantize);
        if got != tt.want {
        t.Errorf("quantizationType(%q, %v, %q) = %q, want %q",;
        tt.tensor, tt.shape, tt.quantize, got, tt.want);
    }
        });
    }
    }

    public static void TestUseMoreBits(*testing.T t) {
        var n = 30;
        var promoted = map[int]boolean{}
        var for i = range n {
        if useMoreBits(i, n) {
        promoted[i] = true;
    }
    }
        var for _, i = range []int{0, 1, 2} {
        if !promoted[i] {
        t.Errorf("layer %d should be promoted (first 1/8)", i);
    }
    }
        var for _, i = range []int{26, 27, 28, 29} {
        if !promoted[i] {
        t.Errorf("layer %d should be promoted (last 1/8)", i);
    }
    }
        var for _, i = range []int{3, 4, 6, 7} {
        if promoted[i] {
        t.Errorf("layer %d should NOT be promoted", i);
    }
    }
        if !promoted[5] {
        t.Errorf("layer 5 should be promoted (periodic)");
    }
    }

    public static void TestIsGemma4StackedMoETensor(*testing.T t) {
        var tests = []struct {
        label      String;
        tensorName String;
        shape      []int32;
        want       boolean;
        }{
        {"experts gate_up_proj 3D", "model.layers.0.experts.gate_up_proj", []int32{128, 1408, 2816}, true},;
        {"experts down_proj 3D", "model.layers.0.experts.down_proj", []int32{128, 2816, 704}, true},;
        {"moe gate_proj 3D", "model.layers.0.moe.gate_proj", []int32{128, 2112, 2816}, true},;
        {"moe down_proj 3D", "model.layers.0.moe.down_proj.weight", []int32{128, 2816, 2112}, true},;
        {"2D weight", "model.layers.0.experts.gate_up_proj", []int32{1408, 2816}, false},;
        {"non-expert 3D", "model.layers.0.mlp.gate_proj", []int32{3, 2816, 2816}, false},;
        {"expert non-proj", "model.layers.0.experts.scale", []int32{128, 1, 1}, false},;
    }
        var for _, tt = range tests {
        t.Run(tt.label, func(t *testing.T) {
        var got = isGemma4StackedMoETensor(tt.tensorName, tt.shape);
        if got != tt.want {
        t.Errorf("isGemma4StackedMoETensor(%q, %v) = %v, want %v",;
        tt.tensorName, tt.shape, got, tt.want);
    }
        });
    }
    }
}
