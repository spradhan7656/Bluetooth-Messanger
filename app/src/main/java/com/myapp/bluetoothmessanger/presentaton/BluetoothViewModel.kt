package com.myapp.bluetoothmessanger.presentaton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.bluetoothmessanger.domain.chat.BluetoothController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
):ViewModel (){

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            bluetoothController.startDiscovery()
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, errorMessage = e.message) }
        }finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun stopScan() {
        _state.update { it.copy(isLoading = false) }
        bluetoothController.stopDiscovery()
    }

    fun updatePairedDevices(){
        bluetoothController.updatePairedDevices()
    }
}