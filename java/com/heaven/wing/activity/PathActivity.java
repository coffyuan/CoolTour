package com.heaven.wing.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.PoiItem;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.android.volley.RequestQueue;
import com.heaven.wing.R;
import com.heaven.wing.adapter.DbAdapter;
import com.heaven.wing.entity.PathRecord;
import com.heaven.wing.entity.Photo;
import com.heaven.wing.entity.Scene;
import com.heaven.wing.entity.Trace;
import com.heaven.wing.ui.MarkerOverlay;
import com.heaven.wing.ui.MarkerOverlayHttp;
import com.heaven.wing.util.ParseUtil;
import com.heaven.wing.util.PopupWindows;
import com.heaven.wing.util.TraceRePlay;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by 刘康斌 on 2018/11/9.
 */

public class PathActivity extends AppCompatActivity implements
        AMap.OnMapLoadedListener, TraceListener, View.OnClickListener{
    private final static int AMAP_LOADED = 2;

    private RadioButton mOriginRadioButton, mGraspRadioButton;
    private ToggleButton mDisplaybtn;
    private Button mStartPoint;

    private MapView mMapView;
    private AMap mAMap;
    private Marker mOriginStartMarker, mOriginEndMarker, mOriginRoleMarker;
    private Marker mGraspStartMarker, mGraspEndMarker, mGraspRoleMarker;
    private Polyline mOriginPolyline, mGraspPolyline;

    private String mRecordItemId;
    private String mTraceItemId;
    private List<LatLng> mOriginLatLngList;
    private List<LatLng> mGraspLatLngList;
    private boolean mGraspChecked = false;
    private boolean mOriginChecked = true;
    private ExecutorService mThreadPool;
    private TraceRePlay mRePlay;

    Handler HttpBitmapHandler;
    private static RequestQueue requestQueue;
    //数据库操作
    DbAdapter mDbAdapter = new DbAdapter(this);

    //标点覆盖物
    private MarkerOverlayHttp poiOverlayEdited;
    private MarkerOverlay poiOverlayNotEdited;

    //轨迹周围的景点
    private ArrayList<PoiItem> scenes = new ArrayList<>();

    //下方弹出框
    private PopupWindows popupWindows;



    public void testUpLoadPhoto(BmobFile fileTest){

        Photo photo = new Photo();
        photo.setSceneId("2140d25346");
        photo.setPhoto(fileTest);
        Log.e("test","test" + fileTest.toString());
        photo.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    Log.e("test","保存图片成功");
                }else{
                    Log.e("test","保存图片失败！" + e.getMessage());
                }
            }
        });
     }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorddisplay_activity);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        mGraspRadioButton = (RadioButton) findViewById(R.id.record_show_activity_grasp_radio_button);
        mOriginRadioButton = (RadioButton) findViewById(R.id.record_show_activity_origin_radio_button);
        mDisplaybtn = (ToggleButton) findViewById(R.id.displaybtn);
        //mStartPoint = (Button)findViewById(R.id.startPoint);
        mOriginRadioButton.setOnClickListener(this);
        mGraspRadioButton.setOnClickListener(this);
        mDisplaybtn.setOnClickListener(this);
        //mStartPoint.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        Intent recordIntent = getIntent();
        if (recordIntent != null) {
            mRecordItemId = recordIntent.getStringExtra("pathId");
            Log.e("test","mrecordItem =" + mRecordItemId);
        }
        //availableProcessors(),返回到Java虚拟机的可用的处理器数量
        // 为什么要乘2加3:？表示没看懂
        int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 3;
        mThreadPool = Executors.newFixedThreadPool(threadPoolSize);


