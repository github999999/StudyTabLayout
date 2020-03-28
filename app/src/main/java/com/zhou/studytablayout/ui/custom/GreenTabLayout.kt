package com.zhou.studytablayout.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx
import com.zhou.studytablayout.util.getFontTypeFace
import com.zhou.studytablayout.util.sp2px
import kotlin.math.roundToInt

/**
 * 绿色版
 *
 * @author Hank.Zhou
 *
 */
class GreenTabLayout : HorizontalScrollView, ViewPager.OnPageChangeListener {
    constructor(ctx: Context) : super(ctx) {
        init(null, 0)
    }

    constructor(ctx: Context, attributes: AttributeSet) : super(ctx, attributes) {
        init(attributes, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    // 自定义属性相关

    // TabView相关属性
    class TabViewAttrs {
        var tabViewTextSize: Float = 0f // 字体大小，单位sp
        var tabViewTextSizeSelected: Float = 0f // 选中的字体大小
        var tabViewTextColor: Int = 0 // 字体颜色
        var tabViewBackgroundColor: Int = 0 // 背景色
        var tabViewTextTypeface: Typeface? = null // 字体 指定ttf文件   /// TODO 这个无效，原因不明，不过优先级放低，后面看
        var tabViewTextColorSelected: Int = 0 // 选中之后的字体颜色
        var tabViewTextPaddingLeft: Float = 0f
        var tabViewTextPaddingRight: Float = 0f
        var tabViewTextPaddingTop: Float = 0f
        var tabViewTextPaddingBottom: Float = 0f
    }

    class IndicatorAttrs {

        var indicatorColor: Int = 0
        /**
         * 支持 Gravity.BOTTOM 和 Gravity.TOP
         * 设置其他属性，则默认为Gravity.BOTTOM
         */
        var indicatorLocationGravity: LocationGravity = LocationGravity.BOTTOM

        enum class LocationGravity(v: Int) {
            TOP(0),
            BOTTOM(1)
        }

        /**
         * 长度模式，TabView长度的倍数，或者 定死长度，还是取 TextView长度的倍数
         */
        var indicatorWidthMode: WidthMode? = null

        enum class WidthMode(v: Int) {
            RELATIVE_TAB_VIEW(0),// 取相对于TabView的长度的百分比(没有哪个傻缺会超过1，对吧?我就不做限制了)
            EXACT(1)// 指定长度精确值
        }

        /**
         * indicator的长度是TabView百分比长度，
         * 如果值是1，那就是等长indicatorExactWidth
         */
        var indicatorWidthPercentages: Float = 0.5f

        /**
         * 精确长度，只有在width模式为EXACT的时候有效, 单位dp
         */
        var indicatorExactWidth: Float = 0f

        var indicatorHeight: Float = 0f // 高度，单位dp

        /**
         * 对齐模式
         */
        var indicatorAlignMode: AlignMode? = AlignMode.CENTER //

        enum class AlignMode(v: Int) {
            LEFT(0), // 靠左
            CENTER(1),// 居中
            RIGHT(2) // 靠右
        }

        var indicatorMargin: Float = 0f // 根据 locationGravity 决定，如果是放在底部，就是与底部的距离

        var indicatorDrawable: Drawable? = null // 默认drawable

        /**
         *  indicator的弹性效果
         */
        var indicatorElastic: Boolean = false
        /**
         *  拉伸的基础倍数，倍数越大，拉伸效果越明显
         */
        var indicatorElasticBaseMultiple = 1f //
    }

    private lateinit var indicatorLayout: SlidingIndicatorLayout
    lateinit var mViewPager: ViewPager

    var tabViewAttrs: TabViewAttrs = TabViewAttrs()
    var indicatorAttrs: IndicatorAttrs = IndicatorAttrs()


    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        isHorizontalScrollBarEnabled = false  // 禁用滚动横条
        overScrollMode = View.OVER_SCROLL_NEVER // 禁用按下的水波效果

        indicatorLayout = SlidingIndicatorLayout(context, this)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(indicatorLayout, layoutParams)


        dealAttributeSet(attrs = attrs)
    }

    private fun dealAttributeSet(attrs: AttributeSet?) {
        if (attrs == null) return
        var a: TypedArray? = null
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.GreenTabLayout)
            tabViewAttrs.run {
                tabViewTextTypeface = getFontTypeFace(context)

                tabViewTextSize =
                    a.getDimension(
                        R.styleable.GreenTabLayout_tabViewTextSize,
                        sp2px(context, 12f).toFloat()
                    )
                tabViewTextSizeSelected =
                    a.getDimension(
                        R.styleable.GreenTabLayout_tabViewTextSizeSelected,
                        sp2px(context, 15f).toFloat()
                    )
                tabViewTextColor = a.getColor(
                    R.styleable.GreenTabLayout_tabViewTextColor,
                    resources.getColor(R.color.c1)
                )
                tabViewTextColorSelected = a.getColor(
                    R.styleable.GreenTabLayout_tabViewTextColorSelected,
                    resources.getColor(R.color.c3)
                )

                tabViewBackgroundColor = a.getColor(
                    R.styleable.GreenTabLayout_tabViewBackgroundColor,
                    resources.getColor(R.color.c_11)
                )

                tabViewTextPaddingLeft =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingLeft, 5f)
                tabViewTextPaddingRight =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingRight, 5f)
                tabViewTextPaddingTop =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingTop, 5f)
                tabViewTextPaddingBottom =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingBottom, 5f)
            }


            indicatorAttrs.run {
                indicatorColor = a.getColor(
                    R.styleable.GreenTabLayout_indicatorColor,
                    resources.getColor(R.color.c3)
                )
                indicatorMargin = a.getDimension(R.styleable.GreenTabLayout_indicatorMargin, 0f)
                indicatorDrawable = a.getDrawable(R.styleable.GreenTabLayout_indicatorDrawable)
                indicatorHeight =
                    a.getDimension(
                        R.styleable.GreenTabLayout_indicatorHeight,
                        dpToPx(context, 4f).toFloat()
                    )
                indicatorWidthPercentages =
                    a.getFloat(R.styleable.GreenTabLayout_indicatorWidthPercentages, 1f)
                indicatorExactWidth =
                    a.getDimension(
                        R.styleable.GreenTabLayout_indicatorExactWidth,
                        dpToPx(context, 20f).toFloat()
                    )

                // 处理枚举 LocationGravity
                val locationGravity = a.getInteger(
                    R.styleable.GreenTabLayout_indicatorLocationGravity,
                    IndicatorAttrs.LocationGravity.BOTTOM.ordinal
                )
                indicatorLocationGravity =
                    when (locationGravity) {
                        IndicatorAttrs.LocationGravity.TOP.ordinal -> {
                            IndicatorAttrs.LocationGravity.TOP
                        }
                        else -> {
                            IndicatorAttrs.LocationGravity.BOTTOM
                        }
                    }

                // 处理枚举 widthModeW
                val widthMode = a.getInteger(
                    R.styleable.GreenTabLayout_indicatorWidthMode,
                    IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW.ordinal
                )
                indicatorWidthMode =
                    when (widthMode) {
                        IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW.ordinal -> {
                            IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW
                        }
                        else -> {
                            IndicatorAttrs.WidthMode.EXACT
                        }
                    }

                // 处理枚举 AlignMode
                val alignMode = a.getInteger(
                    R.styleable.GreenTabLayout_indicatorAlignMode,
                    IndicatorAttrs.AlignMode.CENTER.ordinal
                )
                indicatorAlignMode = when (alignMode) {
                    IndicatorAttrs.AlignMode.LEFT.ordinal -> {
                        IndicatorAttrs.AlignMode.LEFT
                    }
                    IndicatorAttrs.AlignMode.CENTER.ordinal -> {
                        IndicatorAttrs.AlignMode.CENTER
                    }
                    else -> {
                        IndicatorAttrs.AlignMode.RIGHT
                    }
                }

                indicatorElastic = a.getBoolean(R.styleable.GreenTabLayout_indicatorElastic, true)
                indicatorElasticBaseMultiple =
                    a.getFloat(R.styleable.GreenTabLayout_indicatorElasticBaseMultiple, 1f)
            }
        } finally {
            a?.recycle()
        }

    }

    private fun addTabView(text: String) {
        indicatorLayout.addTabView(text)
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        this.mViewPager = viewPager
        viewPager.addOnPageChangeListener(this)
        val adapter = viewPager.adapter ?: return
        val count = adapter!!.count // 栏目数量
        for (i in 0 until count) {
            val pageTitle = adapter.getPageTitle(i)
            addTabView(pageTitle.toString())
        }
    }

    /**
     * 这段代码值得研究，无论左右，都是position+1即可
     *
     * TODO 一定要研究
     */
    fun scrollTabLayout(position: Int, positionOffset: Float) {
        // 如果是向左, 就用当前的tabView滑动到左边一个tabView
        val currentTabView = indicatorLayout.getChildAt(position) as GreenTabView
        val currentLeft = currentTabView.left
        val currentRight = currentTabView.right

        val nextTabView = indicatorLayout.getChildAt(position + 1) // 目标TabView
        if (nextTabView != null) {

            if (positionOffset != 0f) {
                // 在这里，让当前字体变小，next的字体变大
                val diffSize = tabViewAttrs.tabViewTextSizeSelected - tabViewAttrs.tabViewTextSize
                currentTabView.titleTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tabViewAttrs.tabViewTextSizeSelected - diffSize * positionOffset
                )
                (nextTabView as GreenTabView).titleTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tabViewAttrs.tabViewTextSize + diffSize * positionOffset
                )

                Log.d(
                    "setTextSizeTag",
                    "nextTabView : ${tabViewAttrs.tabViewTextSize + diffSize * positionOffset}"
                )
                Log.d("positionOffsetTag", "$positionOffset")
            }


            val nextLeft = nextTabView.left
            val nextRight = nextTabView.right

            val leftDiff = nextLeft - currentLeft
            val rightDiff = nextRight - currentRight

            indicatorLayout.updateIndicatorPosition(
                currentLeft + (leftDiff * positionOffset).toInt(),
                currentRight + (rightDiff * positionOffset).toInt(),
                positionOffset
            )
        }
    }


    private var mCurrentPositionOffset = 0f

    /**
     * 判断滑动的方向
     */
    private fun judgeScrollDirection(positionOffset: Float) {
        when {
            positionOffset == mCurrentPositionOffset -> {
                //不作处理
            }
            positionOffset > mCurrentPositionOffset -> {
                //从左向右滑
                mCurrentPositionOffset = positionOffset

//                if (mScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
                Log.d(
                    "judgeScrollDirection",
                    "--------->>> $mCurrentPosition ${mCurrentPosition + 1}  positionOffset:$positionOffset"
                )
//                }
            }
            positionOffset < mCurrentPositionOffset -> {
                mCurrentPositionOffset = positionOffset
//                if (mScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
                Log.d(
                    "judgeScrollDirection",
                    "<<<--------- $mCurrentPosition ${mCurrentPosition - 1}   positionOffset:$positionOffset"
                )
//                }
            }
        }
    }

    private var mScrollState: Int = ViewPager.SCROLL_STATE_IDLE
    /**
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    override fun onPageScrollStateChanged(state: Int) {
        mScrollState = state
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        Log.d("positionOffset", "$positionOffset")
        judgeScrollDirection(positionOffset)
        scrollTabLayout(position, positionOffset)
    }

    var mCurrentPosition = 0
    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        val tabView = indicatorLayout.getChildAt(position) as GreenTabView
        if (tabView != null) {
            //也许这里不应该再去更新indicator的位置，而是应该直接滚动最外层布局
            indicatorLayout.updateIndicatorPositionByAnimator(tabView)
        }
    }
}

/**
 * 中间层 可滚动的 线性布局
 */
