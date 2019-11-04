package com.liberaid.ezcurves.ui.custom

class PolynomialCurveInterpolator : ICurveInterpolator {

    override fun getY(
        x: Float,
        points: Array<CurveHandler.DraggablePoint>,
        originLeft: Float,
        canvasSize: Float
    ): Float {
        /* Lagrange polynomial construction */
        val basisPolynomialsY = Array<Float>(points.size) {
            val xi = points[it].x * canvasSize + originLeft

            var result = 1f
            for(i in points.indices){
                if(i == it)
                    continue

                val xj = points[i].x * canvasSize + originLeft

                result *= (x - xj) / (xi - xj)
            }

            result
        }

        var polynomialY = 0f
        for(i in points.indices)
            polynomialY += points[i].y * canvasSize * basisPolynomialsY[i]

        return polynomialY
    }
}