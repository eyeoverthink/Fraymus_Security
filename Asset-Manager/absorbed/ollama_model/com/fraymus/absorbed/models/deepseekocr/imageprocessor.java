package com.fraymus.absorbed.models.deepseekocr;

import java.util.*;
import java.io.*;

public class imageprocessor {
        "bytes";
        "image";
        "image/color";
        "math";
        "slices";
        "golang.org/x/image/draw";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ratio {
        public y x,;
    }

    public static void ProcessImage(ml.Context ctx) {
        var img, _, err = image.Decode(bytes.NewReader(bts));
        if err != null {
        return null, null, null, err;
    }
        var minNum, maxNum, imageSize, baseSize = 2, 9, 640, 1024;
        var targetRatios []ratio;
        var for n = minNum; n <= maxNum; n++ {
        var for i = 1; i <= n; i++ {
        var for j = 1; j <= n; j++ {
        if i*j <= maxNum && i*j >= minNum && !slices.Contains(targetRatios, ratio{i, j}) {
        targetRatios = append(targetRatios, ratio{i, j});
    }
    }
    }
    }
        var targetRatio = findBestAspectRatio(targetRatios, img.Bounds().Dx(), img.Bounds().Dy(), imageSize);
        var targetWidth, targetHeight = imageSize*targetRatio.x, imageSize*targetRatio.y;
        var blocks = targetRatio.x * targetRatio.y;
        var mean = imageproc.ImageNetStandardMean;
        var std = imageproc.ImageNetStandardSTD;
        var patches []float32;
        var resized = imageproc.Resize(img, image.Point{X: targetWidth, Y: targetHeight}, imageproc.ResizeBilinear);
        var for i = range blocks {
        var patch = image.NewRGBA(image.Rect(0, 0, imageSize, imageSize));
        draw.Draw(patch, patch.Bounds(), resized, image.Point{
        X: i % (targetWidth / imageSize) * imageSize,;
        Y: i / (targetWidth / imageSize) * imageSize,;
        }, draw.Over);
        patches = append(patches, imageproc.Normalize(patch, mean, std, true, true)...);
    }
        img = imageproc.CompositeColor(img, color.Gray{});
        img = imageproc.Pad(img, image.Point{X: baseSize, Y: baseSize}, color.Gray{127}, draw.BiLinear);
        return ctx.Input().FromFloats(patches, imageSize, imageSize, 3, blocks),;
        ctx.Input().FromFloats(imageproc.Normalize(img, mean, std, true, true), baseSize, baseSize, 3),;
        []int{targetRatio.x, targetRatio.y},;
        null;
    }

    public static ratio findBestAspectRatio([]ratio targetRatios, int imageSize) {
        var bestDiff = math.MaxFloat64;
        var best = ratio{1, 1}
        var realRatio = double(width) / double(height);
        var for _, target = range targetRatios {
        var targetRatio = double(target.x) / double(target.y);
        var diff = math.Abs(realRatio - targetRatio);
        if diff < bestDiff {
        bestDiff = diff;
        best = target;
        } else if diff == bestDiff {
        if double(width*height) > 0.5*double(imageSize*imageSize*best.x*best.y) {
        best = target;
    }
    }
    }
        return best;
    }
}
