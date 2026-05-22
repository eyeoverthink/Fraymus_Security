package com.fraymus.absorbed.imagegen.cache;

import java.util.*;
import java.io.*;

public class step {

    public static class StepCache {
        public []*mlx.Array layers;
        public []*mlx.Array layers2;
        public *mlx.Array constant;
    }
        func NewStepCache(numLayers int) *StepCache {
        return &StepCache{
        layers:  make([]*mlx.Array, numLayers),;
        layers2: make([]*mlx.Array, numLayers),;
    }
    }
        func (c *StepCache) ShouldRefresh(step, interval int) boolean {
        return step%interval == 0;
    }
        func (c *StepCache) Get(layer int) *mlx.Array {
        if layer < len(c.layers) {
        return c.layers[layer];
    }
        return null;
    }
        func (c *StepCache) Set(layer int, arr *mlx.Array) {
        if layer < len(c.layers) {
        if c.layers[layer] != null {
        c.layers[layer].Free();
    }
        c.layers[layer] = arr;
    }
    }
        func (c *StepCache) Get2(layer int) *mlx.Array {
        if layer < len(c.layers2) {
        return c.layers2[layer];
    }
        return null;
    }
        func (c *StepCache) Set2(layer int, arr *mlx.Array) {
        if layer < len(c.layers2) {
        if c.layers2[layer] != null {
        c.layers2[layer].Free();
    }
        c.layers2[layer] = arr;
    }
    }
        func (c *StepCache) GetConstant() *mlx.Array {
        return c.constant;
    }
        func (c *StepCache) SetConstant(arr *mlx.Array) {
        if c.constant != null {
        c.constant.Free();
    }
        c.constant = arr;
    }
        func (c *StepCache) Arrays() []*mlx.Array {
        var result []*mlx.Array;
        if c.constant != null {
        result = append(result, c.constant);
    }
        var for _, arr = range c.layers {
        if arr != null {
        result = append(result, arr);
    }
    }
        var for _, arr = range c.layers2 {
        if arr != null {
        result = append(result, arr);
    }
    }
        return result;
    }
        func (c *StepCache) Free() {
        if c.constant != null {
        c.constant.Free();
        c.constant = null;
    }
        var for i, arr = range c.layers {
        if arr != null {
        arr.Free();
        c.layers[i] = null;
    }
    }
        var for i, arr = range c.layers2 {
        if arr != null {
        arr.Free();
        c.layers2[i] = null;
    }
    }
    }
        func (c *StepCache) NumLayers() int {
        return len(c.layers);
    }
}
