package com.example.monopolycard.cards

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.monopolycard.R

class CardViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val assetImageView = itemView.findViewById<ImageView>(R.id.card_image_view)
    private val assetImageView2 = itemView.findViewById<ImageView>(R.id.card_image_view2)
    private val assetImageView3 = itemView.findViewById<ImageView>(R.id.card_image_view3)

    @SuppressLint("ClickableViewAccessibility")
    fun setImageSource(
        cardItem: CardItem, onItemClick: (() -> Unit), onItemUp: ((CardItem) -> Unit)
    ) {
        assetImageView.setImageResource(cardItem.image)
        assetImageView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("pager down")
                    onItemClick.invoke()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    println("pager up")
                    onItemUp.invoke(cardItem)
                    true
                }
                else -> false
            }
        }

        if (assetImageView2 != null && assetImageView3 != null) {
            assetImageView2.setImageResource(cardItem.image)
            assetImageView3.setImageResource(cardItem.image)
            setTotalAsset(cardItem.assetLevel)
        }
    }

    private fun setTotalAsset(assetLevel: Int) {
        when (assetLevel) {
            0 -> {
                assetImageView.visibility = View.GONE
                assetImageView2.visibility = View.GONE
                assetImageView3.visibility = View.GONE
            }
            1 -> {
                assetImageView.visibility = View.VISIBLE
                assetImageView2.visibility = View.GONE
                assetImageView3.visibility = View.GONE
            }
            2 -> {
                assetImageView.visibility = View.VISIBLE
                assetImageView2.visibility = View.VISIBLE
                assetImageView3.visibility = View.GONE
            }
            3 -> {
                assetImageView.visibility = View.VISIBLE
                assetImageView2.visibility = View.VISIBLE
                assetImageView3.visibility = View.VISIBLE
            }
        }
    }
}