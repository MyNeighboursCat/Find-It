/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.myapp.controller

import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.viewbinding.ViewBinding
import com.myapp.R
import com.myapp.databinding.GameBinding
import com.myapp.databinding.GamePausedSwipeOrRotateDialogBinding
import com.myapp.databinding.GamePausedTiltDialogBinding
import com.myapp.model.FrameGenerator
import com.myapp.model.Game
import com.myapp.view.MainView


/**
 * @author Colin Walters
 * @version 1.0, 20/04/2023
 */
class GameActivity : AppCompatActivity() {
    // Make public for handler in FindIt.
    @JvmField
    val tAG: String = this.javaClass.simpleName
    private var allowEvents = false
    private var soundMode = false
    private var initialAxesSaved = false
    private var alertDialogErrorType = 0
    private var controlsMode = 0
    private var touchSlop = 0
    private var minFlingVelocity = 0
    private var maxFlingVelocity = 0
    private var highScore = 0L
    private var initialAxisX = 0.0f
    private var initialAxisY = 0.0f

    @Volatile
    var gameBinding: GameBinding? = null
        private set
    private var gameLinearLayout: LinearLayout? = null
    private var statusLinearLayout: LinearLayout? = null
    private var alertDialog: AlertDialog? = null
    private var recalibrateButton: Button? = null
    private var resumeButton: Button? = null
    private var abandonButton: Button? = null
    private var sensorManager1: SensorManager? = null
    private var gestureDetector: GestureDetectorCompat? = null
    var mainFrameGenerator: FrameGenerator? = null
    var mainView: MainView? = null
        private set
    private var game1: Game? = null

    private inner class MyGestureListener : SimpleOnGestureListener() {
        // Always include onDown with return = true because all gestures start with down and
        // returning false - which is the default - would mean other gestures would not be
        // triggered.
        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            event1: MotionEvent, event2: MotionEvent,
            velocityX: Float, velocityY: Float
        ): Boolean {
            // Swipe mode.
            if (game1 != null && controlsMode == 0) {
                // The origin of the colour grid is top left.
                // Reverse the swipe direction so the x and the y velocity signs match the direction
                // of the displayed segment.
                // This means:
                // Swipe up is positive and down is negative.
                // Swipe left is positive and right is negative.
                game1!!.doSwipe(
                    event1, event2, -velocityX, -velocityY,
                    minFlingVelocity, maxFlingVelocity,
                    touchSlop
                )
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Tap is used in all control modes.
            if (game1 != null) {
                game1!!.doSingleTap()
            }
            return true
        }
    }

    private val sensorEventListener1: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            var error = false
            var axisX = 0.0f
            var axisY = 0.0f

