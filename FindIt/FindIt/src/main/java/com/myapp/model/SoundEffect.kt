/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.model

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.myapp.R
import com.myapp.controller.AudioFocusService
import com.myapp.controller.SoundBroadcastReceiver
import kotlinx.coroutines.*
import java.util.*

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
class SoundEffect(
    @field:Volatile private var context1: Context?,
    @field:Volatile private var mediaPlayerTypes1: ArrayList<SoundType>?,
    soundTypePlay: SoundType?, private val soundMode1: Boolean
) {
    private val tag = this.javaClass.simpleName

    @Volatile
    private var currentSoundNumber1 = 0

    @Volatile
    var mediaPlayers1: EnumMap<SoundType, MediaPlayer>? = EnumMap(SoundType::class.java)
        get() = if (soundPlayable) {
            field
        } else null

    @Volatile
    private var soundPlayable = false

    @Volatile
    private var audioFocusService1: AudioFocusService? = null

    @Volatile
    private var soundBroadcastReceiver1: SoundBroadcastReceiver? = null

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Default)

    private fun createMediaPlayer(
        soundType: SoundType,
        soundResource1: Int
    ) {
        try {
            val mediaPlayer1 = MediaPlayer.create(
                context1,
                soundResource1
            )
            mediaPlayerSetVolume(mediaPlayer1)
            mediaPlayers1!![soundType] = mediaPlayer1
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun doDatabase(soundTypePlay: SoundType?) {
        coroutineScope.launch {
            currentSoundNumber1 = if (soundMode1) {
                5
            } else {
                0
            }
            mediaPlayersCreate()

            coroutineScope.launch(Dispatchers.Main) {
                if (soundTypePlay != null) {
                    doSound(soundTypePlay)
                }
            }
        }
    }

    // default package access
    fun doSound(soundType: SoundType) {
        if (!soundPlayable) {
            return
        }
        try {
            // HashMap reference comment
            // Note: the implementation of HashMap is not synchronized.
            // If one thread of several threads accessing an instance modifies
            // the map structurally,
            // access to the map needs to be synchronized.
            // A structural modification is an operation that adds or removes an
            // entry.
            // Changes in the value of an entry are not structural changes.
            val mediaPlayer1 = mediaPlayers1!![soundType]
            if (mediaPlayer1 != null) {
                mediaPlayer1.seekTo(0)
                mediaPlayer1.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun mediaPlayersCreate() = withContext(Dispatchers.Default) {
        if (currentSoundNumber1 == 0) {
            return@withContext
        }
        /* This code must be done before createMediaPlayer() because that uses mediaPlayers1 whose
        set() uses soundPlayable.
         */
        audioFocusService1 = AudioFocusService(context1, this@SoundEffect)
        if (audioFocusService1!!.requestFocus()) {
            soundBroadcastReceiver1 = SoundBroadcastReceiver(
                context1,
                this@SoundEffect
            )
            soundPlayable = true
        }
        for (soundType1 in mediaPlayerTypes1!!) {
            val resource1: Int = when (soundType1) {
                SoundType.SUCCESS -> R.raw.success
                SoundType.FAIL -> R.raw.fail
                else -> {
                    Log.e(tag, "mediaPlayersCreate() error 1")
                    throw RuntimeException()
                }
            }
            createMediaPlayer(soundType1, resource1)
        }
    }

    private fun mediaPlayerSetVolume(mediaPlayer: MediaPlayer) {
        mediaPlayer.setVolume(
            currentSoundNumber1 / 10.0f,
            currentSoundNumber1 / 10.0f
        )
    }

    fun mediaPlayersPause() {
        if (!soundPlayable) {
            return
        }
        var mediaPlayer1: MediaPlayer?
        for (soundType1 in mediaPlayerTypes1!!) {
            try {
                mediaPlayer1 = mediaPlayers1!![soundType1]
                if (mediaPlayer1 != null && mediaPlayer1.isPlaying) {
                    mediaPlayer1.pause()
                    mediaPlayer1.seekTo(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun mediaPlayersRelease() {
        if (mediaPlayers1 != null && mediaPlayerTypes1 != null) {
            if (mediaPlayers1!!.size > 0) {
                for (soundType1 in mediaPlayerTypes1!!) {
                    releaseMediaPlayer(soundType1)
                }
            }
        }
        soundPlayable = false
        if (mediaPlayerTypes1 != null) {
            mediaPlayerTypes1!!.clear()
            mediaPlayerTypes1 = null
        }
        if (mediaPlayers1 != null) {
            mediaPlayers1!!.clear()
            mediaPlayers1 = null
        }
        if (audioFocusService1 != null) {
            audioFocusService1!!.abandonFocus()
            audioFocusService1 = null
        }
        if (soundBroadcastReceiver1 != null) {
            soundBroadcastReceiver1!!.unRegisterReceiver()
            soundBroadcastReceiver1 = null
        }
        context1 = null
        coroutineScope.cancel()
    }

    fun mediaPlayersTurnDownVolume() {
        if (!soundPlayable) {
            return
        }
        var mediaPlayer1: MediaPlayer?
        for (soundType1 in mediaPlayerTypes1!!) {
            try {
                mediaPlayer1 = mediaPlayers1!![soundType1]
                mediaPlayer1?.setVolume(0.0f, 0.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun mediaPlayersTurnUpVolume() {
        if (!soundPlayable) {
            return
        }
        var mediaPlayer1: MediaPlayer?
        for (soundType1 in mediaPlayerTypes1!!) {
            try {
                mediaPlayer1 = mediaPlayers1!![soundType1]
                if (mediaPlayer1 != null) {
                    mediaPlayerSetVolume(mediaPlayer1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun releaseMediaPlayer(soundType: SoundType) {
        try {
            val mediaPlayer1 = mediaPlayers1!![soundType]
            mediaPlayer1?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    enum class SoundType {
        SUCCESS, FAIL
    }

    init {
        doDatabase(soundTypePlay)
    }
}