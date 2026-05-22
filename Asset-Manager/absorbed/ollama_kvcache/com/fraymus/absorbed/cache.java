package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cache {
        "errors";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );
        var (;
        ErrKvCacheFull  = errors.New("could not find a kv cache slot");
        ErrNotSupported = errors.New("model does not support operation");
        );
        type Cache interface {
        SetLayer(layer int);
        Get(ctx ml.Context) (ml.Tensor, ml.Tensor, ml.Tensor);
        Put(ctx ml.Context, key, value ml.Tensor);
        SetConfig(ml.CacheConfig);
        Init(backend ml.Backend, dtype ml.DType, maxSequences, capacity, maxBatch int);
        Close();
        StartForward(ctx ml.Context, batch input.Batch, reserve boolean) error;
        CopyPrefix(srcSeq, dstSeq int, len int32);
        CanResume(seq int, pos int32) boolean;
        Remove(seq int, beginIndex, endIndex int32) error;
    }
        type CheckpointCache interface {
        PrepareRestore(seq int, targetPos int32) (int32, boolean);
    }
}
