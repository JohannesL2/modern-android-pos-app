package com.example.test_design.components.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight


@Composable
fun AmountSelector(
    remainingAmount: Int,
    nextAmountInput: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    Text(
        text = "Välj andel",
        fontSize = if (isTablet) 16.sp else 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(0.10f, 0.25f, 0.5f, 1f).forEach { fraction ->
            val calculatedAmount = (remainingAmount * fraction).toInt()
            OutlinedButton(
                onClick = { onAmountChange(calculatedAmount.toString()) },
                modifier = Modifier
                    .weight(1f)
                    .height(if (isTablet) 64.dp else 48.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (nextAmountInput == calculatedAmount.toString()) Color.Black else Color.LightGray
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (nextAmountInput == calculatedAmount.toString()) Color.Black else Color.Transparent,
                    contentColor = if (nextAmountInput == calculatedAmount.toString()) Color.White else Color.Black
                )
            ) {
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    fontSize = if (isTablet) 18.sp else 14.sp,
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = nextAmountInput,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() }) {
                val num = newValue.toIntOrNull() ?: 0
                if (num <= remainingAmount) {
                    onAmountChange(newValue)
                }
            }
        },
        label = { Text("Belopp att betala") },
        suffix = { Text("kr", fontSize = if (isTablet) 20.sp else 16.sp) },
        modifier = Modifier
            .fillMaxWidth(if (isTablet) 0.4f else 1f),
        textStyle = LocalTextStyle.current.copy(
            fontSize = if (isTablet) 24.sp else 18.sp,
            fontWeight = FontWeight.Bold
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            focusedLabelColor = Color.Black,
            cursorColor = Color.Black
        )
    )
}
}
