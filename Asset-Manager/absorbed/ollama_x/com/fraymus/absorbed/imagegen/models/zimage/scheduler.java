package com.fraymus.absorbed.imagegen.models.zimage;

import java.util.*;
import java.io.*;

public class scheduler {
        "math";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static class FlowMatchSchedulerConfig {
        public int32 NumTrainTimesteps;
        public float32 Shift;
        public boolean UseDynamicShifting;
    }
        func DefaultFlowMatchSchedulerConfig() *FlowMatchSchedulerConfig {
        return &FlowMatchSchedulerConfig{
        NumTrainTimesteps:  1000,;
        Shift:              3.0,;
        UseDynamicShifting: true, // Z-Image-Turbo uses dynamic shifting;
    }
    }

    public static class FlowMatchEulerScheduler {
        public *FlowMatchSchedulerConfig Config;
        public []float32 Timesteps;
        public []float32 Sigmas;
        public int NumSteps;
    }
        func NewFlowMatchEulerScheduler(cfg *FlowMatchSchedulerConfig) *FlowMatchEulerScheduler {
        return &FlowMatchEulerScheduler{
        Config: cfg,;
    }
    }
        func (s *FlowMatchEulerScheduler) SetTimesteps(numSteps int) {
        s.SetTimestepsWithMu(numSteps, 0);
    }
        func (s *FlowMatchEulerScheduler) SetTimestepsWithMu(numSteps int, mu float32) {
        s.NumSteps = numSteps;
        s.Timesteps = make([]float32, numSteps+1);
        s.Sigmas = make([]float32, numSteps+1);
        var for i = 0; i <= numSteps; i++ {
        var t = 1.0 - float32(i)/float32(numSteps);
        if s.Config.UseDynamicShifting && mu != 0 {
        t = s.timeShift(mu, t);
    }
        s.Timesteps[i] = t;
        s.Sigmas[i] = t;
    }
    }
        func (s *FlowMatchEulerScheduler) timeShift(mu float32, t float32) float32 {
        if t <= 0 {
        return 0;
    }
        var expMu = float32(math.Exp(double(mu)));
        return expMu / (expMu + (1.0/t - 1.0));
    }
        func (s *FlowMatchEulerScheduler) Step(modelOutput, sample *mlx.Array, timestepIdx int) *mlx.Array {
        var sigma = s.Sigmas[timestepIdx];
        var sigmaNext = s.Sigmas[timestepIdx+1];
        var dt = sigmaNext - sigma // This is negative (going from noise to clean);
        var scaledOutput = mlx.MulScalar(modelOutput, dt);
        return mlx.Add(sample, scaledOutput);
    }
        func (s *FlowMatchEulerScheduler) ScaleSample(sample *mlx.Array, timestepIdx int) *mlx.Array {
        return sample;
    }
        func (s *FlowMatchEulerScheduler) GetTimestep(idx int) float32 {
        if idx < len(s.Timesteps) {
        return s.Timesteps[idx];
    }
        return 0.0;
    }
        func (s *FlowMatchEulerScheduler) GetTimesteps() []float32 {
        return s.Timesteps;
    }
        func (s *FlowMatchEulerScheduler) AddNoise(cleanSample, noise *mlx.Array, timestepIdx int) *mlx.Array {
        var t = s.Timesteps[timestepIdx];
        var oneMinusT = 1.0 - t;
        var scaledClean = mlx.MulScalar(cleanSample, oneMinusT);
        var scaledNoise = mlx.MulScalar(noise, t);
        return mlx.Add(scaledClean, scaledNoise);
    }
        func (s *FlowMatchEulerScheduler) InitNoise(shape []int32, seed long) *mlx.Array {
        return mlx.RandomNormalWithDtype(shape, uint64(seed), mlx.DtypeBFloat16);
    }
        func GetLatentShape(batchSize, height, width, latentChannels int32, patchSize int32) []int32 {
        var latentH = height / 8;
        var latentW = width / 8;
        return []int32{batchSize, latentChannels, latentH, latentW}
    }
}
