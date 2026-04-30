package com.example.test_design.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.test_design.components.quantity.AddQuantityButton
import com.example.test_design.components.quantity.RemoveQuantityButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme

@Composable
fun RefundQuantityControl(
    currentQty: Int,
    maxAvailable: Int,
    onQuantityChange: (Int) -> Unit,
    onInteraction: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(0.05f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        RemoveQuantityButton(
            onClick = {
                if (currentQty > 0) {
                    onQuantityChange(currentQty - 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInteraction()
                }
            }
        )

        Box(
            modifier = Modifier
                .width(32.dp)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentQty.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 18.sp
                ),
            )
        }

        AddQuantityButton(
            onClick = {
                if (currentQty < maxAvailable) {
                    onQuantityChange(currentQty + 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInteraction()
                }
            }
        )
    }

    if (showDialog) {
        RefundQuantityDialog(
            initialQty = currentQty,
            maxAvailable = maxAvailable,
            onDismiss = { },
            onConfirm = { newQty ->
                onQuantityChange(newQty)
                onInteraction()
            }
        )
    }
}

@Composable
fun RefundQuantityDialog(
    initialQty: Int,
    maxAvailable: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(initialQty.toString()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ange antal (Max $maxAvailable)") },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { input ->
                    // Tillåt bara siffror och spärra vid maxAvailable
                    val digits = input.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        textValue = ""
                    } else {
                        val num = digits.toIntOrNull() ?: 0
                        if (num <= maxAvailable) {
                            textValue = digits
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(textValue.toIntOrNull() ?: 0)
            }) {
                Text("Spara")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}