# Cart Summary

`CartSummary` is a central UI component in the Android application that manages the display, modification, and payment flow of the user's shopping cart. It is built using **Jetpack Compose** and is optimized for both mobile and tablet form factors.

---

## Features

- **Smart Grouping**: Automatically merges products with the same article number and variant to keep the list compact.
- **Responsive Layout**: Dynamically switches layout based on screen width (Tablet >= 600dp).
- **Discount Handling**: Real-time display of subtotal and final price when discount codes are applied.
- **Quantity Control**: Direct integration with `QuantityControl` to increase or decrease item counts.
- **Secure Clearing**: Includes a confirmation dialog (`AlertDialog`) before the cart is emptied.
- **Haptic Feedback**: Provides physical sensations during key interactions (long press, item removal).

---

## Technical Specifications

### Parameters (Props)

| Name | Type | Description |
| :--- | :--- | :--- |
| `navController` | `NavController` | Handles navigation to flows like "Split Payment". |
| `cart` | `SnapshotStateList<CartItem>` | The reactive list containing cart items. |
| `appliedAmount` | `Int` | The discount amount in local currency. |
| `appliedCode` | `String` | The name of the active discount code. |
| `onPay` | `(discount, code, total) -> Unit` | Callback for the payment process. |
| `onClearCart` | `() -> Unit` | Callback triggered when the cart is cleared. |

### Libraries & Technologies
- **Coil**: For asynchronous image loading.
- **Compose Animation**: For smooth transitions of "Delete" buttons and price changes.
- **DerivedState**: For efficient recalculation of totals and groupings without unnecessary recompositions.

---

## Usage

Example of how to include the component within a screen:
```kotlin
CartSummary(
    navController = navController,
    cart = cartViewModel.items,
    appliedAmount = 50,
    appliedCode = "WELCOME2026",
    onPay = { discount, code, total ->
        processPayment(total)
    },
    onClearCart = {
        logger.log("Cart cleared by user")
    }
)