class SlidingIndicatorLayout : LinearLayout {

    private var indicatorLeft = 0
    private var indicatorRight = 0
    private var positionOffset = 0f
    var parent: GreenTabLayout
    private var inited: Boolean = false
    private var scrollAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val tabViewBounds = Rect()
    private val parentBounds = Rect()

    constructor(ctx: Context, parent: GreenTabLayout) : super(ctx) {
        init()
        this.parent = parent
    }

    private fun init() {
        setWillNotDraw(false) // 如果不这么做，它自身的draw方法就不会调用
        gravity = Gravity.CENTER_VERTICAL
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        Log.d("SlidingIndicatorLayout", "onLayout")
        if (!inited)
            parent.scrollTabLayout(0, 0f)//
    }

    /**
     * 作为一个viewGroup，有可能它不会执行自身的draw方法，这里有一个值去控制  setWillNotDraw
     */
    override fun draw(canvas: Canvas?) {
        var top: Int
        var bottom: Int
        var margin: Int = parent.indicatorAttrs.indicatorMargin.roundToInt()
        var indicatorHeight: Int = parent.indicatorAttrs.indicatorHeight.roundToInt()

        // 处理属性 indicatorAttrs.locationGravity --> indicator的Gravity
        when (parent.indicatorAttrs.indicatorLocationGravity) {
            GreenTabLayout.IndicatorAttrs.LocationGravity.BOTTOM -> {
                top = height - indicatorHeight - margin
                bottom = height - margin
            }
            GreenTabLayout.IndicatorAttrs.LocationGravity.TOP -> {
                top = 0 + margin
                bottom = indicatorHeight + margin
            }
        }


        var selectedIndicator: Drawable
        if (null != parent.indicatorAttrs.indicatorDrawable) {// 如果drawable是空
            selectedIndicator = parent.indicatorAttrs.indicatorDrawable!!
        } else { // 那就涂颜色
            selectedIndicator = GradientDrawable()
            DrawableCompat.setTint(
                selectedIndicator,
                parent.indicatorAttrs.indicatorColor
            )// 规定它的颜色
        }

        val tabViewWidth = indicatorRight - indicatorLeft
        var indicatorWidth = 0f

        // 处理属性 widthMode
        when (parent.indicatorAttrs.indicatorWidthMode) {
            GreenTabLayout.IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW -> {
                indicatorWidth =
                    ((indicatorRight - indicatorLeft) * parent.indicatorAttrs.indicatorWidthPercentages)
            }
            GreenTabLayout.IndicatorAttrs.WidthMode.EXACT -> {
                indicatorWidth = parent.indicatorAttrs.indicatorExactWidth
            }
        }

        val dif = tabViewWidth - indicatorWidth
        var centerX = 0
        // 处理属性 alignMode
        when (parent.indicatorAttrs.indicatorAlignMode) {
            GreenTabLayout.IndicatorAttrs.AlignMode.LEFT -> {
                centerX = (((indicatorLeft + indicatorRight - dif) / 2).toInt())
            }
            GreenTabLayout.IndicatorAttrs.AlignMode.CENTER -> {
                centerX =
                    ((indicatorLeft + indicatorRight) / 2) // 这个就是中心位置
            }
            GreenTabLayout.IndicatorAttrs.AlignMode.RIGHT -> {
                centerX = (((indicatorLeft + indicatorRight + dif) / 2).toInt())
            }
        }

        // 是否开启 indicator的弹性拉伸效果
        // 计算临界值
        val baseMultiple = parent.indicatorAttrs.indicatorElasticBaseMultiple // 基础倍数,决定拉伸的最大程度
        val basePositionOffsetCriticalValue = 0.5f // positionOffset的中值
        val indicatorCriticalValue = 1 + baseMultiple
        // indicatorCriticalValue的计算方法很有参考价值，所以详细记录下来
        // positionOffset 是 从 0 慢慢变成1的，分为两段，一段从0->0.5 ,一段从0.5->1
        // 我要求，前半段的ratio最终值，要和后半段的初始值相等，这样才能无缝衔接
        //  前半段的ratio最终值 = 1（原始倍率）+ 0.5 * baseMultiple（拉伸倍数，数值越大，拉伸越明显）
        //  后半段的ratio值 = indicatorCriticalValue（临界值） - 0.5f * baseMultiple
        // 两者必须相等，所以算出 indicatorCriticalValue（临界值） = 1（原始倍率）+0.5 * baseMultiple + 0.5 * baseMultiple
        // 最终， indicatorCriticalValue（临界值） = 1+ baseMultiple
        var ratio =
            if (parent.indicatorAttrs.indicatorElastic) {
                when {
                    positionOffset >= 0 && positionOffset < 0.5 -> {
                        1 + positionOffset * baseMultiple // 拉伸长度
                    }
                    else -> {// 如果到了下半段，当offset越过中值之后ratio的值
                        indicatorCriticalValue - positionOffset * baseMultiple
                    }
                }
            } else 1f

        // 能不能一边draw一边改变 TabView里面TextView的textSize呢？
        // 这里能够获取到

        // 可以开始绘制
        selectedIndicator.run {
            setBounds(
                ((centerX - indicatorWidth * ratio / 2).toInt()),
                top,
                ((centerX + indicatorWidth * ratio / 2).toInt()),
                bottom
            )// 规定它的边界
            draw(canvas!!)// 然后绘制到画布上
        }

        initIndicator()// 刚开始的时候，indicatorLeft和indicatorRight都是0，所以需要通过触发一次tabView的click事件来绘制
        super.draw(canvas)
    }

