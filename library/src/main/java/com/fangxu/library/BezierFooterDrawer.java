package com.fangxu.library;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by Administrator on 2016/11/17.
 */
public class BezierFooterDrawer implements IFooterDrawer {
    private RectF footerRegion;
    private DrawParams drawParams;

    private int dragState;

    private Paint rectPaint;
    private Paint bezierPaint;
    private Paint textPaint;
    private Path bezierPath;
    private RectF dragRect;
    private float tmpIconPos;

    private float bezierDragThreshold;
    private float rectFooterThick;

    private ValueAnimator iconRotateAnimator;
    private float rotateThreshold;
    private float iconRotateDegree = 0;
    private boolean everRotatedIcon = false;
    private boolean dragOutRotateAnimatorExecuted = false;
    private boolean dragInRotateAnimatorExecuted = false;

    private float[] bezierParams;
    private float[] iconParams;
    //store text array,if orientation is TOP or BOTTOM,the length of textRows is 1,otherwise is the max length of normalString and eventString
    private String[] textRows;

    public static class DrawParams {
        public int textIconGap;
        public String normalString;
        public String eventString;
        public int footerColor;
        public int textColor;
        public float textSize;
        public Drawable iconDrawable;
        public float iconSize;
    }

    public BezierFooterDrawer(DrawParams drawParams) {
        footerRegion = new RectF();
        this.drawParams = drawParams;

        //store bezier params, params[0] = sx,params[1] = sy, params[2] = cx,params[3] = cy, params[4] = ex,params[5] = ey
        //sx:bezier start point x
        //sy:bezier start point y
        //cx:bezier control point x
        //cy:bezier control point y
        //ex:bezier end point x
        //ey:bezier end point y
        bezierParams = new float[6];

        //store icon position: left, top, right, bottom
        iconParams = new float[4];

        initPaints();
        initTextRows();
    }

    public void setParams(float rectFooterThick, float bezierDragThreshold) {
        this.bezierDragThreshold = bezierDragThreshold;
        this.rotateThreshold = bezierDragThreshold * 0.9f;
        this.rectFooterThick = rectFooterThick;
    }

    private void initTextRows() {
        if (drawParams.normalString == null || drawParams.normalString.isEmpty()) {
            return;
        }

        int normalLength = drawParams.normalString.length();
        int eventLength = normalLength;
        if (drawParams.eventString != null && !drawParams.eventString.isEmpty()) {
            eventLength = drawParams.eventString.length();
        }
        int rowsLength = normalLength > eventLength ? normalLength : eventLength;
        textRows = new String[rowsLength];
    }

