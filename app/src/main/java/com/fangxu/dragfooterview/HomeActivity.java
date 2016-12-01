package com.fangxu.dragfooterview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fangxu.dragfooterview.adapter.HomeRecyclerAdapter;
import com.fangxu.library.footer.ArrowPathFooterDrawer;
import com.fangxu.library.footer.BezierFooterDrawer;
import com.fangxu.library.DragContainer;
import com.fangxu.library.DragListener;
import com.fangxu.library.footer.NormalFooterDrawer;

/**
 * Created by dear33 on 2016/11/12.
 */
public class HomeActivity extends AppCompatActivity implements DragListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        setTitle("HomeActivity");

        setupImageView();
        setupButton();
        setupTextView();
        setupRecyclerView();
        setupHorizontalScrollView();
    }

    @Override
    public void onDragEvent() {
        Intent intent = new Intent(HomeActivity.this, ShowMoreActivity.class);
        startActivity(intent);
    }

    private void setupImageView() {
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        Glide.with(this).load(Constants.urls[0]).into(imageView);

        DragContainer dragContainer = (DragContainer) findViewById(R.id.drag_image_view);
        dragContainer.setFooterDrawer(new BezierFooterDrawer.Builder(this, 0xffffc000).setIconDrawable(getResources().getDrawable(R.drawable.left)).build());
        dragContainer.setDragListener(this);
    }

    private void setupTextView() {
        DragContainer dragContainer = (DragContainer) findViewById(R.id.drag_text_view);
        dragContainer.setDragListener(this);
    }

    private void setupButton() {
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "onClick", Toast.LENGTH_SHORT).show();
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(HomeActivity.this, "onLongClick", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        DragContainer dragContainer = (DragContainer) findViewById(R.id.drag_button);
        dragContainer.setDragListener(this);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        HomeRecyclerAdapter adapter = new HomeRecyclerAdapter(this);
        recyclerView.setAdapter(adapter);

        DragContainer dragContainer = (DragContainer) findViewById(R.id.drag_recycler_view);
        dragContainer.setFooterDrawer(new NormalFooterDrawer.Builder(this, 0xffffc000).setIconDrawable(getResources().getDrawable(R.drawable.left_2)).build());
        dragContainer.setDragListener(this);
    }

    private void setupHorizontalScrollView() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        for (int i = 10; i < 20; i++) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(120), ViewGroup.LayoutParams.MATCH_PARENT);
            params.leftMargin = 0;
            params.rightMargin = dp2px(5);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(params);
            linearLayout.addView(imageView);
            Glide.with(this).load(Constants.urls[i]).into(imageView);
        }

        DragContainer dragContainer = (DragContainer) findViewById(R.id.drag_scroll_view);
        dragContainer.setFooterDrawer(new ArrowPathFooterDrawer());
        dragContainer.setDragListener(this);
    }

    private int dp2px(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
