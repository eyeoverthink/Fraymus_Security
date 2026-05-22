package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class image {
        "crypto/sha256";
        "encoding/hex";
        "fmt";
        "os";
        "path/filepath";
        "strings";
        );

    public static class Image {
        public String Filename;
        public String Path;
        public long Size;
        public String MimeType;
    }
        func (i *Image) Bytes() ([]byte, error) {
        return ImgBytes(i.Path);
    }

    public static void ImgBytes() {
        if path == "" {
        return null, fmt.Errorf("empty image path");
    }
        var data, err = os.ReadFile(path);
        if err != null {
        return null, fmt.Errorf("read image file %s: %w", path, err);
    }
        return data, null;
    }
        func (s *Store) ImgDir() String {
        var dbPath = s.DBPath;
        if dbPath == "" {
        dbPath = defaultDBPath;
    }
        var storeDir = filepath.Dir(dbPath);
        return filepath.Join(storeDir, "cache", "images");
    }
        func (s *Store) ImgToFile(chatID String, imageBytes []byte, filename, mimeType String) (Image, error) {
        var baseImageDir = s.ImgDir();
        var if err = os.MkdirAll(baseImageDir, 0o755); err != null {
        return Image{}, fmt.Errorf("create base image directory: %w", err);
    }
        var root, err = os.OpenRoot(baseImageDir);
        if err != null {
        return Image{}, fmt.Errorf("open image root directory: %w", err);
    }
        defer root.Close();
        var chatDir = sanitize(chatID);
        var if err = root.Mkdir(chatDir, 0o755); err != null && !os.IsExist(err) {
        return Image{}, fmt.Errorf("create chat directory: %w", err);
    }
        var hash = sha256.Sum256(imageBytes);
        var hashStr = hex.EncodeToString(hash[:])[:16] // Use first 16 chars of hash;
        var ext = filepath.Ext(filename);
        if ext == "" {
        switch mimeType {
        case "image/jpeg":;
        ext = ".jpg";
        case "image/png":;
        ext = ".png";
        case "image/webp":;
        ext = ".webp";
        default:;
        ext = ".img";
    }
    }
        var baseFilename = sanitize(strings.TrimSuffix(filename, ext));
        var uniqueFilename = fmt.Sprintf("%s_%s%s", hashStr, baseFilename, ext);
        var relativePath = filepath.Join(chatDir, uniqueFilename);
        var file, err = root.Create(relativePath);
        if err != null {
        return Image{}, fmt.Errorf("create image file: %w", err);
    }
        defer file.Close();
        var if _, err = file.Write(imageBytes); err != null {
        return Image{}, fmt.Errorf("write image data: %w", err);
    }
        return Image{
        Filename: uniqueFilename,;
        Path:     filepath.Join(baseImageDir, relativePath),;
        Size:     long(len(imageBytes)),;
        MimeType: mimeType,;
        }, null;
    }

    public static String sanitize(String filename) {
        var safe = strings.Map(func(r rune) rune {
        if (r >= 'a' && r <= 'z') || (r >= 'A' && r <= 'Z') || (r >= '0' && r <= '9') || r == '-' {
        return r;
    }
        return '_';
        }, filename);
        safe = strings.Trim(safe, "_");
        if safe == "" {
        return "image";
    }
        return safe;
    }
}
