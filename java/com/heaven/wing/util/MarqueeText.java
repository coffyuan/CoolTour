package com.heaven.wing.util;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by 刘康斌 on 2018/11/12.
 * 跑马灯
 */

public class MarqueeText extends TextView {

    public MarqueeText(Context context) {
           super(context);
    }

    public  MarqueeText(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public  MarqueeText(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused,int direction,Rect previouslyFocusedRect) {

    }
}
