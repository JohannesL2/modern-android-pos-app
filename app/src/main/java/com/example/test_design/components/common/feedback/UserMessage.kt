package com.example.test_design.components.common.feedback

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

enum class MessageType {
    Error,
    Success,
    Info
}

data class UiMessage(
    val text: String,
    val type: MessageType
)

@Composable
fun UserMessage(
    message: UiMessage?,
  // modifier: Modifier = Modifier
) {
    if (message == null) return

    val color = when (message.type) {
        MessageType.Error -> Color.Red
        MessageType.Success -> Color(0xFF2E7D32)
        MessageType.Info -> Color.Gray
    }

    Text(
        text = message.text,
        color = color,
        fontSize = 16.sp,
        //modifier = modifier
    )
}
