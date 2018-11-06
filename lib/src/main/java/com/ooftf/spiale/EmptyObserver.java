package com.ooftf.spiale;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author ooftf
 * @date 2018/9/24 0024
 **/
public class EmptyObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {
        Log.e("EmptyObserver.onError",e.toString());
    }

    @Override
    public void onComplete() {

    }
}
