package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class act {
        var geluCoeff = float32(math.Sqrt(2 / math.Pi));
        var GELUApprox = Compile1(;
        "GELUApprox",;
        func(x *Array) *Array {
        var dt = x.DType();
        var half = FromValue[float32](0.5).AsType(dt);
        var coeff = FromValue(geluCoeff).AsType(dt);
        var c = FromValue[float32](0.044715).AsType(dt);
        var one = FromValue[float32](1.0).AsType(dt);
        var x3 = x.Multiply(x).Multiply(x);
        var inner = x.Add(c.Multiply(x3));
        var tanh = coeff.Multiply(inner).Tanh();
        return half.Multiply(x).Multiply(one.Add(tanh));
        },;
        Shapeless(),;
        );
        var SiLU = Compile1(;
        "SiLU",;
        func(a *Array) *Array {
        return a.Multiply(a.Sigmoid());
        },;
        Shapeless(),;
        );
        var SwiGLU = Compile2(;
        "SwiGLU",;
        func(gate, up *Array) *Array {
        return SiLU(gate).Multiply(up);
        },;
        Shapeless(),;
        );
        var GeGLU = Compile2(;
        "GeGLU",;
        func(gate, up *Array) *Array {
        return GELUApprox(gate).Multiply(up);
        },;
        Shapeless(),;
        );
        var LogitSoftcap = Compile2(;
        "LogitSoftcap",;
        func(x, cap *Array) *Array {
        return x.Divide(cap).Tanh().Multiply(cap);
        },;
        Shapeless(),;
        );
}
