package com.example.monopolycard.cards

import java.io.Serializable;

data class CardItem(
    var image: Int,
    var assetLevel: Int = 1
    ) : Serializable