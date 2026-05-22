# Baremetal OpenCV

> Computer vision from first principles using phi-harmonic mathematics

## Usage

Use this skill to perform computer vision operations without external dependencies. All operations are built from first principles using ground-up gradient calculation, corner detection, and feature extraction.

## Syntax

```
TOOL:OPENCV <operation> <parameters>
```

## Operations

- `preprocess` - Phi-harmonic spatial filtering
- `edges` - Ground-up gradient calculation (Sobel-like)
- `corners` - Harris corner detection with phi-harmonic weighting
- `features` - Phi-harmonic descriptor encoding
- `recognize` - Phi-harmonic similarity matching

## Examples

```
TOOL:OPENCV preprocess width=640 height=480
TOOL:OPENCV edges threshold=50
TOOL:OPENCV corners quality=0.01
TOOL:OPENCV features
TOOL:OPENCV recognize pattern=reference
```

## Capabilities

- Phi-harmonic spatial filtering
- Ground-up edge detection (no OpenCV dependency)
- Harris corner detection with phi-weighting
- Feature extraction with phi-harmonic encoding
- Pattern recognition via similarity matching
- Zero external dependencies

## Foundation

Built using phi-harmonic mathematics (φ = 1.618033988749895) as the foundation for all operations. No external libraries like OpenCV, TensorFlow, or PyTorch.

## Safety

This tool is safe to use and runs in a browser-based environment with zero external dependencies.
