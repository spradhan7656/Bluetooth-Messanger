package com.myapp.bluetoothmessanger.presentaton

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapp.bluetoothmessanger.presentaton.component.DeviceScreen
import com.myapp.bluetoothmessanger.ui.theme.BluetoothMessangerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleBluetoothEnableResult()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BluetoothMessangerTheme {
                val viewModel: BluetoothViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val coroutineScope = rememberCoroutineScope()


                LaunchedEffect(key1 = state.errorMessage) {
                    state.errorMessage?.let { message->
                        Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
                    }
                }

                LaunchedEffect(key1 = state.isConnected) {
                    if(state.isConnected){
                        Toast.makeText(applicationContext,"You are connected",Toast.LENGTH_LONG).show()
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    when{
                        state.isConnecting->{
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                CircularProgressIndicator()
                                Text(text = "Connecting...")
                            }
                        }else->{
                        DeviceScreen(
                            state = state,
                            onStartScan = {
                                coroutineScope.launch { handleScanAction(viewModel) }
                            },
                            onStopScan = viewModel::stopScan,
                            onDeviceClick = viewModel::connectedToDevice,
                            onStartServer = viewModel::waitForInComingConnections
                        )
                        }
                    }


                }

                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        checkInitialState(viewModel)
                    }
                }
            }
        }
    }

    private suspend fun checkInitialState(viewModel: BluetoothViewModel) {
        when {
            isBluetoothEnabled && hasRequiredPermissions() -> {
                viewModel.updatePairedDevices()
                viewModel.startScan()
            }

            isBluetoothEnabled -> requestBluetoothPermissions()
            else -> enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private suspend fun handleScanAction(viewModel: BluetoothViewModel) {
        if (!isBluetoothEnabled) {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isLocationEnabled()) {
            Toast.makeText(this, "Enable location services to scan", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (hasRequiredPermissions()) {
            viewModel.startScan()
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun handleBluetoothEnableResult() {
        if (isBluetoothEnabled) {
            requestBluetoothPermissions()
        } else {
            Toast.makeText(this, "Bluetooth is required to continue", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied: ${permissions.keys}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }.filter {
            ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LocationManager::class.java)
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
