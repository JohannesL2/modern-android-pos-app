---
id: introduction
title: Introduction
slug: /
---

# The Integration Layer

The integration layer is the application's public interface towards the terminal, payments, scanner, and printing.  
The application layer must use the `TerminalApi` contract.  
The Verifone Payment SDK or internal implementations must not be used directly by the application layer.

## Start reading

Choose your use case:
- Run the existing POS application → [Setup to run POS app](run-pos/run-pos-app.md)
- Build your own POS application → [Setup integration layer for your own POS application](build-pos/build-pos-app.md)
- Developing the integration layer → [Architecture](integration-development/architecture.md)

## When you need more

The features are described separately:

- [Payments](build-pos/features/payments.md)
- [Refund](build-pos/features/refund.md)
- [Void](build-pos/features/void.md)
- [Scanner](build-pos/features/scanner.md)
- [Receipt printing](build-pos/features/receipt-printing.md)

Known limitations are listed in [Limitations](build-pos/limitations.md).

Internal architecture and implementation are described in [Architecture](integration-development/architecture.md). This document is intended for developers of the integration layer.

Handle state according to [State and flows](build-pos/state-and-flows.md).
Handle results and errors according to [Error handling](build-pos/error-handling.md).