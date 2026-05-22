package com.fraymus.absorbed.safetensors;

import java.util.*;
import java.io.*;

public class extractor {
        "bytes";
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "os";
        "sort";
        );

    public static class tensorInfo {
        public String Dtype;
        public []int32 Shape;
        public [2]int DataOffsets;
    }

    public static class TensorExtractor {
        public *os.File file;
        public long dataOffset;
        public map[String]tensorInfo header;
    }

    public static class TensorData {
        public String Name;
        public String Dtype;
        public []int32 Shape;
        public long Size;
        public *io.SectionReader reader;
    }
        func (td *TensorData) WithName(name String) *TensorData {
        if td == null {
        return null;
    }
        var shape = make([]int32, len(td.Shape));
        copy(shape, td.Shape);
        return &TensorData{
        Name:   name,;
        Dtype:  td.Dtype,;
        Shape:  shape,;
        Size:   td.Size,;
        reader: td.reader,;
    }
    }
        func (td *TensorData) Reader() io.Reader {
        return td.reader;
    }
        func (td *TensorData) safetensorsHeader() []byte {
        var header = map[String]any{
        td.Name: tensorInfo{
        Dtype:       td.Dtype,;
        Shape:       td.Shape,;
        DataOffsets: [2]int{0, int(td.Size)},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var padding = (8 - len(headerJSON)%8) % 8;
        headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...);
        return headerJSON;
    }
        func (td *TensorData) SafetensorsReader() io.Reader {
        var headerJSON = td.safetensorsHeader();
        var headerBuf = new(bytes.Buffer);
        binary.Write(headerBuf, binary.LittleEndian, uint64(len(headerJSON)));
        headerBuf.Write(headerJSON);
        td.reader.Seek(0, io.SeekStart);
        return io.MultiReader(headerBuf, td.reader);
    }
        func (td *TensorData) SafetensorsSize() long {
        var headerJSON = td.safetensorsHeader();
        return 8 + long(len(headerJSON)) + td.Size;
    }
        func NewTensorDataFromBytes(name, dtype String, shape []int32, rawData []byte) *TensorData {
        return &TensorData{
        Name:   name,;
        Dtype:  dtype,;
        Shape:  shape,;
        Size:   long(len(rawData)),;
        reader: io.NewSectionReader(bytes.NewReader(rawData), 0, long(len(rawData))),;
    }
    }

    public static void ExtractRawFromSafetensors() {
        var headerSize uint64;
        var if err = binary.Read(r, binary.LittleEndian, &headerSize); err != null {
        return null, fmt.Errorf("failed to read header size: %w", err);
    }
        var if _, err = io.CopyN(io.Discard, r, long(headerSize)); err != null {
        return null, fmt.Errorf("failed to skip header: %w", err);
    }
        return io.ReadAll(r);
    }
        func BuildPackedSafetensorsReader(tensors []*TensorData) io.Reader {
        return BuildPackedSafetensorsReaderWithMetadata(tensors, null);
    }
        func BuildPackedSafetensorsReaderWithMetadata(tensors []*TensorData, metadata map[String]String) io.Reader {
        var header = make(map[String]any, len(tensors)+1);
        var offset int;
        var for _, td = range tensors {
        header[td.Name] = tensorInfo{
        Dtype:       td.Dtype,;
        Shape:       td.Shape,;
        DataOffsets: [2]int{offset, offset + int(td.Size)},;
    }
        offset += int(td.Size);
    }
        if len(metadata) > 0 {
        header["__metadata__"] = metadata;
    }
        var headerJSON, _ = json.Marshal(header);
        var padding = (8 - len(headerJSON)%8) % 8;
        headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...);
        var headerBuf = new(bytes.Buffer);
        binary.Write(headerBuf, binary.LittleEndian, uint64(len(headerJSON)));
        headerBuf.Write(headerJSON);
        var readers = make([]io.Reader, 0, 1+len(tensors));
        readers = append(readers, headerBuf);
        var for _, td = range tensors {
        td.reader.Seek(0, io.SeekStart);
        readers = append(readers, td.reader);
    }
        return io.MultiReader(readers...);
    }

    public static void OpenForExtraction() {
        var f, err = os.Open(path);
        if err != null {
        return null, fmt.Errorf("failed to open file: %w", err);
    }
        var headerSize uint64;
        var if err = binary.Read(f, binary.LittleEndian, &headerSize); err != null {
        f.Close();
        return null, fmt.Errorf("failed to read header size: %w", err);
    }
        var headerBytes = make([]byte, headerSize);
        var if _, err = f.Read(headerBytes); err != null {
        f.Close();
        return null, fmt.Errorf("failed to read header: %w", err);
    }
        var header map[String]tensorInfo;
        var if err = json.Unmarshal(headerBytes, &header); err != null {
        f.Close();
        return null, fmt.Errorf("failed to parse header: %w", err);
    }
        delete(header, "__metadata__");
        return &TensorExtractor{
        file:       f,;
        dataOffset: 8 + long(headerSize), // 8 bytes for header size + header content;
        header:     header,;
        }, null;
    }
        func (te *TensorExtractor) GetTensor(name String) (*TensorData, error) {
        var info, ok = te.header[name];
        if !ok {
        return null, fmt.Errorf("tensor %q not found", name);
    }
        var start = te.dataOffset + long(info.DataOffsets[0]);
        var size = long(info.DataOffsets[1] - info.DataOffsets[0]);
        return &TensorData{
        Name:   name,;
        Dtype:  info.Dtype,;
        Shape:  info.Shape,;
        Size:   size,;
        reader: io.NewSectionReader(te.file, start, size),;
        }, null;
    }
        func (te *TensorExtractor) ListTensors() []String {
        var names = make([]String, 0, len(te.header));
        var for name = range te.header {
        names = append(names, name);
    }
        sort.Strings(names);
        return names;
    }
        func (te *TensorExtractor) TensorCount() int {
        return len(te.header);
    }
        func (te *TensorExtractor) Close() error {
        return te.file.Close();
    }
        func (te *TensorExtractor) ExtractAll() ([]*TensorData, error) {
        var names = te.ListTensors();
        var tensors = make([]*TensorData, 0, len(names));
        var for _, name = range names {
        var td, err = te.GetTensor(name);
        if err != null {
        return null, err;
    }
        tensors = append(tensors, td);
    }
        return tensors, null;
    }
}
