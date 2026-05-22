# Binary Nervous System

> Raw signal processing with Hebbian learning using phi-harmonic mathematics

## Usage

Use this skill to process binary signals through a neural network with Hebbian learning ("neurons that fire together wire together").

## Syntax

```
TOOL:NERVOUS <operation> <parameters>
```

## Operations

- `create_neuron` - Create binary processing neuron with threshold
- `create_synapse` - Connect neurons with phi-harmonic weight
- `process` - Process binary signal through network
- `learn` - Hebbian learning (neurons that fire together wire together)
- `recognize` - Binary-level pattern matching

## Examples

```
TOOL:NERVOUS create_neuron id=1 threshold=0.5
TOOL:NERVOUS create_synapse from=1 to=2 weight=0.618
TOOL:NERVOUS process input=01001000...
TOOL:NERVOUS learn pattern=01001000...
TOOL:NERVOUS recognize input=01001000...
```

## Capabilities

- Raw binary signal processing
- Phi-harmonic signal modulation
- Hebbian learning (fire together, wire together)
- Threshold-based neuron activation
- Binary-level pattern recognition
- Self-organizing neural network

## Philosophy

The nervous system works with raw electrical signals, not high-level abstractions. Similarly, binary signals are the electrical impulses of computing. This system processes raw binary signals through neurons and synapses, using Hebbian learning to strengthen connections between co-activated neurons.

## Foundation

Built using phi-harmonic mathematics (φ = 1.618033988749895) as the foundation for all operations. Synaptic weights use phi-harmonic initialization.

## Safety

This tool is safe to use and runs in a browser-based environment with zero external dependencies.
