package com.fangxu.library;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2016/11/2.
 */
public class DraggableRecyclerView extends RecyclerView {
    private int[] into;

    public DraggableRecyclerView(Context context) {
        this(context, null);
    }

    public DraggableRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
        }
    }

    private boolean reachedBottom() {
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter == null || adapter.getItemCount() == 0) {
            return false;
        }

        LayoutManager layoutManager = getLayoutManager();
        int lastCompletelyVisibleItemPosition;
        if (layoutManager instanceof GridLayoutManager || layoutManager instanceof LinearLayoutManager) {
            lastCompletelyVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(into);
            int max = into[0];
            for (int value : into) {
                if (value > max) {
                    max = value;
                }
            }
            lastCompletelyVisibleItemPosition = max;
        } else {
            throw new IllegalArgumentException("still not support other LayoutManager");
        }

        return lastCompletelyVisibleItemPosition == adapter.getItemCount() - 1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (reachedBottom()) {
            requestDisallowInterceptTouchEvent(false);
            return false;
        }
        return super.onTouchEvent(e);
    }
}