    private fun initIndicator() {
        Log.d("addTabViewTag", "$childCount")
        if (childCount > 0) {
            if (!inited) {
                inited = true
                val tabView0 = getChildAt(0) as GreenTabView
                tabView0.performClick() // 难道这里在岗添加进去，测量尚未完成？那怎么办,那只能在onDraw里面去执行了
            }
        }
    }

    fun updateIndicatorPosition(targetLeft: Int, targetRight: Int, positionOffset_: Float) {
        indicatorLeft = targetLeft
        indicatorRight = targetRight
        positionOffset = positionOffset_
        postInvalidate()
    }

    /**
     * 用动画平滑更新indicator的位置
     * @param tabView 当前这个子view
     */
    fun updateIndicatorPositionByAnimator(tabView: GreenTabView) {
        // 处理最外层布局( HankTabLayout )的滑动
        parent.run {
            tabView.getHitRect(tabViewBounds)
            getHitRect(parentBounds)
            val scrolledX = scrollX // 已经滑动过的距离
            val tabViewRealLeft = tabViewBounds.left - scrolledX  // 真正的left, 要算上scrolledX
            val tabViewRealRight = tabViewBounds.right - scrolledX // 真正的right, 要算上scrolledX

            val tabViewCenterX = (tabViewRealLeft + tabViewRealRight) / 2
            val parentCenterX = (parentBounds.left + parentBounds.right) / 2
            val needToScrollX = -parentCenterX + tabViewCenterX //  差值就是需要滚动的距离

            startScrollAnimator(this, scrolledX, scrolledX + needToScrollX)
        }

        // 把其他的 TabView 都设置成未选中状态
        for (i in 0 until childCount) {
            val current = getChildAt(i) as GreenTabView
            if (current.hashCode() == tabView.hashCode()) {// 如果是当前被点击的这个，那么就不需要管
                current.setSelectedStatus(true) // 选中状态
            } else {// 如果不是
                current.setSelectedStatus(false)// 非选中状态
            }
        }
    }

