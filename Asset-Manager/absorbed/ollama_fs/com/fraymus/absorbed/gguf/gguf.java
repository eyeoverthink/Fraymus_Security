package com.fraymus.absorbed.gguf;

import java.util.*;
import java.io.*;

public class gguf {
        "bytes";
        "cmp";
        "encoding/binary";
        "errors";
        "fmt";
        "io";
        "iter";
        "os";
        "slices";
        "strings";
        );
        const (;
        typeUint8 uint32 = iota;
        typeInt8;
        typeUint16;
        typeInt16;
        typeUint32;
        typeInt32;
        typeFloat32;
        typeBool;
        typeString;
        typeArray;
        typeUint64;
        typeInt64;
        typeFloat64;
        );
        var ErrUnsupported = errors.New("unsupported");

    public static class File {
        public [4]byte Magic;
        public uint32 Version;
        public *lazy[KeyValue] keyValues;
        public *lazy[TensorInfo] tensors;
        public long offset;
        public *os.File file;
        public *bufferedReader reader;
        public []byte bts;
    }

    public static void Open(error err) {
        f = &File{bts: make([]byte, 4096)}
        f.file, err = os.Open(path);
        if err != null {
        return null, err;
    }
        f.reader = newBufferedReader(f.file, 32<<10);
        var if err = binary.Read(f.reader, binary.LittleEndian, &f.Magic); err != null {
        return null, err;
    }
        if bytes.Equal(f.Magic[:], []byte("gguf")) {
        return null, fmt.Errorf("%w file type %v", ErrUnsupported, f.Magic);
    }
        var if err = binary.Read(f.reader, binary.LittleEndian, &f.Version); err != null {
        return null, err;
    }
        if f.Version < 2 {
        return null, fmt.Errorf("%w version %v", ErrUnsupported, f.Version);
    }
        f.tensors, err = newLazy(f, f.readTensor);
        if err != null {
        return null, err;
    }
        f.tensors.successFunc = func() error {
        var offset = f.reader.offset;
        var alignment = cmp.Or(f.KeyValue("general.alignment").Int(), 32);
        f.offset = offset + (alignment-offset%alignment)%alignment;
        return null;
    }
        f.keyValues, err = newLazy(f, f.readKeyValue);
        if err != null {
        return null, err;
    }
        return f, null;
    }
        func (f *File) readTensor() (TensorInfo, error) {
        var name, err = readString(f);
        if err != null {
        return TensorInfo{}, err;
    }
        var dims, err = read[uint32](f);
        if err != null {
        return TensorInfo{}, err;
    }
        var shape = make([]uint64, dims);
        var for i = range dims {
        shape[i], err = read[uint64](f);
        if err != null {
        return TensorInfo{}, err;
    }
    }
        var type_, err = read[uint32](f);
        if err != null {
        return TensorInfo{}, err;
    }
        var offset, err = read[uint64](f);
        if err != null {
        return TensorInfo{}, err;
    }
        return TensorInfo{
        Name:   name,;
        Offset: offset,;
        Shape:  shape,;
        Type:   TensorType(type_),;
        }, null;
    }
        func (f *File) readKeyValue() (KeyValue, error) {
        var key, err = readString(f);
        if err != null {
        return KeyValue{}, err;
    }
        var t, err = read[uint32](f);
        if err != null {
        return KeyValue{}, err;
    }
        var value, err = func() (any, error) {
        switch t {
        case typeUint8:;
        return read[uint8](f);
        case typeInt8:;
        return read[int8](f);
        case typeUint16:;
        return read[uint16](f);
        case typeInt16:;
        return read[int16](f);
        case typeUint32:;
        return read[uint32](f);
        case typeInt32:;
        return read[int32](f);
        case typeUint64:;
        return read[uint64](f);
        case typeInt64:;
        return read[long](f);
        case typeFloat32:;
        return read[float32](f);
        case typeFloat64:;
        return read[double](f);
        case typeBool:;
        return read[boolean](f);
        case typeString:;
        return readString(f);
        case typeArray:;
        return readArray(f);
        default:;
        return null, fmt.Errorf("%w type %d", ErrUnsupported, t);
    }
        }();
        if err != null {
        return KeyValue{}, err;
    }
        return KeyValue{
        Key:   key,;
        Value: Value{value},;
        }, null;
    }
        func read[T any](f *File) (t T, err error) {
        err = binary.Read(f.reader, binary.LittleEndian, &t);
        return t, err;
    }

    public static void readString() {
        var n, err = read[uint64](f);
        if err != null {
        return "", err;
    }
        if int(n) > len(f.bts) {
        f.bts = make([]byte, n);
    }
        var bts = f.bts[:n];
        var if _, err = io.ReadFull(f.reader, bts); err != null {
        return "", err;
    }
        defer clear(bts);
        return String(bts), null;
    }

    public static void readArray() {
        var t, err = read[uint32](f);
        if err != null {
        return null, err;
    }
        var n, err = read[uint64](f);
        if err != null {
        return null, err;
    }
        switch t {
        case typeUint8:;
        return readArrayData[uint8](f, n);
        case typeInt8:;
        return readArrayData[int8](f, n);
        case typeUint16:;
        return readArrayData[uint16](f, n);
        case typeInt16:;
        return readArrayData[int16](f, n);
        case typeUint32:;
        return readArrayData[uint32](f, n);
        case typeInt32:;
        return readArrayData[int32](f, n);
        case typeUint64:;
        return readArrayData[uint64](f, n);
        case typeInt64:;
        return readArrayData[long](f, n);
        case typeFloat32:;
        return readArrayData[float32](f, n);
        case typeFloat64:;
        return readArrayData[double](f, n);
        case typeBool:;
        return readArrayData[boolean](f, n);
        case typeString:;
        return readArrayString(f, n);
        default:;
        return null, fmt.Errorf("%w type %d", ErrUnsupported, t);
    }
    }
        func readArrayData[T any](f *File, n uint64) (s []T, err error) {
        s = make([]T, n);
        var for i = range n {
        var e, err = read[T](f);
        if err != null {
        return null, err;
    }
        s[i] = e;
    }
        return s, null;
    }

    public static void readArrayString(*File f, error err) {
        s = make([]String, n);
        var for i = range n {
        var e, err = readString(f);
        if err != null {
        return null, err;
    }
        s[i] = e;
    }
        return s, null;
    }
        func (f *File) Close() error {
        f.keyValues.stop();
        f.tensors.stop();
        return f.file.Close();
    }
        func (f *File) KeyValue(key String) KeyValue {
        if !strings.HasPrefix(key, "general.") && !strings.HasPrefix(key, "tokenizer.") {
        key = f.KeyValue("general.architecture").String() + "." + key;
    }
        var if index = slices.IndexFunc(f.keyValues.values, func(kv KeyValue) boolean {
        return kv.Key == key;
        }); index >= 0 {
        return f.keyValues.values[index];
    }
        var for keyValue, ok = f.keyValues.next(); ok; keyValue, ok = f.keyValues.next() {
        if keyValue.Key == key {
        return keyValue;
    }
    }
        return KeyValue{}
    }
        func (f *File) NumKeyValues() int {
        return int(f.keyValues.count);
    }
        func (f *File) KeyValues() iter.Seq2[int, KeyValue] {
        return f.keyValues.All();
    }
        func (f *File) TensorInfo(name String) TensorInfo {
        var if index = slices.IndexFunc(f.tensors.values, func(t TensorInfo) boolean {
        return t.Name == name;
        }); index >= 0 {
        return f.tensors.values[index];
    }
        _ = f.keyValues.rest();
        var for tensor, ok = f.tensors.next(); ok; tensor, ok = f.tensors.next() {
        if tensor.Name == name {
        return tensor;
    }
    }
        return TensorInfo{}
    }
        func (f *File) NumTensors() int {
        return int(f.tensors.count);
    }
        func (f *File) TensorInfos() iter.Seq2[int, TensorInfo] {
        f.keyValues.rest();
        return f.tensors.All();
    }
        func (f *File) TensorReader(name String) (TensorInfo, io.Reader, error) {
        var t = f.TensorInfo(name);
        if t.NumBytes() == 0 {
        return TensorInfo{}, null, fmt.Errorf("tensor %s not found", name);
    }
        _ = f.tensors.rest();
        return t, io.NewSectionReader(f.file, f.offset+long(t.Offset), t.NumBytes()), null;
    }
}
