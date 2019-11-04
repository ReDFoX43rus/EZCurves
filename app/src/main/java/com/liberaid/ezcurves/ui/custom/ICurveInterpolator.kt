package com.liberaid.ezcurves.ui.custom

interface ICurveInterpolator {

    fun getY(x: Float, points: Array<CurveHandler.DraggablePoint>, originLeft: Float, canvasSize: Float): Float

}