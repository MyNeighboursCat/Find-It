/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.controller

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.IBinder
import com.myapp.model.SoundEffect
import kotlinx.coroutines.*

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
class AudioFocusService : Service, OnAudioFocusChangeListener {
    private var context1: Context? = null
    @Volatile
    private var soundEffect1: SoundEffect? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Default)

    // Leave in because manifest complains.
    constructor() : super()

    constructor(context: Context?, soundEffect: SoundEffect?) {
        context1 = context
        soundEffect1 = soundEffect
        if (context1 != null) {
            audioManager = context1!!
                .getSystemService(AUDIO_SERVICE) as AudioManager
        }
    }

    fun abandonFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (focusRequest != null) {
                    audioManager!!.abandonAudioFocusRequest(focusRequest!!)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager!!.abandonAudioFocus(this)
            }
        }
        releaseResources()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (soundEffect1 == null) {
            return
        }
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (soundEffect1!!.mediaPlayers1 != null) {
                if (soundEffect1!!.mediaPlayers1!!.size == 0) {
                    // Must have been AUDIOFOCUS_LOSS
                    coroutineScope.launch {
                        soundEffect1!!.mediaPlayersCreate()
                    }
                } else {
                    soundEffect1!!.mediaPlayersTurnUpVolume()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> { }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> soundEffect1!!.mediaPlayersPause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                soundEffect1!!.mediaPlayersTurnDownVolume()
            }
            else -> {
            }
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    private fun releaseResources() {
        context1 = null
        soundEffect1 = null
        audioManager = null
        focusRequest = null
        coroutineScope.cancel()
    }

    fun requestFocus(): Boolean {
        if (audioManager == null) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager = context1!!.getSystemService(AUDIO_SERVICE) as AudioManager
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(this)
                .build()
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager!!
                .requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager!!.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
                )
        }
    }
}