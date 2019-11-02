package com.liberaid.ezcurves.ui.custom

class PolynomialCurveInterpolator : ICurveInterpolator {

    override fun getInterpolation(points: Array<CurveHandler.DraggablePoint>, originLeft: Float, canvasSize: Float): (Float) -> Float {
        /* Lagrange polynomial construction */
        val basisPolynomials = Array<(Float) -> Float>(points.size) {

            val xi = points[it].x * canvasSize + originLeft

            val result: (Float) -> Float = { x ->

                var result = 1f

                for(i in points.indices){
                    if(i == it)
                        continue

                    val xj = points[i].x * canvasSize + originLeft

                    result *= (x - xj) / (xi - xj)
                }

                result
            }

            result
        }

        val polynomial: (Float) -> Float = { x ->
            var result = 0f

            for(i in points.indices)
                result += points[i].y * canvasSize * basisPolynomials[i](x)

            result
        }

        return polynomial
    }
}