package com.heaven.wing.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heaven.wing.R;
import com.heaven.wing.activity.PathActivity;
import com.heaven.wing.entity.PathRecord;
import com.heaven.wing.entity.Photo;
import com.heaven.wing.util.ParseUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by 刘康斌 on 2018/11/7.
 */

public class PathAdapter extends  RecyclerView.Adapter<PathAdapter.MyTVHolder>{

    private List<PathRecord> list;
    private Context mContext;
    Handler HttpBitmapHandler;

    public PathAdapter(Context context, List<PathRecord> list) {
        this.list = list;
        this.mContext = context;
    }

    @Override
    public PathAdapter.MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyTVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_path_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyTVHolder holder, final int position) {
        final int j = position;
        Log.e("test","position:" + position);
        PathRecord path = list.get(position);
        Log.e("test","city:" + path.getCity());
//      holder.photo.setImageResource(R.drawable.ic_girl);
        holder.title.setText(path.getCity() +" " + ParseUtil.parseDate(path.getDate()));

        BmobQuery<Photo> query = new BmobQuery<>();
        query.addWhereEqualTo("traceId", path.getId());
        query.setLimit(50);
        query.findObjects(new FindListener<Photo>() {
            @Override
            public void done(List<Photo> list, BmobException e) {
                if(e==null){
                    if(list.size() > 0) {

                        Bitmap bitmap = getHttpBitmap(list.get(0).getPhoto().getFileUrl());
                        holder.photo.setImageBitmap(bitmap);

//                        final Thread getHttpBitmapThread = new GetHttpBitmapThread(list.get(0).getPhoto().getFileUrl());
//                        getHttpBitmapThread.start();
//                        HttpBitmapHandler = new Handler(){
//                            @Override
//                            public void handleMessage(Message msg) {
//                                super.handleMessage(msg);
//                                Bundle data = msg.getData();
//                                Bitmap bitmap = (Bitmap)data.get("bitmap");
//
//                                holder.photo.setImageBitmap(bitmap);
//                                Log.e("test","设置图片成功" + bitmap.toString());
//                            }
//
//                        };
                    }else{
                        holder.photo.setImageResource(R.drawable.view_default);
                        Log.i("bmob","设置默认图片成功：");
                    }

                }else{
                    Log.i("bmob","取图片失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PathActivity.class);
                intent.putExtra("pathId",list.get(position).getId());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MyTVHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView photo;
        TextView title;

        public MyTVHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.path_card);
            photo = (ImageView) itemView.findViewById(R.id.path_photo_);
            title = (TextView) itemView.findViewById(R.id.path_title);
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
