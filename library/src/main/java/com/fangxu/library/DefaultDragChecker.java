package com.fangxu.library;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by dear33 on 2016/11/12.
 */
public class DefaultDragChecker implements IDragChecker {
    @Override
    public boolean canDrag(View childView) {
        if (childView instanceof RecyclerView) {
            return recyclerBottom((RecyclerView) childView);
        } else if (childView instanceof HorizontalScrollView) {
            return ((HorizontalScrollView) childView).getChildAt(0).getMeasuredWidth() - childView.getScrollX()
                    <= childView.getMeasuredWidth();
        }

        return true;
    }

    private boolean recyclerBottom(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null || adapter.getItemCount() == 0) {
            return false;
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int lastCompletelyVisibleItemPosition;
        if (layoutManager instanceof GridLayoutManager || layoutManager instanceof LinearLayoutManager) {
            lastCompletelyVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
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
}
