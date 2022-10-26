package com.example.monopolycard.cards

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.example.monopolycard.R

class CardViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView = itemView.findViewById<ImageView>(R.id.card_image_view)

    @SuppressLint("ClickableViewAccessibility")
    fun setImageSource(cardItem: CardItem,
                       onItemClick: ((CardItem) -> Unit), onItemUp: (() -> Unit)
    ) {
        imageView.setImageResource(cardItem.image)
//        if (cardItem.image == R.drawable.spr_py_orange_house_card) {
            imageView.setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        println("pager down")
                        onItemClick.invoke(cardItem)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        println("pager up")
                        onItemUp.invoke()
                        true
                    }
                    else -> false
                }
            }
//        } else {
//            imageView.setOnClickListener {
//                onItemClick.invoke(cardItem)
//            }
//        }
    }
}