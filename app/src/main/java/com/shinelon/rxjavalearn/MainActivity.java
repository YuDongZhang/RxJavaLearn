package com.shinelon.rxjavalearn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shinelon.rxjavalearn.manyrx.RxJava01;
import com.shinelon.rxjavalearn.manyrx.RxJavaFlowable;
import com.shinelon.rxjavalearn.manyrx.RxJavaMapType;
import com.shinelon.rxjavalearn.manyrx.RxJavaThreadControl;
import com.shinelon.rxjavalearn.manyrx.RxjavaZip;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RxJava01 rxJava01;
    private RxJavaThreadControl rxJavaThreadControl;
    private RxJavaMapType rxJavaMapType;
    private RxjavaZip rxjavaZip;
    private RxJavaFlowable rxJavaFlowable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //LogUtils.d(TAG,"nihao");
        rxJava01 = new RxJava01();
        rxJavaThreadControl = new RxJavaThreadControl();
        rxJavaMapType = new RxJavaMapType();
        rxjavaZip = new RxjavaZip();
        rxJavaFlowable = new RxJavaFlowable();
        rxJavaFlowable.testFlowable();

    }
}
