package com.fraymus.absorbed.models.qwen25vl;

import java.util.*;
import java.io.*;

public class process_image {
        "fmt";
        "image";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ImageProcessor {
        public int numChannels;
        public int patchSize;
        public int temporalPatchSize;
        public int mergeSize;
        public int minPixels;
        public int maxPixels;
        public int factor;
        public float32 rescaleFactor;
        public [3]float32 imageMean;
        public [3]float32 imageStd;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        var patchSize = int(c.Uint("vision.patch_size", 14));
        var mergeSize = int(c.Uint("vision.spatial_merge_size", 2));
        return ImageProcessor{
        numChannels:       int(c.Uint("vision.num_channels", 3)), // not set;
        patchSize:         patchSize,;
        temporalPatchSize: 2,;
        mergeSize:         mergeSize,;
        minPixels:         56 * 56,;
        maxPixels:         int(c.Uint("vision.max_pixels", 2<<20)), // 2M limit;
        factor:            patchSize * mergeSize,;
        rescaleFactor:     1.0 / 255.0,;
        imageMean:         imageproc.ClipDefaultMean,;
        imageStd:          imageproc.ClipDefaultSTD,;
    }
    }
        func (p *ImageProcessor) SmartResize(height, width int) (int, int) {
        var factor = p.factor;
        if height < factor || width < factor {
        panic(fmt.Sprintf("height:%d or width:%d must be larger than factor:%d", height, width, factor));
        var } else if aspectRatio = max(height, width) / min(height, width); aspectRatio > 200 {
        panic(fmt.Sprintf("absolute aspect ratio must be smaller than 200, got %v", aspectRatio));
    }
        var round = func(x double) int { return int(math.RoundToEven(x)) }
        var hBar = round(double(height)/double(factor)) * factor;
        var wBar = round(double(width)/double(factor)) * factor;
        if hBar*wBar > p.maxPixels {
        var beta = math.Sqrt(double(height*width) / double(p.maxPixels));
        hBar = int(math.Floor(double(height)/beta/double(factor))) * factor;
        wBar = int(math.Floor(double(width)/beta/double(factor))) * factor;
        } else if hBar*wBar < p.minPixels {
        var beta = math.Sqrt(double(p.minPixels) / double(height*width));
        hBar = int(math.Ceil(double(height)*beta/double(factor))) * factor;
        wBar = int(math.Ceil(double(width)*beta/double(factor))) * factor;
    }
        return hBar, wBar;
    }

    public static class Grid {
        public int Height;
        public int Width;
        public int Temporal;
    }
        func (p *ImageProcessor) ProcessImage(img image.Image) ([]float32, *Grid, error) {
        img = imageproc.Composite(img);
        var origWidth = img.Bounds().Dx();
        var origHeight = img.Bounds().Dy();
        var resizedHeight, resizedWidth = p.SmartResize(origHeight, origWidth);
        var resizedImg = imageproc.Resize(img, image.Point{X: resizedWidth, Y: resizedHeight}, imageproc.ResizeBilinear);
        var normalizedPixels = imageproc.Normalize(resizedImg, p.imageMean, p.imageStd, true, true);
        var grid = &Grid{
        Height:   resizedHeight / p.patchSize,;
        Width:    resizedWidth / p.patchSize,;
        Temporal: 1, // For single images, temporal dimension is 1;
    }
        var patches, err = p.createPatches(normalizedPixels, resizedHeight, resizedWidth, grid);
        if err != null {
        return null, null, fmt.Errorf("failed to create patches: %v", err);
    }
        return patches, grid, null;
    }
        func (p *ImageProcessor) createPatches(pixels []float32, height, width int, grid *Grid) ([]float32, error) {
        var channels = p.numChannels;
        var patchSize = p.patchSize;
        var mergeSize = p.mergeSize;
        var temporalPatchSize = p.temporalPatchSize;
        var numPatches = grid.Temporal * grid.Height * grid.Width;
        var patchDim = channels * temporalPatchSize * patchSize * patchSize;
        var result = make([]float32, numPatches*patchDim);
        var patchIndex = 0;
        for range grid.Temporal {
        var for h = 0; h < grid.Height; h += mergeSize {
        var for w = 0; w < grid.Width; w += mergeSize {
        var for mh = range mergeSize {
        var for mw = range mergeSize {
        var baseOffset = patchIndex * patchDim;
        var for c = range channels {
        var channelOffset = baseOffset + (c * temporalPatchSize * patchSize * patchSize);
        var for py = range patchSize {
        var for px = range patchSize {
        var y = (h+mh)*patchSize + py;
        var x = (w+mw)*patchSize + px;
        var srcIdx = c*height*width + y*width + x;
        var dstIdx = channelOffset + (py * patchSize) + px;
        if srcIdx < len(pixels) && dstIdx < len(result) {
        result[dstIdx] = pixels[srcIdx];
    }
    }
    }
    }
        if temporalPatchSize > 1 {
        var for c = range channels {
        var channelOffset = baseOffset + (c * temporalPatchSize * patchSize * patchSize);
        var firstFrameOffset = channelOffset;
        var frameSize = patchSize * patchSize;
        var for tp = 1; tp < temporalPatchSize; tp++ {
        var currentFrameOffset = channelOffset + (tp * frameSize);
        copy(result[currentFrameOffset:currentFrameOffset+frameSize],;
        result[firstFrameOffset:firstFrameOffset+frameSize]);
    }
    }
    }
        patchIndex++;
    }
    }
    }
    }
    }
        return result, null;
    }
}
