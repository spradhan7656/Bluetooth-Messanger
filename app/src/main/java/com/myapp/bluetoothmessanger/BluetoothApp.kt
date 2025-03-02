package com.myapp.bluetoothmessanger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BluetoothApp:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}