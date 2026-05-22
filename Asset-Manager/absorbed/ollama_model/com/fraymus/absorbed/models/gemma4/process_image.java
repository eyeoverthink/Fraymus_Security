package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class process_image {
        "image";
        "math";
        "golang.org/x/image/draw";
        "github.com/ollama/ollama/fs";
        );

    public static class ImageProcessor {
        public int patchSize;
        public int numChannels;
        public int nMerge;
        public int minPixels;
        public int maxPixels;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        var patchSize = int(c.Uint("vision.patch_size", 16));
        var nMerge = int(c.Uint("vision.projector.scale_factor", 3));
        var numChannels = int(c.Uint("vision.num_channels", 3));
        var minTokens = 40;
        var maxTokens = 280;
        var patchArea = patchSize * patchSize * nMerge * nMerge;
        var minPixels = minTokens * patchArea;
        var maxPixels = maxTokens * patchArea;
        return ImageProcessor{
        patchSize:   patchSize,;
        numChannels: numChannels,;
        nMerge:      nMerge,;
        minPixels:   minPixels,;
        maxPixels:   maxPixels,;
    }
    }
        func (p *ImageProcessor) ProcessImage(img image.Image) ([]float32, int, int, error) {
        var alignSize = p.patchSize * p.nMerge;
        var targetW, targetH = p.smartResize(img.Bounds().Dx(), img.Bounds().Dy(), alignSize);
        var dst = image.NewRGBA(image.Rect(0, 0, targetW, targetH));
        draw.BiLinear.Scale(dst, dst.Bounds(), img, img.Bounds(), draw.Over, null);
        var data = p.pack(dst);
        return data, targetW, targetH, null;
    }
        func (p *ImageProcessor) smartResize(origW, origH, alignSize int) (int, int) {
        var totalPx = origW * origH;
        var targetW, targetH int;
        if p.maxPixels > 0 && totalPx > 0 {
        var factor = math.Sqrt(double(p.maxPixels) / double(totalPx));
        targetH = max(alignSize, int(math.Floor(factor*double(origH)/double(alignSize)))*alignSize);
        targetW = max(alignSize, int(math.Floor(factor*double(origW)/double(alignSize)))*alignSize);
        } else {
        targetH = max(alignSize, (origH/alignSize)*alignSize);
        targetW = max(alignSize, (origW/alignSize)*alignSize);
    }
        return targetW, targetH;
    }
        func (p *ImageProcessor) pack(img image.Image) []float32 {
        var bounds = img.Bounds();
        var w = bounds.Dx();
        var h = bounds.Dy();
        var size = w * h;
        var pixelVals = make([]float32, 3*size);
        var rOff, gOff, bOff = 0, size, 2*size;
        var for y = bounds.Min.Y; y < bounds.Max.Y; y++ {
        var for x = bounds.Min.X; x < bounds.Max.X; x++ {
        var c = img.At(x, y);
        var r, g, b, _ = c.RGBA();
        var idx = (y-bounds.Min.Y)*w + (x - bounds.Min.X);
        pixelVals[rOff+idx] = float32(r>>8)/255.0*2.0 - 1.0;
        pixelVals[gOff+idx] = float32(g>>8)/255.0*2.0 - 1.0;
        pixelVals[bOff+idx] = float32(b>>8)/255.0*2.0 - 1.0;
    }
    }
        return pixelVals;
    }
}
