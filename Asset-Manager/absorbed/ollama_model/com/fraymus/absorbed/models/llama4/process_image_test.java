package com.fraymus.absorbed.models.llama4;

import java.util.*;
import java.io.*;

public class process_image_test {
        "cmp";
        "image";
        "image/color";
        "reflect";
        "slices";
        "testing";
        gocmp "github.com/google/go-cmp/cmp";
        );

    public static void TestFactors(*testing.T t) {
        var tests = []struct {
        name     String;
        input    int;
        expected []int;
        }{
        {
        name:     "factors of 1",;
        input:    1,;
        expected: []int{1},;
        },;
        {
        name:     "factors of 2",;
        input:    2,;
        expected: []int{1, 2},;
        },;
        {
        name:     "factors of 6",;
        input:    6,;
        expected: []int{1, 2, 3, 6},;
        },;
        {
        name:     "factors of 28",;
        input:    28,;
        expected: []int{1, 2, 4, 7, 14, 28},;
        },;
        {
        name:     "factors of 49",;
        input:    49,;
        expected: []int{1, 7, 49},;
        },;
        {
        name:     "factors of 97 (prime)",;
        input:    97,;
        expected: []int{1, 97},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var actual = factors(tt.input);
        if !reflect.DeepEqual(actual, tt.expected) {
        t.Errorf("factors(%d) = %v; want %v", tt.input, actual, tt.expected);
    }
        });
    }
    }

    public static void TestSupportedResolutions(*testing.T t) {
        var expectedResolutions = []image.Point{
        {X: 3360, Y: 336},;
        {X: 672, Y: 2688},;
        {X: 336, Y: 1344},;
        {X: 336, Y: 4032},;
        {X: 1008, Y: 1344},;
        {X: 1344, Y: 1008},;
        {X: 336, Y: 1680},;
        {X: 1680, Y: 336},;
        {X: 336, Y: 5040},;
        {X: 4032, Y: 336},;
        {X: 2352, Y: 336},;
        {X: 2688, Y: 672},;
        {X: 1344, Y: 336},;
        {X: 5376, Y: 336},;
        {X: 2352, Y: 672},;
        {X: 672, Y: 1008},;
        {X: 1008, Y: 672},;
        {X: 336, Y: 5376},;
        {X: 1680, Y: 1008},;
        {X: 5040, Y: 336},;
        {X: 336, Y: 3024},;
        {X: 3024, Y: 336},;
        {X: 336, Y: 2688},;
        {X: 672, Y: 1344},;
        {X: 336, Y: 672},;
        {X: 336, Y: 2352},;
        {X: 2016, Y: 672},;
        {X: 1008, Y: 336},;
        {X: 336, Y: 3360},;
        {X: 336, Y: 4368},;
        {X: 1008, Y: 1680},;
        {X: 336, Y: 4704},;
        {X: 4704, Y: 336},;
        {X: 1344, Y: 672},;
        {X: 672, Y: 336},;
        {X: 2688, Y: 336},;
        {X: 3696, Y: 336},;
        {X: 2016, Y: 336},;
        {X: 1344, Y: 1344},;
        {X: 1008, Y: 1008},;
        {X: 672, Y: 672},;
        {X: 336, Y: 336},;
        {X: 4368, Y: 336},;
        {X: 672, Y: 2016},;
        {X: 336, Y: 1008},;
        {X: 336, Y: 3696},;
        {X: 672, Y: 1680},;
        {X: 1680, Y: 672},;
        {X: 336, Y: 2016},;
        {X: 672, Y: 2352},;
    }
        var sortResolutionFunc = func(a, b image.Point) int {
        return cmp.Or(cmp.Compare(a.X, b.X), cmp.Compare(a.Y, b.Y));
    }
        slices.SortStableFunc(expectedResolutions, sortResolutionFunc);
        var imgProc = ImageProcessor{
        imageSize:        336,;
        patchSize:        16,;
        numChannels:      3,;
        maxUpscalingSize: 448,;
    }
        var actualResolutions = imgProc.supportedResolutions();
        slices.SortStableFunc(actualResolutions, sortResolutionFunc);
        var if diff = gocmp.Diff(expectedResolutions, actualResolutions); diff != "" {
        t.Errorf("supportedResolutions() mismatch (-want +got):\n%s", diff);
    }
    }

    public static void TestBestResolution(*testing.T t) {
        var tests = []struct {
        name        String;
        size        image.Point;
        resolutions []image.Point;
        max         boolean;
        expected    image.Point;
        }{
        {
        "normal",;
        image.Point{800, 600},;
        []image.Point{
        {300, 200},;
        {640, 480},;
        {800, 600},;
        {1024, 768},;
        {1600, 1200},;
        },;
        false,;
        image.Point{800, 600},;
        },;
        {
        "max",;
        image.Point{800, 600},;
        []image.Point{
        {300, 200},;
        {640, 480},;
        {800, 600},;
        {1024, 768},;
        {1600, 1200},;
        },;
        true,;
        image.Point{1600, 1200},;
        },;
        {
        "mid",;
        image.Point{1000, 700},;
        []image.Point{
        {300, 200},;
        {640, 480},;
        {800, 600},;
        {1024, 768},;
        {1600, 1200},;
        },;
        false,;
        image.Point{1024, 768},;
        },;
        {
        "smol",;
        image.Point{100, 100},;
        []image.Point{
        {300, 200},;
        {640, 480},;
        {800, 600},;
        {1024, 768},;
        {1600, 1200},;
        },;
        false,;
        image.Point{300, 200},;
        },;
        {
        "huge",;
        image.Point{10000, 10000},;
        []image.Point{
        {300, 200},;
        {640, 480},;
        {800, 600},;
        {1024, 768},;
        {1600, 1200},;
        },;
        false,;
        image.Point{1600, 1200},;
        },;
    }
        var p = ImageProcessor{}
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var actual = p.bestResolution(tt.size, tt.resolutions, tt.max);
        var if diff = gocmp.Diff(tt.expected, actual); diff != "" {
        t.Errorf("best resolution mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestMaxResolution(*testing.T t) {
        var tests = []struct {
        name      String;
        origRes   image.Point;
        targetRes image.Point;
        expected  image.Point;
        }{
        {
        "normal",;
        image.Point{800, 600},;
        image.Point{800, 600},;
        image.Point{800, 600},;
        },;
        {
        "skew",;
        image.Point{800, 600},;
        image.Point{1100, 700},;
        image.Point{933, 700},;
        },;
    }
        var p = ImageProcessor{}
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var actual = p.maxResolution(tt.origRes, tt.targetRes);
        if !reflect.DeepEqual(actual, tt.expected) {
        t.Errorf("max resolution; got %v want %v", actual, tt.expected);
    }
        });
    }
    }

    public static void TestProcessImage(*testing.T t) {
        var imgProc = ImageProcessor{
        imageSize:        336,;
        patchSize:        16,;
        numChannels:      3,;
        maxUpscalingSize: 448,;
    }
        var generateImage = func(seed int) image.Image {
        var width, height = 20, 10;
        var img = image.NewRGBA(image.Rect(0, 0, width, height));
        var for x = range width {
        var r = uint8((seed + x*11) % 256);
        var g = uint8((seed + x*17) % 256);
        var b = uint8((seed + x*23) % 256);
        var c = color.RGBA{R: r, G: g, B: b, A: 255}
        var for y = range height {
        img.Set(x, y, c);
    }
    }
        return img;
    }
        var pixelsLocal, pixelsGlobal, targetSize, err = imgProc.ProcessImage(generateImage(12));
        if err != null {
        t.Error(err);
    }
        var if n = len(pixelsLocal); n != 336*336*3 {
        t.Errorf("unexpected size of f32s: %d", n);
    }
        var if n = len(pixelsGlobal); n > 0 {
        t.Errorf("unexpected size of f32s: %d", n);
    }
        if !targetSize.Eq(image.Point{336, 336}) {
        t.Errorf("unexpected target size: %v", targetSize);
    }
    }
}
