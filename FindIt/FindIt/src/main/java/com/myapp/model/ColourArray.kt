/*
 * Copyright (c) 2022 Colin Walters.  All rights reserved.
 */
package com.myapp.model

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import java.util.*

/**
 * @author Colin Walters
 * @version 1.0, 16/11/2022
 */
// default package access
internal class ColourArray {
    private val tag = this.javaClass.simpleName

    // default package access
    // Default value of zero.  Means no colour (int of zero doesn't match any of the colours' ints)
    // will be removed from colour array when determining initial colour to find.
    @Volatile
    var colourToFind: Int
        private set

    @Volatile
    private var selectColor: IntArray? = IntArray(SEGMENTS_NO)

    @Volatile
    private var selectColorArray: IntArray? = IntArray(SEGMENTS_NO)

    // default package access
    @Volatile
    var positionDisplayed: IntArray? = intArrayOf(1, 1)
        private set

    // Only used for testing - see comments for positionDegrees declaration above.
    // default package access
    // Note: positionDegrees are used for testing only.  To display the degrees, go to
    // FindIt, doMessage() and uncomment scoreTextView.setText() code.
    @Volatile
    var positionDegrees: FloatArray? = floatArrayOf(0.0f, 0.0f)
        private set

    @Volatile
    private var rand: Random? = Random()

    // default package access
    constructor() : super() {

        // Color.BLACK is used for background in game over message
        selectColor!![0] = Color.BLUE
        selectColor!![1] = Color.GREEN
        selectColor!![2] = Color.RED
        selectColor!![3] = Color.CYAN
        selectColor!![4] = Color.YELLOW
        selectColor!![5] = Color.MAGENTA
        // teal
        selectColor!![6] = Color.rgb(64, 128, 128)
        // purple
        selectColor!![7] = Color.rgb(142, 60, 145)
        // orange
        selectColor!![8] = Color.rgb(255, 128, 0)
        colourToFind = setColourToFind()
        Arrays.fill(selectColorArray!!, Color.WHITE)
        val colours = ArrayList<Int>()
        for (c in selectColor!!) {
            colours.add(c)
        }
        val positions = ArrayList<Int>()
        for (i in 0 until SEGMENTS_NO) {
            positions.add(i)
        }
        if (colours.size != positions.size) {
            Log.e(tag, "ColourArray() error 1")
            throw RuntimeException()
        }
        val coloursSize = colours.size
        for (i in 0 until coloursSize) {
            val coloursArrayListPosition = rand!!.nextInt(colours.size)
            val positionsArrayListPosition = rand!!.nextInt(positions.size)
            val currentSelectColour = colours[coloursArrayListPosition]
            val currentPosition = positions[positionsArrayListPosition]
            selectColorArray!![currentPosition] = currentSelectColour
            colours.removeAt(coloursArrayListPosition)
            positions.removeAt(positionsArrayListPosition)
        }
    }

    // default package access
    constructor(bundle1: Bundle) {
        var bundleCounter = 0
        colourToFind = bundle1.getInt(COLOUR_ARRAY_BUNDLE + bundleCounter++)
        selectColor = bundle1.getIntArray(COLOUR_ARRAY_BUNDLE + bundleCounter++)
        selectColorArray = bundle1.getIntArray(COLOUR_ARRAY_BUNDLE + bundleCounter++)
        positionDisplayed = bundle1.getIntArray(COLOUR_ARRAY_BUNDLE + bundleCounter++)
        positionDegrees = bundle1.getFloatArray(COLOUR_ARRAY_BUNDLE + bundleCounter)
    }

    // default package access
    fun doMove(axisX: Float, axisY: Float, controlsMode: Int) {
        when (controlsMode) {
            0, 1 ->                 // Swipe
                // Tilt: accelerometer
                doSwipeOrTiltMove(axisX, axisY)
            2 ->                 // Rotate: game rotation vector
                doRotationVectorMove(axisX, axisY)
            else -> {
                Log.e(tag, "doMove error 1")
                throw RuntimeException()
            }
        }
    }

    // For swipe...
    // NOTE: It is important to note that the x and y signs have been reversed in GameActivity,
    // MyGestureListener, onFling() - see comments there - because the current segment moves in the
    // opposite direction to the swipe. For example, swiping right to left moves the current segment
    // to the right.  It does however move the currently selected colour to the left.
    // Swiping acts like physically moving the currently displayed colour in that direction.
    // The direction comments below relate to the movement of the current segment and not the
    // currently selected colour.
    // The sign of axisX and axisY should match the movement direction of the current segment.
    // Positive means increasing the row or column position.
    // Negative means decreasing the row or column position.
    //
    // For tilt...
    // NOTE: It is important to note that the x sign has been reversed in GameActivity,
    // SensorEventListener, onSensorChanged().
    // The sign of axisX and axisY should match the movement direction of the current segment.
    // Positive means increasing the row or column position.
    // Negative means decreasing the row or column position.
    // This means when tilting to...
    // right, axisX is > 0 (column increases)
    // left, axisX is < 0 (column decreases)
    // up, axisY is > 0 (row increases)
    // down, axisY is < 0 (row decreases).
    private fun doSwipeOrTiltMove(axisX: Float, axisY: Float) {
        var newRowPosition = positionDisplayed!![0]
        var newColumnPosition = positionDisplayed!![1]
        when {
            axisX == 0f -> {
                when {
                    axisY == 0f -> {
                        // None.
                        newRowPosition = -1
                        newColumnPosition = -1
                    }
                    axisY > 0 -> {
                        // Down.
                        newRowPosition++
                    }
                    else -> {
                        // Up.
                        newRowPosition--
                    }
                }
            }
            axisX > 0 -> {
                when {
                    axisY == 0f -> {
                        // Right.
                        newColumnPosition++
                    }
                    axisY > 0 -> {
                        // Down right.
                        newRowPosition++
                        newColumnPosition++
                    }
                    else -> {
                        // Up Right.
                        newRowPosition--
                        newColumnPosition++
                    }
                }
            }
            else -> {
                // axisX < 0
                when {
                    axisY == 0f -> {
                        // Left.
                        newColumnPosition--
                    }
                    axisY > 0 -> {
                        // Down left.
                        newRowPosition++
                        newColumnPosition--
                    }
                    else -> {
                        // Up Left.
                        newRowPosition--
                        newColumnPosition--
                    }
                }
            }
        }

        // Keep these two ifs separate to allow movement when on the edge of the grid.
        if (newRowPosition > -1 && newRowPosition < ROW_NO) {
            positionDisplayed!![0] = newRowPosition
        }
        if (newColumnPosition > -1 && newColumnPosition < COLUMN_NO) {
            positionDisplayed!![1] = newColumnPosition
        }
    }

