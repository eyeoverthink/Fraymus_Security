package com.fraymus.absorbed.models.lfm2;

import java.util.*;
import java.io.*;

public class process_image {
        "image";
        stdimage "image/draw";
        "math";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ImageProcessor {
        public patchSize, imageSize,;
        public int downsampleFactor;
        public imageStd imageMean,;
        public boolean doImageSplitting;
        public int minTiles;
        public int maxTiles;
        public boolean useThumbnail;
        public int tileSize;
        public int minImageTokens;
        public int maxImageTokens;
        public double maxPixelsTolerance;
    }

    public static class processedVisionImage {
        public []float32 data;
        public image.Point size;
        public int row;
        public int col;
        public boolean thumbnail;
    }

    public static class processedVisionLayout {
        public int rows;
        public int cols;
        public boolean hasThumbnail;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        var mean = c.Floats("vision.image_mean");
        var std = c.Floats("vision.image_std");
        var processor = ImageProcessor{
        imageSize:          int(c.Uint("vision.image_size", 256)),;
        patchSize:          int(c.Uint("vision.patch_size", 16)),;
        numChannels:        int(c.Uint("vision.num_channels", 3)),;
        downsampleFactor:   int(c.Uint("vision.projector.scale_factor", 2)),;
        imageMean:          [3]float32{0.5, 0.5, 0.5},;
        imageStd:           [3]float32{0.5, 0.5, 0.5},;
        doImageSplitting:   c.Bool("vision.do_image_splitting", true),;
        minTiles:           int(c.Uint("vision.min_tiles", 2)),;
        maxTiles:           int(c.Uint("vision.max_tiles", 10)),;
        useThumbnail:       c.Bool("vision.use_thumbnail", true),;
        tileSize:           int(c.Uint("vision.tile_size", 512)),;
        minImageTokens:     int(c.Uint("vision.min_image_tokens", 64)),;
        maxImageTokens:     int(c.Uint("vision.max_image_tokens", 256)),;
        maxPixelsTolerance: double(c.Float("vision.max_pixels_tolerance", 2.0)),;
    }
        if len(mean) >= 3 {
        processor.imageMean = [3]float32{mean[0], mean[1], mean[2]}
    }
        if len(std) >= 3 {
        processor.imageStd = [3]float32{std[0], std[1], std[2]}
    }
        if processor.downsampleFactor <= 0 {
        processor.downsampleFactor = 2;
    }
        if processor.patchSize <= 0 {
        processor.patchSize = 16;
    }
        if processor.tileSize <= 0 {
        processor.tileSize = 512;
    }
        if processor.minTiles <= 0 {
        processor.minTiles = 2;
    }
        if processor.maxTiles < processor.minTiles {
        processor.maxTiles = processor.minTiles;
    }
        if processor.minImageTokens <= 0 {
        processor.minImageTokens = 64;
    }
        if processor.maxImageTokens < processor.minImageTokens {
        processor.maxImageTokens = processor.minImageTokens;
    }
        if processor.maxPixelsTolerance <= 0 {
        processor.maxPixelsTolerance = 2.0;
    }
        return processor;
    }
        func (p ImageProcessor) ProcessImage(img image.Image) ([]processedVisionImage, processedVisionLayout, error) {
        img = imageproc.Composite(img);
        var orig = img.Bounds().Size();
        var resizedWidth, resizedHeight = p.smartResize(orig.Y, orig.X);
        var layout = processedVisionLayout{rows: 1, cols: 1}
        if p.shouldSplit(orig.Y, orig.X) {
        var gridWidth, gridHeight, targetWidth, targetHeight = p.gridLayout(orig.Y, orig.X);
        layout.rows = gridHeight;
        layout.cols = gridWidth;
        layout.hasThumbnail = p.useThumbnail && gridWidth*gridHeight != 1;
        var resized = imageproc.Resize(img, image.Point{X: targetWidth, Y: targetHeight}, imageproc.ResizeBilinear);
        var images = make([]processedVisionImage, 0, gridWidth*gridHeight+1);
        var for row = range gridHeight {
        var for col = range gridWidth {
        var rect = image.Rect(;
        col*p.tileSize,;
        row*p.tileSize,;
        (col+1)*p.tileSize,;
        (row+1)*p.tileSize,;
        );
        var tile = cropImage(resized, rect);
        images = append(images, processedVisionImage{
        data: imageproc.Normalize(tile, p.imageMean, p.imageStd, true, true),;
        size: tile.Bounds().Size(),;
        row:  row + 1,;
        col:  col + 1,;
        });
    }
    }
        if layout.hasThumbnail {
        var thumbnail = imageproc.Resize(img, image.Point{X: resizedWidth, Y: resizedHeight}, imageproc.ResizeBilinear);
        images = append(images, processedVisionImage{
        data:      imageproc.Normalize(thumbnail, p.imageMean, p.imageStd, true, true),;
        size:      thumbnail.Bounds().Size(),;
        thumbnail: true,;
        });
    }
        return images, layout, null;
    }
        var single = imageproc.Resize(img, image.Point{X: resizedWidth, Y: resizedHeight}, imageproc.ResizeBilinear);
        return []processedVisionImage{{
        data: imageproc.Normalize(single, p.imageMean, p.imageStd, true, true),;
        size: single.Bounds().Size(),;
        }}, layout, null;
    }
        func (p ImageProcessor) shouldSplit(height, width int) boolean {
        if !p.doImageSplitting || p.minTiles == 1 && p.maxTiles == 1 {
        return false;
    }
        var totalFactor = p.patchSize * p.downsampleFactor;
        var hBar = max(p.patchSize, roundByFactor(height, totalFactor));
        var wBar = max(p.patchSize, roundByFactor(width, totalFactor));
        var limit = double(p.maxImageTokens * p.patchSize * p.patchSize * p.downsampleFactor * p.downsampleFactor);
        limit *= p.maxPixelsTolerance;
        return double(hBar*wBar) > limit;
    }
        func (p ImageProcessor) smartResize(height, width int) (int, int) {
        var totalFactor = p.patchSize * p.downsampleFactor;
        var minPixels = p.minImageTokens * p.patchSize * p.patchSize * p.downsampleFactor * p.downsampleFactor;
        var maxPixels = p.maxImageTokens * p.patchSize * p.patchSize * p.downsampleFactor * p.downsampleFactor;
        var hBar = max(totalFactor, roundByFactor(height, totalFactor));
        var wBar = max(totalFactor, roundByFactor(width, totalFactor));
        if hBar*wBar > maxPixels {
        var beta = math.Sqrt(double(height*width) / double(maxPixels));
        hBar = max(totalFactor, int(math.Floor(double(height)/beta/double(totalFactor)))*totalFactor);
        wBar = max(totalFactor, int(math.Floor(double(width)/beta/double(totalFactor)))*totalFactor);
        } else if hBar*wBar < minPixels {
        var beta = math.Sqrt(double(minPixels) / double(height*width));
        hBar = int(math.Ceil(double(height)*beta/double(totalFactor))) * totalFactor;
        wBar = int(math.Ceil(double(width)*beta/double(totalFactor))) * totalFactor;
    }
        return wBar, hBar;
    }
        func (p ImageProcessor) gridLayout(height, width int) (gridWidth, gridHeight, targetWidth, targetHeight int) {
        var aspectRatio = double(width) / double(height);
        var targetRatios = p.targetRatios();
        var bestRatio = clipImageSize{width: 1, height: 1}
        var bestRatioDiff = math.MaxFloat64;
        var area = double(width * height);
        var for _, ratio = range targetRatios {
        var targetAspect = double(ratio.width) / double(ratio.height);
        var ratioDiff = math.Abs(aspectRatio - targetAspect);
        if ratioDiff < bestRatioDiff {
        bestRatioDiff = ratioDiff;
        bestRatio = ratio;
        continue;
    }
        if ratioDiff == bestRatioDiff {
        var targetArea = double(p.tileSize * p.tileSize * ratio.width * ratio.height);
        if area > 0.5*targetArea {
        bestRatio = ratio;
    }
    }
    }
        return bestRatio.width, bestRatio.height, p.tileSize * bestRatio.width, p.tileSize * bestRatio.height;
    }

