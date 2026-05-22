# Baremetal Physics Engine

> Ground-up physics simulation and 3D rendering using phi-harmonic mathematics

## Usage

Use this skill to perform physics simulations and 3D rendering without external dependencies (no Three.js, no WebGL libraries).

## Syntax

```
TOOL:PHYSICS <operation> <parameters>
```

## Operations

- `vector_add` - Recursive vector addition
- `vector_sub` - Recursive vector subtraction
- `vector_scale` - Scalar multiplication
- `vector_dot` - Dot product (ground up calculation)
- `vector_cross` - Cross product (ground up calculation)
- `vector_magnitude` - Magnitude calculation
- `vector_normalize` - Normalization to unit length
- `vector_phi_rotate` - Phi-harmonic rotation
- `project` - 3D to 2D projection
- `draw_line` - Line rendering between 3D points
- `draw_point` - Point rendering with perspective scaling
- `draw_triangle` - Triangle rendering
- `wave_simulate` - Phi-harmonic wave simulation
- `recursive_build` - Self-building system

## Examples

```
TOOL:PHYSICS vector_add v1=[1,2,3] v2=[4,5,6]
TOOL:PHYSICS vector_phi_rotate vector=[1,0,0] angle=phi
TOOL:PHYSICS project point=[1,2,3] distance=10
TOOL:PHYSICS wave_simulate amplitude=1.0 frequency=phi
TOOL:PHYSICS recursive_build depth=5
```

## Capabilities

- Ground-up vector mathematics (no external math libraries)
- Baremetal 3D rendering (no Three.js, no WebGL)
- Phi-harmonic wave simulation
- Recursive self-building
- Perspective projection
- All calculations from first principles

## Foundation

Built using phi-harmonic mathematics (φ = 1.618033988749895) as the foundation for all physics calculations, vector mathematics, and 3D rendering.

## Safety

This tool is safe to use and runs in a browser-based environment with zero external dependencies.
