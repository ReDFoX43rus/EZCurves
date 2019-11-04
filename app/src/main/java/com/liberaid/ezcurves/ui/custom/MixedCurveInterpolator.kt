package com.liberaid.ezcurves.ui.custom

class MixedCurveInterpolator(private val firstInterpolator: ICurveInterpolator,
                             private val secondInterpolator: ICurveInterpolator,
                             private val sigma: Float = 0.5f) :
    ICurveInterpolator {

    override fun getY(
        x: Float,
        points: Array<CurveHandler.DraggablePoint>,
        originLeft: Float,
        canvasSize: Float
    ): Float {
        val y1 = firstInterpolator.getY(x, points, originLeft, canvasSize)
        val y2 = secondInterpolator.getY(x, points, originLeft, canvasSize)

        return y1 * sigma + y2 * (1 - sigma)
    }

    companion object {
        fun getPolyLinearInterpolator(sigma: Float) =
            MixedCurveInterpolator(
                PolynomialCurveInterpolator(),
                LinearCurveInterpolator(),
                sigma
            )

        fun getThreePointsPolyLinearInterpolator(sigma: Float) = ThreePointsCurveInterpolator(getPolyLinearInterpolator(sigma))

        fun testInterpolator(): ICurveInterpolator {
            val polynomialInterpolator = PolynomialCurveInterpolator()
            val linearInterpolator = LinearCurveInterpolator()
            val quadraticInterpolator = ThreePointsCurveInterpolator(polynomialInterpolator)

            val mixed1 = MixedCurveInterpolator(polynomialInterpolator, linearInterpolator, 0.25f)
            val mixed2 = MixedCurveInterpolator(mixed1, quadraticInterpolator, 0.8f)

            return mixed2
        }

        fun testInterpolator2(): ICurveInterpolator {
            val polynomialInterpolator = PolynomialCurveInterpolator()
            val linearInterpolator = LinearCurveInterpolator()
            val quadraticInterpolator = ThreePointsCurveInterpolator(polynomialInterpolator)

            val mixed1 = MixedCurveInterpolator(quadraticInterpolator, linearInterpolator, 0.25f)
            val mixed2 = MixedCurveInterpolator(mixed1, polynomialInterpolator, 0.9f)

            return mixed2
        }
    }
}