//        File fileTest  = new File("内部存储/test1.jpg");
//        Log.e("test","111" + fileTest.getName() + fileTest.getPath());
//        final BmobFile file = new BmobFile(fileTest);
//        file.upload(new UploadFileListener() {
//            @Override
//            public void done(BmobException e) {
//                if (e == null) {
//                    testUpLoadPhoto(file);
//                    Log.e("test","保存图片成功1");
//                }else{
//                    Log.e("test","保存图片失败1" + e.getMessage());
//                }
//            }
//        });
        initMap();

    }

    private void initMap() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mAMap.setOnMapLoadedListener(this);
            //
            mAMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener(){
                @Override
                public boolean onMarkerClick(Marker marker){
                    Log.e("marker test",marker.getId());
                    final LatLng markerLatLng = marker.getPosition();
                    String latlngStr = markerLatLng.latitude + "," + markerLatLng.longitude;
                    //查询条件1
                    BmobQuery<Scene> query1 = new BmobQuery<Scene>();
                    query1.addWhereEqualTo("sceneLatLng",latlngStr);
                    //查询条件2
                    BmobQuery<Scene> query2 = new BmobQuery<Scene>();
                    query2.addWhereEqualTo("pathId",mRecordItemId);
                    List<BmobQuery<Scene>> queryList = new ArrayList<BmobQuery<Scene>>();
                    queryList.add(query1);
                    queryList.add(query2);
                    BmobQuery<Scene> query = new BmobQuery<Scene>();
                    query.and(queryList);
                   // Log.e("test","所点击景点的经纬度：" + latlngStr);
                    query.findObjects(new FindListener<Scene>() {
                        @Override
                        public void done(List<Scene> list, BmobException e) {
                            if(e == null){
                        //        Log.e("test","从数据库取出的景点个数1：" + list.size());
                                showPopupWindow(list.get(0));
                            }else{
                                Log.e("test","查找数据失败1");
                            }

                        }
                    });

                    return true;
                }

            });
