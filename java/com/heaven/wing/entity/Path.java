package com.heaven.wing.entity;

import android.content.Context;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by 刘康斌 on 2018/11/12.
 */

public class Path extends BmobObject implements Serializable {
    /*
    * 自增Id
    * 路线Id
    * 路线名称
    * */
    private String pathId;
    private String sceneId;
    private String title;

    public Path() {

    }

    public Path(String pathId, String title) {
        super();
        this.pathId = pathId;
        this.title = title;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageResourceId(Context context )
    {
        try
        {
            return context.getResources().getIdentifier(this.pathId, "drawable", context.getPackageName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
}
