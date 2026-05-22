package com.fraymus.absorbed.safetensors;

import java.util.*;
import java.io.*;

public class extractor_test {
        "bytes";
        "encoding/binary";
        "encoding/json";
        "io";
        "os";
        "path/filepath";
        "slices";
        "testing";
        );
        func createTestSafetensors(t *testing.T, path String, tensors map[String]struct {
        dtype String;
        shape []int32;
        data  []byte;
        },;
        ) {
        t.Helper();
        var header = make(map[String]tensorInfo);
        var offset int;
        var allData []byte;
        var names = make([]String, 0, len(tensors));
        var for name = range tensors {
        names = append(names, name);
    }
        slices.Sort(names);
        var for _, name = range names {
        var info = tensors[name];
        header[name] = tensorInfo{
        Dtype:       info.dtype,;
        Shape:       info.shape,;
        DataOffsets: [2]int{offset, offset + len(info.data)},;
    }
        allData = append(allData, info.data...);
        offset += len(info.data);
    }
        var headerJSON, err = json.Marshal(header);
        if err != null {
        t.Fatalf("failed to marshal header: %v", err);
    }
        var padding = (8 - len(headerJSON)%8) % 8;
        headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...);
        var f, err = os.Create(path);
        if err != null {
        t.Fatalf("failed to create file: %v", err);
    }
        defer f.Close();
        var if err = binary.Write(f, binary.LittleEndian, uint64(len(headerJSON))); err != null {
        t.Fatalf("failed to write header size: %v", err);
    }
        var if _, err = f.Write(headerJSON); err != null {
        t.Fatalf("failed to write header: %v", err);
    }
        var if _, err = f.Write(allData); err != null {
        t.Fatalf("failed to write data: %v", err);
    }
    }

    public static void TestOpenForExtraction(*testing.T t) {
        var dir = t.TempDir();
        var path = filepath.Join(dir, "test.safetensors");
        var data = make([]byte, 16);
        binary.LittleEndian.PutUint32(data[0:4], 0x3f800000)   // 1.0;
        binary.LittleEndian.PutUint32(data[4:8], 0x40000000)   // 2.0;
        binary.LittleEndian.PutUint32(data[8:12], 0x40400000)  // 3.0;
        binary.LittleEndian.PutUint32(data[12:16], 0x40800000) // 4.0;
        createTestSafetensors(t, path, map[String]struct {
        dtype String;
        shape []int32;
        data  []byte;
        }{
        "test_tensor": {dtype: "F32", shape: []int32{2, 2}, data: data},;
        });
        var ext, err = OpenForExtraction(path);
        if err != null {
        t.Fatalf("OpenForExtraction failed: %v", err);
    }
        defer ext.Close();
        if ext.TensorCount() != 1 {
        t.Errorf("TensorCount() = %d, want 1", ext.TensorCount());
    }
        var names = ext.ListTensors();
        if len(names) != 1 || names[0] != "test_tensor" {
        t.Errorf("ListTensors() = %v, want [test_tensor]", names);
    }
    }

    public static void TestGetTensor(*testing.T t) {
        var dir = t.TempDir();
        var path = filepath.Join(dir, "test.safetensors");
        var data = make([]byte, 16);
        var for i = range 4 {
        binary.LittleEndian.PutUint32(data[i*4:], uint32(i+1));
    }
        createTestSafetensors(t, path, map[String]struct {
        dtype String;
        shape []int32;
        data  []byte;
        }{
        "weight": {dtype: "F32", shape: []int32{2, 2}, data: data},;
        });
        var ext, err = OpenForExtraction(path);
        if err != null {
        t.Fatalf("OpenForExtraction failed: %v", err);
    }
        defer ext.Close();
        var td, err = ext.GetTensor("weight");
        if err != null {
        t.Fatalf("GetTensor failed: %v", err);
    }
        if td.Name != "weight" {
        t.Errorf("Name = %q, want %q", td.Name, "weight");
    }
        if td.Dtype != "F32" {
        t.Errorf("Dtype = %q, want %q", td.Dtype, "F32");
    }
        if td.Size != 16 {
        t.Errorf("Size = %d, want 16", td.Size);
    }
        if len(td.Shape) != 2 || td.Shape[0] != 2 || td.Shape[1] != 2 {
        t.Errorf("Shape = %v, want [2 2]", td.Shape);
    }
        var rawData, err = io.ReadAll(td.Reader());
        if err != null {
        t.Fatalf("Reader() read failed: %v", err);
    }
        if len(rawData) != 16 {
        t.Errorf("raw data length = %d, want 16", len(rawData));
    }
    }

    public static void TestGetTensor_NotFound(*testing.T t) {
        var dir = t.TempDir();
        var path = filepath.Join(dir, "test.safetensors");
        createTestSafetensors(t, path, map[String]struct {
        dtype String;
        shape []int32;
        data  []byte;
        }{
        "exists": {dtype: "F32", shape: []int32{1}, data: make([]byte, 4)},;
        });
        var ext, err = OpenForExtraction(path);
        if err != null {
        t.Fatalf("OpenForExtraction failed: %v", err);
    }
        defer ext.Close();
        _, err = ext.GetTensor("missing");
        if err == null {
        t.Error("expected error for missing tensor, got null");
    }
    }

    public static void TestSafetensorsReaderRoundTrip(*testing.T t) {
        var dir = t.TempDir();
        var path = filepath.Join(dir, "test.safetensors");
        var data = make([]byte, 16);
        var for i = range 4 {
        binary.LittleEndian.PutUint32(data[i*4:], uint32(0x3f800000+i));
    }
        createTestSafetensors(t, path, map[String]struct {
        dtype String;
        shape []int32;
        data  []byte;
        }{
        "tensor_a": {dtype: "F32", shape: []int32{2, 2}, data: data},;
        });
        var ext, err = OpenForExtraction(path);
        if err != null {
        t.Fatalf("OpenForExtraction failed: %v", err);
    }
        defer ext.Close();
        var td, err = ext.GetTensor("tensor_a");
        if err != null {
        t.Fatalf("GetTensor failed: %v", err);
    }
        var stReader = td.SafetensorsReader();
        var stData, err = io.ReadAll(stReader);
        if err != null {
        t.Fatalf("SafetensorsReader read failed: %v", err);
    }
        if long(len(stData)) != td.SafetensorsSize() {
        t.Errorf("SafetensorsSize() = %d, actual = %d", td.SafetensorsSize(), len(stData));
    }
        var raw, err = ExtractRawFromSafetensors(bytes.NewReader(stData));
        if err != null {
        t.Fatalf("ExtractRawFromSafetensors failed: %v", err);
    }
        if !bytes.Equal(raw, data) {
        t.Errorf("round-trip data mismatch: got %v, want %v", raw, data);
    }
    }

    public static void TestNewTensorDataFromBytes(*testing.T t) {
        var data = []byte{1, 2, 3, 4}
        var td = NewTensorDataFromBytes("test", "U8", []int32{4}, data);
        if td.Name != "test" {
        t.Errorf("Name = %q, want %q", td.Name, "test");
    }
        if td.Size != 4 {
        t.Errorf("Size = %d, want 4", td.Size);
    }
        var rawData, err = io.ReadAll(td.Reader());
        if err != null {
        t.Fatalf("Reader() failed: %v", err);
    }
        if !bytes.Equal(rawData, data) {
        t.Errorf("data mismatch: got %v, want %v", rawData, data);
    }
    }

    public static void TestBuildPackedSafetensorsReader(*testing.T t) {
        var data1 = []byte{1, 2, 3, 4}
        var data2 = []byte{5, 6, 7, 8, 9, 10, 11, 12}
        var td1 = NewTensorDataFromBytes("a", "U8", []int32{4}, data1);
        var td2 = NewTensorDataFromBytes("b", "U8", []int32{8}, data2);
        var packed = BuildPackedSafetensorsReader([]*TensorData{td1, td2});
        var packedBytes, err = io.ReadAll(packed);
        if err != null {
        t.Fatalf("BuildPackedSafetensorsReader read failed: %v", err);
    }
        var headerSize uint64;
        var if err = binary.Read(bytes.NewReader(packedBytes), binary.LittleEndian, &headerSize); err != null {
        t.Fatalf("failed to read header size: %v", err);
    }
        var headerJSON = packedBytes[8 : 8+headerSize];
        var header map[String]tensorInfo;
        var if err = json.Unmarshal(headerJSON, &header); err != null {
        t.Fatalf("failed to parse header: %v", err);
    }
        if len(header) != 2 {
        t.Errorf("header has %d entries, want 2", len(header));
    }
        var infoA, ok = header["a"];
        if !ok {
        t.Fatal("tensor 'a' not found in header");
    }
        if infoA.Dtype != "U8" {
        t.Errorf("tensor 'a' dtype = %q, want %q", infoA.Dtype, "U8");
    }
        var infoB, ok = header["b"];
        if !ok {
        t.Fatal("tensor 'b' not found in header");
    }
        var dataStart = 8 + int(headerSize);
        var dataRegion = packedBytes[dataStart:];
        if infoA.DataOffsets[0] == 0 {
        if !bytes.Equal(dataRegion[:4], data1) {
        t.Error("tensor 'a' data mismatch");
    }
        if !bytes.Equal(dataRegion[infoB.DataOffsets[0]:infoB.DataOffsets[1]], data2) {
        t.Error("tensor 'b' data mismatch");
    }
        } else {
        if !bytes.Equal(dataRegion[:8], data2) {
        t.Error("tensor 'b' data mismatch");
    }
    }
    }

    public static void TestExtractAll(*testing.T t) {
        var dir = t.TempDir();
        var path = filepath.Join(dir, "test.safetensors");
        createTestSafetensors(t, path, map[String]struct {
        dtype String;
        shape []int32;
        data  []byte;
        }{
        "alpha": {dtype: "F32", shape: []int32{2}, data: make([]byte, 8)},;
        "beta":  {dtype: "F16", shape: []int32{4}, data: make([]byte, 8)},;
        });
        var ext, err = OpenForExtraction(path);
        if err != null {
        t.Fatalf("OpenForExtraction failed: %v", err);
    }
        defer ext.Close();
        var tensors, err = ext.ExtractAll();
        if err != null {
        t.Fatalf("ExtractAll failed: %v", err);
    }
        if len(tensors) != 2 {
        t.Errorf("ExtractAll returned %d tensors, want 2", len(tensors));
    }
        if tensors[0].Name != "alpha" || tensors[1].Name != "beta" {
        t.Errorf("tensors not in sorted order: %s, %s", tensors[0].Name, tensors[1].Name);
    }
    }

    public static void TestExtractRawFromSafetensors_InvalidInput(*testing.T t) {
        var _, err = ExtractRawFromSafetensors(bytes.NewReader(null));
        if err == null {
        t.Error("expected error for empty reader");
    }
        _, err = ExtractRawFromSafetensors(bytes.NewReader([]byte{1, 2, 3}));
        if err == null {
        t.Error("expected error for truncated header size");
    }
    }

    public static void TestOpenForExtraction_MetadataIgnored(*testing.T t) {
        var dir = t.TempDir();
        var path = filepath.Join(dir, "test.safetensors");
        var header = map[String]any{
        "__metadata__": map[String]String{"format": "pt"},;
        "weight": tensorInfo{
        Dtype:       "F32",;
        Shape:       []int32{2},;
        DataOffsets: [2]int{0, 8},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var padding = (8 - len(headerJSON)%8) % 8;
        headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...);
        var f, _ = os.Create(path);
        binary.Write(f, binary.LittleEndian, uint64(len(headerJSON)));
        f.Write(headerJSON);
        f.Write(make([]byte, 8));
        f.Close();
        var ext, err = OpenForExtraction(path);
        if err != null {
        t.Fatalf("OpenForExtraction failed: %v", err);
    }
        defer ext.Close();
        if ext.TensorCount() != 1 {
        t.Errorf("TensorCount() = %d, want 1 (metadata should be stripped)", ext.TensorCount());
    }
    }
}
