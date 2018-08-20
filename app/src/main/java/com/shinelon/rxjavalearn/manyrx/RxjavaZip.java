package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by pateo on 18-8-20.
 * zip的作用事组合但是要注意 , 就是 组合2个observable  不过要注意线程的问题
 *
 * 用途事在2个地方要用的时候
 */

public class RxjavaZip {
    public static final String TAG = RxjavaZip.class.getSimpleName();

    public void testZip() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "emit 1");
                e.onNext(1);
                Log.d(TAG, "emit 2");
                e.onNext(2);
                Log.d(TAG, "emit 3");
                e.onNext(3);
                Log.d(TAG, "emit 4");
                e.onNext(4);
                Log.d(TAG, "emit complete1");
                e.onComplete();
            }
        });

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.d(TAG, "emit A");
                e.onNext("A");
                Log.d(TAG, "emit B");
                e.onNext("B");
                Log.d(TAG, "emit C");
                e.onNext("C");
                Log.d(TAG, "emit complete2");
                e.onComplete();
            }
        });

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });
       /* 阿西吧, 好像真的是先发送的水管一再发送的水管二呢, 为什么会有这种情况呢?
                因为我们两根水管都是运行在同一个线程里, 同一个线程里执行代码肯定有先后顺序呀.*/
    }


    /**
     * 组合的过程是分别从 两根水管里各取出一个事件 来进行组合, 并且一个事件只能被使用一次,
     * 组合的顺序是严格按照事件发送的顺利 来进行的, 也就是说不会出现圆形1 事件和三角形B 事件进行合并,
     * 也不可能出现圆形2 和三角形A 进行合并的情况.
     * 最终下游收到的事件数量 是和上游中发送事件最少的那一根水管的事件数量 相同. 这个也很好理解,
     * 因为是从每一根水管 里取一个事件来进行合并, 最少的 那个肯定就最先取完 , 这个时候其他的水管尽管还有事件 ,
     * 但是已经没有足够的事件来组合了, 因此下游就不会收到剩余的事件了.
     */
    public void testZipThread(){
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "emit 1");
                e.onNext(1);
                Thread.sleep(1000);

                Log.d(TAG, "emit 2");
                e.onNext(2);
                Thread.sleep(1000);

                Log.d(TAG, "emit 3");
                e.onNext(3);
                Thread.sleep(1000);

                Log.d(TAG, "emit 4");
                e.onNext(4);


                Log.d(TAG, "emit complete1");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());


        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.d(TAG, "emit A");
                e.onNext("A");
                Thread.sleep(1000);

                Log.d(TAG, "emit B");
                e.onNext("B");
                Thread.sleep(1000);

                Log.d(TAG, "emit C");
                e.onNext("C");
                Thread.sleep(1000);

                Log.d(TAG, "emit complete2");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });
    }
}
