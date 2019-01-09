package com.heaven.wing.util;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.trace.TraceLocation;
import com.heaven.wing.entity.PathRecord;
import com.heaven.wing.entity.Scene;
import com.heaven.wing.entity.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 *格式转换工具类
 * Created by Eric on 2018/11/20.
 */

public class ParseUtil {
    /**
     * 把经纬度字符串转化为LatLng
     * @param string
     * @return
     */
    public static LatLng parseLatLng(String string){
        return new LatLng(Double.parseDouble(string.split(",")[0]),Double.parseDouble(string.split(",")[1]));
    }
    /**
     * 把sceneList转化为PoiItemList
     * @param sceneList
     * @return
     */
    public static List<PoiItem> parsePoiItemList(List<Scene> sceneList){
        List<PoiItem> poiItemList = new ArrayList<>();
        if(sceneList == null){
            return null;
        }
        for(int i = 0; i < sceneList.size(); i++){
            poiItemList.add(parsePoiItem(sceneList.get(i)));
        }
        return poiItemList;
    }

    public static PoiItem parsePoiItem(Scene scene){
        String latlngStr = scene.getSceneLatLng();
        double lat = Double.parseDouble(latlngStr.split(",")[0]);
        double lng = Double.parseDouble(latlngStr.split(",")[1]);
        LatLonPoint latLonPoint = new LatLonPoint(lat,lng);
        PoiItem poiItem = new PoiItem(scene.getObjectId(),latLonPoint,scene.getSceneName(),scene.getSceneName());
        return poiItem;
    }
    /**
     * 把LatLng转化为LatLonPoint
     * @param aMapLocation
     * @return
     */
    public static LatLonPoint ParseToLatLonPoint(AMapLocation aMapLocation){
        if(aMapLocation == null){
            return null;
        }
        return new LatLonPoint(aMapLocation.getLatitude(),aMapLocation.getLongitude());
    }

    /**
     * 把LatLngList转化为LatLonPointList
     * @param latLngList
     * @return
     */
    public static ArrayList<LatLonPoint> converToLatLonPoint(List<LatLng> latLngList){
        ArrayList<LatLonPoint> mlatLonPoints  = new ArrayList<LatLonPoint>();
        if(latLngList == null){
            return null;
        }
        for(LatLng point:latLngList){
            mlatLonPoints.add(new LatLonPoint(point.latitude,point.longitude));
        }
        return mlatLonPoints;
    }
    /**
     * 去除date后面的时分秒
     * @param date 2018-5-2 6:30:12
     * @return  2018-5-2
     */
    public static String parseDate(String date){
        return date.split(" ")[0];
    }

    /**
     * 把Trace转化为PathRecord
     * @param object
     * @return
     */
    public static PathRecord parsePathRecord(Trace object){
        PathRecord record = new PathRecord();
        record.setAveragespeed(object.getAveragespeed());
        record.setDate(object.getDate());
        record.setId(object.getObjectId());
        record.setUserId(object.getUserId());
        record.setDistance(String.valueOf(object.getDistance()));
        record.setDuration(object.getDuration());
        record.setEndpoint(ParseUtil.parseLocation(object.getEndpoint()));
        record.setStartpoint(ParseUtil.parseLocation(object.getStartpoint()));
        record.setPathline(ParseUtil.parseLocations(object.getPathline()));
        record.setCity(object.getCity());

        return record;
    }
    /**
     * 将AMapLocation List 转为TraceLocation list
     *
     * @param
     * @return
     */
    public static List<TraceLocation> parseTraceLocationList(
            List<AMapLocation> list) {
        List<TraceLocation> traceList = new ArrayList<TraceLocation>();
        if (list == null) {
            return traceList;
        }
        for (int i = 0; i < list.size(); i++) {
            TraceLocation location = new TraceLocation();
            AMapLocation amapLocation = list.get(i);
            location.setBearing(amapLocation.getBearing());
            location.setLatitude(amapLocation.getLatitude());
            location.setLongitude(amapLocation.getLongitude());
            location.setSpeed(amapLocation.getSpeed());
            location.setTime(amapLocation.getTime());
            traceList.add(location);
        }
        return traceList;
    }
    public static TraceLocation parseTraceLocation(AMapLocation amapLocation) {
        TraceLocation location = new TraceLocation();
        location.setBearing(amapLocation.getBearing());
        location.setLatitude(amapLocation.getLatitude());
        location.setLongitude(amapLocation.getLongitude());
        location.setSpeed(amapLocation.getSpeed());
        location.setTime(amapLocation.getTime());
        return  location;
    }

    /**
     * 将AMapLocation List 转为LatLng list
     * @param list
     * @return
     */
    public static List<LatLng> parseLatLngList(List<AMapLocation> list) {
        List<LatLng> traceList = new ArrayList<LatLng>();
        if (list == null) {
            return traceList;
        }
        for (int i = 0; i < list.size(); i++) {
            AMapLocation loc = list.get(i);
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            LatLng latlng = new LatLng(lat, lng);
            traceList.add(latlng);
        }
        return traceList;
    }

    /**
     * 把经纬度字符串转化为AMpLoaction
     * @param latLonStr
     * @return
     */
    public static AMapLocation parseLocation(String latLonStr) {
        if (latLonStr == null || latLonStr.equals("") || latLonStr.equals("[]")) {
            return null;
        }
        String[] loc = latLonStr.split(",");
        AMapLocation location = null;
        if (loc.length == 6) {
            location = new AMapLocation(loc[2]);
            location.setProvider(loc[2]);
            location.setLatitude(Double.parseDouble(loc[0]));
            location.setLongitude(Double.parseDouble(loc[1]));
            location.setTime(Long.parseLong(loc[3]));
            location.setSpeed(Float.parseFloat(loc[4]));
            location.setBearing(Float.parseFloat(loc[5]));
        }else if(loc.length == 2){
            location = new AMapLocation("gps");
            location.setLatitude(Double.parseDouble(loc[0]));
            location.setLongitude(Double.parseDouble(loc[1]));
        }

        return location;
    }
    //把经纬度字符串改成定位信息列表
    public static ArrayList<AMapLocation> parseLocations(String latLonStr) {
        ArrayList<AMapLocation> locations = new ArrayList<AMapLocation>();
        String[] latLonStrs = latLonStr.split(";");
        for (int i = 0; i < latLonStrs.length; i++) {
            AMapLocation location = ParseUtil.parseLocation(latLonStrs[i]);
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }
}
