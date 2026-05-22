package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class random {
        func (t *Array) Categorical(axis int) *Array {
        var key = New("");
        var out = New("");
        C.mlx_random_categorical(&out.ctx, t.ctx, C.int(axis), key.ctx, DefaultStream().ctx);
        return out;
    }
}
