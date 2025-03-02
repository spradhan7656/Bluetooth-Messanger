package com.myapp.bluetoothmessanger.domain.chat

import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluethoohDevices>>
    val pairedDevices: StateFlow<List<BluethoohDevices>>

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
    fun updatePairedDevices()
}