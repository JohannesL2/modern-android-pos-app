# Limitations

**Target audience:** consumers of the integration layer.

These are constraints that the application layer must be aware of.

## Process and Configuration

* The integration layer must be started once per app process.
* Terminal configuration cannot be changed after `ApiModule.start(scope)`.
* `ApiModule.start(scope)` is asynchronous; use `terminalReady`.

Startup and configuration are described in [Quick Start](build-pos-app.md) and [Configuration](configure-terminal.md).

## Operations

* Only one payment at a time is supported.
* Void requires `appSpecificData` from the original payment.
* Refund card verification compares only the BIN and the last four digits when used.
* Starting the scanner requires an Android `Activity`.

See the respective feature pages for details.

## Printing

* `PrintContentType.IMAGE` is not verified as true image printing on the terminal printer.
* Epson printing uses a separate Epson integration, not the terminal printer.

See [Receipt Printing](features/receipt-printing.md).
