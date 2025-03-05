package com.myapp.bluetoothmessanger.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val isConnected:StateFlow<Boolean>

    val scannedDevices: StateFlow<List<BluethoohDevices>>
    val pairedDevices: StateFlow<List<BluethoohDevices>>

    val errors:SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>

    fun connectToDevice(devices: BluethoohDevices):Flow<ConnectionResult>

    fun closeConnection()

    fun release()
    fun updatePairedDevices()
}