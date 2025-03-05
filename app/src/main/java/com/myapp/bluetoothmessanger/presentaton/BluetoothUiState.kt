package com.myapp.bluetoothmessanger.presentaton

import com.myapp.bluetoothmessanger.domain.chat.BluethoohDevices


data class BluetoothUiState(
    val scannedDevices: List<BluethoohDevices> = emptyList(),
    val pairedDevices: List<BluethoohDevices> = emptyList(),
    val isConnected:Boolean = false,
    val isConnecting:Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null


)