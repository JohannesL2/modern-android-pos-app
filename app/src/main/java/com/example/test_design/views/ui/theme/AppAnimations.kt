package com.example.test_design.views.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

object AppMotion {
    private const val ENTER_DURATION = 300
    private const val EXIT_DURATION = 250
    private const val START_SCALE = 0.95f
    private val standardEasing = LinearOutSlowInEasing

    val PopEnter = fadeIn(
        animationSpec = tween(ENTER_DURATION)
    ) + scaleIn(
        initialScale = START_SCALE,
        animationSpec = tween(ENTER_DURATION, easing = standardEasing)
    )

    val PopExit = fadeOut(
        animationSpec = tween(EXIT_DURATION)
    ) + scaleOut(
        targetScale = START_SCALE,
        animationSpec = tween(EXIT_DURATION)
    )

    val ExpandEnter = expandVertically(
        animationSpec = tween(ENTER_DURATION, easing = standardEasing)
    ) + fadeIn(animationSpec = tween(ENTER_DURATION))

    val CollapseExit = shrinkVertically(
        animationSpec = tween(EXIT_DURATION, easing = standardEasing)
    ) + fadeOut(animationSpec = tween(EXIT_DURATION))
}