package com.example.test_design.components.overlays

import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.example.test_design.R
import kotlinx.coroutines.delay
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Backspace

@Composable
fun SellerLogin(
    navController: NavController,
    onDismiss: () -> Unit,
    onPinVerified: (String) -> Unit,
    isAlreadyLoggedIn: Boolean = false,
    onLock: (() -> Unit)? = null
) {
    rememberCoroutineScope()

    val scope = rememberCoroutineScope()
    var errorFlash by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    val maxLength = 4

    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    val interactionMap = remember {
        buttons.flatten()
            .filter { it.isNotEmpty() }
            .associateWith { MutableInteractionSource() }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (errorFlash) Color(0x80FF0000) else Color.White
    )

    val context = LocalContext.current
    val view = LocalView.current
    val window = (context as? ComponentActivity)?.window

    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    val haptic = LocalHapticFeedback.current

    val isInPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    val successSound = remember {
        if (isInPreview) null
        else try {
        MediaPlayer.create(context, R.raw.access_succeed)
        } catch (e: Exception) {
            null
        }
    }

    val lockSound = remember {
        if (isInPreview) null
        else try {
        MediaPlayer.create(context, R.raw.beep_denied)
        } catch (e: Exception) {
            null
        }
    }

    SideEffect {
        successSound?.setOnCompletionListener { it.seekTo(0) }
        lockSound?.setOnCompletionListener { it.seekTo(0) }
    }

    remember { window?.navigationBarColor }
    remember {
        window?.let { WindowInsetsControllerCompat(it, it.decorView).isAppearanceLightStatusBars }
    }

    SideEffect {
        window?.let { win ->
            win.navigationBarColor = 0xFF1E1E2F.toInt()
            val insetsController = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
            insetsController?.isAppearanceLightNavigationBars = false
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == maxLength) {
            if (pin == "1234") {
                successSound?.start()
                onPinVerified(pin)  // loggar in direkt
                pin = ""             // nollställ PIN efter inloggning
            } else {
                lockSound?.start()
                errorFlash = true
                delay(700)
                errorFlash = false
                pin = ""
            }
        }
    }

    val focusRequester = remember { FocusRequester() }

    //Begär fokus när komponenten startas
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val label = when (keyEvent.key) {
                        Key.Zero, Key.NumPad0 -> "0"
                        Key.One, Key.NumPad1 -> "1"
                        Key.Two, Key.NumPad2 -> "2"
                        Key.Three, Key.NumPad3 -> "3"
                        Key.Four, Key.NumPad4 -> "4"
                        Key.Five, Key.NumPad5 -> "5"
                        Key.Six, Key.NumPad6 -> "6"
                        Key.Seven, Key.NumPad7 -> "7"
                        Key.Eight, Key.NumPad8 -> "8"
                        Key.Nine, Key.NumPad9 -> "9"
                        Key.Backspace -> "⌫"
                        else -> null
                    }

                    label?.let { l ->
                        if (l == "⌫") {
                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                        } else if (pin.length < maxLength) {
                            pin += l
                        }

                        scope.launch {
                            val press = androidx.compose.foundation.interaction.PressInteraction.Press(androidx.compose.ui.geometry.Offset.Zero)
                            interactionMap[l]?.emit(press)
                            delay(100)
                            interactionMap[l]?.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                        }
                        return@onKeyEvent true
                    }
                }
                false
            },
        contentAlignment = Alignment.Center
    ) {

        Card(
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .background(backgroundColor)
                    .padding(top = 26.dp)
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val isLoggedIn by rememberUpdatedState(newValue = isAlreadyLoggedIn)
                if (!isLoggedIn) {
                    Text(
                        text = stringResource(R.string.login_header),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.login_instruction),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )

                    Spacer(
                        modifier = Modifier
                            .height(32.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    )
                    {
                        repeat(maxLength) { index ->
                            val isFilled = index < pin.length

                            val dotScale by animateFloatAsState(
                                targetValue = if (isFilled) 1.0f else 0.8f,
                                animationSpec = tween(150),
                                label = "dotScale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .border(
                                        width = 2.dp,
                                        color = if (isFilled) Color.Black else Color.Black.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .background(
                                        if (isFilled)
                                        Color.Black else {
                                            Color.Transparent
                                        }
                                    )
                                    .padding(4.dp),
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

                    val buttonSize = if (isTablet) 90.dp else 84.dp
                    val horizontalSpacing = if (isTablet) 28.dp else 18.dp
                    val verticalSpacing = if (isTablet) 12.dp else 10.dp

                    buttons.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing, Alignment.CenterHorizontally),
                            modifier = Modifier
                                .padding(vertical = verticalSpacing)
                                .then(
                                    if (isTablet)
                                        Modifier.width(350.dp)
                                        else Modifier.fillMaxWidth())
                        ) {
                            row.forEach { label ->
                                val interactionSource = interactionMap[label] ?: remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                val buttonColor by animateColorAsState(
                                    targetValue = if (isPressed) Color(0xFFD1D1D1) else Color(0xFFF7F7F7),
                                    animationSpec = tween(durationMillis = 100),
                                    label = "buttonColor"
                                )

                                val scale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.96f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "scale"
                                )

                                val isBack = label == "⌫"
                                val isOk = label == "OK"
                                val isSpace = label.isEmpty()
                                label == "PATTERN"
                                val enabled = when {
                                    isSpace -> false
                                    isOk -> pin.length == maxLength
                                    else -> pin.length < maxLength
                                }

                                Button(
                                    onClick = {
                                        if (isSpace) return@Button

                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)


                                        when {
                                            isBack && pin.isNotEmpty() -> {
                                                pin = pin.dropLast(1)
                                            }

                                            !isBack && !isOk && pin.length < maxLength -> pin += label
                                        }
                                        view.postDelayed({ }, 100)
                                    },
                                    modifier = Modifier
                                        .size(buttonSize)
                                        .scale(scale),
                                    shape = RoundedCornerShape(50.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    enabled = enabled,
                                    interactionSource = interactionSource,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            label.isEmpty() -> Color.Transparent
                                            label == "⌫" -> Color(0xFFFFD85F)
                                            else -> buttonColor
                                        },
                                        contentColor = when {
                                            isSpace || isOk -> Color.Transparent
                                            isBack -> Color.Black
                                            else -> Color.Black
                                        },
                                        disabledContainerColor = Color.Transparent,
                                        disabledContentColor = Color.Transparent
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    when (label) {
                                        "⌫" -> {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Back",
                                                tint = if (errorFlash) Color.Transparent else Color.Black,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(end = 4.dp)
                                            )
                                        }

                                        "OK" -> {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_keyboard_return),
                                                contentDescription = "Enter",
                                                tint = Color.Black,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

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
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                            contentAlignment = Alignment.Center
                    ) {

//                        Image(
//                            painter = painterResource(R.drawable.lockscreen),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .fillMaxSize(),
//                            contentScale = ContentScale.Crop
//                        )

                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(horizontal = 16.dp)
                            ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stäng",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(56.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    lockSound?.start()
                                    pin = ""
                                    onLock?.invoke()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFF5722
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(0.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Lås kassan",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lås kassan",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    navController.navigate("refund") {
                                        popUpTo("refund") { inclusive = true }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(contentColor = Color.Black),
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                            ) {
                                Text("Refund", color = Color.White)
                            }

                            Button(
                                onClick = { onDismiss() },
                                colors = ButtonDefaults.buttonColors(contentColor = Color.Gray),
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                            ) {
                                Text("Avbryt", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}