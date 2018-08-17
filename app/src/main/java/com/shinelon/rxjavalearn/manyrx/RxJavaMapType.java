package com.shinelon.rxjavalearn.manyrx;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by pateo on 18-8-17.
 */

public class RxJavaMapType {
    public static final String TAG = RxJavaMapType.class.getSimpleName();

    public void symbolMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).map(new Function<Integer, String>() {  //在这里进行变换,前变后
            @Override
            public String apply(Integer integer) throws Exception {
                return "This is result " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
            }
        });
    }
    /*
    通过Map, 可以将上游发来的事件转换为任意的类型, 可以是一个Object, 也可以是一个集合, 如此强大的操作符你难道不想试试

     接口Function A functional interface that takes a value and returns another value, possibly with a
     different type and allows throwing a checked exception.
     泛型前面的参数 是传入, 后面的是返回
     */

    //flat 是平的意思
    public void symbolFlatMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i=0;i<3;i++){
                    list.add("I am value " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG,s);
            }
        });
    }

    /*
    FlatMap将一个发送事件的上游Observable变换为多个发送事件的Observables，
    然后将它们发射的事件合并后放进一个单独的Observable里.上游每发送一个事件, flatMap都将创建一个新的水管,
    然后发送转换之后的新的事件, 下游接收到的就是这些新的水管发送的数据. 这里需要注意的是, flatMap并不保证事件的顺序,
    也就是图中所看到的, 并不是事件1就在事件2的前面. 如果需要保证顺序则需要使用concatMap.
     */

    //concat 函数, 合并意思
    public void symbolConcatMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i=0;i<3;i++){
                    list.add("I am value " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG,s);
            }
        });
    }

}
