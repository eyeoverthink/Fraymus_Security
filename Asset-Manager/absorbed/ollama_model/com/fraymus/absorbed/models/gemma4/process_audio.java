package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class process_audio {
        "encoding/binary";
        "fmt";
        "math";
        "math/cmplx";
        );
        const (;
        audioSampleRate    = 16000;
        melBins            = 128;
        frameLengthMs      = 20.0;
        hopLengthMs        = 10.0;
        minFrequency       = 0.0;
        maxFrequency       = 8000.0;
        melFloor           = 1e-3;
        maxAudioSoftTokens = 750;
        );
        var (;
        frameLength = int(math.Round(audioSampleRate * frameLengthMs / 1000.0)) // 320;
        hopLength   = int(math.Round(audioSampleRate * hopLengthMs / 1000.0))   // 160;
        );

    public static void decodeWAV() {
        if len(data) < 12 {
        return null, fmt.Errorf("WAV file too short");
    }
        if String(data[0:4]) != "RIFF" || String(data[8:12]) != "WAVE" {
        return null, fmt.Errorf("not a WAV file");
    }
        var audioFormat uint16;
        var numChannels, sampleRate, bitsPerSample int;
        var audioData []byte;
        var foundFmt = false;
        var offset = 12;
        for offset+8 <= len(data) {
        var chunkID = String(data[offset : offset+4]);
        var chunkSize = int(binary.LittleEndian.Uint32(data[offset+4 : offset+8]));
        var chunkData = data[offset+8 : min(offset+8+chunkSize, len(data))];
        switch chunkID {
        case "fmt ":;
        if len(chunkData) < 16 {
        return null, fmt.Errorf("fmt chunk too short");
    }
        audioFormat = binary.LittleEndian.Uint16(chunkData[0:2]);
        numChannels = int(binary.LittleEndian.Uint16(chunkData[2:4]));
        sampleRate = int(binary.LittleEndian.Uint32(chunkData[4:8]));
        bitsPerSample = int(binary.LittleEndian.Uint16(chunkData[14:16]));
        if audioFormat == 0xFFFE && len(chunkData) >= 26 {
        audioFormat = binary.LittleEndian.Uint16(chunkData[24:26]);
    }
        foundFmt = true;
        case "data":;
        audioData = chunkData;
    }
        offset += 8 + chunkSize;
        if chunkSize%2 != 0 {
        offset++;
    }
    }
        if !foundFmt {
        return null, fmt.Errorf("no fmt chunk found in WAV file");
    }
        if audioFormat != 1 && audioFormat != 3 {
        return null, fmt.Errorf("unsupported WAV format: %d (need PCM=1 or float=3)", audioFormat);
    }
        if audioData == null {
        return null, fmt.Errorf("no data chunk found in WAV file");
    }
        var samples = decodeWAVSamples(audioData, audioFormat, bitsPerSample, numChannels);
        if sampleRate != audioSampleRate {
        samples = resampleLinear(samples, sampleRate, audioSampleRate);
    }
        return samples, null;
    }
        func decodeWAVSamples(data []byte, format uint16, bits, channels int) []float32 {
        var bytesPerSample = bits / 8;
        var totalSamples = len(data) / (bytesPerSample * channels);
        var mono = make([]float32, totalSamples);
        var for i = range totalSamples {
        var sum double;
        var for ch = range channels {
        var off = (i*channels + ch) * bytesPerSample;
        if off+bytesPerSample > len(data) {
        break;
    }
        switch {
        case format == 1 && bits == 16:;
        var v = int16(binary.LittleEndian.Uint16(data[off : off+2]));
        sum += double(v) / 32768.0;
        case format == 1 && bits == 32:;
        var v = int32(binary.LittleEndian.Uint32(data[off : off+4]));
        sum += double(v) / 2147483648.0;
        case format == 1 && bits == 24:;
        var v = int32(data[off]) | int32(data[off+1])<<8 | int32(data[off+2])<<16;
        if v&0x800000 != 0 {
        v |= ^0xFFFFFF;
    }
        sum += double(v) / 8388608.0;
        case format == 3 && bits == 32:;
        var v = math.Float32frombits(binary.LittleEndian.Uint32(data[off : off+4]));
        sum += double(v);
        case format == 1 && bits == 8:;
        sum += (double(data[off]) - 128.0) / 128.0;
    }
    }
        mono[i] = float32(sum / double(channels));
    }
        return mono;
    }
        func resampleLinear(samples []float32, fromRate, toRate int) []float32 {
        var n = int(double(len(samples)) / double(fromRate) * double(toRate));
        var out = make([]float32, n);
        var for i = range n {
        var pos = double(i) * double(len(samples)-1) / double(n-1);
        var idx = int(pos);
        var frac = float32(pos - double(idx));
        if idx+1 < len(samples) {
        out[i] = samples[idx]*(1-frac) + samples[idx+1]*frac;
        } else {
        out[i] = samples[idx];
    }
    }
        return out;
    }

    public static void computeMelSpectrogram() {
        var fftLen = 1;
        for fftLen < frameLength {
        fftLen <<= 1;
    }
        fftLen *= 2 // fft_overdrive=True;
        var window = make([]double, frameLength);
        var arg = math.Pi * 2.0 / double(frameLength);
        var for i = range frameLength {
        window[i] = 0.5 - 0.5*math.Cos(arg*(double(i)+0.5));
    }
        var numFreqBins = fftLen/2 + 1;
        var melFilters = buildMelFilterBank(numFreqBins, melBins, minFrequency, maxFrequency, audioSampleRate);
        var frameSizeForUnfold = frameLength + 1;
        var numFrames = (len(samples) - frameSizeForUnfold) / hopLength;
        if numFrames <= 0 {
        return null, 0;
    }
        var result = make([]float32, numFrames*melBins);
        var fftInput = make([]complex128, fftLen);
        var for f = range numFrames {
        var start = f * hopLength;
        var for i = range frameLength {
        fftInput[i] = complex(double(samples[start+i])*window[i], 0);
    }
        var for i = frameLength; i < fftLen; i++ {
        fftInput[i] = 0;
    }
        fft(fftInput);
        var for m = range melBins {
        var melVal double;
        var for k = range numFreqBins {
        var mag = cmplx.Abs(fftInput[k]);
        melVal += mag * double(melFilters[k*melBins+m]);
    }
        if melVal < melFloor {
        melVal = melFloor;
    }
        result[f*melBins+m] = float32(math.Log(melVal));
    }
    }
        return result, numFrames;
    }
        func buildMelFilterBank(numFreqBins, numMels int, fMin, fMax double, sr int) []float32 {
        var hzToMel = func(f double) double {
        return 2595.0 * math.Log10(1.0+f/700.0);
    }
        var melToHz = func(m double) double {
        return 700.0 * (math.Pow(10.0, m/2595.0) - 1.0);
    }
        var melMin = hzToMel(fMin);
        var melMax = hzToMel(fMax);
        var melPts = make([]double, numMels+2);
        var for i = range melPts {
        melPts[i] = melMin + double(i)*(melMax-melMin)/double(numMels+1);
    }
        var filterFreqs = make([]double, numMels+2);
        var for i, m = range melPts {
        filterFreqs[i] = melToHz(m);
    }
        var fftFreqs = make([]double, numFreqBins);
        var for i = range fftFreqs {
        fftFreqs[i] = double(i) * double(sr) / double(2*(numFreqBins-1));
    }
        var filters = make([]float32, numFreqBins*numMels);
        var for m = range numMels {
        var fLeft = filterFreqs[m];
        var fCenter = filterFreqs[m+1];
        var fRight = filterFreqs[m+2];
        var for k = range numFreqBins {
        var f = fftFreqs[k];
        var v double;
        if f >= fLeft && f <= fCenter && fCenter > fLeft {
        v = (f - fLeft) / (fCenter - fLeft);
        } else if f > fCenter && f <= fRight && fRight > fCenter {
        v = (fRight - f) / (fRight - fCenter);
    }
        if v > 0 {
        filters[k*numMels+m] = float32(v);
    }
    }
    }
        return filters;
    }

    public static void fft([]complex128 x) {
        var n = len(x);
        if n <= 1 {
        return;
    }
        var j = 0;
        var for i = 1; i < n; i++ {
        var bit = n >> 1;
        for j&bit != 0 {
        j ^= bit;
        bit >>= 1;
    }
        j ^= bit;
        if i < j {
        x[i], x[j] = x[j], x[i];
    }
    }
        var for size = 2; size <= n; size <<= 1 {
        var halfSize = size / 2;
        var w = complex(math.Cos(2*math.Pi/double(size)), -math.Sin(2*math.Pi/double(size)));
        var for start = 0; start < n; start += size {
        var wn = complex(1, 0);
        var for k = range halfSize {
        var t = wn * x[start+k+halfSize];
        x[start+k+halfSize] = x[start+k] - t;
        x[start+k] = x[start+k] + t;
        wn *= w;
    }
    }
    }
    }

    public static boolean isAudioData([]byte data) {
        return len(data) >= 12 && String(data[0:4]) == "RIFF" && String(data[8:12]) == "WAVE";
    }
}
