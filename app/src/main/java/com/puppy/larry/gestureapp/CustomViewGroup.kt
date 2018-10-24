package com.puppy.larry.gestureapp

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.view.*

class CustomViewGroup constructor(context: Context, attributeSet: AttributeSet? = null)
    : ViewGroup(context, attributeSet) {
    val SCREENWITH = 1920
    val SCREENHEIGHT = 2560
    /**
     * 默认宽度
     */
    private var mFirstViewOriginWidth = 0
    private var mFirstViewOriginHeight = 0

    private var mViewDragHelper: ViewDragHelper
    var mScaleGestureDetector: ScaleGestureDetector? = null

    var mFirstView: View? = null
    private var mSecondView: View? = null
    /**
     * 最小的宽度
     */
    var mFirstViewCurWith = 0
    var mFirstViewCurHeight = 0
    /**
     * 横向可以活动空间
     */
    var mHorizontalRange = 0
    /**
     * 纵向可以活动的空间
     */
    var mVeriticalRange = 0
    var mLeft = 0
    var mTop = 0
    /**
     * 当前是不是最小状态
     */
    var mIsFinishInit = false
    var mOriginCallBack: CallBack? = null
    /**
     * 上次双指缩放的时间
     */
    var mLastScaleEndTime = 0L


    init {
        mViewDragHelper = ViewDragHelper.create(this, MyHelperCallback())
        mScaleGestureDetector = ScaleGestureDetector(context, MyScaleGestureDetectorLisenter())
    }

    inner class MyScaleGestureDetectorLisenter() : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            curScale = detector.scaleFactor * preScale
            if (curScale > 3 || curScale < 0.8) {
                preScale = curScale
                return true
            }
            var lastWidth = mFirstViewCurWith
            var lastHeight = mFirstViewCurHeight
            mFirstViewCurWith = (mFirstViewCurWith * curScale).toInt()
            mFirstViewCurHeight = (mFirstViewCurHeight * curScale).toInt()
            mLeft -= (mFirstViewCurWith - lastWidth) / 2
            mTop -= (mFirstViewCurHeight - lastHeight) / 2

            requestLayoutLight()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            mLastScaleEndTime = System.currentTimeMillis()
        }


    }

    var preScale = 1.0f
    var curScale = 1.0f

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 2) {
            throw RuntimeException("this ViewGroup must only have 2 child")
        }
        mSecondView = getChildAt(0)
        mFirstView = getChildAt(1)

        textView7.setOnClickListener {
            Toast.makeText(context, "666666", Toast.LENGTH_LONG).show()
            mFirstViewExitScreen()
        }
        textView4.setOnClickListener {
            Toast.makeText(context, "3333", Toast.LENGTH_LONG).show()
            mFirstViewEnterScreen()
        }
    }

    private fun mFirstViewExitScreen() {
        mFirstViewCurHeight = 300
        mFirstViewCurWith = 500
        onLayoutLight()
    }

    private fun mFirstViewEnterScreen() {
        mFirstViewCurWith = SCREENWITH
        mFirstViewCurHeight = SCREENHEIGHT
        mLeft = 0
        mTop = 0
        onLayoutLight()
    }

    //测量工作用于确定view的位置
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        customMeasure(widthMeasureSpec, heightMeasureSpec)

        var maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        var maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                View.resolveSizeAndState(maxHeight, heightMeasureSpec, 0))

        if (!mIsFinishInit) {
            mFirstViewCurWith = mFirstView!!.measuredWidth
            restorePosition()
            mIsFinishInit = true
        }
    }

    /**
     * 复原位置
     */
    private fun restorePosition() {
        mFirstView!!.let {
            it.alpha = 1f
            it.left = 0
            it.top = 0
            mLeft = 0
            mTop = 0
        }
    }

    private fun customMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureFirst(widthMeasureSpec, heightMeasureSpec)
        measureSecond(widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureFirst(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var lp = mFirstView!!.layoutParams
        if (!mIsFinishInit) {
            var measureWidth = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight, lp.width)
            var measureHeight = getChildMeasureSpec(widthMeasureSpec, paddingTop + paddingBottom, lp.height)

            mFirstViewCurWith = MeasureSpec.getSize(measureWidth)
            mFirstViewCurHeight = MeasureSpec.getSize(measureHeight)

            mFirstViewOriginWidth = mFirstViewCurWith
            mFirstViewOriginHeight = mFirstViewCurHeight
        }
        justMeasureFirstView()
    }

    private fun justMeasureFirstView() {
        var w = MeasureSpec.makeMeasureSpec(mFirstViewCurWith, MeasureSpec.EXACTLY)
        var h = MeasureSpec.makeMeasureSpec(mFirstViewCurHeight, MeasureSpec.EXACTLY)

        mFirstView?.measure(w, h)
    }

    private fun measureSecond(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(mSecondView, widthMeasureSpec, heightMeasureSpec)
    }

    //    =====================  OnLayout  ============================================
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        onLayoutLight()
    }

    private fun onLayoutLight() {
        mSecondView?.layout(0, 0, SCREENWITH, SCREENHEIGHT)
        justLayoutFirstView()
    }

    private fun justLayoutFirstView() {
        mFirstView?.layout(mLeft, mTop, mLeft + mFirstViewCurWith, mTop + mFirstViewCurHeight)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.pointerCount == 1) {
            mViewDragHelper.shouldInterceptTouchEvent(ev)
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when {
            event.pointerCount == 1 -> {
                //双指操作完 300毫秒内,单指的数据不进行处理
                if (System.currentTimeMillis() - mLastScaleEndTime <= 300) {
                    return super.onTouchEvent(event)
                }
                var isHit = mViewDragHelper.isViewUnder(mFirstView, event.x.toInt(), event.y.toInt())
                try {
                    mViewDragHelper.processTouchEvent(event)

                } catch (exception: IllegalArgumentException) {
                    exception.printStackTrace()
                }
                isHit
            }
            event.pointerCount == 2 -> {
                mScaleGestureDetector!!.onTouchEvent(event)
                return true
            }
            else -> {
                super.onTouchEvent(event)
            }
        }


    }

    inner class MyHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(p0: View, p1: Int): Boolean {
            return p0 == mFirstView
        }

        //checkslop 的时候   垂直 ke 拖动的最大距离
        override fun getViewVerticalDragRange(child: View): Int {
            return if (child == mFirstView) {
                SCREENHEIGHT - mFirstViewCurHeight
            } else {
                0
            }
        }

        //checkslop 的时候   水平 ke 拖动的最大距离
        override fun getViewHorizontalDragRange(child: View): Int {
            return if (child == mFirstView) {
                SCREENWITH - mFirstViewCurWith
            } else {
                0
            }

        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            mLeft = left
            return super.clampViewPositionHorizontal(child, left, dx)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            mTop = top
            return super.clampViewPositionVertical(child, top, dy)
        }

        //当ViewDragHelper状态发生变化时回调（IDLE,DRAGGING,SETTING[自动滚动时]）
        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_IDLE) {

            }
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            requestLayoutLight()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        }

    }

    fun requestLayoutLight() {
        justMeasureFirstView()
        justLayoutFirstView()
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun setOriginCallBack(callback: CallBack) {
        mOriginCallBack = callback
    }

    interface CallBack {
        fun onRestore()
    }
}