/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.myapp.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myapp.R
import android.media.AudioManager
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.myapp.databinding.ListDisplayBinding
import java.util.ArrayList

/**
 * @author Colin Walters
 * @version 1.0, 20/04/2023
 */
class HighScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Find extras - values passed in
        val extras = this.intent.extras
        val highScoreStringArray = ArrayList<String>()
        if (extras != null) {
            highScoreStringArray.add(
                this.getString(R.string.score)
                        + ": "
                        + extras.getString(
                    this
                        .getString(R.string.high_score_intent_parameter)
                )
            )
        }

        // Make sure only the music stream volume is adjusted
        this.volumeControlStream = AudioManager.STREAM_MUSIC
        val listDisplayBinding = ListDisplayBinding.inflate(
            this.layoutInflater
        )
        this.setContentView(listDisplayBinding.root)
        setSupportActionBar(listDisplayBinding.toolbar)
        val actionBar = this.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val adapter1 = ArrayAdapter(
            this@HighScoreActivity,
            android.R.layout.simple_list_item_1, highScoreStringArray
        )
        listDisplayBinding.list.adapter = adapter1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Respond to the action bar's Up/Home button.
        if (item.itemId == android.R.id.home) {
            // Return to existing instance of the calling activity rather than create a new one.
            // This keeps the existing state of the calling activity.
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}