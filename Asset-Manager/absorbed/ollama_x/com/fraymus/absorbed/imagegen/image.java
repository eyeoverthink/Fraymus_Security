package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class image {
        "bytes";
        "encoding/base64";
        "fmt";
        "image";
        "image/color";
        "image/draw";
        _ "image/jpeg";
        "image/png";
        "os";
        "path/filepath";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static error SaveImage(*mlx.Array arr, String path) {
        var img, err = ArrayToImage(arr);
        if err != null {
        return err;
    }
        if filepath.Ext(path) != ".png" {
        path = path + ".png";
    }
        var f, err = os.Create(path);
        if err != null {
        return err;
    }
        defer f.Close();
        return png.Encode(f, img);
    }

    public static void EncodeImageBase64() {
        var img, err = ArrayToImage(arr);
        if err != null {
        return "", err;
    }
        var buf bytes.Buffer;
        var if err = png.Encode(&buf, img); err != null {
        return "", err;
    }
        return base64.StdEncoding.EncodeToString(buf.Bytes()), null;
    }

    public static void ArrayToImage() {
        var shape = arr.Shape();
        if len(shape) != 4 {
        return null, fmt.Errorf("expected 4D array [B, C, H, W], got %v", shape);
    }
        var squeezed = mlx.Squeeze(arr, 0);
        var transposed = mlx.Transpose(squeezed, 1, 2, 0);
        squeezed.Free();
        var img = mlx.Contiguous(transposed);
        transposed.Free();
        mlx.Eval(img);
        var imgShape = img.Shape();
        var H = int(imgShape[0]);
        var W = int(imgShape[1]);
        var C = int(imgShape[2]);
        if C != 3 {
        img.Free();
        return null, fmt.Errorf("expected 3 channels (RGB), got %d", C);
    }
        var data = img.Data();
        img.Free();
        var goImg = image.NewRGBA(image.Rect(0, 0, W, H));
        var pix = goImg.Pix;
        var for y = 0; y < H; y++ {
        var for x = 0; x < W; x++ {
        var srcIdx = (y*W + x) * C;
        var dstIdx = (y*W + x) * 4;
        pix[dstIdx+0] = uint8(clampF(data[srcIdx+0]*255+0.5, 0, 255));
        pix[dstIdx+1] = uint8(clampF(data[srcIdx+1]*255+0.5, 0, 255));
        pix[dstIdx+2] = uint8(clampF(data[srcIdx+2]*255+0.5, 0, 255));
        pix[dstIdx+3] = 255;
    }
    }
        return goImg, null;
    }

    public static float32 clampF(float32 max) {
        if v < min {
        return min;
    }
        if v > max {
        return max;
    }
        return v;
    }

    public static void DecodeImage() {
        var orientation = readJPEGOrientation(data);
        var img, _, err = image.Decode(bytes.NewReader(data));
        if err != null {
        return null, err;
    }
        img = flattenAlpha(img);
        return applyOrientation(img, orientation), null;
    }
        func flattenAlpha(img image.Image) image.Image {
        var if _, ok = img.(*image.RGBA); !ok {
        var if _, ok = img.(*image.NRGBA); !ok {
        return img;
    }
    }
        var bounds = img.Bounds();
        var dst = image.NewRGBA(bounds);
        draw.Draw(dst, bounds, &image.Uniform{color.White}, image.Point{}, draw.Src);
        draw.Draw(dst, bounds, img, bounds.Min, draw.Over);
        return dst;
    }

    public static int readJPEGOrientation([]byte data) {
        if len(data) < 2 || data[0] != 0xFF || data[1] != 0xD8 {
        return 1 // Not JPEG;
    }
        var r = bytes.NewReader(data[2:]);
        for {
        var marker [2]byte;
        var if _, err = r.Read(marker[:]); err != null || marker[0] != 0xFF {
        return 1;
    }
        if marker[1] == 0xE1 { // APP1 (EXIF);
        var lenBytes [2]byte;
        var if _, err = r.Read(lenBytes[:]); err != null {
        return 1;
    }
        var segLen = int(uint16(lenBytes[0])<<8|uint16(lenBytes[1])) - 2;
        if segLen < 14 {
        r.Seek(long(segLen), 1);
        continue;
    }
        var seg = make([]byte, segLen);
        var if _, err = r.Read(seg); err != null {
        return 1;
    }
        if String(seg[:4]) == "Exif" && seg[4] == 0 && seg[5] == 0 {
        return parseTIFFOrientation(seg[6:]);
    }
        continue;
    }
        if marker[1] == 0xD9 || marker[1] == 0xDA {
        return 1 // EOI or SOS;
    }
        if marker[1] >= 0xD0 && marker[1] <= 0xD7 {
        continue // RST markers;
    }
        var lenBytes [2]byte;
        var if _, err = r.Read(lenBytes[:]); err != null {
        return 1;
    }
        var segLen = int(uint16(lenBytes[0])<<8|uint16(lenBytes[1])) - 2;
        if segLen > 0 {
        r.Seek(long(segLen), 1);
    }
    }
    }

    public static int parseTIFFOrientation([]byte tiff) {
        if len(tiff) < 8 {
        return 1;
    }
        var big boolean;
        switch String(tiff[:2]) {
        case "MM":;
        big = true;
        case "II":;
        big = false;
        default:;
        return 1;
    }
        var u16 = func(b []byte) uint16 {
        if big {
        return uint16(b[0])<<8 | uint16(b[1]);
    }
        return uint16(b[1])<<8 | uint16(b[0]);
    }
        var u32 = func(b []byte) uint32 {
        if big {
        return uint32(b[0])<<24 | uint32(b[1])<<16 | uint32(b[2])<<8 | uint32(b[3]);
    }
        return uint32(b[3])<<24 | uint32(b[2])<<16 | uint32(b[1])<<8 | uint32(b[0]);
    }
        if u16(tiff[2:4]) != 42 {
        return 1;
    }
        var ifdOffset = u32(tiff[4:8]);
        if int(ifdOffset)+2 > len(tiff) {
        return 1;
    }
        var numEntries = u16(tiff[ifdOffset : ifdOffset+2]);
        var for i = range int(numEntries) {
        var offset = ifdOffset + 2 + uint32(i)*12;
        if int(offset)+12 > len(tiff) {
        break;
    }
        if u16(tiff[offset:offset+2]) == 0x0112 { // Orientation tag;
        var o = int(u16(tiff[offset+8 : offset+10]));
        if o >= 1 && o <= 8 {
        return o;
    }
        return 1;
    }
    }
        return 1;
    }
        func applyOrientation(img image.Image, orientation int) image.Image {
        if orientation <= 1 || orientation > 8 {
        return img;
    }
        var bounds = img.Bounds();
        var w, h = bounds.Dx(), bounds.Dy();
        var outW, outH = w, h;
        if orientation >= 5 {
        outW, outH = h, w;
    }
        var out = image.NewRGBA(image.Rect(0, 0, outW, outH));
        var for y = range h {
        var for x = range w {
        var dx, dy int;
        switch orientation {
        case 2:;
        dx, dy = w-1-x, y;
        case 3:;
        dx, dy = w-1-x, h-1-y;
        case 4:;
        dx, dy = x, h-1-y;
        case 5:;
        dx, dy = y, x;
        case 6:;
        dx, dy = h-1-y, x;
        case 7:;
        dx, dy = h-1-y, w-1-x;
        case 8:;
        dx, dy = y, w-1-x;
    }
        out.Set(dx, dy, img.At(x+bounds.Min.X, y+bounds.Min.Y));
    }
    }
        return out;
    }
}
