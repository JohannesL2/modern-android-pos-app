package com.example.test_design.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import com.example.test_design.components.quantity.AddQuantityButton
import com.example.test_design.components.quantity.QuantityTextWithDialog
import com.example.test_design.components.quantity.RemoveQuantityButton

@Composable
fun QuantityControl(
    product: UiProduct,
    cart: SnapshotStateList<CartItem>,
    onInteraction: () -> Unit = {},
    forceMobileMode: Boolean = false,
    isTablet: Boolean = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
) {
    val haptic = LocalHapticFeedback.current

    fun findFreshIndex(): Int {
        val normalizedVariant = product.variantValue?.trim()
        val exactIndex = cart.indexOfFirst {
            it.product.articleNumber == product.articleNumber &&
                    it.product.variantValue?.trim().equals(normalizedVariant, ignoreCase = true)
        }

        if (exactIndex >= 0) return exactIndex

        val sameArticleIndex = cart.indexOfFirst {
            it.product.articleNumber == product.articleNumber &&
                    (normalizedVariant == null || it.product.variantValue?.trim().equals(normalizedVariant, ignoreCase = true))
        }

        return if (sameArticleIndex >= 0) sameArticleIndex else -1
    }

    // För UI-visning
    val currentIndex = findFreshIndex()
    val quantity = if (currentIndex >= 0) cart[currentIndex].quantity else 0

    val useTabletLayout = isTablet && !forceMobileMode

    val containerModifier = if (useTabletLayout) {
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(horizontal = 4.dp)
    } else {
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(0.05f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (useTabletLayout) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp),
        modifier = containerModifier
            .clickable {
                onInteraction()
            }
    ) {
        RemoveQuantityButton(
            onClick = {
                val freshIndex = findFreshIndex()
                if (freshIndex >= 0) {
                    if (cart[freshIndex].quantity > 1) {
                        cart[freshIndex] = cart[freshIndex].copy(quantity = cart[freshIndex].quantity - 1)
                    } else {
                        cart.removeAt(freshIndex)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInteraction()
                }
            }
        )

        QuantityTextWithDialog(
            quantity = quantity,
            product = product,
            cart = cart
        )

        AddQuantityButton(
            onClick = {
                val freshIndex = findFreshIndex()
                if (freshIndex >= 0) {
                    if (cart[freshIndex].quantity < 99) {
                        cart[freshIndex] = cart[freshIndex].copy(quantity = cart[freshIndex].quantity + 1)
                    }
                } else {
                    cart.add(CartItem(product, 1))
                }
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onInteraction()
            }
        )
    }
}
