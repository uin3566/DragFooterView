package com.fangxu.dragfooterview.customfooters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;

import com.fangxu.library.DimenUtil;
import com.fangxu.library.DragContainer;
import com.fangxu.library.footer.BaseFooterDrawer;
import com.fangxu.library.footer.IconRotateController;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/1.
 */

public class ArrowPathFooterDrawer extends BaseFooterDrawer {
    private static final String TAG = "ArrowPathFooterDrawer";

    //arrow
    private boolean hasInitArrowPath = false;
    private PathMeasure arrowPathMeasure;
    private Path arrowPath;
    private Path drawingArrowPath = new Path();
    private IconRotateController rotateController;
    private ArrowPathParams arrowPathParams;

    //hintText string
    private String hintText;
    private ArrayList<float[]> hintLineList;
    private ArrayList<Path> hintPathList;
    private PathMeasure hintPathMeasure;
    private Path drawingHintPath;
    private float hintOffsetX, hintOffsetY;
    private float hintWidth, hintHeight;
    private boolean hasBeginDrawArrowPath;

    private Paint pathPaint;
    private Paint bgPaint;
    private float rotateThreshold;

    private static class ArrowPathParams {
        float finishDrawArrowPathDragDistance;
        float arrowHeadLength;
        float arrowHeadHeight;
        float arrowTailHeight;
        float arrowTailLength;
        float arrowSmallRectLength;
        float arrowSmallRectMargin;
        float arrowMarginLeft;
        float arrowMarginRight;
        float arrowRightMostPosX;
        float arrowTailPosY;
    }

    private ArrowPathFooterDrawer(Builder builder) {
        hintText = builder.hintText;
        footerColor = builder.footerColor;

        footerRegion = new RectF();
        rotateThreshold = DimenUtil.dp2px(builder.context, builder.rotateThreshold);
        rotateController = new IconRotateController(rotateThreshold, builder.rotateDuration);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(builder.pathColor);
        pathPaint.setStrokeWidth(DimenUtil.dp2px(builder.context, builder.pathWidth));

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(footerColor);
        bgPaint.setStyle(Paint.Style.FILL);

        setArrowPathParams(builder);
        setHintString();
    }

    private void setArrowPathParams(Builder builder) {
        arrowPathParams = new ArrowPathParams();
        Context context = builder.context;
        arrowPathParams.finishDrawArrowPathDragDistance = DimenUtil.dp2px(context, builder.finishDrawArrowPathDragDistance);
        arrowPathParams.arrowHeadLength = DimenUtil.dp2px(context, builder.arrowHeadLength);
        arrowPathParams.arrowHeadHeight = DimenUtil.dp2px(context, builder.arrowHeadHeight);
        arrowPathParams.arrowTailHeight = DimenUtil.dp2px(context, builder.arrowTailHeight);
        arrowPathParams.arrowTailLength = DimenUtil.dp2px(context, builder.arrowTailLength);
        arrowPathParams.arrowSmallRectLength = DimenUtil.dp2px(context, builder.arrowSmallRectLength);
        arrowPathParams.arrowSmallRectMargin = DimenUtil.dp2px(context, builder.arrowSmallRectMargin);
        arrowPathParams.arrowMarginLeft = DimenUtil.dp2px(context, builder.arrowMarginLeft);
        arrowPathParams.arrowMarginRight = DimenUtil.dp2px(context, builder.arrowMarginRight);
    }

    public void setHintString() {
        if (hintText == null || hintText.isEmpty()) {
            return;
        }

        hintPathMeasure = new PathMeasure();
        drawingHintPath = new Path();
        hintLineList = StoreHousePath.getPath(hintText, 65 * 0.01f, 24);
        setHintWidthAndHeight();
        initHintPathArray();
    }

