/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.myapp.controller.GameActivity

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
class MainView : SurfaceView, SurfaceHolder.Callback {
    private var gameActivity1: GameActivity? = null

    @Volatile
    private var surfaceHolder1: SurfaceHolder? = null

    @Volatile
    private var cache: Bitmap? = null

    @Volatile
    private var paint1: Paint? = Paint()

    constructor(context: Context?) : super(context)
    constructor(context: GameActivity?) : super(context) {
        gameActivity1 = context

        // Minimum dimensions
        this.minimumWidth = 100
        this.minimumHeight = 100
        surfaceHolder1 = this.holder
        surfaceHolder1!!.addCallback(this)
    }

    // called from Game.doDraw() which is on background thread
    // so volatile SurfaceHolder surfaceHolder1
    fun drawOnSurfaceHolderCanvas() {
        if (cache != null && surfaceHolder1 != null) {
            val canvas = surfaceHolder1!!.lockCanvas()
            if (canvas != null && paint1 != null) {
                canvas.drawBitmap(cache!!, 0.0f, 0.0f, paint1)
                // post to main UI thread
                surfaceHolder1!!.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun releaseResources() {
        gameActivity1 = null
        surfaceHolder1 = null
        cache = null
        paint1 = null
    }

    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int,
        width: Int, height: Int
    ) {
        // no need to code for surface dimension change because
        // the screen has fixed orientation
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (cache == null) {
            cache = Bitmap.createBitmap(
                this.measuredWidth,
                this.measuredHeight, Bitmap.Config.ARGB_8888
            )
        }
        gameActivity1!!.startMainFrameGenerator(Canvas(cache!!))
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // activity already destroyed
        // thread stopped by GameActivity.onDestroy()
        if (gameActivity1 == null) {
            return
        }

        // stop the background thread and wait for it to finish so
        // it is not possible to draw to surface after it has been destroyed
        val mainFrameGenerator = gameActivity1!!
            .mainFrameGenerator
        if (mainFrameGenerator != null) {
            var retry = true
            mainFrameGenerator.done()
            while (retry) {
                try {
                    mainFrameGenerator.join()
                    retry = false
                } catch (e: InterruptedException) {
                    // do nothing
                }
            }
            gameActivity1!!.mainFrameGenerator = null
        }
        releaseResources()
    }
}