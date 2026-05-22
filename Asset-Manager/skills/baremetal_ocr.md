# Baremetal OCR

> Text recognition from first principles using phi-harmonic mathematics

## Usage

Use this skill to perform optical character recognition without external dependencies. All operations are built from first principles using phi-harmonic thresholding, segmentation, and character matching.

## Syntax

```
TOOL:OCR <operation> <parameters>
```

## Operations

- `binarize` - Phi-harmonic thresholding
- `segment` - Phi-harmonic character segmentation
- `match` - Phi-harmonic character matching
- `learn` - Self-learning character database
- `recognize` - Full text recognition pipeline

## Examples

```
TOOL:OCR recognize width=640 height=480
TOOL:OCR learn character=A
TOOL:OCR binarize threshold=128
TOOL:OCR segment
TOOL:OCR match database=default
```

## Capabilities

- Phi-harmonic image binarization
- Character segmentation with phi-weighting
- Self-learning character database
- Phi-harmonic character matching
- Full text recognition pipeline
- Zero external dependencies (no Tesseract, no OCR libraries)

## Foundation

Built using phi-harmonic mathematics (φ = 1.618033988749895) as the foundation for all operations. No external libraries like Tesseract or commercial OCR engines.

## Safety

This tool is safe to use and runs in a browser-based environment with zero external dependencies.
