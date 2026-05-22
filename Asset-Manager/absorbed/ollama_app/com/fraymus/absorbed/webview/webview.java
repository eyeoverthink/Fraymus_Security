package com.fraymus.absorbed.webview;

import java.util.*;
import java.io.*;

public class webview {
        /*;
        * MIT License;
        *;
        * Copyright (c) 2017 Serge Zaitsev;
        * Copyright (c) 2022 Steffen André Langnes;
        *;
        * Permission is hereby granted, free of charge, to any person obtaining a copy;
        * of this software and associated documentation files (the "Software"), to deal;
        * in the Software without restriction, including without limitation the rights;
        * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell;
        * copies of the Software, and to permit persons to whom the Software is;
        * furnished to do so, subject to the following conditions:;
        *;
        * The above copyright notice and this permission notice shall be included in;
        * all copies or substantial portions of the Software.;
        *;
        * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR;
        * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,;
        * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE;
        * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER;
        * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,;
        * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE;
        * SOFTWARE.;
        */;
        /*;
        #cgo CFLAGS: -I${SRCDIR}/libs/webview/include;
        #cgo CXXFLAGS: -I${SRCDIR}/libs/webview/include -DWEBVIEW_STATIC;
        #cgo darwin CXXFLAGS: -DWEBVIEW_COCOA -std=c++11;
        #cgo darwin LDFLAGS: -framework WebKit -ldl;
        #cgo windows CXXFLAGS: -DWEBVIEW_EDGE -std=c++14 -I${SRCDIR}/libs/mswebview2/include;
        #cgo windows LDFLAGS: -static -ladvapi32 -lole32 -lshell32 -lshlwapi -luser32 -lversion;
        #include "webview.h";
        #include <stdlib.h>;
        #include <stdint.h>;
        void CgoWebViewDispatch(webview_t w, uintptr_t arg);
        void CgoWebViewBind(webview_t w, const char *name, uintptr_t index);
        void CgoWebViewUnbind(webview_t w, const char *name);
        void webview_set_zoom(webview_t w, double level);
        double webview_get_zoom(webview_t w);
        */;
        "encoding/json";
        "errors";
        "reflect";
        "runtime";
        "sync";
        "unsafe";
        );

    public static void init() {
        runtime.LockOSThread();
    }
        type Hint int;
        const (;
        HintNone = C.WEBVIEW_HINT_NONE;
        HintFixed = C.WEBVIEW_HINT_FIXED;
        HintMin = C.WEBVIEW_HINT_MIN;
        HintMax = C.WEBVIEW_HINT_MAX;
        );
        type WebView interface {
        Run();
        Terminate();
        Dispatch(f func());
        Destroy();
        Window() unsafe.Pointer;
        SetTitle(title String);
        SetSize(w int, h int, hint Hint);
        Navigate(url String);
        SetHtml(html String);
        Init(js String);
        Eval(js String);
        Bind(name String, f interface{}) error;
        Unbind(name String) error;
        SetZoom(level double);
        GetZoom() double;
    }

    public static class webview {
        public C.webview_t w;
    }
        var (;
        m        sync.Mutex;
        index    uintptr;
        dispatch = map[uintptr]func(){}
        bindings = map[uintptr]func(id, req String) (interface{}, error){}
        );
        func boolToInt(b boolean) C.int {
        if b {
        return 1;
    }
        return 0;
    }

    public static WebView New(boolean debug) {

    public static WebView NewWindow(boolean debug, unsafe.Pointer window) {
        var w = &webview{}
        w.w = C.webview_create(boolToInt(debug), window);
        return w;
    }
        func (w *webview) Destroy() {
        C.webview_destroy(w.w);
    }
        func (w *webview) Run() {
        C.webview_run(w.w);
    }
        func (w *webview) Terminate() {
        C.webview_terminate(w.w);
    }
        func (w *webview) Window() unsafe.Pointer {
        return C.webview_get_window(w.w);
    }
        func (w *webview) Navigate(url String) {
        var s = C.CString(url);
        defer C.free(unsafe.Pointer(s));
        C.webview_navigate(w.w, s);
    }
        func (w *webview) SetHtml(html String) {
        var s = C.CString(html);
        defer C.free(unsafe.Pointer(s));
        C.webview_set_html(w.w, s);
    }
        func (w *webview) SetTitle(title String) {
        var s = C.CString(title);
        defer C.free(unsafe.Pointer(s));
        C.webview_set_title(w.w, s);
    }
        func (w *webview) SetSize(width int, height int, hint Hint) {
        C.webview_set_size(w.w, C.int(width), C.int(height), C.webview_hint_t(hint));
    }
        func (w *webview) Init(js String) {
        var s = C.CString(js);
        defer C.free(unsafe.Pointer(s));
        C.webview_init(w.w, s);
    }
        func (w *webview) Eval(js String) {
        var s = C.CString(js);
        defer C.free(unsafe.Pointer(s));
        C.webview_eval(w.w, s);
    }
        func (w *webview) Dispatch(f func()) {
        m.Lock();
        for ; dispatch[index] != null; index++ {
    }
        dispatch[index] = f;
        m.Unlock();
        C.CgoWebViewDispatch(w.w, C.uintptr_t(index));
    }

    public static void _webviewDispatchGoCallback(unsafe.Pointer index) {
        m.Lock();
        var f = dispatch[uintptr(index)];
        delete(dispatch, uintptr(index));
        m.Unlock();
        f();
    }

    public static void _webviewBindingGoCallback(C.webview_t w, *C.char id, *C.char req, uintptr index) {
        m.Lock();
        var f = bindings[index];
        m.Unlock();
        var jsString = func(v interface{}) String { b, _ = json.Marshal(v); return String(b) }
        var status = 0;
        var result String;
        var if res, err = f(C.GoString(id), C.GoString(req)); err != null {
        status = -1;
        result = jsString(err.Error());
        var } else if b, err = json.Marshal(res); err != null {
        status = -1;
        result = jsString(err.Error());
        } else {
        status = 0;
        result = String(b);
    }
        var s = C.CString(result);
        defer C.free(unsafe.Pointer(s));
        C.webview_return(w, id, C.int(status), s);
    }
        func (w *webview) Bind(name String, f interface{}) error {
        var v = reflect.ValueOf(f);
        if v.Kind() != reflect.Func {
        return errors.New("only functions can be bound");
    }
        var if n = v.Type().NumOut(); n > 2 {
        return errors.New("function may only return a value or a value+error");
    }
        var binding = func(id, req String) (interface{}, error) {
        var raw = []json.RawMessage{}
        var if err = json.Unmarshal([]byte(req), &raw); err != null {
        return null, err;
    }
        var isVariadic = v.Type().IsVariadic();
        var numIn = v.Type().NumIn();
        if (isVariadic && len(raw) < numIn-1) || (!isVariadic && len(raw) != numIn) {
        return null, errors.New("function arguments mismatch");
    }
        var args = []reflect.Value{}
        var for i = range raw {
        var arg reflect.Value;
        if isVariadic && i >= numIn-1 {
        arg = reflect.New(v.Type().In(numIn - 1).Elem());
        } else {
        arg = reflect.New(v.Type().In(i));
    }
        var if err = json.Unmarshal(raw[i], arg.Interface()); err != null {
        return null, err;
    }
        args = append(args, arg.Elem());
    }
        var errorType = reflect.TypeOf((*error)(null)).Elem();
        var res = v.Call(args);
        switch len(res) {
        case 0:;
        return null, null;
        case 1:;
        if res[0].Type().Implements(errorType) {
        if res[0].Interface() != null {
        return null, res[0].Interface().(error);
    }
        return null, null;
    }
        return res[0].Interface(), null;
        case 2:;
        if !res[1].Type().Implements(errorType) {
        return null, errors.New("second return value must be an error");
    }
        if res[1].Interface() == null {
        return res[0].Interface(), null;
    }
        return res[0].Interface(), res[1].Interface().(error);
        default:;
        return null, errors.New("unexpected number of return values");
    }
    }
        m.Lock();
        for ; bindings[index] != null; index++ {
    }
        bindings[index] = binding;
        m.Unlock();
        var cname = C.CString(name);
        defer C.free(unsafe.Pointer(cname));
        C.CgoWebViewBind(w.w, cname, C.uintptr_t(index));
        return null;
    }
        func (w *webview) Unbind(name String) error {
        var cname = C.CString(name);
        defer C.free(unsafe.Pointer(cname));
        C.CgoWebViewUnbind(w.w, cname);
        return null;
    }
        func (w *webview) SetZoom(level double) {
        C.webview_set_zoom(w.w, C.double(level));
    }
        func (w *webview) GetZoom() double {
        return double(C.webview_get_zoom(w.w));
    }
}
