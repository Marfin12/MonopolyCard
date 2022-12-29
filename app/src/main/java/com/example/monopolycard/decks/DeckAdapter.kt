package com.example.monopolycard.decks

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monopolycard.DeckActionType
import com.example.monopolycard.DownBarActionEvent
import com.example.monopolycard.R

class DeckAdapter(
    private val context: Context,
    private val deckItem: MutableList<DeckItem>,
    private val onPagerDown: (() -> Unit),
    private val onPagerUp: (() -> Unit),
    private val onPagerSwipe: ((isNext: Boolean) -> Unit),
    private val downBarActionEvent: DownBarActionEvent
) :
    RecyclerView.Adapter<DeckViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        return DeckViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.deck_item_container,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.setPlayerDeck(deckItem[position], context,
            onPagerDown,
            onPagerUp,
            onPagerSwipe,
            downBarActionEvent
        )
    }

    override fun getItemCount(): Int {
        return deckItem.size
    }

    fun postCurrentPlayerAction(actionType: String, whichPlayer: Int) {
        deckItem[whichPlayer].actionType = actionType
        notifyDataSetChanged()
    }

    fun nextTurn(currentPlayer: Int, nextPlayer: Int) {
        deckItem[currentPlayer].isActionStepExist = false
        deckItem[currentPlayer].isAssetStepExist = false
        deckItem[currentPlayer].isMoneyStepExist = false
        deckItem[currentPlayer].actionType = DeckActionType.IDLE

        deckItem[nextPlayer].isActionStepExist = true
        deckItem[nextPlayer].isAssetStepExist = true
        deckItem[nextPlayer].isMoneyStepExist = true
    }
}