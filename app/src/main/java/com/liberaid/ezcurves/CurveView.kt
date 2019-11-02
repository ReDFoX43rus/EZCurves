package com.liberaid.ezcurves

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import timber.log.Timber
import kotlin.math.min

class CurveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var gridLines = 7

    private val drawRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var borderColor = Color.DKGRAY
    private var borderWidth = 3f
    private var gridColor = Color.GRAY
    private var gridWidth = 1f

    private var canvasPath = Path()

    private val curveInterpolator = CurveInterpolator()
    private val circles = Array(3) { CurveInterpolator.CircleInfo() }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mw = MeasureSpec.getSize(widthMeasureSpec)
        val mh = MeasureSpec.getSize(heightMeasureSpec)

        val minSide = min(mw, mh)

        /* Calculate workspace square */
        val side = minSide.toFloat()
        val (x, y) = if(minSide == mw) {
            0f to (mh / 2f - side / 2)
        } else (mw / 2f - side / 2) to 0f

        drawRect.set(x + paddingStart, y + paddingTop, x + side - paddingEnd, y + side - paddingBottom)

        curveInterpolator.apply {
            oBottom = drawRect.bottom
            oLeft = drawRect.left
            canvasSize = drawRect.width()
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        /* Draw border */
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
        canvas.drawRect(drawRect, paint)

        paint.apply {
            style = Paint.Style.STROKE
            color = gridColor
            strokeWidth = gridWidth
        }

        /* Draw grid */
        val gridSize = drawRect.width() / (gridLines + 1)
        for(i in 0 until gridLines) {

            /* Draw vertical grid */
            val x = (i + 1) * gridSize + drawRect.left
            canvas.drawLine(x, drawRect.top, x, drawRect.bottom, paint)

            /* Draw horizontal drig */
            val y = (i + 1) * gridSize + drawRect.top
            canvas.drawLine(drawRect.left, y, drawRect.right, y, paint)
        }

        /* Draw curve */
        paint.apply {
            color = Color.RED
        }

        /* Draw path */
        curveInterpolator.fillCanvasPath(canvasPath)
        canvas.drawPath(canvasPath, paint)

        /* Draw circles */
        curveInterpolator.fillCircleArray(circles, 2f)
        circles.forEach { (x, y, radius) ->
            canvas.drawCircle(x, y, radius, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event == null) {
            Timber.w("Event is null")
            return super.onTouchEvent(event)
        }

        when(event.action) {
            MotionEvent.ACTION_UP -> { curveInterpolator.releasePoint() }
            MotionEvent.ACTION_MOVE -> {
                if(curveInterpolator.movePoint(event.x, event.y))
                    invalidate()
            }
            MotionEvent.ACTION_DOWN -> { curveInterpolator.grabPoint(event.x, event.y) }
        }

        return true
    }
}