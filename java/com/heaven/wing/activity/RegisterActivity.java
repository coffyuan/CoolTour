package com.heaven.wing.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.heaven.wing.R;
import com.heaven.wing.entity.User;

import java.util.regex.Pattern;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvUsername;
    private EditText etPassword;
    private TextView tvPhone;
    private TextView etCode;
    private Button btn_code;
    private TimeCount mTimeCount;
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        tvUsername = (TextView) findViewById(R.id.register_username);
        etPassword = (EditText) findViewById(R.id.register_password);
        tvPhone = (TextView) findViewById(R.id.register_phone);
        etCode = (EditText) findViewById(R.id.register_code);

        findViewById(R.id.register_button).setOnClickListener(this);

        //短信验证
        btn_code = (Button)findViewById(R.id.bt_code);
        btn_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobSMS.requestSMSCode(tvPhone.getText().toString(),"酷行天下",new QueryListener<Integer>(){
                    @Override
                    public void done(Integer integer, BmobException e) {
                        if(e == null){
                            System.out.println("发送验证码成功，短信ID:" + integer);
                            mTimeCount = null;
                            mTimeCount = new TimeCount(60 * 1000, 1000);
                            mTimeCount.start();
                        }else{
                            System.out.println("发送验证码失败:" + e.getErrorCode() + "-" + e.getMessage());
                            btn_code.setText("重新获取");
                        }
                    }
                });


            }
        });

    }

    @Override
    public void onClick(View v) {
        final String username = tvUsername.getText().toString();
        final String password = etPassword.getText().toString();
        final String code = etCode.getText().toString();
        final String phone = tvPhone.getText().toString();

        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"用户名或密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(!Pattern.compile("^[A-Za-z0-9]+$").matcher(username).matches() ||
                username.length()<6 || username.length() > 12){
            Toast.makeText(this,"用户名或密码只能为6至12位英文或数字",Toast.LENGTH_SHORT).show();
            return;
        }else if (!Pattern.compile("1[345789][0-9]{9}").matcher(phone).matches()){
            Toast.makeText(this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
            return;
        }else if (code.length()<1){
            Toast.makeText(this,"验证码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else{
            BmobSMS.verifySmsCode(phone, code, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        User user = new User();
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setMobilePhoneNumber(phone);
                        user.signUp(new SaveListener<User>() {
                            @Override
                            public void done(User user, BmobException e) {
                                if(e == null){
                                    Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                                    intent.setClass(getApplicationContext(),LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        System.out.println("验证码验证失败：" + e.getErrorCode() + "-" + e.getMessage());
                        Toast.makeText(getApplicationContext(),"验证码错误",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * 实现倒计时的类
     */
    private class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * 计时过程显示 按钮不可用 设置为灰色
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onTick(long millisUntilFinished) {
            btn_code.setClickable(false);
            btn_code.setBackground(getResources().getDrawable(R.drawable.btn_waitting_bg));
            btn_code.setTextColor(getResources().getColor(R.color.black));
            //btn_code.setBackgroundColor(getResources().getColor(R.color.colorBtn));
            btn_code.setText("(" + millisUntilFinished / 1000 + ")秒后重试");
        }

        /**
         * 计时结束调用
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onFinish() {
            btn_code.setClickable(true);
            btn_code.setText("获取验证码");
            btn_code.setBackground(getResources().getDrawable(R.drawable.btn_bg));
            btn_code.setTextColor(getResources().getColor(R.color.white));
            //btn_code.setBackgroundColor(getResources().getColor(R.color.colorBtn));
        }
    }
}