    private void setHintWidthAndHeight() {
        for (int i = 0; i < hintLineList.size(); i++) {
            float[] line = hintLineList.get(i);
            hintWidth = Math.max(hintWidth, line[0]);
            hintWidth = Math.max(hintWidth, line[2]);

            hintHeight = Math.max(hintHeight, line[1]);
            hintHeight = Math.max(hintHeight, line[3]);
        }
    }

    private void initHintPathArray() {
        hintPathList = new ArrayList<>(hintLineList.size());
        for (int i = 0; i < hintLineList.size(); i++) {
            float[] line = hintLineList.get(i);
            Path path = new Path();
            path.moveTo(line[0], line[1]);
            path.lineTo(line[2], line[3]);
            hintPathList.add(path);
        }
    }

    @Override
    public void drawFooter(Canvas canvas, float left, float top, float right, float bottom) {
        super.drawFooter(canvas, left, top, right, bottom);
        drawRect(canvas);
        drawArrow(canvas);
        drawHintText(canvas);
    }

    private void drawRect(Canvas canvas) {
        canvas.drawRect(footerRegion, bgPaint);
    }

    private void drawArrow(Canvas canvas) {
        initArrowPath();
        drawArrowPath(canvas);
    }

    private void drawHintText(Canvas canvas) {
        if (hintText == null || !hasBeginDrawArrowPath) {
            return;
        }

        setStringOffset();

        float moveX = getDragDistance() - getArrowTotalLength() - arrowPathParams.arrowMarginLeft;
        float percent = getDragPercent(moveX);

        canvas.save();
        canvas.translate(hintOffsetX, hintOffsetY);
        for (int i = 0; i < hintLineList.size(); i++) {
            Path path = hintPathList.get(i);
            drawingHintPath.reset();
            hintPathMeasure.setPath(path, false);
            hintPathMeasure.getSegment(0, percent * hintPathMeasure.getLength(), drawingHintPath, true);
            canvas.drawPath(drawingHintPath, pathPaint);
        }
        canvas.restore();
    }

    private void setStringOffset() {
        hintOffsetX = arrowPathParams.arrowRightMostPosX + arrowPathParams.arrowMarginRight;
        hintOffsetY = footerRegion.top + (footerRegion.bottom - footerRegion.top - hintHeight) / 2;
    }

    private void initArrowPath() {
        if (hasInitArrowPath) {
            return;
        }

        arrowPathMeasure = new PathMeasure();
        arrowPath = new Path();

        arrowPathParams.arrowRightMostPosX = footerRegion.right;
        arrowPathParams.arrowTailPosY = (footerRegion.bottom - footerRegion.top) / 2 - arrowPathParams.arrowTailHeight / 2;

        setArrowPath(arrowPathParams.arrowRightMostPosX, arrowPathParams.arrowTailPosY);

        hasInitArrowPath = true;
    }

    //set arrow path
    private void setArrowPath(float rightMostPosX, float tailPosY) {
        arrowPath.reset();

        float arrowHeadDy = (arrowPathParams.arrowHeadHeight - arrowPathParams.arrowTailHeight) / 2;
        float arrowTailLength = arrowPathParams.arrowTailLength;
        float arrowTailHeight = arrowPathParams.arrowTailHeight;
        float rectLength = arrowPathParams.arrowSmallRectLength;
        float rectMargin = arrowPathParams.arrowSmallRectMargin;

        float tailPos = rightMostPosX - (arrowPathParams.arrowSmallRectLength + arrowPathParams.arrowSmallRectMargin) * 2;
        arrowPath.moveTo(tailPos, tailPosY);
        arrowPath.lineTo(tailPos - arrowTailLength, tailPosY);
        arrowPath.lineTo(tailPos - arrowTailLength, tailPosY - arrowHeadDy);
        arrowPath.lineTo(tailPos - arrowTailLength - arrowPathParams.arrowHeadLength, tailPosY + arrowTailHeight / 2);
        arrowPath.lineTo(tailPos - arrowTailLength, tailPosY + arrowTailHeight + arrowHeadDy);
        arrowPath.lineTo(tailPos - arrowTailLength, tailPosY + arrowTailHeight);
        arrowPath.lineTo(tailPos, tailPosY + arrowTailHeight);
        arrowPath.lineTo(tailPos, tailPosY);

        arrowPath.addRect(tailPos + rectMargin, tailPosY, tailPos + rectMargin + rectLength, tailPosY + arrowTailHeight, Path.Direction.CCW);
        arrowPath.addRect(tailPos + rectMargin * 2 + rectLength, tailPosY, tailPos + 2 * (rectMargin + rectLength), tailPosY + arrowTailHeight, Path.Direction.CW);

        arrowPathMeasure.setPath(arrowPath, false);
    }

