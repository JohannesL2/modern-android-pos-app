package com.example.test_design

import android.app.Application
import com.example.integration.api.ApiModule
import com.example.integration.api.model.TerminalConnectionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.example.test_design.scancontrol.ScanSoundPlayer
import com.example.integration.api.model.NetworkConfiguration

class App : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ScanSoundPlayer.init(this)
        ApiModule.setUseEmulatedTerminal(BuildConfig.USE_EMULATED_TERMINAL)
        if (!BuildConfig.USE_EMULATED_TERMINAL) {
            if (BuildConfig.USE_LOCAL_TERMINAL) {
                ApiModule.setTerminalConnectionConfig(TerminalConnectionConfig.OnDevice)
            } else {
                ApiModule.setTerminalConnectionConfig(
                    TerminalConnectionConfig.TcpIpClient(
                        address = BuildConfig.OFF_DEVICE_TERMINAL_IP,
                        forgetPersistedDevice = true,
                        networkConfiguration = NetworkConfiguration.STATIC,
                    )
                )
            }
        }
        ApiModule.initialize(this)
        ApiModule.start(appScope)
    }
}