package com.heaven.wing.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.heaven.wing.R;
import com.heaven.wing.entity.User;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private TextView tvUsername;
    private EditText etPassword;
    private SharedPreferences pref;
    private CheckBox rememberPass;
    private SharedPreferences.Editor editor;
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        rememberPass = (CheckBox)findViewById(R.id.remember_pass);
        boolean isRemember = pref.getBoolean("remember_password",false);

        tvUsername = (TextView) findViewById(R.id.login_username);
        etPassword = (EditText) findViewById(R.id.login_password);
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.register_button).setOnClickListener(this);

        if(isRemember){
            String username = pref.getString("username","");
            String password = pref.getString("password","");
            tvUsername.setText(username);
            etPassword.setText(password);
            rememberPass.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                String username = tvUsername.getText().toString();
                String password = etPassword.getText().toString();
                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this,"账号或密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.login(new SaveListener<User>() {
                    @Override
                    public void done(User user, BmobException e) {
                        if(e == null){
                            editor = pref.edit();
                            if(rememberPass.isChecked()){
                                editor.putBoolean("remember_password",true);
                                editor.putString("username",user.getUsername());
                                editor.putString("password",etPassword.getText().toString() );
                            }else{
                                editor.clear();
                            }
                            editor.apply();
                            intent.setClass(getApplicationContext(),MainActivity2.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this,"账号或密码错误",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.register_button:
                intent.setClass(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }
}