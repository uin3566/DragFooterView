package com.fangxu.dragfooterview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fangxu.dragfooterview.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/2.
 */
public class DraggableRecyclerAdapter extends RecyclerView.Adapter<DraggableRecyclerAdapter.MyHolder> {

    private ArrayList<Integer> list = new ArrayList<>();

    public void setData(ArrayList<Integer> datas) {
        if (datas != null) {
            list.addAll(datas);
        }
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_recycler_item, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder viewHolder, int i) {
        viewHolder.textView.setText(String.valueOf("No." + list.get(i)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv_item);
        }
    }
}
