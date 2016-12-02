package com.fangxu.dragfooterview.customfooters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.fangxu.library.DimenUtil;
import com.fangxu.library.DragContainer;
import com.fangxu.library.footer.BaseFooterDrawer;
import com.fangxu.library.footer.IconRotateController;

/**
 * Created by Administrator on 2016/11/24.
 */

public class NormalFooterDrawer extends BaseFooterDrawer {
    private Drawable iconDrawable;
    private float iconWidth, iconHeight;
    private float iconMarginEdge;
    private float rotateThreshold;

    private Paint rectPaint;

    private IconRotateController iconRotateController;

    private NormalFooterDrawer(Builder builder) {
        footerRegion = new RectF();

        this.footerColor = builder.footerColor;
        this.iconWidth = DimenUtil.dp2px(builder.context, builder.iconWidth);
        this.iconHeight = DimenUtil.dp2px(builder.context, builder.iconHeight);
        this.iconMarginEdge = DimenUtil.dp2px(builder.context, builder.iconMarginLeftEdge);
        this.iconDrawable = builder.iconDrawable;

        this.rotateThreshold = DimenUtil.dp2px(builder.context, builder.rotateThreshold);
        iconRotateController = new IconRotateController(rotateThreshold, builder.iconRotateDuration);

        initPaints();
    }

    private void initPaints() {
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(footerColor);
        rectPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void drawFooter(Canvas canvas, float left, float top, float right, float bottom) {
        super.drawFooter(canvas, left, top, right, bottom);
        drawRect(canvas);
        drawIcon(canvas);
    }

    private void drawRect(Canvas canvas) {
        canvas.drawRect(footerRegion, rectPaint);
    }

    private void drawIcon(Canvas canvas) {
        if (iconDrawable == null) {
            return;
        }

        iconRotateController.calculateRotateDegree(dragState, footerRegion.right - footerRegion.left);

        int left = (int) footerRegion.left + (int) iconMarginEdge;
        int right = left + (int) iconWidth;
        int top = (int) ((footerRegion.bottom - footerRegion.top) / 2) - (int) (iconHeight / 2);
        int bottom = top + (int) iconHeight;

        canvas.save();
        float degree = iconRotateController.getRotateDegree();

        canvas.rotate(degree, left + iconWidth / 2, top + iconHeight / 2);

        iconDrawable.setBounds(left, top, right, bottom);
        iconDrawable.draw(canvas);

        canvas.restore();
    }

    @Override
    public void updateDragState(int dragState) {
        super.updateDragState(dragState);
        if (dragState == DragContainer.RELEASE) {
            iconRotateController.reset();
        }
    }

    @Override
    public boolean shouldTriggerEvent(float dragDistance) {
        return dragDistance > rotateThreshold;
    }

    public static class Builder {
        private static final int DEFAULT_ICON_WIDTH = 40;
        private static final int DEFAULT_ICON_HEIGHT = 15;
        private static final int DEFAULT_ICON_MARGIN_EDGE = 8;
        private static final int DEFAULT_ROTATE_THRESHOLD = 100;
        private static final int DEFAULT_ICON_ROTATE_DURATION = 100;

        private final int footerColor;
        private final Context context;

        private Drawable iconDrawable;
        private float iconWidth = DEFAULT_ICON_WIDTH;
        private float iconHeight = DEFAULT_ICON_HEIGHT;
        private float iconMarginLeftEdge = DEFAULT_ICON_MARGIN_EDGE;
        private float rotateThreshold = DEFAULT_ROTATE_THRESHOLD;
        private int iconRotateDuration = DEFAULT_ICON_ROTATE_DURATION;

        public Builder(Context context, int footerColor) {
            this.footerColor = footerColor;
            this.context = context;
        }

        public Builder setIconRotateDuration(int iconRotateDuration) {
            this.iconRotateDuration = iconRotateDuration;
            return this;
        }

        public Builder setRotateThreshold(float rotateThreshold) {
            this.rotateThreshold = rotateThreshold;
            return this;
        }

        public Builder setIconMarginLeftEdge(float iconMarginLeftEdge) {
            this.iconMarginLeftEdge = iconMarginLeftEdge;
            return this;
        }

        public Builder setIconDrawable(Drawable iconDrawable) {
            this.iconDrawable = iconDrawable;
            return this;
        }

        public Builder setIconWidth(float iconWidth) {
            this.iconWidth = iconWidth;
            return this;
        }

        public Builder setIconHeight(float iconHeight) {
            this.iconHeight = iconHeight;
            return this;
        }

        public NormalFooterDrawer build() {
            return new NormalFooterDrawer(this);
        }
    }
}
