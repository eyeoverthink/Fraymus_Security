package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class runner_test {
        "log/slog";
        "os";
        "testing";
        );

    public static void init() {
        var logger = slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelDebug}));
        slog.SetDefault(logger);
    }

    public static void TestFilterOverlapByLibrary(*testing.T t) {

    public static class testcase {
        public String name;
        public map[String]map[String]map[String]int inp;
        public []boolean exp;
    }
        var for _, tc = range []testcase{
        {
        name: "empty",;
        inp:  map[String]map[String]map[String]int{},;
        exp:  []boolean{}, // needs deletion;
        },;
        {
        name: "single no overlap",;
        inp: map[String]map[String]map[String]int{
        "CUDA": {
        "cuda_v12": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 0,;
        },;
        },;
        },;
        exp: []boolean{false},;
        },;
        {
        name: "100% overlap pick 2nd",;
        inp: map[String]map[String]map[String]int{
        "CUDA": {
        "cuda_v12": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 0,;
        "GPU-cd6c3216-03d2-a8eb-8235-2ffbf571712e": 1,;
        },;
        "cuda_v13": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 2,;
        "GPU-cd6c3216-03d2-a8eb-8235-2ffbf571712e": 3,;
        },;
        },;
        },;
        exp: []boolean{true, true, false, false},;
        },;
        {
        name: "100% overlap pick 1st",;
        inp: map[String]map[String]map[String]int{
        "CUDA": {
        "cuda_v13": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 0,;
        "GPU-cd6c3216-03d2-a8eb-8235-2ffbf571712e": 1,;
        },;
        "cuda_v12": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 2,;
        "GPU-cd6c3216-03d2-a8eb-8235-2ffbf571712e": 3,;
        },;
        },;
        },;
        exp: []boolean{false, false, true, true},;
        },;
        {
        name: "partial overlap pick older",;
        inp: map[String]map[String]map[String]int{
        "CUDA": {
        "cuda_v13": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 0,;
        },;
        "cuda_v12": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 1,;
        "GPU-cd6c3216-03d2-a8eb-8235-2ffbf571712e": 2,;
        },;
        },;
        },;
        exp: []boolean{true, false, false},;
        },;
        {
        name: "no overlap",;
        inp: map[String]map[String]map[String]int{
        "CUDA": {
        "cuda_v13": {
        "GPU-d7b00605-c0c8-152d-529d-e03726d5dc52": 0,;
        },;
        "cuda_v12": {
        "GPU-cd6c3216-03d2-a8eb-8235-2ffbf571712e": 1,;
        },;
        },;
        },;
        exp: []boolean{false, false},;
        },;
        } {
        t.Run(tc.name, func(t *testing.T) {
        var needsDelete = make([]boolean, len(tc.exp));
        filterOverlapByLibrary(tc.inp, needsDelete);
        var for i, exp = range tc.exp {
        if needsDelete[i] != exp {
        t.Fatalf("expected: %v\ngot: %v", tc.exp, needsDelete);
    }
    }
        });
    }
    }
}
