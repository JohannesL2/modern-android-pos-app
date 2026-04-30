# Receipt Printing

**Target audience:** consumers of the integration layer.

The integration layer provides two printing paths. API signatures are defined in
[TerminalApi](../terminal-api.md), and the result model in
[Error Handling](../error-handling.md).

## Verifone terminal printer

```kotlin
val result = ApiModule.terminal.print(
    content = "Receipt",
    contentType = PrintContentType.TEXT,
)
```

`PrintContentType` can be `HTML`, `TEXT`, or `IMAGE`. See known limitations in
[Limitations](../limitations.md).

## Epson printer

```kotlin
val initResult = ApiModule.terminal.initializeEpsonPrinter()
val result = ApiModule.terminal.printEpson(data)
```

`EpsonPrintData` consists of blocks such as text, logo, feed, cut, and barcode.

## Example

See [Example: Printing](../examples/printing.md).
