package com.example.test_design.components.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.test_design.components.overlays.ProductVarietyModal
import com.example.test_design.data.entity.ProductEntity

@Composable
fun ProductGrid(
    products: List<UiProduct>,
    cart: SnapshotStateList<CartItem>,
    onProductClick: (UiProduct) -> Unit,
    allProductEntities: List<ProductEntity>,
    modifier: Modifier = Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 250.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 120.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products, key = { it.articleNumber }) { product ->
            ProductCard(
                product = product,
                cart = cart,
                onProductClick = onProductClick,
                products = allProductEntities
            )
        }
    }
}