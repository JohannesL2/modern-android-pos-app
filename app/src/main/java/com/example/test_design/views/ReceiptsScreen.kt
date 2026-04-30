package com.example.test_design.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.test_design.components.base.buttons.WideButton
import com.example.test_design.components.common.inputs.SearchBar
import com.example.test_design.components.ui.ScreenHeader
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.utils.isValidReceipt
import com.example.test_design.viewmodels.PaymentViewModel
import kotlinx.coroutines.launch
import com.example.test_design.data.entity.OrderEntity
import kotlinx.coroutines.withContext
import com.example.test_design.data.dao.OrderWithRows
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.test_design.R
import com.example.test_design.data.utils.formatTimestamp
import kotlinx.coroutines.delay

@Composable
fun ReceiptsScreen(
    navController: NavController,
    orderDao: OrderDao,
    paymentDao: PaymentDao,
    paymentViewModel: PaymentViewModel,
    onClose: () -> Unit
) {
    val isPrinting = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    val scope = rememberCoroutineScope()

    var receiptInput by remember { mutableStateOf("") }

    var allOrders by remember { mutableStateOf<List<OrderWithRows>>(emptyList()) }
    var filteredOrders by remember { mutableStateOf<List<OrderWithRows>>(emptyList()) }
    var selectedOrder by remember { mutableStateOf<OrderWithRows?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var searchResult by remember { mutableStateOf<OrderEntity?>(null) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            val orders = orderDao.getAllOrdersWithRows()
            allOrders = orders.sortedByDescending { it.order.id }
            filteredOrders = allOrders
            isLoading = false
        }
    }

    LaunchedEffect(receiptInput) {
        filteredOrders = if (receiptInput.isEmpty()) {
            allOrders
        } else {
            allOrders.filter { it.order.orderNumber.contains(receiptInput) }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .zIndex(10f)
        .background(Color.White)
        .padding(top = 18.dp)) {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .zIndex(10f)
                .padding(top = 18.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header
            ScreenHeader("Kvittohistorik")

            Spacer(modifier = Modifier.height(24.dp))

        SearchBar(
            modifier = Modifier.fillMaxWidth(if (isTablet) 0.6f else 1f),
            query = receiptInput,
            onQueryChange = { input ->
                receiptInput = input.filter { it.isDigit() }.take(6)
            },
            onClear = {
                receiptInput = ""
                selectedOrder = null
            },
            onScanClick = {
                val activity = context as? androidx.activity.ComponentActivity
                activity?.let { paymentViewModel.startSingleScan(it) }
            },
            placeholderText = "Ange kvittonummer",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else if (selectedOrder == null) {
            // --- VISA LISTAN ---
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(if (isTablet) 0.8f else 1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders) { item ->
                    OrderRowItem(orderWithRows = item) {
                        selectedOrder = item
                        keyboardController?.hide()
                    }
                }
            }
        } else {
            selectedOrder?.let { result ->
                OrderDetailsCard(
                    order = result.order,
                    isTablet = isTablet,
                    onBack = { selectedOrder = null },
                    onPrint = {
                        scope.launch {
                            isPrinting.value = true
                            val startTime = System.currentTimeMillis()

                            paymentViewModel.printReceiptFromDBWithExternalPrinter(
                                context = context,
                                receiptNumber = result.order.orderNumber,
                                orderDao = orderDao,
                                paymentDao = paymentDao
                            )

                            val elapsed = System.currentTimeMillis() - startTime
                            if (elapsed < 3000L) delay(3000L - elapsed)

                            isPrinting.value = false
                        }
                    }
                )
            }
        }
    }

        Box(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomEnd)
                .padding(horizontal = 28.dp, vertical = 20.dp)
                .zIndex(10f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate("main") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.menu_desc_close),
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }


        if (isPrinting.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.8f))
                    .zIndex(20f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Skriver ut kvitto-kopia...", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OrderRowItem(orderWithRows: OrderWithRows, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "#${orderWithRows.order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = formatTimestamp(orderWithRows.order.orderDate), fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${orderWithRows.order.totalAmount} kr", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(text = "${orderWithRows.rows.size} produkter", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun OrderDetailsCard(order: OrderEntity, isTablet: Boolean, onBack: () -> Unit, onPrint: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Kvitto #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Totalt: ${order.totalAmount} kr", fontSize = 18.sp)

            Spacer(modifier = Modifier.height(24.dp))

            WideButton(onClick = onPrint, text = "Skriv ut kopia")

            TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
                Text("Tillbaka till listan", color = Color.DarkGray)
            }
        }
    }
}
