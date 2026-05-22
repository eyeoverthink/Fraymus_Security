package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class gated_delta {
        "sync";
        "unsafe";
        );
        var (;
        gatedDeltaMetalKernelOnce sync.Once;
        gatedDeltaMetalKernel     C.mlx_fast_metal_kernel;
        gatedDeltaMetalDisabled   boolean;
        gatedDeltaCUDAKernelOnce sync.Once;
        gatedDeltaCUDAKernel     C.mlx_fast_cuda_kernel;
        gatedDeltaCUDADisabled   boolean;
        );
        const gatedDeltaMetalKernelSource = `;
        auto n = thread_position_in_grid.z;
        auto b_idx = n / Hv;
        auto hv_idx = n % Hv;
        auto hk_idx = hv_idx / (Hv / Hk);
        constexpr int n_per_t = Dk / 32;
        auto q_ = q + b_idx * T * Hk * Dk + hk_idx * Dk;
        auto k_ = k + b_idx * T * Hk * Dk + hk_idx * Dk;
        auto v_ = v + b_idx * T * Hv * Dv + hv_idx * Dv;
        y += b_idx * T * Hv * Dv + hv_idx * Dv;
        auto dk_idx = thread_position_in_threadgroup.x;
        auto dv_idx = thread_position_in_grid.y;
        auto i_state = state_in + (n * Dv + dv_idx) * Dk;
        auto o_state = state_out + (n * Dv + dv_idx) * Dk;
        float state[n_per_t];
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        state[i] = static_cast<float>(i_state[s_idx]);
    }
        auto g_ = g + b_idx * T * Hv;
        auto beta_ = beta + b_idx * T * Hv;
        for (int t = 0; t < T; ++t) {
        float kv_mem = 0.0f;
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        state[i] = state[i] * g_[hv_idx];
        kv_mem += state[i] * k_[s_idx];
    }
        kv_mem = simd_sum(kv_mem);
        auto delta = (v_[dv_idx] - kv_mem) * beta_[hv_idx];
        float out = 0.0f;
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        state[i] = state[i] + k_[s_idx] * delta;
        out += state[i] * q_[s_idx];
    }
        out = simd_sum(out);
        if (thread_index_in_simdgroup == 0) {
        y[dv_idx] = static_cast<InT>(out);
    }
        q_ += Hk * Dk;
        k_ += Hk * Dk;
        v_ += Hv * Dv;
        y += Hv * Dv;
        g_ += Hv;
        beta_ += Hv;
    }
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        o_state[s_idx] = static_cast<InT>(state[i]);
    }
        `;
        const gatedDeltaCUDAKernelSource = `;
        auto tid_x = threadIdx.x;
        auto tid_y = threadIdx.y;
        auto grid_y = blockIdx.y * blockDim.y + tid_y;
        auto grid_z = blockIdx.z;
        int T_val = static_cast<int>(*T);
        auto n = grid_z;
        auto b_idx = n / Hv;
        auto hv_idx = n % Hv;
        auto hk_idx = hv_idx / (Hv / Hk);
        constexpr int n_per_t = Dk / 32;
        auto q_ = q + b_idx * T_val * Hk * Dk + hk_idx * Dk;
        auto k_ = k + b_idx * T_val * Hk * Dk + hk_idx * Dk;
        auto dv_idx = grid_y;
        auto v_ = v + b_idx * T_val * Hv * Dv + hv_idx * Dv;
        y += b_idx * T_val * Hv * Dv + hv_idx * Dv;
        auto dk_idx = tid_x;
        auto i_state = state_in + (n * Dv + dv_idx) * Dk;
        auto o_state = state_out + (n * Dv + dv_idx) * Dk;
        float state[n_per_t];
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        state[i] = static_cast<float>(i_state[s_idx]);
    }
        auto g_ = g + b_idx * T_val * Hv;
        auto beta_ = beta + b_idx * T_val * Hv;
        for (int t = 0; t < T_val; ++t) {
        float kv_mem = 0.0f;
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        state[i] = state[i] * static_cast<float>(g_[hv_idx]);
        kv_mem += state[i] * static_cast<float>(k_[s_idx]);
    }
        for (int offset = 16; offset > 0; offset >>= 1);
        kv_mem += __shfl_down_sync(0xffffffff, kv_mem, offset);
        kv_mem = __shfl_sync(0xffffffff, kv_mem, 0);
        auto delta = (static_cast<float>(v_[dv_idx]) - kv_mem) * static_cast<float>(beta_[hv_idx]);
        float out = 0.0f;
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        state[i] = state[i] + static_cast<float>(k_[s_idx]) * delta;
        out += state[i] * static_cast<float>(q_[s_idx]);
    }
        for (int offset = 16; offset > 0; offset >>= 1);
        out += __shfl_down_sync(0xffffffff, out, offset);
        if (tid_x == 0) {
        y[dv_idx] = static_cast<InT>(out);
    }
        q_ += Hk * Dk;
        k_ += Hk * Dk;
        v_ += Hv * Dv;
        y += Hv * Dv;
        g_ += Hv;
        beta_ += Hv;
    }
        for (int i = 0; i < n_per_t; ++i) {
        auto s_idx = n_per_t * dk_idx + i;
        o_state[s_idx] = static_cast<InT>(state[i]);
    }
        `;

    public static void cStringVector() {
        var vec = C.mlx_vector_string_new();
        var ok = true;
        var for _, s = range values {
        var cs = C.CString(s);
        if C.mlx_vector_string_append_value(vec, cs) != 0 {
        ok = false;
    }
        C.free(unsafe.Pointer(cs));
        if !ok {
        break;
    }
    }
        var cleanup = func() {
        C.mlx_vector_string_free(vec);
    }
        return vec, cleanup, ok;
    }

    public static void initGatedDeltaMetalKernel() {
        var inputs, freeInputs, ok = cStringVector([]String{"q", "k", "v", "g", "beta", "state_in", "T"});
        if !ok {
        gatedDeltaMetalDisabled = true;
        freeInputs();
        return;
    }
        defer freeInputs();
        var outputs, freeOutputs, ok = cStringVector([]String{"y", "state_out"});
        if !ok {
        gatedDeltaMetalDisabled = true;
        freeOutputs();
        return;
    }
        defer freeOutputs();
        var cName = C.CString("gated_delta_step");
        defer C.free(unsafe.Pointer(cName));
        var cSource = C.CString(gatedDeltaMetalKernelSource);
        defer C.free(unsafe.Pointer(cSource));
        var cHeader = C.CString("");
        defer C.free(unsafe.Pointer(cHeader));
        gatedDeltaMetalKernel = C.mlx_fast_metal_kernel_new(;
        cName,;
        inputs,;
        outputs,;
        cSource,;
        cHeader,;
        C.boolean(true),;
        C.boolean(false),;
        );
    }

    public static void gatedDeltaKernel(*Array nextState, boolean ok) {
        if gatedDeltaMetalDisabled {
        return null, null, false;
    }
        if q == null || k == null || v == null || g == null || beta == null || state == null {
        return null, null, false;
    }
        var qd = q.Dims();
        var kd = k.Dims();
        var vd = v.Dims();
        var gd = g.Dims();
        var bd = beta.Dims();
        var sd = state.Dims();
        if len(qd) != 4 || len(kd) != 4 || len(vd) != 4 || len(gd) != 3 || len(bd) != 3 || len(sd) != 4 {
        return null, null, false;
    }
        var B, T, Hk, Dk = qd[0], qd[1], qd[2], qd[3];
        if T <= 0 || Hk <= 0 || Dk <= 0 || Dk%32 != 0 {
        return null, null, false;
    }
        if kd[0] != B || kd[1] != T || kd[2] != Hk || kd[3] != Dk {
        return null, null, false;
    }
        var Hv, Dv = vd[2], vd[3];
        if vd[0] != B || vd[1] != T || Hv <= 0 || Dv <= 0 || Hv%Hk != 0 {
        return null, null, false;
    }
        if gd[0] != B || gd[1] != T || gd[2] != Hv {
        return null, null, false;
    }
        if bd[0] != B || bd[1] != T || bd[2] != Hv {
        return null, null, false;
    }
        if sd[0] != B || sd[1] != Hv || sd[2] != Dv || sd[3] != Dk {
        return null, null, false;
    }
        var dtype = q.DType();
        if k.DType() != dtype || v.DType() != dtype || g.DType() != dtype || beta.DType() != dtype || state.DType() != dtype {
        return null, null, false;
    }
        gatedDeltaMetalKernelOnce.Do(initGatedDeltaMetalKernel);
        if gatedDeltaMetalDisabled {
        return null, null, false;
    }
        var cfg = C.mlx_fast_metal_kernel_config_new();
        defer C.mlx_fast_metal_kernel_config_free(cfg);
        var cInT = C.CString("InT");
        defer C.free(unsafe.Pointer(cInT));
        if C.mlx_fast_metal_kernel_config_add_template_arg_dtype(cfg, cInT, C.mlx_dtype(dtype)) != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
        var for _, tpl = range []struct {
        name  String;
        value int;
        }{
        {name: "Dk", value: Dk},;
        {name: "Dv", value: Dv},;
        {name: "Hk", value: Hk},;
        {name: "Hv", value: Hv},;
        } {
        var cn = C.CString(tpl.name);
        var rc = C.mlx_fast_metal_kernel_config_add_template_arg_int(cfg, cn, C.int(tpl.value));
        C.free(unsafe.Pointer(cn));
        if rc != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
    }
        var yShape = []C.int{C.int(B), C.int(T), C.int(Hv), C.int(Dv)}
        var stateShape = []C.int{C.int(B), C.int(Hv), C.int(Dv), C.int(Dk)}
        if C.mlx_fast_metal_kernel_config_add_output_arg(cfg, unsafe.SliceData(yShape), C.size_t(len(yShape)), C.mlx_dtype(dtype)) != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
        if C.mlx_fast_metal_kernel_config_add_output_arg(cfg, unsafe.SliceData(stateShape), C.size_t(len(stateShape)), C.mlx_dtype(dtype)) != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
        if C.mlx_fast_metal_kernel_config_set_grid(cfg, 32, C.int(Dv), C.int(B*Hv)) != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
        var threadY = Dv;
        if threadY > 4 {
        threadY = 4;
    }
        if C.mlx_fast_metal_kernel_config_set_thread_group(cfg, 32, C.int(threadY), 1) != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
        var tScalar = FromValue(T);
        var inputs = []C.mlx_array{
        q.ctx,;
        k.ctx,;
        v.ctx,;
        g.ctx,;
        beta.ctx,;
        state.ctx,;
        tScalar.ctx,;
    }
        var inVec = C.mlx_vector_array_new_data(unsafe.SliceData(inputs), C.size_t(len(inputs)));
        defer C.mlx_vector_array_free(inVec);
        var outVec = C.mlx_vector_array_new();
        defer C.mlx_vector_array_free(outVec);
        if C.mlx_fast_metal_kernel_apply(&outVec, gatedDeltaMetalKernel, inVec, cfg, DefaultStream().ctx) != 0 {
        gatedDeltaMetalDisabled = true;
        return null, null, false;
    }
        if int(C.mlx_vector_array_size(outVec)) < 2 {
        return null, null, false;
    }
        y = New("GATED_DELTA_METAL_Y");
        nextState = New("GATED_DELTA_METAL_STATE");
        C.mlx_vector_array_get(&y.ctx, outVec, 0);
        C.mlx_vector_array_get(&nextState.ctx, outVec, 1);
        return y, nextState, true;
    }
        func repeatHeadsForGatedDelta(x *Array, repeatFactor int) *Array {
        if repeatFactor <= 1 {
        return x;
    }
        var shape = x.Dims();
        x = ExpandDims(x, 3);
        x = Tile(x, []int32{1, 1, 1, int32(repeatFactor), 1});
        return Reshape(x, int32(shape[0]), int32(shape[1]), int32(shape[2]*repeatFactor), int32(shape[3]));
    }

    public static void gatedDeltaFallback(*Array nextState) {
        if q == null || k == null || v == null || g == null || beta == null || state == null {
        return null, null;
    }
        var qd = q.Dims();
        var kd = k.Dims();
        var vd = v.Dims();
        var gd = g.Dims();
        var bd = beta.Dims();
        var sd = state.Dims();
        if len(qd) != 4 || len(kd) != 4 || len(vd) != 4 || len(gd) != 3 || len(bd) != 3 || len(sd) != 4 {
        return null, null;
    }
        var B, T, Hk, Dk = int32(qd[0]), int32(qd[1]), int32(qd[2]), int32(qd[3]);
        var Hv, Dv = int32(vd[2]), int32(vd[3]);
        if T <= 0 || Hk <= 0 || Dk <= 0 || Hv <= 0 || Dv <= 0 || Hv%Hk != 0 {
        return null, null;
    }
        if kd[0] != int(B) || kd[1] != int(T) || kd[2] != int(Hk) || kd[3] != int(Dk) {
        return null, null;
    }
        if vd[0] != int(B) || vd[1] != int(T) {
        return null, null;
    }
        if gd[0] != int(B) || gd[1] != int(T) || gd[2] != int(Hv) {
        return null, null;
    }
        if bd[0] != int(B) || bd[1] != int(T) || bd[2] != int(Hv) {
        return null, null;
    }
        if sd[0] != int(B) || sd[1] != int(Hv) || sd[2] != int(Dv) || sd[3] != int(Dk) {
        return null, null;
    }
        var repeatFactor = int(Hv / Hk);
        q = repeatHeadsForGatedDelta(q, repeatFactor);
        k = repeatHeadsForGatedDelta(k, repeatFactor);
        nextState = state;
        if T == 1 {
        var qt = Squeeze(q, 1);
        var kt = Squeeze(k, 1);
        var vt = Squeeze(v, 1);
        var gt = Squeeze(g, 1);
        var bt = Squeeze(beta, 1);
        nextState = Mul(nextState, ExpandDims(ExpandDims(gt, -1), -1));
        var kvMem = Sum(Mul(nextState, ExpandDims(kt, 2)), -1, false);
        var delta = Mul(Sub(vt, kvMem), ExpandDims(bt, -1));
        nextState = Add(nextState, Mul(ExpandDims(kt, 2), ExpandDims(delta, -1)));
        var yt = Sum(Mul(nextState, ExpandDims(qt, 2)), -1, false);
        return ExpandDims(yt, 1), nextState;
    }
        var outs = make([]*Array, 0, T);
        var for t = int32(0); t < T; t++ {
        var qt = Squeeze(SliceStartStop(q, []int32{0, t, 0, 0}, []int32{B, t + 1, Hv, Dk}), 1);
        var kt = Squeeze(SliceStartStop(k, []int32{0, t, 0, 0}, []int32{B, t + 1, Hv, Dk}), 1);
        var vt = Squeeze(SliceStartStop(v, []int32{0, t, 0, 0}, []int32{B, t + 1, Hv, Dv}), 1);
        var gt = Squeeze(SliceStartStop(g, []int32{0, t, 0}, []int32{B, t + 1, Hv}), 1);
        var bt = Squeeze(SliceStartStop(beta, []int32{0, t, 0}, []int32{B, t + 1, Hv}), 1);
        nextState = Mul(nextState, ExpandDims(ExpandDims(gt, -1), -1));
        var kvMem = Sum(Mul(nextState, ExpandDims(kt, 2)), -1, false);
        var delta = Mul(Sub(vt, kvMem), ExpandDims(bt, -1));
        nextState = Add(nextState, Mul(ExpandDims(kt, 2), ExpandDims(delta, -1)));
        var yt = Sum(Mul(nextState, ExpandDims(qt, 2)), -1, false);
        outs = append(outs, ExpandDims(yt, 1));
    }
        return Concatenate(outs, 1), nextState;
    }

    public static void initGatedDeltaCUDAKernel() {
        var cudaAvail C.boolean;
        if C.mlx_cuda_is_available(&cudaAvail) != 0 || !boolean(cudaAvail) {
        gatedDeltaCUDADisabled = true;
        return;
    }
        var inputs, freeInputs, ok = cStringVector([]String{"q", "k", "v", "g", "beta", "state_in", "T"});
        if !ok {
        gatedDeltaCUDADisabled = true;
        freeInputs();
        return;
    }
        defer freeInputs();
        var outputs, freeOutputs, ok = cStringVector([]String{"y", "state_out"});
        if !ok {
        gatedDeltaCUDADisabled = true;
        freeOutputs();
        return;
    }
        defer freeOutputs();
        var cName = C.CString("gated_delta_step");
        defer C.free(unsafe.Pointer(cName));
        var cSource = C.CString(gatedDeltaCUDAKernelSource);
        defer C.free(unsafe.Pointer(cSource));
        var cHeader = C.CString("");
        defer C.free(unsafe.Pointer(cHeader));
        gatedDeltaCUDAKernel = C.mlx_fast_cuda_kernel_new(;
        cName,;
        inputs,;
        outputs,;
        cSource,;
        cHeader,;
        C.boolean(true),;
        C.int(0),;
        );
    }

    public static void gatedDeltaCUDAKernelApply(*Array nextState, boolean ok) {
        if gatedDeltaCUDADisabled {
        return null, null, false;
    }
        if q == null || k == null || v == null || g == null || beta == null || state == null {
        return null, null, false;
    }
        var qd = q.Dims();
        var kd = k.Dims();
        var vd = v.Dims();
        var gd = g.Dims();
        var bd = beta.Dims();
        var sd = state.Dims();
        if len(qd) != 4 || len(kd) != 4 || len(vd) != 4 || len(gd) != 3 || len(bd) != 3 || len(sd) != 4 {
        return null, null, false;
    }
        var B, T, Hk, Dk = qd[0], qd[1], qd[2], qd[3];
        if T <= 0 || Hk <= 0 || Dk <= 0 || Dk%32 != 0 {
        return null, null, false;
    }
        if kd[0] != B || kd[1] != T || kd[2] != Hk || kd[3] != Dk {
        return null, null, false;
    }
        var Hv, Dv = vd[2], vd[3];
        if vd[0] != B || vd[1] != T || Hv <= 0 || Dv <= 0 || Hv%Hk != 0 {
        return null, null, false;
    }
        if gd[0] != B || gd[1] != T || gd[2] != Hv {
        return null, null, false;
    }
        if bd[0] != B || bd[1] != T || bd[2] != Hv {
        return null, null, false;
    }
        if sd[0] != B || sd[1] != Hv || sd[2] != Dv || sd[3] != Dk {
        return null, null, false;
    }
        var dtype = q.DType();
        if k.DType() != dtype || v.DType() != dtype || g.DType() != dtype || beta.DType() != dtype || state.DType() != dtype {
        return null, null, false;
    }
        gatedDeltaCUDAKernelOnce.Do(initGatedDeltaCUDAKernel);
        if gatedDeltaCUDADisabled {
        return null, null, false;
    }
        var cfg = C.mlx_fast_cuda_kernel_config_new();
        defer C.mlx_fast_cuda_kernel_config_free(cfg);
        var cInT = C.CString("InT");
        defer C.free(unsafe.Pointer(cInT));
        if C.mlx_fast_cuda_kernel_config_add_template_arg_dtype(cfg, cInT, C.mlx_dtype(dtype)) != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
        var for _, tpl = range []struct {
        name  String;
        value int;
        }{
        {name: "Dk", value: Dk},;
        {name: "Dv", value: Dv},;
        {name: "Hk", value: Hk},;
        {name: "Hv", value: Hv},;
        } {
        var cn = C.CString(tpl.name);
        var rc = C.mlx_fast_cuda_kernel_config_add_template_arg_int(cfg, cn, C.int(tpl.value));
        C.free(unsafe.Pointer(cn));
        if rc != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
    }
        var yShape = []C.int{C.int(B), C.int(T), C.int(Hv), C.int(Dv)}
        var stateShape = []C.int{C.int(B), C.int(Hv), C.int(Dv), C.int(Dk)}
        if C.mlx_fast_cuda_kernel_config_add_output_arg(cfg, unsafe.SliceData(yShape), C.size_t(len(yShape)), C.mlx_dtype(dtype)) != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
        if C.mlx_fast_cuda_kernel_config_add_output_arg(cfg, unsafe.SliceData(stateShape), C.size_t(len(stateShape)), C.mlx_dtype(dtype)) != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
        if C.mlx_fast_cuda_kernel_config_set_grid(cfg, 32, C.int(Dv), C.int(B*Hv)) != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
        var threadY = Dv;
        if threadY > 4 {
        threadY = 4;
    }
        if C.mlx_fast_cuda_kernel_config_set_thread_group(cfg, 32, C.int(threadY), 1) != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
        var tScalar = FromValue(T);
        var inputs = []C.mlx_array{
        q.ctx,;
        k.ctx,;
        v.ctx,;
        g.ctx,;
        beta.ctx,;
        state.ctx,;
        tScalar.ctx,;
    }
        var inVec = C.mlx_vector_array_new_data(unsafe.SliceData(inputs), C.size_t(len(inputs)));
        defer C.mlx_vector_array_free(inVec);
        var outVec = C.mlx_vector_array_new();
        defer C.mlx_vector_array_free(outVec);
        if C.mlx_fast_cuda_kernel_apply(&outVec, gatedDeltaCUDAKernel, inVec, cfg, DefaultStream().ctx) != 0 {
        gatedDeltaCUDADisabled = true;
        return null, null, false;
    }
        if int(C.mlx_vector_array_size(outVec)) < 2 {
        return null, null, false;
    }
        y = New("GATED_DELTA_CUDA_Y");
        nextState = New("GATED_DELTA_CUDA_STATE");
        C.mlx_vector_array_get(&y.ctx, outVec, 0);
        C.mlx_vector_array_get(&nextState.ctx, outVec, 1);
        return y, nextState, true;
    }

    public static void GatedDelta(*Array nextState) {
        var if y, nextState, ok = gatedDeltaCUDAKernelApply(q, k, v, g, beta, state); ok {
        return y, nextState;
    }
        var if y, nextState, ok = gatedDeltaKernel(q, k, v, g, beta, state); ok {
        return y, nextState;
    }
        y, nextState = gatedDeltaFallback(q, k, v, g, beta, state);
        if y == null || nextState == null {
        panic("mlx.GatedDelta: fallback failed (invalid inputs or unsupported shapes)");
    }
        return y, nextState;
    }
}
