package com.ooftf.spiale

import android.content.Context
import android.util.Log
import android.view.animation.Interpolator
import android.widget.Scroller
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


/**
 * Created by master on 2017/10/18 0018.
 */
abstract class ScrollerPlus : Scroller {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, interpolator: Interpolator?) : super(context, interpolator)
    constructor(context: Context?, interpolator: Interpolator?, flywheel: Boolean) : super(context, interpolator, flywheel)

    var disposable: Disposable? = null
    var isScrolling = false

    abstract fun onMoving(currX: Int, currY: Int)

    open fun onFinish() {

    }

    /**
     * computeScrollOffset 每次只能调用一次，如果调用两次会出现，两次结果不一致的情况，因为第一次运算，有可能得出结果已经结束但这次放回结果却是true，但是下一次调用返回结果确实false
     *
     * 可以理解为，computeScrollOffset 返回值，代表 两次computeScrollOffset时得出的结果是否有差值
     */
    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, duration)
        disposable?.dispose()
        Observable
                .intervalRange(0, (duration / 10f).toLong(), 0, 1000 / 100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {
                    override fun onComplete() {
                        isScrolling = false
                        onFinish()
                    }

                    override fun onSubscribe(d: Disposable) {
                        isScrolling = true
                        disposable = d
                    }

                    override fun onNext(t: Long) {
                        computeScrollOffset();
                        onMoving(currX, currY)
                    }

                    override fun onError(e: Throwable) {
                        isScrolling = false
                        Log.e("ScrollerPlus", e.toString())
                    }

                })
    }

    fun cancel() {
        if (isScrolling) {
            onCancel()
            isScrolling = false
        }
        disposable?.dispose()
    }

    open fun onCancel() {

    }
}