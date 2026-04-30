package com.example.test_design.components.cart

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test_design.domain.models.CartItem
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.test_design.R


@Composable
fun CartBar (
    cart: SnapshotStateList<CartItem>,
    onClick: () -> Unit,
    isExpanded: Boolean,
    isMenuOpen: Boolean = false,
    discountAmount: Int,
    appliedCode: String,
    onPay: (discount: Int, code: String, total: Int) -> Unit
) {
    if (cart.isEmpty()) return

    val isTablet = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val totalUnits = remember(cart) {
        derivedStateOf { cart.sumOf { it.quantity } }
    }

    val total by remember(cart, discountAmount) {
        derivedStateOf {
            maxOf(
                0,
                cart.sumOf { item ->
                    item.product.price * item.quantity
                } - discountAmount
            )
        }
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isMenuOpen) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val purpleColor = Color(0xFF4700B3)
    val badgeRed = Color(0xFFF87171)

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.1f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = if (isTablet) Alignment.BottomCenter else Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .then(if (isTablet) Modifier.width(400.dp) else Modifier.fillMaxWidth()),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .graphicsLayer { alpha = contentAlpha }
                    .padding(end = 28.dp)
                    .background(
                        color = purpleColor,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(start = 32.dp, end = 40.dp)
                    .clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onPay(discountAmount, appliedCode, total)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = stringResource(R.string.content_desc_cart),
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterStart)
                    )

                    Text(
                        text = stringResource(R.string.cart_pay_button, total),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = contentAlpha
                    }
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black, CircleShape)
                        .border(
                            width = if (isExpanded) 2.dp else 0.dp,
                            color = if (isExpanded) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            onClick()
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier.graphicsLayer(rotationZ = rotation),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isExpanded) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.content_desc_close),
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shopping_basket),
                                contentDescription = stringResource(R.string.content_desc_cart),
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                if (!isExpanded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(22.dp)
                            .background(badgeRed, CircleShape)
                            .zIndex(2f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${totalUnits.value}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}