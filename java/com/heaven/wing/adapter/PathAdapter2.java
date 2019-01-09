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
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Eric on 2018/11/29.
 */

public class PathAdapter2 extends  RecyclerView.Adapter<PathAdapter2.MyTVHolder>{
    private List<PathRecord> list;
    private Context mContext;
    Handler HttpBitmapHandler;

    private static final int NOTELIST_MODE_CHECK = 0;
    int mEditMode = NOTELIST_MODE_CHECK;
    private int secret = 0;
    private String title = "";
    private PathAdapter2.OnItemClickListener mOnItemClickListener;

    public PathAdapter2(Context context, List<PathRecord> list) {
        this.list = list;
        this.mContext = context;
    }

    @Override
    public PathAdapter2.MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PathAdapter2.MyTVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_path_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final PathAdapter2.MyTVHolder holder, final int position) {
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


        if (mEditMode == NOTELIST_MODE_CHECK) {
            holder.mCheckBox.setVisibility(View.GONE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            if (path.isSelect()) {
                holder.mCheckBox.setImageResource(R.mipmap.ic_checked);
            } else {
                holder.mCheckBox.setImageResource(R.mipmap.ic_uncheck);
            }
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditMode == 1) {
                    mOnItemClickListener.onItemClickListener(holder.getAdapterPosition(),list);
                }else {
                    Intent intent = new Intent(mContext, PathActivity.class);
                    intent.putExtra("pathId",list.get(position).getId());

                    mContext.startActivity(intent);
                }
            }
        });

    }

    public void notifyAdapter(List<PathRecord> list, boolean isAdd) {
        if (!isAdd) {
            this.list = list;
        } else {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<PathRecord> getMyLiveList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }


    public void setOnItemClickListener(PathAdapter2.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClickListener(int pos, List<PathRecord> myLiveList);
    }
    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MyTVHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView photo,mCheckBox;
        TextView title;

        public MyTVHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.path_card);
            photo = (ImageView) itemView.findViewById(R.id.path_photo_);
            title = (TextView) itemView.findViewById(R.id.path_title);
            mCheckBox = (ImageView) itemView.findViewById(R.id.check_box);
            ButterKnife.inject(this,itemView);
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