            // From code lab example:
            // You use the clone() method to explicitly make a copy of the data in the values array.
            // The SensorEvent object (and the array of values it contains) is reused across calls
            // to onSensorChanged(). Cloning those values prevents the data you're currently
            // interested in from being changed by more recent data before you're done with it.
            val sensorData = sensorEvent.values.clone()
            when (controlsMode) {
                1 -> {
                    // Tilt: accelerometer.
                    // NOTE: When help system is talking about phone lying flat on table having an
                    // acceleration of 9.81m\s^2 (gravity), it's talking about the z-axis or
                    // sensorData.values[2].
                    // When the top of the phone is pointing away from user (with the screen
                    // pointing upwards) the x-axis runs parallel to the user (right\left plane),
                    // the y-axis runs in the direction the user and the top of the phone is
                    // pointing (bottom\top plane) and the z-axis runs through the phone upwards
                    // (below\above plane).
                    // Scenarios (measurements in m\s^2)
                    // 1) Top of phone pointing away from user (screen pointing upwards):
                    // x-axis: 0
                    // y-axis: 0
                    // z-axis: 9.81
                    // 2) Top of phone pointing upwards (screen pointing towards users):
                    // x-axis: 0
                    // y-axis: 9.81
                    // z-axis: 0
                    // 3) Top of phone pointing towards user (screen pointing down):
                    // x-axis: 0
                    // y-axis: 0
                    // z-axis: -9.81
                    // 4) Top of phone pointing downwards (screen point away from user):
                    // x-axis: 0
                    // y-axis: -9.81
                    // z-axis: 0
                    // 5) Top of phone pointing away from user (screen pointing to the right):
                    // x-axis: -9.81
                    // y-axis: 0
                    // z-axis: 0
                    // 6) Top of phone pointing away from user (screen pointing to the left):
                    // x-axis: 9.81
                    // y-axis: 0
                    // z-axis: 0

                    // Change sign on x-axis.
                    axisX = -sensorData[0]
                    axisY = sensorData[1]

                    // The accelerometer sensor recalibrate button sets initialAxesSaved to false so
                    // leave the initial axes here.  The rotation sensor stores initialAxisXOrigin
                    // in FindIt because starting the game resets the origin to the
                    // current position of the device.
                    if (!initialAxesSaved) {
                        initialAxisX = axisX
                        initialAxisY = axisY
                        initialAxesSaved = true
                    }
                    axisX -= initialAxisX
                    axisY -= initialAxisY
                }
                2 -> {
                    // Rotate.
                    // NOTE: For displaying the degrees for testing, read positionDegrees comment in
                    // ColourArray.
                    //
                    // NOTE: There is an intermittent problem with the TYPE_GAME_ROTATION_VECTOR
                    // sensor on my device.  This also happens with the TYPE_ROTATION_VECTOR sensor.
                    // It keeps adding or subtracting degrees and continues throughout the game. The
                    // data used for the x-axis i.e. sensorEvent.values[0] changes without moving
                    // the device so the problem is with the sensor itself.
                    //
                    // Using the TYPE_GAME_ROTATION_VECTOR sensor.
                    // From the official documentation:
                    // Identical to TYPE_ROTATION_VECTOR except that it doesn't use the geomagnetic
                    // field. Therefore the Y axis doesn't point north, but instead to some other
                    // reference, that reference is allowed to drift by the same order of magnitude
                    // as the gyroscope drift around the Z axis.  In the ideal case, a phone rotated
                    // and returning to the same real-world orientation should report the same game
                    // rotation vector (without using the earth's geomagnetic field). However, the
                    // orientation may drift somewhat over time.
                    //
                    // The drift mentioned above appears to be a few degrees throughout the game so
                    // not much of a problem here.
                    //
                    // From code lab documentation:
                    // A rotation matrix is a linear algebra term that translates the sensor data
                    // from one coordinate system to another — in this case, from the device's
                    // coordinate system to the Earth's coordinate system. That matrix is an array
                    // of nine float values, because each point (on all three axes) is expressed as
                    // a 3D vector.
                    //
                    // The device-coordinate system is a standard 3-axis (x, y, z) coordinate system
                    // relative to the device's screen when it is held in the default or natural
                    // orientation. Most sensors use this coordinate system. In this orientation:
                    // 1) The x-axis is horizontal and points to the right edge of the device.
                    // 2) The y-axis is vertical and points to the top edge of the device.
                    // 3) The z-axis extends up from the surface of the screen. Negative z values
                    //    are behind the screen.
                    //
                    // The Earth's coordinate system is also a 3-axis system, but relative to the
                    // surface of the Earth itself. In the Earth's coordinate system:
                    // 1) The y-axis points to magnetic north along the surface of the Earth.
                    // 2) The x-axis is 90 degrees from y, pointing approximately east.
                    // 3) The z-axis extends up into space. Negative z extends down into the ground.
                    val orientation = FloatArray(3)
                    val inRotationMatrix = FloatArray(9)
                    val outRotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(inRotationMatrix, sensorData)
                    // Remap does the following (swaps the y and z axes):
                    // Device    Earth
                    //      x -> x
                    //      y -> z
                    //      z -> y
                    // Think of the device's axes being lined up with the Earth's i.e. device's
                    // x-axis with Earth's x-axis, device's y-axis with Earth's z-axis and device's
                    // z-axis with Earth's y-axis.
                    //
                    // NOTE: The in rotation matrix should NOT be the same as out rotation matrix.
                    if (SensorManager.remapCoordinateSystem(
                            inRotationMatrix, SensorManager.AXIS_X,
                            SensorManager.AXIS_Z, outRotationMatrix
                        )
                    ) {
                        SensorManager.getOrientation(outRotationMatrix, orientation)

                        // Think of a fixed pole through the centre of the device at right-angle to
                        // the screen.
                        //
                        // NOTE: The following is talking about the magnetic north pole which the
                        // TYPE_ROTATION_VECTOR sensor uses not the TYPE_GAME_ROTATION_VECTOR sensor
                        // which is used here. The TYPE_GAME_ROTATION_VECTOR sensor uses some other
                        // origin.
                        // Also, because of the remap, azimuth now applies to twisting of the device
                        // when it is upright with the screen pointing towards the user and not when
                        // it is flat with the screen point upwards.
                        // From the official documentation (NOTE: This description applies to a
                        // coordinate system which has NOT been remapped.):
                        // values[0]: Azimuth, angle of rotation about the -z axis. This value
                        // represents the angle between the device's y axis and the magnetic north
                        // pole. When facing north, this angle is 0, when facing south, this angle
                        // is π. Likewise, when facing east, this angle is π/2, and when facing
                        // west, this angle is -π/2. The range of values is -π to π.
                        //
                        // angle in degrees = angle in radians*(180/PI)
                        // So range of azimuth in degrees is -180 to 180.
                        //
                        // From the official documentation:
                        // Converts an angle measured in radians to an approximately equivalent
                        // angle measured in degrees. The conversion from radians to degrees is
                        // generally inexact; users should not expect cos(toRadians(90.0)) to
                        // exactly equal 0.0.  Both the parameter and return type is double.
                        //
                        // Appears to be OK to use float argument (and not double) and cast the
                        // returned value from double to float.
                        axisX = Math.toDegrees(orientation[0].toDouble()).toFloat()

                        // Make all negative values 180 to 360 degrees.
                        // OK to use integers here e.g. 0 not 0.0F (without the F, same as using f,
                        // the floating point literal defaults to a double) because Java
                        // automatically converts widening type to float.
                        if (axisX < 0) {
                            axisX += 360f
                        }

                        // Think of a fixed horizontal pole through the device and twist the pole.
                        //
                        // From the official documentation:
                        // values[1]: Pitch, angle of rotation about the x axis. This value
                        // represents the angle between a plane parallel to the device's screen and
                        // a plane parallel to the ground. Assuming that the bottom edge of the
                        // device faces the user and that the screen is face-up, tilting the top
                        // edge of the device toward the ground creates a positive pitch angle. The
                        // range of values is -π to π.
                        // NOTE: The pitch is actually in the range -π/2 to π/2 radians on my
                        // device.  This isn't a result of the remap.  I found an example online
                        // where the same issue occurs.  Therefore there is no way to convert to
                        // 0-360 degrees range like axisX because there are two positive and
                        // negative readings which are the same.  For example, two 45 degrees and
                        // two -45 degrees readings.
                        //
                        // angle in degrees = angle in radians*(180/PI)
                        // So range of pitch in degrees is -180 to 180.
                        // NOTE: The pitch is actually in the range -90 to 90 degrees on my device.
                        // See radians range comment above.
                        //
                        // From the official documentation:
                        // Converts an angle measured in radians to an approximately equivalent
                        // angle measured in degrees. The conversion from radians to degrees is
                        // generally inexact; users should not expect cos(toRadians(90.0)) to
                        // exactly equal 0.0.  Both the parameter and return type is double.
                        //
                        // Appears to be OK to use float argument (and not double) and cast the
                        // returned value from double to float.
                        axisY = Math.toDegrees(orientation[1].toDouble()).toFloat()

                        // orientation[2] is not used here.
                        // Think of a fixed vertical pole through the device and twist the pole.
                        //
                        // From the official documentation (perpendicular means at right-angle):
                        // values[2]: Roll, angle of rotation about the y axis. This value
                        // represents the angle between a plane perpendicular to the device's screen
                        // and a plane perpendicular to the ground. Assuming that the bottom edge of
                        // the device faces the user and that the screen is face-up, tilting the
                        // left edge of the device toward the ground creates a positive roll angle.
                        // The range of values is -π/2 to π/2.
                        //
                        // angle in degrees = angle in radians*(180/PI)
                        // So range of pitch in degrees is -90 to 90.
                    } else {
                        error = true
                    }
                }
                else -> {
                    Log.e(tAG, "onSensorChanged error 1")
                    throw RuntimeException()
                }
            }
            if (game1 != null) {
                if (!game1!!.isPaused && !error) {
                    game1!!.doMotionSensor(
                        axisX, axisY,
                        controlsMode
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Find extras - values passed in
        val extras = this.intent.extras
        if (extras != null) {
            soundMode = extras.getBoolean(
                this
                    .getString(R.string.sound_mode_intent_parameter)
            )
            highScore = extras.getLong(
                this
                    .getString(R.string.high_score_intent_parameter)
            )
            controlsMode = extras.getInt(
                this
                    .getString(R.string.controls_mode_intent_parameter)
            )
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.getBundle(GAME) != null) {
                val gameBundle = savedInstanceState.getBundle(GAME)
                if (gameBundle != null) {
                    game1 = Game(gameBundle)
                }
            }

            var cnt = 0
            val booleanArray1 = savedInstanceState
                .getBooleanArray(
                    GAME_ACTIVITY_BUNDLE
                            + cnt++
                )
            if (booleanArray1 != null) {
                initialAxesSaved = booleanArray1[0]
                allowEvents = booleanArray1[1]
            }
            alertDialogErrorType = savedInstanceState
                .getInt(GAME_ACTIVITY_BUNDLE + cnt++)
            val floatArray1 = savedInstanceState
                .getFloatArray(GAME_ACTIVITY_BUNDLE + cnt)
            if (floatArray1 != null) {
                initialAxisX = floatArray1[0]
                initialAxisY = floatArray1[1]
            }
        }

        // make sure only the music stream volume is adjusted
        this.volumeControlStream = AudioManager.STREAM_MUSIC

        // hide the title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        gameBinding = GameBinding.inflate(this.layoutInflater)
        this.setContentView(gameBinding!!.root)
        statusLinearLayout = gameBinding!!.statusLinearLayout
        gameLinearLayout = gameBinding!!.gameLinearLayout
        gameBinding!!.pauseImageButton.setOnClickListener { doUserRequestedPaused() }

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Hide the status bar.
            window.insetsController?.hide(WindowInsets.Type.statusBars())
            /* Only show the status bar for a short time after swipe down from the top before hiding
            again. */
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    override fun onResume() {
        super.onResume()

        // game has been restored in onCreate so don't set
        // game1 = null
        mainFrameGenerator = null
        mainView = null

        // cannot add MainView to XML code (through custom tab in XML editor)
        // because this uses the context parameter in the constructor
        // whereas MainView needs to receive a GameActivity parameter
        gameLinearLayout!!.removeAllViews()
        mainView = MainView(this)
        gameLinearLayout!!.addView(mainView)
        if (game1 != null) {
            if (game1!!.isPaused && game1!!.missesLeft > 0) {
                doAlertDialog()
            }
        }
    }

    override fun onPause() {
        if (game1 != null) {
            if (!game1!!.isPaused) {
                pauseGame()
            }
            game1!!.setAllowGameEnd(false)
            val soundEffect1 = game1!!.soundEffect1
            soundEffect1?.mediaPlayersRelease()
        }
        doUnregisterSensors()
        if (mainFrameGenerator != null) {
            var retry = true
            mainFrameGenerator!!.done()
            while (retry) {
                try {
                    mainFrameGenerator!!.join()
                    retry = false
                } catch (e: InterruptedException) {
                    // do nothing
                }
            }
            mainFrameGenerator = null
        }
        mainView = null
        if (alertDialog != null) {
            alertDialog!!.dismiss()
            alertDialog = null
        }
        super.onPause()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector != null) {
            gestureDetector!!.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Don't call super - activity will be destroyed
        // super.onBackPressed();
        if (allowEvents) {
            doUserRequestedPaused()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (game1 != null) {
            if (!game1!!.isPaused) {
                pauseGame()
            }
            outState.putBundle(
                GAME,
                game1!!.writeToBundle(Bundle())
            )
        }
        val booleanArray1 = booleanArrayOf(initialAxesSaved, allowEvents)
        val floatArray1 = floatArrayOf(initialAxisX, initialAxisY)

        var cnt = 0
        outState.putBooleanArray(GAME_ACTIVITY_BUNDLE + cnt++, booleanArray1)
        outState.putInt(GAME_ACTIVITY_BUNDLE + cnt++, alertDialogErrorType)
        outState.putFloatArray(GAME_ACTIVITY_BUNDLE + cnt, floatArray1)

        // do this last like book\online code
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        releaseResources()
        super.onDestroy()
    }

    fun startMainFrameGenerator(canvas: Canvas?) {
        // If activity hasn't been restored.
        if (game1 == null) {
            game1 = Game(this, canvas, soundMode, highScore)
        } else {
            game1!!.resetGame(this, canvas!!)
        }
        mainFrameGenerator = FrameGenerator(this, game1)
        mainFrameGenerator!!.start()
        if (game1 != null) {
            if (!game1!!.isPaused && game1!!.missesLeft > 0) {
                doRegisterSensors()
            }
        }
    }

    fun setAllowEvents(allowEvents: Boolean) {
        this.allowEvents = allowEvents
    }

    private fun doUserRequestedPaused() {
        // Pause or back button pressed.
        alertDialogErrorType = 0
        doPausedDialog()
    }

    private fun doPausedDialog() {
        if (game1 == null || !allowEvents) {
            return
        }
        if (!game1!!.isPaused) {
            pauseGame()
        }
        doAlertDialog()
    }

    private fun pauseGame() {
        if (mainFrameGenerator == null || game1 == null) {
            return
        }
        game1!!.setPaused()
    }

    private fun doAlertDialog() {
        val viewBinding: ViewBinding
        doUnregisterSensors()
        if (alertDialog != null) {
            alertDialog!!.dismiss()
            alertDialog = null
        }
        val alertDialogBuilder = AlertDialog.Builder(
            this
        )
        val layout: View
        when (controlsMode) {
            0, 2 -> {
                // Swipe.
                // Rotate
                viewBinding = GamePausedSwipeOrRotateDialogBinding.inflate(
                    this.layoutInflater
                )
                layout = viewBinding.root
            }
            1 -> {
                // Tilt: accelerometer.
                viewBinding = GamePausedTiltDialogBinding.inflate(this.layoutInflater)
                layout = viewBinding.root
            }
            else -> {
                Log.e(tAG, "doAlertDialog error 1")
                throw RuntimeException()
            }
        }
        alertDialogBuilder.setView(layout)

        // Tilt: accelerometer.
        if (controlsMode == 1) {
            if (viewBinding is GamePausedTiltDialogBinding) {
                recalibrateButton = viewBinding.recalibrateButton
            } else {
                Log.e(tAG, "doAlertDialog error 2")
                throw RuntimeException()
            }
            recalibrateButton!!.setOnClickListener {
                if (alertDialog != null) {
                    alertDialog!!.dismiss()
                    alertDialog = null
                }
                initialAxesSaved = false
                doRegisterSensors()
                game1!!.setPaused()
            }
        }
        if (viewBinding is GamePausedSwipeOrRotateDialogBinding) {
            resumeButton = viewBinding.resumeButton
            abandonButton = viewBinding.abandonButton
        } else {
            resumeButton = (viewBinding as GamePausedTiltDialogBinding).resumeButton
            abandonButton = viewBinding.abandonButton
        }
        resumeButton!!.setOnClickListener {
            if (alertDialog != null) {
                alertDialog!!.dismiss()
                alertDialog = null
            }
            doRegisterSensors()
            game1!!.setPaused()
        }
        abandonButton!!.setOnClickListener {
            if (alertDialog != null) {
                alertDialog!!.dismiss()
                alertDialog = null
            }
            finish()
        }
        alertDialogBuilder.setTitle(this.getString(R.string.game_paused))

        // Back button is disabled.
        alertDialogBuilder.setCancelable(false)
        alertDialog = alertDialogBuilder.create()
        alertDialog!!.setOwnerActivity(this)
        alertDialog!!.show()
    }

    private fun doRegisterSensors() {
        // Tap is used for all control modes.
        gestureDetector = GestureDetectorCompat(this, MyGestureListener())
        val viewConfiguration = ViewConfiguration.get(this@GameActivity)
        // The slop tolerance helps differentiate between tap and swipe.
        touchSlop = viewConfiguration.scaledTouchSlop
        minFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        maxFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
        when (controlsMode) {
            0 -> {
            }
            1, 2 -> {
                val sensorType: Int = if (controlsMode == 1) {
                    Sensor.TYPE_ACCELEROMETER
                } else {
                    Sensor.TYPE_GAME_ROTATION_VECTOR
                }
                sensorManager1 = this.getSystemService(SENSOR_SERVICE) as SensorManager
                if (sensorManager1 != null) {
                    // Don't use the TYPE_GRAVITY sensor: recalibrate doesn't work sometimes.
                    val sensor1 = sensorManager1!!.getDefaultSensor(sensorType)
                    if (sensor1 != null) {
                        /* Note: If SENSOR_DELAY_FASTEST were to be used here it would need the
                        * HIGH_SAMPLING_RATE_SENSORS permission in the manifest. */
                        sensorManager1!!.registerListener(
                            sensorEventListener1,
                            sensor1, SensorManager.SENSOR_DELAY_GAME
                        )
                    } else {
                        val toast = Toast.makeText(
                            this.applicationContext,
                            R.string.sensor_is_not_available, Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                } else {
                    val toast = Toast.makeText(
                        this.applicationContext,
                        R.string.sensor_service_is_not_available, Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
            else -> {
                Log.e(tAG, "doRegisterSensors error 1")
                throw RuntimeException()
            }
        }
        setEnableButtons(true)
    }

    private fun doUnregisterSensors() {
        if (gestureDetector != null) {
            gestureDetector = null
        }
        if (sensorManager1 != null) {
            sensorManager1!!.unregisterListener(sensorEventListener1)
            sensorManager1 = null
        }
        setEnableButtons(false)
    }

    private fun setEnableButtons(enableButtons: Boolean) {
        if (!enableButtons) {
            allowEvents = false
        }

        // status layout
        val childMaximum: Int = statusLinearLayout!!.childCount
        for (i in 0 until childMaximum) {
            statusLinearLayout!!.getChildAt(i).isEnabled = enableButtons
        }
        gameLinearLayout!!.isEnabled = enableButtons
        if (enableButtons) {
            allowEvents = true
        }
    }

    private fun releaseResources() {
        if (mainFrameGenerator != null) {
            var retry = true
            mainFrameGenerator!!.done()
            while (retry) {
                try {
                    mainFrameGenerator!!.join()
                    retry = false
                } catch (e: InterruptedException) {
                    // Do nothing.
                }
            }
            mainFrameGenerator = null
        }
        if (game1 != null) {
            game1!!.releaseResources()
            game1 = null
        }
        if (mainView != null) {
            // Let MainView.surfaceDestroyed() call MainView.releaseResources().
            // Called when this.gameLinearLayout.removeAllViews() called.
            mainView = null
        }
        if (gameLinearLayout != null) {
            gameLinearLayout!!.removeAllViews()
            gameLinearLayout = null
        }
        if (alertDialog != null) {
            alertDialog!!.dismiss()
            alertDialog = null
        }
        gameBinding = null
        resumeButton = null
        abandonButton = null
        recalibrateButton = null
        statusLinearLayout = null
        sensorManager1 = null
        gestureDetector = null
    }

    companion object {
        // Don't call this GAME_BUNDLE to distinguish from Game class bundle name.
        private const val GAME = "GAME"
        private const val GAME_ACTIVITY_BUNDLE = "GAME_ACTIVITY_BUNDLE_"
    }
}