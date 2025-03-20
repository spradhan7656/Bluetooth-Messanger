package com.myapp.bluetoothmessanger.domain.chat

data class BluetoothMessage (
    val message:String,
    val senderName:String,
    val isFromLocalUser:Boolean
)