package com.ooftf.spiale

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.View
import android.widget.BaseAdapter
import android.widget.FrameLayout
import tf.oof.com.service.engine.LoopTimer
import tf.oof.com.service.engine.ScrollerPlus
import java.util.*

class SpialeLayout : FrameLayout {
    var position = 0
        internal set
    private val scroller = InnerScrollerPlus()
    private var unUsedViewPool: MutableList<View> = ArrayList()
    var adapter: BaseAdapter? = null
        set(value) {
            //处理原来的adapter
            field?.unregisterDataSetObserver(observer)
            //赋予新的adapter
            field = value
            field?.registerDataSetObserver(observer)
            reLayoutItem()
        }
    private val runningTimer: InnerLoopTimer by lazy {
        InnerLoopTimer()
    }
    private var observer = InnerDataSetObserver()
    private var listener: ((position: Int, itemView: View, itemData: Any) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        obtainAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        obtainAttrs(attrs)
    }

    var scrollMillis: Int = 2000
    var showMillis: Int = 2000
    private fun obtainAttrs(attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SpialeLayout)
        scrollMillis = attributes.getInteger(R.styleable.SpialeLayout_scrollMillis, 2000)
        showMillis = attributes.getInteger(R.styleable.SpialeLayout_showMillis, 2000)
    }

    fun setOnItemClickListener(listener: (position: Int, itemView: View, itemData: Any) -> Unit) {
        this.listener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        when (layoutParams.height) {
            LayoutParams.WRAP_CONTENT -> throw IllegalAccessException("VerticalRunningLayout layout_height必须是固定高度")
        }
    }

    /**
     * 重新添加控件
     */
    private fun reLayoutItem() {
        recyclerAllViews()
        addItemView(position)
        addItemView(position + 1)
        runningTimer.start()
    }

    private fun recyclerAllViews() {
        while (childCount > 0) {
            var view = getChildAt(0)
            removeView(view)
        }
    }

    override fun removeView(view: View) {
        super.removeView(view)
        unUsedViewPool.add(view)
    }

    override fun addView(child: View) {
        unUsedViewPool.remove(child)
        super.addView(child)
    }

    /**
     * 添加View
     */
    private fun addItemView(position: Int) {
        adapter?.let {
            if (it.count == 0) return@addItemView
            val item: View
            if (unUsedViewPool.size > 0) {
                item = it.getView(convertPosition(position), unUsedViewPool[0], this)
            } else {
                item = it.getView(convertPosition(position), null, this)
                item.setOnClickListener { v ->
                    val positionInner = convertPosition(v.getTag(TAG_KEY_POSITION) as Int)
                    listener?.invoke(positionInner, v, it.getItem(positionInner))
                }
                if (item.layoutParams == null) {
                    item.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                } else {
                    item.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                    item.layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
                }
            }
            item.setTag(TAG_KEY_POSITION, position)
            addView(item)
        }

    }

    private fun convertPosition(totalPosition: Int = position) = totalPosition % adapter!!.count

    private fun scrollToNextPosition() {
        scroller.startScroll(0, this.position * height, 0, height, scrollMillis)
    }


    private fun findViewsByPosition(position: Int): View? {
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            val viewPosition = v.getTag(TAG_KEY_POSITION) as Int
            if (viewPosition == position) {
                return v
            }
        }
        return null
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            val position = v.getTag(TAG_KEY_POSITION) as Int
            v.layout(0, position * (b - t), r,
                    (position + 1) * (b - t))
        }
    }

    override fun onDetachedFromWindow() {
        runningTimer.cancel()
        scroller.cancel()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE) {
            if (adapter != null) {
                runningTimer.start()
            }
        } else {
            runningTimer.cancel()
        }
    }

    companion object {
        private val TAG_KEY_POSITION = R.integer.tag_key_position//随便一个id，因为tag 的key必须为id
    }

    /**
     * 用于 完成滚动动画
     */
    inner class InnerScrollerPlus : ScrollerPlus(context) {
        override fun onMoving(currX: Int, currY: Int) {
            scrollTo(currX, currY)
        }

        override fun onFinish() {
            position++
            val recycle = findViewsByPosition(position - 1)
            if (recycle != null) {
                removeView(recycle)
            }
            addItemView(position + 1)
        }
    }

    inner class InnerDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            reLayoutItem()
        }

        override fun onInvalidated() {
            reLayoutItem()
        }
    }

    /**
     * 用于控件不断的滚动
     */
    inner class InnerLoopTimer : LoopTimer(showMillis.toLong(), showMillis + scrollMillis.toLong()) {
        override fun onTrick() {
            scrollToNextPosition()
        }
    }
}
