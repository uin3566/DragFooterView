package com.fangxu.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/10/31.
 */
public class DragFooterView extends View {
    private Paint paint;
    private Path path;
    private int width, height;

    private int quandControlY;

    public DragFooterView(Context context) {
        this(context, null);
    }

    public DragFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);

        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (quandControlY > 0) {
            path.moveTo(0, 0);
            path.quadTo((float)width / 2.0f, quandControlY, width, 0);
            canvas.drawPath(path, paint);
        }
    }

    public void reset() {
        quandControlY = 0;
    }

    public void updateController(int dy) {
        Log.d("sbsbdsb", "quandControlY=" + quandControlY);
        quandControlY += dy;
        invalidate();
    }
}
