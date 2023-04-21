/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.myapp.model

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.myapp.R
import com.myapp.controller.GameActivity
import com.myapp.model.SoundEffect.SoundType
import com.myapp.view.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

/**
 * @author Colin Walters
 * @version 1.0, 20/04/2023
 */
// default package access
internal class FindIt : Gaming {
    private val tag = this.javaClass.simpleName

    // Threads: volatile keyword is used to make sure different threads have the same value of a
    // variable.
    // the synchronized keyword used in the method declaration is an alternative
    @Volatile
    override var isPaused = false
        private set

    @Volatile
    override var missesLeft = 1
    override var longScore = 0L
    private var soundMode1 = false
    private var canvasWidth = 0
    private var canvasHeight = 0
    private var gameActivity1: GameActivity? = null
    private var mainView1: MainView? = null
    private var canvas1: Canvas? = null
    private var bitmap1: Bitmap? = null
    private var mediaPlayerTypes: ArrayList<SoundType>? = null

    @Volatile
    override var soundEffect1: SoundEffect? = null
        private set
    private var res: Resources? = null
    private var paintBackground: Paint? = null

    @Volatile
    private var gameStarted = true
    private var paintText: Paint? = null

    // WARNING: Need to synchronize everywhere the colour array is used because the swipe and motion
    // sensor code alters the position of the viewpoint in the grid from the UI thread.
    @Volatile
    private var colourArray: ColourArray? = ColourArray()

    @Volatile
    private var initialAxisXOrigin = INVALID_DEFAULT_VALUE

    @Volatile
    private var timeTextView: TextView? = null

    @Volatile
    private var countDownCurrentTime = COUNT_DOWN_TIMER_MAXIMUM

    @Volatile
    private var countDownTimer: CountDownTimer? = null

    @Volatile
    private var tiltMoveCounter = 0

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    // default package access
    constructor(bundle1: Bundle) : super() {
        // can't call super
        // super(bundle1);

        // Don't use 'counter' as variable name as it increase with each line
        // for some reason!
        var cnt = 0
        val booleanArray1 = bundle1.getBooleanArray(GAME_FIND_IT_BUNDLE + cnt++)
        if (booleanArray1 != null) {
            isPaused = booleanArray1[0]
            soundMode1 = booleanArray1[1]
            gameStarted = booleanArray1[2]
        }
        missesLeft = bundle1.getInt(
            GAME_FIND_IT_BUNDLE
                    + cnt++
        )
        val longArray1 = bundle1.getLongArray(
            GAME_FIND_IT_BUNDLE
                    + cnt
        )
        if (longArray1 != null) {
            longScore = longArray1[0]
            countDownCurrentTime = longArray1[1]
        }
        val colourArrayBundle = bundle1.getBundle(COLOUR_ARRAY_BUNDLE + "0")
        if (colourArrayBundle != null) {
            colourArray = ColourArray(colourArrayBundle)
        }
    }

    // default package access
    constructor(activity: GameActivity?, canvas: Canvas?, soundMode: Boolean) : super() {
        gameActivity1 = activity
        canvas1 = canvas
        canvasWidth = canvas1!!.width
        canvasHeight = canvas1!!.height
        soundMode1 = soundMode
        doConstructorInitialize(true)
        setSoundEffect1()
    }

    // must be public not private because implementing Gaming interface method
    override fun doConstructorInitialize(firstInstance: Boolean) {
        mainView1 = gameActivity1!!.mainView
        res = gameActivity1!!.resources

        // paints
        paintText = Paint()
        paintText!!.textSize = res!!.getDimension(R.dimen.font_size)
        paintText!!.color = Color.BLACK
        paintText!!.textAlign = Paint.Align.CENTER
        paintText!!.isFakeBoldText = true
        paintBackground = Paint()
        paintBackground!!.color = Color.BLACK
        timeTextView = gameActivity1!!.gameBinding!!.timeTextView
        doCountDownTimer()
    }

