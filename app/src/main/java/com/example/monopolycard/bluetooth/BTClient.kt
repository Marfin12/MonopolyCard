package com.example.monopolycard.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Message
import androidx.core.app.ActivityCompat
import com.example.monopolycard.bluetooth.BTConstant.MY_UUID
import com.example.monopolycard.bluetooth.BTState.STATE_CONNECTED
import com.example.monopolycard.bluetooth.BTState.STATE_CONNECTION_FAILED
import java.io.IOException

class BTClient(
    context: Context,
    device: BluetoothDevice,
    handler: Handler
) : Thread() {
    private var socket: BluetoothSocket? = null
    var sendReceive: BTSendReceive? = null
    private lateinit var handler: Handler
    private lateinit var context: Context

    override fun run() {
        try {

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                socket?.connect()

                val message: Message = Message.obtain()
                message.what = STATE_CONNECTED
                handler.sendMessage(message)

                if (socket != null) {
                    sendReceive = BTSendReceive(socket!!, handler)
                    sendReceive?.start()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()

            val message: Message = Message.obtain()

            message.what = STATE_CONNECTION_FAILED

            handler.sendMessage(message)
        }
    }

    @JvmName("getSendReceive1")
    fun getSendReceive(): BTSendReceive? {
        return sendReceive
    }

    init {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                this.handler = handler
                this.context = context
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}