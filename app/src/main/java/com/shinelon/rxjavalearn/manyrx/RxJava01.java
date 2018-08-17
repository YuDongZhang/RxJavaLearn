package com.shinelon.rxjavalearn.manyrx;


import android.util.Log;

import com.shinelon.rxjavalearn.LogUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by Shinelon on 2018/8/15.
 * 响应式编程是一种基于异步数据流概念的编程模式。数据流就像一条河：它可以被观测，被过滤，被操作，
 * 或者为新的消费者与另外一条流合并为一条新的流。响应式编程的一个关键概念是事件。事件可以被等待，可以触发过程，
 * 也可以触发其它事件。事件是唯一的以合适的方式将我们的现实世界映射到我们的软件中：如果屋里太热了我们就打开一扇窗户。
 * 同样的，当我们的天气app从服务端获取到新的天气数据后，我们需要更新app上展示天气信息的UI；汽车上的车道偏移系统探测到车辆偏移了
 * 正常路线就会提醒驾驶者纠正，就是是响应事件。
 */

public class RxJava01 {
    private static final String TAG = RxJava01.class.getSimpleName();

    public  void  testInteger(){
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                 /*
                    ObservableEmitter： Emitter是发射器的意思，那就很好猜了，这个就是用来发出事件的，它可以发出三种类型的
                    事件，通过调用emitter的onNext(T value)、onComplete()和onError(Throwable error)就可以分别发出
                    next事件、complete事件和error事件。
                 */

                Log.e(TAG, "Observable emit 1" + "\n");
                e.onNext(1);
                Log.e(TAG, "Observable emit 2" + "\n");
                e.onNext(2);
                Log.e(TAG, "Observable emit 3" + "\n");
                e.onNext(3);
                e.onComplete();
                Log.e(TAG, "Observable emit 4" + "\n" );
                e.onNext(4);

            }
        }).subscribe(// 第三步：订阅
                new Observer<Integer>() {// 第二步：初始化Observer

                    private int i;
                    private Disposable mDisposable;
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                i++;
                if (i == 2){
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件
                   mDisposable.dispose();//是有返回值
                }

                Log.d(TAG,"onNext : value :"+integer + "\n");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onComplete" + "\n" );
            }

            @Override
            public void onComplete() {

                LogUtils.e(TAG, "onComplete" + "\n");

            }
        });
    }

    /*
    *上游可以发送无限个onNext, 下游也可以接收无限个onNext.
    当上游发送了一个onComplete后, 上游onComplete之后的事件将会继续发送, 而下游收到onComplete事件之后将不再继续接收事件.
    当上游发送了一个onError后, 上游onError之后的事件将继续发送, 而下游收到onError事件之后将不再继续接收事件.
    上游可以不发送onComplete或onError.
    最为关键的是onComplete和onError必须唯一并且互斥, 即不能发多个onComplete, 也不能发多个onError, 也不能先发一个onComplete, 然后再发一个onError, 反之亦然
    注: 关于onComplete和onError唯一并且互斥这一点, 是需要自行在代码中进行控制, 如果你的代码逻辑中违背了这个规则, **并不一定会导致程序崩溃. ** 比如发送多个onComplete是可以正常运行的, 依然是收到第一个onComplete就不再接收了, 但若是发送多个onError, 则收到第二个onError事件会导致程序会崩溃.

    * */


    public void testConsumer(){ //
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {

                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onComplete();
            }
        }).subscribe( //查看subscribe重载的方法 , 来进行判断要接受的东西
                new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG,"onNext:"+integer);
            }
        });
    }


}
