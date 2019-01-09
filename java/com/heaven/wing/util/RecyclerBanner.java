package com.heaven.wing.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.heaven.wing.R;
import com.heaven.wing.entity.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 刘康斌 on 2018/11/20.
 */

public class RecyclerBanner extends FrameLayout {

    RecyclerView recyclerView;
    LinearLayout linearLayout;
    GradientDrawable defaultDrawable,selectedDrawable;

    RecyclerAdapter adapter;
    OnPagerClickListener onPagerClickListener;
    private List<Photo> list = new ArrayList<>();

    int size,startX, startY,currentIndex;
    boolean isPlaying;

    public interface OnPagerClickListener{
        void onClick(Photo photo);
    }

    private Handler handler = new Handler();

    private Runnable playTask = new  Runnable(){

        @Override
        public void run() {
            recyclerView.smoothScrollToPosition(++currentIndex);
            changePoint();
            handler.postDelayed(this,3000);
        }
    };

    public RecyclerBanner(Context context) {
        this(context,null);
    }

    public RecyclerBanner(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RecyclerBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        size = (int) (6 * context.getResources().getDisplayMetrics().density + 0.5f);
        defaultDrawable = new GradientDrawable();
        defaultDrawable.setSize(size,size);
        defaultDrawable.setCornerRadius(size);
        defaultDrawable.setColor(0xffffffff);
        selectedDrawable = new GradientDrawable();
        selectedDrawable.setSize(size,size);
        selectedDrawable.setCornerRadius(size);
        selectedDrawable.setColor(0xff0094ff);

        recyclerView = new RecyclerView(context);
        LayoutParams vpLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams linearLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(size * 2,size * 2,size * 2,size * 2);
        linearLayoutParams.gravity = Gravity.BOTTOM;
        addView(recyclerView,vpLayoutParams);
        addView(linearLayout,linearLayoutParams);

        new PagerSnapHelper().attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int first = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int last = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if(currentIndex != (first + last) / 2){
                    currentIndex = (first + last) / 2;
                    changePoint();
                }
            }
        });
    }

    public void setOnPagerClickListener(OnPagerClickListener onPagerClickListener) {
        this.onPagerClickListener = onPagerClickListener;
    }

    public synchronized void setPlaying(boolean playing){
        if(!isPlaying && playing && adapter != null && adapter.getItemCount() > 2){
            handler.postDelayed(playTask,3000);
            isPlaying = true;
        }else if(isPlaying && !playing){
            handler.removeCallbacksAndMessages(null);
            isPlaying = false;
        }
    }

    public int setDatas(List<Photo> datas){
        setPlaying(false);
        this.list.clear();
        linearLayout.removeAllViews();
        if(datas != null){
            this.list.addAll(datas);
        }
        if(this.list.size() > 1){
            currentIndex = this.list.size() * 10000;
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(currentIndex);
            for (int i = 0; i < this.list.size(); i++) {
                ImageView img = new ImageView(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = size/2;
                lp.rightMargin = size/2;
                img.setImageDrawable(i == 0 ? selectedDrawable : defaultDrawable);
                linearLayout.addView(img,lp);
            }
            setPlaying(true);
        }else {
            currentIndex = 0;
            adapter.notifyDataSetChanged();
        }
        return this.list.size();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                setPlaying(false);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();
                int disX = moveX - startX;
                int disY = moveY - startY;
                getParent().requestDisallowInterceptTouchEvent(2 * Math.abs(disX) > Math.abs(disY));
                setPlaying(false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPlaying(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setPlaying(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setPlaying(false);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if(visibility == View.GONE){
            // 停止轮播
            setPlaying(false);
        }else if(visibility == View.VISIBLE){
            // 开始轮播
            setPlaying(true);
        }
        super.onWindowVisibilityChanged(visibility);
    }
    // 内置适配器
    private class RecyclerAdapter extends RecyclerView.Adapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView img = new ImageView(parent.getContext());
            RecyclerView.LayoutParams l = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setLayoutParams(l);
            img.setId(R.id.icon);
            img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onPagerClickListener != null){
                        onPagerClickListener.onClick(list.get(currentIndex % list.size()));
                    }
                }
            });
            return new RecyclerView.ViewHolder(img) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ImageView img = (ImageView) holder.itemView.findViewById(R.id.icon);

            //暂从网络拿取图片，将此换成图片的数据库路径，方法详见https://blog.csdn.net/shangmingchao/article/details/51125554
            Glide.with(img.getContext()).load(list.get(position % list.size()).getPhoto().getFileUrl()).placeholder(R.mipmap.ic_launcher).into(img);
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size() < 2 ? list.size() : Integer.MAX_VALUE;
        }
    }

    private class PagerSnapHelper extends LinearSnapHelper {

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
            int targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
            final View currentView = findSnapView(layoutManager);
            if(targetPos != RecyclerView.NO_POSITION && currentView != null){
                int currentPosition = layoutManager.getPosition(currentView);
                int first = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
                int last = ((LinearLayoutManager)layoutManager).findLastVisibleItemPosition();
                currentPosition = targetPos < currentPosition ? last : (targetPos > currentPosition ? first : currentPosition);
                targetPos = targetPos < currentPosition ? currentPosition - 1 : (targetPos > currentPosition ? currentPosition + 1 : currentPosition);
            }
            return targetPos;
        }
    }

    private void changePoint(){
        if(linearLayout != null && linearLayout.getChildCount() > 0){
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                ((ImageView)linearLayout.getChildAt(i)).setImageDrawable(i == currentIndex % list.size() ? selectedDrawable : defaultDrawable);
            }
        }
    }
}