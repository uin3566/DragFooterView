package com.fangxu.dragfooterview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.fangxu.dragfooterview.adapter.ShowMoreAdapter;

/**
 * Created by dear33 on 2016/11/13.
 */
public class ShowMoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_more);
        setTitle("ShowMoreActivity");

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        ShowMoreAdapter adapter = new ShowMoreAdapter(this);
        recyclerView.setAdapter(adapter);
    }
}
