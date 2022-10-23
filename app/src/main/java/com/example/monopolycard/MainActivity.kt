package com.example.monopolycard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.monopolycard.cards.CardAdapter
import com.example.monopolycard.cards.CardItem
import com.example.monopolycard.databinding.ActivityMainBinding
import java.io.IOException
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val cardItems = mutableListOf<CardItem>()

    private lateinit var sp: SoundPool
    private var cashSound: Int = 0
    private var moneyRotateSound: Int = 0
    private var assetPlacedSound: Int = 0
    private var spLoaded = false

    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false

    private var isShowingCard = true
    private var showCardY = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager(binding.viewPagerMonopoly)
        initViewPager(binding.cardPlaceholderViewPager)
        initViewPager(binding.cardPlaceholderViewPager2)
        binding.cardPlaceholderViewPager.setPageTransformer(null)
        binding.cardPlaceholderViewPager.adapter = CardAdapter(this@MainActivity, cardItems, false) { item ->
            println("item clicked")
        }
        binding.cardPlaceholderViewPager2.setPageTransformer(null)
        binding.cardPlaceholderViewPager2.adapter = CardAdapter(this@MainActivity, cardItems, false) { item ->
            println("item clicked")
        }
        initSound()
        initBindingListener()

        showPlayerDeck()
    }

    private fun initViewPager(viewPager2: ViewPager2) {
        cardItems.add(CardItem(R.drawable.spr_py_brown_house_card))
        cardItems.add(CardItem(R.drawable.spr_py_orange_house_card))
        cardItems.add(CardItem(R.drawable.spr_py_2m_card))
        cardItems.add(CardItem(R.drawable.spr_py_2m_card))
        cardItems.add(CardItem(R.drawable.spr_py_orange_house_card))

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
        viewPager2.adapter = CardAdapter(this@MainActivity, cardItems) { item ->

        }
        binding.rainMoneyInc.root.visibility = View.GONE
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
        binding.cardPlaceholderViewPager.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("pager click")
                    true
                }
                else -> false
            }
        }

        binding.skipButtonImageView.setOnClickListener {
            hideCard(true)
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

    private fun postPlayerMoney() {
        showPagerAndSkipButton(false)
        binding.chosenCardImageView.isVisible = true

        val moneyY = binding.chosenCardImageView.y
        val moneyMoveToY = moneyY + 390

        val rotate = ObjectAnimator.ofFloat(
            binding.chosenCardImageView, View.ROTATION, 0f, 271f
        ).setDuration(1000)
        val scaleX = ObjectAnimator.ofFloat(
            binding.chosenCardImageView, View.SCALE_X, 1.0f, 0.71f
        ).setDuration(1000)
        val scaleY = ObjectAnimator.ofFloat(
            binding.chosenCardImageView, View.SCALE_Y, 1.0f, 0.7f
        ).setDuration(1000)
        val translateY = ObjectAnimator.ofFloat(
            binding.chosenCardImageView, View.TRANSLATION_Y, moneyY, moneyMoveToY
        ).setDuration(1000)

        AnimatorSet().apply {
            playTogether(rotate, scaleX, scaleY, translateY)
            doOnStart {
                if (spLoaded) {
                    sp.play(moneyRotateSound, 1f, 1f, 0, 0, 1f)
                }
            }
            doOnEnd {
                onEndPostPlayerMoney()
            }
        }.start()
    }

    private fun onEndPostPlayerMoney() {
        with(binding.rainMoneyInc) {
            val moneyY = rainMoneyLayout.y
            val moneyMoveToY = moneyY + 680

            val moneyRain = ObjectAnimator.ofFloat(
                rainMoneyLayout, View.TRANSLATION_Y, moneyY+20, moneyMoveToY
            ).setDuration(1600)

            AnimatorSet().apply {
                playTogether(moneyRain)
                binding.txtCash.text = "7"
                doOnStart {
                    if (spLoaded) {
                        sp.play(cashSound, 1f, 1f, 0, 0, 1f)
                    }
                }
                doOnEnd {
                    binding.usedMoneyView.visibility = View.VISIBLE
                    showPagerAndSkipButton(true)
                    binding.chosenCardImageView.isVisible = false
                    cardItems.removeAt(2)
                    binding.viewPagerMonopoly.adapter = CardAdapter(this@MainActivity, cardItems) { item ->
                        if (item.image == R.drawable.spr_py_2m_card) {
                            postPlayerMoney()
                        } else {
                            postPlayerAsset()
                        }
                    }
                    binding.viewPagerMonopoly.currentItem = 1
                    showPlayerDeck()
                }
            }.start()
            root.visibility = View.VISIBLE
        }
    }

    private fun postPlayerAsset() {
        showPagerAndSkipButton(false)
        binding.chosenAssetImageView.isVisible = true
    }

    private fun playAssetCardAnimation() {
        val initX = binding.chosenAssetImageView.x
//        val placeholderX = (binding.cardPlaceholderImageView.x - binding.chosenAssetImageView.x) + 73.2
        val initY = binding.chosenAssetImageView.y
//        val placeHolderY = (binding.cardPlaceholderImageView.y - binding.chosenAssetImageView.y) - 24

        val scaleY = ObjectAnimator.ofFloat(
            binding.chosenAssetImageView, View.SCALE_Y, 1.0f, 0.88f
        ).setDuration(1000)
        val moveX = ObjectAnimator.ofFloat(
            binding.chosenAssetImageView, View.TRANSLATION_X, initX, 0.0f
        ).setDuration(1000)
        val moveY = ObjectAnimator.ofFloat(
            binding.chosenAssetImageView, View.TRANSLATION_Y, initY, 0.0f
        ).setDuration(1000)
        val dummyY = ObjectAnimator.ofFloat(
            binding.rainMoneyInc.root, View.TRANSLATION_Y,1.0f, 0.9f
        ).setDuration(2000)

        AnimatorSet().apply {
            playTogether(scaleY, moveX, moveY, dummyY)
            doOnStart {
                if (spLoaded) {
                    sp.play(assetPlacedSound, 1f, 1f, 0, 0, 1f)
                }
            }
            doOnEnd {
                binding.usedMoneyView.visibility = View.VISIBLE
                showPagerAndSkipButton(true)
                binding.chosenCardImageView.isVisible = false
                cardItems.removeAt(1)
                binding.viewPagerMonopoly.adapter = CardAdapter(this@MainActivity, cardItems) { item ->
                    if (item.image == R.drawable.spr_py_2m_card) {
                        postPlayerMoney()
                    }
                }
                binding.viewPagerMonopoly.currentItem = 1
                binding.viewPagerLayout.bringToFront()
                binding.skipButtonImageView.bringToFront()
                binding.downBarLayout.bringToFront()
                binding.usedAssetView.isVisible = true

                showPlayerDeck()
            }
        }.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun switchPlayerTurn() {
        with(binding.cardPlaceholderImageView2) {
            val scrollEnemyX = ObjectAnimator.ofInt(
                enemyCardScrollView, "scrollX", 2050
            ).setDuration(3000)

            AnimatorSet().apply {
                play(scrollEnemyX)
            }.start()
        }
    }

    private fun onArrowClick() {
        if (isShowingCard) {
            isShowingCard = false
            hideCard(false)
        } else {
            isShowingCard = true
            showPlayerDeck()
        }
    }

    private fun showPlayerDeck() {
        showPagerAndSkipButton(true)
//        val arrowMoveToY = showCardY - 280
//
//        val moveUp =
//            ObjectAnimator.ofFloat(binding.viewPagerLayout, View.TRANSLATION_Y, 600f, -600f)
//                .setDuration(1000)
//
//        AnimatorSet().apply {
//            playTogether(moveUp)
//            doOnEnd {
//                showCardY = arrowMoveToY
//            }
//        }.start()
    }

    private fun hideCard(isSkip: Boolean) {
        val arrowMoveToY = showCardY + 280

        val moveUp =
            ObjectAnimator.ofFloat(binding.viewPagerLayout, View.TRANSLATION_Y, -600f, 600f)
                .setDuration(1000)

        AnimatorSet().apply {
            playTogether(moveUp)
            doOnEnd {
                if (isSkip) onSkip()
                else showPagerAndSkipButton(false)
                showCardY = arrowMoveToY
            }
        }.start()
    }

    private fun onSkip() {
        val firstPlayerX = binding.firstPlayerLayout.x
        val secondPlayerX = binding.secondPlayerLayout.x

        val moveFirstX = ObjectAnimator.ofFloat(
            binding.firstPlayerLayout, View.TRANSLATION_X, firstPlayerX, firstPlayerX + 940
        ).setDuration(1000)
        val moveSecondX = ObjectAnimator.ofFloat(
            binding.secondPlayerLayout, View.TRANSLATION_X, secondPlayerX, secondPlayerX + 2450
        ).setDuration(1400)

        AnimatorSet().apply {
            playTogether(moveFirstX, moveSecondX)
            doOnEnd {
                switchPlayerTurn()
            }
        }.start()
    }

    private fun enableMoveCard() {
        binding.chosenAssetImageView.isVisible = true
    }

    private fun showPagerAndSkipButton(isShowUp: Boolean) {
        binding.viewPagerMonopoly.isVisible = isShowUp
        binding.skipButtonImageView.isVisible = isShowUp
    }
}