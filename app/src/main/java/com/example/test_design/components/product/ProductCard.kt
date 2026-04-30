package com.example.test_design.components.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.example.test_design.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.example.test_design.components.overlays.ProductVarietyModal
import com.example.test_design.data.entity.ProductEntity

@Composable
fun ProductCard(
    product: UiProduct,
    cart: SnapshotStateList<CartItem>,
    onProductClick: (UiProduct) -> Unit,
    modifier: Modifier = Modifier,
    products: List<ProductEntity>
    ) {
    val context = LocalContext.current
    val isTablet = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600

    var selectedProductForModal by remember {
        androidx.compose.runtime.mutableStateOf<UiProduct?>(
            null
        )
    }

    val variants = remember(products, product.articleNumber) {
        val baseProductCode = product.articleNumber.substringBefore("-")
        products.filter { it.isVariant && it.baseProductCode == baseProductCode }
    }

    val hasVariants = remember(product.name) {
        val productsWithSizes =
            listOf("Kaffe", "Latte", "Havre latte", "Matcha latte", "Smoothie", "Smörgås")
        productsWithSizes.contains(product.name)
    }

    val formattedPrice = stringResource(R.string.cart_price_format, product.price)
    val priceLabel = if (hasVariants) "Från $formattedPrice" else formattedPrice


    val totalQuantityInCart = cart
        .filter { it.product.articleNumber.substringBefore("-") == product.articleNumber.substringBefore("-") }
        .sumOf { it.quantity }

    val imageModel = remember(product.imageRes) {
        val path = product.imageRes
        when {
            path.startsWith("content://") || path.startsWith("file://") -> path.toUri()
            path.contains("R.drawable.") -> {
                val resName = path.substringAfterLast(".")
                context.resources.getIdentifier(resName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: com.example.test_design.R.drawable.placeholder_image
            }

            else -> com.example.test_design.R.drawable.placeholder_image
        }
    }

    selectedProductForModal?.let { selectedProduct ->
        ProductVarietyModal(
            product = selectedProduct,
            cart = cart,
            variants = variants,
            onDismiss = { selectedProductForModal = null }
        )
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isTablet) {
                    Modifier
                        .height(300.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            enabled = hasVariants,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(color = Color(0xFF6200EE)),
                            onClick = { onProductClick(product) }
                        )
                } else {
                    Modifier
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                })
            .clip(RoundedCornerShape(10.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.Black.copy(alpha = 0.05f)
        )
    )
    {
        if (isTablet) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = product.name,
                        modifier = Modifier.size(170.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    if (totalQuantityInCart > 0) {
                        Text(
                            text = totalQuantityInCart.toString(),
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(28.dp)
                                .background(Color(0xFF6200EE), shape = CircleShape)
                                .wrapContentHeight(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = priceLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6200EE)
                    )
                }

                if (!hasVariants) {
                    QuantityControl(product = product, cart = cart)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF6200EE).copy(alpha = 0.1f), RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Välj storlek", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .then(
                        if (hasVariants) {
                            Modifier.clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = Color(0xFF6200EE)),
                                onClick = { onProductClick(product) }
                            )
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = product.name,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(priceLabel, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (!hasVariants) {
                    QuantityControl(product = product, cart = cart)
                } else {
                    Text("Välj", color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
    }
}