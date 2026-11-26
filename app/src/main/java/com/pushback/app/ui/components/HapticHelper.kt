package com.pushback.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

class TactileFeedback(
    private val haptic: HapticFeedback,
    private val view: android.view.View,
    private val enabled: Boolean = true,
) {
    fun click() {
        if (!enabled) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun stampSlam() {
        if (!enabled) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun tick() {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun success() {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun warning() {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}

@Composable
fun rememberTactileFeedback(enabled: Boolean = true): TactileFeedback {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(haptic, view, enabled) {
        TactileFeedback(haptic, view, enabled)
    }
}
