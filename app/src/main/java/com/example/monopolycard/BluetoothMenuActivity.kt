package com.example.monopolycard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.monopolycard.databinding.ActivityBluetoothMenuBinding

class BluetoothMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}