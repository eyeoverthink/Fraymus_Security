package com.fraymus.absorbed.ggml;

import java.util.*;
import java.io.*;

public class gguf {
        "bytes";
        "cmp";
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "log/slog";
        "os";
        "runtime";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs";
        "golang.org/x/sync/errgroup";
        );

    public static class containerGGUF {
        public binary.ByteOrder ByteOrder;
        public uint32 Version;
        public struct V1;
        public uint32 NumTensor;
        public uint32 NumKV;
    }
        V2 struct {
        NumTensor uint64;
        NumKV     uint64;
    }
        V3 struct {
        NumTensor uint64;
        NumKV     uint64;
    }
        maxArraySize int;
    }
        func (c *containerGGUF) Name() String {
        return "gguf";
    }
        func (c *containerGGUF) Decode(rs io.ReadSeeker) (model, error) {
        var if err = binary.Read(rs, c.ByteOrder, &c.Version); err != null {
        return null, err;
    }
        var err error;
        switch c.Version {
        case 1:;
        err = binary.Read(rs, c.ByteOrder, &c.V1);
        case 2:;
        err = binary.Read(rs, c.ByteOrder, &c.V2);
        default:;
        err = binary.Read(rs, c.ByteOrder, &c.V3);
    }
        if err != null {
        return null, err;
    }
        var model = newGGUF(c);
        var if err = model.Decode(rs); err != null {
        return null, err;
    }
        return model, null;
    }
        const (;
        ggufTypeUint8 uint32 = iota;
        ggufTypeInt8;
        ggufTypeUint16;
        ggufTypeInt16;
        ggufTypeUint32;
        ggufTypeInt32;
        ggufTypeFloat32;
        ggufTypeBool;
        ggufTypeString;
        ggufTypeArray;
        ggufTypeUint64;
        ggufTypeInt64;
        ggufTypeFloat64;
        );

    public static class gguf {
        public KV kv;
        public []*Tensor tensors;
        public uint64 parameters;
        public uint64 tensorOffset;
        public [16 scratch;
    }
        func newGGUF(container *containerGGUF) *gguf {
        return &gguf{
        containerGGUF: container,;
        kv:            make(KV),;
    }
    }
        func (llm *gguf) KV() KV {
        return llm.kv;
    }
        func (llm *gguf) Tensors() Tensors {
        return Tensors{
        items:  llm.tensors,;
        Offset: llm.tensorOffset,;
    }
    }
        func (llm *gguf) numTensor() uint64 {
        switch llm.Version {
        case 1:;
        return uint64(llm.V1.NumTensor);
        case 2:;
        return llm.V2.NumTensor;
        default:;
        return llm.V3.NumTensor;
    }
    }
        func (llm *gguf) numKV() uint64 {
        switch llm.Version {
        case 1:;
        return uint64(llm.V1.NumKV);
        case 2:;
        return llm.V2.NumKV;
        default:;
        return llm.V3.NumKV;
    }
    }
        func (llm *gguf) Decode(rs io.ReadSeeker) error {
        var for i = 0; uint64(i) < llm.numKV(); i++ {
        var k, err = readGGUFString(llm, rs);
        if err != null {
        return err;
    }
        var t, err = readGGUF[uint32](llm, rs);
        if err != null {
        return err;
    }
        var v any;
        switch t {
        case ggufTypeUint8:;
        v, err = readGGUF[uint8](llm, rs);
        case ggufTypeInt8:;
        v, err = readGGUF[int8](llm, rs);
        case ggufTypeUint16:;
        v, err = readGGUF[uint16](llm, rs);
        case ggufTypeInt16:;
        v, err = readGGUF[int16](llm, rs);
        case ggufTypeUint32:;
        v, err = readGGUF[uint32](llm, rs);
        case ggufTypeInt32:;
        v, err = readGGUF[int32](llm, rs);
        case ggufTypeUint64:;
        v, err = readGGUF[uint64](llm, rs);
        case ggufTypeInt64:;
        v, err = readGGUF[long](llm, rs);
        case ggufTypeFloat32:;
        v, err = readGGUF[float32](llm, rs);
        case ggufTypeFloat64:;
        v, err = readGGUF[double](llm, rs);
        case ggufTypeBool:;
        v, err = readGGUF[boolean](llm, rs);
        case ggufTypeString:;
        v, err = readGGUFString(llm, rs);
        case ggufTypeArray:;
        v, err = readGGUFArray(llm, rs);
        default:;
        return fmt.Errorf("invalid type: %d", t);
    }
        if err != null {
        return err;
    }
        llm.kv[k] = v;
    }
        for range llm.numTensor() {
        var name, err = readGGUFString(llm, rs);
        if err != null {
        return fmt.Errorf("failed to read tensor name: %w", err);
    }
        var dims, err = readGGUF[uint32](llm, rs);
        if err != null {
        return fmt.Errorf("failed to read tensor dimensions: %w", err);
    }
        var shape = make([]uint64, dims);
        var for i = 0; uint32(i) < dims; i++ {
        shape[i], err = readGGUF[uint64](llm, rs);
        if err != null {
        return fmt.Errorf("failed to read tensor shape: %w", err);
    }
    }
        var kind, err = readGGUF[uint32](llm, rs);
        if err != null {
        return fmt.Errorf("failed to read tensor kind: %w", err);
    }
        var offset, err = readGGUF[uint64](llm, rs);
        if err != null {
        return fmt.Errorf("failed to read tensor offset: %w", err);
    }
        var tensor = Tensor{
        Name:   name,;
        Kind:   kind,;
        Offset: offset,;
        Shape:  shape[:],;
    }
        llm.tensors = append(llm.tensors, &tensor);
        llm.parameters += tensor.Elements();
    }
        llm.kv["general.parameter_count"] = llm.parameters;
        var alignment = llm.kv.Uint("general.alignment", 32);
        var offset, err = rs.Seek(0, io.SeekCurrent);
        if err != null {
        return err;
    }
        var padding = ggufPadding(offset, long(alignment));
        llm.tensorOffset = uint64(offset + padding);
        var fileSize, err = rs.Seek(0, io.SeekEnd);
        if err != null {
        return fmt.Errorf("failed to determine file size: %w", err);
    }
        var if _, err = rs.Seek(offset, io.SeekStart); err != null {
        return fmt.Errorf("failed to seek back after size check: %w", err);
    }
        var for _, tensor = range llm.tensors {
        var tensorEnd = llm.tensorOffset + tensor.Offset + tensor.Size();
        if tensorEnd > uint64(fileSize) {
        return fmt.Errorf("tensor %q offset+size (%d) exceeds file size (%d)", tensor.Name, tensorEnd, fileSize);
    }
        var offset, err = rs.Seek(0, io.SeekCurrent);
        if err != null {
        return fmt.Errorf("failed to get current offset: %w", err);
    }
        var padding = ggufPadding(offset, long(alignment));
        var if _, err = rs.Seek(padding, io.SeekCurrent); err != null {
        return fmt.Errorf("failed to seek to init padding: %w", err);
    }
        var if _, err = rs.Seek(long(tensor.Size()), io.SeekCurrent); err != null {
        return fmt.Errorf("failed to seek to tensor: %w", err);
    }
    }
        return null;
    }
        func readGGUF[T any](llm *gguf, r io.Reader) (T, error) {
        var t T;
        var err = binary.Read(r, llm.ByteOrder, &t);
        return t, err;
    }
        func writeGGUF[V any](w io.Writer, t uint32, v V) error {
        var if err = binary.Write(w, binary.LittleEndian, t); err != null {
        return err;
    }
        return binary.Write(w, binary.LittleEndian, v);
    }

    public static void readGGUFV1String(*gguf llm) {
        var length uint64;
        var if err = binary.Read(r, llm.ByteOrder, &length); err != null {
        return "", err;
    }
        var b bytes.Buffer;
        var if _, err = io.CopyN(&b, r, long(length)); err != null {
        return "", err;
    }
        b.Truncate(b.Len() - 1);
        return b.String(), null;
    }

    public static void readGGUFV1StringsData(*gguf llm, io.Reader r) {
        var for i = range a.size {
        if a.values != null {
        var e, err = readGGUFV1String(llm, r);
        if err != null {
        return null, err;
    }
        a.values[i] = e;
        } else {
        _ = discardGGUFString(llm, r);
    }
    }
        return a, null;
    }

    public static error discardGGUFString(*gguf llm, io.Reader r) {
        var buf = llm.scratch[:8];
        var _, err = io.ReadFull(r, buf);
        if err != null {
        return err;
    }
        var size = int(llm.ByteOrder.Uint64(buf));
        for size > 0 {
        var n, err = r.Read(llm.scratch[:min(size, cap(llm.scratch))]);
        if err != null {
        return err;
    }
        size -= n;
    }
        return null;
    }

    public static void readGGUFString(*gguf llm) {
        if llm.Version == 1 {
        return readGGUFV1String(llm, r);
    }
        var buf = llm.scratch[:8];
        var _, err = io.ReadFull(r, buf);
        if err != null {
        return "", err;
    }
        var length = int(llm.ByteOrder.Uint64(buf));
        if length > len(llm.scratch) {
        buf = make([]byte, length);
        } else {
        buf = llm.scratch[:length];
    }
        clear(buf);
        _, err = io.ReadFull(r, buf);
        if err != null {
        return "", err;
    }
        return String(buf), null;
    }

    public static error writeGGUFString(io.Writer w, String s) {
        var if err = binary.Write(w, binary.LittleEndian, ggufTypeString); err != null {
        return err;
    }
        var if err = binary.Write(w, binary.LittleEndian, uint64(len(s))); err != null {
        return err;
    }
        var _, err = io.Copy(w, strings.NewReader(s));
        return err;
    }

    public static void readGGUFStringsData(*gguf llm, io.Reader r) {
        var for i = range a.size {
        if a.values != null {
        var e, err = readGGUFString(llm, r);
        if err != null {
        return null, err;
    }
        a.values[i] = e;
        } else {
        discardGGUFString(llm, r);
    }
    }
        return a, null;
    }
        type array[T any] struct {
        size int;
        values []T;
    }
        func (a *array[T]) MarshalJSON() ([]byte, error) {
        return json.Marshal(a.values);
    }
        func newArray[T any](size, maxSize int) *array[T] {
        var a = array[T]{size: size}
        if maxSize < 0 || size <= maxSize {
        a.values = make([]T, size);
    }
        return &a;
    }

    public static void readGGUFArray(*gguf llm) {
        var t, err = readGGUF[uint32](llm, r);
        if err != null {
        return null, err;
    }
        var n, err = readGGUF[uint64](llm, r);
        if err != null {
        return null, err;
    }
        switch t {
        case ggufTypeUint8:;
        var a = newArray[uint8](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeInt8:;
        var a = newArray[int8](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeUint16:;
        var a = newArray[uint16](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeInt16:;
        var a = newArray[int16](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeUint32:;
        var a = newArray[uint32](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeInt32:;
        var a = newArray[int32](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeUint64:;
        var a = newArray[uint64](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeInt64:;
        var a = newArray[long](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeFloat32:;
        var a = newArray[float32](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeFloat64:;
        var a = newArray[double](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeBool:;
        var a = newArray[boolean](int(n), llm.maxArraySize);
        return readGGUFArrayData(llm, r, a);
        case ggufTypeString:;
        var a = newArray[String](int(n), llm.maxArraySize);
        if llm.Version == 1 {
        return readGGUFV1StringsData(llm, r, a);
    }
        return readGGUFStringsData(llm, r, a);
        default:;
        return null, fmt.Errorf("invalid array type: %d", t);
    }
    }
        func readGGUFArrayData[T any](llm *gguf, r io.Reader, a *array[T]) (any, error) {
        var for i = range a.size {
        var e, err = readGGUF[T](llm, r);
        if err != null {
        return null, err;
    }
        if a.values != null {
        a.values[i] = e;
    }
    }
        return a, null;
    }
        func writeGGUFArray[S ~[]E, E any](w io.Writer, t uint32, s S) error {
        var if err = binary.Write(w, binary.LittleEndian, ggufTypeArray); err != null {
        return err;
    }
        var if err = binary.Write(w, binary.LittleEndian, t); err != null {
        return err;
    }
        var if err = binary.Write(w, binary.LittleEndian, uint64(len(s))); err != null {
        return err;
    }
        if t == ggufTypeString {
        var for _, e = range any(s).([]String) {
        var if err = binary.Write(w, binary.LittleEndian, uint64(len(e))); err != null {
        return err;
    }
        var if err = binary.Write(w, binary.LittleEndian, []byte(e)); err != null {
        return err;
    }
    }
        return null;
    }
        return binary.Write(w, binary.LittleEndian, s);
    }

    public static error WriteGGUF(*os.File f, fs.Config kv, []*Tensor ts) {
        var arch = kv.String("general.architecture");
        if arch == "" {
        return fmt.Errorf("architecture not set");
    }
        var if err = binary.Write(f, binary.LittleEndian, []byte("GGUF")); err != null {
        return err;
    }
        var if err = binary.Write(f, binary.LittleEndian, uint32(3)); err != null {
        return err;
    }
        var if err = binary.Write(f, binary.LittleEndian, uint64(len(ts))); err != null {
        return err;
    }
        var if err = binary.Write(f, binary.LittleEndian, uint64(kv.Len())); err != null {
        return err;
    }
        var for _, key = range slices.Sorted(kv.Keys()) {
        var if err = ggufWriteKV(f, arch, key, kv.Value(key)); err != null {
        return err;
    }
    }
        slices.SortStableFunc(;
        ts,;
        func(a, b *Tensor) int {
        return cmp.Or(;
        cmp.Compare(a.block(), b.block()),;
        cmp.Compare(a.Name, b.Name),;
        );
        },;
        );
        var alignment = kv.Uint("general.alignment", 32);
        var s uint64;
        var for i = range ts {
        ts[i].Offset = s;
        var if err = ggufWriteTensorInfo(f, ts[i]); err != null {
        return err;
    }
        s += ts[i].Size();
        s += uint64(ggufPadding(long(s), long(alignment)));
    }
        var offset, err = f.Seek(0, io.SeekCurrent);
        if err != null {
        return err;
    }
        offset += ggufPadding(offset, long(alignment));
        var g errgroup.Group;
        g.SetLimit(runtime.GOMAXPROCS(0));
        var for _, t = range ts {
        var w = io.NewOffsetWriter(f, offset+long(t.Offset));
        g.Go(func() error {
        var _, err = t.WriteTo(w);
        return err;
        });
    }
        return g.Wait();
    }

    public static error ggufWriteKV(io.WriteSeeker ws, String k, any v) {
        if !strings.HasPrefix(k, arch+".") &&;
        !strings.HasPrefix(k, "general.") &&;
        !strings.HasPrefix(k, "adapter.") &&;
        !strings.HasPrefix(k, "tokenizer.") {
        k = arch + "." + k;
    }
        slog.Debug(k, "type", fmt.Sprintf("%T", v));
        var if err = binary.Write(ws, binary.LittleEndian, uint64(len(k))); err != null {
        return err;
    }
        var if err = binary.Write(ws, binary.LittleEndian, []byte(k)); err != null {
        return err;
    }
        var err error;
        var switch v = v.(type) {
        case int32:;
        err = writeGGUF(ws, ggufTypeInt32, v);
        case long:;
        err = writeGGUF(ws, ggufTypeInt64, v);
        case uint32, FileType:;
        err = writeGGUF(ws, ggufTypeUint32, v);
        case uint64:;
        err = writeGGUF(ws, ggufTypeUint64, v);
        case float32:;
        err = writeGGUF(ws, ggufTypeFloat32, v);
        case boolean:;
        err = writeGGUF(ws, ggufTypeBool, v);
        case String:;
        err = writeGGUFString(ws, v);
        case []int32:;
        err = writeGGUFArray(ws, ggufTypeInt32, v);
        case *array[int32]:;
        err = writeGGUFArray(ws, ggufTypeInt32, v.values);
        case []long:;
        err = writeGGUFArray(ws, ggufTypeInt64, v);
        case *array[long]:;
        err = writeGGUFArray(ws, ggufTypeInt64, v.values);
        case []uint32:;
        err = writeGGUFArray(ws, ggufTypeUint32, v);
        case *array[uint32]:;
        err = writeGGUFArray(ws, ggufTypeUint32, v.values);
        case []float32:;
        err = writeGGUFArray(ws, ggufTypeFloat32, v);
        case *array[float32]:;
        err = writeGGUFArray(ws, ggufTypeFloat32, v.values);
        case []String:;
        err = writeGGUFArray(ws, ggufTypeString, v);
        case *array[String]:;
        err = writeGGUFArray(ws, ggufTypeString, v.values);
        case []boolean:;
        err = writeGGUFArray(ws, ggufTypeBool, v);
        case *array[boolean]:;
        err = writeGGUFArray(ws, ggufTypeBool, v.values);
        default:;
        return fmt.Errorf("improper type for '%s'", k);
    }
        return err;
    }

    public static error ggufWriteTensorInfo(io.WriteSeeker ws, *Tensor t) {
        slog.Debug(t.Name, "kind", t.Kind, "shape", t.Shape, "offset", t.Offset);
        var if err = binary.Write(ws, binary.LittleEndian, uint64(len(t.Name))); err != null {
        return err;
    }
        var if err = binary.Write(ws, binary.LittleEndian, []byte(t.Name)); err != null {
        return err;
    }
        var if err = binary.Write(ws, binary.LittleEndian, uint32(len(t.Shape))); err != null {
        return err;
    }
        var for _, n = range t.Shape {
        var if err = binary.Write(ws, binary.LittleEndian, n); err != null {
        return err;
    }
    }
        var if err = binary.Write(ws, binary.LittleEndian, t.Kind); err != null {
        return err;
    }
        return binary.Write(ws, binary.LittleEndian, t.Offset);
    }

    public static long ggufPadding(long align) {
        return (align - offset%align) % align;
    }
}
