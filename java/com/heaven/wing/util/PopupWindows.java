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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.heaven.wing.R;
import com.heaven.wing.activity.DetailsActivity;
import com.heaven.wing.entity.Scene;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class PopupWindows extends PopupWindow{

    private View mView;
    public TextView title;
    public TextView titleInfo;

    public PopupWindows(final Activity context, final Scene scene) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.main_popupwindow, null);

        title = (TextView) mView.findViewById(R.id.tv_popupwindow);
        titleInfo = (TextView) mView.findViewById(R.id.vt_popupwindow);

        title.setText(scene.getSceneName());
        titleInfo.setText(scene.getDescription());

        Button details = (Button)mView.findViewById(R.id.btn_details);
        Button delete_Scene_bt = (Button)mView.findViewById(R.id.btn_delete_scene);

        details.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("sceneId",scene.getObjectId());
                intent.putExtra("traceId",scene.getPathId());
                v.getContext().startActivity(intent);
                dismiss();
            }
        });

        delete_Scene_bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sceneId = scene.getObjectId();
                Scene sceneDelete = new Scene();
                sceneDelete.setObjectId(sceneId);
                sceneDelete.delete(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e == null){
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                            dismiss();
                        }else{
                            Toast.makeText(context,"删除失败",Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }
                });
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

