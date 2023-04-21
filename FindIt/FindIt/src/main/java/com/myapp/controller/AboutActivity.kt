/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.myapp.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.AudioManager
import android.view.MenuItem
import com.myapp.R
import com.myapp.databinding.TextDisplayBinding

/**
 * @author Colin Walters
 * @version 1.0, 20/04/2023
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure only the music stream volume is adjusted.
        this.volumeControlStream = AudioManager.STREAM_MUSIC
        val textDisplayBinding = TextDisplayBinding.inflate(this.layoutInflater)
        this.setContentView(textDisplayBinding.root)
        setSupportActionBar(textDisplayBinding.toolbar)
        val actionBar = this.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val string1 =
            "${this.getString(R.string.app_name)}\n\n${this.getString(R.string.version)}: ${
                this.getString(R.string.application_version_name)
            }\n\n${this.getString(R.string.copyright)} ${this.getString(R.string.creation_year)} ${
                this.getString(
                    R.string.my_name
                )
            }.  ${this.getString(R.string.all_rights_reserved)}"
        textDisplayBinding.textDisplayTextView.text = string1
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