package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
                Log.d(TAG, "e complete");
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
                Log.e(TAG, "onError: ", t);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        };

        upStream.subscribe(downStream);
    }
    /*
    我们注意到这次和Observable有些不同. 首先是创建Flowable的时候增加了一个参数, 这个参数是用来选择背压,
    也就是出现上下游流速不均衡的时候应该怎么处理的办法, 这里我们直接用BackpressureStrategy.ERROR这种方式,
    这种方式会在出现上下游流速不均衡的时候直接抛出一个异常,这个异常就是著名的MissingBackpressureException.
    其余的策略后面再来讲解.

    另外的一个区别是在下游的onSubscribe方法中传给我们的不再是Disposable了, 而是Subscription, 它俩有什么区别呢,
    首先它们都是上下游中间的一个开关, 之前我们说调用Disposable.dispose()方法可以切断水管,
    同样的调用Subscription.cancel()也可以切断水管, 不同的地方在于Subscription增加了一个void request(long n)方法,
    这个方法有什么用呢, 在上面的代码中也有这么一句代码:
     s.request(Long.MAX_VALUE);

    从运行结果中可以看到, 在上游发送第一个事件之后, 下游就抛出了一个著名的MissingBackpressureException异常,
    并且下游没有收到任何其余的事件. 可是这是一个同步的订阅呀, 上下游工作在同一个线程,
    上游每发送一个事件应该会等待下游处理完了才会继续发事件啊, 不可能出现上下游流速不均衡的问题呀.

    这是因为Flowable在设计的时候采用了一种新的思路也就是响应式拉取的方式来更好的解决上下游流速不均衡的问题,
    与我们之前所讲的控制数量和控制速度不太一样, 这种方式用通俗易懂的话来说就好比是叶问打鬼子, 我们把上游看成小日本,
    把下游当作叶问, 当调用Subscription.request(1)时, 叶问就说我要打一个! 然后小日本就拿出一个鬼子给叶问, 让他打,
    等叶问打死这个鬼子之后, 再次调用request(10), 叶问就又说我要打十个! 然后小日本又派出十个鬼子给叶问,
    然后就在边上看热闹, 看叶问能不能打死十个鬼子, 等叶问打死十个鬼子后再继续要鬼子接着打...

    所以我们把request当做是一种能力, 当成下游处理事件的能力, 下游能处理几个就告诉上游我要几个, 这样只要上游根据
    下游的处理能力来决定发送多少事件, 就不会造成一窝蜂的发出一堆事件来, 从而导致OOM. 这也就完美的解决之前我们所
    学到的两种方式的缺陷, 过滤事件会导致事件丢失, 减速又可能导致性能损失. 而这种方式既解决了事件丢失的问题,
    又解决了速度的问题, 完美 !

    但是太完美的东西也就意味着陷阱也会很多, 你可能只是被它的外表所迷惑, 失去了理智, 如果你滥用或者不遵守规则,
    一样会吃到苦头.

    比如这里需要注意的是, 只有当上游正确的实现了如何根据下游的处理能力来发送事件的时候, 才能达到这种效果, 如果上游
    根本不管下游的处理能力, 一股脑的瞎他妈发事件, 仍然会产生上下游流速不均衡的问题, 这就好比小日本管他叶问要打几个,
    老子直接拿出1万个鬼子, 这尼玛有种打死给我看看? 那么如何正确的去实现上游呢, 这里先卖个关子, 之后我们再来讲解.

    学习了request, 我们就可以解释上面的两段代码了.

    首先第一个同步的代码, 为什么上游发送第一个事件后下游就抛出了MissingBackpressureException异常, 这是因为下游
    没有调用request, 上游就认为下游没有处理事件的能力, 而这又是一个同步的订阅, 既然下游处理不了, 那上游不可能一直等待吧,
    如果是这样, 万一这两根水管工作在主线程里, 界面不就卡死了吗, 因此只能抛个异常来提醒我们. 那如何解决这种情况呢,
    很简单啦, 下游直接调用request(Long.MAX_VALUE)就行了, 或者根据上游发送事件的数量来request就行了, 比如这里
    request(3)就可以了.

    然后我们再来看看第二段代码, 为什么上下游没有工作在同一个线程时, 上游却正确的发送了所有的事件呢? 这是因为在Flowable
    里默认有一个大小为128的水缸, 当上下游工作在不同的线程中时, 上游就会先把事件发送到这个水缸中, 因此, 下游虽然没有调用
    request, 但是上游在水缸中保存着这些事件, 只有当下游调用request时, 才从水缸里取出事件发给下游.

    是不是这样呢, 我们来验证一下:

     */

    private static Subscription mSubscription;

    public static void request(long n) {
        mSubscription.request(n); //在外部调用request请求上游
    }

    public static void testFlowableRequest() {
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
                        mSubscription = s;  //把Subscription保存起来
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


    /**
     * 刚刚我们有说到水缸的大小为128, 有朋友就问了, 你说128就128吗, 又不是唯品会周年庆, 我不信. 那就来验证一下:
     */

    public void testFlowable128() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 128; i++) { //这里可以改成129试一试, 出现 MissingBackpressureException
                    //可以看出默认事128个事件的
                    Log.d(TAG, "e " + i);
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

    /**
     * 想要发送更多事件
     */
    public void testBackPressBuffer() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 1000; i++) {
                    Log.d(TAG, "e " + i);
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

    /**
     * 从上可以看出,上面的可以发送很多事件 FLowable和Observable一样的,
     * 时的FLowable表现出来的特性的确和Observable一模一样, 因此, 如果你像这样单纯的使用FLowable,
     * 同样需要注意OOM的问题, 例如下面这个例子:
     */

    public void testBackPressFor() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0;;i++){
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

    public void testBackPressDrop() {  //drop 有放弃的意思 , 就是会存一个事件 , 当你取走的时候会拿到 刚刚发过来的现在的 128个事件
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0;;i++){
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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



    public void testBackPressLatest() {  //drop 有放弃的意思 , 就是会存一个事件 , 当你取走的时候会拿到 刚刚发过来的现在的 128个事件
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0;;i++){
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

    /**
     * 诶, 看上去好像和Drop差不多啊, Latest也首先保存了0-127这128个事件, 等下游把这128个事件处理了之后才进行
     * 之后的处理, 光从这里没有看出有任何区别啊...
     * 作为初学者的入门导师, 是不能给大家留下一点点疑惑的, 来让我们继续揭开这个疑问.
     * 我们把上面两段代码改良一下, 先来看看DROP的改良版:
     */


    public void testBackPressDrop10000() {  //drop 有放弃的意思 , 就是会存一个事件 , 当你取走的时候会拿到 刚刚发过来的现在的 128个事件
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0;i<10000;i++){
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(128);  //一开始就处理掉128个事件
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

    /**
     * 测试的结果发现drop 只能拿到最后 再拿酒没有了
     * 从运行结果中可以看到, 除去前面128个事件, 与Drop不同,
     * Latest总是能获取到最后最新的事件, 例如这里我们总是能获得最后一个事件9999.
     */

    public void testBackPressLatest10000() {  //drop 有放弃的意思 , 就是会存一个事件 , 当你取走的时候会拿到 刚刚发过来的现在的 128个事件
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0;i<10000;i++){
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(128);
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

    /**
     * 好了, 关于FLowable的策略我们也讲完了, 有些朋友要问了, 这些FLowable是我自己创建的, 所以我可以选择策略,
     * 那面对有些FLowable并不是我自己创建的, 该怎么办呢? 比如RxJava中的interval操作符, 这个操作符并不是我们自
     * 己创建的, 来看下面这个例子吧:
     作者：Season_zlc
     链接：https://www.jianshu.com/p/a75ecf461e02
     來源：简书
     简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
     */
    public void testInterval(){
        Flowable.interval(1, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d(TAG, "onNext: " + aLong);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
     * 一运行就抛出了MissingBackpressureException异常, 提醒我们发太多了, 那么怎么办呢, 这个又不是我们自己创建的FLowable啊...

     别慌, 虽然不是我们自己创建的, 但是RxJava给我们提供了其他的方法:

     onBackpressureBuffer()
     onBackpressureDrop()
     onBackpressureLatest()

     熟悉吗? 这跟我们上面学的策略是一样的, 用法也简单, 拿刚才的例子现学现用:

     */

    public void testIntervalBack(){
        Flowable.interval(1, TimeUnit.MICROSECONDS)
                .onBackpressureDrop()   //添加背压策略
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d(TAG, "onNext: " + aLong);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
     * interval操作符发送Long型的事件, 从0开始, 每隔指定的时间就把数字加1并发送出来,
     * 在这个例子里, 我们让它每隔1毫秒就发送一次事件, 在下游延时1秒去接收处理, 不用猜也知道结果是什么:
     *
     *
     * 通过测试这个两个方法 添加背压的新的方式
     */




}
