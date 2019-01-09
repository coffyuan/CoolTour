package com.heaven.wing.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lenovo on 2018/11/21.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration{

    private int BigSpace;
    private int MiddleSpace;
    private int SmallSpace;

    public SpaceItemDecoration(int BigSpace,int MiddleSpace,int SmallSpace) {
        this.BigSpace = BigSpace;
        this.MiddleSpace = MiddleSpace;
        this.SmallSpace = SmallSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(parent.getChildAdapterPosition(view) % 3 == 0) {
            outRect.top = BigSpace;
            outRect.left = BigSpace;
            outRect.right = SmallSpace;
        }else if(parent.getChildAdapterPosition(view) % 3 == 1) {
            outRect.top = BigSpace;
            outRect.left = MiddleSpace;
            outRect.right = MiddleSpace;
        }else{
            outRect.top = BigSpace;
            outRect.left = SmallSpace;
            outRect.right = BigSpace;
        }
    }
}
