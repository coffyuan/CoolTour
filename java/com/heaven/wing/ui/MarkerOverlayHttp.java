package com.heaven.wing.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.heaven.wing.R;
import com.heaven.wing.entity.Photo;
import com.heaven.wing.entity.Scene;
import com.heaven.wing.util.ParseUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Eric on 2018/11/28.
 */

public class MarkerOverlayHttp {
    private List<Scene> sceneList;
    private AMap mAMap;
    String sceneId;
    private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
    private Context context;
    Handler HttpBitmapHandler;
    /**
     * 通过此构造函数创建Poi图层。
     * @param amap 地图对象。
     * @param sceneList 要在地图上添加的poi。列表中的poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类<strong> <a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
     */
    public MarkerOverlayHttp(AMap amap, List<Scene> sceneList, Context context) {
        mAMap = amap;
        this.sceneList = sceneList;
        this.context = context;
    }
    /**
     * 添加Marker到地图中。
     */
    public void addToMap() {
        try{
            for (int i = 0; i < sceneList.size(); i++) {
                final int index = i;

                BmobQuery<Photo> query = new BmobQuery<>();
                query.addWhereEqualTo("sceneId", getSceneId(index));
                Log.e("test", "sceneId clicked:" + getSceneId(index));
                query.findObjects(new FindListener<Photo>() {
                    @Override
                    public void done(List<Photo> list, BmobException e) {
                        if (e == null) {
                            Log.e("test", "取图片成功" + list.toString());
                            BmobFile file = list.get(0).getPhoto();
                            Log.e("test", "图片名：" + file.getFilename());
                            Log.e("test", "图片url：" + file.getFileUrl());
//                            Bitmap bitmap = getHttpBitmap(list.get(0).getPhoto().getFileUrl());
                            //Bitmap bitmap = BitmapFactory.decodeFile("test1.png");
                            Log.e("test","index:" + index);
                            //把index传给线程，线程再传回handlerMessage，解决index同步问题
                            Thread getHttpBitmapThread = new GetHttpBitmapThread(list.get(0).getPhoto().getFileUrl(),index);
                            getHttpBitmapThread.start();
                            HttpBitmapHandler = new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);

                                Bundle data = msg.getData();
                                Bitmap bitmap = (Bitmap)data.get("bitmap");

                                Marker marker = mAMap.addMarker(getMarkerOptions(msg.what,bitmap));
                                marker.setObject(msg.what);
                                mPoiMarks.add(marker);


                            }

                        };



                        }
                    }
                });

//                Marker marker = mAMap.addMarker(getMarkerOptions(i));
//                marker.setObject(i);
//                mPoiMarks.add(marker);

            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
//    public void getImageData(int index) {
//        BmobQuery<Photo> query = new BmobQuery<>();
//        query.addWhereEqualTo("sceneId", getSceneId(index));
//        Log.e("test", "sceneId clicked:" + getSceneId(index));
//        query.findObjects(new FindListener<Photo>() {
//            @Override
//            public void done(List<Photo> list, BmobException e) {
//                if (e == null) {
//                    Log.e("test", "取图片成功" + list.toString());
//                    BmobFile file = list.get(0).getPhoto();
//                    Log.e("test", "图片名：" + file.getFilename());
//                    Log.e("test", "图片url：" + file.getFileUrl());
//                    Bitmap bitmap = getHttpBitmap(list.get(0).getPhoto().getFileUrl());
//
//                    Message msg = new Message();
//                    msg.what = 1;
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable("bitmap", bitmap);
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
//                }
//            }
//        });
//    }
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

    private MarkerOptions getMarkerOptions(int index,Bitmap bitmap) {
        Marker m;

        return new MarkerOptions()
                .position(ParseUtil.parseLatLng(sceneList.get(index).getSceneLatLng()))

                .icon(getBitmapDescriptor(index,bitmap));
    }
    /**
     * 给第几个Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     * @param index 第几个Marker。
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getBitmapDescriptor(int index, Bitmap bitmap) {
        View view = View.inflate(context, R.layout.custom_view, null);
        TextView textView = ((TextView) view.findViewById(R.id.title));
        ImageView imageView = (ImageView)view.findViewById(R.id.path_photo);

        textView.setText(getTitle(index));
        if(bitmap == null){
            Log.e("test","bitmap为null");

        }else{
            //Log.e("test","bitmap为:" + bitmap.toString());
            imageView.setImageBitmap(bitmap);
            //imageView.setImageResource(R.drawable.view_photo_test);
        }

//        // Log.e("test","index: " + index + getTitle(index));
//        BmobQuery<Photo> query = new BmobQuery<>();
//        query.addWhereEqualTo("sceneId",getSceneId(index));
//        Log.e("test","sceneId clicked:" + getSceneId(index) );
//        query.findObjects(new FindListener<Photo>() {
//            @Override
//            public void done(List<Photo> list, BmobException e) {
//                if(e == null){
//                    Log.e("test","取图片成功" + list.toString());
//                    BmobFile file = list.get(0).getPhoto();
//                    Log.e("test","图片名：" + file.getFilename());
//                    Log.e("test","图片url：" + file.getFileUrl());
//                    Bitmap bitmap = getHttpBitmap(list.get(0).getPhoto().getFileUrl());
//                    imageView.setImageBitmap(bitmap);
//                    //imageView.setImageResource(R.drawable.view_photo_test);
//                    imageView.invalidate();
////                        Thread getHttpBitmapThread = new GetHttpBitmapThread(file.getFileUrl());
////                        getHttpBitmapThread.start();
////                        HttpBitmapHandler = new Handler(){
////                            @Override
////                            public void handleMessage(Message msg) {
////                                super.handleMessage(msg);
////                                Bundle data = msg.getData();
////                                Bitmap bitmap = (Bitmap)data.get("bitmap");
////
////                                imageView.setImageResource(R.drawable.view_photo_test);
////                                Log.e("test","设置图片成功" + bitmap.toString());
////                            }
////
////                        };
//
//                    //                       Glide.with(imageView.getContext()).load(file.getFileUrl()).placeholder(R.drawable.view_photo_test).into(imageView);
////                        File image = new File(PathActivity.this.getExternalCacheDir(), list.get(0).getObjectId()+".PNG");
////                        String path = PathActivity.this.getExternalCacheDir()+"/"+list.get(0).getObjectId()+".PNG";
////                        Bitmap photoBitmap = BitmapFactory.decodeFile(path);
////                        imageView.setImageBitmap(photoBitmap);
////
////                        requestQueue = Volley.newRequestQueue(getApplicationContext());
////                        ImageLoader imageLoader = new ImageLoader(requestQueue, new BitmapCache());
////                        ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView,R.mipmap.ic_launch,R.mipmap.ic_launch);
////                        imageLoader.get(list.get(0).getPhoto().getUrl(), imageListener );
//                }else{
//                    Log.e("test","取图片失败");
//                }
//            }
//        });

        return  BitmapDescriptorFactory.fromView(view);

    }
    //根据url异步加载网络图片
    public class GetHttpBitmapThread extends Thread {
        URL myURL;
        int index;

        /**
         *
         * @param url 图片url
         * @param index
         */
        public GetHttpBitmapThread(String url, int index){
            try {
                this.index = index;
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
                Log.e("test","bitmap转化成功");
                is.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable("bitmap",bitmap);
            Message msg = new Message();
            msg.what = index;
            msg.setData(bundle);
            HttpBitmapHandler.sendMessage(msg);
        }

    }

    //加载网络图片
    public  Bitmap getHttpBitmap(String url) {
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
