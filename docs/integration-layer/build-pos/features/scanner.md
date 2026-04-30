# Scanner

**Target audience:** consumers of the integration layer.

The scanner is accessed via `TerminalApi`. API signatures are defined in
[TerminalApi](../terminal-api.md).

## Initialize and start

```kotlin id="p6g9i1"
ApiModule.terminal.initializeScanner()

ApiModule.terminal.startScanner(
    activity = activity,
    behavior = ScanBehavior.SINGLE,
)
```

`ScanBehavior` can be `SINGLE` or `CONTINUOUS`.

## Reading results

Scanner results are delivered via `scannedCode`, see
[State and Flows](../state-and-flows.md).

## Example

See [Example: Scanner](../examples/scanner.md).