    /**
     * 用动画效果平滑滚动过去
     */
    private fun startScrollAnimator(tabLayout: GreenTabLayout, from: Int, to: Int) {
        if (scrollAnimator != null && scrollAnimator.isRunning) scrollAnimator.cancel()
        scrollAnimator.duration = 200
        scrollAnimator.interpolator = FastOutSlowInInterpolator()
        scrollAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            val diff = to - from
            val currentDif = (diff * progress).toInt()
            tabLayout.scrollTo(from + currentDif, 0)
        }
        scrollAnimator.start()
    }

    /**
     * 添加TabView
     */
    fun addTabView(text: String) {
        val tabView = GreenTabView(context, this)
        val margin = dpToPx(context, 10f)
        tabView.setPadding(0, margin, 0, margin)
        val param = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        val textView = TextView(context)
        param.setMargins(margin, 0, margin, 0)
        textView.text = text
        tabView.setTextView(textView)

        addView(tabView, param)

    }
}

/**
 * 最里层TabView
 */
class GreenTabView : LinearLayout {
    lateinit var titleTextView: TextView
    private var selectedStatue: Boolean = false
    private var parent: SlidingIndicatorLayout

    constructor(ctx: Context, parent: SlidingIndicatorLayout) : super(ctx) {
        this.parent = parent
    }

