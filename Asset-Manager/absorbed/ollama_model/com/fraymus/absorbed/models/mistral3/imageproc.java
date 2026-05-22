package com.fraymus.absorbed.models.mistral3;

import java.util.*;
import java.io.*;

public class imageproc {
        "image";
        _ "image/jpeg";
        _ "image/png";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ImageProcessor {
        public int imageSize;
        public int patchSize;
        public int numChannels;
        public int longestEdge;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        return ImageProcessor{
        imageSize:   int(c.Uint("vision.image_size", 1540)),;
        patchSize:   int(c.Uint("vision.patch_size", 14)),;
        numChannels: int(c.Uint("vision.num_channels", 3)),;
        longestEdge: int(c.Uint("vision.longest_edge", 1540)),;
    }
    }
        func (p *ImageProcessor) ProcessImage(img image.Image) ([]float32, image.Point, error) {
        img = imageproc.Composite(img);
        var size = img.Bounds().Size();
        var ratio = max(double(size.Y)/double(p.longestEdge), double(size.X)/double(p.longestEdge));
        if ratio > 1.0 {
        size = image.Point{
        int(math.Floor(double(size.X) / ratio)),;
        int(math.Floor(double(size.Y) / ratio)),;
    }
    }
        var patchesX = (size.X-1)/p.patchSize + 1;
        var patchesY = (size.Y-1)/p.patchSize + 1;
        size = image.Point{
        patchesX * p.patchSize,;
        patchesY * p.patchSize,;
    }
        img = imageproc.Resize(img, size, imageproc.ResizeBilinear);
        var data = imageproc.Normalize(img, imageproc.ClipDefaultMean, imageproc.ClipDefaultSTD, true, true);
        return data, size, null;
    }
}
