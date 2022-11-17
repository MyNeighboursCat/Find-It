/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.controller

import android.content.BroadcastReceiver
import android.content.Context
import com.myapp.model.SoundEffect
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
class SoundBroadcastReceiver : BroadcastReceiver {
    private var context1: Context? = null
    private var soundEffect1: SoundEffect? = null

    // Leave in because manifest complains.
    constructor() : super()

    constructor(context: Context?, soundEffect: SoundEffect?) {
        context1 = context
        soundEffect1 = soundEffect
        registerReceiver()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (soundEffect1 == null) {
            return
        }
        val stringIntent = intent.action
        if (stringIntent != null) {
            if (stringIntent == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                soundEffect1!!.mediaPlayersTurnDownVolume()
            }
        }
    }

    private fun registerReceiver() {
        if (context1 != null) {
            val intentFilter = IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY
            )
            context1!!.registerReceiver(this, intentFilter)
        }
    }

    private fun releaseResources() {
        context1 = null
        soundEffect1 = null
    }

    fun unRegisterReceiver() {
        if (context1 != null) {
            context1!!.unregisterReceiver(this)
        }
        releaseResources()
    }
}