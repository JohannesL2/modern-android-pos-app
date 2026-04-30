package com.example.test_design.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.round
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import com.example.test_design.components.overlays.ProductVarietyModal
import androidx.compose.runtime.setValue

@Composable
fun ProductList(
    products: List<UiProduct>,
    cart: SnapshotStateList<CartItem>,
    onProductClick: (UiProduct) -> Unit,
    allProductEntities: List<com.example.test_design.data.entity.ProductEntity>,
    modifier: Modifier = Modifier
) {

    val lazyListState = rememberLazyListState()

    val snapLayoutInfoProvider = remember(lazyListState) {
        SnapLayoutInfoProvider(
            lazyListState = lazyListState,
            snapPosition = SnapPosition.Start
        )
    }

    val snapBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider)

    val density = androidx.compose.ui.platform.LocalDensity.current

    val scrollbarColor = Color(0xFF4700B3)

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val cardHeight = 104.dp
            val spacing = 12.dp

            val bottomPadding = 50.dp
            val availableHeight = maxHeight - bottomPadding

            val dynamicVisibleCount =
                (availableHeight / (cardHeight + spacing)).toInt().coerceAtLeast(1)

            val totalListHeight =
                (cardHeight * dynamicVisibleCount) + (spacing * (dynamicVisibleCount - 1))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalListHeight)
            ) {
                LazyColumn(
                    state = lazyListState,
                    flingBehavior = snapBehavior,
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = products,
                        key = { product ->
                            "${product.articleNumber}_${product.name}_${product.variantValue ?: "base"}"
                        }
                    ) { product ->
                        Box(modifier = Modifier.height(cardHeight)) {
                            ProductCard(
                                product = product,
                                cart = cart,
                                onProductClick = onProductClick,
                                products = allProductEntities
                            )
                        }
                    }
                }

                val isScrollable by remember(lazyListState) {
                    derivedStateOf {
                        lazyListState.layoutInfo.totalItemsCount > dynamicVisibleCount
                    }
                }

                if (products.isNotEmpty() && isScrollable) {
                    val scrollMetrics by remember(lazyListState, products.size, totalListHeight) {
                        derivedStateOf {
                            val layoutInfo = lazyListState.layoutInfo
                            val viewportHeightPx =
                                layoutInfo.viewportEndOffset.toFloat() - layoutInfo.viewportStartOffset.toFloat()

                            val itemHeightPx = with(density) { cardHeight.toPx() }
                            val spacingPx = with(density) { spacing.toPx() }
                            val contentHeightPx = (products.size * itemHeightPx) +
                                    ((products.size - 1).coerceAtLeast(0) * spacingPx)

                            val heightPercent =
                                (viewportHeightPx / contentHeightPx).coerceIn(0.1f, 1f)
                            val tHeight = totalListHeight * heightPercent
                            val tHeightPx = with(density) { tHeight.toPx() }

                            val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
                            val tYPx =
                                if (firstItem != null && contentHeightPx > viewportHeightPx) {

                                    val itemStepPx =
                                        firstItem.size.toFloat() + with(density) { spacing.toPx() }
                                    val currentScrollPx =
                                        (firstItem.index * itemStepPx) - firstItem.offset


                                    val maxScrollInList = contentHeightPx - viewportHeightPx
                                    val scrollPercent =
                                        (currentScrollPx / maxScrollInList).coerceIn(0f, 1f)

                                    val trackHeightPx = with(density) { totalListHeight.toPx() }
                                    val maxThumbTravel = trackHeightPx - tHeightPx

                                    scrollPercent * maxThumbTravel
                                } else 0f

                            tHeight to tYPx
                        }
                    }

                    val (thumbHeight, thumbYPx) = scrollMetrics

                    Box(
                        modifier = Modifier
                            .height(totalListHeight)
                            .width(6.dp)
                            .align(Alignment.CenterEnd)
                            .background(
                                scrollbarColor.copy(alpha = 0.09f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(thumbHeight)
                                .graphicsLayer {
                                    translationY = round(thumbYPx.coerceAtLeast(0f))
                                }
                                .background(
                                    scrollbarColor.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}