    private void initPaints() {
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(drawParams.footerColor);
        rectPaint.setStyle(Paint.Style.FILL);

        dragRect = new RectF();

        bezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bezierPaint.setColor(drawParams.footerColor);
        bezierPaint.setStyle(Paint.Style.FILL);

        bezierPath = new Path();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(drawParams.textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(drawParams.textSize);
    }

    @DragContainer.DragState
    @Override
    public void updateDragState(int dragState) {
        this.dragState = dragState;
        if (dragState == DragContainer.RELEASE) {
            everRotatedIcon = excessRotateThreshold();
            dragOutRotateAnimatorExecuted = false;
            dragInRotateAnimatorExecuted = false;
        }
    }

    @Override
    public void drawFooter(Canvas canvas, float left, float top, float right, float bottom) {
        footerRegion.set(left, top, right, bottom);
        drawRect(canvas);
        drawBezier(canvas);
        drawIcon(canvas);
        drawText(canvas);
    }

    private void drawRect(Canvas canvas) {
        if (footerRegion.right - footerRegion.left >= rectFooterThick) {
            dragRect.set(footerRegion.right - rectFooterThick, 0, footerRegion.right, footerRegion.bottom);
        } else {
            dragRect.set(footerRegion.left, 0, footerRegion.right, footerRegion.bottom);
        }
        canvas.drawRect(dragRect, rectPaint);
    }

    private void drawBezier(Canvas canvas) {
        if (shouldDrawBezier()) {
            setBezierParams();
            bezierPath.reset();
            bezierPath.moveTo(bezierParams[0], bezierParams[1]);
            bezierPath.quadTo(bezierParams[2], bezierParams[3], bezierParams[4], bezierParams[5]);
            canvas.drawPath(bezierPath, bezierPaint);
        }
    }

    private void setBezierParams() {
        float sx = footerRegion.right - rectFooterThick;
        float sy = 0;
        float cy = (footerRegion.bottom - footerRegion.top) / 2;
        float cx;
        if (footerRegion.right - footerRegion.left >= bezierDragThreshold) {
            cx = footerRegion.right - bezierDragThreshold;
        } else {
            cx = footerRegion.left;
        }
        float ex = footerRegion.right - rectFooterThick;
        float ey = footerRegion.bottom;

        bezierParams[0] = sx;
        bezierParams[1] = sy;
        bezierParams[2] = cx;
        bezierParams[3] = cy;
        bezierParams[4] = ex;
        bezierParams[5] = ey;
    }

    private boolean shouldDrawBezier() {
        return footerRegion.right - footerRegion.left >= rectFooterThick;
    }

    private void drawIcon(final Canvas canvas) {
        if (drawParams.iconDrawable == null) {
            return;
        }

        setIconPosParams();

        if (dragState == DragContainer.RELEASE) {
            if (everRotatedIcon && !dragInRotateAnimatorExecuted && !excessRotateThreshold()) {
                startIconRotateAnimator();
            }
        } else if (dragState == DragContainer.DRAG_OUT) {
            if (excessRotateThreshold() && !dragOutRotateAnimatorExecuted) {
                startIconRotateAnimator();
            }
        } else {
            if (!excessRotateThreshold() && !dragInRotateAnimatorExecuted) {
                startIconRotateAnimator();
            }
        }

        canvas.save();
        canvas.rotate(iconRotateDegree, getIconRotateX(drawParams.iconSize), getIconRotateY());

        drawParams.iconDrawable.setBounds((int) iconParams[0], (int) iconParams[1], (int) iconParams[2], (int) iconParams[3]);
        drawParams.iconDrawable.draw(canvas);

        canvas.restore();
    }

    private void startIconRotateAnimator() {
        int duration = 100;
        if (iconRotateAnimator != null && iconRotateAnimator.isRunning()) {
            iconRotateAnimator.cancel();
        }
        if (dragState == DragContainer.DRAG_OUT) {
            dragOutRotateAnimatorExecuted = true;
            dragInRotateAnimatorExecuted = false;
            iconRotateAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        } else {
            dragInRotateAnimatorExecuted = true;
            dragOutRotateAnimatorExecuted = false;
            iconRotateAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        }
        iconRotateAnimator.setDuration(duration);
        iconRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconRotateDegree = (float) animation.getAnimatedValue() * 180;
            }
        });
        iconRotateAnimator.start();
    }

    private float getIconRotateX(float drawableSize) {
        float left = iconParams[0];
        return left + drawableSize / 2;
    }

    private float getIconRotateY() {
        return (footerRegion.bottom - footerRegion.top) / 2;
    }

    private boolean excessRotateThreshold() {
        return footerRegion.right - footerRegion.left > rotateThreshold;
    }

    private void setIconPosParams() {
        float top = getIconRotateY() - drawParams.iconSize / 2;
        float bottom = top + drawParams.iconSize;
        float left, right;
        if (footerRegion.right - footerRegion.left <= rotateThreshold) {
            left = footerRegion.left + (footerRegion.right - footerRegion.left) / 2;
            right = left + drawParams.iconSize;
            tmpIconPos = left;
        } else {
            left = tmpIconPos;
            right = left + drawParams.iconSize;
        }

        iconParams[0] = left;
        iconParams[1] = top;
        iconParams[2] = right;
        iconParams[3] = bottom;
    }

    private void drawText(Canvas canvas) {
        if (drawParams.normalString == null || drawParams.normalString.isEmpty()) {
            return;
        }

        if (drawParams.iconDrawable == null) {
            setIconPosParams();
        }

        float iconTop = iconParams[1];
        float iconRight = iconParams[2];

        float x = iconRight + textPaint.getTextSize() / 2 + drawParams.textIconGap;
        float y = iconTop + drawParams.iconSize / 2;

        setTextRows();
        drawTextInRows(textRows, canvas, x, y);
    }

    private void setTextRows() {
        if (drawParams.eventString == null || drawParams.eventString.isEmpty()) {
            drawParams.eventString = drawParams.normalString;
        }

        String tmp;
        tmp = excessRotateThreshold() ? drawParams.eventString : drawParams.normalString;
        for (int i = 0; i < tmp.length(); i++) {
            textRows[i] = String.valueOf(tmp.charAt(i));
        }
    }

    /**
     * draw texts in rows
     */
    private void drawTextInRows(String[] strings, Canvas canvas, float x, float y) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int length = strings.length;
        float total = (length - 1) * (-top + bottom) + (-fontMetrics.ascent + fontMetrics.descent);
        float offset = total / 2 - bottom;
        for (int i = 0; i < length; i++) {
            float yAxis = -(length - i - 1) * (-top + bottom) + offset;
            canvas.drawText(strings[i], x, y + yAxis, textPaint);
        }
    }
}
