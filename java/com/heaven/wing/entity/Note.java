package com.heaven.wing.entity;

import android.content.Context;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by 刘康斌 on 2018/11/7.
 */

public class Note extends BmobObject implements Serializable{
    /*
    * 游记Id
    * 游记封面图片Id
    * 游记标题
    * 编写游记时间
    * 游记内容
    * */
    private String userId;
    private String sceneId;
    private String imageName;
    private String title;
    private String time;
    private String note;
    private boolean isSelect;

    public Note() {

    }

    public Note(String imageName, String title, String time, String note) {
        this.imageName = imageName;
        this.title = title;
        this.time = time;
        this.note = note;
    }



    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public int getImageResourceId(Context context )
    {
        try
        {
            return context.getResources().getIdentifier(this.imageName, "drawable", context.getPackageName());

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Note{" +
                ", userId='" + userId + '\'' +
                ", sceneId='" + sceneId + '\'' +
                ", imageName='" + imageName + '\'' +
                ", title='" + title + '\'' +
                ", time='" + time + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
