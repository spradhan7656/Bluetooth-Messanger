package com.myapp.bluetoothmessanger.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

import com.myapp.bluetoothmessanger.domain.chat.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}