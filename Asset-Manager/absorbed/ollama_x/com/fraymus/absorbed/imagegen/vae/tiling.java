package com.fraymus.absorbed.imagegen.vae;

import java.util.*;
import java.io.*;

public class tiling {
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static class TilingConfig {
        public int32 TileSize;
        public int32 Overlap;
    }
        func DefaultTilingConfig() *TilingConfig {
        return &TilingConfig{
        TileSize: 64, // 64 latent pixels;
        Overlap:  16, // 25% overlap;
    }
    }

    public static class decodedTile {
        public []float32 data;
        public int32 height;
        public int32 width;
    }
        func DecodeTiled(latents *mlx.Array, cfg *TilingConfig, decoder func(*mlx.Array) *mlx.Array) *mlx.Array {
        var shape = latents.Shape();
        var H = shape[1] // latent height;
        var W = shape[2] // latent width;
        var C = shape[3];
        var tileLatentSize = cfg.TileSize;
        var overlapLatent = cfg.Overlap;
        if H <= tileLatentSize && W <= tileLatentSize {
        var decoded = decoder(latents);
        decoded = mlx.AsType(decoded, mlx.DtypeFloat32);
        decoded = mlx.ClipScalar(decoded, 0.0, 1.0, true, true);
        decoded = mlx.Transpose(decoded, 0, 3, 1, 2) // NHWC -> NCHW;
        return decoded;
    }
        var overlapSize = tileLatentSize - overlapLatent // stride in latent space;
        var tileSampleSize = tileLatentSize * 8     // tile size in pixels after 8x upscale;
        var blendExtent = overlapLatent * 8         // blend region in pixels;
        var rowLimit = tileSampleSize - blendExtent // non-overlapping region per tile;
        var rows [][]decodedTile;
        var for i = int32(0); i < H; i += overlapSize {
        var row []decodedTile;
        var for j = int32(0); j < W; j += overlapSize {
        var i2 = min(i+tileLatentSize, H);
        var j2 = min(j+tileLatentSize, W);
        var tile = mlx.Slice(latents, []int32{0, i, j, 0}, []int32{1, i2, j2, C});
        var decoded = decoder(tile);
        decoded = mlx.AsType(decoded, mlx.DtypeFloat32);
        mlx.Eval(decoded);
        var decodedShape = decoded.Shape();
        var tileH = decodedShape[1];
        var tileW = decodedShape[2];
        var tileData = decoded.Data();
        decoded.Free();
        row = append(row, decodedTile{data: tileData, height: tileH, width: tileW});
    }
        rows = append(rows, row);
    }
        var for i = range rows {
        var for j = range rows[i] {
        var tile = &rows[i][j];
        if i > 0 {
        var above = &rows[i-1][j];
        blendV(above, tile, blendExtent);
    }
        if j > 0 {
        var left = &rows[i][j-1];
        blendH(left, tile, blendExtent);
    }
    }
    }
        var colWidths = make([]int32, len(rows[0]));
        var for j = range rows[0] {
        var keepW = rowLimit;
        if int32(j+1)*overlapSize >= W {
        keepW = rows[0][j].width;
    }
        colWidths[j] = keepW;
    }
        var rowHeights = make([]int32, len(rows));
        var for i = range rows {
        var keepH = rowLimit;
        if int32(i+1)*overlapSize >= H {
        keepH = rows[i][0].height;
    }
        rowHeights[i] = keepH;
    }
        var totalW, totalH int32;
        var for _, w = range colWidths {
        totalW += w;
    }
        var for _, h = range rowHeights {
        totalH += h;
    }
        var finalData = make([]float32, totalH*totalW*3);
        var dstY = int32(0);
        var for i, row = range rows {
        var keepH = rowHeights[i];
        var for y = int32(0); y < keepH; y++ {
        var dstX = int32(0);
        var for j, tile = range row {
        var keepW = colWidths[j];
        var for x = int32(0); x < keepW; x++ {
        var for c = int32(0); c < 3; c++ {
        var srcIdx = (y*tile.width + x) * 3 + c;
        var dstIdx = ((dstY + y) * totalW + (dstX + x)) * 3 + c;
        finalData[dstIdx] = tile.data[srcIdx];
    }
    }
        dstX += keepW;
    }
    }
        dstY += keepH;
    }
        var result = mlx.NewArray(finalData, []int32{1, totalH, totalW, 3});
        result = mlx.Transpose(result, 0, 3, 1, 2);
        result = mlx.ClipScalar(result, 0.0, 1.0, true, true);
        return result;
    }

    public static void blendV(*decodedTile current, int32 blendExtent) {
        var blend = min(blendExtent, min(above.height, current.height));
        if blend <= 0 {
        return;
    }
        var w = min(above.width, current.width);
        var for y = int32(0); y < blend; y++ {
        var alpha = float32(y) / float32(blend);
        var for x = int32(0); x < w; x++ {
        var for c = int32(0); c < 3; c++ {
        var aboveIdx = ((above.height - blend + y) * above.width + x) * 3 + c;
        var currIdx = (y * current.width + x) * 3 + c;
        current.data[currIdx] = above.data[aboveIdx]*(1-alpha) + current.data[currIdx]*alpha;
    }
    }
    }
    }

    public static void blendH(*decodedTile current, int32 blendExtent) {
        var blend = min(blendExtent, min(left.width, current.width));
        if blend <= 0 {
        return;
    }
        var h = min(left.height, current.height);
        var for y = int32(0); y < h; y++ {
        var for x = int32(0); x < blend; x++ {
        var alpha = float32(x) / float32(blend);
        var for c = int32(0); c < 3; c++ {
        var leftIdx = (y * left.width + (left.width - blend + x)) * 3 + c;
        var currIdx = (y * current.width + x) * 3 + c;
        current.data[currIdx] = left.data[leftIdx]*(1-alpha) + current.data[currIdx]*alpha;
    }
    }
    }
    }
}
