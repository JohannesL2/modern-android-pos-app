package com.example.integration.api.model

sealed class TerminalConnectionConfig {
    data object Persisted : TerminalConnectionConfig()
    data object OnDevice : TerminalConnectionConfig()

    data class TcpIpClient(
        val address: String,
        val networkConfiguration: NetworkConfiguration = NetworkConfiguration.DYNAMIC,
        val forgetPersistedDevice: Boolean = false
    ) : TerminalConnectionConfig()

    data class TcpIpServer(
        val serialNumber: String? = null
    ) : TerminalConnectionConfig()
}

enum class NetworkConfiguration {
    DYNAMIC,
    STATIC,
    SERVICE_DISCOVERY
}
