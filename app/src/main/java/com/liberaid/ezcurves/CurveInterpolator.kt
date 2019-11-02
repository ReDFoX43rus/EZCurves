package com.liberaid.ezcurves

import android.graphics.Path
import timber.log.Timber

class CurveInterpolator {

    private var isGrabbed = false
    private var grabbedPointIndex = 0
    private var pointRadius = 32f

    private val points = Array(3) {
        when(it){
            0 -> DraggablePoint(0f, 0f)
            1 -> DraggablePoint(0.5f, 0.5f)
            else -> DraggablePoint(1f, 1f)
        }
    }

    fun grabPoint(x: Float, y: Float, bottom: Float, left: Float, size: Float): Boolean {
        if(isGrabbed) {
            Timber.d("Point is already grabbed")
            return false
        }

        val xRange = (x - pointRadius)..(x + pointRadius)
        val yRange = (y - pointRadius)..(y + pointRadius)

        Timber.d("Grab: xRange=$xRange, yRange=$yRange")

        var index = 0
        var found = false

        for(i in 0 until 3){
            val (px, py) = points[i]

            val scaledX = left + px * size
            val scaledY = bottom - py * size

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

        isGrabbed = true
        grabbedPointIndex = index

        Timber.d("Grabbed point number $index")

        return true
    }

    fun movePoint(toX: Float, toY: Float, bottom: Float, left: Float, size: Float): Boolean {
        if(!isGrabbed) {
            Timber.d("Cannot move: there is no grabbed point")
            return false
        }

        val xRange = left..(left + size)
        val yRange = (bottom - size)..bottom

        if(toX !in xRange || toY !in yRange){
            Timber.d("To pos not in range, toX=$toX, toY=$toY, xRange=$xRange, yRange=$yRange")
            return false
        }

        val x = (toX - left) / size
        val y = (bottom - toY) / size

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

    fun fillCircleArray(circles: Array<Triple<Float, Float, Float>>, bottom: Float, left: Float, size: Float){
        if(circles.size < 3)
            return

        for(i in 0 until 3){
            val (x, y) = points[i]

            val scaledX = left + x * size
            val scaledY = bottom - y * size

            circles[i] = Triple(scaledX, scaledY, pointRadius)
        }
    }

    fun fillCanvasPath(path: Path, bottom: Float, left: Float, size: Float) {

        /* Lagrange polynomial construction */
        val basisPolynomials = Array<(Float) -> Float>(3) {

            val xi = points[it].x * size + left

            val result: (Float) -> Float = { x ->

                var result = 1f

                for(i in 0 until 3){
                    if(i == it)
                        continue

                    val xj = points[i].x * size + left

                    result *= (x - xj) / (xi - xj)
                }

                result
            }

            result
        }

        val polynomial: (Float) -> Float = { x ->
            var result = 0f

            for(i in 0 until 3){
                result += points[i].y * size * basisPolynomials[i](x)
            }

            result
        }

        /* Interpolate path */
        path.reset()

        val steps = 256
        val step = size / steps

        path.moveTo(left, bottom)

        for(i in 1 until steps){
            var x = left + i * step
            var y = bottom - polynomial(x)

            if(x < left)
                x = left
            else if(x > left + size)
                x = left + size

            if(y < bottom - size)
                y = bottom - size
            else if(y > bottom)
                y = bottom

            path.lineTo(x, y)
        }
    }

    private class DraggablePoint(var x: Float, var y: Float) {
        override fun toString() = "x=$x, y=$y"

        operator fun component1() = x
        operator fun component2() = y
    }
}