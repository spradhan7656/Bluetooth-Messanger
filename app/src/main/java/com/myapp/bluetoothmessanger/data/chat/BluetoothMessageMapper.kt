package com.myapp.bluetoothmessanger.data.chat

import com.myapp.bluetoothmessanger.domain.chat.BluetoothMessage


fun String.toBluetoothMessage(isFromLocalUser:Boolean):BluetoothMessage{
    val name= substringBeforeLast("#")
    val message = substringAfter("#")
    return BluetoothMessage(
        message = message,
        senderName = name,
        isFromLocalUser = isFromLocalUser
    )
}

fun BluetoothMessage.toByteArray():ByteArray{
    return "$senderName#$message".encodeToByteArray()
}