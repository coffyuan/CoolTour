package com.heaven.wing.entity;

import cn.bmob.v3.BmobObject;


/**
 * Created by Eric on 2018/11/19.
 */

public class ViewMarker extends BmobObject {

    public ViewMarker(){}

    private String userId;

    private String position;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
