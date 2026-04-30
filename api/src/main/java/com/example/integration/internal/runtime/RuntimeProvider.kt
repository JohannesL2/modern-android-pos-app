package com.example.integration.internal.runtime

import android.content.Context
import kotlin.isInitialized

internal object RuntimeProvider {

    private lateinit var runtime: SdkRuntime

    fun init(context: Context) {
        if (::runtime.isInitialized) return
        runtime = SdkRuntime(context.applicationContext)
    }

    fun get(): SdkRuntime {
        check(::runtime.isInitialized) { "RuntimeProvider not initialized" }
        return runtime
    }
}