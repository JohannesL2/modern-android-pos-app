package com.example.test_design.components.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentStatusCard(
    remainingAmount: Int,
    totalAmount: Int,
    paidAmount: Int,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    Surface(
        modifier = Modifier
            .fillMaxWidth(if (isTablet) 0.5f else 1f)
            .padding(vertical = 16.dp),
        color = Color(0xFFF7F7F7),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "KVAR ATT BETALA",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Text(
                text = "$remainingAmount kr",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (remainingAmount <= 0) Color(0xFF2E7D32) else Color.Black
            )

            val progress =
                if (totalAmount > 0) paidAmount.toFloat() / totalAmount.toFloat() else 0f
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.Black,
                trackColor = Color.Black.copy(0.1f)
            )
        }
    }
}