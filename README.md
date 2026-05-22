# Fraymus Security

Security and cryptographic applications.

## Applications

- **Steganographer** - Steganographer - Hide Secrets in Images
- **VolatileString** - Volatile String - Secure Memory Management
- **HydraStorage** - Hydra Storage - Multi-Headed Redundant Storage
- **DeadMansSwitch** - Dead Man's Switch - Automated Response Protocol
- **RootScrambler** - Root Scrambler - File System Obfuscation
- **ContinuityNode** - Continuity Node - Distributed Backup

## Dependencies

- LWJGL 3.3.3 (OpenGL, GLFW, STB)
- ImGui Java 1.86.11
- JOML 1.10.5
- Minimal external dependencies

## Build

```bash
cd Asset-Manager
./gradlew build
```

## Run Security Apps

```bash
./gradlew runSteganographer   # Run Steganographer
./gradlew runVolatile        # Run Volatile String
./gradlew runHydra           # Run Hydra Storage
./gradlew runDeadManSwitch   # Run Dead Man's Switch
./gradlew runRootScrambler   # Run Root Scrambler
./gradlew runContinuity      # Run Continuity Node
```

## Requirements

- Java 21
- Gradle 9.2+
