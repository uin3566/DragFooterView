package com.fangxu.dragfooterview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/11/2.
 */
public class NormalDragDemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_normal_drag_demo);
        findViewById(R.id.tv_single).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NormalDragDemoActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
