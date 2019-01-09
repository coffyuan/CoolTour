package com.heaven.wing.entity;

import com.amap.api.location.AMapLocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于记录一条轨迹，包括起点、终点、轨迹中间点、距离、耗时、平均速度、时间
 * 
 * @author ligen
 * 
 */
public class PathRecord implements Serializable {
	private AMapLocation mStartPoint;
	private AMapLocation mEndPoint;
	private List<AMapLocation> mPathLinePoints = new ArrayList<AMapLocation>();
	private String mDistance;
	private String mDuration;
	private String mAveragespeed;
	private String mDate;
	private String mId;
	private String city;
	private String userId;
	private boolean isSelect;

	public PathRecord() {

	}

	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCity(){
		return city;
	}
	public void setCity(String city){
		this.city = city;
	}
	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public AMapLocation getStartpoint() {
		return mStartPoint;
	}

	public void setStartpoint(AMapLocation startpoint) {
		this.mStartPoint = startpoint;
	}

	public AMapLocation getEndpoint() {
		return mEndPoint;
	}

	public void setEndpoint(AMapLocation endpoint) {
		this.mEndPoint = endpoint;
	}

	public List<AMapLocation> getPathline() {
		return mPathLinePoints;
	}

	public void setPathline(List<AMapLocation> pathline) {
		this.mPathLinePoints = pathline;
	}

	public String getDistance() {
		return mDistance;
	}

	public void setDistance(String distance) {
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

	public void setAveragespeed(String averagespeed) {
		this.mAveragespeed = averagespeed;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		this.mDate = date;
	}

	public void addpoint(AMapLocation point) {
		mPathLinePoints.add(point);
	}
	public String LatAndLongToString(AMapLocation point){
		StringBuffer latlong = new StringBuffer();
		latlong.append("纬度：" + point.getLatitude() + "经度：" + point.getLongitude());
		return latlong.toString();
	}
	@Override
	public String toString() {
		StringBuilder record = new StringBuilder();
		record.append("date" + getDate() + ", ");
		record.append("distance:" + getDistance() + "m, ");
		record.append("duration:" + getDuration() + "s");
		return record.toString();
	}
}
