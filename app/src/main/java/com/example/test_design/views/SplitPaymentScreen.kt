
package com.example.test_design.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test_design.components.base.buttons.WideButton
import com.example.test_design.components.payment.AmountSelector
import com.example.test_design.components.payment.PaymentStatusCard
import com.example.integration.api.model.CardType
import com.example.test_design.viewmodels.PaymentViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.test_design.viewmodels.OrderViewModel
import com.example.test_design.domain.models.CartItem

@Composable
fun SplitPaymentScreen(
    navController: NavController,
    totalAmount: Int,
    cart: SnapshotStateList<CartItem>,
    paymentViewModel: PaymentViewModel,
    orderViewModel: OrderViewModel,
    onPaymentComplete: () -> Unit
) {
    var inputAmount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CardType.CARD) }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val paidAmount = paymentViewModel.getPaidAmount()
    val remainingAmount = paymentViewModel.getRemainingAmount(totalAmount)
    val payments = paymentViewModel.getSplitPaymentParts()

    val amountToPay = inputAmount.toIntOrNull() ?: 0

    val isValidAmount = amountToPay in 1..remainingAmount
    val canSubmit = remainingAmount <= 0 || isValidAmount


    LaunchedEffect(Unit) {
        paymentViewModel.startSplitFlow(cart, orderViewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Split betalning",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        PaymentStatusCard(
            remainingAmount = remainingAmount,
            totalAmount = totalAmount,
            paidAmount = paidAmount
        )

        Spacer(Modifier.height(12.dp))

        if (remainingAmount > 0) {
            AmountSelector(
                remainingAmount = remainingAmount,
                nextAmountInput = inputAmount,
                onAmountChange = { inputAmount = it }
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType == CardType.CARD,
                onClick = { selectedType = CardType.CARD },
                label = { Text("Kort") }
            )
            FilterChip(
                selected = selectedType == CardType.GIFT_CARD,
                onClick = { selectedType = CardType.GIFT_CARD },
                label = { Text("Presentkort") }
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(payments) { payment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Black.copy(0.05f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Kort",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Genomförd",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "${payment.amountMinorUnits / 100} kr",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        WideButton(
            text = if (remainingAmount > 0)
                "Betala ${if (amountToPay > 0) "$amountToPay kr" else ""}"
            else "Slutför köp",

            icon = if (remainingAmount > 0)
                Icons.Default.AddCard
            else
                Icons.Default.Check,

            backgroundColor = when {
                remainingAmount <= 0 -> Color(0xFF2E7D32)
                !canSubmit || isProcessing -> Color.LightGray
                else -> Color(0xFF4700B3)
            },


            onClick = {
                if (!isProcessing && canSubmit) {

                    isProcessing = true

                    if (remainingAmount > 0) {
                        paymentViewModel.paySingleSplit(
                            context = context,
                            amountKr = amountToPay,
                            paymentType = selectedType,
                            totalAmount = totalAmount,
                            cart = cart,
                            orderViewModel = orderViewModel,
                        ) { success ->
                            isProcessing = false
                            if (success) inputAmount = ""
                        }
                    } else {
                        paymentViewModel.startSplitPaymentFlow(totalAmount) { success ->
                            isProcessing = false
                            // Navigation sker via SaleState i MainActivity
                        }
                    }

                }
            }
        )

        Spacer(Modifier.height(12.dp))


        TextButton(
            onClick = {
                paymentViewModel.clearSplitPayments()
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Avbryt betalning", color = Color.Gray)
        }
    }
}