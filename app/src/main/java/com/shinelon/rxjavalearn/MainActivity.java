package com.shinelon.rxjavalearn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shinelon.rxjavalearn.manyrx.RxJava_1;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RxJava_1 rxJava_1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //LogUtils.d(TAG,"nihao");
        rxJava_1 = new RxJava_1();
        rxJava_1.testInteger();
    }
}