    private float getDragDistance() {
        return footerRegion.right - footerRegion.left;
    }

    private float getArrowTotalLength() {
        return arrowPathParams.arrowHeadLength + arrowPathParams.arrowTailLength
                + arrowPathParams.arrowSmallRectMargin * 2 + arrowPathParams.arrowSmallRectLength * 2;
    }

    private float getDragPercent(float moveX) {
        float percent;
        if (moveX < arrowPathParams.finishDrawArrowPathDragDistance) {
            percent = moveX / arrowPathParams.finishDrawArrowPathDragDistance;
        } else {
            percent = 1.0f;
        }

        return percent;
    }

    private void drawArrowPath(Canvas canvas) {
        float arrowTotalLength = getArrowTotalLength();
        float arrowMarginLeft = arrowPathParams.arrowMarginLeft;
        float dragDx = getDragDistance();

        hasBeginDrawArrowPath = false;
        if (dragDx < arrowMarginLeft + arrowTotalLength) {
            return;
        }

        hasBeginDrawArrowPath = true;
        drawingArrowPath.reset();

        float moveX = dragDx - arrowTotalLength - arrowMarginLeft;
        float percent = getDragPercent(moveX);

        //move arrow
        arrowPathParams.arrowRightMostPosX = footerRegion.right - moveX;
        setArrowPath(arrowPathParams.arrowRightMostPosX
                , arrowPathParams.arrowTailPosY);

        arrowPathMeasure.getSegment(0, percent * arrowPathMeasure.getLength(), drawingArrowPath, true);
        while (arrowPathMeasure.nextContour()) {
            arrowPathMeasure.getSegment(0, percent * arrowPathMeasure.getLength(), drawingArrowPath, true);
        }

        //rotate arrow
        canvas.save();
        rotateController.calculateRotateDegree(dragState, getDragDistance());
        float degree = rotateController.getRotateDegree();
        float centerX = arrowPathParams.arrowRightMostPosX - arrowTotalLength / 2;
        float centerY = arrowPathParams.arrowTailPosY + arrowPathParams.arrowTailHeight / 2;
        canvas.rotate(degree, centerX, centerY);
        canvas.drawPath(drawingArrowPath, pathPaint);
        canvas.restore();
    }

    @Override
    public void updateDragState(int dragState) {
        super.updateDragState(dragState);
        if (dragState == DragContainer.RELEASE) {
            rotateController.reset();
        }
    }

    @Override
    public boolean shouldTriggerEvent(float dragDistance) {
        return dragDistance > rotateThreshold;
    }

    public static class Builder {
        private static final int DEFAULT_ROTATE_DURATION = 150;
        private static final float DEFAULT_ARROW_MARGIN = 8;
        private static final float DEFAULT_ROTATE_THRESHOLD = 100;
        private static final float DEFAULT_ARROW_HEAD_LENGTH = 15;
        private static final float DEFAULT_ARROW_HEAD_HEIGHT = 15;
        private static final float DEFAULT_ARROW_TAIL_LENGTH = 10;
        private static final float DEFAULT_ARROW_TAIL_HEIGHT = 5;
        private static final float DEFAULT_ARROW_SMALL_RECT_LENGTH = 2.5f;
        private static final float DEFAULT_ARROW_SMALL_RECT_MARGIN = 2.5f;
        private static final float DEFAULT_FINISH_DRAW_ARROW_PATH_DRAG_DISTANCE = 60;
        private static final float DEFAULT_PATH_WIDTH = 1;
        private static final int DEFAULT_PATH_COLOR = 0xffcdcdcd;
        private static final String DEFAULT_HINT_TEXT = "MORE";

