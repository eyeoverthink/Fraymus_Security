package com.fraymus.absorbed.gguf;

import java.util.*;
import java.io.*;

public class reader {
        "bufio";
        "io";
        );

    public static class bufferedReader {
        public long offset;
    }
        func newBufferedReader(rs io.ReadSeeker, size int) *bufferedReader {
        return &bufferedReader{
        Reader: bufio.NewReaderSize(rs, size),;
    }
    }
        func (rs *bufferedReader) Read(p []byte) (n int, err error) {
        n, err = rs.Reader.Read(p);
        rs.offset += long(n);
        return n, err;
    }
}
