package com.fangxu.library.footer;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2016/11/17.
 */
public abstract class BaseFooterDrawer {
    protected int dragState;
    protected RectF footerRegion;
    protected int footerColor;

    public abstract boolean shouldTriggerEvent(float dragDistance);

    //called in onDraw() method
    public void drawFooter(Canvas canvas, float left, float top, float right, float bottom) {
        if (footerRegion == null) {
            throw new NullPointerException("footerRegion is null, should be initialize in constructor");
        }
        footerRegion.set(left, top, right, bottom);
    }

    public void updateDragState(int dragState) {
        this.dragState = dragState;
    }
}
