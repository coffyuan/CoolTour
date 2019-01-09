package com.heaven.wing.entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by Eric on 2018/11/15.
 */

public class Trace extends BmobObject {
    private String mStartPoint;
    private String mEndPoint;
    private String mPathLinePoints;
    private float mDistance;
    private String mDuration;
    private String mAveragespeed;
    private String mDate;
    private String userId;
    private String city;

    public Trace() {

    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getStartpoint() {
        return mStartPoint;
    }

    public void setStartpoint(String startpoint) {
        this.mStartPoint = startpoint;
    }

    public String getEndpoint() {
        return mEndPoint;
    }

    public void setEndpoint(String endpoint) {
        this.mEndPoint = endpoint;
    }

    public String getPathline() {
        return mPathLinePoints;
    }

    public void setPathline(String pathline) {
        this.mPathLinePoints = pathline;
    }

    public float getDistance() {
        return mDistance;
    }

    public void setDistance(float distance) {
        this.mDistance = distance;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public String getAveragespeed() {
        return mAveragespeed;
    }

    public void setAveragespeed(String averagespeed) {this.mAveragespeed = averagespeed;}

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }


}