    // At this stage, xDegrees is in the range of 0 to 360 degrees.  The degrees are in
    // converted degrees not the sensor degrees.
    // When xDegrees is 0, the first column starts.  The next column starts at SEGMENT_X_DEGREES
    // and the last column ends at MAXIMUM_X_DEGREES.
    //
    // yDegrees = 0 when the top of phone is upright with the screen facing the user.  The current
    // tile is on the middle row.
    // yDegrees decreases when the top of phone is tilted towards the user until -90 degrees.
    // yDegrees < Y_DEGREES_ABOVE_ROW_START moves the current tile to the higher row.
    // yDegrees increases when the top of phone is tilted away from the user until 90 degrees.
    // yDegrees > Y_DEGREES_BELOW_ROW_START moves the current tile to the lower row.
    // Also keep in mind:
    // yDegrees = 90 when the top of phone is flat pointing away from user (screen is pointing
    // upwards).
    // yDegrees decreases when the top of phone is tilted away from user beyond 90 degrees.
    // yDegrees = 0 when the top of phone is upright with the screen facing away from the user.
    // The readings above are the same but negative when the phone is tilted in the remaining 180
    // degrees.
    private fun doRotationVectorMove(xDegrees: Float, yDegrees: Float) {
        var colourColumn = -1
        var currentSegmentDegrees = 0

        // Only change column if in the range of valid degrees.  Ignore otherwise.
        if (xDegrees in 0.0..MAXIMUM_X_DEGREES.toDouble()) {
            do {
                colourColumn++
                currentSegmentDegrees += SEGMENT_X_DEGREES
            } while (currentSegmentDegrees < xDegrees)
        }

        // Middle row.
        var colourRow = 1
        if (yDegrees < Y_DEGREES_ABOVE_ROW_START) {
            // Top row.
            colourRow = 0
        } else if (yDegrees > Y_DEGREES_BELOW_ROW_START) {
            // Bottom row.
            colourRow = 2
        }
        positionDisplayed!![0] = colourRow
        if (colourColumn > -1) {
            positionDisplayed!![1] = colourColumn
        }
        positionDegrees!![0] = xDegrees
        positionDegrees!![1] = yDegrees
    }

    private fun setColourToFind(): Int {
        val selectColourArrayList = ArrayList<Int>()
        for (aSelectColor in selectColor!!) {
            selectColourArrayList.add(aSelectColor)
        }
        selectColourArrayList.remove(Integer.valueOf(colourToFind))
        return selectColourArrayList[rand!!.nextInt(selectColourArrayList.size)]
    }

    // default package access
    val colourToDisplay: Int
        get() = selectColorArray!![positionDisplayed!![1] +
                positionDisplayed!![0] * COLUMN_NO]

    // default package access
    fun checkColourSelectedMatchesColourToFind(): Boolean {
        val match = colourToDisplay == colourToFind
        if (match) {
            colourToFind = setColourToFind()
        }
        return match
    }

    // default package access
    fun writeToBundle(bundle1: Bundle): Bundle {
        var bundleCounter = 0
        bundle1.putInt(COLOUR_ARRAY_BUNDLE + bundleCounter++, colourToFind)
        bundle1.putIntArray(COLOUR_ARRAY_BUNDLE + bundleCounter++, selectColor)
        bundle1.putIntArray(COLOUR_ARRAY_BUNDLE + bundleCounter++, selectColorArray)
        bundle1.putIntArray(COLOUR_ARRAY_BUNDLE + bundleCounter++, positionDisplayed)
        bundle1.putFloatArray(COLOUR_ARRAY_BUNDLE + bundleCounter, positionDegrees)
        return bundle1
    }

    // default package access
    fun releaseResources() {
        selectColor = null
        selectColorArray = null
        positionDisplayed = null
        positionDegrees = null
        rand = null
    }

    companion object {
        private const val COLOUR_ARRAY_BUNDLE = "COLOUR_ARRAY_BUNDLE_"
        private const val ROW_NO = 3
        private const val COLUMN_NO = 3
        private const val SEGMENT_X_DEGREES = 60
        const val MAXIMUM_X_DEGREES = SEGMENT_X_DEGREES * COLUMN_NO
        private const val SEGMENTS_NO = ROW_NO * COLUMN_NO
        private const val Y_DEGREES_BELOW_ROW_START = 60
        private const val Y_DEGREES_ABOVE_ROW_START = -30
    }
}