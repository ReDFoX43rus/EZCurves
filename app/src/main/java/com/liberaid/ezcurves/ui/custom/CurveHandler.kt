package com.liberaid.ezcurves.ui.custom

import android.graphics.Path
import timber.log.Timber

class CurveHandler(private val curveInterpolator: ICurveInterpolator) {

    private var isGrabbed = false
    private var grabbedPointIndex = 0
    private var grabXRange = 0f..0f
    
    /* Origin coordinates */
    var oBottom = -1f
    var oLeft = -1f
    
    /* canvasSize of canvas */
    var canvasSize = -1f

    private val points = Array(INIT_POINTS_N) {
        val step = 1f / (INIT_POINTS_N - 1)
        DraggablePoint(it * step, it * step)
    }

    fun grabPoint(x: Float, y: Float): Boolean {
        assertOriginCoordinates()

        if(isGrabbed) {
            Timber.d("Point is already grabbed")
            return false
        }

        val xRange = (x - POINT_RADIUS)..(x + POINT_RADIUS)
        val yRange = (y - POINT_RADIUS)..(y + POINT_RADIUS)

        Timber.d("Grab: xRange=$xRange, yRange=$yRange")

        var index = 0
        var found = false

        for(i in 1 until points.size - 1){
            val (px, py) = points[i]

            val scaledX = oLeft + px * canvasSize
            val scaledY = oBottom - py * canvasSize

            Timber.d("Grab: scaledX=$scaledX, scaledY=$scaledY")

            if(scaledX in xRange && scaledY in yRange){
                index = i
                found = true
                break
            }
        }

        if(!found) {
            Timber.d("Point to grab not found")
            return false
        }

        if(index == 0)
            grabXRange = 0f..points[1].x
        else if(index == points.size - 1)
            grabXRange = points[index - 1].x..1f
        else grabXRange = points[index - 1].x..points[index + 1].x

        isGrabbed = true
        grabbedPointIndex = index

        Timber.d("Grabbed point number $index")

        return true
    }

    fun movePoint(toX: Float, toY: Float): Boolean {
        assertOriginCoordinates()

        if(!isGrabbed) {
            Timber.d("Cannot move: there is no grabbed point")
            return false
        }

        val xRange = oLeft..(oLeft + canvasSize)
        val yRange = (oBottom - canvasSize)..oBottom

        if(toX !in xRange || toY !in yRange){
            Timber.d("To pos not in range, toX=$toX, toY=$toY, xRange=$xRange, yRange=$yRange")
            return false
        }

        val x = (toX - oLeft) / canvasSize
        val y = (oBottom - toY) / canvasSize

        if(x !in grabXRange) {
            return false
        }

        points[grabbedPointIndex].x = x
        points[grabbedPointIndex].y = y

        return true
    }

    fun releasePoint(): Boolean {
        if(!isGrabbed)
            return false

        isGrabbed = false
        return true
    }

    fun fillCircleArray(circles: Array<CircleInfo>, radiusDivider: Float = 1f){
        assertOriginCoordinates()

        if(circles.size < points.size - 2)
            return

        for(i in 1 until points.size - 1){
            val (x, y) = points[i]

            val scaledX = oLeft + x * canvasSize
            val scaledY = oBottom - y * canvasSize

            circles[i - 1].also {
                it.x = scaledX
                it.y = scaledY
                it.radius = POINT_RADIUS / radiusDivider
            }
        }
    }

    fun fillCanvasPath(path: Path) {
        /* Interpolate path */
        path.reset()

        val steps = 256
        val step = canvasSize / steps

        path.moveTo(oLeft, oBottom)

        for(i in 1 until steps){
            var x = oLeft + i * step
            var y = oBottom - getY(x)

            if(x < oLeft)
                x = oLeft
            else if(x > oLeft + canvasSize)
                x = oLeft + canvasSize

            if(y < oBottom - canvasSize)
                y = oBottom - canvasSize
            else if(y > oBottom)
                y = oBottom

            path.lineTo(x, y)
        }
    }

    fun getY(x: Float): Float {
        assertOriginCoordinates()
        return curveInterpolator.getY(x, points, oLeft, canvasSize)
    }

    private fun assertOriginCoordinates() {
        if(oBottom < 0f || oLeft < 0f || canvasSize < 0f)
            throw AssertionError("Origin coordinates were not set oBottom=$oBottom, oLeft=$oLeft, canvasSize=$canvasSize")
    }

    class CircleInfo(var x: Float = 0f, var y: Float = 0f, var radius: Float = 0f) {
        operator fun component1() = x
        operator fun component2() = y
        operator fun component3() = radius
    }

    class DraggablePoint(var x: Float, var y: Float) {
        override fun toString() = "x=$x, y=$y"

        operator fun component1() = x
        operator fun component2() = y
    }
    
    companion object {
        const val INIT_POINTS_N = 5
        const val POINT_RADIUS = 48f
    }
}