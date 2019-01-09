package com.heaven.wing.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heaven.wing.R;
import com.heaven.wing.activity.NoteActivity;
import com.heaven.wing.entity.Note;
import com.heaven.wing.entity.Photo;

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
 * Created by 刘康斌 on 2018/11/7.
 */

public class NoteAdapter extends  RecyclerView.Adapter<NoteAdapter.MyTVHolder> {

    private static final int NOTELIST_MODE_CHECK = 0;
    int mEditMode = NOTELIST_MODE_CHECK;
    private int secret = 0;
    private String title = "";
    private OnItemClickListener mOnItemClickListener;


    private List<Note> list;
    private Context mContext;

    public NoteAdapter(Context context, List<Note> list) {
        this.mContext = context;
        this.list = list;
    }

    public void notifyAdapter(List<Note> list, boolean isAdd) {
        if (!isAdd) {
            this.list = list;
        } else {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<Note> getMyLiveList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyTVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_note_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyTVHolder holder, int position) {
        final int j = position;
        final Note note = list.get(position);
 //       holder.photo.setImageDrawable(mContext.getDrawable(note.getImageResourceId(mContext)));
        holder.title.setText(note.getTitle());
        holder.time.setText(note.getTime());
        holder.content.setText(note.getNote());

        BmobQuery<Photo> query = new BmobQuery<>();
        query.addWhereEqualTo("sceneId", note.getSceneId());
        Log.e("test","getsceneID:" + note.getSceneId());
//        query.addWhereEqualTo("userId", "18370982633");
        query.setLimit(50);
        query.findObjects(new FindListener<Photo>() {
            @Override
            public void done(List<Photo> list, BmobException e) {
                if(e==null){
                    if(list.size() > 0) {
                        Bitmap bitmap = getHttpBitmap(list.get(0).getPhoto().getFileUrl());
                        holder.photo.setImageBitmap(bitmap);
                    }else
                        holder.photo.setImageDrawable(mContext.getDrawable(note.getImageResourceId(mContext)));
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });

        if (mEditMode == NOTELIST_MODE_CHECK) {
            holder.mCheckBox.setVisibility(View.GONE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            if (note.isSelect()) {
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
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra("noteId",list.get(j).getObjectId());
                    mContext.startActivity(intent);
                }
            }
        });
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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClickListener(int pos, List<Note> myLiveList);
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
        TextView title,time,content;

        public MyTVHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.note_card);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            title = (TextView) itemView.findViewById(R.id.title);
            time = (TextView) itemView.findViewById(R.id.time);
            content = (TextView) itemView.findViewById(R.id.content);
            mCheckBox = (ImageView) itemView.findViewById(R.id.check_box);
            ButterKnife.inject(this,itemView);

        }
    }

}
