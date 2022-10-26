package com.example.monopolycard.decks

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.monopolycard.DeckActionType
import com.example.monopolycard.DownBarActionEvent
import com.example.monopolycard.R
import com.example.monopolycard.cards.CardAdapter
import com.example.monopolycard.cards.CardItem
import kotlin.math.abs


class DeckViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val assetViewPager = itemView.findViewById<ViewPager2>(R.id.asset_view_pager)
    private val playerViewPager = itemView.findViewById<ViewPager2>(R.id.player_view_pager)
    private val nextArrowImageView = itemView.findViewById<ImageView>(R.id.next_image_view)
    private val prevArrowImageView = itemView.findViewById<ImageView>(R.id.prev_image_view)
    private val chosenCardImageView = itemView.findViewById<ImageView>(R.id.chosen_card_image_view)
    private val dummyDelayLayout = itemView.findViewById<ConstraintLayout>(R.id.dummy_delay_layout)

    private var assetCardAdapter: CardAdapter? = null
    private var assetPlayerAdapter: CardAdapter? = null

    private var deckItem = DeckItem(false, mutableListOf(), mutableListOf())

    private var onPagerDown: (() -> Unit) = {}
    private var onPagerUp: (() -> Unit) = {}
    private var onPagerSwipe: ((isNext: Boolean) -> Unit) = {}

    companion object {
        private var isMoneyStepExist = true
        private var isAssetStepExist = true
        private var isActionStepExist = true

        fun onSkipButtonClick() {
            isMoneyStepExist = false
            isAssetStepExist = false
            isActionStepExist = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setPlayerDeck(
        deckItem: DeckItem,
        context: Context,
        onPagerDown: (() -> Unit),
        onPagerUp: (() -> Unit),
        onPagerSwipe: ((isNext: Boolean) -> Unit),
        downBarActionEvent: DownBarActionEvent
    ) {
        this.deckItem = deckItem
        this.onPagerDown = onPagerDown
        this.onPagerUp = onPagerUp
        this.onPagerSwipe = onPagerSwipe
        this.assetCardAdapter = CardAdapter(context, deckItem.assetCardItem, false,
            onItemDown = {
                onPagerDown.invoke()
            },
            onItemUp = {}
        )
        this.assetPlayerAdapter = CardAdapter(context, deckItem.playerCardItem,
            onItemDown = {
                onPagerDown.invoke()
            },
            onItemUp = { cardItem ->
                onPostCard(cardItem, downBarActionEvent)
            }
        )

        initArrow()
        initViewPager(assetViewPager)
        modifyShownCardViewPager(assetViewPager)

        if (deckItem.isYourDeck) initViewPager(playerViewPager)
        else playerViewPager.isVisible = false
    }

    private fun initArrow() {
        nextArrowImageView.setOnClickListener {
            onPagerSwipe.invoke(true)
        }
        prevArrowImageView.setOnClickListener {
            onPagerSwipe.invoke(false)
        }
    }

    private fun modifyShownCardViewPager(viewPager2: ViewPager2) {
        viewPager2.setPageTransformer(null)
        viewPager2.adapter = assetCardAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager(viewPager2: ViewPager2) {
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.clipToOutline = false
        viewPager2.offscreenPageLimit = 3
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        viewPager2.currentItem = 2

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }

        viewPager2.setPageTransformer(compositePageTransformer)
        viewPager2.adapter = assetPlayerAdapter
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == 0) onPagerUp.invoke()
                super.onPageScrollStateChanged(state)
            }
        })
    }

    private fun onPostCard(
        cardItem: CardItem,
        downBarActionEvent: DownBarActionEvent
    ) {
        if (isValidPostMoney(cardItem)) onPostMoney(cardItem, downBarActionEvent)
        if (isValidPostAsset(cardItem)) onPostAsset(cardItem, downBarActionEvent)
    }

    private fun onPostAsset(
        cardItem: CardItem,
        downBarActionEvent: DownBarActionEvent
    ) {
        onStartPlacingCard(cardItem)

        assetViewPager.setCurrentItem(this.deckItem.assetCardItem.size - 1, true)
        chosenCardImageView.visibility = View.VISIBLE
        val chosenCardY = chosenCardImageView.y
        val chosenCardToY = chosenCardImageView.y - 950

        val scaleX = ObjectAnimator.ofFloat(
            chosenCardImageView, View.SCALE_X, 1.0f, 0.468f
        ).setDuration(1000)
        val scaleY = ObjectAnimator.ofFloat(
            chosenCardImageView, View.SCALE_Y, 1.0f, 0.44f
        ).setDuration(1000)
        val translateY = ObjectAnimator.ofFloat(
            chosenCardImageView, View.TRANSLATION_Y, chosenCardY, chosenCardToY
        ).setDuration(1000)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, translateY)
            doOnEnd {
                isAssetStepExist = false
                assetCardAdapter?.addCardItem(R.drawable.spr_card_placeholder)

                downBarActionEvent.onEndPostCard("7", DeckActionType.ASSET)
                onFinishingCardPlaced(chosenCardY)
            }
        }.start()
    }

    private fun onPostMoney(
        cardItem: CardItem,
        downBarActionEvent: DownBarActionEvent
    ) {
        downBarActionEvent.onStartPostCard()
        onStartPlacingCard(cardItem)
        val chosenCardY = chosenCardImageView.y
        val chosenCardToY = chosenCardImageView.y - 1300

        val rotate = ObjectAnimator.ofFloat(
            chosenCardImageView, View.ROTATION, 0f, 271f
        ).setDuration(1000)
        val scaleX = ObjectAnimator.ofFloat(
            chosenCardImageView, View.SCALE_X, 1.0f, 0.4f
        ).setDuration(1000)
        val scaleY = ObjectAnimator.ofFloat(
            chosenCardImageView, View.SCALE_Y, 1.0f, 0.4f
        ).setDuration(1000)
        val translateY = ObjectAnimator.ofFloat(
            chosenCardImageView, View.TRANSLATION_Y, chosenCardY, chosenCardToY
        ).setDuration(1000)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, translateY, rotate)
            doOnEnd {
                isMoneyStepExist = false

                downBarActionEvent.onEndPostCard("10", DeckActionType.MONEY)
                onFinishingCardPlaced(chosenCardY)
            }
        }.start()
    }

    private fun onFinishingCardPlaced(backToOriginalY: Float) {
        chosenCardImageView.visibility = View.GONE
        chosenCardImageView.y = backToOriginalY
        val currentItem = playerViewPager.currentItem
        playerViewPager.setCurrentItem(currentItem - 1, true)
        startDelayedFunction(1000L) {
            assetPlayerAdapter?.removeCardItem(currentItem)
            resumeAnyUserInteraction()
        }
    }

    private fun onStartPlacingCard(cardItem: CardItem) {
        pauseAnyUserInteraction()
        assetPlayerAdapter?.setCardToGone(playerViewPager.currentItem)
        chosenCardImageView.setImageResource(cardItem.image)
        chosenCardImageView.visibility = View.VISIBLE
    }

    private fun startDelayedFunction(delay: Long, onFunctionInvoke: () -> Unit) {
        val noAnim = ObjectAnimator.ofFloat(
            dummyDelayLayout, View.SCALE_X, 1.0f, 0.0f
        ).setDuration(delay)
        AnimatorSet().apply {
            play(noAnim)
            doOnEnd {
                onFunctionInvoke.invoke()
            }
        }.start()
    }

    private fun pauseAnyUserInteraction() {
        playerViewPager.isUserInputEnabled = false
        assetViewPager.isUserInputEnabled = false
    }

    private fun resumeAnyUserInteraction() {
        playerViewPager.isUserInputEnabled = true
        assetViewPager.isUserInputEnabled = true
    }

    private fun isValidPostMoney(cardItem: CardItem): Boolean =
        cardItem.image == R.drawable.spr_py_2m_card && isMoneyStepExist

    private fun isValidPostAsset(cardItem: CardItem): Boolean =
        (cardItem.image == R.drawable.spr_py_orange_house_card
                || cardItem.image == R.drawable.spr_py_brown_house_card)
                && isAssetStepExist

}