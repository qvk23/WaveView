package gst.trainingcourse.waveviewlibrary

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.sin

class WaveView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs)

    /**
     * +------------------------+
     * | wave length            |__________
     * |   /\          |   /\   |  |
     * |  /  \         |  /  \  | amplitude
     * | /    \        | /    \ |  |
     * |/      \       |/      \|__|_______
     * |        \      /        |  |
     * |         \    /         |  |
     * |          \  /          |  |
     * |           \/           | water level
     * |                        |  |
     * |                        |  |
     * +------------------------+__|_______
     */
    enum class ShapeType {
        CIRCLE, SQUARE
    }


    // Dynamic properties

    // Properties
    private var title: String? = null

    private var amplitudeRatio = DEFAULT_AMPLITUDE_RATIO
    private var waveLengthRatio = DEFAULT_WAVE_LENGTH_RATIO
    private var waterLevelRatio = 0f
    private var waveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO

    private var progressValue = DEFAULT_WATER_LEVEL_RATIO

    private var shapeType = ShapeType.CIRCLE.ordinal

    private var bgWaveColor = DEFAULT_BACKGROUND_WAVE_COLOR
    private var frontWaveColor = DEFAULT_FRONT_WAVE_COLOR

    private var defaultWaterLevel = 0f


    // object to draw
    private var waveShader: BitmapShader? = null
    private var shaderMatrix: Matrix = Matrix()
    private var borderPaint = Paint()
    private var wavePaint: Paint = Paint().apply {
        isAntiAlias = true
    }
    private val titlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    init {
        // Init Wave
        initWaveInfinitely()
        // Load the styled attributes and set their properties
        val attributes =
            getContext().theme.obtainStyledAttributes(attrs, R.styleable.WaveView, 0, 0)
        with(attributes) {
            // init amplitudeRatio
            amplitudeRatio =
                getFloat(R.styleable.WaveView_wv_amplitudeRatio, DEFAULT_AMPLITUDE_RATIO)
            progressValue =
                getFloat(R.styleable.WaveView_wv_waveWaterLevel, DEFAULT_WATER_LEVEL_RATIO)
            updateLevelAnim(progressValue)
            waveLengthRatio =
                getFloat(R.styleable.WaveView_wv_waveLengthRatio, DEFAULT_WAVE_LENGTH_RATIO)
            waveShiftRatio =
                getFloat(R.styleable.WaveView_wv_waveShiftRatio, DEFAULT_WAVE_SHIFT_RATIO)
            // init Wave
            frontWaveColor = getColor(R.styleable.WaveView_wv_frontColor, DEFAULT_FRONT_WAVE_COLOR)
            bgWaveColor =
                getColor(R.styleable.WaveView_wv_backgroundColor, DEFAULT_BACKGROUND_WAVE_COLOR)
            // init Shape
            shapeType = getInt(R.styleable.WaveView_wv_waveShape, 0)
        }

        // Init Center Title
        titlePaint.apply {
            textSize = attributes.getDimension(
                R.styleable.WaveView_wv_textSize,
                sp2px(DEFAULT_TEXT_SIZE)
            )
            color = attributes.getColor(
                R.styleable.WaveView_wv_textColor,
                DEFAULT_TITLE_COLOR
            )
        }
        title = attributes.getString(R.styleable.WaveView_wv_text)

        attributes.recycle()
    }

    private fun initWaveInfinitely() {
//        val animators: MutableList<Animator> = ArrayList()
        // horizontal animation.
        // Wave waves infinitely.
        ObjectAnimator.ofFloat(this, "waveShiftRatio", 0f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 1000
            interpolator = LinearInterpolator()
        }.start()
//        animators.add(waveShiftAnim)
        // amplitude animation.
        // wave grows big then grows small, repeatedly
        ObjectAnimator.ofFloat(
            this, "amplitudeRatio", 0.0001f, 0.05f
        ).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = 5000
            interpolator = LinearInterpolator()
        }.start()
