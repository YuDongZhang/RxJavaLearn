package com.shinelon.rxjavalearn.TwoRx;


import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    public void testTimer(){
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

    public void testInterval(){
        Log.d(TAG, TimeUtils.getNowString());
        Observable.interval(3,2,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                    }
                });
    }

}
