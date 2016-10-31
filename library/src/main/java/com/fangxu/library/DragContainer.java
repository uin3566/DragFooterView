package com.fangxu.library;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 2016/10/31.
 */
public class DragContainer extends ViewGroup {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT, RIGHT, TOP, BOTTOM})
    public @interface FooterOrientation {

    }

    private int orientation;

    private int containerWidth, containerHeight;
    private float downY;

    private View mainView;
    private DragFooterView dragFooterView;

    public DragContainer(Context context) {
        this(context, null);
    }

    public DragContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(BOTTOM);
    }

    @FooterOrientation
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(@FooterOrientation int orientation) {
        this.orientation = orientation;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        switch (orientation) {
            case LEFT:
                dragFooterView = (DragFooterView)getChildAt(0);
                mainView = getChildAt(1);
                break;
            case RIGHT:
                mainView = getChildAt(0);
                dragFooterView = (DragFooterView)getChildAt(1);
                break;
            case TOP:
                dragFooterView = (DragFooterView)getChildAt(0);
                mainView = getChildAt(1);
                break;
            case BOTTOM:
                mainView = getChildAt(0);
                dragFooterView = (DragFooterView)getChildAt(1);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        checkChildren();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.AT_MOST) {
            width = mainView.getMeasuredWidth();
        } else {
            width = widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            height = mainView.getMeasuredHeight();
        } else {
            height = heightSize;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        checkChildren();

        mainView.layout(0, 0, containerWidth, containerHeight);
        layoutFooter(changed, l, r, t, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = event.getY() - downY;
                setMainView(0, (int)dy, containerWidth, containerHeight + (int)dy);
                if (Math.abs(dy) < dragFooterView.getHeight()) {
                    setFooterView(0, containerHeight + (int)dy, containerWidth, containerHeight + dragFooterView.getHeight() + (int)dy);
                } else {
                    setFooterView(0, containerHeight - dragFooterView.getHeight(), containerWidth, containerHeight);
                    dragFooterView.updateController((int)dy - dragFooterView.getHeight());
                }
                break;
            case MotionEvent.ACTION_UP:
                setMainView(0, 0, containerWidth, containerHeight);
                setFooterView(0, containerHeight, dragFooterView.getWidth(), containerHeight + dragFooterView.getHeight());
                dragFooterView.reset();
                break;
        }
        return true;
    }

    private void setMainView(int left, int top, int right, int bottom) {
        mainView.setLeft(left);
        mainView.setTop(top);
        mainView.setRight(right);
        mainView.setBottom(bottom);
    }

    private void setFooterView(int left, int top, int right, int bottom) {
        dragFooterView.setLeft(left);
        dragFooterView.setTop(top);
        dragFooterView.setRight(right);
        dragFooterView.setBottom(bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        containerWidth = w;
        containerHeight = h;
    }

    private void checkChildren() {
        int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalStateException("DragContainer must hold two child, check how many child you put in DragContainer");
        }
    }

    private void layoutFooter(boolean changed, int l, int t, int r, int b) {
        int footerWidth = dragFooterView.getMeasuredWidth();
        int footerHeight = dragFooterView.getMeasuredHeight();
        switch (orientation) {
            case LEFT:
                dragFooterView.layout(-footerWidth, 0, 0, footerHeight);
                break;
            case RIGHT:
                dragFooterView.layout(0, 0, footerWidth, footerHeight);
                break;
            case TOP:
                dragFooterView.layout(0, -footerHeight, footerWidth, 0);
                break;
            case BOTTOM:
                dragFooterView.layout(0, getMeasuredHeight(), footerWidth, getMeasuredHeight() + footerHeight);
                break;
        }
    }
}
