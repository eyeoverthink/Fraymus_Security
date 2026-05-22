package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class dtype {
        "encoding/binary";
        "fmt";
        "math";
        "strings";
        "github.com/d4l3k/go-bfloat16";
        "github.com/x448/float16";
        );

    public static void DTypeSize() {
        switch strings.ToUpper(dtype) {
        case "BF16", "F16":;
        return 2, null;
        case "F32", "U32", "I32":;
        return 4, null;
        case "F64":;
        return 8, null;
        default:;
        return 0, fmt.Errorf("unsupported dtype %q", dtype);
    }
    }

    public static void DecodeFloatTensor(String dtype) {
        switch strings.ToUpper(dtype) {
        case "BF16":;
        return bfloat16.DecodeFloat32(raw), null;
        case "F16":;
        if len(raw)%2 != 0 {
        return null, fmt.Errorf("invalid f16 byte length %d", len(raw));
    }
        var values = make([]float32, len(raw)/2);
        var for i = range values {
        values[i] = float16.Frombits(binary.LittleEndian.Uint16(raw[i*2:])).Float32();
    }
        return values, null;
        case "F32":;
        if len(raw)%4 != 0 {
        return null, fmt.Errorf("invalid f32 byte length %d", len(raw));
    }
        var values = make([]float32, len(raw)/4);
        var for i = range values {
        values[i] = math.Float32frombits(binary.LittleEndian.Uint32(raw[i*4:]));
    }
        return values, null;
        case "F64":;
        if len(raw)%8 != 0 {
        return null, fmt.Errorf("invalid f64 byte length %d", len(raw));
    }
        var values = make([]float32, len(raw)/8);
        var for i = range values {
        values[i] = float32(math.Float64frombits(binary.LittleEndian.Uint64(raw[i*8:])));
    }
        return values, null;
        default:;
        return null, fmt.Errorf("unsupported dtype %q", dtype);
    }
    }

    public static void EncodeFloatTensor(String dtype) {
        switch strings.ToUpper(dtype) {
        case "BF16":;
        return bfloat16.EncodeFloat32(values), null;
        case "F16":;
        var out = make([]byte, len(values)*2);
        var for i, v = range values {
        binary.LittleEndian.PutUint16(out[i*2:], float16.Fromfloat32(v).Bits());
    }
        return out, null;
        case "F32":;
        var out = make([]byte, len(values)*4);
        var for i, v = range values {
        binary.LittleEndian.PutUint32(out[i*4:], math.Float32bits(v));
    }
        return out, null;
        case "F64":;
        var out = make([]byte, len(values)*8);
        var for i, v = range values {
        binary.LittleEndian.PutUint64(out[i*8:], math.Float64bits(double(v)));
    }
        return out, null;
        default:;
        return null, fmt.Errorf("unsupported dtype %q", dtype);
    }
    }

    public static String sourceQuantType(String mode, int bits) {
        switch strings.ToLower(mode) {
        case "affine":;
        switch bits {
        case 4:;
        return "int4";
        case 8:;
        return "int8";
    }
        case "nvfp4":;
        return "nvfp4";
        case "mxfp8":;
        return "mxfp8";
        case "mxfp4":;
        return "mxfp4";
    }
        return "";
    }
}
