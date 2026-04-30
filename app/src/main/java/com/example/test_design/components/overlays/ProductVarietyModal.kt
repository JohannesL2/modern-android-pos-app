package com.example.test_design.components.overlays

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.example.test_design.R
import com.example.test_design.components.quantity.AddQuantityButton
import com.example.test_design.components.quantity.RemoveQuantityButton
import com.example.test_design.data.entity.ProductEntity
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import kotlinx.coroutines.launch

@Composable
fun ProductVarietyModal(
    product: UiProduct,
    cart: SnapshotStateList<CartItem>,
    variants: List<ProductEntity>,
    onDismiss: () -> Unit
) {
    val isTablet = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600

    val standardLabel = stringResource(R.string.variant_standard)

    val sizeOptions = variants.map {
        Triple(
            it.variantValue ?: standardLabel,
            "",
            it.priceModifier
        )
    }

    val accessoryOptions = listOf(
        "Ingen mjölk" to 0,
        "Vanlig Mjölk" to 5,
        "Havredryck" to 7,
        "Sojadryck" to 7
    )

    val displayList = remember(sizeOptions) {
        if (sizeOptions.isEmpty()) {
            listOf(Triple(standardLabel, "", 0))
        } else {
            sizeOptions
        }
    }

    var selectedSizeName by remember(displayList) {
        mutableStateOf(
            sizeOptions.find { it.first.equals("S", ignoreCase = true) }?.first
                ?: sizeOptions.find { it.first.equals("STANDARD", ignoreCase = true) }?.first
                ?: displayList.first().first
        )
    }

    var selectedOptionName by remember { mutableStateOf("Vanlig Mjölk") }

    var quantity by remember { mutableStateOf(1) }

    val initialModifier = variants.find {
        it.variantValue?.equals(product.variantValue, ignoreCase = true) == true
    }?.priceModifier ?: 0

    val trueBasePrice = product.price - initialModifier

    val sizeCost = sizeOptions.find { it.first == selectedSizeName }?.third ?: 0

    val totalPrice = trueBasePrice + sizeCost

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val purpleColor = Color(0xFF4A00B5)
    val modalGray = Color(0xFFF2F2F2)

    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            Modifier
                .padding(horizontal = 4.dp)
                .fillMaxWidth(if (isTablet) 0.5f else 0.95f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = modalGray
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.modal_customize, product.name),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black, CircleShape)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stäng",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                Modifier.padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.modal_from_price, trueBasePrice),
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.modal_label_size),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    displayList.forEach { (name, volume, cost) ->
                        val isSelected = selectedSizeName == name

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val buttonModifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)

                            if (isSelected) {
                                Button(
                                    onClick = { selectedSizeName = name },
                                    modifier = buttonModifier,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = purpleColor),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    SizeButtonContent(
                                        name = name,
                                        volume = volume,
                                        isSelected = true
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedSizeName = name },
                                    modifier = buttonModifier,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, purpleColor),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    SizeButtonContent(name = name, volume = volume, isSelected = false)
                                }
                            }
                            Box(
                                modifier = Modifier.height(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected && cost > 0) {
                                    Text(text = stringResource(R.string.modal_price_modifier, cost), fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.modal_label_quantity),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RemoveQuantityButton(onClick = {
                        if (quantity > 1) quantity--
                    })

                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    AddQuantityButton(onClick = {
                        quantity++
                    })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val finalSummaryPrice = totalPrice * quantity

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = purpleColor),
                onClick = {
                    val selectedVariant = variants.find { it.variantValue?.equals(selectedSizeName, ignoreCase = true) == true }
                    val selectedArticleNumber = selectedVariant?.articleNumber ?: product.articleNumber

                    val index = cart.indexOfFirst {
                        it.product.articleNumber == selectedArticleNumber &&
                                it.product.variantValue?.trim()?.equals(selectedSizeName.trim(), ignoreCase = true) == true
                    }

                    if (index >= 0) {
                        cart[index] = cart[index].copy(quantity = cart[index].quantity + quantity)
                    } else {
                        cart.add(CartItem(
                            product = product.copy(
                                name = product.name,
                                price = totalPrice,
                                articleNumber = selectedArticleNumber,
                                variantValue = selectedSizeName,
                                ean = selectedVariant?.ean ?: product.ean
                            ),
                            quantity = quantity
                        ))
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 18.dp),
                shape = RoundedCornerShape(100.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterStart)
                    )

                    Text(
                        text = stringResource(R.string.modal_add_to_cart, finalSummaryPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
fun SizeButtonContent(name: String, volume: String, isSelected: Boolean) {
    val titleColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdditionalButtonContent(name: String, isSelected: Boolean) { // Ingen extraCost här
    val textColor by animateColorAsState(if (isSelected) Color.White else Color.Black)
    Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
        Text(text = name, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}