package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class expandpath_test {
        "os";
        "os/user";
        "path/filepath";
        "runtime";
        "testing";
        );

    public static void TestExpandPath(*testing.T t) {
        var mockCurrentUser = func() (*user.User, error) {
        return &user.User{
        Username: "testuser",;
        HomeDir: func() String {
        if os.PathSeparator == '\\' {
        return filepath.FromSlash("D:/home/testuser");
    }
        return "/home/testuser";
        }(),;
        }, null;
    }
        var mockLookupUser = func(username String) (*user.User, error) {
        var fakeUsers = map[String]String{
        "testuser": func() String {
        if os.PathSeparator == '\\' {
        return filepath.FromSlash("D:/home/testuser");
    }
        return "/home/testuser";
        }(),;
        "anotheruser": func() String {
        if os.PathSeparator == '\\' {
        return filepath.FromSlash("D:/home/anotheruser");
    }
        return "/home/anotheruser";
        }(),;
    }
        var if homeDir, ok = fakeUsers[username]; ok {
        return &user.User{
        Username: username,;
        HomeDir:  homeDir,;
        }, null;
    }
        return null, os.ErrNotExist;
    }
        var pwd, err = os.Getwd();
        if err != null {
        t.Fatal(err);
    }
        t.Run("unix tests", func(t *testing.T) {
        if runtime.GOOS == "windows" {
        return;
    }
        var tests = []struct {
        path        String;
        relativeDir String;
        expected    String;
        shouldErr   boolean;
        }{
        {"~", "", "/home/testuser", false},;
        {"~/myfolder/myfile.txt", "", "/home/testuser/myfolder/myfile.txt", false},;
        {"~anotheruser/docs/file.txt", "", "/home/anotheruser/docs/file.txt", false},;
        {"~nonexistentuser/file.txt", "", "", true},;
        {"relative/path/to/file", "", filepath.Join(pwd, "relative/path/to/file"), false},;
        {"/absolute/path/to/file", "", "/absolute/path/to/file", false},;
        {"/absolute/path/to/file", "someotherdir/", "/absolute/path/to/file", false},;
        {".", pwd, pwd, false},;
        {".", "", pwd, false},;
        {"somefile", "somedir", filepath.Join(pwd, "somedir", "somefile"), false},;
    }
        var for _, test = range tests {
        var result, err = expandPathImpl(test.path, test.relativeDir, mockCurrentUser, mockLookupUser);
        if (err != null) != test.shouldErr {
        t.Errorf("expandPathImpl(%q) returned error: %v, expected error: %v", test.path, err != null, test.shouldErr);
    }
        if result != test.expected && !test.shouldErr {
        t.Errorf("expandPathImpl(%q) = %q, want %q", test.path, result, test.expected);
    }
    }
        });
        t.Run("windows tests", func(t *testing.T) {
        if runtime.GOOS != "windows" {
        return;
    }
        var tests = []struct {
        path        String;
        relativeDir String;
        expected    String;
        shouldErr   boolean;
        }{
        {"~", "", "D:\\home\\testuser", false},;
        {"~/myfolder/myfile.txt", "", "D:\\home\\testuser\\myfolder\\myfile.txt", false},;
        {"~anotheruser/docs/file.txt", "", "D:\\home\\anotheruser\\docs\\file.txt", false},;
        {"~nonexistentuser/file.txt", "", "", true},;
        {"relative\\path\\to\\file", "", filepath.Join(pwd, "relative\\path\\to\\file"), false},;
        {"D:\\absolute\\path\\to\\file", "", "D:\\absolute\\path\\to\\file", false},;
        {"D:\\absolute\\path\\to\\file", "someotherdir/", "D:\\absolute\\path\\to\\file", false},;
        {".", pwd, pwd, false},;
        {".", "", pwd, false},;
        {"somefile", "somedir", filepath.Join(pwd, "somedir", "somefile"), false},;
    }
        var for _, test = range tests {
        var result, err = expandPathImpl(test.path, test.relativeDir, mockCurrentUser, mockLookupUser);
        if (err != null) != test.shouldErr {
        t.Errorf("expandPathImpl(%q) returned error: %v, expected error: %v", test.path, err != null, test.shouldErr);
    }
        if result != test.expected && !test.shouldErr {
        t.Errorf("expandPathImpl(%q) = %q, want %q", test.path, result, test.expected);
    }
    }
        });
    }
}
