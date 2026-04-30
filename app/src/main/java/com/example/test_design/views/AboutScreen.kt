package com.example.test_design.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.zIndex
import com.example.test_design.R
import com.example.test_design.viewmodels.PaymentViewModel
import com.example.integration.api.model.DeviceInfo
import com.example.integration.api.model.MerchantInfo

private var currentToast: Toast? = null

@Composable
fun AboutScreen(
    paymentViewModel: PaymentViewModel,
    onClose: () -> Unit
) {
    val deviceInfoState = paymentViewModel.deviceInfo.collectAsState(initial = null)

    AboutScreen(
        deviceInfo = deviceInfoState.value,
        onClose = onClose
        )
}

@Composable
fun AboutScreen(
    deviceInfo: DeviceInfo?,
    onClose: () -> Unit
    ) {

    if (deviceInfo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        InfoCard(title = stringResource(R.string.about_device_info_title)) {
            InfoRow(stringResource(R.string.about_payment_app_name), deviceInfo.appName)
            InfoRow(stringResource(R.string.about_payment_app_version), deviceInfo.appVersion)
            InfoRow(stringResource(R.string.about_psdk_version), deviceInfo.psdkVersion)
            InfoRow(stringResource(R.string.about_serial_number), deviceInfo.serialNumber)
            InfoRow(stringResource(R.string.about_manufacturer), deviceInfo.manufacturer)
            InfoRow(stringResource(R.string.about_model), deviceInfo.model)
            InfoRow(stringResource(R.string.about_os_version), deviceInfo.osVersion)
        }

        deviceInfo.merchants.forEachIndexed { index, merchant ->
            MerchantCard(
                merchant = merchant,
                index = index
            )
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.BottomEnd)
            .padding(horizontal = 28.dp, vertical = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .zIndex(2f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black, CircleShape)
                    .clip(CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.menu_desc_close),
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
    }

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun MerchantCard(
    merchant: MerchantInfo,
    index: Int
) {
    InfoCard(title = stringResource(R.string.about_merchant_info_title)) {

        InfoRow(stringResource(R.string.about_name), merchant.name)

        InfoRow(
            stringResource(R.string.about_merchant_id),
            merchant.merchantId.joinToString(", ")
        )

        InfoRow(
            stringResource(R.string.about_currencies),
            merchant.currencies.joinToString(", ")
        )

        InfoRow(stringResource(R.string.about_address), merchant.address)

        InfoRow(stringResource(R.string.about_phone_number), merchant.phoneNumber)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoRow(title: String, value: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val notAvailable = stringResource(R.string.about_not_available)
    val copiedMessage = stringResource(R.string.about_copied_message)

    val safeValue = value.trim().ifEmpty { notAvailable }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (safeValue != notAvailable) {
                        clipboardManager.setText(AnnotatedString(safeValue))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        currentToast?.cancel()

                        currentToast = Toast.makeText(
                            context,
                            copiedMessage.format(safeValue),
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                }
            )
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = value.ifEmpty { notAvailable },
            style = MaterialTheme.typography.bodyLarge
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
