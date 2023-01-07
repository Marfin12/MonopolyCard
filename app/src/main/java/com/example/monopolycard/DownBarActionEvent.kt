package com.example.monopolycard

interface DownBarActionEvent {
    fun onEndPostCard(actionValue: Int, actionType: String, isAllTypePosted: Boolean)
    fun onStartPostCard()
}