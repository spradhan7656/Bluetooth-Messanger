package com.myapp.bluetoothmessanger.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.myapp.bluetoothmessanger.domain.chat.BluetoothController
import com.myapp.bluetoothmessanger.domain.chat.BluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBluetoothcontroller(
    private val context: Context
) : BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        Log.d("BluetoothController", "startDiscovery called")
        val scanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.BLUETOOTH_ADMIN
        }
        if (!hasPermission(scanPermission)) {
            Log.d("BluetoothController", "Bluetooth scan permission denied")
            return
        }

        // Check location permission for Android <12
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val hasLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (!hasLocation) {
                Log.d("BluetoothController", "Location permission required for scanning")
                return
            }
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        bluetoothAdapter?.startDiscovery()
        Log.d("BluetoothController", "startDiscovery finished")
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    override fun updatePairedDevices() {
        Log.d("BluetoothController", "updatePairedDevices called")
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        }
        if (!hasPermission(permission)) {
            Log.d("BluetoothController", "Permission $permission denied")
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                Log.d("BluetoothController", "Paired devices: $devices")
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        val hasPerm = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        Log.d("BluetoothController", "Permission $permission: $hasPerm")
        return hasPerm
    }
}