        private final int footerColor;
        private final Context context;

        //length in dp
        private float arrowMarginLeft = DEFAULT_ARROW_MARGIN;
        private float arrowMarginRight = DEFAULT_ARROW_MARGIN;
        private float arrowHeadLength = DEFAULT_ARROW_HEAD_LENGTH;
        private float arrowHeadHeight = DEFAULT_ARROW_HEAD_HEIGHT;
        private float arrowTailLength = DEFAULT_ARROW_TAIL_LENGTH;
        private float arrowTailHeight = DEFAULT_ARROW_TAIL_HEIGHT;
        private float arrowSmallRectLength = DEFAULT_ARROW_SMALL_RECT_LENGTH;
        private float arrowSmallRectMargin = DEFAULT_ARROW_SMALL_RECT_MARGIN;
        private float finishDrawArrowPathDragDistance = DEFAULT_FINISH_DRAW_ARROW_PATH_DRAG_DISTANCE;
        private float rotateThreshold = DEFAULT_ROTATE_THRESHOLD;
        private float pathWidth = DEFAULT_PATH_WIDTH;

        private int rotateDuration = DEFAULT_ROTATE_DURATION;
        private String hintText = DEFAULT_HINT_TEXT;

        private int pathColor = DEFAULT_PATH_COLOR;

        public Builder(Context context, int footerColor) {
            this.context = context;
            this.footerColor = footerColor;
        }

        public Builder setPathColor(int pathColor) {
            this.pathColor = pathColor;
            return this;
        }

        public Builder setArrowMarginLeft(float arrowMarginLeft) {
            this.arrowMarginLeft = arrowMarginLeft;
            return this;
        }

        public Builder setArrowMarginRight(float arrowMarginRight) {
            this.arrowMarginRight = arrowMarginRight;
            return this;
        }

        public Builder setArrowHeadLength(float arrowHeadLength) {
            this.arrowHeadLength = arrowHeadLength;
            return this;
        }

        public Builder setArrowHeadHeight(float arrowHeadHeight) {
            this.arrowHeadHeight = arrowHeadHeight;
            return this;
        }

        public Builder setArrowTailLength(float arrowTailLength) {
            this.arrowTailLength = arrowTailLength;
            return this;
        }

        public Builder setArrowTailHeight(float arrowTailHeight) {
            this.arrowTailHeight = arrowTailHeight;
            return this;
        }

        public Builder setArrowSmallRectLength(float arrowSmallRectLength) {
            this.arrowSmallRectLength = arrowSmallRectLength;
            return this;
        }

        public Builder setArrowSmallRectMargin(float arrowSmallRectMargin) {
            this.arrowSmallRectMargin = arrowSmallRectMargin;
            return this;
        }

        public Builder setFinishDrawArrowPathDragDistance(float finishDrawArrowPathDragDistance) {
            this.finishDrawArrowPathDragDistance = finishDrawArrowPathDragDistance;
            return this;
        }

        public Builder setRotateThreshold(float rotateThreshold) {
            this.rotateThreshold = rotateThreshold;
            return this;
        }

        public Builder setPathWidth(float pathWidth) {
            this.pathWidth = pathWidth;
            return this;
        }

        public Builder setRotateDuration(int rotateDuration) {
            this.rotateDuration = rotateDuration;
            return this;
        }

        public Builder setHintText(String hintText) {
            this.hintText = hintText;
            return this;
        }

        public ArrowPathFooterDrawer build() {
            return new ArrowPathFooterDrawer(this);
        }
    }
}