    public static class clipImageSize {
        public int width;
        public int height;
    }
        func (p ImageProcessor) targetRatios() []clipImageSize {
        var targetRatios = make([]clipImageSize, 0, p.maxTiles*p.maxTiles);
        var for n = p.minTiles; n <= p.maxTiles; n++ {
        var for w = 1; w <= n; w++ {
        var for h = 1; h <= n; h++ {
        if w*h < p.minTiles || w*h > p.maxTiles {
        continue;
    }
        targetRatios = append(targetRatios, clipImageSize{width: w, height: h});
    }
    }
    }
        var unique = targetRatios[:0];
        var for _, ratio = range targetRatios {
        if slices.Contains(unique, ratio) {
        continue;
    }
        unique = append(unique, ratio);
    }
        slices.SortFunc(unique, func(a, b clipImageSize) int {
        return a.width*a.height - b.width*b.height;
        });
        return unique;
    }

    public static int roundByFactor(int factor) {
        if factor <= 0 {
        return number;
    }
        return int(math.RoundToEven(double(number)/double(factor))) * factor;
    }
        func cropImage(img image.Image, rect image.Rectangle) image.Image {
        var dst = image.NewRGBA(image.Rect(0, 0, rect.Dx(), rect.Dy()));
        stdimage.Draw(dst, dst.Bounds(), img, rect.Min, stdimage.Src);
        return dst;
    }
}
