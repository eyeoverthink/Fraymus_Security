# Baremetal Audio

> Audio processing from first principles using phi-harmonic mathematics

## Usage

Use this skill to perform audio processing without external dependencies. All operations are built from first principles using ground-up FFT implementation and phi-harmonic frequency analysis.

## Syntax

```
TOOL:AUDIO <operation> <parameters>
```

## Operations

- `fft` - FFT from first principles (Cooley-Tukey algorithm)
- `fft_recursive` - Recursive FFT implementation
- `analyze` - Phi-harmonic frequency analysis
- `synthesize` - Phi-harmonic waveform generation
- `filter` - Phi-harmonic filtering (lowpass/highpass)

## Examples

```
TOOL:AUDIO fft samples=1024
TOOL:AUDIO analyze frequency=440
TOOL:AUDIO synthesize frequency=440 duration=1.0
TOOL:AUDIO filter type=lowpass cutoff=1000
TOOL:AUDIO fft_recursive depth=10
```

## Capabilities

- Ground-up FFT implementation (Cooley-Tukey)
- Recursive FFT for educational purposes
- Phi-harmonic frequency analysis
- Waveform synthesis with phi-modulation
- Phi-harmonic filtering
- Zero external dependencies (no librosa, no scipy)

## Foundation

Built using phi-harmonic mathematics (φ = 1.618033988749895) as the foundation for all operations. No external audio processing libraries.

## Safety

This tool is safe to use and runs in a browser-based environment with zero external dependencies.
