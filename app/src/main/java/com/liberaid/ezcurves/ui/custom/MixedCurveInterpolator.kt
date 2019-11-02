package com.liberaid.ezcurves.ui.custom

class MixedCurveInterpolator(private val firstInterpolator: ICurveInterpolator,
                             private val secondInterpolator: ICurveInterpolator,
                             private val sigma: Float = 0.5f) :
    ICurveInterpolator {

    override fun getInterpolation(
        points: Array<CurveHandler.DraggablePoint>,
        originLeft: Float,
        canvasSize: Float
    ): (Float) -> Float {
        val interpolator1 = firstInterpolator.getInterpolation(points, originLeft, canvasSize)
        val interpolator2 = secondInterpolator.getInterpolation(points, originLeft, canvasSize)

        return { x ->
            interpolator1(x) * sigma + interpolator2(x) * (1 - sigma)
        }
    }

    companion object {
        fun getPolyLinearInterpolator(sigma: Float) =
            MixedCurveInterpolator(
                PolynomialCurveInterpolator(),
                LinearCurveInterpolator(),
                sigma
            )
    }
}