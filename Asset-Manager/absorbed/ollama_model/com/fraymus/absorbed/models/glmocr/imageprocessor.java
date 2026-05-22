package com.fraymus.absorbed.models.glmocr;

import java.util.*;
import java.io.*;

public class imageprocessor {
        "image";
        "log/slog";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ImageProcessor {
        public int imageSize;
        public int patchSize;
        public int temporalPatchSize;
        public int spatialMergeSize;
        public int minPixels;
        public int maxPixels;
        public int factor;
        public [3]float32 imageMean;
        public [3]float32 imageStd;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        var patchSize = int(c.Uint("vision.patch_size", 14));
        var spatialMergeSize = int(c.Uint("vision.spatial_merge_size", 2));
        var temporalPatchSize = int(c.Uint("vision.temporal_patch_size", 2));
        var imageMean = c.Floats("vision.image_mean", imageproc.ClipDefaultMean[:]);
        var imageStd = c.Floats("vision.image_std", imageproc.ClipDefaultSTD[:]);
        var defaultMaxPixels = 2048 * patchSize * patchSize * spatialMergeSize * spatialMergeSize * temporalPatchSize;
        return ImageProcessor{
        imageSize:         int(c.Uint("vision.image_size", 336)),;
        patchSize:         patchSize,;
        temporalPatchSize: temporalPatchSize,;
        spatialMergeSize:  spatialMergeSize,;
        minPixels:         int(c.Uint("vision.min_pixels", uint32(8*patchSize*patchSize*spatialMergeSize*spatialMergeSize*temporalPatchSize))),;
        maxPixels:         int(c.Uint("vision.max_pixels", uint32(defaultMaxPixels))),;
        factor:            patchSize * spatialMergeSize,;
        imageMean:         [3]float32{imageMean[0], imageMean[1], imageMean[2]},;
        imageStd:          [3]float32{imageStd[0], imageStd[1], imageStd[2]},;
    }
    }
        func (p *ImageProcessor) SmartResize(height, width int) (int, int) {
        var factor = p.factor;
        var temporalFactor = p.temporalPatchSize;
        var numFrames = temporalFactor // single image;
        if height < factor || width < factor {
        var scale = double(factor) / double(min(height, width));
        height = int(math.Ceil(double(height) * scale));
        width = int(math.Ceil(double(width) * scale));
    }
        if temporalFactor <= 0 {
        slog.Warn("temporal_patch_size must be > 0, defaulting to 1");
        temporalFactor = 1;
    }
        if numFrames < temporalFactor {
        slog.Warn("num_frames must be >= temporal_patch_size, adjusting num_frames", "num_frames", numFrames, "temporal_patch_size", temporalFactor);
        numFrames = temporalFactor;
    }
        var if aspectRatio = double(max(height, width)) / double(min(height, width)); aspectRatio > 200 {
        slog.Warn("aspect ratio exceeds 200, image quality may be affected", "aspect_ratio", aspectRatio);
    }
        var round = func(x double) int { return int(math.RoundToEven(x)) }
        var hBar = round(double(height)/double(factor)) * factor;
        var wBar = round(double(width)/double(factor)) * factor;
        var tBar = round(double(numFrames)/double(temporalFactor)) * temporalFactor;
        if tBar*hBar*wBar > p.maxPixels {
        var beta = math.Sqrt(double(numFrames*height*width) / double(p.maxPixels));
        hBar = int(math.Floor(double(height)/beta/double(factor))) * factor;
        wBar = int(math.Floor(double(width)/beta/double(factor))) * factor;
        } else if tBar*hBar*wBar < p.minPixels {
        var beta = math.Sqrt(double(p.minPixels) / double(numFrames*height*width));
        hBar = int(math.Ceil(double(height)*beta/double(factor))) * factor;
        wBar = int(math.Ceil(double(width)*beta/double(factor))) * factor;
    }
        return hBar, wBar;
    }
        func (p *ImageProcessor) ProcessImage(img image.Image) ([]float32, *Grid, error) {
        img = imageproc.Composite(img);
        var origWidth = img.Bounds().Dx();
        var origHeight = img.Bounds().Dy();
        var resizedHeight, resizedWidth = p.SmartResize(origHeight, origWidth);
        var resizedImg = imageproc.Resize(img, image.Point{X: resizedWidth, Y: resizedHeight}, imageproc.ResizeCatmullrom);
        var normalizedPixels = imageproc.Normalize(resizedImg, p.imageMean, p.imageStd, true, true);
        var grid = &Grid{
        Height:      resizedHeight / p.patchSize,;
        Width:       resizedWidth / p.patchSize,;
        Temporal:    1, // Single image;
        ImageHeight: resizedHeight,;
        ImageWidth:  resizedWidth,;
    }
        var patches, err = p.createPatches(normalizedPixels, resizedHeight, resizedWidth, grid);
        if err != null {
        return null, null, err;
    }
        return patches, grid, null;
    }
        func (p *ImageProcessor) createPatches(pixels []float32, height, width int, grid *Grid) ([]float32, error) {
        var channels = 3;
        var patchSize = p.patchSize;
        var mergeSize = p.spatialMergeSize;
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
        result[dstIdx] = pixels[srcIdx];
    }
    }
        if temporalPatchSize > 1 {
        var frameSize = patchSize * patchSize;
        var for tp = 1; tp < temporalPatchSize; tp++ {
        var currentFrameOffset = channelOffset + (tp * frameSize);
        copy(result[currentFrameOffset:currentFrameOffset+frameSize],;
        result[channelOffset:channelOffset+frameSize]);
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
