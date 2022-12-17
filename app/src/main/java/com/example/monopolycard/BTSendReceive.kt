package com.example.monopolycard

import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.example.monopolycard.BTState.STATE_MESSAGE_RECEIVED
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class BTSendReceive(socket: BluetoothSocket, handler: Handler) : Thread() {
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    private val handler: Handler

    override fun run() {
        // buffer nesnesi oluşturma
        val buffer = ByteArray(1024)

        // bytes değişkeni
        var bytes: Int

        // true değeri döndükçe
        while (true) {
            try {
                // bytes değişkenine read buffer yap.
                bytes = inputStream.read(buffer)

                // parametreleri tutan bir mesaj oluşturmak için kullanılır.
                handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget()

                // hatayı yakalama işlemi
            } catch (e: IOException) {
                // hatayı yazdır
                e.printStackTrace()
            }
        }
    }

    fun write(bytes: ByteArray?) {
        try {
            outputStream.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    init {
        var tempInput: InputStream? = null
        var tempOutput: OutputStream? = null

        try {
            tempInput = socket.inputStream
            tempOutput = socket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (tempInput != null) {
            inputStream = tempInput
        }

        this.handler = handler

        if (tempOutput != null) {
            outputStream = tempOutput
        }
    }
}