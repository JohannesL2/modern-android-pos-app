package com.example.test_design.components.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.sp


@Composable
fun DiscountCode (
    isValid: (String) -> Boolean,
    appliedAmount: Int,
    onApply: (String) -> Unit
) {
    var discountCode by remember { mutableStateOf("") }

    var hasAttemptedApply by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val isEnabled = discountCode.isNotBlank()

    val isError = hasAttemptedApply && !isValid(discountCode) && discountCode.isNotBlank()

    appliedAmount > 0 && !isError

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            OutlinedTextField(
                value = discountCode,
                onValueChange = {
                    discountCode = it
                    if (hasAttemptedApply) hasAttemptedApply = false
                    },
                isError = isError,
                placeholder = { Text("Rabattkod") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.3f),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (discountCode.isNotBlank()) {
                            hasAttemptedApply = true
                            onApply(discountCode)
                            focusManager.clearFocus()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    unfocusedBorderColor = Color.LightGray,
                    errorBorderColor = Color.Red,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color(0xFFDAD6F5)
                )
            )

            Button(
                shape = RoundedCornerShape(12.dp),
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                onClick = {
                    hasAttemptedApply = true
                    onApply(discountCode)
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Använd",
                    fontSize = 16.sp)
            }
        }

        Box(modifier = Modifier.padding(start = 8.dp, top = 4.dp).height(20.dp)) {
        if (isError) {
            Text("Ogiltig rabattkod",
                color = Color.Red,
                fontSize = 14.sp
            )
        }
        }
    }
}