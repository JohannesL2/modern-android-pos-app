# POS Integration Architecture (Anonymized)

This repository contains a conceptual POS (Point of Sale) application demonstrating an integration architecture for payment terminals, receipt printing, and scanning.

The original implementation has been anonymized:
- Vendor-specific SDKs have been removed
- All hardware interactions are represented as pseudocode
- Sensitive logic has been simplified

The purpose is to showcase architecture and integration patterns — not to provide a production-ready solution.

**We describe our project more in details and provide full documentation here:**

**[Documentation Site](https://<your-docs-url>)**

## Background

This project was developed as part of an internship (LIA – *Lärande i arbete*), where a POS application was built and integrated with a payment terminal.

The system supports both:
- **On-device integration** (POS app and payment running on the same device)
- **Off-device integration** (external terminal and printer)

This repository reflects the architectural structure and design decisions from that work, without exposing proprietary SDK code.

## Scope & Limitations

This is **not a functional payment system**.

- No real SDK communication
- No actual payment processing
- No hardware interaction
- Pseudocode replaces vendor implementations

## Project Structure

```
/app            # Example POS application
/integration    # Integration layer (core logic)
/docs           # Documentation source
```

## The people behind the project

<a href="https://github.com/JohannesL2">
  <img src="https://avatars.githubusercontent.com/u/183211686?size=50">
</a>

<a href="https://github.com/angelika-friis">
  <img src="https://avatars.githubusercontent.com/u/180537254?size=50">
</a>

<a href="https://github.com/Rozabelay339">
  <img src="https://avatars.githubusercontent.com/u/180546490?widht="50" height="50"">
</a>

<a href="https://github.com/ewahjelm">
  <img src="https://avatars.githubusercontent.com/u/181728720?size=50">
</a>
