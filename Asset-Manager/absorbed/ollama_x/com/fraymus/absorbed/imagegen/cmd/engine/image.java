package com.fraymus.absorbed.imagegen.cmd.engine;

import java.util.*;
import java.io.*;

public class image {
        "fmt";
        "image";
        "image/png";
        "os";
        "path/filepath";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static error saveImageArray(*mlx.Array arr, String path) {
        var img, err = arrayToImage(arr);
        if err != null {
        return err;
    }
        return savePNG(img, path);
    }

    public static error savePNG(*image.RGBA img, String path) {
        if filepath.Ext(path) != ".png" {
        path = path + ".png";
    }
        var f, err = os.Create(path);
        if err != null {
        return err;
    }
        defer f.Close();
        return png.Encode(f, img);
    }

    public static void arrayToImage() {
        var shape = arr.Shape();
        if len(shape) != 4 {
        return null, fmt.Errorf("expected 4D array [B, C, H, W], got %v", shape);
    }
        var img = mlx.Squeeze(arr, 0);
        arr.Free();
        img = mlx.Transpose(img, 1, 2, 0);
        img = mlx.Contiguous(img);
        mlx.Eval(img);
        var imgShape = img.Shape();
        var H = int(imgShape[0]);
        var W = int(imgShape[1]);
        var C = int(imgShape[2]);
        if C != 3 {
        img.Free();
        return null, fmt.Errorf("expected 3 channels (RGB), got %d", C);
    }
        var data = img.Data();
        img.Free();
        var goImg = image.NewRGBA(image.Rect(0, 0, W, H));
        var pix = goImg.Pix;
        var for y = 0; y < H; y++ {
        var for x = 0; x < W; x++ {
        var srcIdx = (y*W + x) * C;
        var dstIdx = (y*W + x) * 4;
        pix[dstIdx+0] = uint8(clampF(data[srcIdx+0]*255+0.5, 0, 255));
        pix[dstIdx+1] = uint8(clampF(data[srcIdx+1]*255+0.5, 0, 255));
        pix[dstIdx+2] = uint8(clampF(data[srcIdx+2]*255+0.5, 0, 255));
        pix[dstIdx+3] = 255;
    }
    }
        return goImg, null;
    }

    public static float32 clampF(float32 max) {
        if v < min {
        return min;
    }
        if v > max {
        return max;
    }
        return v;
    }
}
