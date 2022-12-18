package com.example.monopolycard.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.IOException
import android.os.Message
import com.example.monopolycard.bluetooth.BTConstant.APP_NAME
import com.example.monopolycard.bluetooth.BTConstant.MY_UUID
import com.example.monopolycard.bluetooth.BTState.STATE_CONNECTED
import com.example.monopolycard.bluetooth.BTState.STATE_CONNECTING
import com.example.monopolycard.bluetooth.BTState.STATE_CONNECTION_FAILED


@SuppressLint("MissingPermission")
class BTServer(
    bluetoothAdapter: BluetoothAdapter, handler: Handler
)  : Thread() {
    var sendReceive: BTSendReceive? = null
    private var serverSocket: BluetoothServerSocket? = null
    private lateinit var handler: Handler

    // run fonksiyonunun işlemleri
    override fun run() {
        // BluetoothSocket nesnesine null verildi.
        var socket: BluetoothSocket? = null

        // socket değeri null ise
        while (true) {
            try {
                // Alınan mesajı istediğimiz değerleri verebiliriz.
                val message: Message = Message.obtain()

                // bağlantı oluştuğunda
                message.what = STATE_CONNECTING

                // mesajı yollama işi
                handler.sendMessage(message)

                // socket işini kabul et
                socket = serverSocket!!.accept()
            } catch (e: IOException) {
                e.printStackTrace()

                // mesaj örneğini alma işi
                val message: Message = Message.obtain()

                // STATE_CONNECTION_FAILED olayı olunca
                message.what = STATE_CONNECTION_FAILED

                // mesajı yollama
                handler.sendMessage(message)
            }

            // socket değeri null değilse
            if (socket != null) {

                // Alınan mesajı istediğimiz değerleri verebiliriz.
                val message: Message = Message.obtain()

                // bağlantı oluştuğunu anlama işi
                message.what = STATE_CONNECTED

                // mesajı yollama işini handler ile yap
                handler.sendMessage(message)


                // sendReceive nesnesi tanımı
                sendReceive = BTSendReceive(socket, handler)

                // sendReceive işini başlat
                sendReceive?.start()

                // işlemi kırk bırak
                break
            }
        }
    }

    @JvmName("getSendReceive1")
    fun getSendReceive(): BTSendReceive? {
        return sendReceive
    }

    // ServerClass yapıcı fonksiyonu
    init {
        try {
            // serverSocket değerine uuid değerini kayıtla
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
            this.handler = handler

            // IOException yakalama
        } catch (e: IOException) {
            // hatayı bastır
            e.printStackTrace()
        }
    }
}