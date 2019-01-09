package com.heaven.wing.entity;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by 刘康斌 on 2018/11/13.
 */

public class Scene extends BmobObject {
    /*
    * 景点实体类
    *
    *
    * 景点所在路线id
    * 用户id
    * 景点经纬度
    * 景点名称
    * 景点状态、是否被编辑过
    * */
    private String pathId;
    private String userId;
    private String sceneLatLng;
    private String sceneName;
    private String description;
    private boolean state = false;
    private BmobFile scenePic;

    public Scene(){
    }

    public BmobFile getScenePic() {
        return scenePic;
    }

    public void setScenePic(BmobFile scenePic) {
        this.scenePic = scenePic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean getState(){return this.state;};

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getSceneLatLng() {
        return sceneLatLng;
    }

    public void setSceneLatLng(String sceneLatLng) {
        this.sceneLatLng = sceneLatLng;
    }
}
