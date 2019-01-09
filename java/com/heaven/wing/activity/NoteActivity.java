package com.heaven.wing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.heaven.wing.R;
import com.heaven.wing.entity.Note;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by 刘康斌 on 2018/11/8.
 */

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView titleTV, timeTV, contentTV;
    String sceneId,noteId;

    //得到当前用户的id
    private final static String USER_ID = BmobUser.getCurrentUser().getObjectId();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle("游记");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        getLayoutInflater().inflate(R.layout.app_save_toobar, toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//返回箭头

        titleTV = (TextView) findViewById(R.id.titleET);
        timeTV = (TextView) findViewById(R.id.timeET);
        contentTV = (TextView) findViewById(R.id.contentET);

        Intent intent = getIntent();
        sceneId = intent.getStringExtra("sceneId");
        noteId = intent.getStringExtra("noteId");
        if(noteId != null) {//数据库里含有游记数据，则取出数据布置到面板上
            queryNote(noteId);
        }else if(sceneId != null){//数据库里没有有游记数据
            titleTV.setText("");
            timeTV.setText("");
            contentTV.setText("");
        }

        //保存数据
        findViewById(R.id.save_bt).setOnClickListener(this);
    }

    //游记查询
    public void queryNote(String noteId) {
        BmobQuery<Note> query = new BmobQuery<>();
        query.getObject(noteId, new QueryListener<Note>() {
            @Override
            public void done(Note Note, BmobException e) {
                if (e == null) {
                    titleTV.setText(Note.getTitle());
                    timeTV.setText(Note.getTime());
                    contentTV.setText(Note.getNote());
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        final String title = titleTV.getText().toString();
        final String time = timeTV.getText().toString();
        final String content = contentTV.getText().toString();
        if(noteId != null) {//如果noteId不为空即为修改游记
            Note note = new Note();
            note.setTitle(title);
            note.setTime(time);
            note.setNote(content);
            note.setUserId(USER_ID);
            note.update(noteId,new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e == null){
                        Toast.makeText(NoteActivity.this,"修改成功", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(NoteActivity.this,"修改失败" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {//否则为添加游记
            Note note = new Note();
            note.setTitle(title);
            note.setTime(time);
            note.setSceneId(sceneId);
            note.setNote(content);
            note.setUserId(USER_ID);
            note.save(new SaveListener<String>() {
                @Override
                public void done(String objectId,BmobException e) {
                    if(e == null){
                        Toast.makeText(NoteActivity.this,"添加成功", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(NoteActivity.this,"添加失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
