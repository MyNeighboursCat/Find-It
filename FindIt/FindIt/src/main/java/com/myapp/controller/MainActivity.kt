/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.controller

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.myapp.R
import com.myapp.databinding.MainBinding
import com.myapp.model.SoundEffect
import com.myapp.model.SoundEffect.SoundType
import java.util.*

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
class MainActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private var showResetDataDialog = false
    private var allowEvents = true
    private var startingGame = false
    private var soundMode = false
    private var controlsMode = 0
    private var mainBinding: MainBinding? = null
    private var tableLayout1: TableLayout? = null
    private var controlsButton1: Button? = null
    private var soundButton1: Button? = null
    private var soundImageButton1: ImageButton? = null
    private var resetDataAlertDialog: AlertDialog? = null
    private var soundEffect1: SoundEffect? = null
    private var startForResult = this.registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val extras = intent.extras
                if (extras != null) {
                    setHighScore(
                        extras.getLong(
                            this@MainActivity.getString(
                                R.string.high_score_intent_parameter
                            )
                        )
                    )
                }
            }
        }
        startingGame = false
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            // Restoring - cannot do in onRestoreInstanceState() because that is called after
            // onCreate() so dialog would not appear.
            val cnt = 0
            val booleanArray1 = savedInstanceState
                .getBooleanArray(MAIN_BUNDLE + cnt)
            if (booleanArray1 != null) {
                showResetDataDialog = booleanArray1[0]
                allowEvents = booleanArray1[1]
                startingGame = booleanArray1[2]
            }
        }
        val settings = getPreferences(MODE_PRIVATE)
        soundMode = settings.getBoolean(SOUND_MODE, true)
        controlsMode = settings.getInt(CONTROLS_MODE, 0)

        // Make sure only the music stream volume is adjusted.
        this.volumeControlStream = AudioManager.STREAM_MUSIC
        mainBinding = MainBinding.inflate(this.layoutInflater)
        this.setContentView(mainBinding!!.root)
        setSupportActionBar(mainBinding!!.toolbar)
        tableLayout1 = mainBinding!!.tableLayout1
        controlsButton1 = mainBinding!!.controlsButton
        soundImageButton1 = mainBinding!!.soundImageButton
        soundButton1 = mainBinding!!.soundButton
        doButtonOnClickListeners()
        doControlsMode()
        doSoundMode()
        val mediaPlayerTypes = ArrayList<SoundType>()
        mediaPlayerTypes.add(SoundType.SUCCESS)
        soundEffect1 = SoundEffect(
            this.applicationContext, mediaPlayerTypes,
            SoundType.SUCCESS, soundMode
        )

        // Use this to see which sensors are available on the current device.
        /*SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: deviceSensors) {
            Log.i(TAG, "Sensor Name: " + sensor.getName());
        }*/
    }

    override fun onStart() {
        super.onStart()
        if (showResetDataDialog) {
            doResetDataAlertDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!startingGame) {
            setButtonsStatus(true)
        }
    }

    override fun onStop() {
        showResetDataDialog = false
        if (resetDataAlertDialog != null) {
            resetDataAlertDialog!!.dismiss()
            resetDataAlertDialog = null
            showResetDataDialog = true
        }
        if (soundEffect1 != null) {
            soundEffect1!!.mediaPlayersRelease()
            soundEffect1 = null
        }
        super.onStop()
    }

    override fun onDestroy() {
        releaseResources()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val booleanArray1 = booleanArrayOf(
            showResetDataDialog, allowEvents,
            startingGame
        )
        val cnt = 0
        outState.putBooleanArray(MAIN_BUNDLE + cnt, booleanArray1)
        super.onSaveInstanceState(outState)
    }

    private fun doButtonOnClickListeners() {
        mainBinding!!.playImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.playButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.controlsImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.controlsButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.highScoreImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.highScoreButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.soundImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.soundButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.helpImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.helpButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.aboutImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.aboutButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.resetDataImageButton.setOnClickListener { v -> onClickButtonHandler(v) }
        mainBinding!!.resetDataButton.setOnClickListener { v -> onClickButtonHandler(v) }
    }

    private fun onClickButtonHandler(v: View) {
        // allowEvents is needed because disabling buttons takes time.  It is therefore possible to
        // trigger the same event more than once in a short space of time without this variable
        // check.
        if (!allowEvents) {
            return
        }
        allowEvents = false
        val settings = getPreferences(MODE_PRIVATE)
        val intent: Intent
        setButtonsStatus(false)
        if (v === mainBinding!!.playImageButton || v === mainBinding!!.playButton) {
            startingGame = true
            doGame()
        } else if (v === mainBinding!!.controlsImageButton || v === mainBinding!!.controlsButton) {
            controlsMode += 1
            if (controlsMode > 2) {
                controlsMode = 0
            }
            val editor = settings.edit()
            editor.putInt(CONTROLS_MODE, controlsMode)
            editor.apply()
            doControlsMode()
            setButtonsStatus(true)
        } else if (v === mainBinding!!.highScoreImageButton || v === mainBinding!!.highScoreButton) {
            intent = Intent(this, HighScoreActivity::class.java)
            val highScore = settings.getLong(HIGH_SCORE, 0L)
            intent.putExtra(
                this.getString(R.string.high_score_intent_parameter),
                highScore.toString()
            )
            this.startActivity(intent)
        } else if (v === mainBinding!!.soundImageButton || v === mainBinding!!.soundButton) {
            soundMode = !soundMode
            val editor2 = settings.edit()
            editor2.putBoolean(SOUND_MODE, soundMode)
            editor2.apply()
            doSoundMode()
            setButtonsStatus(true)
        } else if (v === mainBinding!!.helpImageButton || v === mainBinding!!.helpButton) {
            intent = Intent(this, HelpActivity::class.java)
            this.startActivity(intent)
        } else if (v === mainBinding!!.aboutImageButton || v === mainBinding!!.aboutButton) {
            intent = Intent(this, AboutActivity::class.java)
            this.startActivity(intent)
        } else if (v === mainBinding!!.resetDataImageButton || v === mainBinding!!.resetDataButton) {
            doResetDataAlertDialog()
        } else {
            Log.e(tag, "onClickButtonHandler error 1")
            throw RuntimeException()
        }
    }

    private fun doGame() {
        val settings = getPreferences(MODE_PRIVATE)
        val highScore = settings.getLong(HIGH_SCORE, 0L)
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(this.getString(R.string.sound_mode_intent_parameter), soundMode)
        intent.putExtra(this.getString(R.string.high_score_intent_parameter), highScore)
        intent.putExtra(this.getString(R.string.controls_mode_intent_parameter), controlsMode)
        startForResult.launch(intent)
    }

    private fun doControlsMode() {
        if (controlsButton1 == null) {
            return
        }
        var string1 = this.getString(R.string.controls) + " : "
        string1 += when (controlsMode) {
            0 -> this.getString(R.string.swipe)
            1 -> this.getString(R.string.tilt)
            2 -> this.getString(R.string.rotate)
            else -> {
                Log.e(tag, "doControlsMode error 1")
                throw RuntimeException()
            }
        }
        controlsButton1!!.text = string1
    }

    private fun doSoundMode() {
        if (soundImageButton1 == null || soundButton1 == null) {
            return
        }
        val imageResource: Int
        var string1 = this.getString(R.string.sound) + " : "
        if (soundMode) {
            imageResource = R.drawable.ic_menu_volume_on
            string1 += this.getString(R.string.on)
        } else {
            imageResource = R.drawable.ic_menu_volume_muted
            string1 += this.getString(R.string.off)
        }
        soundImageButton1!!.setImageResource(imageResource)
        soundButton1!!.text = string1
    }

    private fun doResetDataAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(
            this@MainActivity
        )
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert)
        alertDialogBuilder.setTitle(this.getString(R.string.reset_data))
        alertDialogBuilder.setMessage(this.getString(R.string.reset_data_warning))

        // Back button is disabled.
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(
            this.getString(android.R.string.ok)
        ) { dialog, id -> // Reset preferences to defaults.
            soundMode = true
            val settings = getPreferences(MODE_PRIVATE)
            val editor = settings.edit()
            editor.putLong(HIGH_SCORE, 0L)
            editor.putBoolean(SOUND_MODE, soundMode)
            editor.apply()
            doSoundMode()
            val toast = Toast.makeText(
                this@MainActivity.applicationContext,
                R.string.data_has_been_reset, Toast.LENGTH_SHORT
            )
            toast.show()
            if (resetDataAlertDialog != null) {
                resetDataAlertDialog!!.dismiss()
                resetDataAlertDialog = null
            }
            setButtonsStatus(true)
        }.setNegativeButton(
            this.getString(android.R.string.cancel)
        ) { dialog, which ->
            val toast = Toast.makeText(
                this@MainActivity.applicationContext,
                R.string.data_has_not_been_reset, Toast.LENGTH_SHORT
            )
            toast.show()
            if (resetDataAlertDialog != null) {
                resetDataAlertDialog!!.dismiss()
                resetDataAlertDialog = null
            }
            setButtonsStatus(true)
        }
        resetDataAlertDialog = alertDialogBuilder.create()
        resetDataAlertDialog!!.setOwnerActivity(this@MainActivity)
        resetDataAlertDialog!!.show()
    }

    private fun setButtonsStatus(enableButtons: Boolean) {
        if (startingGame && enableButtons) {
            return
        }
        if (!enableButtons) {
            allowEvents = false
        }
        if (tableLayout1 == null) {
            return
        }
        val childMaximum: Int = tableLayout1!!.childCount
        for (i in 0 until childMaximum) {
            val tableRow1 = tableLayout1!!.getChildAt(i) as TableRow
            val childMaximum2 = tableRow1.childCount
            for (j in 0 until childMaximum2) {
                tableRow1.getChildAt(j).isEnabled = enableButtons
            }
        }
        if (enableButtons) {
            allowEvents = true
        }
    }

    private fun releaseResources() {
        mainBinding = null
        tableLayout1 = null
        controlsButton1 = null
        soundButton1 = null
        soundImageButton1 = null
        resetDataAlertDialog = null
        soundEffect1 = null
    }

    private fun setHighScore(newScore: Long) {
        val settings = getPreferences(MODE_PRIVATE)
        val editor = settings.edit()
        val highScore = settings.getLong(HIGH_SCORE, 0L)
        if (newScore > highScore) {
            editor.putLong(HIGH_SCORE, newScore)
        }
        editor.apply()
    }

    companion object {
        private const val MAIN_BUNDLE = "MAIN_BUNDLE_"
        private const val HIGH_SCORE = "HIGH_SCORE"
        private const val CONTROLS_MODE = "CONTROLS_MODE"
        private const val SOUND_MODE = "SOUND_MODE"
    }
}