package com.myapp.bluetoothmessanger.presentaton.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.provider.FontsContractCompat.Columns
import com.myapp.bluetoothmessanger.domain.chat.BluethoohDevices
import com.myapp.bluetoothmessanger.presentaton.BluetoothUiState

@Composable
fun DeviceScreen(
    state: BluetoothUiState,
    onStartScan:()->Unit,
    onStopScan:()->Unit,
    onDeviceClick:(BluethoohDevices)->Unit,
    onStartServer:()->Unit
) {
   Column(
       modifier = Modifier.fillMaxSize()
   ) {

       BlueToothDeviceList(
           pairedDevices = state.pairedDevices,
           scannedDevices = state.scannedDevices,
           onClick = onDeviceClick,
           modifier = Modifier.fillMaxWidth()
               .weight(1f)
       )

       Row (
           modifier = Modifier.fillMaxWidth(),
           horizontalArrangement = Arrangement.SpaceAround
       ){
           Button(
               onClick = onStartScan
           ) {
               Text(text = "Start Scan")
           }
           Button(
               onClick = onStopScan
           ) {
               Text(text = "Stop Scan")
           }
           Button(
               onClick = onStartServer
           ) {
               Text(text = "Start Server")
           }
       }
   }
}

@Composable
fun BlueToothDeviceList(
    pairedDevices : List<BluethoohDevices>,
    scannedDevices: List<BluethoohDevices>,
    onClick:(BluethoohDevices)->Unit,
    modifier: Modifier= Modifier
){
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = "Paired Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
        items(pairedDevices){devices->
            Text(
                text = devices.name?:"(no name)",
                modifier = Modifier.fillMaxWidth().clickable{onClick(devices)}.padding(16.dp)
            )
        }

        item {
            Text(
                text = "Scanned Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
        items(scannedDevices){devices->
            Text(
                text = devices.name?:"(no name)",
                modifier = Modifier.fillMaxWidth().clickable{onClick(devices)}.padding(16.dp)
            )
        }
    }
}