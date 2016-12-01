package com.fangxu.library.footer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.Log;

import com.fangxu.library.DragContainer;

import java.util.Locale;

/**
 * Created by Administrator on 2016/12/1.
 */

public class ArrowPathFooterDrawer extends BaseFooterDrawer {
    private static final String TAG = "ArrowPathFooterDrawer";

    private PathMeasure pathMeasure;
    private Path arrowPath;
    private Path drawingArrowPath = new Path();
    private Paint paint;
    private int pathColor = 0xffcdcdcd;
    private float pathWidth = 3;
    private boolean hasInitArrowPath = false;
    private IconRotateController rotateController;

    private ArrowPathParams pathParams;

    private static class ArrowPathParams {
        float finishDrawArrowPathDragDistance = 300;
        float arrowTotalMoveX = 100;
        float arrowHeadLength = 40;
        float arrowHeadHeight = 40;
        float arrowTailHeight = 15;
        float arrowTailLength = 30;
        float arrowSmallRectLength = 7.5f;
        float arrowSmallRectMargin = 7.5f;
        float arrowTailPosX;
        float arrowTailPosY;

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH,
                    "finishDrawArrowPathDragDistance=%f,arrowHeadLength=%f, arrowHeadHeight=%f" +
                            ", arrowTailHeight=%f, arrowTailLength=%f, arrowTailPosX=%f, arrowTailPosY=%f"
                    , finishDrawArrowPathDragDistance, arrowHeadLength, arrowHeadHeight
                    , arrowTailHeight, arrowTailLength, arrowTailPosX, arrowTailPosY);
        }
    }

    public ArrowPathFooterDrawer() {
        footerRegion = new RectF();

        rotateController = new IconRotateController(300, 500);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(pathColor);
        paint.setStrokeWidth(pathWidth);
    }

    private void initArrowPath() {
        if (hasInitArrowPath) {
            return;
        }

        pathMeasure = new PathMeasure();
        pathParams = new ArrowPathParams();
        arrowPath = new Path();

        pathParams.arrowTailPosX = footerRegion.right - pathParams.arrowSmallRectLength * 2 - pathParams.arrowSmallRectMargin * 2;
        pathParams.arrowTailPosY = (footerRegion.bottom - footerRegion.top) / 2 - pathParams.arrowTailHeight / 2;

        setArrowPath(pathParams.arrowTailPosX, pathParams.arrowTailPosY);

        hasInitArrowPath = true;
    }

    private void setArrowPath(float tailPosX, float tailPosY) {
        arrowPath.reset();

        float arrowHeadDy = (pathParams.arrowHeadHeight - pathParams.arrowTailHeight) / 2;
        float arrowTailLength = pathParams.arrowTailLength;
        float arrowTailHeight = pathParams.arrowTailHeight;
        float rectLength = pathParams.arrowSmallRectLength;
        float rectMargin = pathParams.arrowSmallRectMargin;

        arrowPath.moveTo(tailPosX, tailPosY);
        arrowPath.lineTo(tailPosX - arrowTailLength, tailPosY);
        arrowPath.lineTo(tailPosX - arrowTailLength, tailPosY - arrowHeadDy);
        arrowPath.lineTo(tailPosX - arrowTailLength - pathParams.arrowHeadLength, tailPosY + arrowTailHeight / 2);
        arrowPath.lineTo(tailPosX - arrowTailLength, tailPosY + arrowTailHeight + arrowHeadDy);
        arrowPath.lineTo(tailPosX - arrowTailLength, tailPosY + arrowTailHeight);
        arrowPath.lineTo(tailPosX, tailPosY + arrowTailHeight);
        arrowPath.lineTo(tailPosX, tailPosY);

        arrowPath.addRect(tailPosX + rectMargin, tailPosY, tailPosX + rectMargin + rectLength, tailPosY + arrowTailHeight, Path.Direction.CCW);
        arrowPath.addRect(tailPosX + rectMargin * 2 + rectLength, tailPosY, tailPosX + 2 * (rectMargin + rectLength), tailPosY + arrowTailHeight, Path.Direction.CW);

        pathMeasure.setPath(arrowPath, false);
    }

    @Override
    public void drawFooter(Canvas canvas, float left, float top, float right, float bottom) {
        super.drawFooter(canvas, left, top, right, bottom);
        drawArrow(canvas);
    }

    private void drawArrow(Canvas canvas) {
        initArrowPath();
        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        drawingArrowPath.reset();
        float dragDx = footerRegion.right - footerRegion.left;
        float percent;
        if (dragDx < pathParams.finishDrawArrowPathDragDistance) {
            percent = dragDx / pathParams.finishDrawArrowPathDragDistance;
        } else {
            percent = 1.0f;
        }

        //move arrow
        float moveX;
        moveX = dragDx * 0.4f;
        setArrowPath(pathParams.arrowTailPosX - moveX, pathParams.arrowTailPosY);

        pathMeasure.getSegment(0, percent * pathMeasure.getLength(), drawingArrowPath, true);
        while (pathMeasure.nextContour()) {
            pathMeasure.getSegment(0, percent * pathMeasure.getLength(), drawingArrowPath, true);
        }

        rotateController.calculateRotateDegree(dragState, footerRegion.right - footerRegion.left);

        //rotate arrow
        canvas.save();
        float degree = rotateController.getRotateDegree();
        float centerX = pathParams.arrowTailPosX - moveX - (pathParams.arrowTailLength + pathParams.arrowHeadLength) / 2;
        float centerY = pathParams.arrowTailPosY + pathParams.arrowTailHeight / 2;
        canvas.rotate(degree, centerX, centerY);
        canvas.drawPath(drawingArrowPath, paint);
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
        return false;
    }
}
