/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.model

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import com.myapp.R
import com.myapp.controller.GameActivity
import com.myapp.view.MainView

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
class Game : Gaming {
    @Volatile
    private var gameActivity1: GameActivity? = null

    @Volatile
    private var mainView1: MainView? = null

    @Volatile
    private var canvas1: Canvas? = null
    private val soundMode1: Boolean

    @Volatile
    private var highScore1: Long

    @Volatile
    private var findIt: FindIt? = null

    @Volatile
    override var missesLeft : Int = 0
        get() {
            missesLeft = 0
            val gaming1 = gaming
            if (gaming1 != null) {
                missesLeft = gaming1.missesLeft
            }
            return field
        }

    @Volatile
    private var canvasWidth = 0

    @Volatile
    private var canvasHeight = 0

    @Volatile
    private var paintText: Paint? = null

    @Volatile
    private var allowGameEnd = true

    constructor(bundle1: Bundle) : super() {

        // don't use 'counter' as variable name as it increase with each line
        // for some reason!
        var cnt = 0
        soundMode1 = bundle1.getBoolean(GAME_BUNDLE + cnt++)
        missesLeft = bundle1.getInt(GAME_BUNDLE + cnt++)
        highScore1 = bundle1.getLong(GAME_BUNDLE + cnt++)
        val gameBundle = bundle1.getBundle(GAME_BUNDLE + cnt)
        if (gameBundle != null) {
            findIt = FindIt(gameBundle)
        }
    }

    constructor(
        activity: GameActivity?, canvas: Canvas?,
        soundMode: Boolean, highScore: Long
    ) : super() {
        gameActivity1 = activity
        mainView1 = gameActivity1!!.mainView
        canvas1 = canvas
        soundMode1 = soundMode
        highScore1 = highScore
        canvasWidth = canvas1!!.width
        canvasHeight = canvas1!!.height
        doConstructorInitialize(true)
    }

    override fun doConstructorInitialize(firstInstance: Boolean) {
        if (firstInstance) {
            // must create all games here (on main\UI thread)
            // and not in isEndGame() (on background thread)
            findIt = FindIt(
                gameActivity1,
                canvas1, soundMode1
            )
        } else {
            findIt!!.resetGame(gameActivity1, canvas1)
        }
        paintText = Paint()
        paintText!!.textSize = gameActivity1!!.resources.getDimension(R.dimen.font_size)
        paintText!!.color = Color.BLACK
        paintText!!.textAlign = Paint.Align.CENTER
        paintText!!.isFakeBoldText = true
    }

