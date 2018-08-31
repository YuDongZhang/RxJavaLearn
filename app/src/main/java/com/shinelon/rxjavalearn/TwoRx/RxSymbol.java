package com.shinelon.rxjavalearn.TwoRx;


import android.support.annotation.NonNull;
import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by pateo on 18-8-23.
 * 链接：https://www.jianshu.com/p/e9c79eacc8e3
 */

public class RxSymbol {
    public static final String TAG = RxSymbol.class.getSimpleName();
    private String mRxOperatorsText;

    public void testDistinct() { //通过打印看出这个符号的意思是去除重复的东西
        Observable.just(1, 1, 2, 2, 3, 4, 5)
                .distinct() //明显的，清楚的;卓越的，不寻常的;有区别的;确切的
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "accept: " + integer);
                    }
                });
    }

    /**
     * 信我，Filter 你会很常用的，它的作用也很简单，过滤器嘛。可以接受一个参数，让其过滤掉不符合我们条件的值
     */
    public void testFilter() {
        Observable.just(1, 2, 3, 4, 5, 6, 1)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer > 3;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG, "accept: " + integer);
            }
        });
    }

    /**
     * buffer 操作符接受两个参数，buffer(count,skip)，作用是将 Observable 中的数据按 skip (步长) 分成最大
     * 不超过 count 的 buffer ，然后生成一个 Observable 。也许你还不太理解，我们可以通过我们的示例图和示例代码
     * 来进一步深化它。
     */
    public void testBuffer() {
        Observable.just(1, 2, 3, 4, 5)
                .buffer(3, 2)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) throws Exception {
                        Log.e(TAG, "buffer size : " + integers.size() + "\n");
                        Log.e(TAG, "buffer value : ");
                        for (Integer i : integers) {
                            Log.e(TAG, i + "");
                        }
                        Log.e(TAG, "\n");
                    }
                });
    }

    /**
     * tongd
     */
    public void testTimer() {
        // Log.e(TAG, "timer start : " + TimeUtil.getNowStrTime() + "\n");
        Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        // timer 默认在新线程，所以需要切换回主线程
                        // Log.e(TAG, "timer :" + aLong + " at " + TimeUtil.getNowStrTime() + "\n");
                    }
                });
    }

    public void testInterval() {
        Log.e(TAG, "interval start : " + TimeUtils.getNowString() + "\n");
        Observable.interval(3, 2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.e(TAG, "interval :" + aLong + " at " + TimeUtils.getNowString() + "\n");
                    }
                });
    }

    /*
    如同 Log 日志一样，第一次延迟了 3 秒后接收到，后面每次间隔了 2 秒。
     然而，心细的小伙伴可能会发现，由于我们这个是间隔执行，所以当我们的Activity 都销毁的时候，实际上这个操作还依然在
     进行，所以，我们得花点小心思让我们在不需要它的时候干掉它。查看源码发现，我们subscribe(Cousumer<? super T> onNext)
     返回的是Disposable，我们可以在这上面做文章。


    前面改成这样再来修改 等等.......mDisposable = Observable.interval(3, 2, TimeUnit.SECONDS)
     @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

     */

    public void testDoOnNext() {
        Observable.just(1, 2, 3, 4)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "doOnNext 保存 " + integer + "成功" + "\n");
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                Log.e(TAG, "doOnNext :" + integer + "\n");
            }
        });

    }

    /*
    其实觉得 doOnNext 应该不算一个操作符，但考虑到其常用性，我们还是咬咬牙将它放在了这里。它的作用是让订阅者在接收到
    数据之前干点有意思的事情。假如我们在获取到数据之前想先保存一下它，无疑我们可以这样实现。
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext 保存 1成功
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext :1
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext 保存 2成功
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext :2
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext 保存 3成功
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext :3
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext 保存 4成功
    08-31 06:28:01.119 22361-22361/com.shinelon.rxjavalearn E/RxSymbol: doOnNext :4
    */

    public void testSkip() {
        Observable.just(1, 2, 3, 4)
                .skip(2)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "skip : " + integer + "\n");
                    }
                });
    }

    /*
    skip 很有意思，其实作用就和字面意思一样，接受一个 long 型参数 count ，代表跳过 count 个数目开始接收。
    08-31 07:40:02.044 20652-20652/com.shinelon.rxjavalearn E/RxSymbol: skip : 3
    08-31 07:40:02.044 20652-20652/com.shinelon.rxjavalearn E/RxSymbol: skip : 4

    发现就是跳过了2个
     */


    public void testTake() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .take(3)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "accept: take : " + integer + "\n");
                    }
                });
    }

    /*
    take，接受一个 long 型参数 count ，代表至多接收 count 个数据。
     */


    public void testSingle() {
        Single.just(new Random().nextInt())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer value) {
                        Log.e(TAG, "single : onSuccess : " + value + "\n");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "single : onError : " + e.getMessage() + "\n");
                    }
                });
    }

    /*
    顾名思义，Single 只会接收一个参数，而 SingleObserver 只会调用 onError() 或者 onSuccess()。
    08-31 07:53:28.603 25974-25974/? E/RxSymbol: single : onSuccess : -1004930974
     */

    /**
     * 去除发送频率过快的项，看起来好像没啥用处，但你信我，后面绝对有地方很有用武之地。
     */
    public void testDebounce() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1); // skip
                 Thread.sleep(400);
                 emitter.onNext(2); // deliver
                 Thread.sleep(505);
                 emitter.onNext(3); // skip
                 Thread.sleep(100);
                 emitter.onNext(4); // deliver
                 Thread.sleep(605);
                 emitter.onNext(5); // deliver
                 Thread.sleep(510);
                 emitter.onComplete();

            }
        }).debounce(500,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG,"debounce :" + integer + "\n");
                    }
                });
    }
    /*
        08-31 08:06:20.560 32485-32485/com.shinelon.rxjavalearn E/RxSymbol: debounce :2
        08-31 08:06:21.165 32485-32485/com.shinelon.rxjavalearn E/RxSymbol: debounce :4
        08-31 08:06:21.770 32485-32485/com.shinelon.rxjavalearn E/RxSymbol: debounce :5

        代码很清晰，去除发送间隔时间小于 500 毫秒的发射事件，所以 1 和 3 被去掉了
     */

    /**
     * 简单地时候就是每次订阅都会创建一个新的 Observable，并且如果没有被订阅，就不会产生新的 Observable
     */
    public void testDefer(){
         Observable<Integer> observable = Observable.defer(new Callable<ObservableSource<? extends Integer>>() {
            @Override
            public ObservableSource<? extends Integer> call() throws Exception {
                return Observable.just(1,2,3,4);
            }
        });

         observable.subscribe(new Observer<Integer>() {
             @Override
             public void onSubscribe(Disposable d) {

             }

             @Override
             public void onNext(Integer value) {
                 Log.e(TAG, "defer : " + value + "\n");
             }

             @Override
             public void onError(Throwable e) {
                 Log.e(TAG, "defer : onError : " + e.getMessage() + "\n");
             }

             @Override
             public void onComplete() {
                 Log.e(TAG, "defer : onComplete\n");
             }
         });
    }
    /*
    08-31 08:16:25.405 4672-4672/com.shinelon.rxjavalearn E/RxSymbol: defer : 1
    08-31 08:16:25.405 4672-4672/com.shinelon.rxjavalearn E/RxSymbol: defer : 2
    08-31 08:16:25.405 4672-4672/com.shinelon.rxjavalearn E/RxSymbol: defer : 3
    08-31 08:16:25.405 4672-4672/com.shinelon.rxjavalearn E/RxSymbol: defer : 4
    08-31 08:16:25.405 4672-4672/com.shinelon.rxjavalearn E/RxSymbol: defer : onComplete
     */

    /**
     * last 操作符仅取出可观察到的最后一个值，或者是满足某些条件的最后一项。
     */
    public void testLast(){
        Observable.just(1,2,3)
                .last(4)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "last : " + integer + "\n");
                    }
                });

    }
    /*
    08-31 08:32:29.380 11174-11174/? E/RxSymbol: last : 3
     */

    /**
     * merge 顾名思义，熟悉版本控制工具的你一定不会不知道 merge 命令，而在 Rx 操作符中，merge 的作用是把多个
     * Observable 结合起来，接受可变参数，也支持迭代器集合。注意它和 concat 的区别在于，不用等到 发射器 A 发送完
     * 所有的事件再进行发射器 B 的发送。
     */

    public void testMerge(){
        Observable.merge(Observable.just(1,2),Observable.just(3,4,5))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "accept: merge :" + integer + "\n" );
                    }
                });
    }
    /*
    08-31 08:37:06.464 13045-13045/com.shinelon.rxjavalearn E/RxSymbol: accept: merge :1
    08-31 08:37:06.464 13045-13045/com.shinelon.rxjavalearn E/RxSymbol: accept: merge :2
    08-31 08:37:06.465 13045-13045/com.shinelon.rxjavalearn E/RxSymbol: accept: merge :2
    08-31 08:37:06.465 13045-13045/com.shinelon.rxjavalearn E/RxSymbol: accept: merge :4
    08-31 08:37:06.465 13045-13045/com.shinelon.rxjavalearn E/RxSymbol: accept: merge :5
     */


    public void testReduce(){
        Observable.just(1,2,3)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.e(TAG, "accept: reduce : " + integer + "\n");
            }
        });
    }
    /*
    reduce((x,y) => x + y)
    reduce 操作符每次用一个方法处理一个值，可以有一个 seed 作为初始值。
    08-31 08:53:52.545 20747-20747/? E/RxSymbol: accept: reduce : 6
    可以看到，代码中，我们中间采用 reduce ，支持一个 function 为两数值相加，所以应该最后的值是：1 + 2 = 3 + 3 = 6
    ， 而Log 日志完美解决了我们的问题。
     */


    public void testScan(){
        Observable.just(1,2,3)
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.e(TAG, "accept: reduce : " + integer + "\n");
            }
        });
    }

    /*
    scan 操作符作用和上面的 reduce 一致，唯一区别是 reduce 是个只追求结果的坏人，而 scan 会始终如一地把每一个步骤都输出。
    08-31 09:12:24.473 30202-30202/com.shinelon.rxjavalearn E/RxSymbol: accept: reduce : 1
    08-31 09:12:24.473 30202-30202/com.shinelon.rxjavalearn E/RxSymbol: accept: reduce : 3
    08-31 09:12:24.473 30202-30202/com.shinelon.rxjavalearn E/RxSymbol: accept: reduce : 6
     */

    /**
     * 按照实际划分窗口，将数据发送给不同的 Observable
     */
    public void testWindown(){
        Observable.interval(1,TimeUnit.SECONDS)
                .take(15)
                .window(3,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Observable<Long>>() {
                    @Override
                    public void accept(Observable<Long> longObservable) throws Exception {
                        Log.e(TAG, "Sub Divide begin...\n");
                        longObservable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        Log.e(TAG, "Next:" + aLong + "\n");
                                    }
                                });
                    }
                });

    }

    /*
        08-31 09:39:57.481 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Sub Divide begin...
        08-31 09:39:58.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:0
        08-31 09:39:59.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:1
        08-31 09:40:00.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Sub Divide begin...
        08-31 09:40:00.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:2
        08-31 09:40:01.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:3
        08-31 09:40:02.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:4
        08-31 09:40:03.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Sub Divide begin...
        08-31 09:40:03.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:5
        08-31 09:40:04.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:6
        08-31 09:40:05.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:7
        08-31 09:40:06.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Sub Divide begin...
        08-31 09:40:06.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:8
        08-31 09:40:07.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:9
        08-31 09:40:08.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:10
        08-31 09:40:09.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Sub Divide begin...
        08-31 09:40:09.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:11
        08-31 09:40:10.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:12
        08-31 09:40:11.477 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:13
        08-31 09:40:12.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Sub Divide begin...
        08-31 09:40:12.476 10944-10944/com.shinelon.rxjavalearn E/RxSymbol: Next:14

     */
}
