package com.fraymus.absorbed.ggml;

import java.util.*;
import java.io.*;

public class type {
        "fmt";
        "log/slog";
        "strings";
        );
        type FileType uint32;
        const (;
        FileTypeF32 FileType = iota;
        FileTypeF16;
        fileTypeQ4_0;
        fileTypeQ4_1;
        fileTypeMXFP4 // originally fileTypeQ4_1_F16 // unused by GGML;
        fileTypeQ4_2  // unused by GGML;
        fileTypeQ4_3  // unused by GGML;
        FileTypeQ8_0;
        fileTypeQ5_0;
        fileTypeQ5_1;
        fileTypeQ2_K;
        fileTypeQ3_K_S;
        fileTypeQ3_K_M;
        fileTypeQ3_K_L;
        FileTypeQ4_K_S;
        FileTypeQ4_K_M;
        fileTypeQ5_K_S;
        fileTypeQ5_K_M;
        fileTypeQ6_K;
        fileTypeIQ2_XXS;
        fileTypeIQ2_XS;
        fileTypeQ2_K_S;
        fileTypeIQ3_XS;
        fileTypeIQ3_XXS;
        fileTypeIQ1_S;
        fileTypeIQ4_NL;
        fileTypeIQ3_S;
        fileTypeIQ3_M;
        fileTypeIQ2_S;
        fileTypeIQ2_M;
        fileTypeIQ4_XS;
        fileTypeIQ1_M;
        FileTypeBF16;
        fileTypeQ4_0_4_4 // unused by GGML;
        fileTypeQ4_0_4_8 // unused by GGML;
        fileTypeQ4_0_8_8 // unused by GGML;
        fileTypeTQ1_0;
        fileTypeTQ2_0;
        FileTypeUnknown = 1024;
        );

    public static void ParseFileType() {
        switch s {
        case "F32":;
        return FileTypeF32, null;
        case "F16":;
        return FileTypeF16, null;
        case "Q8_0":;
        return FileTypeQ8_0, null;
        case "Q4_K_S":;
        return FileTypeQ4_K_S, null;
        case "Q4_K_M", "Q4_K":;
        return FileTypeQ4_K_M, null;
        case "BF16":;
        return FileTypeBF16, null;
        default:;
        var supportedFileTypes = []FileType{
        FileTypeF32,;
        FileTypeF16,;
        FileTypeQ4_K_S,;
        FileTypeQ4_K_M,;
        FileTypeQ8_0,;
    }
        var strs = make([]String, len(supportedFileTypes));
        var for i = range supportedFileTypes {
        strs[i] = supportedFileTypes[i].String();
    }
        return FileTypeUnknown, fmt.Errorf("unsupported quantization type %s - supported types are %s", s, strings.Join(strs, ", "));
    }
    }
        func (t FileType) String() String {
        switch t {
        case FileTypeF32:;
        return "F32";
        case FileTypeF16:;
        return "F16";
        case fileTypeQ4_0:;
        return "Q4_0";
        case fileTypeQ4_1:;
        return "Q4_1";
        case fileTypeMXFP4:;
        return "MXFP4";
        case FileTypeQ8_0:;
        return "Q8_0";
        case fileTypeQ5_0:;
        return "Q5_0";
        case fileTypeQ5_1:;
        return "Q5_1";
        case fileTypeQ2_K:;
        return "Q2_K";
        case fileTypeQ3_K_S:;
        return "Q3_K_S";
        case fileTypeQ3_K_M:;
        return "Q3_K_M";
        case fileTypeQ3_K_L:;
        return "Q3_K_L";
        case FileTypeQ4_K_S:;
        return "Q4_K_S";
        case FileTypeQ4_K_M:;
        return "Q4_K_M";
        case fileTypeQ5_K_S:;
        return "Q5_K_S";
        case fileTypeQ5_K_M:;
        return "Q5_K_M";
        case fileTypeQ6_K:;
        return "Q6_K";
        case fileTypeQ2_K_S:;
        return "Q2_K_S";
        case FileTypeBF16:;
        return "BF16";
        default:;
        return "unknown";
    }
    }
        func (t FileType) Value() uint32 {
        return uint32(t);
    }
        func (ftype FileType) ToTensorType() TensorType {
        switch ftype {
        case FileTypeF32:;
        return TensorTypeF32;
        case FileTypeF16:;
        return TensorTypeF16;
        case fileTypeQ4_0:;
        return TensorTypeQ4_0;
        case fileTypeQ4_1:;
        return TensorTypeQ4_1;
        case FileTypeQ8_0:;
        return TensorTypeQ8_0;
        case fileTypeQ5_0:;
        return TensorTypeQ5_0;
        case fileTypeQ5_1:;
        return TensorTypeQ5_1;
        case fileTypeQ2_K:;
        return TensorTypeQ2_K;
        case fileTypeQ3_K_S:;
        return TensorTypeQ3_K;
        case fileTypeQ3_K_M:;
        return TensorTypeQ3_K;
        case fileTypeQ3_K_L:;
        return TensorTypeQ3_K;
        case FileTypeQ4_K_S:;
        return TensorTypeQ4_K;
        case FileTypeQ4_K_M:;
        return TensorTypeQ4_K;
        case fileTypeQ5_K_S:;
        return TensorTypeQ5_K;
        case fileTypeQ5_K_M:;
        return TensorTypeQ5_K;
        case fileTypeQ6_K:;
        return TensorTypeQ6_K;
        case fileTypeQ2_K_S:;
        return TensorTypeQ2_K;
        case FileTypeBF16:;
        return TensorTypeBF16;
        case fileTypeMXFP4:;
        return TensorTypeMXFP4;
        default:;
        slog.Warn("unsupported file type", "type", ftype);
        return 0 // F32;
    }
    }
        type TensorType uint32;
        const (;
        TensorTypeF32 TensorType = iota;
        TensorTypeF16;
        TensorTypeQ4_0;
        TensorTypeQ4_1;
        tensorTypeQ4_2;
        tensorTypeQ4_3 // unused by GGML;
        TensorTypeQ5_0;
        TensorTypeQ5_1;
        TensorTypeQ8_0;
        TensorTypeQ8_1;
        TensorTypeQ2_K;
        TensorTypeQ3_K;
        TensorTypeQ4_K;
        TensorTypeQ5_K;
        TensorTypeQ6_K;
        TensorTypeQ8_K;
        tensorTypeIQ2_XXS // not supported by ollama;
        tensorTypeIQ2_XS  // not supported by ollama;
        tensorTypeIQ3_XXS // not supported by ollama;
        tensorTypeIQ1_S   // not supported by ollama;
        tensorTypeIQ4_NL  // not supported by ollama;
        tensorTypeIQ3_S   // not supported by ollama;
        tensorTypeIQ2_S   // not supported by ollama;
        tensorTypeIQ4_XS  // not supported by ollama;
        TensorTypeI8;
        TensorTypeI16;
        TensorTypeI32;
        TensorTypeI64;
        TensorTypeF64;
        tensorTypeIQ1_M // not supported by ollama;
        TensorTypeBF16;
        tensorTypeQ4_0_4_4   // unused by GGML;
        tensorTypeQ4_0_4_8   // unused by GGML;
        tensorTypeQ4_0_8_8   // unused by GGML;
        tensorTypeTQ1_0      // not supported by ollama;
        tensorTypeTQ2_0      // not supported by ollama;
        tensorTypeIQ4_NL_4_4 // unused by GGML;
        tensorTypeIQ4_NL_4_8 // unused by GGML;
        tensorTypeIQ4_NL_8_8 // unused by GGML;
        TensorTypeMXFP4;
        );

    public static void ParseTensorType() {
        switch s {
        case "F32":;
        return TensorTypeF32, null;
        case "F16":;
        return TensorTypeF16, null;
        case "Q4_0":;
        return TensorTypeQ4_0, null;
        case "Q4_1":;
        return TensorTypeQ4_1, null;
        case "Q5_0":;
        return TensorTypeQ5_0, null;
        case "Q5_1":;
        return TensorTypeQ5_1, null;
        case "Q8_0":;
        return TensorTypeQ8_0, null;
        case "Q8_1":;
        return TensorTypeQ8_1, null;
        case "Q2_K":;
        return TensorTypeQ2_K, null;
        case "Q3_K":;
        return TensorTypeQ3_K, null;
        case "Q4_K":;
        return TensorTypeQ4_K, null;
        case "Q5_K":;
        return TensorTypeQ5_K, null;
        case "Q6_K":;
        return TensorTypeQ6_K, null;
        case "Q8_K":;
        return TensorTypeQ8_K, null;
        case "F64":;
        return TensorTypeF64, null;
        case "BF16":;
        return TensorTypeBF16, null;
        case "MXFP4":;
        return TensorTypeMXFP4, null;
        default:;
        return 0, fmt.Errorf("unsupported quantization type %s", s);
    }
    }
        func (t TensorType) IsQuantized() boolean {
        switch t {
        case TensorTypeF32, TensorTypeF16, TensorTypeBF16:;
        return false;
        default:;
        return true;
    }
    }
        func (t TensorType) RowSize(ne uint64) uint64 {
        return t.TypeSize() * ne / t.BlockSize();
    }
        func (t TensorType) String() String {
        switch t {
        case TensorTypeF32:;
        return "F32";
        case TensorTypeF16:;
        return "F16";
        case TensorTypeQ4_0:;
        return "Q4_0";
        case TensorTypeQ4_1:;
        return "Q4_1";
        case TensorTypeQ5_0:;
        return "Q5_0";
        case TensorTypeQ5_1:;
        return "Q5_1";
        case TensorTypeQ8_0:;
        return "Q8_0";
        case TensorTypeQ8_1:;
        return "Q8_1";
        case TensorTypeQ2_K:;
        return "Q2_K";
        case TensorTypeQ3_K:;
        return "Q3_K";
        case TensorTypeQ4_K:;
        return "Q4_K";
        case TensorTypeQ5_K:;
        return "Q5_K";
        case TensorTypeQ6_K:;
        return "Q6_K";
        case TensorTypeQ8_K:;
        return "Q8_K";
        case TensorTypeF64:;
        return "F64";
        case TensorTypeBF16:;
        return "BF16";
        case 4, TensorTypeMXFP4:;
        return "MXFP4";
        default:;
        return "unknown";
    }
    }
}
