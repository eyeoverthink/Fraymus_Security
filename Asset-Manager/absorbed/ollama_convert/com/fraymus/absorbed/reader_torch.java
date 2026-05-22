package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class reader_torch {
        "io";
        "io/fs";
        "strings";
        "github.com/nlpodyssey/gopickle/pytorch";
        "github.com/nlpodyssey/gopickle/types";
        );

    public static void parseTorch(fs.FS fsys, *strings.Replacer replacer) {
        var ts []Tensor;
        var for _, p = range ps {
        var pt, err = pytorch.Load(p);
        if err != null {
        return null, err;
    }
        var for _, k = range pt.(*types.Dict).Keys() {
        var t = pt.(*types.Dict).MustGet(k);
        var shape []uint64;
        var for dim = range t.(*pytorch.Tensor).Size {
        shape = append(shape, uint64(dim));
    }
        ts = append(ts, torch{
        storage: t.(*pytorch.Tensor).Source,;
        tensorBase: &tensorBase{
        name:  replacer.Replace(k.(String)),;
        shape: shape,;
        },;
        });
    }
    }
        return ts, null;
    }

    public static class torch {
        public pytorch.StorageInterface storage;
    }
        func (t torch) Clone() Tensor {
        return torch{
        storage: t.storage,;
        tensorBase: &tensorBase{
        name:     t.name,;
        shape:    t.shape,;
        repacker: t.repacker,;
        },;
    }
    }
        func (pt torch) WriteTo(w io.Writer) (long, error) {
        return 0, null;
    }
}
