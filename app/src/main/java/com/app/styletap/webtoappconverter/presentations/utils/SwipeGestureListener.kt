package com.app.styletap.webtoappconverter.presentations.utils

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeGestureListener(
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        if (e1 == null) return false

        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        if (abs(diffX) > abs(diffY)) {
            if (abs(diffX) > SWIPE_THRESHOLD &&
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
                return true
            }
        }
        return false
    }
}
