package com.fangxu.dragfooterview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fangxu.dragfooterview.R;
import com.fangxu.dragfooterview.Constants;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/2.
 */
public class HomeRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> urlList = new ArrayList<>();
    private Context context;

    public HomeRecyclerAdapter(Context context) {
        this.context = context;
        for (int i = 1; i < 10; i++) {
            urlList.add(Constants.urls[i]);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        Glide.with(context).load(urlList.get(i)).into((ImageView) viewHolder.itemView);
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }
}
