package com.heaven.wing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.heaven.wing.R;
import com.heaven.wing.entity.User;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

/**
 * 启动app，加载相关资源
 */
public class LaunchActivity extends AppCompatActivity {

    private BmobUser bmobUser;
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        Handler x = new Handler();//定义一个handle对象
        x.postDelayed(new SplashHandler(), 2000);//设置2秒钟延迟执行splashhandler线程。其实你这里可以再新建一个线程去执行初始化工作，如判断SD,网络状态等

        Bmob.initialize(this,"5e7a422c02ecdd389a1fb9905f523da6");
        bmobUser = BmobUser.getCurrentUser(User.class);
    }

    class SplashHandler implements Runnable{
        public void run() {
            if(bmobUser != null){
                intent.setClass(getApplicationContext(),MainActivity2.class);
            }else{
                intent.setClass(getApplicationContext(),LoginActivity.class);
            }
            startActivity(intent);
            LaunchActivity.this.finish();// 把当前的LaunchActivity结束掉
        }
    }
}
