package com.fangxu.dragfooterview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fangxu.dragfooterview.Constants;
import com.fangxu.dragfooterview.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dear33 on 2016/11/13.
 */
public class ShowMoreAdapter extends RecyclerView.Adapter<ShowMoreAdapter.MyHolder> {
    private Context context;
    private Random random;
    private ArrayList<Integer> heights;

    public ShowMoreAdapter(Context context) {
        this.context = context;
        random = new Random(System.currentTimeMillis());
        heights = new ArrayList<>(Constants.urls.length);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stagger_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();

        if (heights.size() <= position) {
            int height = dp2px(context, 200) + random.nextInt(dp2px(context, 50));
            heights.add(height);
        }

        layoutParams.height = heights.get(position);
        holder.itemView.setLayoutParams(layoutParams);

        Glide.with(context).load(Constants.urls[position]).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return Constants.urls.length;
    }

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
        }
    }
}
