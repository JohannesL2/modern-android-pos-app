package com.example.test_design.scancontrol

import android.os.SystemClock
import com.example.test_design.data.utils.normalizeBarcode

/**
 * Prevents repeated handling of the same scanner value within a cooldown window.
 */
class ScanCooldownGate(
    private val cooldownMs: Long
) {
    private val lastHandledAt = mutableMapOf<String, Long>()

    fun shouldHandle(rawValue: String): Boolean {
        val key = normalizeBarcode(rawValue)
        if (key.isEmpty()) return false

        val now = SystemClock.elapsedRealtime()
        val last = lastHandledAt[key]
        if (last != null && now - last < cooldownMs) {
            return false
        }

        lastHandledAt[key] = now
        return true
    }
}
