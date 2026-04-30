package com.example.test_design.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.navigation.NavController
import com.example.test_design.domain.models.CartItem
import com.example.test_design.components.overlays.PrintingLoading
import com.example.test_design.viewmodels.PaymentViewModel
import com.example.test_design.viewmodels.RefundViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.test_design.R
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.delay
import com.example.test_design.BuildConfig
import androidx.compose.foundation.shape.RoundedCornerShape

enum class FeedbackFlowType {
    PAYMENT,
    REFUND
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedBackScreen (
    navController: NavController,
    flowType: FeedbackFlowType,
    isSuccess: Boolean,
    cart: SnapshotStateList<CartItem>,
    appliedCode: String,
    discountAmount: Int,
    paymentViewModel: PaymentViewModel,
    refundViewModel: RefundViewModel? = null,
    onLock: (() -> Unit)?,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPrinting = remember { mutableStateOf(false) }

    val title = when (flowType) {
        FeedbackFlowType.PAYMENT -> stringResource(R.string.feedback_title_payment)
        FeedbackFlowType.REFUND -> stringResource(R.string.feedback_title_refund)
    }
    val successButton = stringResource(R.string.feedback_btn_print)
    val continueButtonText = stringResource(R.string.feedback_btn_skip)

    val successButtonColor = Color(0xFF2E7D32).copy(alpha = 0.85f)
    val paymentInfo = paymentViewModel.lastPaymentInfo

    val onContinue = {
        if (flowType == FeedbackFlowType.PAYMENT) {
            cart.clear()
        } else {
            refundViewModel?.clearCompletedRefund()
        }

        onLock?.let { it() }

        navController.navigate("main") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val onPrintReceipt: () -> Unit = printReceipt@{
        if (!isSuccess) return@printReceipt

        scope.launch {
            isPrinting.value = true
            val startTime = System.currentTimeMillis()

            try {
                if (!BuildConfig.USE_LOCAL_TERMINAL) {
                    val order = paymentViewModel.lastOrder
                    if (order != null) {
                        paymentViewModel.printReceiptWithExternalPrinter(
                            context = context,
                            order = order,
                            slipHtml = paymentViewModel.lastPaymentInfo,
                            appliedCode = appliedCode,
                            discountAmount = discountAmount
                        )
                    }
                } else {
                    withContext(kotlinx.coroutines.Dispatchers.IO) {
                        when (flowType) {
                            FeedbackFlowType.PAYMENT -> {
                                val order = paymentViewModel.lastOrder ?: return@withContext
                                paymentViewModel.testPrintFromCart(
                                    context,
                                    paymentInfo = paymentInfo,
                                    appliedCode = appliedCode,
                                    discountAmount = discountAmount,
                                    order = order
                                )
                            }

                            FeedbackFlowType.REFUND -> {
                                refundViewModel?.printCompletedRefundReceipt(context)
                            }
                        }
                    }
                }

                val elapsed = System.currentTimeMillis() - startTime
                val minDuration = 4000L
                if (elapsed < minDuration) {
                    delay(minDuration - elapsed)
                }

            } catch (e: Exception) {
                Log.e("FEEDBACK_PRINT", "PRINT failed", e)
            } finally {
                isPrinting.value = false
                onContinue()
            }
        }
    }

    val feedbackGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
        0.0f to Color.White,
        0.2f to Color(0xFFF1F9F1),
        1.0f to Color(0xFFD0E8D2)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 600.dp
        val contentWidthModifier = if (isTablet) Modifier.width(500.dp) else Modifier.fillMaxWidth()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(feedbackGradient)
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
                Column(
                    modifier = Modifier
                        .then(if (isTablet) Modifier.width(500.dp) else Modifier.fillMaxWidth())
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(if (isTablet) 180.dp else 120.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(if (isTablet) 120.dp else 80.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.desc_success_check),
                                tint = Color.White,
                                modifier = Modifier.size(if (isTablet) 54.dp else 36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isTablet) 48.dp else 32.dp))

                    Text(
                        text = title,
                        fontSize = if (isTablet) 36.sp else 28.sp,
                        fontWeight = Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = if (isTablet) 44.sp else 34.sp
                    )

                    Spacer(modifier = Modifier.height(if (isTablet) 100.dp else 80.dp))

                    Button(
                        onClick = onPrintReceipt,
                        colors = ButtonDefaults.buttonColors(containerColor = successButtonColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 72.dp else 56.dp)
                            .padding(horizontal = 32.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = successButton,
                            color = Color.White,
                            fontSize = if (isTablet) 24.sp else 20.sp,
                            fontWeight = Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 16.dp))

                    androidx.compose.material3.OutlinedButton(
                        onClick = onContinue,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color.Black.copy(alpha = 0.35f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 72.dp else 56.dp)
                            .padding(horizontal = 32.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = continueButtonText,
                            color = Color.Black.copy(alpha = 0.5f),
                            fontSize = if (isTablet) 22.sp else 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
        }

        if (isPrinting.value) {
            PrintingLoading(
                isPrinting = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedBackScreenSimplePreview() {
    LocalContext.current

    // Minimala "fusk-objekt" för att previewn inte ska krascha
    val dummyNavController = androidx.navigation.compose.rememberNavController()
    val dummyCart = remember { SnapshotStateList<CartItem>() }
    remember { mutableStateOf(false) }

    // Vi skapar en tom ViewModel (kräver att den har en tom konstruktor)
    // Om din PaymentViewModel kräver parametrar, använd en mock eller
    // gör om parametern i FeedBackScreen till ett interface.
    val dummyViewModel = remember { PaymentViewModel() }

    MaterialTheme {
        FeedBackScreen(
            navController = dummyNavController,
            flowType = FeedbackFlowType.PAYMENT,
            isSuccess = true, // Sätt till true för att se innehållet!
            cart = dummyCart,
            appliedCode = "TEST10",
            discountAmount = 10,
            paymentViewModel = dummyViewModel,
            onLock = {},
        )
    }
}
