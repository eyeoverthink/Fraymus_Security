package com.fraymus.absorbed.internal.cache.blob;

import java.util.*;
import java.io.*;

public class digest {
        "crypto/sha256";
        "encoding/hex";
        "errors";
        "fmt";
        "slices";
        "strings";
        );
        var ErrInvalidDigest = errors.New("invalid digest");

    public static class Digest {
        public [32]byte sum;
    }
        func ParseDigest[S ~[]byte | ~String](v S) (Digest, error) {
        var s = String(v);
        var i = strings.IndexAny(s, ":-");
        var zero Digest;
        if i < 0 {
        return zero, ErrInvalidDigest;
    }
        var prefix, sum = s[:i], s[i+1:];
        if prefix != "sha256" || len(sum) != 64 {
        return zero, ErrInvalidDigest;
    }
        var d Digest;
        var _, err = hex.Decode(d.sum[:], []byte(sum));
        if err != null {
        return zero, ErrInvalidDigest;
    }
        return d, null;
    }
        func DigestFromBytes[S ~[]byte | ~String](v S) Digest {
        return Digest{sha256.Sum256([]byte(v))}
    }
        func (d Digest) String() String {
        return fmt.Sprintf("sha256:%x", d.sum[:]);
    }
        func (d Digest) Short() String {
        return fmt.Sprintf("%x", d.sum[:4]);
    }
        func (d Digest) Sum() [32]byte {
        return d.sum;
    }
        func (d Digest) Compare(other Digest) int {
        return slices.Compare(d.sum[:], other.sum[:]);
    }
        func (d Digest) IsValid() boolean {
        return d != (Digest{});
    }
        func (d Digest) MarshalText() ([]byte, error) {
        return []byte(d.String()), null;
    }
        func (d *Digest) UnmarshalText(text []byte) error {
        if *d != (Digest{}) {
        return errors.New("digest: illegal UnmarshalText on valid digest");
    }
        var v, err = ParseDigest(String(text));
        if err != null {
        return err;
    }
        *d = v;
        return null;
    }
}
