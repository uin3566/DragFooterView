package com.fangxu.library.footer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.fangxu.library.DimenUtil;
import com.fangxu.library.DragContainer;

/**
 * Created by Administrator on 2016/11/17.
 */
public class BezierFooterDrawer extends BaseFooterDrawer {
    private DrawParams drawParams;

    private Paint rectPaint;
    private Paint bezierPaint;
    private Paint textPaint;
    private Path bezierPath;
    private RectF dragRect;
    private float tmpIconPos;

    private float[] bezierParams;
    private float[] iconParams;
    private String[] textRows;

    private IconRotateController iconRotateController;
    private float rotateThreshold;

    private static class DrawParams {
        int textIconGap;
        String normalString;
        String eventString;
        public int textColor;
        float textSize;
        Drawable iconDrawable;
        float iconSize;
        float bezierDragThreshold;
        float rectFooterThick;
    }

    private BezierFooterDrawer(Builder builder) {
        footerRegion = new RectF();

        drawParams = new DrawParams();
        Context context = builder.context;
        drawParams.textSize = DimenUtil.sp2px(context, builder.textSize);
        drawParams.textIconGap = DimenUtil.dp2px(context, builder.textIconGap);
        drawParams.textColor = builder.textColor;
        drawParams.normalString = builder.normalString;
        drawParams.eventString = builder.eventString;
        drawParams.iconDrawable = builder.iconDrawable;
        drawParams.iconSize = DimenUtil.dp2px(context, builder.iconSize);
        drawParams.bezierDragThreshold = DimenUtil.dp2px(context, builder.bezierDragThreshold);
        drawParams.rectFooterThick = DimenUtil.dp2px(context, builder.rectFooterThick);

        rotateThreshold = drawParams.bezierDragThreshold * 0.9f;

        iconRotateController = new IconRotateController(rotateThreshold, builder.iconRotateDuration);

        footerColor = builder.footerColor;

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
        rectPaint.setColor(footerColor);
        rectPaint.setStyle(Paint.Style.FILL);

        dragRect = new RectF();

        bezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bezierPaint.setColor(footerColor);
        bezierPaint.setStyle(Paint.Style.FILL);

        bezierPath = new Path();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(drawParams.textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(drawParams.textSize);
    }

    @Override
    public boolean shouldTriggerEvent(float dragDistance) {
        return dragDistance > rotateThreshold;
    }

    @Override
    public void updateDragState(@DragContainer.DragState int dragState) {
        super.updateDragState(dragState);
        if (dragState == DragContainer.RELEASE) {
            iconRotateController.reset();
        }
    }

    @Override
    public void drawFooter(Canvas canvas, float left, float top, float right, float bottom) {
        super.drawFooter(canvas, left, top, right, bottom);
        drawRect(canvas);
        drawBezier(canvas);
        drawIcon(canvas);
        drawText(canvas);
    }

    private void drawRect(Canvas canvas) {
        if (getDraggedDistance() >= drawParams.rectFooterThick) {
            dragRect.set(footerRegion.right - drawParams.rectFooterThick, 0, footerRegion.right, footerRegion.bottom);
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
        float sx = footerRegion.right - drawParams.rectFooterThick;
        float sy = 0;
        float cy = (footerRegion.bottom - footerRegion.top) / 2;
        float cx;
        if (getDraggedDistance() >= drawParams.bezierDragThreshold) {
            cx = footerRegion.right - drawParams.bezierDragThreshold;
        } else {
            cx = footerRegion.left;
        }
        float ex = footerRegion.right - drawParams.rectFooterThick;
        float ey = footerRegion.bottom;

        bezierParams[0] = sx;
        bezierParams[1] = sy;
        bezierParams[2] = cx;
        bezierParams[3] = cy;
        bezierParams[4] = ex;
        bezierParams[5] = ey;
    }

    private boolean shouldDrawBezier() {
        return getDraggedDistance() >= drawParams.rectFooterThick;
    }

    private void drawIcon(final Canvas canvas) {
        if (drawParams.iconDrawable == null) {
            return;
        }

        setIconPosParams();
        iconRotateController.calculateRotateDegree(dragState, getDraggedDistance());

        canvas.save();
        float degree = iconRotateController.getRotateDegree();
        canvas.rotate(degree, getIconRotateX(drawParams.iconSize), getIconRotateY());

        drawParams.iconDrawable.setBounds((int) iconParams[0], (int) iconParams[1], (int) iconParams[2], (int) iconParams[3]);
        drawParams.iconDrawable.draw(canvas);

        canvas.restore();
    }

    private float getIconRotateX(float drawableSize) {
        float left = iconParams[0];
        return left + drawableSize / 2;
    }

    private float getIconRotateY() {
        return (footerRegion.bottom - footerRegion.top) / 2;
    }

    private void setIconPosParams() {
        float top = getIconRotateY() - drawParams.iconSize / 2;
        float bottom = top + drawParams.iconSize;
        float left, right;
        if (getDraggedDistance() <= rotateThreshold) {
            left = footerRegion.left + (getDraggedDistance()) / 2;
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

    private float getDraggedDistance() {
        return footerRegion.right - footerRegion.left;
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
        tmp = (getDraggedDistance() > rotateThreshold) ? drawParams.eventString : drawParams.normalString;
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

    public static class Builder {
        private static final int DEFAULT_FOOTER_HEIGHT = 20;
        private static final int DEFAULT_BEZIER_DRAG_THRESHOLD = 100;
        private static final int DEFAULT_ICON_SIZE = 10;
        private static final int DEFAULT_TEXT_ICON_GAP = 4;
        private static final int DEFAULT_TEXT_SIZE = 10;
        private static final int DEFAULT_TEXT_COLOR = 0xff222222;
        private static final int DEFAULT_ICON_ROTATE_DURATION = 100;
        private static final String DEFAULT_NORMAL_STRING = "释放查看";
        private static final String DEFAULT_EVENT_STRING = "查看更多";

        private Drawable iconDrawable;
        private int iconRotateDuration = DEFAULT_ICON_ROTATE_DURATION;
        private int textIconGap = DEFAULT_TEXT_ICON_GAP;
        private String normalString = DEFAULT_NORMAL_STRING;
        private String eventString = DEFAULT_EVENT_STRING;
        private int textColor = DEFAULT_TEXT_COLOR;
        private float textSize = DEFAULT_TEXT_SIZE;
        private float iconSize = DEFAULT_ICON_SIZE;
        public float bezierDragThreshold = DEFAULT_BEZIER_DRAG_THRESHOLD;
        public float rectFooterThick = DEFAULT_FOOTER_HEIGHT;

        public final int footerColor;
        public final Context context;

        public Builder(Context context, int footerColor) {
            this.footerColor = footerColor;
            this.context = context;
        }

        public Builder setIconRotateDuration(int iconRotateDuration) {
            this.iconRotateDuration = iconRotateDuration;
            return this;
        }

        public Builder setBezierDragThreshold(float bezierDragThreshold) {
            this.bezierDragThreshold = bezierDragThreshold;
            return this;
        }

        public Builder setRectFooterThick(float rectFooterThick) {
            this.rectFooterThick = rectFooterThick;
            return this;
        }

        public Builder setTextIconGap(int textIconGap) {
            this.textIconGap = textIconGap;
            return this;
        }

        public Builder setNormalString(String normalString) {
            this.normalString = normalString;
            return this;
        }

        public Builder setEventString(String eventString) {
            this.eventString = eventString;
            return this;
        }

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setIconDrawable(Drawable iconDrawable) {
            this.iconDrawable = iconDrawable;
            return this;
        }

        public Builder setIconSize(float iconSize) {
            this.iconSize = iconSize;
            return this;
        }

        public BezierFooterDrawer build() {
            return new BezierFooterDrawer(this);
        }
    }
}
