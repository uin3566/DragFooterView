package com.fangxu.dragfooterview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.fangxu.library.DragContainer;

/**
 * Created by Administrator on 2016/10/31.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_normal_drag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NormalDragDemoActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_recycler_drag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecyclerDragDemoActivity.class);
                startActivity(intent);
            }
        });
    }
}
