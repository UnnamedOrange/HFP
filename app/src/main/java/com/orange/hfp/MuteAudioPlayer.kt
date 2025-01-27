// Copyright (c) UnnamedOrange. Licensed under the MIT License.
// See the LICENSE file in the repository root for full license text.

package com.orange.hfp

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MuteAudioPlayer : AutoCloseable {
    companion object {
        @Suppress("unused")
        private const val TAG = "MuteAudioPlayer"

        private const val BUFFER_SIZE = 1024
    }

    private val muteAudioTrack: AudioTrack
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        val attributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        val format =
            AudioFormat.Builder().setSampleRate(16000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()
        muteAudioTrack = AudioTrack.Builder().setAudioAttributes(attributes).setAudioFormat(format)
            .setBufferSizeInBytes(BUFFER_SIZE).setTransferMode(AudioTrack.MODE_STREAM).build()

        scope.launch {
            val silence = ByteArray(BUFFER_SIZE)
            muteAudioTrack.play()
            while (muteAudioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                delay(0)
                muteAudioTrack.write(silence, 0, silence.size)
            }
        }
    }

    override fun close() {
        scope.cancel()

        muteAudioTrack.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                pause()
                flush()
            }
            release()
        }
    }
}
