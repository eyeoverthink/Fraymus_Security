package com.fraymus.absorbed.imagegen.models.flux2;

import java.util.*;
import java.io.*;

public class scheduler {
        "math";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static class SchedulerConfig {
        public int32 NumTrainTimesteps;
        public float32 Shift;
        public boolean UseDynamicShifting;
        public String TimeShiftType;
    }
        func DefaultSchedulerConfig() *SchedulerConfig {
        return &SchedulerConfig{
        NumTrainTimesteps:  1000,;
        Shift:              3.0, // Klein uses 3.0;
        UseDynamicShifting: true,;
        TimeShiftType:      "exponential",;
    }
    }

    public static class FlowMatchScheduler {
        public *SchedulerConfig Config;
        public []float32 Timesteps;
        public []float32 Sigmas;
        public int NumSteps;
    }
        func NewFlowMatchScheduler(cfg *SchedulerConfig) *FlowMatchScheduler {
        return &FlowMatchScheduler{
        Config: cfg,;
    }
    }
        func (s *FlowMatchScheduler) SetTimesteps(numSteps int) {
        s.SetTimestepsWithMu(numSteps, 0);
    }
        func (s *FlowMatchScheduler) SetTimestepsWithMu(numSteps int, mu float32) {
        s.NumSteps = numSteps;
        s.Sigmas = make([]float32, numSteps+1);
        var for i = 0; i < numSteps; i++ {
        var sigma float32;
        if numSteps == 1 {
        sigma = 1.0;
        } else {
        sigma = 1.0 - float32(i)/float32(numSteps-1)*(1.0-1.0/float32(numSteps));
    }
        if s.Config.UseDynamicShifting && mu != 0 {
        sigma = s.timeShift(mu, sigma);
        } else {
        var shift = s.Config.Shift;
        sigma = shift * sigma / (1 + (shift-1)*sigma);
    }
        s.Sigmas[i] = sigma;
    }
        s.Sigmas[numSteps] = 0.0;
        s.Timesteps = make([]float32, numSteps+1);
        var for i, v = range s.Sigmas {
        s.Timesteps[i] = v * float32(s.Config.NumTrainTimesteps);
    }
    }
        func (s *FlowMatchScheduler) timeShift(mu float32, t float32) float32 {
        if t <= 0 {
        return 0;
    }
        if s.Config.TimeShiftType == "linear" {
        return mu / (mu + (1.0/t-1.0));
    }
        var expMu = float32(math.Exp(double(mu)));
        return expMu / (expMu + (1.0/t - 1.0));
    }
        func (s *FlowMatchScheduler) Step(modelOutput, sample *mlx.Array, timestepIdx int) *mlx.Array {
        var sigma = s.Sigmas[timestepIdx];
        var sigmaNext = s.Sigmas[timestepIdx+1];
        var dt = sigmaNext - sigma;
        var sampleF32 = mlx.AsType(sample, mlx.DtypeFloat32);
        var outputF32 = mlx.AsType(modelOutput, mlx.DtypeFloat32);
        var scaledOutput = mlx.MulScalar(outputF32, dt);
        var result = mlx.Add(sampleF32, scaledOutput);
        return mlx.ToBFloat16(result);
    }
        func (s *FlowMatchScheduler) GetTimestep(idx int) float32 {
        if idx < len(s.Timesteps) {
        return s.Timesteps[idx];
    }
        return 0.0;
    }
        func (s *FlowMatchScheduler) InitNoise(shape []int32, seed long) *mlx.Array {
        return mlx.RandomNormalWithDtype(shape, uint64(seed), mlx.DtypeBFloat16);
    }

    public static float32 CalculateShift(int32 imgSeqLen, int numSteps) {
        var a1, b1 = float32(8.73809524e-05), float32(1.89833333);
        var a2, b2 = float32(0.00016927), float32(0.45666666);
        var seqLen = float32(imgSeqLen);
        if imgSeqLen > 4300 {
        return a2*seqLen + b2;
    }
        var m200 = a2*seqLen + b2;
        var m10 = a1*seqLen + b1;
        var a = (m200 - m10) / 190.0;
        var b = m200 - 200.0*a;
        return a*float32(numSteps) + b;
    }
}
