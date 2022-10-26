package com.example.monopolycard

interface DownBarActionEvent {
    fun onEndPostCard(actionValue: String, actionType: String)
    fun onStartPostCard()
}