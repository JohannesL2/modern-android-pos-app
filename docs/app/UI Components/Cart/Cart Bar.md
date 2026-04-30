---
sidebar_label: 'Cart Bar'
sidebar_position: 2
---

# CartBar

The **CartBar** is a persistent action bar used at the bottom of the shopping screen.

## CartBar

This component is a "Smart Component" that adaptive to screen size and manages complex animations for the cart state.

---

### Preview
<img src="/img/cart/cart-bar.png" width="180" />

---

### Key Features
* **Adaptive Layout:** Changes width based on device type (Tablet vs Phone).
* **Animations:** Uses Spring animations for expanding/collapsing and rotating the basket icon.
* **Badging:** Shows a red badge with the total number of units in the cart.
* **Haptics:** Triggers a long-press vibration when the "Pay" action is clicked.

### Usage

```kotlin
CartBar(
    cart = cartList,
    isExpanded = false,
    discountAmount = 50,
    appliedCode = "SUMMER24",
    onClick = { /* Toggle expand */ },
    onPay = { discount, code, total -> 
        println("Paying $total with code $code") 
    }
)
```

### Behavior
* Fixed height (64dp)
* Primary Color: #4700B3 (Custom Purple)
* Rounded (pill-shaped)

### Props
### Props

| Prop | Type | Description |
| :--- | :--- | :--- |
| **cart** | `SnapshotStateList<CartItem>` | List of items in the cart. Used to calculate total price and units. |
| **onClick** | `() -> Unit` | Action triggered when the basket/close icon is clicked. |
| **isExpanded** | `Boolean` | Determines if the bar is in its expanded state (shows close icon). |
| **isMenuOpen** | `Boolean` | If true, fades out the bar (alpha = 0) to avoid overlapping menus. |
| **discountAmount** | `Int` | The fixed amount to be deducted from the total sum. |
| **appliedCode** | `String` | The discount code string to be passed along to the payment process. |
| **onPay** | `(discount: Int, code: String, total: Int) -> Unit` | Callback triggered on payment. Returns final discount, code, and total. |