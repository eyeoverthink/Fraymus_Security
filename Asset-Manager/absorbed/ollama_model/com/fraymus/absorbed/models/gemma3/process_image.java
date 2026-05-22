package com.fraymus.absorbed.models.gemma3;

import java.util.*;
import java.io.*;

public class process_image {
        "image";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class ImageProcessor {
        public patchSize, imageSize,;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        return ImageProcessor{
        imageSize:   int(c.Uint("vision.image_size")),;
        patchSize:   int(c.Uint("vision.patch_size")),;
        numChannels: int(c.Uint("vision.num_channels")),;
    }
    }
        func (p *ImageProcessor) pack(img image.Image, mean, std [3]float32) []float32 {
        var pixelVals, rVals, gVals, bVals []float32;
        var bounds = img.Bounds();
        var for y = bounds.Min.Y; y < bounds.Max.Y; y++ {
        var for x = bounds.Min.X; x < bounds.Max.X; x++ {
        var c = img.At(x, y);
        var r, g, b, _ = c.RGBA();
        var rVal = float32(r>>8) / 255.0;
        var gVal = float32(g>>8) / 255.0;
        var bVal = float32(b>>8) / 255.0;
        rVal = (rVal - mean[0]) / std[0];
        gVal = (gVal - mean[1]) / std[1];
        bVal = (bVal - mean[2]) / std[2];
        rVals = append(rVals, rVal);
        gVals = append(gVals, gVal);
        bVals = append(bVals, bVal);
    }
    }
        pixelVals = append(pixelVals, rVals...);
        pixelVals = append(pixelVals, gVals...);
        pixelVals = append(pixelVals, bVals...);
        return pixelVals;
    }
        func (p ImageProcessor) ProcessImage(img image.Image) ([]float32, error) {
        var outputSize = image.Point{p.imageSize, p.imageSize}
        var newImage = imageproc.Composite(img);
        newImage = imageproc.Resize(newImage, outputSize, imageproc.ResizeBilinear);
        var data = p.pack(newImage, imageproc.ImageNetStandardMean, imageproc.ImageNetStandardSTD);
        return data, null;
    }
}
