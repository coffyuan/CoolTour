package com.heaven.wing.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.heaven.wing.R;
import com.heaven.wing.adapter.DbAdapter;
import com.heaven.wing.entity.PathRecord;
import com.heaven.wing.entity.Scene;
import com.heaven.wing.entity.Trace;
import com.heaven.wing.util.ParseUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class JourneyActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener, TraceListener, PoiSearch.OnPoiSearchListener{
    private final static int CALLTRACE = 0;
    private MapView mMapView;
    private AMap mAMap;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private PolylineOptions mPolyoptions, tracePolytion;
    private Polyline mpolyline;
    private PathRecord record;
    private long mStartTime;
    private long mEndTime;
    private ToggleButton btn;
    private DbAdapter DbHepler;
    //轨迹纠偏点
    private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();
    private List<TraceOverlay> mOverlayList = new ArrayList<TraceOverlay>();
    private List<AMapLocation> recordList = new ArrayList<AMapLocation>();
    private int tracesize = 30;
    private int mDistance = 0;

    private int markerCount = 10;
    //用于绘制轨迹纠偏接口回调的一条平滑轨迹
    private TraceOverlay mTraceoverlay;
    private TextView mResultShow;
    private Marker mlocMarker;
    //权限申请标识
    private static final int BAIDU_READ_PHONE_STATE =100;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    MyLocationStyle myLocationStyle = new MyLocationStyle();
    //动态权限申请集合
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // 声明一个集合，用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();

    //轨迹周围的景点
    private ArrayList<PoiItem> scenes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "5e7a422c02ecdd389a1fb9905f523da6");
        setContentView(R.layout.basicmap_activity);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写

        //判断是否为android6.0系统版本，如果是，需要动态添加权限
        if (Build.VERSION.SDK_INT>=23){

            showContacts();
        }else{
            init();
            initpolyline();//为定位方法,//开始定位
        }
    }

    public void showContacts(){
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            //把为授权的权限添加进未授权数组中
            if (ContextCompat.checkSelfPermission(JourneyActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
               // Log.e("hello"," " + "a");
            }
        }
        //未授予的权限为空，表示都授予了
        if (mPermissionList.isEmpty()) {
            Toast.makeText(JourneyActivity.this,"已经授权",Toast.LENGTH_LONG).show();
            init();
            initpolyline();//为定位方法,//开始定位
        } else {
            //请求权限方法
            //Log.e("hello"," " + "c");//test
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(JourneyActivity.this, permissions, BAIDU_READ_PHONE_STATE);

        }
    }


    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.e("hello"," " + "b");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    init();
                    initpolyline();
                } else {
                    // 没有获取到权限，做特殊处理
                    //Log.e("hello"," " + "f");//test
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                    init();
                    initpolyline();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            setUpMap();
        }
        //为开始停止按钮设置监听
        btn = (ToggleButton) findViewById(R.id.start_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果按下开始按钮，则开始记录轨迹
                if (btn.isChecked()) {
                    //清除地图上所有的overlay（marker，polyline，circle等对象）
                    mAMap.clear(true);
                    //清除轨迹记录
                    if (record != null) {
                        record = null;
                    }
                    //开始记录
                    record = new PathRecord();
                    //记录开始时间
                    mStartTime = System.currentTimeMillis();
                    record.setDate(getcueDate(mStartTime));
                    mResultShow.setText("总距离");
                    //当按下结束按钮时，进行轨迹处理和存储
                } else {
                    //结束时间
                    mEndTime = System.currentTimeMillis();
                    //添加轨迹纠偏接口回调的平滑轨迹
                    mOverlayList.add(mTraceoverlay);
                    //计算总距离
                    DecimalFormat decimalFormat = new DecimalFormat("0.0");
                    mResultShow.setText(
                            decimalFormat.format(getTotalDistance() / 1000d) + "KM");
                    //轨迹纠偏类，对GPS高精度定位轨迹集合纠偏
                    LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
                    mTraceClient.queryProcessedTrace(2, ParseUtil.parseTraceLocationList(record.getPathline())
                            , LBSTraceClient.TYPE_AMAP, JourneyActivity.this);
                    //把数据存入本地
                   // saveRecord(record.getPathline(), record.getDate());
                    //把数据存入后端云bmob中
                    Log.e("test",record.getCity());
                    saveTrace(record.getPathline(), record.getDate(), record.getCity());

                    //按下结束按钮后跳转至主界面（历史轨迹界面）
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),MainActivity2.class);
                    startActivity(intent);
                }
            }
        });
        mResultShow = (TextView) findViewById(R.id.show_all_dis);

        mTraceoverlay = new TraceOverlay(mAMap);
    }
    public void saveScene(List<ArrayList> list){
        Scene scene = new Scene();

    }
    /**
     * 把轨迹数据存入后端云Bmob中
     * @param list
     * @param time
     */
    public void saveTrace(final List<AMapLocation> list, String time, String city){
        Trace trace = new Trace();
        //计算时间间隔
        String duration = getDuration();
        //计算距离
        float distance = getDistance(list);
        //计算平均速度
        String average = getAverage(distance);
        //计算时间点
        String pathlineSring = getPathLineString(list);
        //起始点
        AMapLocation firstLocaiton = list.get(0);
        AMapLocation lastLocaiton = list.get(list.size() - 1);
        String stratpoint = amapLocationToString(firstLocaiton);
        String endpoint = amapLocationToString(lastLocaiton);

        //把轨迹信息存入Bmob中
        trace.setDistance(distance);
        trace.setDuration(duration);
        trace.setAveragespeed(average);
        trace.setDate(time);
        trace.setPathline(pathlineSring);
        trace.setEndpoint(endpoint);
        trace.setStartpoint(stratpoint);
        trace.setUserId(BmobUser.getCurrentUser().getObjectId());
        trace.setCity(city);

        trace.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if(e==null){
                    Log.e("save","存储轨迹成功！");
                    saveScene(objectId);

                }else{
                    Log.e("save","存储失败！");
                }
            }
        });
    }

    /**
     * 保存景点相关信息
     */
    public void  saveScene(String pathId){
        if(scenes != null){
            scenes = pastLeep(scenes);
            for(int i = 0; i < scenes.size(); i++){
                Scene scene = new Scene();
                scene.setPathId(pathId);
                scene.setSceneLatLng(scenes.get(i).getLatLonPoint().toString());
                scene.setSceneName(scenes.get(i).getTitle());
                scene.setUserId(BmobUser.getCurrentUser().getObjectId());
                scene.setState(false);
                scene.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        Log.e("test","保存景点信息成功！");
                    }
                });
            }
        }else{
            Log.e("test","景点数据为空！");
        }

    }



    public LatLng parseLatLonPointToLatLng(LatLonPoint latLonPoint){
        return new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude());
    }

    //set集合去重，不改变原有的顺序
    public  ArrayList<PoiItem> pastLeep(List<PoiItem> list){
        System.out.println("list = [" + list.toString() + "]");
        ArrayList<PoiItem> listNew=new ArrayList<>();
        Set set=new HashSet();
        for (PoiItem point:list) {
            if(set.add(point)){
                listNew.add(point);
            }
        }
        return listNew;

    }

    /**
     *
     * 保存轨迹路线等相关信息
     */
