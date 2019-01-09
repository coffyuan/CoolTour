package com.heaven.wing.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.heaven.wing.R;
import com.heaven.wing.entity.Photo;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by 刘康斌 on 2018/11/7.
 */

public class PhotoAdapter extends  RecyclerView.Adapter<PhotoAdapter.MyTVHolder> {

    private static final int NOTELIST_MODE_CHECK = 0;
    int mEditMode = NOTELIST_MODE_CHECK;
    private int secret = 0;
    private String title = "";
    private OnItemClickListener mOnItemClickListener;

    private List<Photo> list;
    private String userId;
    private String sceneId;
    private List<PhotoInfo> mList;
    private Context mContext;

    public PhotoAdapter(Context context, List<PhotoInfo> mList,List<Photo> list,String userId,String sceneId) {
        this.sceneId = sceneId;
        this.userId = userId;
        this.mList = mList;
        this.list = list;
        this.mContext = context;
    }

    public void notifyAdapter(List<Photo> list, boolean isAdd) {
        if (!isAdd) {
            this.list = list;
        } else {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<Photo> getMyLiveList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyTVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_photo_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyTVHolder holder,final int position) {
        if(mList.size() == 0) {
            Glide.with(holder.photo.getContext()).load(list.get(position % list.size()).getPhoto().getFileUrl()).placeholder(R.mipmap.ic_launcher).into(holder.photo);
        }else {
            BmobQuery<Photo> query = new BmobQuery<>();
            query.addWhereEqualTo("sceneId", sceneId);
            query.setLimit(50);
            query.findObjects(new FindListener<Photo>() {
                @Override
                public void done(List<Photo> list, BmobException e) {
                    if(e==null){
                        Glide.with(holder.photo.getContext()).load(list.get(position % list.size()).getPhoto().getFileUrl()).placeholder(R.mipmap.ic_launcher).into(holder.photo);
                    }else{
                        Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                    }
                }
            });
        }

        Photo photo = list.get(position);

        if (mEditMode == NOTELIST_MODE_CHECK) {
            holder.mCheckBox.setVisibility(View.GONE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);

            if (photo.isSelect()) {
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
                    /*点击使图片变大*/
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClickListener(int pos, List<Photo> myLiveList);
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

        public MyTVHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.photo_card);
            photo = (ImageView) itemView.findViewById(R.id.scene_photo);
            mCheckBox = (ImageView) itemView.findViewById(R.id.check_box);
            ButterKnife.inject(this,itemView);
        }
    }



}
