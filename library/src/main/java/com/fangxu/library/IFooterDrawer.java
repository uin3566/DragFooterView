package com.fangxu.library;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2016/11/17.
 */
public interface IFooterDrawer {
    void drawFooter(Canvas canvas, float left, float top, float right, float bottom);
    void updateDragState(int dragState);
}