    // called from background thread
    override fun doDraw() {
        val gaming1 = gaming
        if (gaming1 == null) {
            return
        } else {
            gaming1.doDraw()
        }

        // this check needs to be after gaming.doDraw() because that takes time
        // and
        // these variables may be cleared in the meantime
        // synchronizing the method doesn't help here
        if (gameActivity1 == null || mainView1 == null
            || canvas1 == null
        ) {
            return
        }

        if (missesLeft <= 0) {
            if (!isPaused) {
                setPaused()
            }
            if (gameActivity1 != null) {
                gameActivity1!!.setAllowEvents(false)
            }
            val currentScore = longScore
            if (currentScore > highScore1) {
                highScore1 = currentScore
            }

            // game over
            if (missesLeft != 0) {
                // DIFF with Sensors: set background colour so text shows up.
                paintText!!.color = Color.BLACK
                canvas1!!.drawRect(
                    0.0f, 0.0f, canvasWidth.toFloat(), canvasHeight.toFloat(),
                    paintText!!
                )
                paintText!!.color = Color.WHITE

                // missesLeft will be -1 here
                // end game
                canvas1!!.drawText(
                    gameActivity1!!.getString(R.string.game_over),
                    canvasWidth / 2.0f, canvasHeight / 4.0f,
                    paintText!!
                )
                doScoreText(currentScore)
                var str1 = ""
                str1 += (" " + gameActivity1!!.getString(R.string.high_score)
                        + " : " + highScore1)
                canvas1!!.drawText(
                    str1, canvasWidth / 2.0f,
                    canvasHeight * (3.0f / 4.0f), paintText!!
                )
            }
            mainView1!!.drawOnSurfaceHolderCanvas()
            allowGameEnd = true
            try {
                // add a delay to show text on screen
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (allowGameEnd) {
                // game completed OK
                if (missesLeft == 0) {
                    /* Important to set gaming1.missesLeft and not missesLeft because get() sets
                    missesLeft from gaming1.missesLeft.*/
                    // next game
                    gaming1.missesLeft = -2
                    // game over
                } else {
                    /* Important to set gaming1.missesLeft and not missesLeft because get() sets
                    missesLeft from gaming1.missesLeft.*/
                    // end game
                    gaming1.missesLeft = -3
                }
            }
            if (gameActivity1 != null) {
                gameActivity1!!.setAllowEvents(true)
            }
        }
    }

    override fun doMotionSensor(axisX: Float, axisY: Float, controlsMode: Int) {
        val gaming1 = gaming
        gaming1?.doMotionSensor(axisX, axisY, controlsMode)
    }

    private fun doScoreText(currentScore: Long) {
        if (canvas1 == null) {
            return
        }
        canvas1!!.drawText(
            gameActivity1!!.getString(R.string.score)
                    + " : " + currentScore, canvasWidth / 2.0f,
            canvasHeight / 2.0f, paintText!!
        )
    }

    override fun doSingleTap() {
        val gaming1 = gaming
        gaming1?.doSingleTap()
    }

    override fun doSwipe(
        event1: MotionEvent?, event2: MotionEvent?, velocityX: Float,
        velocityY: Float, minFlingVelocity: Int,
        maxFlingVelocity: Int, touchSlop: Int
    ) {
        val gaming1 = gaming
        gaming1?.doSwipe(
            event1, event2, velocityX, velocityY, minFlingVelocity,
            maxFlingVelocity, touchSlop
        )
    }

    // This returns the current game type.  If there was more than one game type then this method
    // would be useful.  The whole point of the Game class is to make the code as generic as
    // possible.
    private val gaming: Gaming?
        get() = findIt

    override var longScore: Long
        get() {
            var longScore = 0L
            val gaming1 = gaming
            if (gaming1 != null) {
                longScore = gaming1.longScore
            }
            return longScore
        }
        set(longScore) {
            val gaming1 = gaming
            if (gaming1 != null) {
                gaming1.longScore = longScore
            }
        }

    override val soundEffect1: SoundEffect?
        get() {
            var soundEffect1: SoundEffect? = null
            val gaming1 = gaming
            if (gaming1 != null) {
                soundEffect1 = gaming1.soundEffect1
            }
            return soundEffect1
        }// restart current game// reset ready for next time

    // default package access
    val isEndGame: Boolean
        get() {
            var endGame = false
            if (missesLeft == -3) {
                endGame = true
            } else if (missesLeft == -2) {
                val gaming1 = gaming
                if (gaming1 != null) {
                    // reset ready for next time
                    gaming1.resetForNextTime()
                    val gaming2 = gaming
                    if (gaming2 != null) {
                        gaming2.longScore = gaming1.longScore
                        gaming2.setSoundEffect1()

                        // restart current game
                        if (isPaused) {
                            setPaused()
                        }
                    }
                }
            }
            return endGame
        }
    override val isPaused: Boolean
        get() {
            var isPaused = false
            val gaming1 = gaming
            if (gaming1 != null) {
                isPaused = gaming1.isPaused
            }
            return isPaused
        }

    override fun releaseResources() {
        if (findIt != null) {
            findIt!!.releaseResources()
            findIt = null
        }
        gameActivity1 = null
        mainView1 = null
        canvas1 = null
        paintText = null
    }

    override fun resetForNextTime() {
        val gaming1 = gaming
        gaming1?.resetForNextTime()
    }

    override fun resetGame(activity: GameActivity?, canvas: Canvas?) {
        gameActivity1 = activity
        mainView1 = gameActivity1!!.mainView
        canvas1 = canvas
        canvasWidth = canvas1!!.width
        canvasHeight = canvas1!!.height
        doConstructorInitialize(false)
    }

    fun setAllowGameEnd(allowGameEnd: Boolean) {
        this.allowGameEnd = allowGameEnd
    }

    override fun setPaused() {
        val gaming1 = gaming
        gaming1?.setPaused()
    }

    override fun setSoundEffect1() {
        val gaming1 = gaming
        gaming1?.setSoundEffect1()
    }

    override fun writeToBundle(bundle1: Bundle?): Bundle {
        // don't use 'counter' as variable name as it increase with each line
        // for some reason!
        var cnt = 0

        // do not use bundle1 - this is null but needed because of Gaming
        // interface definition
        bundle1!!.putBoolean(GAME_BUNDLE + cnt++, soundMode1)
        bundle1.putInt(GAME_BUNDLE + cnt++, missesLeft)
        bundle1.putLong(GAME_BUNDLE + cnt++, highScore1)
        bundle1.putBundle(GAME_BUNDLE + cnt, findIt!!.writeToBundle(Bundle()))
        return bundle1
    }

    companion object {
        private const val GAME_BUNDLE = "GAME_BUNDLE_"
    }
}