package com.example.monopolycard.decks

import com.example.monopolycard.cards.CardItem
import java.io.Serializable

class DeckItem(
    val isYourDeck: Boolean,
    val assetCardItem: MutableList<CardItem>,
    val playerCardItem: MutableList<CardItem>
) : Serializable
