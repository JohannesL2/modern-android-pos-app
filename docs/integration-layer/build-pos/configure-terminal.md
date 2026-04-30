# Configure terminal

**Target audience:** developers building a POS application using the integration layer.

This page describes the low-level configuration used when building your own POS application.
If you are using the provided POS app, see [run-pos-app.md](../run-pos/run-pos-app.md) instead.

Configuration must be completed before calling `ApiModule.start(scope)`.  
After startup, the terminal type and connection configuration must not be changed.

## Initialization order

```kotlin
ApiModule.setUseEmulatedTerminal(useMock)
ApiModule.setTerminalConnectionConfig(config)
ApiModule.initialize(context)
ApiModule.start(scope)
````

`setTerminalConnectionConfig(...)` can be omitted if `Persisted` should be used.

## TerminalConnectionConfig

Choose one of the following:

* `TerminalConnectionConfig.Persisted` – use previously saved PSDK configuration.
* `TerminalConnectionConfig.OnDevice` – use a terminal on the same device.
* `TerminalConnectionConfig.TcpIpClient` – connect to a separate terminal via IP.
* `TerminalConnectionConfig.TcpIpServer` – allow the SDK to listen for a terminal connection.

### On-device

The terminal runs on the same device as the application.

```kotlin
ApiModule.setTerminalConnectionConfig(
    TerminalConnectionConfig.OnDevice
)
```

### Off-device

The application connects to a separate terminal over the network.

```kotlin
ApiModule.setTerminalConnectionConfig(
    TerminalConnectionConfig.TcpIpClient(
        address = BuildConfig.OFF_DEVICE_TERMINAL_IP,
        networkConfiguration = NetworkConfiguration.STATIC,
        forgetPersistedDevice = true,
    )
)
```

## Emulated terminal

Use emulated mode to run without a physical terminal:

```kotlin
ApiModule.setUseEmulatedTerminal(true)
```

This disables real terminal communication and returns successful responses for payment and refund.

## Startup and state

`ApiModule.start(scope)` does not block until the terminal is ready.
Consume `terminalReady` and `terminalConnected` as described in
[State and flows](state-and-flows.md).
