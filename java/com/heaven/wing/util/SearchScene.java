//package com.heaven.wing.util;
//
//import android.content.Context;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.amap.api.location.AMapLocation;
//import com.amap.api.maps.model.LatLng;
//import com.amap.api.maps.model.MarkerOptions;
//import com.amap.api.services.core.LatLonPoint;
//import com.amap.api.services.core.PoiItem;
//import com.amap.api.services.poisearch.PoiResult;
//import com.amap.api.services.poisearch.PoiSearch;
//import com.heaven.wing.activity.PathActivity;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * 搜索一条轨迹上的景点
// * Created by Eric on 2018/11/21.
// */
//
//public class SearchScene {
//    List<AMapLocation> aMapLocations = new ArrayList<>();
//    Context context;
//    public SearchScene(Context context, List<AMapLocation> list){
//        this.context = context;
//        this.aMapLocations = list;
//    }
//
//
//    public void doPoiSearch(LatLonPoint mLatLonPoint){
//        PoiSearch.Query query = new PoiSearch.Query("公交车站","150700","南昌");
//        query.setPageSize(10);// 设置每页最多返回多少条poiitem
//        query.setPageNum(0);//获取设置查询的是第几页，从0开始
//        query.requireSubPois(true);//设置是否返回父子关系。
//        PoiSearch poiSearch = new PoiSearch(this,query);
//        poiSearch.setOnPoiSearchListener(this);
//
//        // 设置搜索区域为以lp点为圆心，其周围3000米范围
//        poiSearch.setBound(new PoiSearch.SearchBound(mLatLonPoint, 500, true));
//        poiSearch.searchPOIAsyn();// 异步搜索
//    }
//    public ArrayList<LatLonPoint> converToLatLonPoint(List<LatLng> latLngList){
//        ArrayList<LatLonPoint> mlatLonPoints  = new ArrayList<LatLonPoint>();
//        if(latLngList == null){
//            return null;
//        }
//        for(LatLng point:latLngList){
//            mlatLonPoints.add(new LatLonPoint(point.latitude,point.longitude));
//        }
//        return mlatLonPoints;
//    }
//
//    //搜索返回结果回调
//    @Override
//    public void onPoiSearched(PoiResult poiResult, int errorCode) {
//        if (errorCode == 1000) {
//            if (poiResult != null && poiResult.getQuery() != null) {
//
//                List<PoiItem> poiItems = poiResult.getPois();
//                MarkerOptions markerOptions = new MarkerOptions();
//                if (poiItems != null && poiItems.size() > 0) {
//                    //mAMap.clear();// 清理之前的图标
//                    //Log.e("hello",mOriginLatLngList.size() + " " + poiItems.toString());
//
//                    scenes.add(poiItems.get(0));
//                    poiOverlay = new PathActivity.ViewPoiOverlay(mAMap, scenes);
//                    poiOverlay.removeFromMap();
//                    poiOverlay.addToMap();
//                    poiOverlay.zoomToSpan();
//                    Log.e("hello",1 + " " + scenes.toString());
//
//                    markerOptions.position(parseLatLonPointToLatLng(poiItems.get(0).getLatLonPoint()));
//                    //saveMarker(markerOptions,mRecordItemId);
//
//                } else {
//                    Toast.makeText(PathActivity.this, "无搜索结果1", Toast.LENGTH_SHORT).show();
//
//                }
//            } else {
//                Toast.makeText(PathActivity.this, "无搜索结果2", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//    @Override
//    public void onPoiItemSearched(PoiItem poiItem, int i) {
//
//    }
//
//    public LatLng parseLatLonPointToLatLng(LatLonPoint latLonPoint){
//        return new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude());
//    }
//
//    //set集合去重，不改变原有的顺序
//    public  ArrayList<PoiItem> pastLeep(List<PoiItem> list){
//        System.out.println("list = [" + list.toString() + "]");
//        ArrayList<PoiItem> listNew=new ArrayList<>();
//        Set set=new HashSet();
//        for (PoiItem point:list) {
//            if(set.add(point)){
//                listNew.add(point);
//            }
//        }
//        return listNew;
//
//    }
//}
