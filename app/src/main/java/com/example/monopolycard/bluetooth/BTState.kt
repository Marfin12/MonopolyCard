package com.example.monopolycard.bluetooth

import androidx.annotation.Keep

@Keep
object BTState {
    const val STATE_LISTENING = 1
    const val STATE_CONNECTING = 2
    const val STATE_CONNECTED = 3
    const val STATE_CONNECTION_FAILED = 4
    const val STATE_MESSAGE_RECEIVED = 5
}