//            mAMap.setOnPOIClickListener(new AMap.OnPOIClickListener() {
//                @Override
//                public void onPOIClick(Poi poi) {
//                    Log.e("test","poiclick:" + poi.toString());
//                }
//            });
        }
    }

    //地图加载回调
    @Override
    public void onMapLoaded() {
        Log.e("test","地图加载成功！");
        Message msg = handler.obtainMessage();
        msg.what = AMAP_LOADED;
        handler.sendMessage(msg);
    }
    //onMapLoaded()方法的handler回调
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AMAP_LOADED:
                    setupRecord();
                    break;
                default:
                    break;
            }
        }

    };

    public void showMarkerInfo(Marker marker){
        BmobQuery<Trace> query = new BmobQuery<Trace>();
        query.addWhereEqualTo("","");

    }



    private void startMove() {
        if(mRePlay !=null){
            mRePlay.stopTrace();
        }
        if (mOriginChecked) {
            mRePlay = rePlayTrace(mOriginLatLngList, mOriginRoleMarker);
        } else if (mGraspChecked) {
            mRePlay = rePlayTrace(mGraspLatLngList, mGraspRoleMarker);
        }
    }

    /**
     * 轨迹回放方法
     */
    private TraceRePlay rePlayTrace(List<LatLng> list, final Marker updateMarker) {
        TraceRePlay replay = new TraceRePlay(list, 20,
                new TraceRePlay.TraceRePlayListener() {

                    @Override
                    public void onTraceUpdating(LatLng latLng) {
                        if (updateMarker != null) {
                            updateMarker.setPosition(latLng); // 更新小人实现轨迹回放
                        }
                    }

                    @Override
                    public void onTraceUpdateFinish() {
                        mDisplaybtn.setChecked(false);
                        mDisplaybtn.setClickable(true);
                    }
                });
        mThreadPool.execute(replay);
        return replay;
    }

    /**
     * 将纠偏后轨迹小人设置到起点
     */
    private void resetGraspRole() {
        if (mGraspLatLngList == null) {
            return;
        }
        LatLng startLatLng = mGraspLatLngList.get(0);
        if (mGraspRoleMarker != null) {
            mGraspRoleMarker.setPosition(startLatLng);
        }
    }

    /**
     * 将原始轨迹小人设置到起点
     */
    private void resetOriginRole() {
        if (mOriginLatLngList == null) {
            return;
        }
        LatLng startLatLng = mOriginLatLngList.get(0);
        if (mOriginRoleMarker != null) {
            mOriginRoleMarker.setPosition(startLatLng);
        }
    }



    public void onBackClick(View view) {
        this.finish();
        if (mThreadPool != null) {
            mThreadPool.shutdownNow();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mThreadPool != null) {
            mThreadPool.shutdownNow();
        }
    }

    private LatLngBounds getBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (mOriginLatLngList == null) {
            return b.build();
        }
        for (int i = 0; i < mOriginLatLngList.size(); i++) {
            b.include(mOriginLatLngList.get(i));
        }
        return b.build();

    }
    public void initRecord(){

    }
    /**
     * 轨迹数据初始化
     *
     */
    private void setupRecord() {
        // 轨迹纠偏初始化
        //从bmob中取数据
        BmobQuery<Trace> query = new BmobQuery<>();
        query.getObject(mRecordItemId, new QueryListener<Trace>() {
            @Override
            public void done(Trace trace, BmobException e) {
                if(e == null){
                    LBSTraceClient mTraceClient = new LBSTraceClient(
                            getApplicationContext());
                    //把Trace转化为PathRecord
                    PathRecord mRecord = ParseUtil.parsePathRecord(trace);
                    //得到轨迹点集合
                    List<AMapLocation> recordList = mRecord.getPathline();
                  //  Log.e("hello","取出的轨迹点集合：" + recordList.toString());
                    AMapLocation startLoc = mRecord.getStartpoint();
                    AMapLocation endLoc = mRecord.getEndpoint();
                    if (recordList == null || startLoc == null || endLoc == null) {
                        return;
                    }
                    LatLng startLatLng = new LatLng(startLoc.getLatitude(),
                            startLoc.getLongitude());
                    LatLng endLatLng = new LatLng(endLoc.getLatitude(),
                            endLoc.getLongitude());
                    mOriginLatLngList = ParseUtil.parseLatLngList(recordList);

                    addOriginTrace(startLatLng, endLatLng, mOriginLatLngList);

                    List<TraceLocation> mGraspTraceLocationList = ParseUtil
                            .parseTraceLocationList(recordList);
                    // 调用轨迹纠偏，将mGraspTraceLocationList进行轨迹纠偏处理
                    mTraceClient.queryProcessedTrace(1, mGraspTraceLocationList,
                            LBSTraceClient.TYPE_AMAP, PathActivity.this);
                }else{
                    Log.e("setupRecord",e.getMessage());
                }
            }
        });

    }

    /**
     * 地图上添加原始轨迹线路及起终点、轨迹动画小人
     *
     * @param startPoint
     * @param endPoint
     * @param originList
     */
    private void addOriginTrace(LatLng startPoint, LatLng endPoint,
                                List<LatLng> originList) {
        mOriginPolyline = mAMap.addPolyline(new PolylineOptions().color(
                Color.BLUE).addAll(originList));
        mOriginStartMarker = mAMap.addMarker(new MarkerOptions().position(
                startPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.start)));
        mOriginEndMarker = mAMap.addMarker(new MarkerOptions().position(
                endPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.end)));

        try {
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(),
                    50));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOriginRoleMarker = mAMap.addMarker(new MarkerOptions().position(
                startPoint).icon(
                BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.drawable.walk))));
        //添加景点
        makeMarker();
    }

    /**
     * 设置是否显示原始轨迹
     *
     * @param enable
     */
    private void setOriginEnable(boolean enable) {
        mDisplaybtn.setClickable(true);
        if (mOriginPolyline == null || mOriginStartMarker == null
                || mOriginEndMarker == null || mOriginRoleMarker == null) {
            return;
        }
        if (enable) {
            mOriginPolyline.setVisible(true);
            mOriginStartMarker.setVisible(true);
            mOriginEndMarker.setVisible(true);
            mOriginRoleMarker.setVisible(true);
        } else {
            mOriginPolyline.setVisible(false);
            mOriginStartMarker.setVisible(false);
            mOriginEndMarker.setVisible(false);
            mOriginRoleMarker.setVisible(false);
        }
    }

    /**
     * 地图上添加纠偏后轨迹线路及起终点、轨迹动画小人
     *
     */
    private void addGraspTrace(List<LatLng> graspList, boolean mGraspChecked) {
        if (graspList == null || graspList.size() < 2) {
            return;
        }
        LatLng startPoint = graspList.get(0);
        LatLng endPoint = graspList.get(graspList.size() - 1);
        mGraspPolyline = mAMap.addPolyline(new PolylineOptions()
                .setCustomTexture(
                        BitmapDescriptorFactory
                                .fromResource(R.drawable.grasp_trace_line))
                .width(40).addAll(graspList));
        mGraspStartMarker = mAMap.addMarker(new MarkerOptions().position(
                startPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.start)));
        mGraspEndMarker = mAMap.addMarker(new MarkerOptions()
                .position(endPoint).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.end)));
        mGraspRoleMarker = mAMap.addMarker(new MarkerOptions().position(
                startPoint).icon(
                BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.drawable.walk))));
        if (!mGraspChecked) {
            mGraspPolyline.setVisible(false);
            mGraspStartMarker.setVisible(false);
            mGraspEndMarker.setVisible(false);
            mGraspRoleMarker.setVisible(false);
        }

    }

    /**
     * 设置是否显示纠偏后轨迹
     *
     * @param enable
     */
    private void setGraspEnable(boolean enable) {
        mDisplaybtn.setClickable(true);
        if (mGraspPolyline == null || mGraspStartMarker == null
                || mGraspEndMarker == null || mGraspRoleMarker == null) {
            return;
        }
        if (enable) {
            mGraspPolyline.setVisible(true);
            mGraspStartMarker.setVisible(true);
            mGraspEndMarker.setVisible(true);
            mGraspRoleMarker.setVisible(true);
        } else {
            mGraspPolyline.setVisible(false);
            mGraspStartMarker.setVisible(false);
            mGraspEndMarker.setVisible(false);
            mGraspRoleMarker.setVisible(false);
        }
    }



    /**
     * 轨迹纠偏完成数据回调
     */
    @Override
    public void onFinished(int arg0, List<LatLng> list, int arg2, int arg3) {
        addGraspTrace(list, mGraspChecked);
        mGraspLatLngList = list;

    }

    @Override
    public void onRequestFailed(int arg0, String arg1) {
        Toast.makeText(this.getApplicationContext(), "轨迹纠偏失败1:" + arg1,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onTraceProcessing(int arg0, int arg1, List<LatLng> arg2) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.displaybtn:
                if (mDisplaybtn.isChecked()) {
                    startMove();
                    mDisplaybtn.setClickable(false);
                }
                break;
            case R.id.record_show_activity_grasp_radio_button:
                mGraspChecked = true;
                mOriginChecked = false;
                mGraspRadioButton.setChecked(true);
                mOriginRadioButton.setChecked(false);
                setGraspEnable(true);
                setOriginEnable(false);
                mDisplaybtn.setChecked(false);
                resetGraspRole();
                break;
            case R.id.record_show_activity_origin_radio_button:
                mOriginChecked = true;
                mGraspChecked = false;
                mGraspRadioButton.setChecked(false);
                mOriginRadioButton.setChecked(true);
                setGraspEnable(false);
                setOriginEnable(true);
                mDisplaybtn.setChecked(false);
                resetOriginRole();
                break;

        }
    }
    //在地图上标点
    public void makeMarker(){
        BmobQuery<Scene> query = new BmobQuery<>();
        query.addWhereEqualTo("pathId",mRecordItemId);
        query.findObjects(new FindListener<Scene>() {
            @Override
            public void done(List<Scene> list, BmobException e) {
                Log.e("test","取景点数据成功");
                //未编辑过的景点列表
                List<Scene> isNotEditSceneList = new ArrayList<Scene>();
                //编辑过的景点列表
                List<Scene> editedSceneList = new ArrayList<Scene>();
                for(int i = 0; i < list.size(); i++){
                    scenes.add(ParseUtil.parsePoiItem(list.get(i)));
                   // Log.e("test","景点id：" + list.get(i).getObjectId());
                    if(list.get(i).getState() == false){
                        isNotEditSceneList.add(list.get(i));
                    }else{
                        editedSceneList.add(list.get(i));
                    }
                }
                //编辑过的景点显示的marker
                poiOverlayEdited = new MarkerOverlayHttp(mAMap, editedSceneList,getApplicationContext());
                poiOverlayEdited.removeFromMap();
                poiOverlayEdited.addToMap();
                poiOverlayEdited.zoomToSpan();
                Log.e("test","编辑过的景点个数：" + editedSceneList.size());

                //未编辑过的景点显示的marker
                poiOverlayNotEdited = new ViewPoiOverlayDefault(mAMap, isNotEditSceneList);
                poiOverlayNotEdited.removeFromMap();
                poiOverlayNotEdited.addToMap();
                Log.e("test","未编辑过的景点个数：" + isNotEditSceneList.size());
            }
        });


    }

    //下方弹出框
    public void showPopupWindow(Scene scene) {
        popupWindows = new PopupWindows(PathActivity.this,scene);
        View rootView = LayoutInflater.from(PathActivity.this).inflate(R.layout.activity_path, null);
        popupWindows.showAtLocation(rootView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);

//        mFinishProjectPopupWindows = new FinishProjectPopupWindows(PathActivity.this,itemsOnClick);
//        mFinishProjectPopupWindows.showAtLocation(PathActivity.this.findViewById(R.id.drawer_layout),
//                Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);

    }

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
//                    poiOverlay = new ViewPoiOverlay(mAMap, scenes);
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
//    public void saveMarker(Marker marker, String userId){
//        // mDbAdapter.saveMarkerToBmob(marker,userId);
//    }
//    public void  saveScene(){
//        scenes = pastLeep(scenes);
//        for(int i = 0; i < scenes.size(); i++){
//            Scene scene = new Scene();
//            scene.setPathId(mRecordItemId);
//            scene.setSceneLatLng(scenes.get(i).getLatLonPoint().toString());
//            scene.setSceneName(scenes.get(i).getTitle());
//            scene.save();
//        }
//    }
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

    public class ViewPoiOverlay extends MarkerOverlay {

        public ViewPoiOverlay(AMap aMap, List<Scene> list) {
            super(aMap, list);
        }

        @Override
        protected BitmapDescriptor getBitmapDescriptor(int index) {
//            View view = null;
           // Log.e("view","1 " + getTitle(index));
            final View view = View.inflate(PathActivity.this, R.layout.custom_view, null);
            final TextView textView = ((TextView) view.findViewById(R.id.title));
            final ImageView imageView = (ImageView)view.findViewById(R.id.path_photo);

            textView.setText(getTitle(index));
           // Log.e("test","index: " + index + getTitle(index));
            BmobQuery<Photo> query = new BmobQuery<>();
            query.addWhereEqualTo("sceneId",getSceneId(index));
            Log.e("test","sceneId clicked:" + getSceneId(index) );
            query.findObjects(new FindListener<Photo>() {
                @Override
                public void done(List<Photo> list, BmobException e) {
                    if(e == null){
                        Log.e("test","取图片成功" + list.toString());
                        BmobFile file = list.get(0).getPhoto();
                        Log.e("test","图片名：" + file.getFilename());
                        Log.e("test","图片url：" + file.getFileUrl());
                        Bitmap bitmap = getHttpBitmap(list.get(0).getPhoto().getFileUrl());
                      imageView.setImageBitmap(bitmap);
                        //imageView.setImageResource(R.drawable.view_photo_test);
                        imageView.invalidate();
//                        Thread getHttpBitmapThread = new GetHttpBitmapThread(file.getFileUrl());
//                        getHttpBitmapThread.start();
//                        HttpBitmapHandler = new Handler(){
//                            @Override
//                            public void handleMessage(Message msg) {
//                                super.handleMessage(msg);
//                                Bundle data = msg.getData();
//                                Bitmap bitmap = (Bitmap)data.get("bitmap");
//
//                                imageView.setImageResource(R.drawable.view_photo_test);
//                                Log.e("test","设置图片成功" + bitmap.toString());
//                            }
//
//                        };

                        //                       Glide.with(imageView.getContext()).load(file.getFileUrl()).placeholder(R.drawable.view_photo_test).into(imageView);
//                        File image = new File(PathActivity.this.getExternalCacheDir(), list.get(0).getObjectId()+".PNG");
//                        String path = PathActivity.this.getExternalCacheDir()+"/"+list.get(0).getObjectId()+".PNG";
//                        Bitmap photoBitmap = BitmapFactory.decodeFile(path);
//                        imageView.setImageBitmap(photoBitmap);
//
//                        requestQueue = Volley.newRequestQueue(getApplicationContext());
//                        ImageLoader imageLoader = new ImageLoader(requestQueue, new BitmapCache());
//                        ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView,R.mipmap.ic_launch,R.mipmap.ic_launch);
//                        imageLoader.get(list.get(0).getPhoto().getUrl(), imageListener );
                    }else{
                        Log.e("test","取图片失败");
                    }
                }
            });

            return  BitmapDescriptorFactory.fromView(view);
        }



    }
    public class ViewPoiOverlayDefault extends MarkerOverlay {

        public ViewPoiOverlayDefault(AMap aMap, List<Scene> list) {
            super(aMap, list);
        }

        @Override
        protected BitmapDescriptor getBitmapDescriptor(int index) {
            View view = null;
            Log.e("view","1 " + getTitle(index));
            view = View.inflate(PathActivity.this, R.layout.custom_view_not_edit, null);
            TextView textView = ((TextView) view.findViewById(R.id.title));
            textView.setText(getTitle(index));

            return  BitmapDescriptorFactory.fromView(view);
        }
    }

    public class GetHttpBitmapThread extends Thread {
        URL myURL;
        public GetHttpBitmapThread(String url){
            try {
                this.myURL = new URL(url);
            }catch(MalformedURLException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run(){
            Bitmap bitmap = null;

            try {
                HttpURLConnection conn = (HttpURLConnection)myURL.openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable("bitmap",bitmap);
            Message msg = new Message();
            msg.what = 1;
            msg.setData(bundle);
            HttpBitmapHandler.sendMessage(msg);
        }

    }

    public static Bitmap getHttpBitmap(String url) {
        URL myURL = null;
        Bitmap bitmap = null;
        try {
            myURL = new URL(url);
        }catch(MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection)myURL.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
