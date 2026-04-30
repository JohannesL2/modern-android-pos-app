package com.example.test_design.components.quantity

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun QuantityInputDialog (
    visible: Boolean,
    quantityText: String,
    product: UiProduct,
    cart: SnapshotStateList<CartItem>,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var textFieldValue by remember(product.ean) {
        mutableStateOf(
            TextFieldValue(
                text = quantityText,
                selection = TextRange(0, quantityText.length)
            )
        )
    }

        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

    val confirm = {
        val enteredQuantity = textFieldValue.text.toIntOrNull() ?: 0
        val newQuantity = enteredQuantity.coerceAtMost(99)

        val index = cart.indexOfFirst {
            it.product.articleNumber == product.articleNumber &&
                    it.product.variantValue == product.variantValue
        }

        when {
            newQuantity <= 0 && index >= 0 -> {
                cart.removeAt(index)
            }
            index >= 0 -> {
                cart[index] = cart[index].copy(quantity = newQuantity)
            }
            newQuantity > 0 -> {
                cart.add(CartItem(product, newQuantity))
            }
        }
        onDismiss()
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Ändra antal för ${product.name}") },
            text = {
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text.all { it.isDigit() } && newValue.text.length <= 2) {
                            textFieldValue = newValue
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                        ),
                    keyboardActions = KeyboardActions(onDone = {
                            confirm()
                    }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    confirm()
                    }) {
                            Text("OK")
                        }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Avbryt")
                }
            }
        )
    }