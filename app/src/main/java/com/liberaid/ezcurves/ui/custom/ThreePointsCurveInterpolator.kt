package com.liberaid.ezcurves.ui.custom

class ThreePointsCurveInterpolator(private val baseInterpolator: ICurveInterpolator) : ICurveInterpolator {

    private val newPoints = Array(3) { CurveHandler.DraggablePoint(0f, 0f) }

    override fun getY(
        x: Float,
        points: Array<CurveHandler.DraggablePoint>,
        originLeft: Float,
        canvasSize: Float
    ): Float {
        var leftIndex = -1

        for(i in 0 until points.size - newPoints.size + 1){
            val prevX = points[i].x * canvasSize + originLeft
            val nextX = points[i + 1].x * canvasSize + originLeft

            if(x >= prevX && x < nextX){
                leftIndex = i
                break
            }
        }

        if(leftIndex == -1)
            leftIndex = points.size - 3

        /*for(i in newPoints.indices){
            val newPoint = newPoints[i]
            val oldPoint = points[leftIndex + i]

            newPoint.x = oldPoint.x
            newPoint.y = oldPoint.y
        }*/

        val newPoints = Array(3){
            val point = points[leftIndex + it]

            CurveHandler.DraggablePoint(point.x, point.y)
        }

        return baseInterpolator.getY(x, newPoints, originLeft, canvasSize)
    }
}