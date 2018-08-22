package com.shinelon.rxjavalearn.manyrx;

import android.nfc.Tag;
import android.util.Log;

import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;

class WrapDownStreamObserver<T> implements MaybeObserver<T> {

    public static final String TAG = "TAG";

    private MaybeObserver<T> actual;

    //这里将真正的下游传过来,
    public WrapDownStreamObserver(MaybeObserver<T> actual) {
        this.actual = actual;
    }

    @Override
    public void onSubscribe(Disposable d) {
        actual.onSubscribe(d);
    }

    @Override
    public void onSuccess(T t) {
        Log.d(TAG, "Hooked onSuccess"); //在真上游的前方插入的log
        actual.onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, "Hooked onError");
        actual.onError(e);
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Hooked onComplete");
        actual.onComplete();
    }
}


