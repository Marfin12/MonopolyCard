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
    private val placeHolderActionImageView = itemView.findViewById<ImageView>(R.id.placeholder_action_image_view)
    private val placeHolderMoneyImageView = itemView.findViewById<ImageView>(R.id.placeholder_money_image_view)
    private val dummyDelayLayout = itemView.findViewById<ConstraintLayout>(R.id.dummy_delay_layout)

    private var assetCardAdapter: CardAdapter? = null
    private var assetPlayerAdapter: CardAdapter? = null

    private var deckItem = DeckItem(
        false,
        mutableListOf(),
        mutableListOf(),
        DeckActionType.IDLE,
        isMoneyStepExist = false, isAssetStepExist = false, isActionStepExist = false
    )

    private var onPagerDown: (() -> Unit) = {}
    private var onPagerUp: (() -> Unit) = {}
    private var onPagerSwipe: ((isNext: Boolean) -> Unit) = {}

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
                onPostCard(cardItem, downBarActionEvent, deckItem)
            }
        )

        initArrow()
        initViewPager(assetViewPager)
        modifyShownCardViewPager(assetViewPager)
        initViewPager(playerViewPager)

        if (!deckItem.isYourDeck) playerViewPager.isVisible = false

        if (deckItem.actionType == DeckActionType.IDLE) placeHolderActionImageView.visibility = View.GONE

        // for enemy
        if (!deckItem.isYourDeck)
        when(deckItem.actionType) {
            DeckActionType.ASSET -> {
                val selectedAssetCard = deckItem.playerCardItem.filter { card ->
                    card.image == R.drawable.spr_py_orange_house_card || card.image == R.drawable.spr_py_brown_house_card
                }
                if (selectedAssetCard.isNotEmpty()) onPostCard(selectedAssetCard[0], downBarActionEvent, deckItem)
            }
            DeckActionType.MONEY -> {
                val selectedMoneyCard = deckItem.playerCardItem.filter { card ->
                    card.image == R.drawable.spr_py_2m_card
                }
                if (selectedMoneyCard.isNotEmpty()) onPostCard(selectedMoneyCard[0], downBarActionEvent, deckItem)
            }
            DeckActionType.ACTION -> {
                val selectedActionCard = deckItem.playerCardItem.filter { card ->
                    card.image == R.drawable.spr_py_act_go_pass
                }
                if (selectedActionCard.isNotEmpty()) onPostCard(selectedActionCard[0], downBarActionEvent, deckItem)
            }
        }
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
        downBarActionEvent: DownBarActionEvent,
        deckItem: DeckItem
    ) {
        if (isValidPostMoney(cardItem, deckItem)) onPostMoney(cardItem, downBarActionEvent, deckItem)
        if (isValidPostAsset(cardItem, deckItem)) onPostNewAsset(cardItem, downBarActionEvent, deckItem)
        if (isValidPostAction(cardItem, deckItem)) onPostAction(cardItem, downBarActionEvent, deckItem)
    }

    private fun onPostNewAsset(
        cardItem: CardItem,
        downBarActionEvent: DownBarActionEvent?,
        deckItem: DeckItem
    ) {
        downBarActionEvent?.onStartPostCard()
        onStartPlacingCard(cardItem)
        var defaultYCardPlaced = 950
        var selectedAssetCardIndex = this.deckItem.assetCardItem.size - 1
        var defaultActionValue = 0
        lateinit var selectedAssetCard: CardItem

        if (assetCardAdapter?.hasAssetCard(cardItem) == true) {
            selectedAssetCardIndex = assetCardAdapter?.getIndexAssetCard(cardItem) ?: -1
            selectedAssetCard = assetCardAdapter?.getSelectedCard(selectedAssetCardIndex) ?: CardItem(
                R.drawable.spr_card_placeholder,
                -1
            )

            when (selectedAssetCard.assetLevel) {
                1 -> {
                    defaultYCardPlaced = 850
                }
                2 -> {
                    defaultYCardPlaced = 750
                    defaultActionValue = 1
                }
            }
        }

        assetViewPager.setCurrentItem(selectedAssetCardIndex, true)
        val chosenCardY = chosenCardImageView.y
        val chosenCardToY = chosenCardImageView.y - defaultYCardPlaced

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
                deckItem.isAssetStepExist = false
                deckItem.asset += defaultActionValue

                if (assetCardAdapter?.hasAssetCard(cardItem) == true) {
                    assetCardAdapter?.setAssetLevelItem(
                        selectedAssetCardIndex, selectedAssetCard.assetLevel + 1
                    )
                } else {
                    assetCardAdapter?.setCardItem(selectedAssetCardIndex, cardItem.image)
                    assetCardAdapter?.addCardItem(R.drawable.spr_card_placeholder)
                }

                downBarActionEvent?.onEndPostCard(defaultActionValue, DeckActionType.ASSET, isAllCardTypesPosted(deckItem))
                onFinishingCardPlaced(chosenCardY)
            }
        }.start()
    }

    private fun onPostAction(
        cardItem: CardItem,
        downBarActionEvent: DownBarActionEvent?,
        deckItem: DeckItem
    ) {
        downBarActionEvent?.onStartPostCard()
        onStartPlacingCard(cardItem)

        val chosenCardY = chosenCardImageView.y
        val chosenCardToY = chosenCardImageView.y - 490

        val scaleX = ObjectAnimator.ofFloat(
            chosenCardImageView, View.SCALE_X, 1.0f, 0.388f
        ).setDuration(1000)
        val scaleY = ObjectAnimator.ofFloat(
            chosenCardImageView, View.SCALE_Y, 1.0f, 0.34f
        ).setDuration(1000)
        val translateY = ObjectAnimator.ofFloat(
            chosenCardImageView, View.TRANSLATION_Y, chosenCardY, chosenCardToY
        ).setDuration(1000)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, translateY)
            doOnEnd {
                deckItem.isActionStepExist = false
                placeHolderActionImageView.visibility = View.VISIBLE
                downBarActionEvent?.onEndPostCard(
                    0, DeckActionType.ACTION, isAllCardTypesPosted(deckItem)
                )
                onFinishingCardPlaced(chosenCardY)
            }
        }.start()
    }

    private fun onPostMoney(
        cardItem: CardItem,
        downBarActionEvent: DownBarActionEvent?,
        deckItem: DeckItem
    ) {
        downBarActionEvent?.onStartPostCard()
        onStartPlacingCard(cardItem)
        val chosenCardY = chosenCardImageView.y
        val chosenCardToY = chosenCardImageView.y - 1300
        val chosenCardRotation = chosenCardImageView.rotation

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
                deckItem.isMoneyStepExist = false
                chosenCardImageView.rotation = chosenCardRotation
                deckItem.money += 2

                downBarActionEvent?.onEndPostCard(2, DeckActionType.MONEY, isAllCardTypesPosted(deckItem))
                placeHolderMoneyImageView.setImageResource(R.drawable.spr_py_2m_card)
                onFinishingCardPlaced(chosenCardY)
            }
        }.start()
    }

    private fun onFinishingCardPlaced(
        backToOriginalY: Float
    ) {
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

    private fun isValidPostMoney(cardItem: CardItem, deckItem: DeckItem): Boolean =
        cardItem.image == R.drawable.spr_py_2m_card && deckItem.isMoneyStepExist

    private fun isValidPostAsset(cardItem: CardItem, deckItem: DeckItem): Boolean =
        (cardItem.image == R.drawable.spr_py_orange_house_card
                || cardItem.image == R.drawable.spr_py_brown_house_card)
                && deckItem.isAssetStepExist

    private fun isValidPostAction(cardItem: CardItem, deckItem: DeckItem): Boolean =
        cardItem.image == R.drawable.spr_py_act_go_pass && deckItem.isActionStepExist

    private fun isAllCardTypesPosted(deckItem: DeckItem): Boolean =
        !deckItem.isMoneyStepExist && !deckItem.isActionStepExist && !deckItem.isAssetStepExist

    private fun isAllCardTypesNotPosted(): Boolean =
        deckItem.isMoneyStepExist && deckItem.isActionStepExist && deckItem.isAssetStepExist

}