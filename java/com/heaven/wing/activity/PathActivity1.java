//package com.heaven.wing.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import com.heaven.wing.R;
//import com.heaven.wing.entity.Path;
//import com.heaven.wing.util.PopupWindows;
//
///**
// * Created by 刘康斌 on 2018/11/9.
// */
//
//public class PathActivity1 extends AppCompatActivity {
//    private PopupWindows popupWindows;
//    private ImageView path;
//    private TextView title;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_path);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//返回箭头
//        setTitle("路线");
//
//        path = (ImageView) findViewById(R.id.myPath);
//        title = (TextView) findViewById(R.id.myTitle);
//
//        Intent intent = getIntent();
//        Path item = (Path) intent.getSerializableExtra("path");
//        path.setImageDrawable(PathActivity.this.getDrawable(item.getImageResourceId(PathActivity.this)));
//        title.setText(item.getTitle());
//
//        path.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow();
//            }
//        });
//    }
//
//    public void showPopupWindow() {
//        popupWindows = new PopupWindows(PathActivity.this);
//        View rootView = LayoutInflater.from(PathActivity.this).inflate(R.layout.activity_path, null);
//        popupWindows.showAtLocation(rootView,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
//    }
//}
