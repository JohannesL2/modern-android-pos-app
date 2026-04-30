---
sidebar_label: 'Buttons'
sidebar_position: 1
---

# Buttons
Buttons are used to trigger actions. In this project, we use custom buttons built on top of JetPack Compose to ensure consistency across the app.

## WideButton
The **WideButton** is a full-width button with optional icon support.  
It is designed for clear, prominent actions with strong visual presence.

---

### Preview
<div style={{ display: 'flex', gap: '20px' }}>
  <div>
    <p><strong>Without icon</strong></p>
    <img src="/img/buttons/wide-button.png" width="180" />
  </div>

  <div>
    <p><strong>With icon</strong></p>
    <img src="/img/buttons/wide-button-icon.png" width="180" />
  </div>
</div>

---

### Usage

#### Basic
```kotlin
WideButton(
    text = "Generic",
    onClick = {}
)
```

#### With icon
```kotlin
WideButton(
    text = "Generic",
    onClick = {},
    icon = Icons.Default.ShoppingCart
)
```

### When to use
* Primary or secondary actions
* When you need icon + text
* Full-width layouts

### Behavior
* Fixed height (64dp)
* Centered content
* Optional icon support
* Rounded (pill-shaped)

### Props
| Prop            | Type         | Description       |
| --------------- | ------------ | ----------------- |
| text            | String       | Button label      |
| onClick         | () -> Unit   | Click handler     |
| icon            | ImageVector | Optional icon     |
| enabled         | Boolean      | Disabled state    |
| backgroundColor | Color        | Background color  |
| textColor       | Color        | Text & icon color |
