package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by pateo on 18-8-17.
 */

public class RxJavaThreadControl {
    public static final String TAG = RxJavaThreadControl.class.getSimpleName();

    public void sameThread() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "Observable thread :" + Thread.currentThread().getName());
                e.onNext(1);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG, "Consumer thread :" + Thread.currentThread().getName());
            }
        });
    }

    public void differenceThread() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "Observable thread :" + Thread.currentThread().getName());
                e.onNext(1);
            }
            //subscribeOn() 指定的是上游发送事件的线程, observeOn() 指定的是下游接收事件的线程.
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "Consumer thread :" + Thread.currentThread().getName());
                    }
                });
    }

    /*
    subscribeOn() 指定的是上游发送事件的线程, observeOn() 指定的是下游接收事件的线程.
    多次指定上游的线程只有第一次指定的有效, 也就是说多次调用subscribeOn() 只有第一次的有效, 其余的会被忽略.
    多次指定下游的线程是可以的, 也就是说每调用一次observeOn() , 下游的线程就会切换一次.
     */


    public void switchThread() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "Observable thread :" + Thread.currentThread().getName());
                e.onNext(1);
            }

        }).subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "Consumer thread :" + Thread.currentThread().getName());
                    }
                });

    }
    /*
        上游虽然指定了两次线程, 但只有第一次指定的有效, 依然是在RxNewThreadScheduler 线程中, 而下游则跑到了RxCachedThreadScheduler 中,
        这个CacheThread其实就是IO线程池中的一个.
     */


    public void switchTwoThread() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "Observable thread :" + Thread.currentThread().getName());
                e.onNext(1);
            }

        }).subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "After observeOn(io), current thread is : " + Thread.currentThread().getName());
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "Consumer thread :" + Thread.currentThread().getName());
                    }
                });

    }

}
