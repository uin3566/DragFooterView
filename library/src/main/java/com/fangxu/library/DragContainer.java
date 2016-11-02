package com.fangxu.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 2016/10/31.
 */
public class DragContainer extends ViewGroup {

    private static final String TAG = "DragContainer";

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT, RIGHT, TOP, BOTTOM})
    public @interface FooterOrientation {

    }

    private static final int DRAG_OUT = 10;
    private static final int DRAG_IN = 11;
    private static final int RELEASE = 12;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DRAG_OUT, DRAG_IN, RELEASE})
    public @interface DragState {

    }

    private int dragState;
    private float lastMoveX;
    private float lastMoveY;

    private View contentView;
    private int containerWidth, containerHeight;

    private static final int DEFAULT_FOOTER_HEIGHT = 30;
    private static final int DEFAULT_FOOTER_COLOR = 0xffcdcdcd;
    private static final int DEFAULT_BEZIER_DRAG_THRESHOLD = 150;
    private static final int DEFAULT_ICON_SIZE = 15;
    private static final int DEFAULT_TEXT_ICON_GAP = 4;
    private static final int DEFAULT_RESET_DURATION = 800;
    private static final int DEFAULT_TEXT_SIZE = 10;
    private static final int DEFAULT_TEXT_COLOR = 0xff222222;
    private static final float DEFAULT_DRAG_DAMP = 0.7f;

    //user define params
    private int orientation;
    private Drawable iconDrawable;
    private int iconSize;
    private int textIconGap;
    private String normalString;
    private String eventString;
    private int footerHeight;
    private int resetDuration;
    private float dragDamp;
    private int bezierDragThreshold;

    private boolean shouldResetContentView;
    private ValueAnimator resetAnimator;
    private ValueAnimator iconRotateAnimator;
    private int rotateThreshold;
    private float iconRotateDegree = 0;
    private boolean everRotatedIcon = false;
    private boolean dragOutRotateAnimatorExecuted = false;
    private boolean dragInRotateAnimatorExecuted = false;

    private float downX, downY;
    private float dragDx, dragDy;

    private Paint rectPaint;
    private Paint bezierPaint;
    private Paint textPaint;
    private Path bezierPath;
    private RectF dragRect;
    private int tmpIconPos;

    private float[] bezierParams;
    private int[] iconParams;
    //store text array,if orientation is TOP or BOTTOM,the length of textRows is 1,otherwise is the max length of normalString and eventString
    private String[] textRows;

    public DragContainer(Context context) {
        this(context, null);
    }

    public DragContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragContainer);
        orientation = ta.getInteger(R.styleable.DragContainer_dc_orientation, BOTTOM);
        iconDrawable = ta.getDrawable(R.styleable.DragContainer_dc_icon_drawable);
        iconSize = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_icon_size, dp2px(context, DEFAULT_ICON_SIZE));
        normalString = ta.getString(R.styleable.DragContainer_dc_text_normal);
        eventString = ta.getString(R.styleable.DragContainer_dc_text_event);
        int textColor = ta.getColor(R.styleable.DragContainer_dc_text_color, DEFAULT_TEXT_COLOR);
        textIconGap = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_text_icon_gap, dp2px(context, DEFAULT_TEXT_ICON_GAP));
        footerHeight = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_footer_height, dp2px(context, DEFAULT_FOOTER_HEIGHT));
        resetDuration = ta.getInteger(R.styleable.DragContainer_dc_reset_animator_duration, DEFAULT_RESET_DURATION);
        int footerColor = ta.getColor(R.styleable.DragContainer_dc_footer_color, DEFAULT_FOOTER_COLOR);
        int textSize = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_text_size, sp2px(context, DEFAULT_TEXT_SIZE));
        dragDamp = ta.getFloat(R.styleable.DragContainer_dc_drag_damp, DEFAULT_DRAG_DAMP);
        bezierDragThreshold = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_bezier_threshold, dp2px(context, DEFAULT_BEZIER_DRAG_THRESHOLD));
        ta.recycle();

        rotateThreshold = (int) (bezierDragThreshold * 0.9f);

        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(footerColor);
        rectPaint.setStyle(Paint.Style.FILL);

        dragRect = new RectF();

        bezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bezierPaint.setColor(footerColor);
        bezierPaint.setStyle(Paint.Style.FILL);

        bezierPath = new Path();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);

        setDragState(RELEASE);

        //store bezier params, params[0] = sx,params[1] = sy, params[2] = cx,params[3] = cy, params[4] = ex,params[5] = ey
        //sx:bezier start point x
        //sy:bezier start point y
        //cx:bezier control point x
        //cy:bezier control point y
        //ex:bezier end point x
        //ey:bezier end point y
        bezierParams = new float[6];

        //store icon position: left, top, right, bottom
        iconParams = new int[4];

        initTextRows();
    }

    private void initTextRows() {
        if (normalString == null || normalString.isEmpty()) {
            return;
        }

        if (orientation == TOP || orientation == BOTTOM) {
            textRows = new String[1];
        }

        if (orientation == LEFT || orientation == RIGHT) {
            int normalLength = normalString.length();
            int eventLength = normalLength;
            if (eventString != null && !eventString.isEmpty()) {
                eventLength = eventString.length();
            }
            int rowsLength = normalLength > eventLength ? normalLength : eventLength;
            textRows = new String[rowsLength];
        }
    }

    @FooterOrientation
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(@FooterOrientation int orientation) {
        this.orientation = orientation;
    }

    @DragState
    public int getDragState() {
        return dragState;
    }

    public void setDragState(@DragState int dragState) {
        this.dragState = dragState;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        contentView = getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        checkChildren();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.AT_MOST) {
            width = contentView.getMeasuredWidth();
        } else {
            width = widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            height = contentView.getMeasuredHeight();
        } else {
            height = heightSize;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        containerWidth = w;
        containerHeight = h;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        checkChildren();
        contentView.layout(0, 0, containerWidth, containerHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (resetAnimator != null && resetAnimator.isRunning()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMoveX = 0;
                lastMoveY = 0;
                dragDx = 0;
                dragDy = 0;
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastMoveX == 0) {
                    lastMoveX = event.getX();
                }
                if (lastMoveY == 0) {
                    lastMoveY = event.getY();
                }

                updateDragState(event);

                float realDragDistance;
                if (orientation == LEFT || orientation == RIGHT) {
                    dragDx = event.getX() - downX;
                    realDragDistance = dragDx * dragDamp;
                    setContentView((int) realDragDistance, 0, containerWidth + (int) realDragDistance, containerHeight);
                }
                if (orientation == TOP || orientation == BOTTOM) {
                    dragDy = event.getY() - downY;
                    realDragDistance = dragDy * dragDamp;
                    setContentView(0, (int) realDragDistance, containerWidth, containerHeight + (int) realDragDistance);
                }
                break;
            case MotionEvent.ACTION_UP:
                resetContentView();
                break;
        }
        return true;
    }

    private void updateDragState(MotionEvent event) {
        switch (orientation) {
            case LEFT:
                setDragState(event.getX() > lastMoveX ? DRAG_OUT : DRAG_IN);
                lastMoveX = event.getX();
                break;
            case RIGHT:
                setDragState(event.getX() > lastMoveX ? DRAG_IN : DRAG_OUT);
                lastMoveX = event.getX();
                break;
            case TOP:
                setDragState(event.getY() > lastMoveY ? DRAG_OUT : DRAG_IN);
                lastMoveY = event.getY();
                break;
            case BOTTOM:
                setDragState(event.getY() > lastMoveY ? DRAG_IN : DRAG_OUT);
                lastMoveY = event.getY();
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRect(canvas);
        drawBezier(canvas);
        drawIcon(canvas);
        drawText(canvas);
    }

    private void drawRect(Canvas canvas) {
        switch (orientation) {
            case LEFT:
                if (contentView.getLeft() >= footerHeight) {
                    dragRect.set(0, 0, footerHeight, containerHeight);
                } else {
                    dragRect.set(0, 0, contentView.getLeft(), containerHeight);
                }
                break;
            case TOP:
                if (contentView.getTop() >= footerHeight) {
                    dragRect.set(0, 0, containerWidth, footerHeight);
                } else {
                    dragRect.set(0, 0, containerWidth, contentView.getTop());
                }
                break;
            case RIGHT:
                if (containerWidth - contentView.getRight() >= footerHeight) {
                    dragRect.set(containerWidth - footerHeight, 0, containerWidth, containerHeight);
                } else {
                    dragRect.set(containerWidth - contentView.getRight(), 0, containerWidth, containerHeight);
                }
                break;
            case BOTTOM:
                if (containerHeight - contentView.getBottom() >= footerHeight) {
                    dragRect.set(0, containerHeight - footerHeight, containerWidth, containerHeight);
                } else {
                    dragRect.set(0, contentView.getBottom(), containerWidth, containerHeight);
                }
                break;
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
        float sx = 0, sy = 0, cx = 0, cy = 0, ex = 0, ey = 0;
        switch (orientation) {
            case LEFT:
                sx = footerHeight;
                sy = 0;
                cy = containerHeight / 2;
                if (contentView.getLeft() >= bezierDragThreshold) {
                    cx = bezierDragThreshold;
                } else {
                    cx = contentView.getLeft();
                }
                ex = footerHeight;
                ey = containerHeight;
                break;
            case RIGHT:
                sx = containerWidth - footerHeight;
                sy = 0;
                cy = containerHeight / 2;
                if (containerWidth - contentView.getRight() >= bezierDragThreshold) {
                    cx = containerWidth - bezierDragThreshold;
                } else {
                    cx = contentView.getRight();
                }
                ex = containerWidth - footerHeight;
                ey = containerHeight;
                break;
            case TOP:
                sx = 0;
                sy = footerHeight;
                cx = contentView.getWidth() / 2;
                if (contentView.getTop() >= bezierDragThreshold) {
                    cy = bezierDragThreshold;
                } else {
                    cy = contentView.getTop();
                }
                ex = containerWidth;
                ey = footerHeight;
                break;
            case BOTTOM:
                sx = 0;
                sy = containerHeight - footerHeight;
                cx = contentView.getWidth() / 2;
                if (containerHeight - contentView.getBottom() >= bezierDragThreshold) {
                    cy = containerHeight - bezierDragThreshold;
                } else {
                    cy = contentView.getBottom();
                }
                ex = containerWidth;
                ey = containerHeight - footerHeight;
                break;
        }

        bezierParams[0] = sx;
        bezierParams[1] = sy;
        bezierParams[2] = cx;
        bezierParams[3] = cy;
        bezierParams[4] = ex;
        bezierParams[5] = ey;
    }

    private boolean shouldDrawBezier() {
        switch (orientation) {
            case LEFT:
                return contentView.getLeft() >= footerHeight;
            case RIGHT:
                return containerWidth - contentView.getRight() >= footerHeight;
            case TOP:
                return contentView.getTop() >= footerHeight;
            case BOTTOM:
                return containerHeight - contentView.getBottom() >= footerHeight;
        }
        return true;
    }

    private void drawIcon(final Canvas canvas) {
        if (iconDrawable == null) {
            return;
        }

        setIconPosParams();

        if (getDragState() == RELEASE) {
            if (everRotatedIcon && !dragInRotateAnimatorExecuted && !excessRotateThreshold()) {
                startIconRotateAnimator();
            }
        } else if (getDragState() == DRAG_OUT) {
            if (excessRotateThreshold() && !dragOutRotateAnimatorExecuted) {
                startIconRotateAnimator();
            }
        } else {
            if (!excessRotateThreshold() && !dragInRotateAnimatorExecuted) {
                startIconRotateAnimator();
            }
        }

        canvas.save();
        canvas.rotate(iconRotateDegree, getIconRotateX(iconSize), getIconRotateY(iconSize));

        iconDrawable.setBounds(iconParams[0], iconParams[1], iconParams[2], iconParams[3]);
        iconDrawable.draw(canvas);

        canvas.restore();
    }

    private void startIconRotateAnimator() {
        int duration = 120;
        if (iconRotateAnimator != null && iconRotateAnimator.isRunning()) {
            iconRotateAnimator.cancel();
        }
        if (getDragState() == DRAG_OUT) {
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

    private float getIconRotateX(int drawableSize) {
        float x = 0;
        int left = iconParams[0];
        if (orientation == LEFT || orientation == RIGHT) {
            x = left + drawableSize / 2;
        }
        if (orientation == TOP || orientation == BOTTOM) {
            x = containerWidth / 2;
        }
        return x;
    }

    private float getIconRotateY(int drawableSize) {
        float y = 0;
        int top = iconParams[1];
        if (orientation == LEFT || orientation == RIGHT) {
            y = containerHeight / 2;
        }
        if (orientation == TOP || orientation == BOTTOM) {
            y = top + drawableSize / 2;
        }
        return y;
    }

    private boolean excessRotateThreshold() {
        switch (orientation) {
            case LEFT:
                return contentView.getLeft() > rotateThreshold;
            case TOP:
                return contentView.getTop() > rotateThreshold;
            case RIGHT:
                return containerWidth - contentView.getRight() > rotateThreshold;
            case BOTTOM:
                return containerHeight - contentView.getBottom() > rotateThreshold;
        }
        return true;
    }

    private void setIconPosParams() {
        int left = 0, top = 0, right = 0, bottom = 0;
        switch (orientation) {
            case LEFT:
                top = containerHeight / 2 - iconSize / 2;
                bottom = top + iconSize;
                if (contentView.getLeft() <= rotateThreshold) {
                    right = contentView.getLeft() / 2;
                    left = right - iconSize;
                    tmpIconPos = right;
                } else {
                    right = tmpIconPos;
                    left = right - iconSize;
                }
                break;
            case RIGHT:
                top = containerHeight / 2 - iconSize / 2;
                bottom = top + iconSize;
                if (containerWidth - contentView.getRight() <= rotateThreshold) {
                    left = contentView.getRight() + (containerWidth - contentView.getRight()) / 2;
                    right = left + iconSize;
                    tmpIconPos = left;
                } else {
                    left = tmpIconPos;
                    right = left + iconSize;
                }
                break;
            case TOP:
                left = containerWidth / 2 - iconSize / 2;
                right = left + iconSize;
                if (contentView.getTop() <= rotateThreshold) {
                    bottom = contentView.getTop() / 2;
                    top = bottom - iconSize;
                    tmpIconPos = bottom;
                } else {
                    bottom = tmpIconPos;
                    top = bottom - iconSize;
                }
                break;
            case BOTTOM:
                left = containerWidth / 2 - iconSize / 2;
                right = left + iconSize;
                if (containerHeight - contentView.getBottom() <= rotateThreshold) {
                    top = contentView.getBottom() + (containerHeight - contentView.getBottom()) / 2;
                    bottom = top + iconSize;
                    tmpIconPos = top;
                } else {
                    top = tmpIconPos;
                    bottom = top + iconSize;
                }
                break;
        }

        iconParams[0] = left;
        iconParams[1] = top;
        iconParams[2] = right;
        iconParams[3] = bottom;
    }

    private void drawText(Canvas canvas) {
        if (normalString == null || normalString.isEmpty()) {
            return;
        }

        if (iconDrawable == null) {
            setIconPosParams();
        }

        int iconLeft = iconParams[0];
        int iconTop = iconParams[1];
        int iconRight = iconParams[2];
        int iconBottom = iconParams[3];

        float x = 0;
        float y = 0;

        switch (orientation) {
            case LEFT:
                x = iconLeft - textPaint.getTextSize() / 2 - textIconGap;
                y = iconTop + iconSize / 2;
                break;
            case RIGHT:
                x = iconRight + textPaint.getTextSize() / 2 + textIconGap;
                y = iconTop + iconSize / 2;
                break;
            case TOP:
                x = iconLeft + iconSize / 2;
                y = iconTop - textPaint.getTextSize() / 2 - textIconGap;
                break;
            case BOTTOM:
                x = iconLeft + iconSize / 2;
                y = iconBottom + textPaint.getTextSize() / 2 + textIconGap;
                break;
        }

        setTextRows();
        drawTextInRows(textRows, canvas, x, y);
    }

    private void setTextRows() {
        if (eventString == null || eventString.isEmpty()) {
            eventString = normalString;
        }

        String tmp;
        tmp = excessRotateThreshold() ? eventString : normalString;
        if (orientation == TOP || orientation == BOTTOM) {
            textRows[0] = tmp;
        }
        if (orientation == LEFT || orientation == RIGHT) {
            for (int i = 0; i < tmp.length(); i++) {
                textRows[i] = String.valueOf(tmp.charAt(i));
            }
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

    private void setContentView(int left, int top, int right, int bottom) {
        shouldResetContentView = false;
        switch (orientation) {
            case LEFT:
                if (left < 0) {
                    return;
                }
                break;
            case TOP:
                if (top < 0) {
                    return;
                }
                break;
            case RIGHT:
                if (right > containerWidth) {
                    return;
                }
                break;
            case BOTTOM:
                if (bottom > containerHeight) {
                    return;
                }
                break;
        }

        shouldResetContentView = true;
        contentView.setLeft(left);
        contentView.setTop(top);
        contentView.setRight(right);
        contentView.setBottom(bottom);
        invalidate();
    }

    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void resetContentView() {
        if (!shouldResetContentView) {
            return;
        }

        dragOutRotateAnimatorExecuted = false;
        dragInRotateAnimatorExecuted = false;
        everRotatedIcon = excessRotateThreshold();
        setDragState(RELEASE);

        resetAnimator = ValueAnimator.ofFloat(0, 1);
        resetAnimator.setDuration(resetDuration);

        final int left = contentView.getLeft();
        final int right = contentView.getRight();
        final int top = contentView.getTop();
        final int bottom = contentView.getBottom();
        float totalDx = 0;
        float totalDy = 0;
        switch (orientation) {
            case LEFT:
                totalDx = -left;
                break;
            case RIGHT:
                totalDx = containerWidth - right;
                break;
            case TOP:
                totalDy = -top;
                break;
            case BOTTOM:
                totalDy = containerHeight - bottom;
                break;
        }

        final float totalDxFinal = totalDx;
        final float totalDyFinal = totalDy;
        final boolean isVertical = (orientation == TOP || orientation == BOTTOM);
        resetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                float currentDx, currentDy;
                if (isVertical) {
                    currentDy = totalDyFinal * progress;
                    setContentView(left, top + (int) currentDy, right, bottom + (int) currentDy);
                } else {
                    currentDx = totalDxFinal * progress;
                    setContentView(left + (int) currentDx, top, right + (int) currentDx, bottom);
                }
            }
        });
        resetAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dragOutRotateAnimatorExecuted = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        resetAnimator.start();
    }

    private void checkChildren() {
        int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("DragContainer must hold only one child, check how many child you put in DragContainer");
        }
    }
}
