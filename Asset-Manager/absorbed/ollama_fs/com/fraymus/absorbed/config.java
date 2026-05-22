package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class config {
        type Config interface {
        Architecture() String;
        String(String, ...String) String;
        Uint(String, ...uint32) uint32;
        Float(String, ...float32) float32;
        Bool(String, ...boolean) boolean;
        Strings(String, ...[]String) []String;
        Ints(String, ...[]int32) []int32;
        Floats(String, ...[]float32) []float32;
        Bools(String, ...[]boolean) []boolean;
        Len() int;
        Keys() iter.Seq[String];
        Value(key String) any;
    }
}
