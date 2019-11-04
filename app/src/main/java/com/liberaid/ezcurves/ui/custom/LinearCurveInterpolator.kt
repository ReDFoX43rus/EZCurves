package com.liberaid.ezcurves.ui.custom

class LinearCurveInterpolator : ICurveInterpolator {

    override fun getY(
        x: Float,
        points: Array<CurveHandler.DraggablePoint>,
        originLeft: Float,
        canvasSize: Float
    ): Float {
        var leftIndex = 0

        for(i in 0 until points.size - 1){
            val currX = points[i].x * canvasSize + originLeft
            val nextX = points[i + 1].x * canvasSize + originLeft

            if(x >= currX && x < nextX){
                leftIndex = i
                break
            }
        }

        val (sx1, sy1) = points[leftIndex]
        val (sx2, sy2) = points[leftIndex + 1]

        val x1 = sx1 * canvasSize + originLeft
        val x2 = sx2 * canvasSize + originLeft

        val y1 = sy1 * canvasSize
        val y2 = sy2 * canvasSize

        return (x - x1) / (x2 - x1) * (y2 - y1) + y1
    }
}