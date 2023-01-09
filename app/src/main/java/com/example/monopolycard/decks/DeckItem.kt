package com.example.monopolycard.decks

import com.example.monopolycard.cards.CardItem
import java.io.Serializable

class DeckItem(
    val isYourDeck: Boolean,
    val assetCardItem: MutableList<CardItem>,
    val playerCardItem: MutableList<CardItem>,
    var actionType: String,
    var isMoneyStepExist: Boolean,
    var isAssetStepExist: Boolean,
    var isActionStepExist: Boolean,
    var money: Int = 0,
    var asset: Int = 0
) : Serializable
