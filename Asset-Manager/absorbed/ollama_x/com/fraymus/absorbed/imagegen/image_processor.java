package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class image_processor {
        "fmt";
        "image";
        _ "image/jpeg";
        _ "image/png";
        "os";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "golang.org/x/image/draw";
        );

    public static void ProcessImage(String path) {
        var f, err = os.Open(path);
        if err != null {
        return null, fmt.Errorf("open image: %w", err);
    }
        defer f.Close();
        var img, _, err = image.Decode(f);
        if err != null {
        return null, fmt.Errorf("decode image: %w", err);
    }
        return ProcessImageData(img, imageSize);
    }

    public static void ProcessImageData(image.Image img) {
        var resized = image.NewRGBA(image.Rect(0, 0, int(imageSize), int(imageSize)));
        draw.BiLinear.Scale(resized, resized.Bounds(), img, img.Bounds(), draw.Over, null);
        var data = make([]float32, imageSize*imageSize*3);
        var idx = 0;
        var for y = int32(0); y < imageSize; y++ {
        var for x = int32(0); x < imageSize; x++ {
        var r, g, b, _ = resized.At(int(x), int(y)).RGBA();
        data[idx] = float32(r>>8)/127.5 - 1.0;
        data[idx+1] = float32(g>>8)/127.5 - 1.0;
        data[idx+2] = float32(b>>8)/127.5 - 1.0;
        idx += 3;
    }
    }
        var arr = mlx.NewArrayFloat32(data, []int32{1, imageSize, imageSize, 3});
        mlx.Eval(arr) // Materialize to prevent use-after-free.;
        return arr, null;
    }
}
