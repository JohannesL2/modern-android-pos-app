# Example: Printing

**Target audience:** consumers of the integration layer.

For printing rules, see [Receipt Printing](../features/receipt-printing.md).

## Verifone

```kotlin
viewModelScope.launch {
    val result = ApiModule.terminal.print(
        content = "<p>Thank you for your purchase</p>",
        contentType = PrintContentType.HTML,
    )

    when (result) {
        PrintResult.Success -> showMessage("Receipt printed")
        is PrintResult.Failure -> showMessage(result.errorMessage)
    }
}
```

## Epson

```kotlin
viewModelScope.launch {
    val data = EpsonPrintData(
        blocks = listOf(
            EpsonPrintBlock.Text("Gardeco", EpsonAlign.CENTER),
            EpsonPrintBlock.Feed(1),
            EpsonPrintBlock.Text("Thank you for your purchase"),
            EpsonPrintBlock.Cut(),
        )
    )

    val result = ApiModule.terminal.printEpson(data)

    when (result) {
        PrintResult.Success -> showMessage("Receipt printed")
        is PrintResult.Failure -> showMessage(result.errorMessage)
    }
}
```