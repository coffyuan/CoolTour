package com.heaven.wing.entity;

import android.content.Context;

import java.io.File;
import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by 刘康斌 on 2018/11/13.
 */

public class Photo extends BmobObject implements Serializable{
    /*
    * 照片Id
    * 照片所对应的景点Id
    * 照片路径
    * */
    private String userId;
    private String traceId;
    private String sceneId;
    private BmobFile photo;
    private boolean isSelect;

    public Photo() {

    }

    public String getTraceId() {
        return traceId;
    }
    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Photo(String path){
        this.photo = new BmobFile(new File(path));
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

    public BmobFile getPhoto() {
        return photo;
    }

    public void setPhoto(BmobFile photo) {
        this.photo = photo;
    }

    public int getImageResourceId(Context context )
    {
        try
        {
            return context.getResources().getIdentifier(this.userId, "drawable", context.getPackageName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
}
