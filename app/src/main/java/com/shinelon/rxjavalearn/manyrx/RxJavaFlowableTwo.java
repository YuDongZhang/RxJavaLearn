package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by pateo on 18-8-21.
 */

public class RxJavaFlowableTwo {
    public static final String TAG = RxJavaFlowableTwo.class.getSimpleName();
    private Subscription mSubscription;

    /**
     * 先来回顾一下上上节，我们讲Flowable的时候，说它采用了响应式拉的方式，我们还举了个叶问打小日本的例子，
     * 再来回顾一下吧，我们说把上游看成小日本, 把下游当作叶问, 当调用Subscription.request(1)时,
     * 叶问就说我要打一个! 然后小日本就拿出一个鬼子给叶问, 让他打, 等叶问打死这个鬼子之后, 再次调用request(10),
     * 叶问就又说我要打十个! 然后小日本又派出十个鬼子给叶问, 然后就在边上看热闹, 看叶问能不能打死十个鬼子,
     * 等叶问打死十个鬼子后再继续要鬼子接着打。
     * 但是不知道大家有没有发现，在我们前两节中的例子中，我们口中声称的响应式拉并没有完全体现出来，比如这个例子：
     * <p>
     * 作者：Season_zlc
     * 链接：https://www.jianshu.com/p/36e0f7f43a51
     * 來源：简书
     * 简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
     */


    public void flowableExample() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "e 1");
                e.onNext(1);
                Log.d(TAG, "e 2");
                e.onNext(2);
                Log.d(TAG, "e 3");
                e.onNext(3);
                Log.d(TAG, "e complete");
                e.onComplete();

            }
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");

                        mSubscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: " + integer);
                        mSubscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.w(TAG, "onError: ", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
                    }
                });

    }

    /**
     * 虽然我们在下游中是每次处理掉了一个事件之后才调用request(1)去请求下一个事件，也就是说叶问的确是在打死了一个鬼子
     * 之后才继续打下一个鬼子，可是上游呢？上游真的是每次当下游请求一个才拿出一个吗？从上上篇文章中我们知道并不是这样的，
     * 上游仍然是一开始就发送了所有的事件，也就是说小日本并没有等叶问打死一个才拿出一个，而是一开始就拿出了所有的鬼子，
     * 这些鬼子从一开始就在这儿排队等着被打死。
     链接：https://www.jianshu.com/p/36e0f7f43a51
     */

    /**
     * 没错，我们前后所说的就是自相矛盾了，这说明了什么呢，说明我们的实现并不是一个完整的实现，那么，究竟怎样的实现才
     * 是完整的呢？
     * <p>
     * 我们先自己来想一想，在下游中调用Subscription.request(n)就可以告诉上游，下游能够处理多少个事件，那么上游要根据
     * 下游的处理能力正确的去发送事件，那么上游是不是应该知道下游的处理能力是多少啊，对吧，不然，一个巴掌拍不响啊，这种
     * 事情得你情我愿才行。
     * <p>
     * 那么上游从哪里得知下游的处理能力呢？我们来看看上游最重要的部分，肯定就是FlowableEmitter了啊，我们就是通过它来
     * 发送事件的啊，来看看它的源码吧(别紧张，它的代码灰常简单)：
        public interface FlowableEmitter<T> extends Emitter<T> {
            void setDisposable(Disposable s);

            void setCancellable(Cancellable c);

            long requested();

            boolean isCancelled();

            FlowableEmitter<T> serialize();
        }
     FlowableEmitter是个接口，继承Emitter，Emitter里面就是我们的onNext(),onComplete()和onError()三个方法。
     我们看到FlowableEmitter中有这么一个方法：
     long requested();
     方法注释的意思就是当前外部请求的数量，哇哦，这好像就是我们要找的答案呢. 我们还是实际验证一下吧.

     */

    public void FlowableResponsePull(){
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "current requested: " + e.requested());
            }
        },BackpressureStrategy.ERROR)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
                        mSubscription = s;
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
                });

    }




}
