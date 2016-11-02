package com.fangxu.dragfooterview.adapter;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.fangxu.dragfooterview.R;
import com.fangxu.library.DragContainer;
import com.fangxu.library.DraggableRecyclerView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/2.
 */
public class CollectionRecyclerAdapter extends RecyclerView.Adapter<CollectionRecyclerAdapter.MyHolder> {

    private Context context;

    public CollectionRecyclerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.collection_recycler_item, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder viewHolder, int i) {
        setData(viewHolder.recyclerView);
    }

    private void setData(DraggableRecyclerView draggableRecyclerView) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        draggableRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        DraggableRecyclerAdapter adapter = new DraggableRecyclerAdapter();
        adapter.setData(list);

        draggableRecyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        private DraggableRecyclerView recyclerView;
        private GestureDetectorCompat gestureDetector;

        public MyHolder(View itemView) {
            super(itemView);
            recyclerView = (DraggableRecyclerView) itemView.findViewById(R.id.draggable_recycler_view);

            gestureDetector = new GestureDetectorCompat(context, new GestureListener());
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
                }
            });
        }

        private class GestureListener implements GestureDetector.OnGestureListener {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                boolean isVertical = (Math.abs(distanceX) < Math.abs(distanceY));
                ViewGroup parent = (ViewGroup) recyclerView.getParent();
                while (!(parent instanceof DragContainer)) {
                    parent = (ViewGroup) parent.getParent();
                }

                while (!(parent.getParent() instanceof RecyclerView)) {
                    parent = (ViewGroup) parent.getParent();
                }
                if (isVertical) {
                    parent.requestDisallowInterceptTouchEvent(false);
                } else {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        }
    }
}
