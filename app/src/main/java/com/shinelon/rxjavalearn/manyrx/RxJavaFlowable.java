package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by pateo on 18-8-20.
 */

public class RxJavaFlowable {
    public static final String TAG = RxJavaFlowable.class.getSimpleName();

    public void testFlowable() {
        Flowable<Integer> upStream = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "e 1");
                e.onNext(1);
                Log.d(TAG, "e 2");
                e.onNext(2);
                Log.d(TAG, "e 3");
                e.onNext(3);
                Log.d(TAG,"e complete");
                e.onComplete();

            }
        }, BackpressureStrategy.ERROR);//添加一个参数

        Subscriber<Integer> downStream = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG, "onSubscribe");
                s.request(Long.MAX_VALUE);  //注意这句代码
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "onNext: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.w(TAG, "onError: ", t);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        };

        upStream.subscribe(downStream);
    }
}
