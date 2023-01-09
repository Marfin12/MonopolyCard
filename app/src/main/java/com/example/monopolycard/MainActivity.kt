package com.example.monopolycard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.monopolycard.cards.CardItem
import com.example.monopolycard.databinding.ActivityMainBinding
import com.example.monopolycard.decks.DeckAdapter
import com.example.monopolycard.decks.DeckItem
import com.example.monopolycard.decks.DeckViewHolder
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sp: SoundPool
    private var cashSound: Int = 0
    private var moneyRotateSound: Int = 0
    private var assetPlacedSound: Int = 0
    private var spLoaded = false
    private var currentPlayer = 0

    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false
    private val listPlayer = mutableListOf<DeckItem>()
    private val cardItems = mutableListOf<CardItem>()
    private val cardItems2 = mutableListOf<CardItem>()
    private val cardItems3 = mutableListOf<CardItem>()
    private val cardItems4 = mutableListOf<CardItem>()

    private var usedAssetSaved = View.GONE
    private var usedMoneySaved = View.GONE
    private var usedActionSaved = View.GONE
    private var deckAdapter: DeckAdapter? = null

    private var moneyRainAnimator: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSound()
        initBindingListener()
        initDeck()
        initViewPager()
    }

    private fun initDeck() {
        cardItems2.add(CardItem(R.drawable.spr_py_brown_house_card))
        cardItems2.add(CardItem(R.drawable.spr_py_orange_house_card))
        cardItems2.add(CardItem(R.drawable.spr_py_2m_card))
        cardItems2.add(CardItem(R.drawable.spr_py_act_go_pass))
        cardItems2.add(CardItem(R.drawable.spr_py_orange_house_card))
        cardItems4.addAll(cardItems2)
        cardItems.add(CardItem(R.drawable.spr_card_placeholder))
        cardItems3.add(CardItem(R.drawable.spr_card_placeholder))

        listPlayer.add(DeckItem(
            true,
            cardItems,
            cardItems2,
            DeckActionType.IDLE,
            isMoneyStepExist = true, isAssetStepExist = true, isActionStepExist = true
        ))
        listPlayer.add(DeckItem(
            false,
            cardItems3,
            cardItems4,
            DeckActionType.IDLE,
            isMoneyStepExist = false, isAssetStepExist = false, isActionStepExist = false
        ))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        deckAdapter = DeckAdapter(this@MainActivity, listPlayer,
            onPagerDown = {
                binding.monopolyViewPager.isUserInputEnabled = false
            },
            onPagerUp = {
                binding.monopolyViewPager.isUserInputEnabled = true
            },
            onPagerSwipe = { isNext ->
                if (isNext) nextPlayer(false)
                else prevPlayer(false)
            },
            DownBarAction({
                onStartPostCard()
            }, { actionValue, actionType, isAllTypePosted ->
                onEndPostCard(actionValue, actionType, isAllTypePosted)
            }))
        binding.monopolyViewPager.offscreenPageLimit = 3
        binding.monopolyViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        binding.monopolyViewPager.adapter = deckAdapter
        binding.monopolyViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val selectedDeck = deckAdapter?.getSelectedDeck(position)

                if (position == currentPlayer) {
                    binding.usedActionView.visibility = usedActionSaved
                    binding.usedAssetView.visibility = usedAssetSaved
                    binding.usedMoneyView.visibility = usedMoneySaved
                } else {
                    binding.usedActionView.visibility = View.VISIBLE
                    binding.usedAssetView.visibility = View.VISIBLE
                    binding.usedMoneyView.visibility = View.VISIBLE
                    moneyRainAnimator?.end()
                }

                binding.txtCash.text = selectedDeck?.money.toString()
                binding.txtHouse.text = selectedDeck?.asset.toString()
            }
        })
