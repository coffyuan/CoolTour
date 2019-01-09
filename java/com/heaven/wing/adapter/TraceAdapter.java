package com.heaven.wing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.heaven.wing.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2018/11/28.
 */

public class TraceAdapter extends  RecyclerView.Adapter<TraceAdapter.MyTVHolder>{
    private List<PoiItem> mPois;
    private AMap mAMap;
    private Context context;
    private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();

    public TraceAdapter(Context context, AMap amap, List<PoiItem> pois) {
        mAMap = amap;
        mPois = pois;
        this.context = context;
    }

    @Override
    public TraceAdapter.MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TraceAdapter.MyTVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_view, parent, false));
    }
    @Override
    public void onBindViewHolder(final TraceAdapter.MyTVHolder holder, final int position){

    }


    @Override
    public int getItemCount() {
        return mPois == null ? 0 : mPois.size();
    }

    public class MyTVHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView photo;
        TextView title;

        public MyTVHolder(View itemView) {
            super(itemView);
            view = (View)itemView.findViewById(R.id.custom_view);
            photo = (ImageView) itemView.findViewById(R.id.path_photo_);
            title = (TextView) itemView.findViewById(R.id.path_title);
        }
    }
//
//    public class GetHttpBitmapThread extends Thread {
//        URL myURL;
//        public GetHttpBitmapThread(String url){
//            try {
//                this.myURL = new URL(url);
//            }catch(MalformedURLException e) {
//                e.printStackTrace();
//            }
//        }
//        @Override
//        public void run(){
//            Bitmap bitmap = null;
//
//            try {
//                HttpURLConnection conn = (HttpURLConnection)myURL.openConnection();
//                conn.setConnectTimeout(0);
//                conn.setDoInput(true);
//                conn.connect();
//                InputStream is = conn.getInputStream();
//                bitmap = BitmapFactory.decodeStream(is);
//                is.close();
//            }catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("bitmap",bitmap);
//            Message msg = new Message();
//            msg.what = 1;
//            msg.setData(bundle);
//            HttpBitmapHandler.sendMessage(msg);
//        }
//
//    }
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
