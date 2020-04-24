package com.ooftf.spiale

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.View
import android.widget.BaseAdapter
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 自动滚动布局
 */
class SpialeLayout : FrameLayout {
    /**
     * 记录位置
     */
    var position = 0
        internal set

    /**
     * 用于做滚动动画
     */
    private val scroller = InnerScrollerPlus()

    /**
     * view回收池,用于复用
     */
    private var unUsedViewPool: MutableList<View> = ArrayList()

    /**
     * 适配器
     */
    var adapter: BaseAdapter? = null
        set(value) {
            //处理原来的adapter
            field?.unregisterDataSetObserver(observer)
            //赋予新的adapter
            field = value
            field?.registerDataSetObserver(observer)
            reLayoutItem()
        }

    /**
     * 用于取消翻页
     */
    var disposable: Disposable? = null

    /**
     * 翻页定时器
     */
    private val observable: Observable<Long> by lazy {
        Observable
                .interval(showMillis.toLong(), showMillis + scrollMillis.toLong(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 监听 adapter事件
     */
    private var observer = InnerDataSetObserver()

    /**
     * 点击事件
     */
    private var listener: ((position: Int, itemView: View, itemData: Any) -> Unit)? = null

    /**
     * 滚动时间
     */
    var scrollMillis: Int = 1000

    /**
     * 停留时间
     */
    var showMillis: Int = 2000

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        obtainAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        obtainAttrs(attrs)
    }


    private fun obtainAttrs(attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SpialeLayout)
        scrollMillis = attributes.getInteger(R.styleable.SpialeLayout_scrollMillis, scrollMillis)
        showMillis = attributes.getInteger(R.styleable.SpialeLayout_showMillis, showMillis)
    }

    fun setOnItemClickListener(listener: (position: Int, itemView: View, itemData: Any) -> Unit) {
        this.listener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        when (layoutParams.height) {
            LayoutParams.WRAP_CONTENT -> throw IllegalAccessException("SpialeLayout layout_height必须是固定高度")
        }
        startScroll()
    }

    fun startScroll() {
        if (!scroll) {
            return
        }
        if (disposable != null && !disposable!!.isDisposed) {
            return
        }
        observable.subscribe(object : EmptyObserver<Long>() {
            override fun onSubscribe(d: Disposable) {
                disposable?.dispose()
                disposable = d
            }

            override fun onNext(t: Long) {
                if (adapter != null && visibility == View.VISIBLE) {
                    smoothToNextPosition()
                }
            }
        })
    }

    fun stopScroll() {
        disposable?.dispose()
        scroller.cancel()
    }

    override fun onDetachedFromWindow() {
        stopScroll()
        super.onDetachedFromWindow()
    }

    /**
     * 重新添加控件
     */
    private fun reLayoutItem() {
        recyclerAllViews()
        addItemView(position)
        addItemView(position + 1)
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

    fun removeView(position: Int) {
        val recycle = findViewsByPosition(position)
        if (recycle != null) {
            removeView(recycle)
        }
    }

    override fun addView(child: View) {
        unUsedViewPool.remove(child)
        super.addView(child)
    }

    var scroll = true
    fun stop() {
        scroll = false
        stopScroll()
    }

    fun start() {
        scroll = true
        startScroll()
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

    private fun smoothToNextPosition() {
        scroller.startScroll(0, this.position * height, 0, height, scrollMillis)
    }

    private fun scrollToPosition(position: Int) {
        this.position = position;
        scrollTo(0, position * height)
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

    companion object {
        private val TAG_KEY_POSITION = R.id.tag_key_position//随便一个id，因为tag 的key必须为id
    }

    /**
     * 用于 完成滚动动画
     */
    inner class InnerScrollerPlus : ScrollerPlus(context) {
        override fun onMoving(currX: Int, currY: Int) {
            scrollTo(currX, currY)
        }

        override fun onFinish() {
            animFinish()
        }

        override fun onCancel() {
            animFinish()
        }

        private fun animFinish() {
            scrollToPosition(position + 1)
            removeView(position - 1)
            addItemView(position + 1)
        }
    }

    inner class InnerDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            adapter?.let {
                if (it.count <= 1) {
                    stop()
                } else {
                    start()
                }
            }
            reLayoutItem()
        }

        override fun onInvalidated() {
            adapter?.let {
                if (it.count <= 1) {
                    stop()
                } else {
                    start()
                }
            }
            reLayoutItem()
        }
    }
}