//        animators.add(amplitudeAnim)
//        AnimatorSet().playTogether(animators)
    }

    /**
     * get Text Title
     * @return String?
     */
    fun getTextTitle() = title

    /**
     * Set Text in the middle of WaveView
     * @param value String?
     */
    fun setTextTitle(value: String?) {
        title = value
        invalidate()
    }

    /**
     * Paint.setTextSize(float textSize) default unit is px.
     *
     * @param spValue The real size of text
     * @return int - A transplanted sp
     */
    private fun sp2px(spValue: Float): Float {
        val fontScale: Float = context.resources.displayMetrics.scaledDensity
        return spValue * fontScale + 0.5f
    }

    private fun updateLevelAnim(newValue: Float) {
        ObjectAnimator.ofFloat(
            this,
            "waterLevelRatio",
            waterLevelRatio,
            newValue
        ).apply {
            duration = 3000
            interpolator = DecelerateInterpolator()
        }.start()
//        waterLevelRatio = newValue
    }

    /**
     * Water level increases from 0 to the value of WaveView.
     *
     * @param progress Default to be 0.5
     */
    fun setProgressValue(progress: Float) {
        progressValue = progress
        updateLevelAnim(progress)
    }

    //
    fun getProgressValue(): Float = progressValue

    fun setTextColor(value: Int) {
        titlePaint.color = value
    }

    fun getTextColor() = titlePaint.color

    /**
     * get Shift of the wave
     * @return Float
     */
    fun getWaveShiftRatio() = waveShiftRatio

    /**
     * Shift the wave horizontally according to <code>waveShiftRatio</code>.
     * @param value Float should be 0 ~ 1. Default to be 0
     * waveShiftRatio * width of WaveView = length to shift
     */
    fun setWaveShiftRatio(value: Float) {
        if (waveShiftRatio != value) {
            waveShiftRatio = value
            invalidate()
        }
    }

    /**
     * get water level ratio
     */
    fun getWaterLevelRatio() = waterLevelRatio

    /**
     * Set Water Level
     * @param value Float should be 0 ~ 1. Default to be 0.5
     * Ratio of water level to wave view height
     */
    fun setWaterLevelRatio(value: Float) {
        if (waterLevelRatio != value) {
            waterLevelRatio = value
            invalidate()
        }
    }

    fun getAmplitudeRatio() = amplitudeRatio

    fun setAmplitudeRatio(value: Float) {
        if (amplitudeRatio != value) {
            amplitudeRatio = value
            invalidate()
        }
    }

    fun getWaveLengthRatio() = waveLengthRatio

    /**
     * Set horizontal size of wave according to `waveLengthRatio`
     *
     * @param value Default to be 1.
     * Ratio of wave length to width of WaveView.
     */
    fun setWaveLengthRatio(value: Float) {
        waveLengthRatio = value
        invalidate()
    }

    fun setBorder(width: Int, color: Int) {
        borderPaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            setColor(color)
            strokeWidth = width.toFloat()
        }
        invalidate()
    }

    fun setWaveColor(frontColorValue: Int, bgColorValue: Int) {
        frontWaveColor = frontColorValue
        bgWaveColor = bgColorValue
        if (width > 0 && height > 0) {
            waveShader = null
            updateWaveShader()
            invalidate()
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateWaveShader()
    }

    private fun updateWaveShader() {
        val defaultAngularFrequency = 2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / width
        val defaultAmplitude = height * DEFAULT_AMPLITUDE_RATIO
        defaultWaterLevel = height * DEFAULT_WATER_LEVEL_RATIO
        val defaultWaveLength = width

        val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val mWavePaint = Paint().apply {
            strokeWidth = 2f
            isAntiAlias = true
        }
        // Draw default waves into the bitmap
        // y = A * sin(ωx+φ)+h
        val endX = width + 1;
        val endY = height + 1;

        val waveY = FloatArray(endX)

        wavePaint.color = bgWaveColor
        for (beginX in 0 until endX) {
            val wx: Double = beginX * defaultAngularFrequency
            val beginY = (defaultWaterLevel + defaultAmplitude * sin(wx)).toFloat()
            canvas.drawLine(
                beginX.toFloat(),
                beginY,
                beginX.toFloat(),
                endY.toFloat(),
                mWavePaint
            )
            waveY[beginX] = beginY
        }

        mWavePaint.color = frontWaveColor
        val wave2Shift = defaultWaveLength / 4
        for (beginX in 0 until endX) {
            canvas.drawLine(
                beginX.toFloat(),
                waveY[(beginX + wave2Shift) % endX],
                beginX.toFloat(),
                endY.toFloat(),
                mWavePaint
            )
        }
        waveShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        wavePaint.shader = waveShader
    }

    override fun onDraw(canvas: Canvas?) {
        // Draw Wave.
        // Modify paint shader according to showWave state.
        if (waveShader != null) {
            // first call after showWave, assign it to our paint
            if (wavePaint.shader == null) {
                wavePaint.shader = waveShader;
            }

            shaderMatrix.apply {
                // scale shader according to waveLengthRatio and amplitudeRatio
                // this decides the size(waveLengthRatio for width, amplitudeRatio for height) of waves
                setScale(
                    waveLengthRatio / DEFAULT_WAVE_LENGTH_RATIO,
                    amplitudeRatio / DEFAULT_AMPLITUDE_RATIO,
                    0f,
                    defaultWaterLevel
                )
                // translate shader according to waveShiftRatio and waterLevelRatio
                // this decides the start position(waveShiftRatio for x, waterLevelRatio for y) of waves
                postTranslate(
                    waveShiftRatio * width,
                    (DEFAULT_WATER_LEVEL_RATIO - waterLevelRatio) * height
                )
            }
            // assign matrix to invalidate the shader
            waveShader!!.setLocalMatrix(shaderMatrix)

            val borderWidth = borderPaint.strokeWidth

            when (shapeType) {
                ShapeType.CIRCLE.ordinal -> {
                    if (borderWidth > 0) {
                        canvas?.drawCircle(
                            width / 2f, height / 2f,
                            (width - borderWidth) / 2f - 1f, borderPaint
                        )
                    }
                    val radius = width / 2f - borderWidth
                    canvas?.drawCircle(width / 2f, height / 2f, radius, wavePaint)
                }

                ShapeType.SQUARE.ordinal -> {
                    if (borderWidth > 0) {
                        canvas?.drawRect(
                            borderWidth / 2f,
                            borderWidth / 2f,
                            width - borderWidth / 2f - 0.5f,
                            height - borderWidth / 2f - 0.5f,
                            borderPaint
                        );
                    }
                    canvas?.drawRect(
                        borderWidth, borderWidth, width - borderWidth,
                        height - borderWidth, wavePaint
                    )
                }

            }
            title?.let {
                val middle = titlePaint.measureText(title)
                canvas?.drawText(
                    it,
                    (width - middle) / 2,
                    height / 2 - (titlePaint.descent() + titlePaint.ascent()) / 2,
                    titlePaint
                )
            }
        } else {
            wavePaint.shader = null
        }

    }

    companion object {
        private const val DEFAULT_AMPLITUDE_RATIO = 0.05f

        private const val DEFAULT_WATER_LEVEL_RATIO = 0.5f
        private const val DEFAULT_WAVE_LENGTH_RATIO = 1.0f
        private const val DEFAULT_WAVE_SHIFT_RATIO = 0.0f

        private const val DEFAULT_TEXT_SIZE = 22.0f

        private val DEFAULT_TITLE_COLOR = Color.parseColor("#212121")
        private val DEFAULT_BACKGROUND_WAVE_COLOR: Int = Color.parseColor("#28FFFFFF")
        private val DEFAULT_FRONT_WAVE_COLOR: Int = Color.parseColor("#3CFFFFFF")
    }
}