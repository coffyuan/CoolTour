package com.heaven.wing.activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.heaven.wing.R;
import com.heaven.wing.entity.Note;
import com.heaven.wing.entity.Photo;
import com.heaven.wing.util.RecyclerBanner;

import java.io.Serializable;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener{

    RecyclerBanner pager;
    private TextView title,content;
    String sceneId,traceId;
    //得到当前用户的id
    private final static String USER_ID = BmobUser.getCurrentUser().getObjectId();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_details);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//返回箭头
//        setTitle("旅程信息");

        //根据景点及用户Id选取图片
        title = (TextView) findViewById(R.id.tv_address);
        content = (TextView) findViewById(R.id.tv_details);
        pager = (RecyclerBanner) findViewById(R.id.scroller_photo);

        sceneId = getIntent().getStringExtra("sceneId");
        traceId = getIntent().getStringExtra("traceId");

        //加载图片
        BmobQuery<Photo> query = new BmobQuery<>();
        query.addWhereEqualTo("sceneId", sceneId);
        query.setLimit(50);
        query.findObjects(new FindListener<Photo>() {
            @Override
            public void done(List<Photo> list, BmobException e) {
                if(e==null){
                    pager.setDatas(list);
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });

        //加载游记
        BmobQuery<Note> queryNote = new BmobQuery<>();
        queryNote.addWhereEqualTo("sceneId", sceneId);
//        queryNote.addWhereEqualTo("userId", "18370982633");
        queryNote.setLimit(50);
        queryNote.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if(e==null){
                    if(list != null && list.size() > 0) {
                        title.setText(list.get(0).getTitle() + " " + list.get(0).getTime());
                        content.setText(list.get(0).getNote());
                    }else {
                        title.setText("暂无游记");
                        content.setText(" ");
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });

        findViewById(R.id.pop_photo).setOnClickListener(this);
        findViewById(R.id.pop_note).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.pop_photo:
               queryPhoto(sceneId,v);
                break;
            case R.id.pop_note:
                queryNote(sceneId,v);
                break;
        }
    }

    public void queryPhoto(final String sceneId,final View v) {
        BmobQuery<Photo> query = new BmobQuery<>();
        query.addWhereEqualTo("sceneId", sceneId);//"7d2c673c61"
        query.setLimit(50);
        query.findObjects(new FindListener<Photo>() {
            @Override
            public void done(List<Photo> list, BmobException e) {
                if(e==null){
                    if(list != null && list.size() > 0) {
                        Intent photoIntent = new Intent(v.getContext(),PhotoActivity.class);
                        photoIntent.putExtra("photoList",(Serializable)list);
                        photoIntent.putExtra("sceneId",sceneId);
                        photoIntent.putExtra("traceId",traceId);
                        v.getContext().startActivity(photoIntent);
                        Log.i("Test",sceneId);
                    }else{
                        Intent intent = new Intent(v.getContext(),PhotoActivity.class);
                        intent.putExtra("traceId",traceId);
                        intent.putExtra("sceneId",sceneId);
                        startActivity(intent);
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }


    //根据景点Id获得查询是否已有游记，有则传游记的Id，无则传入景点的Id
    public void queryNote(final String sceneId,final View v) {
        BmobQuery<Note> query = new BmobQuery<>();
        query.addWhereEqualTo("sceneId", sceneId);
        query.setLimit(50);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if(e==null){
                    if(list != null  && list.size() > 0) {
                        Intent noteIntent = new Intent(v.getContext(),NoteActivity.class);
                        noteIntent.putExtra("noteId",list.get(0).getObjectId());//已有游记则将游记Id传出
                        v.getContext().startActivity(noteIntent);
                        Log.e("test","有游记");
                    }else {
                        Intent noteIntent = new Intent(v.getContext(),NoteActivity.class);
                        noteIntent.putExtra("sceneId",sceneId);//无游记则将景点Id传出，用来添加游记
                        v.getContext().startActivity(noteIntent);
                        Log.e("test","无游记");
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }
}
