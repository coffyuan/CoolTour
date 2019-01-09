//package com.heaven.wing.util;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.heaven.wing.activity.JourneyActivity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static cn.bmob.newim.util.IMLogger.init;
//import static cn.bmob.v3.Bmob.getApplicationContext;
//
///**
// * 检验工具类、如判断是否联网、是否开启相关权限，动态开启权限等
// * Created by Eric on 2018/11/20.
// */
//
//public class CheckUtil {
//
//    //权限申请标识
//    private static final int BAIDU_READ_PHONE_STATE =100;
//    //动态权限申请集合
//    String[] permissions = new String[]{
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.READ_PHONE_STATE
//    };
//    // 声明一个集合，用来存储用户拒绝授权的权
//    List<String> mPermissionList = new ArrayList<>();
//
//    public void showContacts(){
//        mPermissionList.clear();
//        for (int i = 0; i < permissions.length; i++) {
//            //把未授权的权限添加进未授权数组中
//            if (ContextCompat.checkSelfPermission(JourneyActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
//                mPermissionList.add(permissions[i]);
//                Log.e("hello"," " + "a");
//            }
//        }
//        //未授予的权限为空，表示都授予了
//        if (mPermissionList.isEmpty()) {
//            Toast.makeText(JourneyActivity.this,"已经授权",Toast.LENGTH_LONG).show();
//            init();
//            initpolyline();//为定位方法,//开始定位
//        } else {
//            //请求权限方法
//            //Log.e("hello"," " + "c");//test
//            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
//            ActivityCompat.requestPermissions(JourneyActivity.this, permissions, BAIDU_READ_PHONE_STATE);
//
//        }
//    }
//
//
//    //Android6.0申请权限的回调方法
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        Log.e("hello"," " + "b");
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
//            case BAIDU_READ_PHONE_STATE:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
//                    init();
//                    initpolyline();
//                } else {
//                    // 没有获取到权限，做特殊处理
//                    //Log.e("hello"," " + "f");//test
//                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
//                    init();
//                    initpolyline();
//                }
//                break;
//            default:
//                break;
//        }
//    }
//}
