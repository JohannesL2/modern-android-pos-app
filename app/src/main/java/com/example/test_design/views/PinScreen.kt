package com.example.test_design.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.test_design.domain.models.CartItem
import com.example.test_design.R
import com.example.test_design.data.dao.ProductDao
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.res.stringResource

@Composable
fun PinScreen(
    navController: NavController,
    cart: SnapshotStateList<CartItem>,
    dao: ProductDao?,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    val total = cart.sumOf { it.product.price * it.quantity }

    var showCancelDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val haptic = LocalHapticFeedback.current

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun triggerHaptic(ms: Long, amplitude: Int = 100) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, amplitude))
        } else {
            vibrator.vibrate(ms)
        }
    }

    Box(modifier = Modifier.fillMaxSize() .background(Color.White) .padding(bottom = 20.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ic_contactless),
            contentDescription = stringResource(R.string.desc_contactless_payment),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 24.dp)
                .padding(vertical = 8.dp)
                .size(60.dp)
                .scale(scale),
            colorFilter = ColorFilter.tint(Color.Gray)
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 60.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.pin_amount_to_pay),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Text(
            text = stringResource(R.string.pin_amount_format, total),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val isFilled = index < pin.length

                val dotScale by animateFloatAsState(
                    targetValue = if (isFilled) 1.0f else 0.8f,
                    animationSpec = tween(150),
                    label = "dotScale"
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 2.dp,
                            color = if (isFilled) Color.Black else Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFilled) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(dotScale)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Black)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#"),
            listOf("X", "⌫", "OK")
        )

        buttons.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                row.forEach { label ->
                    val isNumericKey = label.all { it.isDigit() } || label == "*" || label == "#"

                    val isEnabled = when (label) {
                        "⌫" -> pin.isNotEmpty()
                        "OK" -> pin.length == 4
                        "X" -> true
                        else -> true
                    }

                    val buttonColor = when {
                        label == "OK" && isEnabled -> Color(0xFF54E35E)
                        label == "⌫" -> Color(0xFFFFD85F)
                        label == "X" -> Color(0xFFF15353)
                        isNumericKey -> Color.Black
                        else -> Color.LightGray.copy(alpha = 0.4f)
                    }


                    val contentColor = when {
                        isNumericKey || (label == "OK" && isEnabled) -> Color.White
                        else -> Color.Black
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                            when (label) {
                                "OK" -> if(pin.length == 4) triggerHaptic(40, 200) else triggerHaptic(80, 40)
                                "X" -> triggerHaptic(30, 150)
                                else -> triggerHaptic(15)
                            }

                            when (label) {

                                "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)

                                "X" -> showCancelDialog = true

                                "OK" -> if (pin.length == 4) {
                                    onPinEntered(pin)
                                    showConfirmation = true
                                }

                                else -> if (pin.length < 4) pin += label
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        enabled = true,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = contentColor
                        )
                    ) {
                        when (label) {
                            "⌫" -> {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.desc_backspace),
                                    tint = Color.Black,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            "OK" -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_return),
                                    contentDescription = stringResource(R.string.desc_confirm),
                                    tint = Color.Black,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            "X" -> Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.desc_cancel),
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp),
                            )
                            else -> {
                                Text(
                                    text = label,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        if (showCancelDialog) {
            Dialog(onDismissRequest = { showCancelDialog = false }) {
                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.pin_cancel_confirm_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                onClick = { showCancelDialog = false }) {
                                Text(
                                    text = stringResource(R.string.common_no),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Color.Black
                                    )
                            }
                            OutlinedButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                onClick = {
                                cart.clear()
                                navController.navigate("main") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }) {
                                Text(
                                    text = stringResource(R.string.common_yes),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showConfirmation && dao != null) {
            Dialog(onDismissRequest = { showConfirmation = false }) {
                PaymentConfirmation(
                    cart = cart,
                    navController = navController
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PinScreenPreview() {
    // 1. Skapa en fejkad varukorg med ett föremål så vi ser ett pris
    val dummyCart = remember { mutableStateListOf<CartItem>() }

    // 2. Skapa en fejkad NavController (fungerar i Preview)
    val navController = rememberNavController()

    // 3. Vi skickar in DAO som null, men vi mappar om PinScreen
    // så den inte krashar om dao är null i preview-läge.
    PinScreen(
        navController = navController,
        cart = dummyCart,
        // Vi castar null till ProductDao för att kompilatorn ska bli nöjd
        dao = null,
        onPinEntered = {}
    )
}