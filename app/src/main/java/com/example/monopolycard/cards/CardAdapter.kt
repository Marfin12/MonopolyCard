package com.example.monopolycard.cards

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monopolycard.R

class CardAdapter(
    private val context: Context,
    private val cardItem: MutableList<CardItem>,
    private val isPlayerDeck: Boolean = true,
    private val onItemClick: ((CardItem) -> Unit),
) :

    RecyclerView.Adapter<CardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return if (isPlayerDeck) CardViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.card_item_container,
                parent,
                false
            )
        ) else CardViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.card_item_container2,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.setImageSource(cardItem[position], onItemClick, position)
    }

    override fun getItemCount(): Int {
        return cardItem.size
    }
}