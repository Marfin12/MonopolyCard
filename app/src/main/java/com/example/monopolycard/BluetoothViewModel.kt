package com.example.monopolycard

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.monopolycard.bluetooth.BTClient
import com.example.monopolycard.bluetooth.BTServer
import com.example.monopolycard.bluetooth.BTState
import kotlin.coroutines.coroutineContext

class BluetoothViewModel() : ViewModel() {
    private val _stepCode = MutableLiveData("")
    private val stepCode: LiveData<String>
        get() = _stepCode

    private var serverClass: BTServer? = null
    private var clientClass: BTClient? = null

    private var handler: Handler = Handler { msg ->
        when (msg.what) {
            BTState.STATE_MESSAGE_RECEIVED -> {
                val readBuffer = msg.obj as ByteArray
                val tempMessage = String(readBuffer, 0, msg.arg1)

                _stepCode.value = tempMessage
            }
            else -> {
                _stepCode.value = "Something wrong!!"
            }
        }
        true
    }

    fun createHost(bluetoothAdapter: BluetoothAdapter) {
        serverClass = BTServer(bluetoothAdapter, handler)
        serverClass?.start()
    }

    fun joinHost(context: Context, bluetoothDevice: BluetoothDevice) {
        clientClass = BTClient(context, bluetoothDevice, handler)
        clientClass?.start()
    }
}