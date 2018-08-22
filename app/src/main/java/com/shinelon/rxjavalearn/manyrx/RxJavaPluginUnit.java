package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import com.shinelon.rxjavalearn.MainActivity;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by pateo on 18-8-22.
 */

public class RxJavaPluginUnit {
    public static final String TAG = "TAG";

    public void testPlugin() {
        Maybe.just(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "Real onSuccess");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "Real onError");
                    }
                });
    }


    //说明: 下面有些地方要点进去追踪
   /* 运行的结果就是：

    zlc.season.javademo D/Main2Activity: Real onSuccess

    看过之前的教程的都知道这个subscribe()方法是个很重要的方法啦，那我们就来看看这个方法到底干了啥！

    之前说过，subscribe方法有多个重载的方法，通过源码得知，这些重载的方法最后都会调用到其中的一个subscribe方法中：

    public final void subscribe(MaybeObserver<? super T> observer) {
        ObjectHelper.requireNonNull(observer, "observer is null");

        observer = RxJavaPlugins.onSubscribe(this, observer);

        ObjectHelper.requireNonNull(observer, "observer returned by the RxJavaPlugins hook is null");

        try {
            subscribeActual(observer);
        } catch (NullPointerException ex) {...
        } catch (Throwable ex) {...}
    }

    通过这个源码我们一下子就找到了一行关键的代码：

    observer = RxJavaPlugins.onSubscribe(this, observer);

    先简单解释一下，这里的this就是当前的Maybe对象，也就是我们的上游，这里的observer就是我们的下游。

    这意味着什么呢，意味着RxJavaPlugins对我们的subscribe方法做了一个骚操作呀！



    这样我们一下子就找到了RxJavaPlugins和调用链之间的联系，接下来就需要顺藤摸瓜，更加深入的了解一下，来看一下
    RxJavaPlugins.onSubscribe()的源码吧：

    //为了便于理解，把源码中的范型去掉了
    public final class RxJavaPlugins {
        ...
        static volatile BiFunction onMaybeSubscribe;
        ...
        public static void setOnMaybeSubscribe(BiFunction onMaybeSubscribe) {
            RxJavaPlugins.onMaybeSubscribe = onMaybeSubscribe;
        }
        ...
        //source就是我们的上游，observer就是我们的下游
        public static  MaybeObserver onSubscribe(Maybe source, MaybeObserver observer) {
            BiFunction f = onMaybeSubscribe;
            if (f != null) {     //如果onMaybeSubscribe不为空
                return apply(f, source, observer); //调用apply方法创建一个新的下游
            }
            return observer;
        }
        ...
        static MaybeObserver apply(BiFunction f, Maybe source, MaybeObserver observer) {
            return f.apply(source, observer);
        }
    }

    这个代码简直不能再清晰了，大概就是如果我调用了setOnMaybeSubscribe()设置了一个BiFunction类型的变量
    onMaybeSubscribe，那么当我调用subscribe()方法的时候就会调用这个变量的apply()方法来做一个骚操作返回一个新的下游，
    否则就原封不动的把原来的下游返回。

    这就给了我们无限的想象力啊，我们可以通过这个apply()方法直接把原本的下游返回，这样就什么也不做，也可以包装一下原来
    的下游，在真正的下游的方法执行前后插入一些自己的操作，哇哦，好像很厉害的样子。。。

    那既然要包装，首先肯定得有一个包装类：
    WrapDownStreamObserver

    这就是一个简单的包装类了，它和下游都是同样的类型，并且内部持有真正的下游，我们在真正的下游方法调用前都插入了一条日志。

    有了包装类，那么我们就可以调用RxJavaPlugins的setOnMaybeSubscribe()方法来做骚操作了：
    */

    public void testSetOnMaybeSubscribe() {
        RxJavaPlugins.setOnMaybeSubscribe(new BiFunction<Maybe, MaybeObserver, MaybeObserver>() {
            @Override
            public MaybeObserver apply(Maybe maybe, MaybeObserver maybeObserver) throws Exception {
                return new WrapDownStreamObserver(maybeObserver);//这个maybeObserver就是我们真正的下游
            }
        });
    }

    //这样的操作才能看出效果,先用rxjavaplugins 进行修改 , 把包装类传入 , 在调用 maybe 方法
   /*
    rxJavaPluginUnit.testSetOnMaybeSubscribe();
    rxJavaPluginUnit.testPlugin();

    log日志:
    zlc.season.javademo D/Main2Activity: Hooked onSuccess
    zlc.season.javademo D/Main2Activity: Real onSuccess

    哈哈，果不其然，不愧是骚操作！！果然在真正的下游执行前先去执行了包装类里的代码，似乎已经看见了胜利的曙光！！

     不过刚才的是在同一个线程的代码，我们再来一个带有线程切换的代码验证一下：
    */

   public void testMaybe(){
       Maybe.just(1)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<Integer>() {
                   @Override
                   public void accept(Integer integer) throws Exception {
                       Log.d(TAG, "Real onSuccess");
                   }
               }, new Consumer<Throwable>() {
                   @Override
                   public void accept(Throwable throwable) throws Exception {
                       Log.d(TAG, "Real onError");
                   }
               });
   }
   /*
        rxJavaPluginUnit.testSetOnMaybeSubscribe();
        rxJavaPluginUnit.testMaybe();

        log:
        com.shinelon.rxjavalearn D/TAG: Hooked onSuccess
        com.shinelon.rxjavalearn D/TAG: Hooked onSuccess
        com.shinelon.rxjavalearn D/TAG: Hooked onSuccess
        com.shinelon.rxjavalearn D/TAG: Real onSuccess
        包装了一个下游却打印了 3 次,
这个问题要详细的解释清楚估计得花一段时间了，这里就直接给出答案了，因为我们使用RxJavaPlugins的setOnMaybeSubscribe()
方法实际上是给所有的Maybe类型的subscribe()方法都做了一个骚操作，而在我们的RxJava调用链中，除了我们的上游和下游，
其实还有中游，这些中游位于RxJava的内部，我们每做一次链式调用，都会生成一个新的中游，因此我们的骚操作不仅仅只对下游生效，
对这些中游也会生效，所以出现上面的打印结果。从代码也可以看出来，我们分别调用了一次subscribeOn和一次observeOn，
因此对应的产生了两个中游，再加上我们自己的下游，所以一共打印三次Hooked onSuccess也说得通。

但是尽管打印了这么多，我们还是可以从中看到，我们的骚操作依然是有效的，在真正的下游方法执行前，依然执行了包装类中的代码，所以我们的这个方案是完全可行的，只需要避免一下重复处理就可以了。

看到这里，广大吃瓜群众估计还是处于一脸懵逼的状态。。。这TM跟我处理API错误有啥关系？
    */
}