//        binding.monopolyViewPager.setOnTouchListener { v, event ->
//            when (event?.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    println("x: ${event.rawX}")
//                    println("x: ${event.rawY}")
//                    true
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    true
//                }
//                else -> false
//            }
//        }
    }

    private fun nextPlayer(isNextTurn: Boolean) {
        val deckItem = deckAdapter?.nextTurn(currentPlayer, ++currentPlayer)
        binding.txtCash.text = deckItem?.money.toString()
        binding.txtHouse.text = deckItem?.asset.toString()

        val currentItem = binding.monopolyViewPager.currentItem
        if (currentItem < listPlayer.size - 1)
        binding.monopolyViewPager.setCurrentItem(currentItem + 1, true)

        if (isNextTurn) {
            binding.usedAssetView.visibility = View.GONE
            binding.usedActionView.visibility = View.GONE
            binding.usedMoneyView.visibility = View.GONE

            usedActionSaved = View.GONE
            usedAssetSaved = View.GONE
            usedMoneySaved = View.GONE

            binding.skipButtonImageView.visibility = View.GONE

            binding.monopolyViewPager.postDelayed(enemyPostAssetTurn, 6000)
            binding.monopolyViewPager.postDelayed(enemyPostMoneyTurn, 3500)
            binding.monopolyViewPager.postDelayed(enemyPostActionTurn, 9000)
            binding.monopolyViewPager.postDelayed(enemyPostPlayerTurn, 12000)
        }
    }

    private fun prevPlayer(isNextTurn: Boolean) {
        if (isNextTurn) {
            binding.skipButtonImageView.visibility = View.VISIBLE
            binding.usedAssetView.visibility = View.GONE
            binding.usedActionView.visibility = View.GONE
            binding.usedMoneyView.visibility = View.GONE
            usedActionSaved = View.GONE
            usedAssetSaved = View.GONE
            usedMoneySaved = View.GONE
        }
        val currentItem = binding.monopolyViewPager.currentItem

        val deckItem = deckAdapter?.nextTurn(currentPlayer, --currentPlayer)
        binding.txtCash.text = deckItem?.money.toString()
        binding.txtHouse.text = deckItem?.asset.toString()

        // set first player to idle
        listPlayer.first { playerDeck -> !playerDeck.isYourDeck }.actionType = DeckActionType.IDLE
        if (currentItem > 0)
            binding.monopolyViewPager.setCurrentItem(currentItem - 1, true)
    }

    private fun initSound() {
        sp = SoundPool.Builder()
            .setMaxStreams(10)
            .build()

        sp.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                spLoaded = true
            } else {
                Toast.makeText(this@MainActivity, "Failed load", Toast.LENGTH_SHORT).show()
            }
        }
        cashSound = sp.load(this, R.raw.cash, 1)
        moneyRotateSound = sp.load(this, R.raw.money_rotate, 1)
        assetPlacedSound = sp.load(this, R.raw.asset_placed, 1)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBindingListener() {
        binding.skipButtonImageView.setOnClickListener {
            nextPlayer(true)

            binding.usedAssetView.visibility = View.VISIBLE
            binding.usedMoneyView.visibility = View.VISIBLE
            binding.usedActionView.visibility = View.VISIBLE
        }
    }

    private fun initMedia() {
        mMediaPlayer = MediaPlayer()
        val attribute = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mMediaPlayer?.setAudioAttributes(attribute)
        mMediaPlayer?.setVolume(0.2f, 0.2f)

        val afd = applicationContext.resources.openRawResourceFd(R.raw.background)
        try {
            mMediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mMediaPlayer?.setOnPreparedListener {
            isReady = true
            mMediaPlayer?.start()
        }
        mMediaPlayer?.setOnErrorListener { _, _, _ -> false }

        if (!isReady) {
            mMediaPlayer?.prepareAsync()
        } else {
            if (mMediaPlayer?.isPlaying as Boolean) {
                mMediaPlayer?.pause()
            } else {
                mMediaPlayer?.start()
            }
        }
    }

    private fun onStartPostCard() {
        binding.skipButtonImageView.visibility = View.GONE
    }

    private fun onEndPostCard(actionValue: Int, actionType: String, isAllTypePosted: Boolean) {
        if (currentPlayer == 0) binding.skipButtonImageView.visibility = View.VISIBLE
        val selectedDeck = deckAdapter?.getSelectedDeck(currentPlayer)

        when (actionType) {
            DeckActionType.MONEY -> onEndPostMoneyCard(selectedDeck?.money.toString())
            DeckActionType.ASSET -> onEndPostAssetCard(selectedDeck?.asset.toString())
            DeckActionType.ACTION -> onEndPostActionCard(actionValue)
        }
        if (isAllTypePosted) {
            if (currentPlayer < (listPlayer.size - 1)) nextPlayer(true)
            else {
                binding.monopolyViewPager.removeCallbacks(enemyPostPlayerTurn)
                prevPlayer(true)
            }
        }
    }

    private fun onEndPostActionCard(actionType: Int) {
        binding.usedActionView.visibility = View.VISIBLE
        usedActionSaved = View.VISIBLE

        when(actionType) {
            0 -> {
                actionGoPassCard()
            }
            else -> {
                println("error")
            }
        }
    }

    private fun onEndPostAssetCard(totalAsset: String) {
        binding.usedAssetView.visibility = View.VISIBLE
        usedAssetSaved = View.VISIBLE
        binding.txtHouse.text = totalAsset
    }

    private fun onEndPostMoneyCard(totalMoney: String) {
        with(binding.rainMoneyInc) {
            val moneyY = rainMoneyLayout.y
            val moneyMoveToY = moneyY + 900
            usedMoneySaved = View.VISIBLE

            val moneyRain = ObjectAnimator.ofFloat(
                rainMoneyLayout, View.TRANSLATION_Y, moneyY + 20, moneyMoveToY
            ).setDuration(1600)

            moneyRainAnimator = AnimatorSet()

            moneyRainAnimator?.apply {
                playTogether(moneyRain)
                doOnStart {
                    if (spLoaded) {
                        sp.play(cashSound, 1f, 1f, 0, 0, 1f)
                    }
                }
                doOnEnd {
                    binding.txtCash.text = totalMoney
                    rainMoneyLayout.y = moneyY
                    binding.usedMoneyView.visibility = View.VISIBLE
                }
            }?.start()
            root.visibility = View.VISIBLE
        }
    }

    private class DownBarAction(
        private val onStartPostCardAction: (() -> Unit),
        private val onEndPostCardAction: ((
            actionValue: Int,
            actionType: String,
            isAllTypePosted: Boolean
        ) -> Unit),
    ) : DownBarActionEvent {
        override fun onStartPostCard() {
            onStartPostCardAction()
        }

        override fun onEndPostCard(actionValue: Int, actionType: String, isAllTypePosted: Boolean) {
            onEndPostCardAction(actionValue, actionType, isAllTypePosted)
        }
    }

    private val enemyPostAssetTurn = Runnable {
        deckAdapter?.postCurrentPlayerAction(DeckActionType.ASSET, currentPlayer)
    }

    private val enemyPostMoneyTurn = Runnable {
        deckAdapter?.postCurrentPlayerAction(DeckActionType.MONEY, currentPlayer)
    }

    private val enemyPostActionTurn = Runnable {
        deckAdapter?.postCurrentPlayerAction(DeckActionType.ACTION, currentPlayer)
    }

    private val enemyPostPlayerTurn = Runnable {
        prevPlayer(true)
    }

    private fun actionGoPassCard() {
        val selectedPlayerDeck = listPlayer[currentPlayer].playerCardItem

        selectedPlayerDeck.add((CardItem(R.drawable.spr_py_2m_card)))
        selectedPlayerDeck.add((CardItem(R.drawable.spr_py_orange_house_card)))
    }
}