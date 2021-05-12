package com.zhouppei.digimuscore.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class LockableHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {

    private var mScrollable = false

    fun setScrollingEnabled(enabled: Boolean) {
        mScrollable = enabled;
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            if (it.action == MotionEvent.ACTION_DOWN) {
                return mScrollable && super.onTouchEvent(ev)
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return mScrollable && super.onInterceptTouchEvent(ev)
    }

}