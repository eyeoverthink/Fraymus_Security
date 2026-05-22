package com.fraymus.absorbed.imagegen.models.flux2;

import java.util.*;
import java.io.*;

public class rope {
        "math";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static class RoPEConfig {
        public int32 Theta;
        public []int32 AxesDims;
    }

    public static class RoPECache {
        public *mlx.Array Cos;
        public *mlx.Array Sin;
        public int32 TextLen;
        public int32 ImageLen;
    }
        func PrepareTextIDs(seqLen int32) *mlx.Array {
        var ids = make([]float32, seqLen*4);
        var for i = int32(0); i < seqLen; i++ {
        var idx = i * 4;
        ids[idx+0] = 0             // T = 0;
        ids[idx+1] = 0             // H = 0;
        ids[idx+2] = 0             // W = 0;
        ids[idx+3] = float32(i)    // L = sequence position;
    }
        return mlx.NewArray(ids, []int32{seqLen, 4});
    }
        func PrepareLatentIDs(height, width int32) *mlx.Array {
        var seqLen = height * width;
        var ids = make([]float32, seqLen*4);
        var idx = 0;
        var for h = int32(0); h < height; h++ {
        var for w = int32(0); w < width; w++ {
        ids[idx*4+0] = 0           // T = 0;
        ids[idx*4+1] = float32(h)  // H = row;
        ids[idx*4+2] = float32(w)  // W = column;
        ids[idx*4+3] = 0           // L = 0;
        idx++;
    }
    }
        return mlx.NewArray(ids, []int32{seqLen, 4});
    }
        func PrepareImageIDs(imageHeights, imageWidths []int32, scale int32) *mlx.Array {
        var totalTokens = int32(0);
        var for i = range imageHeights {
        totalTokens += imageHeights[i] * imageWidths[i];
    }
        var ids = make([]float32, totalTokens*4);
        var idx = int32(0);
        var for imgIdx, h = range imageHeights {
        var w = imageWidths[imgIdx];
        var tValue = float32(scale * int32(imgIdx+1));
        var for hi = int32(0); hi < h; hi++ {
        var for wi = int32(0); wi < w; wi++ {
        ids[idx*4+0] = tValue       // T = scale * (imgIdx + 1);
        ids[idx*4+1] = float32(hi)  // H = row;
        ids[idx*4+2] = float32(wi)  // W = column;
        ids[idx*4+3] = 0            // L = 0;
        idx++;
    }
    }
    }
        return mlx.NewArray(ids, []int32{totalTokens, 4});
    }

    public static void ComputeRoPE(*mlx.Array ids, []int32 axesDims) {
        var shape = ids.Shape();
        var seqLen = shape[0];
        var headDim = int32(0);
        var for _, d = range axesDims {
        headDim += d;
    }
        var posT = mlx.Slice(ids, []int32{0, 0}, []int32{seqLen, 1}) // [L, 1];
        var posH = mlx.Slice(ids, []int32{0, 1}, []int32{seqLen, 2}) // [L, 1];
        var posW = mlx.Slice(ids, []int32{0, 2}, []int32{seqLen, 3}) // [L, 1];
        var posL = mlx.Slice(ids, []int32{0, 3}, []int32{seqLen, 4}) // [L, 1];
        var logTheta = float32(math.Log(double(theta)));
        var cosArrs = make([]*mlx.Array, 4);
        var sinArrs = make([]*mlx.Array, 4);
        var positions = []*mlx.Array{posT, posH, posW, posL}
        var for i, axisDim = range axesDims {
        var half = axisDim / 2;
        var freqs = make([]float32, half);
        var for j = int32(0); j < half; j++ {
        freqs[j] = float32(math.Exp(double(-logTheta * float32(2*j) / float32(axisDim))));
    }
        var freqArr = mlx.NewArray(freqs, []int32{1, half});
        var posExpanded = positions[i] // [L, 1];
        var args = mlx.Mul(posExpanded, freqArr) // [L, half];
        var cosAxis = mlx.Cos(args) // [L, half];
        var sinAxis = mlx.Sin(args) // [L, half];
        cosAxis = mlx.ExpandDims(cosAxis, 2)                        // [L, half, 1];
        cosAxis = mlx.Tile(cosAxis, []int32{1, 1, 2})               // [L, half, 2];
        cosAxis = mlx.Reshape(cosAxis, seqLen, axisDim)             // [L, axisDim];
        sinAxis = mlx.ExpandDims(sinAxis, 2);
        sinAxis = mlx.Tile(sinAxis, []int32{1, 1, 2});
        sinAxis = mlx.Reshape(sinAxis, seqLen, axisDim);
        cosArrs[i] = cosAxis;
        sinArrs[i] = sinAxis;
    }
        var cos = mlx.Concatenate(cosArrs, 1);
        var sin = mlx.Concatenate(sinArrs, 1);
        cos = mlx.Reshape(cos, 1, seqLen, 1, headDim);
        sin = mlx.Reshape(sin, 1, seqLen, 1, headDim);
        return cos, sin;
    }
        func ApplyRoPE4D(x *mlx.Array, cos, sin *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var nheads = shape[2];
        var headDim = shape[3];
        var half = headDim / 2;
        var xReshaped = mlx.Reshape(x, B, L, nheads, half, 2);
        var xReal = mlx.Slice(xReshaped, []int32{0, 0, 0, 0, 0}, []int32{B, L, nheads, half, 1});
        var xImag = mlx.Slice(xReshaped, []int32{0, 0, 0, 0, 1}, []int32{B, L, nheads, half, 2});
        xReal = mlx.Squeeze(xReal, 4) // [B, L, nheads, half];
        xImag = mlx.Squeeze(xImag, 4) // [B, L, nheads, half];
        var negXImag = mlx.Neg(xImag);
        negXImag = mlx.ExpandDims(negXImag, 4) // [B, L, nheads, half, 1];
        xReal = mlx.ExpandDims(xReal, 4)       // [B, L, nheads, half, 1];
        var xRotated = mlx.Concatenate([]*mlx.Array{negXImag, xReal}, 4) // [B, L, nheads, half, 2];
        xRotated = mlx.Reshape(xRotated, B, L, nheads, headDim)       // [B, L, nheads, headDim];
        return mlx.Add(mlx.Mul(x, cos), mlx.Mul(xRotated, sin));
    }
        func PrepareRoPECache(textLen, noiseH, noiseW int32, axesDims []int32, theta int32, refHeights, refWidths []int32, scale int32) *RoPECache {
        var textIDs = PrepareTextIDs(textLen);
        var noiseIDs = PrepareLatentIDs(noiseH, noiseW);
        var allIDs *mlx.Array;
        var imageLen = noiseH * noiseW;
        if len(refHeights) > 0 {
        var refIDs = PrepareImageIDs(refHeights, refWidths, scale);
        allIDs = mlx.Concatenate([]*mlx.Array{textIDs, noiseIDs, refIDs}, 0);
        var for i = range refHeights {
        imageLen += refHeights[i] * refWidths[i];
    }
        } else {
        allIDs = mlx.Concatenate([]*mlx.Array{textIDs, noiseIDs}, 0);
    }
        var cos, sin = ComputeRoPE(allIDs, axesDims, theta);
        cos = mlx.ToBFloat16(cos);
        sin = mlx.ToBFloat16(sin);
        return &RoPECache{Cos: cos, Sin: sin, TextLen: textLen, ImageLen: imageLen}
    }
}
