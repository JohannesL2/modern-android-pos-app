# Status and Flows

**Target audience:** consumers of the integration layer.

This page describes how the application layer should read state from `TerminalApi`.
The method reference is available in [TerminalApi](terminal-api.md).

## terminalReady

```kotlin
val terminalReady: StateFlow<Boolean>
```

Use this as the primary condition for enabling terminal flows in the UI.
`true` indicates that the integration layer considers the terminal ready to accept new operations.

## terminalConnected

```kotlin
val terminalConnected: StateFlow<Boolean>
```

Represents connection status. `terminalReady` is stricter than `terminalConnected` and should be used for action controls.

## deviceInfo

```kotlin
val deviceInfo: StateFlow<DeviceInfo?>
```

Contains terminal, Payment App, PSDK, and merchant information when available.
The value is `null` until the integration layer has published data.

## logs

```kotlin
val logs: SharedFlow<String>
```

Log entries from the integration layer for debug views, support, and troubleshooting.

## scannedCode

```kotlin
val scannedCode: Flow<String>
```

Scanner results. Scanner initialization and usage are described in [Scanner](features/scanner.md).

## Example

```kotlin
val ready by terminalApi.terminalReady.collectAsState()
val connected by terminalApi.terminalConnected.collectAsState()
val deviceInfo by terminalApi.deviceInfo.collectAsState()
```

The UI should not query the PSDK directly for equivalent state.
