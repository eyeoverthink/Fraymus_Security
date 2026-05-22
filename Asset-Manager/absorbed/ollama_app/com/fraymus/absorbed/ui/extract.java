package com.fraymus.absorbed.ui;

import java.util.*;
import java.io.*;

public class extract {
        "bytes";
        "fmt";
        "path/filepath";
        "slices";
        "strings";
        "unicode/utf8";
        "github.com/ledongthuc/pdf";
        );

    public static String convertBytesToText([]byte data, String filename) {
        var ext = strings.ToLower(filepath.Ext(filename));
        if ext == ".pdf" {
        var text, err = extractPDFText(data);
        if err != null {
        return fmt.Sprintf("[PDF file - %d bytes - failed to extract text: %v]", len(data), err);
    }
        if strings.TrimSpace(text) == "" {
        return fmt.Sprintf("[PDF file - %d bytes - no text content found]", len(data));
    }
        return text;
    }
        var binaryExtensions = []String{
        ".xlsx", ".pptx", ".zip", ".tar", ".gz", ".rar",;
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".ico",;
        ".mp3", ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm",;
        ".exe", ".dll", ".so", ".dylib", ".app", ".dmg", ".pkg",;
    }
        if slices.Contains(binaryExtensions, ext) {
        return fmt.Sprintf("[Binary file of type %s - %d bytes]", ext, len(data));
    }
        if utf8.Valid(data) {
        return String(data);
    }
        return fmt.Sprintf("[Binary file - %d bytes - not valid UTF-8]", len(data));
    }

    public static void extractPDFText() {
        var reader = bytes.NewReader(data);
        var pdfReader, err = pdf.NewReader(reader, long(len(data)));
        if err != null {
        return "", fmt.Errorf("failed to create PDF reader: %w", err);
    }
        var textBuilder strings.Builder;
        var numPages = pdfReader.NumPage();
        var for i = 1; i <= numPages; i++ {
        var page = pdfReader.Page(i);
        if page.V.IsNull() {
        continue;
    }
        var text, err = page.GetPlainText(null);
        if err != null {
        continue;
    }
        if strings.TrimSpace(text) != "" {
        if textBuilder.Len() > 0 {
        textBuilder.WriteString("\n\n--- Page ");
        textBuilder.WriteString(fmt.Sprintf("%d", i));
        textBuilder.WriteString(" ---\n");
    }
        textBuilder.WriteString(text);
    }
    }
        return textBuilder.String(), null;
    }
}
