package com.example.test_design.components.cart

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test_design.domain.models.CartItem
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.test_design.components.product.QuantityControl
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

@Composable
fun CartSummary(
    navController: NavController,
    cart: SnapshotStateList<CartItem>,
    appliedAmount: Int,
    appliedCode: String,
    onPay: (discount: Int, code: String, total: Int) -> Unit,
    onClearCart: () -> Unit? = {}
) {
    if (cart.isEmpty()) return

    val groupedCart = remember(cart.toList()) {
        cart.groupBy { it.product.articleNumber + (it.product.variantValue ?: "") }
            .map { (key, items) ->
                // Skapa en ny CartItem där vi summerar quantity för exakt samma produkt
                val firstItem = items.first()
                val totalQuantity = items.sumOf { it.quantity }
                firstItem.copy(quantity = totalQuantity)
            }
    }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    val subtotal = cart.sumOf { it.totalPrice }
    val finalTotal = maxOf(0, subtotal - appliedAmount)

    var expandedItemId by remember { mutableStateOf<String?>(null) }

    var showClearCartDialog by remember { mutableStateOf(false) }

    fun clearCart() {
        onClearCart()
        cart.clear()
        expandedItemId = null
    }

    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = if (isTablet) 0.dp else 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 4.dp else 10.dp)
                ) {
                    items(
                        groupedCart,
                        key = {
                            it.product.articleNumber + (it.product.variantValue ?: "")
                        }) { item ->
                        val currentUniqueId =
                            item.product.articleNumber + (item.product.variantValue ?: "")
                        val isDeleteVisible = expandedItemId == currentUniqueId

                        val imageModel = remember(item.product.imageRes) {
                            val path = item.product.imageRes
                            when {
                                path.startsWith("content://") || path.startsWith("file://") -> Uri.parse(
                                    path
                                )

                                path.contains("R.drawable.") -> {
                                    val resName = path.substringAfterLast(".")
                                    context.resources.getIdentifier(
                                        resName,
                                        "drawable",
                                        context.packageName
                                    )
                                        .takeIf { it != 0 }
                                        ?: com.example.test_design.R.drawable.placeholder_image
                                }

                                else -> com.example.test_design.R.drawable.placeholder_image
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF0F0F0)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color.Black.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    horizontal = if (isTablet) 0.dp else 16.dp,
                                    vertical = 2.dp
                                )
                                .clip(RoundedCornerShape(if (isTablet) 4.dp else 10.dp))
                                .clickable {
                                    val currentUniqueId =
                                        item.product.articleNumber + (item.product.variantValue
                                            ?: "")
                                    expandedItemId =
                                        if (expandedItemId == currentUniqueId) null else currentUniqueId

                                    if (expandedItemId != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = if (isTablet) 64.dp else 96.dp)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = item.product.name,
                                    modifier = Modifier
                                        .size(if (isTablet) 48.dp else 70.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = item.product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isTablet) 14.sp else 15.sp,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                initialDelayMillis = 2000,
                                                velocity = 40.dp,
                                                spacing = MarqueeSpacing.fractionOfContainer(1f / 3f)
                                            )
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(
                                                com.example.test_design.R.string.cart_price_format,
                                                item.pricePerItem
                                            ),
                                            fontSize = if (isTablet) 14.sp else 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            letterSpacing = 0.5.sp,
                                            softWrap = false,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = stringResource(
                                                com.example.test_design.R.string.cart_quantity_multiplier,
                                                item.quantity
                                            ),
                                            fontSize = if (isTablet) 14.sp else 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8A8A8A),
                                            letterSpacing = 1.sp,
                                            softWrap = false,
                                            maxLines = 1
                                        )
                                        if (!item.product.variantValue.isNullOrBlank() &&
                                            !item.product.variantValue.equals(
                                                "STANDARD",
                                                ignoreCase = true
                                            )
                                        ) {
                                            Text(
                                                text = " • ${item.product.variantValue}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF8A8A8A),
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Box(
                                        modifier = Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            expandedItemId = null
                                        }) {
                                        QuantityControl(
                                            product = item.product,
                                            cart = cart,
                                            onInteraction = { expandedItemId = null },
                                            forceMobileMode = true
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isDeleteVisible,
                                        enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(
                                            expandFrom = Alignment.End,
                                            animationSpec = tween(300)
                                        ),
                                        exit = fadeOut(animationSpec = tween(300)) + shrinkHorizontally(
                                            shrinkTowards = Alignment.End,
                                            animationSpec = tween(300)
                                        )
                                    ) {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                                cart.remove(item)
                                                expandedItemId = null
                                            },
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Ta bort",
                                                tint = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }

            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.4f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(horizontal = if (isTablet) 4.dp else 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        Text(
                            stringResource(com.example.test_design.R.string.cart_label_subtotal),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        AnimatedVisibility(
                            visible = appliedAmount > 0,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = stringResource(
                                    com.example.test_design.R.string.cart_discount_applied,
                                    appliedCode,
                                    appliedAmount
                                ),
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (appliedAmount > 0) {
                            Text(
                                text = stringResource(
                                    com.example.test_design.R.string.cart_price_format,
                                    subtotal
                                ),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(
                            text = "${if (appliedAmount > 0) finalTotal else subtotal} kr",
                            fontSize = 14.sp,
                            color = if (appliedAmount > 0) Color(0xFF2E7D32) else Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(com.example.test_design.R.string.cart_btn_pay),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Text(
                        text = stringResource(
                            com.example.test_design.R.string.cart_price_format,
                            finalTotal
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.4f)
            )

            Box(
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth() .height(52.dp),
                    onClick = {
                        navController.navigate("split_payment/$finalTotal")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = null,
                        Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .clickable {
                                showClearCartDialog = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 10f),
                                        0f
                                    )
                                )
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    style = stroke,
                                    radius = size.minDimension / 2
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Reset cart",
                            tint = Color.LightGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPay(appliedAmount, appliedCode, finalTotal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4700B3),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Betala",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$finalTotal kr",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            } else {
                // MOBIL-VY
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                clearCart()
                            }
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 10f),
                                        0f
                                    )
                                )
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    style = stroke,
                                    radius = size.minDimension / 2
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Reset cart",
                            tint = Color.LightGray,
                            modifier = Modifier.size(if (isTablet) 24.dp else 32.dp)
                        )
                    }
                }
            }
        }
    }
    if (showClearCartDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearCartDialog = false },
            title = { Text("Rensa varukorg") },
            text = { Text("Är du säker på att du vill ta bort alla varor i varukorgen?") },
            confirmButton = {
                Button(
                    onClick = {
                        clearCart()
                        showClearCartDialog = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ja, rensa")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showClearCartDialog = false }
                ) {
                    Text("Avbryt", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}