package com.example.test_design.performance

import android.app.Activity
import android.util.Log
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState

class JankTracker(private val activity: Activity) {
    private var jankStats: JankStats? = null
    private val holder = PerformanceMetricsState.getHolderForHierarchy(activity.window.decorView)

    fun startTracking() {
        val listener = JankStats.OnFrameListener { frameData ->
            if (frameData.isJank) {
                logJank(frameData)
            }
        }

        jankStats = JankStats.createAndTrack(activity.window, listener)
        jankStats?.isTrackingEnabled = true
    }

    fun stopTracking() {
        jankStats?.isTrackingEnabled = false
    }

    fun setContext(key: String, value: String) {
        holder.state?.putState(key, value)
    }

    fun removeContext(key: String) {
        holder.state?.removeState(key)
    }

    private fun logJank(frameData: androidx.metrics.performance.FrameData) {
        val states = frameData.states.joinToString { "${it.key}: ${it.value}" }

        val durationMs = frameData.frameDurationUiNanos / 1_000_000

        Log.w("POS_JANK", "Jank upptäckt! Tid: ${durationMs}ms | Kontext: [$states]")
    }
}