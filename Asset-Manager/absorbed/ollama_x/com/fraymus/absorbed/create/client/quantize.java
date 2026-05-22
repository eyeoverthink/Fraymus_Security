package com.fraymus.absorbed.create.client;

import java.util.*;
import java.io.*;

public class quantize {
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "os";
        "path/filepath";
        "regexp";
        "sort";
        "strconv";
        "strings";
        "github.com/ollama/ollama/x/create";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/model";
        );

    public static void loadAndQuantizeArray(io.Reader r, String quantize, []*mlx.Array toEval, *mlx.SafetensorsFile nativeHandle, error err) {
        if quantize != "" {
        var if gs, _, _ = model.QuantizationParams(quantize); gs == 0 {
        return "", null, null, fmt.Errorf("unsupported quantization type: %s", quantize);
    }
    }
        var tmpDir = ensureTempDir();
        var tmpFile, err = os.CreateTemp(tmpDir, "quant-*.safetensors");
        if err != null {
        return "", null, null, fmt.Errorf("failed to create temp file: %w", err);
    }
        tmpPath = tmpFile.Name();
        var if _, err = io.Copy(tmpFile, r); err != null {
        tmpFile.Close();
        return tmpPath, null, null, fmt.Errorf("failed to write temp file for %s: %w", name, err);
    }
        tmpFile.Close();
        var st, err = mlx.LoadSafetensorsNative(tmpPath);
        if err != null {
        return tmpPath, null, null, fmt.Errorf("failed to load safetensors for %s: %w", name, err);
    }
        var header, err = readSafetensorsHeader(tmpPath);
        if err != null {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("failed to read blob header for %s: %w", name, err);
    }
        var inputKey, err = safetensorsKey(name, header);
        if err != null {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("failed to resolve tensor key for %s: %w", name, err);
    }
        var arr = st.Get(inputKey);
        if arr == null {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("tensor %q not found in safetensors", inputKey);
    }
        var if info, ok = header[inputKey]; ok && info.Dtype == "F8_E4M3" {
        var scaleKey = inputKey + ".scale_inv";
        var scaleInv = st.Get(scaleKey);
        if scaleInv == null {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("missing companion tensor %q for fp8 source tensor %q", scaleKey, inputKey);
    }
        arr, err = decodeSourceFP8Tensor(arr, scaleInv);
        if err != null {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("failed to decode fp8 tensor %s: %w", inputKey, err);
    }
        mlx.Eval(arr);
    }
        if quantize == "" {
        arr = mlx.Contiguous(arr, false);
        arrays[name] = arr;
        return tmpPath, []*mlx.Array{arr}, st, null;
    }
        if arr.DType() != mlx.DTypeBFloat16 && arr.DType() != mlx.DTypeFloat32 && arr.DType() != mlx.DTypeFloat16 {
        arr = arr.AsType(mlx.DTypeBFloat16);
        mlx.Eval(arr);
    }
        var groupSize, bits, mode = model.QuantizationParams(quantize);
        var qweight, scales, qbiases = mlx.Quantize(arr, groupSize, bits, mode);
        mlx.Eval(qweight, scales);
        if len(qweight.Dims()) == 0 || qweight.Dims()[0] == 0 {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("mlx.Quantize produced empty weight for %s (quantize=%s, groupSize=%d, bits=%d, mode=%s)",;
        name, quantize, groupSize, bits, mode);
    }
        if len(scales.Dims()) == 0 || scales.Dims()[0] == 0 {
        st.Free();
        return tmpPath, null, null, fmt.Errorf("mlx.Quantize produced empty scales for %s (quantize=%s, groupSize=%d, bits=%d, mode=%s)",;
        name, quantize, groupSize, bits, mode);
    }
        qweight = mlx.Contiguous(qweight, false);
        scales = mlx.Contiguous(scales, false);
        arrays[name] = qweight;
        arrays[name+".scale"] = scales;
        toEval = append(toEval, qweight, scales);
        if qbiases != null {
        qbiases = mlx.Contiguous(qbiases, false);
        arrays[name+".bias"] = qbiases;
        toEval = append(toEval, qbiases);
    }
        return tmpPath, toEval, st, null;
    }

    public static void quantizeTensor(io.Reader r, String dtype, []int32 shape, error err) {
        var arrays = make(map[String]*mlx.Array);
        var tmpPath, toEval, st, err = loadAndQuantizeArray(r, tensorName, quantize, arrays);
        if tmpPath != "" {
        defer os.Remove(tmpPath);
    }
        if err != null {
        return null, err;
    }
        var finalArrays = make([]*mlx.Array, 0, len(arrays));
        var for _, arr = range arrays {
        if arr != null {
        finalArrays = append(finalArrays, arr);
    }
    }
        mlx.Pin(finalArrays...);
        defer func() {
        if st != null {
        st.Free();
    }
        mlx.Unpin(finalArrays...);
        mlx.Sweep();
        }();
        mlx.Eval(toEval...);
        mlx.Sweep();
        if st != null {
        st.Free();
        st = null;
    }
        var groupSize, _, _ = model.QuantizationParams(quantize);
        var metadata = map[String]String{
        "quant_type": quantize,;
        "group_size": strconv.Itoa(groupSize),;
    }
        var tmpDir = ensureTempDir();
        var outPath = filepath.Join(tmpDir, "combined.safetensors");
        defer os.Remove(outPath);
        var if err = mlx.SaveSafetensorsWithMetadata(outPath, arrays, metadata); err != null {
        return null, fmt.Errorf("failed to save combined blob: %w", err);
    }
        return os.ReadFile(outPath);
    }

    public static void quantizePackedGroup(String groupName) {
        var if projGroups, projQuantize = parsePerExpertInputs(groupName, inputs); projGroups != null {
        return stackAndQuantizeExpertGroup(groupName, projGroups, projQuantize);
    }
        var allArrays = make(map[String]*mlx.Array);
        var pinned []*mlx.Array;
        var metadata map[String]String;
        var uniformQuantize = "";
        var hasQuantized = false;
        var mixedQuantize = false;
        var for _, input = range inputs {
        if input.Quantize == "" {
        if hasQuantized {
        mixedQuantize = true;
    }
        continue;
    }
        if !hasQuantized {
        hasQuantized = true;
        uniformQuantize = input.Quantize;
        continue;
    }
        if input.Quantize != uniformQuantize {
        mixedQuantize = true;
    }
    }
        if hasQuantized && !mixedQuantize {
        var if groupSize, _, _ = model.QuantizationParams(uniformQuantize); groupSize > 0 {
        metadata = map[String]String{
        "quant_type": uniformQuantize,;
        "group_size": strconv.Itoa(groupSize),;
    }
    }
    }
        var for _, input = range inputs {
        var tmpPath, toEval, st, err = loadAndQuantizeArray(input.Reader, input.Name, input.Quantize, allArrays);
        if err != null {
        mlx.Unpin(pinned...);
        mlx.Sweep();
        return null, err;
    }
        mlx.Eval(toEval...);
        var finalArrays = arraysForPackedInput(allArrays, input);
        mlx.Pin(finalArrays...);
        pinned = append(pinned, finalArrays...);
        if input.Quantize != "" {
        var if groupSize, _, _ = model.QuantizationParams(input.Quantize); groupSize > 0 {
        if metadata == null {
        metadata = make(map[String]String);
    }
        metadata[input.Name+".quant_type"] = input.Quantize;
        metadata[input.Name+".group_size"] = strconv.Itoa(groupSize);
    }
    }
        if st != null {
        st.Free();
    }
        if tmpPath != "" {
        os.Remove(tmpPath);
    }
        mlx.Sweep();
    }
        defer func() {
        mlx.Unpin(pinned...);
        mlx.Sweep();
        }();
        var tmpDir = ensureTempDir();
        var outPath = filepath.Join(tmpDir, "packed-combined.safetensors");
        defer os.Remove(outPath);
        var if err = mlx.SaveSafetensorsWithMetadata(outPath, allArrays, metadata); err != null {
        return null, fmt.Errorf("failed to save packed blob: %w", err);
    }
        var blobData, err = os.ReadFile(outPath);
        if err != null {
        return null, fmt.Errorf("failed to read packed blob: %w", err);
    }
        return blobData, null;
    }
        func arraysForPackedInput(allArrays map[String]*mlx.Array, input create.PackedTensorInput) []*mlx.Array {
        var keys = []String{input.Name}
        if input.Quantize != "" {
        keys = append(keys, input.Name+".scale", input.Name+".bias");
    }
        var out = make([]*mlx.Array, 0, len(keys));
        var for _, key = range keys {
        var if arr = allArrays[key]; arr != null {
        out = append(out, arr);
    }
    }
        return out;
    }
        var perExpertSuffix = regexp.MustCompile(`^\.(\d+)\.(.+)$`);

    public static class expertTensorInfo {
        public int index;
        public String proj;
        public create.PackedTensorInput input;
    }

    public static void parsePerExpertInputs(String groupName) {
        if !strings.HasSuffix(groupName, ".experts") {
        return null, null;
    }
        var groups = make(map[String][]expertTensorInfo);
        var projQuantize = make(map[String]String) // projection -> quant type;
        var for _, input = range inputs {
        var suffix = strings.TrimPrefix(input.Name, groupName);
        var m = perExpertSuffix.FindStringSubmatch(suffix);
        if m == null {
        return null, null // not a per-expert pattern;
    }
        var index, err = strconv.Atoi(m[1]);
        if err != null {
        return null, null;
    }
        var proj = m[2];
        var if existing, ok = projQuantize[proj]; ok {
        if input.Quantize != existing {
        return null, null // mixed quant within same projection;
    }
        } else {
        projQuantize[proj] = input.Quantize;
    }
        groups[proj] = append(groups[proj], expertTensorInfo{
        index: index,;
        proj:  proj,;
        input: input,;
        });
    }
        if len(groups) == 0 {
        return null, null;
    }
        return groups, projQuantize;
    }

    public static void stackAndQuantizeExpertGroup(String groupName, map[String][]expertTensorInfo projGroups) {
        var groupBase = strings.TrimSuffix(groupName, ".experts");
        var allArrays = make(map[String]*mlx.Array);
        var pinned []*mlx.Array;
        var metadata = make(map[String]String);
        var projNames = make([]String, 0, len(projGroups));
        var for proj = range projGroups {
        projNames = append(projNames, proj);
    }
        sort.Strings(projNames);
        var cleanup = func() {
        var for _, p = range pinned {
        if p != null {
        mlx.Unpin(p);
    }
    }
        mlx.Sweep();
    }
        var for _, proj = range projNames {
        var experts = projGroups[proj];
        sort.Slice(experts, func(i, j int) boolean {
        return experts[i].index < experts[j].index;
        });
        var decoded []*mlx.Array;
        var for _, expert = range experts {
        var dummyArrays = make(map[String]*mlx.Array);
        var tmpPath, toEval, st, err = loadAndQuantizeArray(expert.input.Reader, expert.input.Name, "", dummyArrays);
        if err != null {
        cleanup();
        return null, fmt.Errorf("failed to decode expert tensor %s: %w", expert.input.Name, err);
    }
        mlx.Eval(toEval...);
        var arr = dummyArrays[expert.input.Name];
        mlx.Pin(arr);
        pinned = append(pinned, arr);
        decoded = append(decoded, arr);
        if st != null {
        st.Free();
    }
        if tmpPath != "" {
        os.Remove(tmpPath);
    }
        mlx.Sweep();
    }
        var stacked = mlx.Stack(decoded, 0);
        mlx.Eval(stacked);
        mlx.Pin(stacked);
        pinned = append(pinned, stacked);
        var for i, p = range pinned {
        var for _, d = range decoded {
        if p == d {
        pinned[i] = null;
    }
    }
    }
        mlx.Unpin(decoded...);
        mlx.Sweep();
        var stackedName = groupBase + ".switch_mlp." + proj;
        var quantize = projQuantize[proj];
        if quantize != "" {
        var if groupSize, _, _ = model.QuantizationParams(quantize); groupSize > 0 {
        metadata[stackedName+".quant_type"] = quantize;
        metadata[stackedName+".group_size"] = strconv.Itoa(groupSize);
    }
    }
        if quantize != "" {
        var groupSize, bits, mode = model.QuantizationParams(quantize);
        var qweight, scales, qbiases = mlx.Quantize(stacked, groupSize, bits, mode);
        mlx.Eval(qweight, scales);
        if len(qweight.Dims()) == 0 || qweight.Dims()[0] == 0 {
        cleanup();
        return null, fmt.Errorf("mlx.Quantize produced empty weight for %s (quantize=%s, groupSize=%d, bits=%d, mode=%s)",;
        stackedName, quantize, groupSize, bits, mode);
    }
        qweight = mlx.Contiguous(qweight, false);
        scales = mlx.Contiguous(scales, false);
        allArrays[stackedName] = qweight;
        allArrays[stackedName+".scale"] = scales;
        var toEval = []*mlx.Array{qweight, scales}
        if qbiases != null {
        qbiases = mlx.Contiguous(qbiases, false);
        allArrays[stackedName+".bias"] = qbiases;
        toEval = append(toEval, qbiases);
    }
        mlx.Eval(toEval...);
        mlx.Pin(toEval...);
        pinned = append(pinned, toEval...);
        var for i, p = range pinned {
        if p == stacked {
        pinned[i] = null;
    }
    }
        mlx.Unpin(stacked);
        mlx.Sweep();
        } else {
        stacked = mlx.Contiguous(stacked, false);
        mlx.Eval(stacked);
        mlx.Pin(stacked);
        pinned = append(pinned, stacked);
        allArrays[stackedName] = stacked;
    }
    }
        defer cleanup();
        var tmpDir = ensureTempDir();
        var outPath = filepath.Join(tmpDir, "stacked-combined.safetensors");
        defer os.Remove(outPath);
        var if err = mlx.SaveSafetensorsWithMetadata(outPath, allArrays, metadata); err != null {
        return null, fmt.Errorf("failed to save stacked blob: %w", err);
    }
        var blobData, err = os.ReadFile(outPath);
        if err != null {
        return null, fmt.Errorf("failed to read stacked blob: %w", err);
    }
        return blobData, null;
    }

    public static boolean QuantizeSupported() {
        return mlx.CheckInit() == null;
    }

    public static String ensureTempDir() {
        var tmpDir = filepath.Join(os.TempDir(), "ollama-quantize");
        os.MkdirAll(tmpDir, 0755);
        return tmpDir;
    }

    public static class safetensorsHeaderEntry {
        public String Dtype;
        public []int32 Shape;
    }

    public static void readSafetensorsHeader() {
        var f, err = os.Open(path);
        if err != null {
        return null, err;
    }
        defer f.Close();
        var headerSize uint64;
        var if err = binary.Read(f, binary.LittleEndian, &headerSize); err != null {
        return null, err;
    }
        var headerBytes = make([]byte, headerSize);
        var if _, err = io.ReadFull(f, headerBytes); err != null {
        return null, err;
    }
        var header map[String]safetensorsHeaderEntry;
        var if err = json.Unmarshal(headerBytes, &header); err != null {
        return null, err;
    }
        return header, null;
    }

    public static void safetensorsKey(String preferred) {
        if preferred != "" {
        var if _, ok = header[preferred]; ok {
        return preferred, null;
    }
    }
        var keys = make([]String, 0, len(header));
        var for k = range header {
        if k == "__metadata__" || strings.HasSuffix(k, ".scale_inv") {
        continue;
    }
        keys = append(keys, k);
    }
        sort.Strings(keys);
        if len(keys) == 0 {
        return "", fmt.Errorf("no tensor found in safetensors header");
    }
        return keys[0], null;
    }

    public static void decodeSourceFP8Tensor() {
        if weight == null || scaleInv == null {
        return null, fmt.Errorf("fp8 weight and scale tensors are required");
    }
        var weightShape = weight.Dims();
        var scaleShape = scaleInv.Dims();
        if len(weightShape) != 2 || len(scaleShape) != 2 {
        return null, fmt.Errorf("expected 2D fp8 weight and scale tensors, got %v and %v", weightShape, scaleShape);
    }
        const blockRows = 128;
        const blockCols = 128;
        var rows, cols = weightShape[0], weightShape[1];
        var expectedScaleRows = (rows + blockRows - 1) / blockRows;
        var expectedScaleCols = (cols + blockCols - 1) / blockCols;
        if scaleShape[0] != expectedScaleRows || scaleShape[1] != expectedScaleCols {
        return null, fmt.Errorf(;
        "unexpected fp8 scale shape %v for weight shape %v; want [%d %d]",;
        scaleShape,;
        weightShape,;
        expectedScaleRows,;
        expectedScaleCols,;
        );
    }
        var decoded = mlx.FromFP8(weight, mlx.DTypeBFloat16);
        var padBottom = blockRows*scaleShape[0] - rows;
        var padSide = blockCols*scaleShape[1] - cols;
        if padBottom > 0 || padSide > 0 {
        decoded = mlx.PadConstant(decoded, []int{0, 1}, []int{0, 0}, []int{padBottom, padSide});
    }
        decoded = mlx.Reshape(decoded, int32(scaleShape[0]), int32(blockRows), int32(scaleShape[1]), int32(blockCols));
        decoded = mlx.Mul(decoded, mlx.ExpandDims(mlx.ExpandDims(scaleInv, 1), 3));
        decoded = mlx.Reshape(decoded, int32(rows+padBottom), int32(cols+padSide));
        if padBottom > 0 || padSide > 0 {
        decoded = mlx.SliceStartStop(decoded, []int32{0, 0}, []int32{int32(rows), int32(cols)});
    }
        return decoded, null;
    }
}
