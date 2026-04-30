package com.example.test_design.preview

import com.example.test_design.ui.theme.TestdesignTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import com.example.test_design.components.base.buttons.WideButton

@Preview(showBackground = true)
@Composable
fun WideButtonPreview() {
    TestdesignTheme() {
        WideButton(
            backgroundColor = Color(0xFF6200EE),
            text = "Generic",
            onClick = {}
        )
    }
}
