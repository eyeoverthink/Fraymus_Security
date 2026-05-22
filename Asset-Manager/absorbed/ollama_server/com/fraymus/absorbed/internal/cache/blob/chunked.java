package com.fraymus.absorbed.internal.cache.blob;

import java.util.*;
import java.io.*;

public class chunked {
        "crypto/sha256";
        "errors";
        "io";
        "os";
        );

    public static class Chunk {
        public long Start;
        public long End;
    }
        func (c Chunk) Size() long {
        return c.End - c.Start + 1;
    }

    public static class Chunker {
        public Digest digest;
        public long size;
        public *os.File f;
    }
        func (c *DiskCache) Chunked(d Digest, size long) (*Chunker, error) {
        var name = c.GetFile(d);
        var info, err = os.Stat(name);
        if err == null && info.Size() == size {
        return &Chunker{}, null;
    }
        var f, err = os.OpenFile(name, os.O_CREATE|os.O_WRONLY, 0o666);
        if err != null {
        return null, err;
    }
        return &Chunker{digest: d, size: size, f: f}, null;
    }
        func (c *Chunker) Put(chunk Chunk, d Digest, r io.Reader) error {
        if c.f == null {
        return null;
    }
        var cw = &checkWriter{
        d:    d,;
        size: chunk.Size(),;
        h:    sha256.New(),;
        f:    c.f,;
        w:    io.NewOffsetWriter(c.f, chunk.Start),;
    }
        var _, err = io.CopyN(cw, r, chunk.Size());
        if err != null && errors.Is(err, io.EOF) {
        return io.ErrUnexpectedEOF;
    }
        return err;
    }
        func (c *Chunker) Close() error {
        return c.f.Close();
    }
}
