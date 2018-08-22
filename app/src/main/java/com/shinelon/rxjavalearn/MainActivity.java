package com.shinelon.rxjavalearn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shinelon.rxjavalearn.manyrx.RxJava01;
import com.shinelon.rxjavalearn.manyrx.RxJavaFlowable;
import com.shinelon.rxjavalearn.manyrx.RxJavaFlowableTwo;
import com.shinelon.rxjavalearn.manyrx.RxJavaMapType;
import com.shinelon.rxjavalearn.manyrx.RxJavaPluginUnit;
import com.shinelon.rxjavalearn.manyrx.RxJavaThreadControl;
import com.shinelon.rxjavalearn.manyrx.RxjavaZip;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RxJava01 rxJava01;
    private RxJavaThreadControl rxJavaThreadControl;
    private RxJavaMapType rxJavaMapType;
    private RxjavaZip rxjavaZip;
    private RxJavaFlowable rxJavaFlowable;
    private RxJavaFlowableTwo rxJavaFlowableTwo;
    private RxJavaPluginUnit rxJavaPluginUnit;

    private Button button1,button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        //LogUtils.d(TAG,"nihao");
        rxJava01 = new RxJava01();
        rxJavaThreadControl = new RxJavaThreadControl();
        rxJavaMapType = new RxJavaMapType();
        rxjavaZip = new RxjavaZip();
        rxJavaFlowable = new RxJavaFlowable();
        rxJavaFlowableTwo = new RxJavaFlowableTwo();
        rxJavaPluginUnit = new RxJavaPluginUnit();
        /*rxJavaPluginUnit.testSetOnMaybeSubscribe();  //这个方法进行重新的修改 插入方法
        rxJavaPluginUnit.testPlugin();*/  //这两个要放到一起来测试
        rxJavaPluginUnit.testSetOnMaybeSubscribe();
        rxJavaPluginUnit.testMaybe();

        testFlowableDorp();
    }

    private void testFlowableDorp() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //rxJavaFlowable.testBackPressLatest10000();
                //rxJavaFlowableTwo.request();
                //RxJavaFlowableTwo.main();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxJavaFlowable.request(128);//每次请求多少事件,testBackPressDrop 请求128个事件
            }                                //每次请求多少事件,testBackPressLatest 请求128个事件
        });
    }



    private void testFlowableSymbol() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxJavaFlowable.testFlowableRequest();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxJavaFlowable.request(1);//每次请求多少事件,testBackPressDrop 请求128个事件
            }                                //每次请求多少事件,testBackPressLatest 请求128个事件
        });
    }
}
