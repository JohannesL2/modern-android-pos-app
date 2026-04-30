---
title: Setup
---

# Setup to run POS app

**Target audience:** developers who want to run the existing POS application.

This page describes how to install required SDKs and prepare the project.

## Overview

The project depends on external SDKs that must be installed manually:

- Verifone Payment SDK (PSDK)
- Epson ePOS SDK (for off-device printing)

## 1. Verifone Payment SDK (PSDK)

1. Download the SDK (`.aar` file) from Verifone
2. Place it in:

```
/app/libs
```

## 2. Epson ePOS SDK

Required only for off-device printing.

1. Download SDK
2. Add to project
3. Add dependency in app module `libs`

## 3. Quick configuration
You can configure the following flags in `build.gradle.kts`:

```kotlin
USE_EMULATED_TERMINAL=true      // no physical terminal required
USE_LOCAL_TERMINAL=true         // on-device terminal
OFF_DEVICE_TERMINAL_IP="..."    // required if USE_LOCAL_TERMINAL=false
```

Common setups:
No hardware:
`USE_EMULATED_TERMINAL=true`
On-device terminal:
`USE_EMULATED_TERMINAL=false`
`USE_LOCAL_TERMINAL=true`
Off-device terminal:
`USE_EMULATED_TERMINAL=false`
`USE_LOCAL_TERMINAL=false`
set `OFF_DEVICE_TERMINAL_IP`

> These flags are a simplified configuration layer in the application layer.
> They internally map to `ApiModule.setTerminalConnectionConfig(...)`.

## 3. Build

```bash
./gradlew clean build
```