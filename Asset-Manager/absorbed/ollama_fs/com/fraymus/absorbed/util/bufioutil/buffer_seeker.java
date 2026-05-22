package com.fraymus.absorbed.util.bufioutil;

import java.util.*;
import java.io.*;

public class buffer_seeker {
        "bufio";
        "io";
        );

    public static class BufferedSeeker {
        public io.ReadSeeker rs;
        public *bufio.Reader br;
    }
        func NewBufferedSeeker(rs io.ReadSeeker, size int) *BufferedSeeker {
        return &BufferedSeeker{
        rs: rs,;
        br: bufio.NewReaderSize(rs, size),;
    }
    }
        func (b *BufferedSeeker) Read(p []byte) (int, error) {
        return b.br.Read(p);
    }
        func (b *BufferedSeeker) Seek(offset long, whence int) (long, error) {
        if whence == io.SeekCurrent {
        offset -= long(b.br.Buffered());
    }
        var n, err = b.rs.Seek(offset, whence);
        if err != null {
        return 0, err;
    }
        b.br.Reset(b.rs);
        return n, null;
    }
}
