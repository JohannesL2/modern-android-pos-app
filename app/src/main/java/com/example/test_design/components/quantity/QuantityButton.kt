package com.example.test_design.components.quantity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

@Composable
private fun QuantityButtonBase(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .size(36.dp)
            .background(backgroundColor, shape = RoundedCornerShape(50))
            .clip(shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
fun AddQuantityButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuantityButtonBase(
        text = "+",
        onClick = onClick,
        backgroundColor = Color.White,
        contentColor = Color.Black,
        modifier = modifier
    )
}
@Composable
fun RemoveQuantityButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
   QuantityButtonBase(
            text = "–",
            onClick = onClick,
            modifier = modifier,
            backgroundColor = Color.White,
            contentColor = Color.Black
        )
    }