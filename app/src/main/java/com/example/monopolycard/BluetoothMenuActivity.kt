package com.example.monopolycard

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.monopolycard.BTState.STATE_CONNECTED
import com.example.monopolycard.BTState.STATE_CONNECTING
import com.example.monopolycard.BTState.STATE_CONNECTION_FAILED
import com.example.monopolycard.BTState.STATE_LISTENING
import com.example.monopolycard.BTState.STATE_MESSAGE_RECEIVED
import com.example.monopolycard.databinding.ActivityBluetoothMenuBinding


class BluetoothMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothMenuBinding
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    val strings = mutableListOf<String>()

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevices = mutableListOf<BluetoothDevice>()

    private var handler: Handler = Handler { msg ->
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            when (msg.what) {
                STATE_LISTENING ->
                    binding.status.text = "listening"
                STATE_CONNECTING ->
                    binding.status.text = "connecting"
                STATE_CONNECTED ->
                    binding.status.text = "connected"
                STATE_CONNECTION_FAILED ->
                    binding.status.text = "failed"
                STATE_MESSAGE_RECEIVED -> {

                    val readBuffer = msg.obj as ByteArray
                    val tempMessage = String(readBuffer, 0, msg.arg1)

                    binding.msg.text = tempMessage
                }
            }
        }

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationPermission()
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        implementListeners()
    }

    private fun implementListeners() {
        var clientClass: BTClient? = null
        var serverClass: BTServer? = null

        binding.btnDiscover.setOnClickListener {
            if (!bluetoothAdapter.isEnabled){
                checkBTPermission {
                    bluetoothAdapter.enable()
                }
            }
            discoveryDevices()
        }

        binding.listView1.onItemClickListener = OnItemClickListener { parent, view, i, id -> // ClientClass oluşturma
            clientClass = BTClient(this, bluetoothDevices[i], handler)
            clientClass?.start()

            binding.status.text = "connecting"
        }

        binding.btnCreateServer.setOnClickListener {
            serverClass = BTServer(bluetoothAdapter, handler)
            serverClass?.start()
        }

        binding.btnSend.setOnClickListener {
            val string: String = java.lang.String.valueOf(binding.writemsg.text)
            if (clientClass != null) {
                println("work on client")
                clientClass?.sendReceive?.write(string.toByteArray())
            } else if (serverClass != null) {
                println("work on server")
                serverClass?.sendReceive?.write(string.toByteArray())
            } else {
                println("not work")
            }
        }
    }

    private fun discoveryDevices() {
        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(mReceiver, filter)
        val arrayAdapter = ArrayAdapter(
            applicationContext, R.layout.simple_list_item_1, strings
        )

        binding.listView1.adapter = arrayAdapter
        checkBTPermission {
            bluetoothAdapter.startDiscovery()
        }
    }

    private fun searchExistingBondedDevices() {
        checkBTPermission {
            val devices = bluetoothAdapter.bondedDevices

            if (devices.size > 0) {
                for (device in devices) {
                    bluetoothDevices.add(device)
                    strings.add(device.name)
                    print("device.name ${device.name}")
                }

                val arrayAdapter = ArrayAdapter(
                    applicationContext, R.layout.simple_list_item_1, strings
                )

                binding.listView1.adapter = arrayAdapter
                return@checkBTPermission true
            }
            return@checkBTPermission false
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun checkBTPermission(onAcceptedAction: () -> Boolean) {
        println("hey")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            } else {
                onAcceptedAction.invoke()
            }
        } else {
            onAcceptedAction.invoke()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                binding.status.text = "start discovery..."
                bluetoothDevices.clear()
                strings.clear()
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                binding.status.text = "discovery done"
            } else if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?

                if (device != null && device.name != null && !bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device)
                    strings.add(device.name)

                    val arrayAdapter = ArrayAdapter(
                        applicationContext, R.layout.simple_list_item_1, strings
                    )

                    binding.listView1.adapter = arrayAdapter
                }
            }
        }
    }
}