package com.example.monopolycard

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.monopolycard.cards.CardItem
import com.example.monopolycard.databinding.ActivityMainBinding
import com.example.monopolycard.decks.DeckAdapter
import com.example.monopolycard.decks.DeckItem
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sp: SoundPool
    private var cashSound: Int = 0
    private var moneyRotateSound: Int = 0
    private var assetPlacedSound: Int = 0
    private var spLoaded = false

    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false
    private val listPlayer = mutableListOf<DeckItem>()
    private val cardItems = mutableListOf<CardItem>()
    private val cardItems2 = mutableListOf<CardItem>()

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
        cardItems.add(CardItem(R.drawable.spr_py_brown_house_card))
        cardItems.add(CardItem(R.drawable.spr_py_orange_house_card))
        cardItems.add(CardItem(R.drawable.spr_py_2m_card))
        cardItems.add(CardItem(R.drawable.spr_py_2m_card))
        cardItems.add(CardItem(R.drawable.spr_py_orange_house_card))
        cardItems2.addAll(cardItems)

        listPlayer.add(DeckItem(true, cardItems, cardItems2))
        listPlayer.add(DeckItem(false, cardItems, cardItems2))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        binding.monopolyViewPager.offscreenPageLimit = 3
        binding.monopolyViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        binding.monopolyViewPager.adapter = DeckAdapter(this@MainActivity, listPlayer,
            onPagerDown = {
                binding.monopolyViewPager.isUserInputEnabled = false
            },
            onPagerUp = {
                binding.monopolyViewPager.isUserInputEnabled = true
            },
            onPagerSwipe = { isNext ->
                if (isNext) nextPlayer()
                else prevPlayer()
            }
        )
        binding.monopolyViewPager.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("x: ${event.rawX}")
                    println("x: ${event.rawY}")
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun nextPlayer() {
        val currentItem = binding.monopolyViewPager.currentItem
        if (currentItem < cardItems.size - 1)
            binding.monopolyViewPager.setCurrentItem(currentItem + 1, true)
    }

    private fun prevPlayer() {
        val currentItem = binding.monopolyViewPager.currentItem
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
//            hideCard(true)
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
}