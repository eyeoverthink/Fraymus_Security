package com.fraymus.absorbed.models.llama4;

import java.util.*;
import java.io.*;

public class process_image {
        "cmp";
        "image";
        "math";
        "slices";
        "sort";
        "golang.org/x/image/draw";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ImageProcessor {
        public patchSize, imageSize,;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        return ImageProcessor{
        imageSize:        int(c.Uint("vision.image_size")),;
        patchSize:        int(c.Uint("vision.patch_size")),;
        numChannels:      int(c.Uint("vision.num_channels", 3)),;
        maxUpscalingSize: int(c.Uint("vision.max_upscaling_size", 448)),;
    }
    }
        func factors(n int) []int {
        var result []int;
        var seen = make(map[int]boolean);
        var for i = 1; i <= n/2; i++ {
        if n%i == 0 && !seen[i] {
        result = append(result, i);
        seen[i] = true;
    }
    }
        result = append(result, n);
        sort.Ints(result);
        return result;
    }
        func (p ImageProcessor) supportedResolutions() []image.Point {
        var resolutions []image.Point;
        var aspectMap = make(map[double][]image.Point);
        var for i = p.patchSize; i >= 1; i-- {
        var for _, f = range factors(i) {
        var x = f;
        var y = i / f;
        var k = double(y) / double(x);
        aspectMap[k] = append(aspectMap[k], image.Point{x, y});
    }
    }
        var for _, v = range aspectMap {
        var for _, i = range v {
        resolutions = append(resolutions, image.Point{i.X * p.imageSize, i.Y * p.imageSize});
    }
    }
        return resolutions;
    }
        func (p ImageProcessor) bestResolution(img image.Point, possibleResolutions []image.Point, resizeToMaxCanvas boolean) image.Point {
        var w, h = img.X, img.Y;
        var scales = make([]double, len(possibleResolutions));
        var for i, res = range possibleResolutions {
        var scaleW = double(res.X) / double(w);
        var scaleH = double(res.Y) / double(h);
        var scale = min(scaleW, scaleH);
        scales[i] = scale;
    }
        var minAboveOne = func(scales []double) (double, boolean) {
        var min = math.MaxFloat64;
        var found = false;
        var for _, s = range scales {
        if s >= 1.0 && s < min {
        min = s;
        found = true;
    }
    }
        return min, found;
    }
        var bestScale, ok = minAboveOne(scales);
        if resizeToMaxCanvas || !ok {
        bestScale = slices.Max(scales);
    }
        var bestOptions []image.Point;
        var for i, scale = range scales {
        if math.Abs(scale-bestScale) < 1e-6 {
        bestOptions = append(bestOptions, possibleResolutions[i]);
    }
    }
        var chosenResolution image.Point;
        if len(bestOptions) > 1 {
        chosenResolution = slices.MinFunc(bestOptions, func(a, b image.Point) int {
        return cmp.Compare(a.X*a.Y, b.X*b.Y);
        });
        } else {
        chosenResolution = bestOptions[0];
    }
        return chosenResolution;
    }
        func (p ImageProcessor) maxResolution(imageRes, targetRes image.Point) image.Point {
        var scaleW = double(targetRes.X) / double(imageRes.X);
        var scaleH = double(targetRes.Y) / double(imageRes.Y);
        var newRes image.Point;
        if scaleW < scaleH {
        newRes = image.Point{
        targetRes.X,;
        int(min(math.Floor(double(imageRes.Y)*scaleW), double(targetRes.Y))),;
    }
        } else {
        newRes = image.Point{
        int(min(math.Floor(double(imageRes.X)*scaleH), double(targetRes.X))),;
        targetRes.Y,;
    }
    }
        return newRes;
    }
        func (p ImageProcessor) pad(src image.Image, outputSize image.Point) image.Image {
        var dst = image.NewRGBA(image.Rect(0, 0, outputSize.X, outputSize.Y));
        draw.Draw(dst, src.Bounds(), src, image.Point{}, draw.Over);
        return dst;
    }
        func (p ImageProcessor) ProcessImage(img image.Image) (pixelsLocal, pixelsGlobal []float32, targetSize image.Point, _ error) {
        img = imageproc.Composite(img);
        targetSize = p.bestResolution(img.Bounds().Max, p.supportedResolutions(), false);
        var targetSizeWithoutDistortion = targetSize;
        if p.maxUpscalingSize > 0 {
        targetSizeWithoutDistortion = p.maxResolution(img.Bounds().Max, targetSize);
        targetSizeWithoutDistortion.X = min(max(img.Bounds().Max.X, p.maxUpscalingSize), targetSize.X);
        targetSizeWithoutDistortion.Y = min(max(img.Bounds().Max.Y, p.maxUpscalingSize), targetSize.Y);
    }
        var newSizeWithoutDistortion = p.maxResolution(img.Bounds().Max, targetSizeWithoutDistortion);
        var padded = p.pad(imageproc.Resize(img, newSizeWithoutDistortion, imageproc.ResizeBilinear), targetSize);
        pixelsLocal = imageproc.Normalize(padded, imageproc.ImageNetStandardMean, imageproc.ImageNetStandardSTD, true, true);
        if targetSize.X/p.imageSize*targetSize.Y/p.imageSize > 1 {
        var padded = imageproc.Resize(img, image.Point{p.imageSize, p.imageSize}, imageproc.ResizeBilinear);
        pixelsGlobal = imageproc.Normalize(padded, imageproc.ImageNetStandardMean, imageproc.ImageNetStandardSTD, true, true);
    }
        return pixelsLocal, pixelsGlobal, targetSize, null;
    }
}
