package com.fangxu.dragfooterview;

import android.app.Activity;
import android.os.Bundle;

import com.fangxu.library.DragContainer;

/**
 * Created by Administrator on 2016/10/31.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        DragContainer container = (DragContainer) findViewById(R.id.drag_container);
        container.setIconDrawable(getResources().getDrawable(R.drawable.top, null));
    }
}
