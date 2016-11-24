package com.fangxu.library.footer;

import android.animation.ValueAnimator;

import com.fangxu.library.DragContainer;

/**
 * Created by Administrator on 2016/11/24.
 */

public class IconRotateController {

    private float threshold;
    private boolean dragInRotateAnimatorExecuted;
    private boolean dragOutRotateAnimatorExecuted;

    private ValueAnimator iconRotateAnimator;

    private float rotateDegree;
    private int rotateDuration;

    public IconRotateController(float threshold, int duration) {
        this.threshold = threshold;
        this.rotateDuration = duration;
        reset();
    }

    public void reset() {
        dragInRotateAnimatorExecuted = false;
        dragOutRotateAnimatorExecuted = false;
    }

    public void calculateRotateDegree(@DragContainer.DragState int dragState, float dragDistance) {
        if (dragState == DragContainer.RELEASE) {
            if (excessRotateThreshold(dragDistance)) {
                startIconRotateAnimator(dragState);
            }
        } else if (dragState == DragContainer.DRAG_OUT) {
            if (excessRotateThreshold(dragDistance) && !dragOutRotateAnimatorExecuted) {
                startIconRotateAnimator(dragState);
            }
        } else {
            if (!excessRotateThreshold(dragDistance) && !dragInRotateAnimatorExecuted && dragOutRotateAnimatorExecuted) {
                startIconRotateAnimator(dragState);
            }
        }
    }

    public float getRotateDegree() {
        return rotateDegree;
    }

    private boolean excessRotateThreshold(float dragDistance) {
        return dragDistance > threshold;
    }

    private void startIconRotateAnimator(@DragContainer.DragState int dragState) {
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
        iconRotateAnimator.setDuration(rotateDuration);
        iconRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rotateDegree = (float) animation.getAnimatedValue() * 180;
            }
        });
        iconRotateAnimator.start();
    }
}
