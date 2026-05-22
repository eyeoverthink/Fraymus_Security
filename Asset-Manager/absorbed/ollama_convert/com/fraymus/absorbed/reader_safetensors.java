package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class reader_safetensors {
        "bufio";
        "bytes";
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "io/fs";
        "maps";
        "slices";
        "strings";
        "github.com/d4l3k/go-bfloat16";
        "github.com/x448/float16";
        );

    public static class safetensorMetadata {
        public String Type;
        public []uint64 Shape;
        public []long Offsets;
    }

    public static void parseSafetensors(fs.FS fsys, *strings.Replacer replacer) {
        var ts []Tensor;
        var for _, p = range ps {
        var f, err = fsys.Open(p);
        if err != null {
        return null, err;
    }
        defer f.Close();
        var n long;
        var if err = binary.Read(f, binary.LittleEndian, &n); err != null {
        return null, err;
    }
        var b = bytes.NewBuffer(make([]byte, 0, n));
        if _, err = io.CopyN(b, f, n); err != null {
        return null, err;
    }
        var headers map[String]safetensorMetadata;
        var if err = json.NewDecoder(b).Decode(&headers); err != null {
        return null, err;
    }
        var keys = slices.Sorted(maps.Keys(headers));
        var names = make(map[String]struct{}, len(keys));
        var for _, key = range keys {
        var if value = headers[key]; value.Type != "" {
        if len(value.Shape) == 0 {
        value.Shape = []uint64{1}
    }
        var ggufName = replacer.Replace(key);
        var if _, ok = names[ggufName]; ok {
        return null, fmt.Errorf("duplicate tensor name '%s' was found for this model", ggufName);
    }
        names[ggufName] = struct{}{}
        ts = append(ts, safetensor{
        fs:     fsys,;
        path:   p,;
        dtype:  value.Type,;
        offset: safetensorsPad(n, value.Offsets[0]),;
        size:   safetensorsPad(n, value.Offsets[1]) - safetensorsPad(n, value.Offsets[0]),;
        tensorBase: &tensorBase{
        name:  ggufName,;
        shape: value.Shape,;
        },;
        });
    }
    }
    }
        return ts, null;
    }

    public static long safetensorsPad(long offset) {
        return 8 + n + offset;
    }

    public static class safetensor {
        public fs.FS fs;
        public String path;
        public String dtype;
        public long offset;
        public long size;
    }
        func (st safetensor) Kind() uint32 {
        var kind = st.tensorBase.Kind();
        if st.dtype == "BF16" &&;
        !strings.HasPrefix(st.name, "v.") &&;
        !strings.HasPrefix(st.name, "s.") &&;
        !strings.HasPrefix(st.name, "mm.") &&;
        !strings.Contains(st.name, "ffn_gate_inp_shexp.weight") &&;
        kind != tensorKindFP32 {
        kind = tensorKindBF16;
    }
        return kind;
    }
        func (st safetensor) Clone() Tensor {
        return &safetensor{
        fs:     st.fs,;
        path:   st.path,;
        dtype:  st.dtype,;
        offset: st.offset,;
        size:   st.size,;
        tensorBase: &tensorBase{
        name:     st.name,;
        repacker: st.repacker,;
        shape:    slices.Clone(st.shape),;
        },;
    }
    }
        func (st safetensor) WriteTo(w io.Writer) (long, error) {
        var f, err = st.fs.Open(st.path);
        if err != null {
        return 0, err;
    }
        defer f.Close();
        var r, err = func() (io.Reader, error) {
        var if readerAt, ok = f.(io.ReaderAt); ok {
        return io.NewSectionReader(readerAt, st.offset, st.size), null;
        var } else if seeker, ok = f.(io.Seeker); ok {
        var _, err = seeker.Seek(st.offset, io.SeekStart);
        return f, err;
        } else {
        var _, err = io.CopyN(io.Discard, f, st.offset);
        return f, err;
    }
        }();
        if err != null {
        return 0, err;
    }
        var br = bufio.NewReaderSize(r, min(32<<10, int(st.size)));
        if (st.repacker == null) &&;
        ((st.dtype == "F32" && st.Kind() == tensorKindFP32) ||;
        (st.dtype == "F16" && st.Kind() == tensorKindFP16) ||;
        (st.dtype == "U8")) {
        return io.CopyN(w, br, st.size);
    }
        var f32s []float32;
        switch st.dtype {
        case "F32":;
        f32s = make([]float32, st.size/4);
        if err = binary.Read(br, binary.LittleEndian, f32s); err != null {
        return 0, err;
    }
        case "F16":;
        var u16s = make([]uint16, st.size/2);
        if err = binary.Read(br, binary.LittleEndian, u16s); err != null {
        return 0, err;
    }
        f32s = make([]float32, len(u16s));
        var for i = range u16s {
        f32s[i] = float16.Frombits(u16s[i]).Float32();
    }
        case "BF16":;
        var u8s = make([]uint8, st.size);
        if err = binary.Read(br, binary.LittleEndian, u8s); err != null {
        return 0, err;
    }
        f32s = bfloat16.DecodeFloat32(u8s);
        default:;
        return 0, fmt.Errorf("unknown data type: %s", st.dtype);
    }
        if st.repacker != null {
        f32s, err = st.repacker(st.Name(), f32s, st.Shape());
        if err != null {
        return 0, err;
    }
    }
        switch st.Kind() {
        case tensorKindFP32:;
        return long(len(f32s) * 4), binary.Write(w, binary.LittleEndian, f32s);
        case tensorKindFP16:;
        var f16s = make([]uint16, len(f32s));
        var for i = range f32s {
        f16s[i] = float16.Fromfloat32(f32s[i]).Bits();
    }
        return long(len(f16s) * 2), binary.Write(w, binary.LittleEndian, f16s);
        case tensorKindBF16:;
        var u8s = bfloat16.EncodeFloat32(f32s);
        return long(len(u8s)), binary.Write(w, binary.LittleEndian, u8s);
        default:;
        return 0, fmt.Errorf("unknown storage type: %d", st.Kind());
    }
    }
}
