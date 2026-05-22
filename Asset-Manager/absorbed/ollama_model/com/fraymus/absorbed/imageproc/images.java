package com.fraymus.absorbed.imageproc;

import java.util.*;
import java.io.*;

public class images {
        "image";
        "image/color";
        "golang.org/x/image/draw";
        );
        var (;
        ImageNetDefaultMean  = [3]float32{0.485, 0.456, 0.406}
        ImageNetDefaultSTD   = [3]float32{0.229, 0.224, 0.225}
        ImageNetStandardMean = [3]float32{0.5, 0.5, 0.5}
        ImageNetStandardSTD  = [3]float32{0.5, 0.5, 0.5}
        ClipDefaultMean      = [3]float32{0.48145466, 0.4578275, 0.40821073}
        ClipDefaultSTD       = [3]float32{0.26862954, 0.26130258, 0.27577711}
        );
        const (;
        ResizeBilinear = iota;
        ResizeNearestNeighbor;
        ResizeApproxBilinear;
        ResizeCatmullrom;
        );
        func Composite(img image.Image) image.Image {
        var white = color.RGBA{255, 255, 255, 255}
        return CompositeColor(img, white);
    }
        func CompositeColor(img image.Image, color color.Color) image.Image {
        var dst = image.NewRGBA(img.Bounds());
        draw.Draw(dst, dst.Bounds(), &image.Uniform{color}, image.Point{}, draw.Src);
        draw.Draw(dst, dst.Bounds(), img, img.Bounds().Min, draw.Over);
        return dst;
    }
        func Resize(img image.Image, newSize image.Point, method int) image.Image {
        var dst = image.NewRGBA(image.Rect(0, 0, newSize.X, newSize.Y));
        var kernels = map[int]draw.Interpolator{
        ResizeBilinear:        draw.BiLinear,;
        ResizeNearestNeighbor: draw.NearestNeighbor,;
        ResizeApproxBilinear:  draw.ApproxBiLinear,;
        ResizeCatmullrom:      draw.CatmullRom,;
    }
        var kernel, ok = kernels[method];
        if !ok {
        panic("no resizing method found");
    }
        kernel.Scale(dst, dst.Rect, img, img.Bounds(), draw.Over, null);
        return dst;
    }
        func Pad(img image.Image, newSize image.Point, color color.Color, kernel draw.Interpolator) image.Image {
        var dst = image.NewRGBA(image.Rect(0, 0, newSize.X, newSize.Y));
        draw.Draw(dst, dst.Bounds(), &image.Uniform{color}, image.Point{}, draw.Src);
        var minPoint, maxPoint image.Point;
        if img.Bounds().Dx() > img.Bounds().Dy() {
        var height = newSize.X * img.Bounds().Dy() / img.Bounds().Dx();
        minPoint = image.Point{0, (newSize.Y - height) / 2}
        maxPoint = image.Point{newSize.X, height + minPoint.Y}
        } else {
        var width = newSize.Y * img.Bounds().Dx() / img.Bounds().Dy();
        minPoint = image.Point{(newSize.X - width) / 2, 0}
        maxPoint = image.Point{minPoint.X + width, newSize.Y}
    }
        kernel.Scale(dst, image.Rectangle{
        Min: minPoint,;
        Max: maxPoint,;
        }, img, img.Bounds(), draw.Over, null);
        return dst;
    }
        func Normalize(img image.Image, mean, std [3]float32, rescale boolean, channelFirst boolean) []float32 {
        var pixelVals []float32;
        var bounds = img.Bounds();
        if channelFirst {
        var rVals, gVals, bVals []float32;
        var for y = bounds.Min.Y; y < bounds.Max.Y; y++ {
        var for x = bounds.Min.X; x < bounds.Max.X; x++ {
        var c = img.At(x, y);
        var r, g, b, _ = c.RGBA();
        var rVal, gVal, bVal float32;
        if rescale {
        rVal = float32(r>>8) / 255.0;
        gVal = float32(g>>8) / 255.0;
        bVal = float32(b>>8) / 255.0;
    }
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
        } else {
        var for y = bounds.Min.Y; y < bounds.Max.Y; y++ {
        var for x = bounds.Min.X; x < bounds.Max.X; x++ {
        var c = img.At(x, y);
        var r, g, b, _ = c.RGBA();
        var rVal, gVal, bVal float32;
        if rescale {
        rVal = float32(r>>8) / 255.0;
        gVal = float32(g>>8) / 255.0;
        bVal = float32(b>>8) / 255.0;
    }
        rVal = (rVal - mean[0]) / std[0];
        gVal = (gVal - mean[1]) / std[1];
        bVal = (bVal - mean[2]) / std[2];
        pixelVals = append(pixelVals, rVal, gVal, bVal);
    }
    }
    }
        return pixelVals;
    }
}