    fun setTextView(textView: TextView) {
        removeAllViews()

        titleTextView = textView
        parent.parent.tabViewAttrs.run {
            titleTextView.setBackgroundColor(tabViewBackgroundColor)

            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSizeSelected)
            Log.d(
                "setTextSizeTag",
                "初始，选中状态下的字体大小 : ${tabViewTextSizeSelected}"
            )

            titleTextView.typeface = tabViewTextTypeface
            titleTextView.setTextColor(tabViewTextColor)
            titleTextView.gravity = Gravity.CENTER

            titleTextView.setPadding(
                tabViewTextPaddingLeft.roundToInt(),
                tabViewTextPaddingTop.roundToInt(),
                tabViewTextPaddingRight.roundToInt(),
                tabViewTextPaddingBottom.roundToInt()
            )

        }

        val param = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        addView(titleTextView, param)

        setOnClickListener {
            parent.updateIndicatorPositionByAnimator(this)
            parent.parent.mViewPager.currentItem =
                parent.indexOfChild(this)// 拿到viewPager，然后强制滑动到指定的page
        }
    }

    fun setSelectedStatus(selected: Boolean) {
        selectedStatue = selected

        parent.parent.tabViewAttrs.run {
            if (selected) {
                titleTextView.setTextColor(tabViewTextColorSelected)
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSizeSelected)
                Log.d(
                    "setTextSizeTag",
                    "变为选中 : $tabViewTextSizeSelected"
                )
            } else {
                titleTextView.setTextColor(tabViewTextColor)
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSize)
                Log.d(
                    "setTextSizeTag",
                    "变为不选中 : $tabViewTextSize"
                )
            }
        }
    }

}

