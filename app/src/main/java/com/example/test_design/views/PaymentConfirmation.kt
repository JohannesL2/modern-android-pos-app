package com.example.test_design.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test_design.domain.models.CartItem
import com.example.test_design.data.utils.generateOrderNumber
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun PaymentConfirmation(
    navController: NavController,
    cart: SnapshotStateList<CartItem>
) {
    val total = cart.sumOf { it.product.price * it.quantity }

    Column {
        Text("Total: $total kr")
        Text("Hur vill du betala?")

        Button(onClick = {
            // card payment
        }) {
            Text("Kort")
        }

        Button(onClick = {
            // gift card
        }) {
            Text("Gift card")
        }

        Button(onClick = {
            navController.navigate("split_payment/$total")
        }) {
            Text("Split")
        }
    }

    val orderNumber by remember { mutableStateOf(generateOrderNumber()) }

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val formattedDateTime = currentDateTime.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Betalning godkänd!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Order.Nr $orderNumber",
            fontSize = 18.sp,
            color = Color.White
        )

        Spacer(
            modifier = Modifier
                .height(12.dp)
        )

        Text(
            text = "Belopp: $total kr!\n" +
                    "KORT: VISA CREDIT\n" +
                    "KORTNR: **** **** **** 4321\n" +
                    "METOD: CHIP/PIN\n" +
                    "$formattedDateTime",
            fontSize = 18.sp,
            color = Color.White
        )

        Spacer(
            modifier = Modifier
                .height(24.dp)
        )

        Text(
            text = "TACK FÖR DITT KÖP!",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                cart.clear()
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Tillbaka till start", color = Color.White, fontSize = 20.sp)
        }
    }
}