    private fun doCountDownTimer() {
        setCounterDisplay()
        if (isPaused) {
            return
        }
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        countDownTimer = object : CountDownTimer(countDownCurrentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownCurrentTime = millisUntilFinished
                setCounterDisplay()
            }

            override fun onFinish() {
                timeTextView!!.text = "0"
                // end game with failure code of -1
                missesLeft = -1
                soundEffect1!!.doSound(SoundType.FAIL)
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun setCounterDisplay() {
        timeTextView!!.text = (countDownCurrentTime / 1000).toString()
    }

    private fun doDirection(axisX: Float, axisY: Float, controlsMode: Int) {
        // NOTE: both swipe and motion sensor code have been synchronised so no need to do it here.
        if (colourArray == null) {
            return
        }
        colourArray!!.doMove(axisX, axisY, controlsMode)
    }

    override fun doSwipe(
        event1: MotionEvent?, event2: MotionEvent?, velocityX: Float,
        velocityY: Float, minFlingVelocity: Int, maxFlingVelocity: Int,
        touchSlop: Int
    ) {
        var velocityX = velocityX
        var velocityY = velocityY
        var touchSlop = touchSlop
        synchronized(this) {
            val swipeDistanceX = abs(event1!!.rawX - event2!!.rawX)
                .toInt()
            val swipeDistanceY = abs(event1.rawY - event2.rawY)
                .toInt()
            val absVelocityX = abs(velocityX).toInt()
            val absVelocityY = abs(velocityY).toInt()

            // Override touch slop.
            touchSlop = 100
            if (swipeDistanceX < touchSlop || absVelocityX < minFlingVelocity
                || absVelocityX > maxFlingVelocity
            ) {
                velocityX = 0f
            }
            if (swipeDistanceY < touchSlop || absVelocityY < minFlingVelocity
                || absVelocityY > maxFlingVelocity
            ) {
                velocityY = 0f
            }
            doDirection(velocityX, velocityY, 0)
        }
    }

    // called from background thread
    override fun doDraw() {
        if (gameActivity1 == null || canvas1 == null
            || mainView1 == null
        ) {
            return
        }
        drawInitialize()
        drawSprites()
        synchronized(this) { tiltMoveCounter++ }
        mainView1!!.drawOnSurfaceHolderCanvas()
    }

    private fun doMessage() {
        synchronized(this) {
            // Main thread.
            coroutineScope.launch {
                val colourTextView = this@FindIt.gameActivity1!!.gameBinding!!.colourTextView
                val scoreTextView = this@FindIt.gameActivity1!!.gameBinding!!.scoreTextView
                val positionTextView = this@FindIt.gameActivity1!!.gameBinding!!.positionTextView
                if (missesLeft < 0) {
                    colourTextView.setBackgroundColor(Color.BLACK)
                    scoreTextView.text = ""
                    positionTextView.background = ResourcesCompat.getDrawable(
                        this@FindIt.gameActivity1!!.resources, R.drawable.grid_0_0,
                        null
                    )
                } else {
                    colourTextView.setBackgroundColor(
                        colourArray!!.colourToFind
                    )
                    // NOTE: Uncomment this code to show the degrees for testing.
                    /*float[] arrayDegrees = FindIt.this.colourArray
                                    .getPositionDegrees();
                            scoreTextView.setText(FindIt.this.longScore + " (" +
                                    (int) arrayDegrees[0] + ", " + (int) arrayDegrees[1] + ")");
                                    */scoreTextView.text = longScore.toString()

                    // Use this to display the position coordinates.
                    val arrayPosition = colourArray!!.positionDisplayed
                    val position = "(" + (arrayPosition!![0] + 1) + ", " +
                            (arrayPosition[1] + 1) + ")"
                    val drawable: Int = when (position) {
                        "(1, 1)" -> R.drawable.grid_1_1
                        "(1, 2)" -> R.drawable.grid_1_2
                        "(1, 3)" -> R.drawable.grid_1_3
                        "(2, 1)" -> R.drawable.grid_2_1
                        "(2, 2)" -> R.drawable.grid_2_2
                        "(2, 3)" -> R.drawable.grid_2_3
                        "(3, 1)" -> R.drawable.grid_3_1
                        "(3, 2)" -> R.drawable.grid_3_2
                        "(3, 3)" -> R.drawable.grid_3_3
                        else -> {
                            Log.e(
                                this@FindIt.gameActivity1!!.tAG,
                                "doMessage error 1"
                            )
                            throw RuntimeException()
                        }
                    }
                    positionTextView.background = ResourcesCompat.getDrawable(
                        this@FindIt.gameActivity1!!.resources, drawable, null
                    )
                }
            }
        }
    }

    override fun doMotionSensor(axisX: Float, axisY: Float, controlsMode: Int) {
        // This method is called by UI thread so lock this object to keep the colour array positions
        // in sync with the background thread.
        var axisX = axisX
        var axisY = axisY
        synchronized(this) {
            when (controlsMode) {
                1 -> {
                    // Tilt: Accelerometer.
                    if (tiltMoveCounter < TILT_MOVE_COUNTER_MAXIMUM) {
                        return
                    } else {
                        tiltMoveCounter = 0
                    }
                    if (abs(axisX) < ACCELERATION_MINIMUM) {
                        axisX = 0f
                    }
                    if (abs(axisY) < ACCELERATION_MINIMUM) {
                        axisY = 0f
                    }
                }
                2 -> {
                    // Rotation vector.
                    // By this stage...
                    // axisX is in the range of 0-360 degrees
                    // axisY is in the range of 0-90 degrees:
                    // 0 when the top of the phone is pointing upwards with the screen pointing
                    // towards the user.
                    // 90 when the top of the phone is pointing away from the user with the screen
                    // pointing upwards.
                    // negative when device is tilted beyond parallel with the screen pointing down
                    // on the user.
                    //
                    // Volatile variable so must lock this object.  Lock this whole section
                    // otherwise the first time rotation sensor starts, the x-axis origin is in
                    // wrong place.
                    if (initialAxisXOrigin == INVALID_DEFAULT_VALUE) {
                        // initialAxisXOrigin is in the sensor degrees.
                        // It is the offset of the converted origin from origin of the sensor.
                        initialAxisXOrigin = axisX - ColourArray.MAXIMUM_X_DEGREES / 2.0f
                        // Handle degrees to the left of zero i.e. 360 and below.
                        if (initialAxisXOrigin < 0) {
                            initialAxisXOrigin += 360f
                        }
                    }

                    // Convert axisX values from sensor degrees to converted degrees.
                    axisX -= initialAxisXOrigin
                    // Handle degrees to the left of zero i.e. 360 and below.
                    if (axisX < 0) {
                        axisX += 360f
                    }
                }
                else -> {
                    Log.e(tag, "doMotionSensor error 1")
                    throw RuntimeException()
                }
            }
            doDirection(axisX, axisY, controlsMode)
        }
    }

    override fun doSingleTap() {
        synchronized(this) {
            if (colourArray == null) {
                return
            }
            if (gameStarted) {
                if (isPaused) {
                    return
                }
            } else {
                gameStarted = true
                return
            }
            if (colourArray!!.checkColourSelectedMatchesColourToFind()) {
                longScore += 1
                synchronized(this) { countDownCurrentTime += COUNT_DOWN_TIMER_SUCCESS_ADD_BACK }
                doCountDownTimer()
                soundEffect1!!.doSound(SoundType.SUCCESS)
            } else {
                // end game with failure code of -1
                //this.missesLeft = -1;
                soundEffect1!!.doSound(SoundType.FAIL)
            }
        }
    }

    private fun drawInitialize() {
        doMessage()

        // Background
        if (bitmap1 == null) {
            bitmap1 = Bitmap.createBitmap(
                canvasWidth,
                canvasHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap1!!)
            canvas.drawRect(
                0.0f, 0.0f, canvasWidth.toFloat(), canvasHeight.toFloat(),
                paintBackground!!
            )
        }
        canvas1!!.drawBitmap(bitmap1!!, 0.0f, 0.0f, null)
    }

    private fun drawSprites() {
        synchronized(this) {
            paintBackground!!.color = colourArray!!.colourToDisplay
            canvas1!!.drawRect(
                0.0f, 0.0f, canvasWidth.toFloat(), canvasHeight.toFloat(),
                paintBackground!!
            )
        }
    }

    override fun releaseResources() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
        if (colourArray != null) {
            colourArray!!.releaseResources()
            colourArray = null
        }
        if (mediaPlayerTypes != null) {
            mediaPlayerTypes!!.clear()
            mediaPlayerTypes = null
        }
        if (soundEffect1 != null) {
            soundEffect1!!.mediaPlayersRelease()
            soundEffect1 = null
        }
        gameActivity1 = null
        mainView1 = null
        canvas1 = null
        bitmap1 = null
        timeTextView = null
        res = null
        paintBackground = null
        paintText = null
    }

    override fun resetForNextTime() {
        doConstructorInitialize(true)

        // the sound effects are re-created in Game.isEndGame() when this game
        // is restarted
        if (mediaPlayerTypes != null) {
            mediaPlayerTypes!!.clear()
            mediaPlayerTypes = null
        }
        if (soundEffect1 != null) {
            soundEffect1!!.mediaPlayersRelease()
            soundEffect1 = null
        }
    }

    override fun resetGame(activity: GameActivity?, canvas: Canvas?) {
        gameActivity1 = activity
        canvas1 = canvas
        canvasWidth = canvas1!!.width
        canvasHeight = canvas1!!.height
        doConstructorInitialize(false)
        setSoundEffect1()
    }

    override fun setPaused() {
        synchronized(this) { isPaused = !isPaused }
        if (isPaused) {
            if (countDownTimer != null) {
                countDownTimer!!.cancel()
            }
            initialAxisXOrigin = INVALID_DEFAULT_VALUE
        } else {
            doCountDownTimer()
        }
    }

    override fun setSoundEffect1() {
        // must reset mediaPlayerTypes because arrays are passed by reference
        // so SoundEffect.mediaPlayersRelease() is called by
        // GameActivity.onStop()
        // which clears mediaPlayerTypes1
        // this method is called by GameActivity.onRestart()
        mediaPlayerTypes = ArrayList()
        mediaPlayerTypes!!.add(SoundType.SUCCESS)
        mediaPlayerTypes!!.add(SoundType.FAIL)
        soundEffect1 = SoundEffect(
            gameActivity1!!.applicationContext,
            mediaPlayerTypes, null, soundMode1
        )
    }

    override fun writeToBundle(bundle1: Bundle?): Bundle {
        // Don't use 'counter' as variable name as it increase with each line
        // for some reason!
        var cnt = 0
        val booleanArray1 = booleanArrayOf(isPaused, soundMode1, gameStarted)
        val longArray1 = longArrayOf(longScore, countDownCurrentTime)
        bundle1!!.putBooleanArray(
            GAME_FIND_IT_BUNDLE + cnt++,
            booleanArray1
        )
        bundle1.putInt(
            GAME_FIND_IT_BUNDLE + cnt++,
            missesLeft
        )
        bundle1.putLongArray(
            GAME_FIND_IT_BUNDLE + cnt,
            longArray1
        )
        bundle1.putBundle(
            COLOUR_ARRAY_BUNDLE + "0",
            colourArray!!.writeToBundle(Bundle())
        )
        return bundle1
    }

    companion object {
        private const val GAME_FIND_IT_BUNDLE = "GAME_FIND_IT_BUNDLE_"
        private const val COLOUR_ARRAY_BUNDLE = "COLOUR_ARRAY_BUNDLE_"
        private const val ACCELERATION_MINIMUM = 1.0f
        private const val TILT_MOVE_COUNTER_MAXIMUM = 20
        private const val INVALID_DEFAULT_VALUE = -1.0f

        // Need to add 1 second to constants:
        // 1 to display starting total e.g. 101 seconds would update after a second to 100 seconds
        // This results in time added on but only way to get the display correct.
        private const val COUNT_DOWN_TIMER_MAXIMUM = 101000L

        // Add back to counter when user taps on right colour.
        private const val COUNT_DOWN_TIMER_SUCCESS_ADD_BACK = 1000L
    }
}