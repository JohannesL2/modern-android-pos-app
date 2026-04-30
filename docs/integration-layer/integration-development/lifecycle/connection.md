# Connection and reconnection

**Target audience:** developers working on the integration layer.

Public status semantics are described in
[State and flows](../../build-pos/state-and-flows.md).
This page covers the internal reconnection logic.

## terminalReady

`TerminalConnectionManager` derives `terminalReady` by combining:

* successful SDK initialization
* `TransactionManagerState.LOGGED_IN`
* `terminalConnected`

## Signals affecting connection state

The manager listens to:

* `paymentCompleted`
* `communicationStatus`
* `notificationEvents`
* `shouldReconnect`

When the connection is lost, the internal connection state is set to `false`.

## Reconnection

Reconnection is guarded by a `Mutex` to ensure only one reconnect flow runs at a time.

Sequence:

1. teardown of the runtime
2. re-initialization using the last known `TerminalConnectionConfig`
3. wait for initialization result
4. login
5. publish device information
6. set connection state to `true`

## Development rule

New reconnection logic should be placed here or in `SdkRuntime`, depending on whether it concerns connection policy or SDK events. It should not be implemented in UI, ViewModels, or payment-specific methods.
