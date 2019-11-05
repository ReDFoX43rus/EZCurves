package com.liberaid.ezcurves.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.liberaid.ezcurves.R
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

class CurveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var curveChangedListener: ICurveChangedListener? = null

    private val drawRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var canvasPath = Path()

    private val curveHandler = CurveHandler(MixedCurveInterpolator.testInterpolator2())
    private val circles = Array(CurveHandler.INIT_POINTS_N - 2) { CurveHandler.CircleInfo() }

    init {
        context.theme.obtainStyledAttributes(attrs,
            R.styleable.CurveView, 0, 0).apply {
            try {
                gridLines = getInteger(R.styleable.CurveView_gridLines, 7)
                gridColor = getColor(
                    R.styleable.CurveView_gridColor,
                    DEFAULT_GRID_COLOR
                )
                gridWidth = getDimension(R.styleable.CurveView_gridWidth, 1f)
                borderColor = getColor(
                    R.styleable.CurveView_borderColor,
                    DEFAULT_BORDER_COLOR
                )
                borderWidth = getDimension(R.styleable.CurveView_borderWidth, 3f)
                bgColor = getColor(
                    R.styleable.CurveView_bgColor,
                    DEFAULT_BG_COLOR
                )
                circleRadius = getDimension(R.styleable.CurveView_circleRadius, 3f)
                circleRadiusDivider = getInteger(R.styleable.CurveView_circleRadiusDivider, 2)
                circleColor = getColor(
                    R.styleable.CurveView_circleColor,
                    DEFAULT_CIRCLE_COLOR
                )
                curveColor = getColor(
                    R.styleable.CurveView_curveColor,
                    DEFAULT_CURVE_COLOR
                )
                curveWidth = getDimension(R.styleable.CurveView_curveWidth, 1f)
            } finally {
                recycle()
            }
        }
    }

    fun fillCurve(curve: ByteArray) {
        if(curve.size < 256)
            return

        val step = drawRect.width() / 256
        for(i in 0 until 256){
            val x = drawRect.left + i * step
            var y = curveHandler.getY(x) / drawRect.width() * 255

            if(y < 0f)
                y = 0f
            else if (y > 255f)
                y = 255f

            curve[i] = y.toByte()
        }
    }

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

        curveHandler.apply {
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
            color = curveColor
        }

        /* Draw path */
        curveHandler.fillCanvasPath(canvasPath)
        canvas.drawPath(canvasPath, paint)

        paint.apply {
            color = circleColor
            style = Paint.Style.FILL
        }

        /* Draw circles */
        curveHandler.fillCircleArray(circles, circleRadiusDivider.toFloat())
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
            MotionEvent.ACTION_UP -> { curveHandler.releasePoint() }
            MotionEvent.ACTION_MOVE -> {
                if(curveHandler.movePoint(event.x, event.y)) {
                    invalidate()
                    curveChangedListener?.onCurveChanged()
                }
            }
            MotionEvent.ACTION_DOWN -> { curveHandler.grabPoint(event.x, event.y) }
        }

        return true
    }

    fun getState() = curveHandler.getState()
    fun setState(state: List<Pair<Float, Float>>) = curveHandler.setState(state)

    interface ICurveChangedListener {
        fun onCurveChanged()
    }

    companion object {
        private const val DEFAULT_GRID_COLOR = Color.GRAY
        private const val DEFAULT_BORDER_COLOR = Color.DKGRAY
        private const val DEFAULT_BG_COLOR = Color.TRANSPARENT
        private const val DEFAULT_CIRCLE_COLOR = Color.RED
        private const val DEFAULT_CURVE_COLOR = Color.RED
    }

    var gridLines = 7
    set(value) {
        field = value
        invalidate()
    }

    var gridColor = 0
    set(value) {
        field = value
        invalidate()
    }

    var gridWidth = 0f
    set(value) {
        field = max(value, 0f) * resources.displayMetrics.density
        invalidate()
    }
    get() = field / resources.displayMetrics.density

    var borderColor = 0
    set(value) {
        field = value
        invalidate()
    }

    var borderWidth = 0f
    set(value) {
        field = max(value, 0f) * resources.displayMetrics.density
        invalidate()
    }
    get() = field / resources.displayMetrics.density

    var bgColor = 0
    set(value) {
        field = value
        invalidate()
    }

    var circleRadius = 0f
    set(value) {
        field = max(value, 0f) * resources.displayMetrics.density
        invalidate()
    }
    get() = field / resources.displayMetrics.density

    var circleRadiusDivider = 2
    set(value){
        if(value > 0) {
            field = value
            invalidate()
        }
    }

    var circleColor = 0
    set(value){
        field = value
        invalidate()
    }

    var curveColor = 0
    set(value) {
        field = value
        invalidate()
    }

    var curveWidth = 0f
    set(value) {
        field = max(value, 0f) * resources.displayMetrics.density
        invalidate()
    }
    get() = field / resources.displayMetrics.density
}