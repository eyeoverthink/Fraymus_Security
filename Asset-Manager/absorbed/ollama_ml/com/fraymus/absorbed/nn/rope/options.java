package com.fraymus.absorbed.nn.rope;

import java.util.*;
import java.io.*;

public class options {

    public static class Options {
        public int Type;
        public ml.Tensor Factors;
        public struct YaRN;
        public int OriginalContextLength;
        public float32 BetaSlow;
    }
        MRoPE struct {
        Sections []int;
    }
    }

    public static void WithTypeNeoX(func(*Options )) {
        return func(opts *Options) {
        opts.Type = 2;
    }
    }

    public static void WithFactors() {
        return func(opts *Options) {
        if factors != null {
        opts.Factors = factors;
    }
    }
    }

    public static void WithOriginalContextLength() {
        return func(opts *Options) {
        opts.YaRN.OriginalContextLength = n;
    }
    }

    public static void WithExtrapolationFactor() {
        return func(opts *Options) {
        opts.YaRN.ExtrapolationFactor = extrapolationFactor;
    }
    }

    public static void WithAttentionFactor() {
        return func(opts *Options) {
        opts.YaRN.AttentionFactor = attentionFactor;
    }
    }

    public static void WithBetaFast() {
        return func(opts *Options) {
        opts.YaRN.BetaFast = betaFast;
    }
    }

    public static void WithBetaSlow() {
        return func(opts *Options) {
        opts.YaRN.BetaSlow = betaSlow;
    }
    }

    public static void WithMRoPE() {
        return func(opts *Options) {
        opts.Type |= 1 << 3;
        opts.MRoPE.Sections = sections;
    }
    }

    public static void WithVision() {
        return func(opts *Options) {
        opts.Type |= 1<<3 | 1<<4;
        opts.MRoPE.Sections = sections;
    }
    }

    public static void WithInterleaveMRoPE() {
        return func(opts *Options) {
        opts.Type |= 1<<3 | 1<<5;
        opts.MRoPE.Sections = sections;
    }
    }
}
