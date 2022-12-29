package com.example.monopolycard

interface DownBarActionEvent {
    fun onEndPostCard(actionValue: String, actionType: String, isAllTypePosted: Boolean)
    fun onStartPostCard()
}