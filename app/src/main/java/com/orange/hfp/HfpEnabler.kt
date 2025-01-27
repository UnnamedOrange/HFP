// Copyright (c) UnnamedOrange. Licensed under the MIT License.
// See the LICENSE file in the repository root for full license text.

package com.orange.hfp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Parcelable
import android.util.Log

@SuppressLint("MissingPermission")
class HfpEnabler(private val context: Context) : AutoCloseable {
    companion object {
        @Suppress("unused")
        private const val TAG = "HfpEnabler"

        // https://stackoverflow.com/a/73311814
        inline fun <reified T : Parcelable> Intent.getParcelableExtraT(key: String): T? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(key, T::class.java)
            } else {
                @Suppress("DEPRECATION") getParcelableExtra(key) as? T
            }
    }

    private val bluetoothProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                val proxy = proxy as BluetoothHeadset
                bluetoothHeadsetProxy = proxy
                // Assume the add-all action precedes any receiver callback.
                connectedBluetoothDevices.addAll(proxy.connectedDevices)
                Log.i(TAG, "connectedBluetoothDevices (after addAll): $connectedBluetoothDevices")

                if (connectedBluetoothDevices.isNotEmpty()) {
                    startSco()
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                connectedBluetoothDevices.clear()
                bluetoothHeadsetProxy = null
            }
        }
    }

    private val bluetoothConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtraT<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    connectedBluetoothDevices.add(device!!)
                    Log.i(
                        TAG,
                        "connectedBluetoothDevices (after on connected): $connectedBluetoothDevices"
                    )

                    startSco()
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    connectedBluetoothDevices.remove(device!!)
                    Log.i(
                        TAG,
                        "connectedBluetoothDevices (after on disconnected): $connectedBluetoothDevices"
                    )

                    if (connectedBluetoothDevices.isEmpty()) {
                        stopSco()
                    }
                }
            }
        }
    }

    private val scoStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(
                AudioManager.EXTRA_SCO_AUDIO_STATE,
                AudioManager.SCO_AUDIO_STATE_ERROR,
            )) {
                AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                }

                AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                    if (connectedBluetoothDevices.isNotEmpty()) {
                        startSco()
                    }
                }

                AudioManager.SCO_AUDIO_STATE_ERROR -> {
                }
            }
        }
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var bluetoothHeadsetProxy: BluetoothHeadset? = null

    private val connectedBluetoothDevices = mutableListOf<BluetoothDevice>()

    private var muteAudioPlayer: MuteAudioPlayer? = null

    init {
        bluetoothAdapter.getProfileProxy(
            context, bluetoothProfileListener, BluetoothProfile.HEADSET
        )
        context.registerReceiver(bluetoothConnectionReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        })
        context.registerReceiver(scoStateReceiver, IntentFilter().apply {
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        })
        muteAudioPlayer = MuteAudioPlayer()

        if (!audioManager.isBluetoothScoAvailableOffCall) {
            Log.w(TAG, "The platform does not support SCO off call, HFP will not function")
        }
    }

    override fun close() {
        stopSco()

        muteAudioPlayer?.close()
        muteAudioPlayer = null
        context.unregisterReceiver(scoStateReceiver)
        context.unregisterReceiver(bluetoothConnectionReceiver)
        bluetoothHeadsetProxy?.let {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, it)
            // [bluetoothHeadsetProxy] and devices will be reset in the disconnect callback.
        }
    }

    private fun startSco() {
        audioManager.apply {
            mode = AudioManager.MODE_IN_COMMUNICATION
            isBluetoothScoOn = true
            @Suppress("DEPRECATION") startBluetoothSco()
        }
    }

    private fun stopSco() {
        audioManager.apply {
            @Suppress("DEPRECATION") stopBluetoothSco()
            isBluetoothScoOn = false
            mode = AudioManager.MODE_NORMAL
        }
    }
}
