package com.example.integration.api.model


data class MerchantInfo(
    val name: String,
    val merchantId: List<String>,
    val currencies: List<String>,
    val address: String = "N/A",
    val phoneNumber: String = "N/A"
)

data class DeviceInfo(
    val appName: String,
    val appVersion: String,
    val psdkVersion: String,
    val serialNumber: String,
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val merchants: List<MerchantInfo>
)