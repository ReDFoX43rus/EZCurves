package com.liberaid.ezcurves.ui.custom

interface ICurveInterpolator {

    fun getInterpolation(points: Array<CurveHandler.DraggablePoint>, originLeft: Float, canvasSize: Float): (Float) -> Float

}