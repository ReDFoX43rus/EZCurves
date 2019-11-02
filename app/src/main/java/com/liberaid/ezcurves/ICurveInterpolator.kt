package com.liberaid.ezcurves

interface ICurveInterpolator {

    fun getInterpolation(points: Array<CurveHandler.DraggablePoint>, originLeft: Float, canvasSize: Float): (Float) -> Float

}