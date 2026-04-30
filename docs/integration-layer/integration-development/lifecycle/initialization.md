# Initialization

**Target audience:** developers working on the integration layer.

The public startup sequence is described in
[Quick start](../../build-pos/build-pos-app.md). This page explains what the implementation does.

## ApiModule.initialize

`initialize(context)` is idempotent.

For a physical terminal, `RuntimeProvider` is initialized with the `applicationContext`.
For an emulated terminal, the module skips physical runtime initialization.

## ApiModule.start

`start(scope)` is idempotent and starts the integration asynchronously.

For an emulated terminal:

* `MockTerminalApi` is created
* `startTerminal(...)` is executed within the provided scope

For a physical terminal:

* `TerminalApiFactory` constructs the object graph
* `TerminalApiImpl.startTerminal(config)` is executed within the provided scope
* `TerminalConnectionManager` receives the start result as connection state
* the Epson printer is initialized

## TerminalApiImpl.startTerminal

The startup sequence is:

1. store `TerminalConnectionConfig` in `TerminalConnectionManager`
2. `SdkRuntime.initialize(config)`
3. `SdkRuntime.awaitInitialized()`
4. `SdkRuntime.login()`
5. `SdkRuntime.emitDeviceInformation()`

Consumers should not call this method directly; see
[TerminalApi](../../build-pos/terminal-api.md).
