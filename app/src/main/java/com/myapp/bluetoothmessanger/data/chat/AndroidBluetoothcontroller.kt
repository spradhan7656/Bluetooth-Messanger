package com.myapp.bluetoothmessanger.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.myapp.bluetoothmessanger.domain.chat.BluethoohDevices
import com.myapp.bluetoothmessanger.domain.chat.BluetoothController
import com.myapp.bluetoothmessanger.domain.chat.BluetoothDeviceDomain
import com.myapp.bluetoothmessanger.domain.chat.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()


    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }


    private val bluetoothStateReceiver = BluetoothStateReceiver{ isConnected,bluetoothDevice ->
        if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice)==true){
            _isConnected.update { isConnected }
        }else{
            CoroutineScope(Dispatchers.IO).launch{
                _errors.emit("Cannot connect to a non-paired devices")
            }

        }
    }

    private var currentServerSocket : BluetoothServerSocket?=null
    private var currentClientSocket : BluetoothSocket?=null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
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

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                Manifest.permission.BLUETOOTH
            }
            if (!hasPermission(permission)) {
               throw SecurityException("No Bluetooth Connect Permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop){
                currentClientSocket = try {
                    currentServerSocket?.accept()
                }catch (e:Exception){
                    shouldLoop=false
                    null
                }

                emit(ConnectionResult.ConnectionEstablished)

                currentClientSocket?.let {
                    currentServerSocket?.close()
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(devices: BluetoothDeviceDomain): Flow<ConnectionResult> {

        return flow {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                Manifest.permission.BLUETOOTH
            }
            if (!hasPermission(permission)) {
                throw SecurityException("No Bluetooth Connect Permission")
            }

            val bluetoothDevice = bluetoothAdapter
                ?.getRemoteDevice(devices.address)

            currentClientSocket = bluetoothDevice
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice)==false){

            }
            currentClientSocket?.let {socket->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)


                }catch (e : Exception){
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
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

    companion object {
        const val SERVICE_UUID = "00e4d51f-75b6-4c6b-a926-7ed574406614"
    }
}