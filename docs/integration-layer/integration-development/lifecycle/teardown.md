# Teardown

**Target audience:** developers working on the integration layer.

The public method is described in
[TerminalApi](../../build-pos/terminal-api.md). This page covers the implementation’s responsibilities.

## TerminalApiImpl.teardownTerminal

Sequence:

1. `SdkRuntime.logout()`
2. `SdkRuntime.teardown()`
3. `TerminalConnectionManager.setConnected(false)`
4. return `true` if both SDK steps succeed

If an exception occurs, it is logged and the method returns `false`.

## Limitation

`ApiModule` does not expose a public reset API for reconfiguring the entire integration within the same process.
Configuration rules for consumers are described in
[Configuration](../../build-pos/configure-terminal.md).
