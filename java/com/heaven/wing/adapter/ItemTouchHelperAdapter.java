package com.heaven.wing.adapter;

/**
 * Created by 刘康斌 on 2018/11/12.
 */

public interface ItemTouchHelperAdapter {
    //移动item
    void onItemMove(int fromPosition,int toPosition);
    //删除item
    void onItemDelete(int position);
}
