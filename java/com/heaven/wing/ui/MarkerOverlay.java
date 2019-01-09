package com.heaven.wing.ui;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.heaven.wing.entity.Scene;
import com.heaven.wing.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2018/11/22.
 */

public class MarkerOverlay {
    private List<Scene> sceneList;
    private AMap mAMap;
    String sceneId;
    private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
    /**
     * 通过此构造函数创建Poi图层。
     * @param amap 地图对象。
     * @param sceneList 要在地图上添加的poi。列表中的poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类<strong> <a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
     */
    public MarkerOverlay(AMap amap, List<Scene> sceneList) {
        mAMap = amap;
        this.sceneList = sceneList;
    }
    /**
     * 添加Marker到地图中。
     */
    public void addToMap() {
        try{
            for (int i = 0; i < sceneList.size(); i++) {
                Marker marker = mAMap.addMarker(getMarkerOptions(i));
                marker.setObject(i);
                mPoiMarks.add(marker);
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 去掉PoiOverlay上所有的Marker。
     */
    public void removeFromMap() {
        for (Marker mark : mPoiMarks) {
            mark.remove();
        }
    }
    /**
     * 移动镜头到当前的视角。
     */
    public void zoomToSpan() {
        try{
            if (sceneList != null && sceneList.size() > 0) {
                if (mAMap == null)
                    return;
                if(sceneList.size()==1){
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                            (ParseUtil.parseLatLng(sceneList.get(0).getSceneLatLng()), 18f));
                }else{
                    LatLngBounds bounds = getLatLngBounds();
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        for (int i = 0; i < sceneList.size(); i++) {
            b.include(ParseUtil.parseLatLng(sceneList.get(i).getSceneLatLng()));
        }
        return b.build();
    }

    private MarkerOptions getMarkerOptions(int index) {
        Marker m;

        return new MarkerOptions()
                .position(ParseUtil.parseLatLng(sceneList.get(index).getSceneLatLng()))

                .icon(getBitmapDescriptor(index));
    }
    /**
     * 给第几个Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     * @param index 第几个Marker。
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getBitmapDescriptor(int index) {
        return null;
    }
    /**
     * 返回第index的Marker的标题。
     * @param index 第几个Marker。
     * @return marker的标题。
     */
    protected String getTitle(int index) {
        return sceneList.get(index).getSceneName();
    }
    protected String getSceneId(int index){
        return sceneList.get(index).getObjectId();
    }
    /**
     * 返回第index的Marker的详情。
     * @param index 第几个Marker。
     * @return marker的详情。
     */
    protected String getSnippet(int index) {
        return sceneList.get(index).getDescription();
    }
    /**
     * 从marker中得到poi在list的位置。
     * @param marker 一个标记的对象。
     * @return 返回该marker对应的poi在list的位置。
     */
    public int getPoiIndex(Marker marker) {
        for (int i = 0; i < mPoiMarks.size(); i++) {
            if (mPoiMarks.get(i).equals(marker)) {
                return i;
            }
        }
        return -1;
    }
    /**
     * 返回第index的poi的信息。
     * @param index 第几个poi。
     * @return poi的信息。poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
     */
    public Scene getPoiItem(int index) {
        if (index < 0 || index >= sceneList.size()) {
            return null;
        }
        return sceneList.get(index);
    }
}
