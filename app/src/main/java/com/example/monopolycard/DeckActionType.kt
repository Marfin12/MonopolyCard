package com.example.monopolycard

import androidx.annotation.Keep

@Keep
object DeckActionType {
    val IDLE: String = "IDLE"
    val MONEY: String = "MONEY"
    val ASSET: String = "ASSET"
    val ACTION: String = "ACTION"
}