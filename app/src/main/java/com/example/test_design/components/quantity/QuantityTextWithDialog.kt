package com.example.test_design.components.quantity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
fun QuantityTextWithDialog(
    quantity: Int,
    product: UiProduct,
    cart: SnapshotStateList<CartItem>,
) {
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val quantityText = quantity.toString()

    Text(
        text = quantityText,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .width(32.dp)
            .clickable{
                showDialog = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
        textAlign = TextAlign.Center
    )

    QuantityInputDialog(
        visible = showDialog,
        quantityText = quantityText,
        product = product,
        cart = cart,
        onDismiss = { showDialog = false }
    )
}