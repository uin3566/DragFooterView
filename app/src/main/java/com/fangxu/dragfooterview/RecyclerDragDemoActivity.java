package com.fangxu.dragfooterview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fangxu.dragfooterview.adapter.CollectionRecyclerAdapter;
import com.fangxu.dragfooterview.adapter.DraggableRecyclerAdapter;
import com.fangxu.library.DraggableRecyclerView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/2.
 */
public class RecyclerDragDemoActivity extends Activity {
    ArrayList<Integer> list = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_drag_demo);

        //single draggable RecyclerView
        initData();

        DraggableRecyclerView draggableRecyclerView = (DraggableRecyclerView) findViewById(R.id.draggable_recycler_view);
        draggableRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        DraggableRecyclerAdapter adapter = new DraggableRecyclerAdapter();
        adapter.setData(list);

        draggableRecyclerView.setAdapter(adapter);

        //numbers of draggable RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.normal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        CollectionRecyclerAdapter adapter1 = new CollectionRecyclerAdapter(this);
        recyclerView.setAdapter(adapter1);
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
    }
}
