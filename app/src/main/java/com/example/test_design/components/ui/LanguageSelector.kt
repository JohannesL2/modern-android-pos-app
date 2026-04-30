package com.example.test_design.components.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

private fun changeLanguage(langCode: String) {
    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLang = if (currentLocales.isEmpty) "sv" else currentLocales[0]?.language ?: "sv"

    var isChangingLanguage by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isChangingLanguage) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "FadeOut"
    )

    Column(
        modifier = modifier
            .zIndex(1f)
            .alpha(animatedAlpha),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Språk / Language",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlagButton(
                flag = "🇸🇪",
                isSelected = currentLang == "sv",
                onClick = {
                    if (currentLang != "sv" && !isChangingLanguage) {
                    scope.launch {
                        isChangingLanguage = true
                        delay(250)
                        changeLanguage("sv")
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.width(24.dp))

            FlagButton(
                flag = "🇬🇧",
                isSelected = currentLang == "en",
                onClick = {
                    if (currentLang != "en" && !isChangingLanguage) {
                        scope.launch {
                            isChangingLanguage = true
                            delay(250)
                            changeLanguage("en")
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun FlagButton(
    flag: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF6200EE).copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF6200EE) else Color.LightGray.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flag,
            fontSize = 22.sp
        )
    }
}