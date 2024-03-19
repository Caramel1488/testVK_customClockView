package vk.test.customclock

import android.content.Context
import android.graphics.*
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Start angle is the top of the clock
private const val START_ANGLE = -Math.PI / 2

// The view is invalidated every *REFRESH_PERIOD* milliseconds
// This number of milliseconds is the maximum number at which sometimes
// uneven jumps of the second hand are imperceptible to the user
private const val REFRESH_PERIOD = 180L

// Default width of the view in dp
private const val DEFAULT_WIDTH_IN_DP = 150

// Default height of the view in dp
private const val DEFAULT_HEIGHT_IN_DP = 150

class CustomAnalogClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Radius of the clock
    private var clockRadius = 0.0f

    // Center of the view by X axis
    private var centerX = 0.0f

    // Center of the view by Y axis
    private var centerY = 0.0f

    // Position variable which will be used to draw elements of the clock
    private val position: PointF = PointF(0.0f, 0.0f)

    // Color of the clock's base
    private var baseColor = 0

    // Color of the hour labels
    private var textColor = 0

    // Color of the border's color
    private var frameColor = 0

    // Color of the dots on the clock
    private var dotsColor = 0

    // Color of the hour hand
    private var hourHandColor = 0

    // Color of the minute hand
    private var minuteHandColor = 0

    // Color of the second hand
    private var secondHandColor = 0

    init {
        // Set the values of colors
        context.withStyledAttributes(attrs, R.styleable.CustomAnalogClockView) {
            baseColor = getColor(
                R.styleable.CustomAnalogClockView_baseColor,
                ContextCompat.getColor(context, R.color.baseColor)
            )
            textColor = getColor(
                R.styleable.CustomAnalogClockView_textColor,
                ContextCompat.getColor(context, R.color.textColor)
            )
            frameColor = getColor(
                R.styleable.CustomAnalogClockView_frameColor,
                ContextCompat.getColor(context, R.color.frameColor)
            )
            dotsColor = getColor(
                R.styleable.CustomAnalogClockView_dotsColor,
                ContextCompat.getColor(context, R.color.dotsColor)
            )
            hourHandColor = getColor(
                R.styleable.CustomAnalogClockView_hourHandColor,
                ContextCompat.getColor(context, R.color.hourHandColor)
            )
            minuteHandColor = getColor(
                R.styleable.CustomAnalogClockView_minuteHandColor,
                ContextCompat.getColor(context, R.color.minuteHandColor)
            )
            secondHandColor = getColor(
                R.styleable.CustomAnalogClockView_secondHandColor,
                ContextCompat.getColor(context, R.color.secondHandColor)
            )
        }
    }

    // Initializing Paint variable here
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textScaleX = 0.9f
        letterSpacing = -0.15f
        typeface = Typeface.DEFAULT
    }

    // Changing the clock's radius and coordinates of the center when the view's size is changed
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        clockRadius = min(width, height) / 2f
        centerX = width / 2f
        centerY = height / 2f
    }

    // Compute coordinates of a minute point on the clock
    private fun PointF.computeXYForPoints(pos: Int, radius: Float) {
        val angle = (pos * (Math.PI / 30)).toFloat()
        x = radius * cos(angle) + centerX
        y = radius * sin(angle) + centerY
    }

    // Compute coordinates of a certain hour label
    private fun PointF.computeXYForHourLabels(hour: Int, radius: Float) {
        val angle = (START_ANGLE + hour * (Math.PI / 6)).toFloat()
        x = radius * cos(angle) + centerX
        // Distance from the baseline to the center needed to adjust text position
        val textBaselineToCenter = (paint.descent() + paint.ascent()) / 2
        y = radius * sin(angle) + centerY - textBaselineToCenter
    }

    // Draw the clock's base
    private fun drawClockBase(canvas: Canvas) {
        paint.color = baseColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, clockRadius, paint)
    }

    // Draw the clock's frame
    private fun drawClockFrame(canvas: Canvas) {
        paint.color = frameColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = clockRadius / 12
        val boundaryRadius = clockRadius - paint.strokeWidth / 2
        canvas.drawCircle(centerX, centerY, boundaryRadius, paint)
        paint.strokeWidth = 0f
    }

    // Draw dots on the clock
    private fun drawDots(canvas: Canvas) {
        paint.color = dotsColor
        paint.style = Paint.Style.FILL
        val dotsDrawLineRadius = clockRadius * 5 / 6
        for (i in 0 until 60) {
            position.computeXYForPoints(i, dotsDrawLineRadius)
            val dotRadius = if (i % 5 == 0) clockRadius / 96 else clockRadius / 128
            canvas.drawCircle(position.x, position.y, dotRadius, paint)
        }
    }

    // Draw the hour labels on the clock
    private fun drawHourLabels(canvas: Canvas) {
        paint.textSize = clockRadius * 2 / 7
        paint.strokeWidth = 0f
        paint.color = textColor
        val labelsDrawLineRadius = clockRadius * 11 / 16
        for (i in 1..12) {
            position.computeXYForHourLabels(i, labelsDrawLineRadius)
            val label = i.toString()
            canvas.drawText(label, position.x, position.y, paint)
        }
    }

    // Draw clock hands
    private fun drawClockHands(canvas: Canvas) {
        val calendar: Calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        // Convert to 12-hour format from 24-hour format
        hour = if (hour > 12) hour - 12 else hour
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        paint.style = Paint.Style.STROKE
        drawHourHand(canvas, hour + minute / 60f)
        drawMinuteHand(canvas, minute)
        drawSecondHand(canvas, second)
    }

    // Draw the hour hand
    private fun drawHourHand(canvas: Canvas, hourWithMinutes: Float) {
        paint.color = hourHandColor
        paint.strokeWidth = clockRadius / 15
        val angle = (Math.PI * hourWithMinutes / 6 + START_ANGLE).toFloat()
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 3 / 14,
            centerY - sin(angle) * clockRadius * 3 / 14,
            centerX + cos(angle) * clockRadius * 7 / 14,
            centerY + sin(angle) * clockRadius * 7 / 14,
            paint
        )
    }

    // Draw the minute hand
    private fun drawMinuteHand(canvas: Canvas, minute: Int) {
        paint.color = minuteHandColor
        paint.strokeWidth = clockRadius / 40
        val angle = (Math.PI * minute / 30 + START_ANGLE).toFloat()
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 2 / 7,
            centerY - sin(angle) * clockRadius * 2 / 7,
            centerX + cos(angle) * clockRadius * 5 / 7,
            centerY + sin(angle) * clockRadius * 5 / 7,
            paint
        )
    }

    // Draw the second hand
    private fun drawSecondHand(canvas: Canvas, second: Int) {
        paint.color = secondHandColor
        val angle = (Math.PI * second / 30 + START_ANGLE).toFloat()
        // Draw the thin part of the hand
        paint.strokeWidth = clockRadius / 80
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 1 / 14,
            centerY - sin(angle) * clockRadius * 1 / 14,
            centerX + cos(angle) * clockRadius * 5 / 7,
            centerY + sin(angle) * clockRadius * 5 / 7,
            paint
        )
        // Draw the broad part of the hand
        paint.strokeWidth = clockRadius / 50
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 2 / 7,
            centerY - sin(angle) * clockRadius * 2 / 7,
            centerX - cos(angle) * clockRadius * 1 / 14,
            centerY - sin(angle) * clockRadius * 1 / 14,
            paint
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the clock's base
        drawClockBase(canvas)
        // Draw the clock's frame
        drawClockFrame(canvas)
        // Draw the dots
        drawDots(canvas)
        // Draw the hour labels
        drawHourLabels(canvas)
        // Draw the clock's hands
        drawClockHands(canvas)
        // Invalidate the view every certain amount of milliseconds
        // so the clock's hands would always show the right time
        // and the second hand would move evenly
        postInvalidateDelayed(REFRESH_PERIOD)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // convert default width in dp to closest amount of px
        val defaultWidth = (DEFAULT_WIDTH_IN_DP * resources.displayMetrics.density).toInt()
        // convert default height in dp to closest amount of px
        val defaultHeight = (DEFAULT_HEIGHT_IN_DP * resources.displayMetrics.density).toInt()

        // Resolve width considering Measure Spec constraints
        val widthToSet = resolveSize(defaultWidth, widthMeasureSpec)
        // Resolve height considering Measure Spec constraints
        val heightToSet = resolveSize(defaultHeight, heightMeasureSpec)

        setMeasuredDimension(widthToSet, heightToSet)
    }

    // Color of the clock's base
    fun setBaseColor(newColor: Int){
        baseColor = newColor
    }
    fun setTextColor(newColor: Int){
        textColor = newColor
    }
    fun setFrameColor(newColor: Int){
        frameColor = newColor
    }
    fun setDotsColor(newColor: Int){
        dotsColor = newColor
    }
    fun setHourHandColor(newColor: Int){
        hourHandColor = newColor
    }
    fun setMinHandColor(newColor: Int){
        minuteHandColor = newColor
    }
    fun setSecHandColor(newColor: Int){
        secondHandColor = newColor
    }

    fun setRadius(newRadius: Int){
        clockRadius = newRadius.toFloat()
    }

    fun getRadius():Float = clockRadius




    // Saving instance state to survive configuration changes
    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        // It makes sense to save colors of the clock's parts in case
        // they are changed at runtime
        bundle.putInt("baseColor", baseColor)
        bundle.putInt("textColor", textColor)
        bundle.putInt("frameColor", frameColor)
        bundle.putInt("dotsColor", dotsColor)
        bundle.putInt("hourHandColor", hourHandColor)
        bundle.putInt("minuteHandColor", minuteHandColor)
        bundle.putInt("secondHandColor", secondHandColor)
        return bundle
    }

    // Restoring instance state
    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            baseColor = state.getInt("baseColor")
            textColor = state.getInt("textColor")
            frameColor = state.getInt("frameColor")
            dotsColor = state.getInt("dotsColor")
            hourHandColor = state.getInt("hourHandColor")
            minuteHandColor = state.getInt("minuteHandColor")
            secondHandColor = state.getInt("secondHandColor")
            superState =
                if (SDK_INT >= 33) state.getParcelable("superState", Parcelable::class.java)
                else @Suppress("DEPRECATION") state.getParcelable("superState")
        }
        super.onRestoreInstanceState(superState)
    }
}