package com.heaven.wing.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.heaven.wing.R;
import com.heaven.wing.activity.DetailsActivity;


public class FinishProjectPopupWindows extends PopupWindow{

    private View mView;
    public TextView tv_popupwindow;
    public EditText vt_popupwindow;
    public Button btn_details;
    private Intent intent;

    public FinishProjectPopupWindows(Activity context, OnClickListener itemsOnClick) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.main_popupwindow, null);

        tv_popupwindow = (TextView) mView.findViewById(R.id.tv_popupwindow);
        btn_details = (Button) mView.findViewById(R.id.btn_details);

        // 设置按钮监听
        btn_details.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

               intent = new Intent(v.getContext(), DetailsActivity.class);
                v.getContext().startActivity(intent);
                //销毁弹框
                dismiss();
            }
        });



        //设置PopupWindow的View
        this.setContentView(mView);
        //设置PopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置PopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置PopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x84111111);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }
}

