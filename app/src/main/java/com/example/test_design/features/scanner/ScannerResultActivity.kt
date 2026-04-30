package com.example.test_design.features.scanner

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.example.test_design.components.overlays.ScannerOverlay
import kotlinx.coroutines.delay

class ScannerResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scannedCode = intent.getStringExtra("SCANNED_CODE") ?: ""

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setWindowAnimations(0)

        setContent {
            LaunchedEffect(Unit) {
                delay(2000)
                finish()
            }

            ScannerOverlay(
                scannedCode = scannedCode,
                onDismiss = { finish() },
            )
        }
    }
}