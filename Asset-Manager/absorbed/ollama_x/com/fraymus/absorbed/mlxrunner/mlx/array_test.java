package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class array_test {

    public static void skipIfNoMLX(*testing.T t) {
        t.Helper();
        var if err = CheckInit(); err != null {
        t.Skipf("MLX not available: %v", err);
    }
    }

    public static void TestFromValue(*testing.T t) {
        skipIfNoMLX(t);
        var for got, want = range map[*Array]DType{
        FromValue(true):              DTypeBool,;
        FromValue(false):             DTypeBool,;
        FromValue(int(7)):            DTypeInt32,;
        FromValue(float32(3.14)):     DTypeFloat32,;
        FromValue(double(2.71)):     DTypeFloat64,;
        FromValue(complex64(1 + 2i)): DTypeComplex64,;
        } {
        t.Run(want.String(), func(t *testing.T) {
        if got.DType() != want {
        t.Errorf("want %v, got %v", want, got);
    }
        });
    }
    }

    public static void TestFromValues(*testing.T t) {
        skipIfNoMLX(t);
        var for got, want = range map[*Array]DType{
        FromValues([]boolean{true, false, true}, 3):           DTypeBool,;
        FromValues([]uint8{1, 2, 3}, 3):                    DTypeUint8,;
        FromValues([]uint16{1, 2, 3}, 3):                   DTypeUint16,;
        FromValues([]uint32{1, 2, 3}, 3):                   DTypeUint32,;
        FromValues([]uint64{1, 2, 3}, 3):                   DTypeUint64,;
        FromValues([]int8{-1, -2, -3}, 3):                  DTypeInt8,;
        FromValues([]int16{-1, -2, -3}, 3):                 DTypeInt16,;
        FromValues([]int32{-1, -2, -3}, 3):                 DTypeInt32,;
        FromValues([]long{-1, -2, -3}, 3):                 DTypeInt64,;
        FromValues([]float32{3.14, 2.71, 1.61}, 3):         DTypeFloat32,;
        FromValues([]double{3.14, 2.71, 1.61}, 3):         DTypeFloat64,;
        FromValues([]complex64{1 + 2i, 3 + 4i, 5 + 6i}, 3): DTypeComplex64,;
        } {
        t.Run(want.String(), func(t *testing.T) {
        if got.DType() != want {
        t.Errorf("want %v, got %v", want, got);
    }
        });
    }
    }
}
