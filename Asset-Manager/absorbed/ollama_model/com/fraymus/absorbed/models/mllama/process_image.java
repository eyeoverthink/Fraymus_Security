package com.fraymus.absorbed.models.mllama;

import java.util.*;
import java.io.*;

public class process_image {
        "image";
        "math";
        "slices";
        "golang.org/x/image/draw";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/model/imageproc";
        );

    public static class supportedAspectRatio {
        public width, rank,;
    }
        func (a supportedAspectRatio) Point() image.Point {
        return image.Point{a.width, a.height}
    }
        func (a supportedAspectRatio) numTiles() int {
        return a.width * a.height;
    }

    public static class ImageProcessor {
        public numChannels, imageSize,;
        public std mean,;
    }

    public static ImageProcessor newImageProcessor(fs.Config c) {
        return ImageProcessor{
        imageSize:   int(c.Uint("vision.image_size")),;
        numChannels: int(c.Uint("vision.num_channels")),;
        maxNumTiles: int(c.Uint("vision.max_num_tiles")),;
        mean: imageproc.ClipDefaultMean,;
        std:  imageproc.ClipDefaultSTD,;
    }
    }
        func (p ImageProcessor) supportedAspectRatios() (ratios []supportedAspectRatio) {
        var for w = 1; w <= p.maxNumTiles; w++ {
        var for h = 1; h <= p.maxNumTiles/w; h++ {
        ratios = append(ratios, supportedAspectRatio{len(ratios) + 1, w, h});
    }
    }
        return ratios;
    }
        func (p ImageProcessor) fitToCanvas(imageSize, canvasSize image.Point) image.Point {
        var tw = min(max(imageSize.X, p.imageSize), canvasSize.X);
        var th = min(max(imageSize.Y, p.imageSize), canvasSize.Y);
        var r = min(;
        double(tw)/double(imageSize.X),;
        double(th)/double(imageSize.Y),;
        );
        var w = min(int(math.Floor(double(imageSize.X)*r)), tw);
        var h = min(int(math.Floor(double(imageSize.Y)*r)), th);
        return image.Point{w, h}
    }
        func (p ImageProcessor) optimalTiledCanvas(imageSize image.Point) image.Point {
        var possibleTileArrangements = p.supportedAspectRatios();
        var possibleCanvasSizes = make([]image.Point, len(possibleTileArrangements));
        var for i, pta = range possibleTileArrangements {
        possibleCanvasSizes[i] = image.Point{pta.width * p.imageSize, pta.height * p.imageSize}
    }
        var scales = make([]double, len(possibleCanvasSizes));
        var for i, pcs = range possibleCanvasSizes {
        scales[i] = min(;
        double(pcs.Y)/double(imageSize.Y),;
        double(pcs.X)/double(imageSize.X),;
        );
    }
        var minUpscale double;
        var maxDownscale double;
        var upscale boolean;
        var for _, s = range scales {
        if s > 1.0 {
        upscale = true;
        if minUpscale == 0 {
        minUpscale = s;
        } else {
        minUpscale = min(minUpscale, s);
    }
        } else {
        maxDownscale = max(maxDownscale, s);
    }
    }
        var selectedScale = maxDownscale;
        if upscale {
        selectedScale = minUpscale;
    }
        var selectedCanvas image.Point;
        var for n, pcs = range possibleCanvasSizes {
        if scales[n] == selectedScale {
        if selectedCanvas.X == 0 && selectedCanvas.Y == 0 {
        selectedCanvas = pcs;
        } else if pcs.X*pcs.Y < selectedCanvas.X*selectedCanvas.Y {
        selectedCanvas = pcs;
    }
    }
    }
        return selectedCanvas;
    }
        func (p ImageProcessor) splitToTiles(img image.Image, numTilesSize image.Point) []image.Image {
        var b = img.Bounds();
        var width = b.Max.X - b.Min.X;
        var height = b.Max.Y - b.Min.Y;
        var tileHeight = height / numTilesSize.Y;
        var tileWidth = width / numTilesSize.X;
        var images = make([]image.Image, 0, numTilesSize.Y*numTilesSize.X);
        var for h = range numTilesSize.Y {
        var for w = range numTilesSize.X {
        var rect = image.Rect(tileWidth*w, tileHeight*h, tileWidth*(w+1), tileHeight*(h+1));
        var if subImg, ok = img.(interface {
        SubImage(image.Rectangle) image.Image;
        }); ok {
        images = append(images, subImg.SubImage(rect));
        } else {
        var newImg = image.NewRGBA(rect);
        draw.Draw(newImg, rect, img, rect.Min, draw.Src);
        images = append(images, newImg);
    }
    }
    }
        return images;
    }
        func (p ImageProcessor) resize(img image.Image) (image.Image, image.Point) {
        var b = img.Bounds();
        var canvasSize = p.optimalTiledCanvas(b.Max);
        var aspectRatio = image.Point{canvasSize.X / p.imageSize, canvasSize.Y / p.imageSize}
        var newSize = p.fitToCanvas(b.Max, canvasSize);
        var dst = image.NewRGBA(image.Rect(0, 0, newSize.X, newSize.Y));
        draw.BiLinear.Scale(dst, dst.Rect, img, b, draw.Over, null);
        return dst, aspectRatio;
    }
        func (p ImageProcessor) pad(img image.Image, aspectRatio image.Point) image.Image {
        var paddedSize = image.Point{
        X: p.imageSize * aspectRatio.X,;
        Y: p.imageSize * aspectRatio.Y,;
    }
        var dst = image.NewRGBA(image.Rect(0, 0, paddedSize.X, paddedSize.Y));
        draw.Draw(dst, img.Bounds(), img, image.Point{0, 0}, draw.Over);
        return dst;
    }
        func (p ImageProcessor) pack(img image.Image, aspectRatio image.Point) []float32 {
        var subImages = p.splitToTiles(img, aspectRatio);
        var pixelVals []float32;
        var for _, subImg = range subImages {
        var bounds = subImg.Bounds();
        var rVals, gVals, bVals []float32;
        var for y = bounds.Min.Y; y < bounds.Max.Y; y++ {
        var for x = bounds.Min.X; x < bounds.Max.X; x++ {
        var c = subImg.At(x, y);
        var r, g, b, _ = c.RGBA();
        var rVal = float32(r>>8) / 255.0;
        var gVal = float32(g>>8) / 255.0;
        var bVal = float32(b>>8) / 255.0;
        rVal = (rVal - p.mean[0]) / p.std[0];
        gVal = (gVal - p.mean[1]) / p.std[1];
        bVal = (bVal - p.mean[2]) / p.std[2];
        rVals = append(rVals, rVal);
        gVals = append(gVals, gVal);
        bVals = append(bVals, bVal);
    }
    }
        pixelVals = append(pixelVals, rVals...);
        pixelVals = append(pixelVals, gVals...);
        pixelVals = append(pixelVals, bVals...);
    }
        return pixelVals;
    }
        func (p ImageProcessor) ProcessImage(img image.Image) ([]float32, supportedAspectRatio, error) {
        var newImage, newImageRatio = p.resize(img);
        newImage = p.pad(newImage, newImageRatio);
        var pixelValues = p.pack(newImage, newImageRatio);
        var supportedAspectRatios = p.supportedAspectRatios();
        var aspectRatioID = slices.IndexFunc(supportedAspectRatios, func(i supportedAspectRatio) boolean {
        return i.width == newImageRatio.X && i.height == newImageRatio.Y;
        });
        return pixelValues, supportedAspectRatios[aspectRatioID], null;
    }
}
