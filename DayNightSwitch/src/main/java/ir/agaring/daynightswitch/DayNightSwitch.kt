package ir.agaring.daynightswitch

import android.content.Context
import android.content.res.Configuration
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
import androidx.core.view.setPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Created by m-latifi on 5/27/2023.
 */

class DayNightSwitch(context: Context, attrs: AttributeSet?, style: Int) :
    LinearLayout(context, attrs, style), View.OnClickListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private lateinit var slider: ImageView
    private lateinit var dayBg: ImageView
    private lateinit var nightBg: ImageView
    private lateinit var sliderContainer: FrameLayout

    private var isDayChecked = false
    private var duration = 300L
    private var cornerRadius: Float = 0f
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var sliderDeltaX: Int = 0
    private var sliderMargin: Int = 0
    private var sliderColor: Int = Color.WHITE

    private lateinit var dayDrawable: Drawable
    private lateinit var nightDrawable: Drawable
    private lateinit var dayIcon: Drawable
    private lateinit var nightIcon: Drawable
    private lateinit var outlineBg: ShapeDrawable
    private lateinit var sliderBg: GradientDrawable
    private var listener: OnSwitchListener? = null


    init {
        initViews(context)
        loadStyleAttrs(context, attrs)
    }

    fun setOnSwitchListener(listener: (Boolean) -> Unit) {
        this.listener = object : OnSwitchListener {
            override fun onSwitch(isDayChecked: Boolean) {
                listener(isDayChecked)
            }
        }
    }

    private fun initViews(context: Context) {
        val rootView = LayoutInflater.from(context).inflate(R.layout.day_night_switch, this, false)
        addView(rootView)

        sliderContainer = findViewById(R.id.day_night_switch_slider_container)
        slider = findViewById(R.id.day_night_switch_slider)
        dayBg = findViewById(R.id.day_night_switch_day_bg)
        nightBg = findViewById(R.id.day_night_switch_night_bg)

        rootView.setOnClickListener(this)
    }

    private fun loadStyleAttrs(context: Context, attrs: AttributeSet?) {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.DayNightSwitch, 0, 0)
        dayDrawable = styleAttrs.getDrawable(R.styleable.DayNightSwitch_meriDayImage)
            ?: resources.getDrawable(R.drawable.day_back)
        nightDrawable = styleAttrs.getDrawable(R.styleable.DayNightSwitch_meriNightImage)
            ?: resources.getDrawable(R.drawable.night_back)
        dayIcon = styleAttrs.getDrawable(R.styleable.DayNightSwitch_meriSliderDayIcon)
            ?: resources.getDrawable(R.drawable.ic_sun)
        nightIcon = styleAttrs.getDrawable(R.styleable.DayNightSwitch_meriSliderNightIcon)
            ?: resources.getDrawable(R.drawable.ic_moon)
        sliderMargin =
            styleAttrs.getDimension(R.styleable.DayNightSwitch_meriSliderPadding, dpToPx()).toInt()
        sliderColor = styleAttrs.getColor(R.styleable.DayNightSwitch_meriSliderColor, Color.WHITE)
        isDayChecked = styleAttrs.getBoolean(R.styleable.DayNightSwitch_meriIsDayChecked, false)
        duration = styleAttrs.getInt(R.styleable.DayNightSwitch_meriSlideDuration, 300).toLong()
        slider.setImageDrawable(dayIcon)
        styleAttrs.recycle()
    }

    private fun dpToPx() =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        sliderDeltaX = viewWidth - viewHeight

        setUpDrawablesAndSizes()
        setUpViews()
    }

    private fun setUpDrawablesAndSizes() {
        cornerRadius = viewHeight / 2f

        outlineBg = ShapeDrawable(RoundRectShape(FloatArray(8) { cornerRadius }, null, null))
        outlineBg.paint.color = Color.argb(0, 0, 0, 0)

        sliderBg = GradientDrawable()
        sliderBg.shape = GradientDrawable.OVAL
        sliderBg.setColor(sliderColor)
    }

    private fun setUpViews() {
        dayBg.setImageDrawable(dayDrawable)
        nightBg.setImageDrawable(nightDrawable)

        dayBg.background = outlineBg
        nightBg.background = outlineBg
//        slider.background = sliderBg //___________________________________________________________

        dayBg.clipToOutline = true
        nightBg.clipToOutline = true

        val lp = slider.layoutParams as FrameLayout.LayoutParams
        lp.width = viewHeight - sliderMargin * 2
        lp.height = lp.width
        slider.layoutParams = lp
        sliderContainer.setPadding(sliderMargin)
        val theme =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != 32
        setDayChecked(isDayChecked = theme, animated = true)
    }

    override fun onClick(v: View?) {
        isDayChecked = !isDayChecked
        switchSliderWithAnimation(animated = true, callListener = true)
    }

    private fun switchSliderWithAnimation(animated: Boolean, callListener: Boolean) {
        val animationDuration = if (animated) duration else 0L
        if (isDayChecked) {
            slider.animate().translationX(0f).rotation(0f).setDuration(animationDuration).start()
            CoroutineScope(IO).launch {
                delay(animationDuration / 2)
                withContext(Main) {
                    slider.setImageDrawable(dayIcon)
                }
            }

            nightBg.animate().alpha(0f).setDuration(animationDuration).start()
            if (callListener)
                listener?.onSwitch(true)
        } else {
            slider.animate().translationX(sliderDeltaX.toFloat()).rotation(360f)
                .setDuration(animationDuration).start()

            CoroutineScope(IO).launch {
                delay(animationDuration / 2)
                withContext(Main) {
                    slider.setImageDrawable(nightIcon)
                }
            }
            nightBg.animate().alpha(1f).setDuration(animationDuration).start()
            if (callListener)
                listener?.onSwitch(false)
        }
    }

    fun isDayChecked() = this.isDayChecked

    fun setDayChecked(isDayChecked: Boolean, animated: Boolean = false) {
        this.isDayChecked = isDayChecked
        switchSliderWithAnimation(animated = animated, callListener = false)
    }
}