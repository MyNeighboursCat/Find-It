/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.myapp.model

import android.graphics.Canvas
import android.os.Bundle
import android.view.MotionEvent
import com.myapp.controller.GameActivity

/**
 * @author Colin Walters
 * @version 1.0, 20/04/2023
 */
internal interface Gaming {
    fun doConstructorInitialize(firstInstance: Boolean)
    fun doDraw()
    fun doMotionSensor(axisX: Float, axisY: Float, controlsMode: Int)
    fun doSingleTap()
    fun doSwipe(
        event1: MotionEvent?, event2: MotionEvent?, velocityX: Float,
        velocityY: Float, minFlingVelocity: Int, maxFlingVelocity: Int,
        touchSlop: Int
    )

    var longScore: Long
    var missesLeft: Int
    val soundEffect1: SoundEffect?
    val isPaused: Boolean
    fun releaseResources()
    fun resetForNextTime()
    fun resetGame(activity: GameActivity?, canvas: Canvas?)
    fun setPaused()
    fun setSoundEffect1()
    fun writeToBundle(bundle1: Bundle?): Bundle?
}