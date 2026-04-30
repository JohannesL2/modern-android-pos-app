package com.example.test_design.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.test_design.viewmodels.PaymentViewModel
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import com.example.test_design.data.utils.isValidReceipt
import androidx.compose.ui.zIndex
import com.example.test_design.components.ui.ScreenHeader
import com.example.test_design.viewmodels.RefundViewModel
import com.example.test_design.viewmodels.SearchSource
import com.example.test_design.components.cart.RefundBar
import com.example.test_design.components.common.inputs.SearchBar
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.test_design.components.product.RefundQuantityControl
import com.example.test_design.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.example.test_design.components.base.buttons.WideButton
import com.example.test_design.config.FeatureFlags
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.layout.width

@Composable
fun RefundScreen (
    orderDao: OrderDao,
    paymentDao: PaymentDao,
    onClose: () -> Unit,
    onRefundCompleted: () -> Unit,
    paymentViewModel: PaymentViewModel,
    refundViewModel: RefundViewModel
) {
    val activity = LocalActivity.current
    val context = LocalContext.current // For temporary print of original receipt, move when needed
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    var receiptInput by remember { mutableStateOf("") }
    val selectedQtyByRowId = remember { mutableStateMapOf<Int, Int>() }
    val cart = refundViewModel.refundCart
    val errorMessageRes = refundViewModel.refundErrorMessage

    val currentSelectedSum by remember {
        derivedStateOf {
            cart.sumOf { row ->
                val maxAvailable = row.quantity.coerceAtLeast(0)
                val quantityToRefund = (selectedQtyByRowId[row.id] ?: 0).coerceIn(0, maxAvailable)
                row.unitPrice * quantityToRefund
            }
        }
    }

    val selectedRowsToRefund by remember {
        derivedStateOf {
            cart.mapNotNull { row ->
                val maxAvailable = row.quantity.coerceAtLeast(0)
                val quantityToRefund = (selectedQtyByRowId[row.id] ?: 0).coerceIn(0, maxAvailable)
                if (quantityToRefund <= 0) return@mapNotNull null

                RefundViewModel.RefundSelection(
                    rowId = row.id,
                    articleNumber = row.articleNumber,
                    productName = row.productName,
                    unitPrice = row.unitPrice,
                    quantityToRefund = quantityToRefund,
                    variantValue = row.variantValue,
                )
            }
        }
    }
    // Vid navigering från sidan rensas RefundState
    DisposableEffect(Unit) {
        onDispose {
            refundViewModel.clearRefundState()
        }
    }

    // Sync scanner input i lokala textfält state
    LaunchedEffect(Unit) {
        paymentViewModel.scannedCode.collect { scannedCode ->
            if (scannedCode.isBlank()) return@collect
            val digitsOnly = scannedCode.filter { it.isDigit() }.take(6)
            if (digitsOnly != receiptInput) {
                receiptInput = digitsOnly
                refundViewModel.searchReceipt(
                    source = SearchSource.SCANNER,
                    receipt = digitsOnly,
                    orderDao = orderDao
                )
            }
        }
    }

    // Autotrigger sökning vid 6 siffror inskrivna
    LaunchedEffect(receiptInput) {
        if (receiptInput.isValidReceipt()) {
            refundViewModel.searchReceipt(
                SearchSource.MANUAL,
                receiptInput,
                orderDao,

            )
            selectedQtyByRowId.clear()
        }
    }

    LaunchedEffect(cart.size, cart.sumOf { it.id }, cart.sumOf { it.quantity }) {
        val validIds = cart.map { it.id }.toSet()
        selectedQtyByRowId.keys.toList().forEach { rowId ->
            if (rowId !in validIds) selectedQtyByRowId.remove(rowId)
        }

        cart.forEach { row ->
            val selectedQty = selectedQtyByRowId[row.id] ?: 0
            selectedQtyByRowId[row.id] = selectedQty.coerceIn(0, row.quantity.coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(Color.White)
            .padding(top = 18.dp)
    ) {
        if (refundViewModel.showCardMismatchDialog) {
            AlertDialog(
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.4f else 0.85f),
                onDismissRequest = { },
                title = { Text(stringResource(R.string.refund_wrong_card_title)) },
                text = { Text(stringResource(R.string.refund_wrong_card_message)) },
                confirmButton = {
                    Button(onClick = {
                        refundViewModel.continueRefundAnyway {
                            onRefundCompleted()
                        }
                    }) {
                        Text(stringResource(R.string.common_yes))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        refundViewModel.cancelRefund()
                    }) {
                        Text(stringResource(R.string.common_no))
                    }
                }
            )
        }
        ///   SKÄRMKOMPONENTER   ///////
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .widthIn(max = if (isTablet) 900.dp else Dp.Unspecified)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ScreenHeader(stringResource(R.string.refund_header))

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchBar(
                    modifier = Modifier
                        .padding(horizontal = if (isTablet) 0.dp else 16.dp)
                        .width(if (isTablet) 500.dp else Dp.Unspecified) // Tvinga bredden istället för widthIn
                        .fillMaxWidth(if (isTablet) 0.6f else 1f),
                    query = receiptInput,
                    onQueryChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }.take(6)
                        receiptInput = digitsOnly // Uppdaterar lokalt state så det syns!

                        refundViewModel.onReceiptInputChanged(digitsOnly)

                        if (digitsOnly.length == 6) {
                            keyboardController?.hide()
                        }
                    },
                    onClear = { receiptInput = "" },
                    onScanClick = {
                        activity?.let { paymentViewModel.startSingleScan(it) }
                    },
                    placeholderText = stringResource(R.string.refund_search_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            errorMessageRes?.let {
                Text(
                    text = stringResource(it),
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }


            ///   EV INNEHÅLL I SÖKNING  ///////
            LazyVerticalGrid(
                columns = if (isTablet) GridCells.Fixed(2) else GridCells.Fixed(1),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp),
                contentPadding = PaddingValues(
                    bottom = if (isTablet) 140.dp else 124.dp,
                    top = 8.dp
                ),
            ) {
                items(cart) { item ->
                    val maxAvailable =
                        item.quantity.coerceAtLeast(0) - item.refundedQuantity.coerceAtLeast(0)
                    val quantityToRefund =
                        (selectedQtyByRowId[item.id] ?: 0).coerceIn(0, maxAvailable)

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF9F9F9)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 4.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 96.dp)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                Text(
                                    text = item.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                        .basicMarquee()
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(
                                            R.string.refund_unit_price,
                                            item.unitPrice
                                        ),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = " " + stringResource(
                                            R.string.refund_bought_quantity,
                                            item.quantity - item.refundedQuantity
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = item.variantValue ?: "",
                                        fontSize = 11.sp,
                                    )
                                }
                            }

                            RefundQuantityControl(
                                currentQty = quantityToRefund,
                                maxAvailable = maxAvailable,
                                onQuantityChange = { newQty ->
                                    selectedQtyByRowId[item.id] = newQty
                                },
                                onInteraction = {}
                            )
                        }
                    }
                }
            }
        }

        ////   CIRKULÄR CLOSE KNAPP  ////////
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isTablet) 24.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(if (isTablet) 0.4f else 0.95f)
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                RefundBar(
                    selectedRows = selectedRowsToRefund,
                    totalAmount = currentSelectedSum.toDouble(),
                    onClose = onClose,
                    onRefundClick = {
                        val receipt = receiptInput
                        if (receipt.isValidReceipt() && selectedRowsToRefund.isNotEmpty()) {
                            scope.launch {
                                val order = orderDao.getOrderByReceipt(receipt)
                                if (order != null) {
                                    val refundResult = refundViewModel.refund(
                                        receiptNumber = receipt,
                                        selections = selectedRowsToRefund,
                                        order = order,
                                        orderDao = orderDao,
                                        paymentDao = paymentDao,
                                    )
                                    if (refundResult != null) {
                                        onRefundCompleted()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
