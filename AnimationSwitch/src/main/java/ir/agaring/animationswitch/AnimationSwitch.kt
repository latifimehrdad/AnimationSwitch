package ir.agaring.animationswitch

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Created by m-latifi on 5/27/2023.
 */

class AnimationSwitch(context: Context, attrs: AttributeSet?, style: Int) :
    LinearLayout(context, attrs, style), View.OnClickListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private lateinit var slider: ImageView
    private lateinit var leftBg: ImageView
    private lateinit var rightBg: ImageView
    private lateinit var sliderContainer: FrameLayout
    private var job: Job? = null
    private var position: EnumSwitchPosition = EnumSwitchPosition.LEFT
    private var duration = 300L
    private var cornerRadius: Float = 0f
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var sliderDeltaX: Int = 0
    private var sliderMargin: Int = 0
    private var sliderColor: Int = Color.WHITE
    private var leftDrawableBack: Drawable? = null
    private var rightDrawableBack: Drawable? = null
    private var leftIcon: Drawable? = null
    private var rightIcon: Drawable? = null

    private lateinit var outlineBg: ShapeDrawable
    private lateinit var sliderBg: GradientDrawable
    private var listener: OnSwitchListener? = null


    //---------------------------------------------------------------------------------------------- init
    init {
        initViews(context)
        loadStyleAttrs(context, attrs)
    }
    //---------------------------------------------------------------------------------------------- init


    //---------------------------------------------------------------------------------------------- initViews
    private fun initViews(context: Context) {
        val rootView = LayoutInflater.from(context).inflate(R.layout.day_night_switch, this, false)
        addView(rootView)
        sliderContainer = findViewById(R.id.switch_slider_container)
        slider = findViewById(R.id.switch_slider)
        leftBg = findViewById(R.id.switch_left_bg)
        rightBg = findViewById(R.id.switch_right_bg)
        rootView.setOnClickListener(this)
    }
    //---------------------------------------------------------------------------------------------- initViews


    //---------------------------------------------------------------------------------------------- loadStyleAttrs
    private fun loadStyleAttrs(context: Context, attrs: AttributeSet?) {
        val styleAttrs = context
            .obtainStyledAttributes(attrs, R.styleable.AnimationSwitch, 0, 0)
        leftDrawableBack =
            styleAttrs.getDrawable(R.styleable.AnimationSwitch_meriSwitchLeftBackImage)
            ?: ResourcesCompat.getDrawable(resources, R.drawable.ic_light_back, context.theme)

        rightDrawableBack = styleAttrs.getDrawable(R.styleable.AnimationSwitch_meriSwitchRightBackImage)
            ?: ResourcesCompat.getDrawable(resources, R.drawable.ic_night_back, context.theme)

        leftIcon = styleAttrs.getDrawable(R.styleable.AnimationSwitch_meriSwitchSliderLeftIcon)
            ?: ResourcesCompat.getDrawable(resources, R.drawable.ic_light_slide, context.theme)

        rightIcon = styleAttrs.getDrawable(R.styleable.AnimationSwitch_meriSwitchSliderRightIcon)
            ?: ResourcesCompat.getDrawable(resources, R.drawable.ic_night_slide, context.theme)

        sliderMargin = styleAttrs
            .getDimension(R.styleable.AnimationSwitch_meriSwitchSliderPadding, dpToPx()).toInt()

        cornerRadius = styleAttrs
            .getDimension(R.styleable.AnimationSwitch_meriSwitchCornerRadius, 0f)

        sliderColor = styleAttrs
            .getColor(R.styleable.AnimationSwitch_meriSwitchSliderColor, Color.WHITE)

        duration = styleAttrs
            .getInt(R.styleable.AnimationSwitch_meriSwitchSlideDuration, 300).toLong()

        slider.setImageDrawable(leftIcon)
        styleAttrs.recycle()
    }
    //---------------------------------------------------------------------------------------------- loadStyleAttrs


    //---------------------------------------------------------------------------------------------- onMeasure
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        sliderDeltaX = viewWidth - viewHeight
        setUpDrawablesAndSizes()
        setUpViews()
    }
    //---------------------------------------------------------------------------------------------- onMeasure


    //---------------------------------------------------------------------------------------------- onClick
    override fun onClick(v: View?) {
        position = when (position) {
            EnumSwitchPosition.LEFT -> EnumSwitchPosition.RIGHT
            EnumSwitchPosition.RIGHT -> EnumSwitchPosition.LEFT
        }
        switchSliderWithAnimation(isAnimated = true, callListener = true)
    }
    //---------------------------------------------------------------------------------------------- onClick


    //---------------------------------------------------------------------------------------------- setUpDrawablesAndSizes
    private fun setUpDrawablesAndSizes() {
        if (cornerRadius.compareTo(0) == 0)
            cornerRadius = viewHeight / 2f
        outlineBg = ShapeDrawable(RoundRectShape(FloatArray(8) { cornerRadius }, null, null))
        outlineBg.paint.color = Color.argb(0, 0, 0, 0)
        sliderBg = GradientDrawable()
        sliderBg.shape = GradientDrawable.OVAL
        sliderBg.setColor(sliderColor)
    }
    //---------------------------------------------------------------------------------------------- setUpDrawablesAndSizes


    //---------------------------------------------------------------------------------------------- setUpViews
    private fun setUpViews() {
        leftBg.setImageDrawable(leftDrawableBack)
        rightBg.setImageDrawable(rightDrawableBack)
        leftBg.background = outlineBg
        rightBg.background = outlineBg
        leftBg.clipToOutline = true
        rightBg.clipToOutline = true
        val lp = slider.layoutParams as FrameLayout.LayoutParams
        lp.width = viewHeight - sliderMargin * 2
        lp.height = lp.width
        slider.layoutParams = lp
        sliderContainer.setPadding(sliderMargin)
        setPosition(position = position, isAnimated = false)
    }
    //---------------------------------------------------------------------------------------------- setUpViews


    //---------------------------------------------------------------------------------------------- dpToPx
    private fun dpToPx() =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics)
    //---------------------------------------------------------------------------------------------- dpToPx


    //---------------------------------------------------------------------------------------------- setOnSwitchListener
    fun setOnSwitchListener(listener: (Boolean) -> Unit) {
        this.listener = object : OnSwitchListener {
            override fun onSwitch(isDayChecked: Boolean) {
                job?.cancel()
                job = CoroutineScope(IO).launch {
                    delay(duration + 50)
                    withContext(Main) {
                        listener(isDayChecked)
                    }
                }
            }
        }
    }
    //---------------------------------------------------------------------------------------------- setOnSwitchListener


    //---------------------------------------------------------------------------------------------- switchSliderWithAnimation
    private fun switchSliderWithAnimation(isAnimated: Boolean, callListener: Boolean) {
        val animationDuration = if (isAnimated) duration else 0L
        when (position) {
            EnumSwitchPosition.LEFT -> animationViewToLeft(
                callListener = callListener,
                animationDuration = animationDuration
            )

            EnumSwitchPosition.RIGHT -> animationViewToRight(
                callListener = callListener,
                animationDuration = animationDuration
            )
        }
    }
    //---------------------------------------------------------------------------------------------- switchSliderWithAnimation


    //---------------------------------------------------------------------------------------------- animationViewToLeft
    private fun animationViewToLeft(
        callListener: Boolean,
        animationDuration: Long
    ) {
        slider.animate().translationX(0f).rotation(0f).setDuration(animationDuration).start()
        CoroutineScope(IO).launch {
            delay(animationDuration / 2)
            withContext(Main) {
                slider.setImageDrawable(leftIcon)
            }
        }

        rightBg.animate().alpha(0f).setDuration(animationDuration).start()
        if (callListener)
            listener?.onSwitch(true)
    }
    //---------------------------------------------------------------------------------------------- animationViewToLeft


    //---------------------------------------------------------------------------------------------- animationViewToRight
    private fun animationViewToRight(
        callListener: Boolean,
        animationDuration: Long
    ) {
        slider.animate().translationX(sliderDeltaX.toFloat()).rotation(360f)
            .setDuration(animationDuration).start()

        CoroutineScope(IO).launch {
            delay(animationDuration / 2)
            withContext(Main) {
                slider.setImageDrawable(rightIcon)
            }
        }
        rightBg.animate().alpha(1f).setDuration(animationDuration).start()
        if (callListener)
            listener?.onSwitch(false)
    }
    //---------------------------------------------------------------------------------------------- animationViewToRight


    //---------------------------------------------------------------------------------------------- currentPosition
    fun currentPosition() = position
    //---------------------------------------------------------------------------------------------- currentPosition


    //---------------------------------------------------------------------------------------------- setPosition
    fun setPosition(position: EnumSwitchPosition, isAnimated: Boolean = false) {
        this.position = position
        switchSliderWithAnimation(isAnimated = isAnimated, callListener = false)
    }
    //---------------------------------------------------------------------------------------------- setPosition

}