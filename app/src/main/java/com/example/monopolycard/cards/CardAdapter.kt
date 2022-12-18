package com.example.monopolycard.cards

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monopolycard.R

@SuppressLint("NotifyDataSetChanged")
class CardAdapter(
    private val context: Context,
    private val cardItem: MutableList<CardItem>,
    private val isPlayerDeck: Boolean = true,
    private val onItemDown: (() -> Unit),
    private val onItemUp: ((CardItem) -> Unit)
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
        holder.setImageSource(cardItem[position], onItemDown, onItemUp)
    }

    override fun getItemCount(): Int {
        return cardItem.size
    }

    fun setCardItem(pos: Int, image: Int) {
        cardItem[pos].image =  image
    }

    fun addCardItem(image: Int) {
        cardItem.add(CardItem(image))
        notifyDataSetChanged()
    }

    fun removeCardItem(idx: Int) {
        cardItem.removeAt(idx)
        notifyDataSetChanged()
    }

    fun setCardToGone(idx: Int) {
        cardItem[idx] = CardItem(0)
        notifyDataSetChanged()
    }
}