//    protected void saveRecord(List<AMapLocation> list, String time) {
//        if (list != null && list.size() > 0) {
//            //打开数据库
//            DbHepler = new DbAdapter(this);
//            DbHepler.open();
//            //计算时间间隔
//            String duration = getDuration();
//            //计算距离
//            float distance = getDistance(list);
//            //计算平均速度
//            String average = getAverage(distance);
//            //计算时间点
//            String pathlineSring = getPathLineString(list);
//            //起始点
//            AMapLocation firstLocaiton = list.get(0);
//            AMapLocation lastLocaiton = list.get(list.size() - 1);
//            String stratpoint = amapLocationToString(firstLocaiton);
//            String endpoint = amapLocationToString(lastLocaiton);
//            //把轨迹相关信息存入数据库
//            DbHepler.createrecord(String.valueOf(distance), duration, average,
//                    pathlineSring, stratpoint, endpoint, time);
//            DbHepler.close();
//        } else {
//            Toast.makeText(JourneyActivity.this, "没有记录到路径", Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
    //计算行走时间
    private String getDuration() {
        return String.valueOf((mEndTime - mStartTime) / 1000f);
    }
    //计算平均速度
    private String getAverage(float distance) {
        return String.valueOf(distance / (float) (mEndTime - mStartTime));
    }
    //计算距离
    private float getDistance(List<AMapLocation> list) {
        float distance = 0;
        if (list == null || list.size() == 0) {
            return distance;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            AMapLocation firstpoint = list.get(i);
            AMapLocation secondpoint = list.get(i + 1);
            LatLng firstLatLng = new LatLng(firstpoint.getLatitude(),
                    firstpoint.getLongitude());
            LatLng secondLatLng = new LatLng(secondpoint.getLatitude(),
                    secondpoint.getLongitude());
            double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
                    secondLatLng);
            distance = (float) (distance + betweenDis);
        }
        return distance;
    }
    //格式转换
    private String getPathLineString(List<AMapLocation> list) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuffer pathline = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            AMapLocation location = list.get(i);
            String locString = amapLocationToString(location);
            pathline.append(locString).append(";");
        }
        String pathLineString = pathline.toString();
        pathLineString = pathLineString.substring(0,
                pathLineString.length() - 1);
        return pathLineString;
    }

    private String amapLocationToString(AMapLocation location) {
        StringBuffer locString = new StringBuffer();
        locString.append(location.getLatitude()).append(",");
        locString.append(location.getLongitude()).append(",");
        locString.append(location.getProvider()).append(",");
        locString.append(location.getTime()).append(",");
        locString.append(location.getSpeed()).append(",");
        locString.append(location.getBearing());
        return locString.toString();
    }

    /**
     * 划线
     */
    private void initpolyline() {
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(10f);
        mPolyoptions.color(Color.GRAY);
        tracePolytion = new PolylineOptions();
        tracePolytion.width(40);
        tracePolytion.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.grasp_trace_line));
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        mAMap.setLocationSource(this);// 设置定位监听
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        startlocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();

        }
        mLocationClient = null;
    }

    /**
     * 开始定位。
     */
    private void startlocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mLocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

            mLocationOption.setInterval(2000);

            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();

        }
    }

    /**
     * 定位结果回调
     * @param amapLocation 位置信息类
     *  每两秒回调一次
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {

                LatLng mylocation = new LatLng(amapLocation.getLatitude(),
                        amapLocation.getLongitude());

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    // 显示系统小蓝点
                    mListener.onLocationChanged(amapLocation);
                    //设置缩放级别
                    mAMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                    //将地图移动到定位点
                    //aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));

                    mListener.onLocationChanged(amapLocation);
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                    //去除圆形阴影区域，即设置透明
                    myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
                    myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
                    mAMap.setMyLocationStyle(myLocationStyle);

                    isFirstLoc = false;
                }
                //如果按了开始按钮，则开始实时轨迹划线
                if (btn.isChecked()) {
                    record.addpoint(amapLocation);
                    record.setCity(amapLocation.getCity());
                   // Log.e("hello",amapLocation.getCity());//test
                    //添加定位信息
                    mPolyoptions.add(mylocation);

                    //格式转换，将AMapLocation List 转为TraceLocation list
                    mTracelocationlist.add(ParseUtil.parseTraceLocation(amapLocation));
                    //每隔十个点（20秒）搜索一次周围的景点
                    if(mTracelocationlist.size() == 1 || mTracelocationlist.size() / (markerCount) == 1){

                        markerCount += 10;
                        //Log.e("test","markerCount:" + markerCount);

                        //搜索景点
                        doPoiSearch(ParseUtil.ParseToLatLonPoint(amapLocation));
                    }
                    //实时轨迹划线
                    redrawline();
                    if (mTracelocationlist.size() > tracesize - 1) {
                        trace();
                    }
                }
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": "
                        + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 查找景点
     * @param mLatLonPoint
     */
    public void doPoiSearch(LatLonPoint mLatLonPoint){
        PoiSearch.Query query = new PoiSearch.Query("景点","110000","南昌");
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);//获取设置查询的是第几页，从0开始
        query.requireSubPois(true);//设置是否返回父子关系。
        PoiSearch poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);

        // 设置搜索区域为以lp点为圆心，其周围3000米范围
        poiSearch.setBound(new PoiSearch.SearchBound(mLatLonPoint, 500, true));
        poiSearch.searchPOIAsyn();// 异步搜索
    }

    //搜索返回结果回调
    @Override
    public void onPoiSearched(PoiResult poiResult, int errorCode) {
        if (errorCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {

                List<PoiItem> poiItems = poiResult.getPois();
                MarkerOptions markerOptions = new MarkerOptions();
                if (poiItems != null && poiItems.size() > 0) {

                    for(int i = 0; i < poiItems.size(); i++){
                        scenes.add(poiItems.get(i));
                    }


                    Log.e("test","目前景点列表为：" + scenes.toString());

                    markerOptions.position(parseLatLonPointToLatLng(poiItems.get(0).getLatLonPoint()));
                    //saveMarker(markerOptions,mRecordItemId);

                } else {
                    Toast.makeText(JourneyActivity.this, "无搜索结果1", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(JourneyActivity.this, "无搜索结果2", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * 实时轨迹画线
     */
    private void redrawline() {
        if (mPolyoptions.getPoints().size() > 1) {
            if (mpolyline != null) {
                mpolyline.setPoints(mPolyoptions.getPoints());
            } else {
                mpolyline = mAMap.addPolyline(mPolyoptions);
            }
        }
//		if (mpolyline != null) {
//			mpolyline.remove();
//		}
//		mPolyoptions.visible(true);
//		mpolyline = mAMap.addPolyline(mPolyoptions);
//			PolylineOptions newpoly = new PolylineOptions();
//			mpolyline = mAMap.addPolyline(newpoly.addAll(mPolyoptions.getPoints()));
//		}
    }

    @SuppressLint("SimpleDateFormat")
    private String getcueDate(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd  HH:mm:ss ");
        Date curDate = new Date(time);
        String date = formatter.format(curDate);
        return date;
    }

//    public void record(View view) {
//        Intent intent = new Intent(JourneyActivity.this, RecordActivity.class);
//        startActivity(intent);
//    }

    private void trace() {
        List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
        mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, this);
        TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
        mTracelocationlist.clear();
        mTracelocationlist.add(lastlocation);
    }

    /**
     * 轨迹纠偏失败回调。
     * @param i
     * @param s
     */
    @Override
    public void onRequestFailed(int i, String s) {
        mOverlayList.add(mTraceoverlay);
        mTraceoverlay = new TraceOverlay(mAMap);
    }

    @Override
    public void onTraceProcessing(int i, int i1, List<LatLng> list) {

    }

    /**
     * 轨迹纠偏成功回调。
     * @param lineID 纠偏的线路ID
     * @param linepoints 纠偏结果
     * @param distance 总距离
     * @param waitingtime 等待时间
     */
    @Override
    public void onFinished(int lineID, List<LatLng> linepoints, int distance, int waitingtime) {
        if (lineID == 1) {
            if (linepoints != null && linepoints.size()>0) {
                mTraceoverlay.add(linepoints);
                mDistance += distance;
                mTraceoverlay.setDistance(mTraceoverlay.getDistance()+distance);
                if (mlocMarker == null) {
                    mlocMarker = mAMap.addMarker(new MarkerOptions().position(linepoints.get(linepoints.size() - 1))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.point))
                            .title("距离：" + mDistance+"米"));
                    mlocMarker.showInfoWindow();
                } else {
                    mlocMarker.setTitle("距离：" + mDistance+"米");
                    Toast.makeText(JourneyActivity.this, "距离"+mDistance, Toast.LENGTH_SHORT).show();
                    mlocMarker.setPosition(linepoints.get(linepoints.size() - 1));
                    mlocMarker.showInfoWindow();
                }
            }
        } else if (lineID == 2) {
            if (linepoints != null && linepoints.size()>0) {
                mAMap.addPolyline(new PolylineOptions()
                        .color(Color.RED)
                        .width(40).addAll(linepoints));
            }
        }

    }

    /**
     * 最后获取总距离
     * @return
     */
    private int getTotalDistance() {
        int distance = 0;
        for (TraceOverlay to : mOverlayList) {
            distance = distance + to.getDistance();
        }
        return distance;
    }
}
