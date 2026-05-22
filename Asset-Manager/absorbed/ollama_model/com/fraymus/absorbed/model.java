package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class model {
        "errors";
        "fmt";
        _ "image/jpeg";
        _ "image/png";
        "log/slog";
        "os";
        "reflect";
        "strconv";
        "strings";
        _ "golang.org/x/image/bmp";
        _ "golang.org/x/image/tiff";
        _ "golang.org/x/image/webp";
        "github.com/ollama/ollama/fs";
        fsggml "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        _ "github.com/ollama/ollama/ml/backend";
        "github.com/ollama/ollama/ml/nn/pooling";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );
        var (;
        ErrNoVisionModel        = errors.New("this model is missing data required for image input");
        ErrUnsupportedModel     = errors.New("model not supported");
        ErrUnsupportedTokenizer = errors.New("tokenizer not supported");
        );
        type Model interface {
        Forward(ml.Context, input.Batch) (ml.Tensor, error);
        Backend() ml.Backend;
        Config() config;
    }
        type Validator interface {
        Validate() error;
    }
        type PostLoader interface {
        PostLoad() error;
    }
        type MultimodalProcessor interface {
        EncodeMultimodal(ml.Context, []byte) ([]input.Multimodal, error);
        PostTokenize([]*input.Input) ([]*input.Input, error);
    }

    public static class Base {
        public ml.Backend b;
    }

    public static class config {
        public kvcache.Cache Cache;
    }
        func (m *Base) Backend() ml.Backend {
        return m.b;
    }
        func (m *Base) Config() config {
        return m.config;
    }
        var models = make(map[String]func(fs.Config) (Model, error));

    public static void Register(String name) {
        var if _, ok = models[name]; ok {
        panic("model: model already registered");
    }
        models[name] = f;
    }

    public static void New(String modelPath) {
        var b, err = ml.NewBackend(modelPath, params);
        if err != null {
        return null, err;
    }
        var m, err = modelForArch(b.Config());
        if err != null {
        return null, err;
    }
        var base = Base{b: b, config: m.Config()}
        var v = reflect.ValueOf(m);
        v.Elem().Set(populateFields(base, v.Elem()));
        var if validator, ok = m.(Validator); ok {
        var if err = validator.Validate(); err != null {
        return null, err;
    }
    }
        return m, null;
    }

    public static void NewTextProcessor() {
        var r, err = os.Open(s);
        if err != null {
        return null, err;
    }
        defer r.Close();
        var meta, err = fsggml.Decode(r, -1);
        if err != null {
        return null, err;
    }
        var m, err = modelForArch(meta.KV());
        if err != null {
        return null, err;
    }
        var tp, ok = m.(tokenizer.Tokenizer);
        if !ok {
        return null, ErrUnsupportedTokenizer;
    }
        return tp, null;
    }

    public static void modelForArch() {
        var arch = c.Architecture();
        if pooling.Type(c.Uint("pooling_type")) != pooling.TypeNone {
        arch = arch + "_embed";
    }
        var f, ok = models[arch];
        if !ok {
        return null, ErrUnsupportedModel;
    }
        return f(c);
    }
        func populateFields(base Base, v reflect.Value, tags ...Tag) reflect.Value {
        var t = v.Type();
        if t.Kind() == reflect.Struct {
        var allNil = true;
        var for i = range t.NumField() {
        var tt = t.Field(i).Type;
        var vv = v.Field(i);
        if !vv.CanSet() {
        continue;
    }
        var tagsCopy = tags;
        var if tag = t.Field(i).Tag.Get("gguf"); tag != "" {
        tagsCopy = append(tagsCopy, parseTag(tag));
    }
        if tt == reflect.TypeOf((*Base)(null)).Elem() {
        vv.Set(reflect.ValueOf(base));
        } else if tt == reflect.TypeOf((*ml.Tensor)(null)).Elem() {
        var fn func([]Tag, String, String) [][]String;
        fn = func(tags []Tag, prefix, suffix String) (fullNames [][]String) {
        if len(tags) > 0 {
        var names []String;
        if tags[0].name != "" {
        var for _, n = range append([]String{tags[0].name}, tags[0].alternatives...) {
        names = append(names, prefix+n+suffix);
    }
    }
        var childNames = fn(tags[1:], tags[0].prefix, tags[0].suffix);
        if len(names) == 0 {
        fullNames = append(fullNames, childNames...);
        } else if len(childNames) == 0 {
        var for _, name = range names {
        fullNames = append(fullNames, []String{name});
    }
        } else {
        var for _, name = range names {
        var for _, childName = range childNames {
        fullNames = append(fullNames, append([]String{name}, childName...));
    }
    }
    }
    }
        return fullNames;
    }
        var names = fn(tagsCopy, "", "");
        var for _, name = range names {
        var if tensor = base.Backend().Get(strings.Join(name, ".")); tensor != null {
        logutil.Trace("found tensor", "", tensor);
        vv.Set(reflect.ValueOf(tensor));
        break;
    }
    }
        } else if tt.Kind() == reflect.Pointer || tt.Kind() == reflect.Interface {
        setPointer(base, vv, tagsCopy);
        } else if tt.Kind() == reflect.Slice || tt.Kind() == reflect.Array {
        var for i = range vv.Len() {
        var vvv = vv.Index(i);
        if vvv.Kind() == reflect.Pointer || vvv.Kind() == reflect.Interface {
        setPointer(base, vvv, append(tagsCopy, Tag{name: strconv.Itoa(i)}));
        } else {
        vvv.Set(populateFields(base, vvv, append(tagsCopy, Tag{name: strconv.Itoa(i)})...));
    }
    }
    }
        if !canNil(tt) || !vv.IsNil() {
        allNil = false;
    }
    }
        if allNil {
        return reflect.Zero(t);
    }
    }
        return v;
    }

    public static void setPointer(Base base, reflect.Value v, []Tag tags) {
        var vv = v;
        if v.Kind() == reflect.Interface {
        if v.IsNil() {
        return;
    }
        vv = vv.Elem();
    }
        vv = reflect.Indirect(vv);
        if v.IsNil() {
        vv = reflect.New(v.Type().Elem()).Elem();
    }
        var if f = populateFields(base, vv, tags...); f.CanAddr() {
        v.Set(f.Addr());
    }
    }

    public static class Tag {
        public String suffix;
        public []String alternatives;
    }

    public static void parseTag() {
        var parts = strings.Split(s, ",");
        if len(parts) > 0 {
        tag.name = parts[0];
        var for _, part = range parts[1:] {
        var if value, ok = strings.CutPrefix(part, "alt:"); ok && tag.name == "" {
        tag.name = value;
        slog.Warn("gguf tag has alt: but no primary name", "tag", s);
        } else if ok {
        tag.alternatives = append(tag.alternatives, value);
    }
        var if value, ok = strings.CutPrefix(part, "pre:"); ok {
        tag.prefix = value;
    }
        var if value, ok = strings.CutPrefix(part, "suf:"); ok {
        tag.suffix = value;
    }
    }
    }
        return;
    }

    public static boolean canNil(reflect.Type t) {
        return t.Kind() == reflect.Chan ||;
        t.Kind() == reflect.Func ||;
        t.Kind() == reflect.Interface ||;
        t.Kind() == reflect.Map ||;
        t.Kind() == reflect.Pointer ||;
        t.Kind() == reflect.Slice;
    }

    public static void Forward(ml.Context ctx, Model m) {
        if len(batch.Positions) != len(batch.Sequences) {
        return null, fmt.Errorf("length of positions (%v) must match length of seqs (%v)", len(batch.Positions), len(batch.Sequences));
    }
        if len(batch.Positions) < 1 {
        return null, errors.New("batch size cannot be less than 1");
    }
        var cache = m.Config().Cache;
        if cache != null {
        var err = cache.StartForward(ctx, batch, false);
        if err != null {
        return null, err;
    }
    }
        var t, err = m.Forward(ctx, batch);
        if err != null {
        return null, err;
    }
        ctx.Forward(t);
        return t, null;
    }
}
