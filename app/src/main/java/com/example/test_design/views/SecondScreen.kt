package com.example.test_design.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.test_design.domain.models.CartItem
import com.example.test_design.components.cart.CartSummary
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.test_design.components.ui.ScreenHeader

@Composable
fun SecondScreen(
    navController: NavController,
    cart: SnapshotStateList<CartItem>,
    appliedAmount: Int,
    appliedCode: String,
    onDiscountApplied: (Int, String) -> Unit,
    onPay: (Int, String, Int) -> Unit,
    onClearCart: () -> Unit? = {},
    onClose: (() -> Unit)? = null
) {

    LaunchedEffect(cart.size) {
        if (cart.isEmpty()) {
            onDiscountApplied(0, "")
            onClose?.invoke()
        }
    }


    LocalContext.current
    rememberCoroutineScope()

    LaunchedEffect(cart.size) {
        if (cart.isEmpty()) {
            onClose?.invoke()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    )
    {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {

            ScreenHeader(stringResource(com.example.test_design.R.string.cart_title))


            CartSummary(
                navController = navController,
                cart = cart,
                appliedAmount = appliedAmount,
                appliedCode = appliedCode,
                onPay = onPay,
                onClearCart = onClearCart
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}