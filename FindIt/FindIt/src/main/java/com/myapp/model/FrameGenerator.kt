/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.myapp.model

import android.app.Activity
import android.content.Intent
import com.myapp.R
import com.myapp.controller.GameActivity
import com.myapp.controller.MainActivity

/**
 * @author Colin Walters
 * @version 1.0, 20/04/2023
 */
// use extends Thread and not implements Runnable because
// MainView.surfaceDestroyed
// needs to use Thread.join()
class FrameGenerator(
    @field:Volatile private var gameActivity1: GameActivity?,
    @field:Volatile private var game1: Game?
) : Thread() {
    // volatile variable on two threads
    // ensures threads have access to the same value of the variable
    @Volatile
    private var done = false
    fun done() {
        done = true
    }

    private fun releaseResources() {
        gameActivity1 = null
        game1 = null
    }

    // runs on a different thread
    override fun run() {
        var notStoppedByBack = false
        var endGame: Boolean
        while (!done) {
            game1!!.doDraw()

            // game over
            endGame = game1!!.isEndGame
            if (endGame) {
                done()
                notStoppedByBack = true
            }
        }

        // pressing the back key means onDestroy() of the gameActivity is called
        // setting done = true and the following code would still execute
        // if (done) {
        if (notStoppedByBack) {
            val intent = Intent(
                gameActivity1,
                MainActivity::class.java
            )
            intent.putExtra(
                gameActivity1!!.getString(R.string.high_score_intent_parameter),
                game1!!.longScore
            )
            gameActivity1!!.setResult(Activity.RESULT_OK, intent)
            gameActivity1!!.finish()
        }
        releaseResources()
    }
}