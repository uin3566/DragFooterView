package com.fangxu.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
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

    private View contentView;
    private int orientation;
    private boolean shouldResetContentView;

    private static final int DEFAULT_FOOTER_HEIGHT = 90;
    private static final int DEFAULT_BEZIER_DRAG_THRESHOLD = 400;

    private int containerWidth, containerHeight;
    private float downX, downY;
    private int footerHeight = DEFAULT_FOOTER_HEIGHT;
    private float dragDx, dragDy;

    private Paint rectPaint;
    private Paint bezierPaint;
    private Paint textPaint;

    private Path bezierPath;
    private RectF dragRect;

    private Drawable iconDrawable;

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
        ta.recycle();

        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.YELLOW);
        rectPaint.setStyle(Paint.Style.FILL);

        bezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bezierPaint.setColor(Color.YELLOW);
        bezierPaint.setStyle(Paint.Style.FILL);

        bezierPath = new Path();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(30);

        dragRect = new RectF();
    }

    @FooterOrientation
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(@FooterOrientation int orientation) {
        this.orientation = orientation;
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragDx = 0;
                dragDy = 0;
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (orientation == LEFT || orientation == RIGHT) {
                    dragDx = event.getX() - downX;
                    setContentView((int) dragDx, 0, containerWidth + (int) dragDx, containerHeight);
                }
                if (orientation == TOP || orientation == BOTTOM) {
                    dragDy = event.getY() - downY;
                    setContentView(0, (int) dragDy, containerWidth, containerHeight + (int) dragDy);
                }
                break;
            case MotionEvent.ACTION_UP:
                resetContentView();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRect(canvas);
        drawBezier(canvas);
        drawIcon(canvas);
        drawTexts(canvas);
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
        Log.i(TAG, "dragDy=" + dragDy + ", footerHeight=" + footerHeight);
        if (containerHeight - contentView.getBottom() >= footerHeight) {
            float sx = 0;
            float sy = containerHeight - footerHeight;
            float cx = contentView.getWidth() * 0.5f;
            float cy;
            if (containerHeight - contentView.getBottom() >= DEFAULT_BEZIER_DRAG_THRESHOLD) {
                cy = containerHeight - DEFAULT_BEZIER_DRAG_THRESHOLD;
            } else {
                cy = contentView.getBottom();
            }
            float ex = containerWidth;
            float ey = containerHeight - footerHeight;
            bezierPath.reset();
            bezierPath.moveTo(sx, sy);
            bezierPath.quadTo(cx, cy, ex, ey);
            canvas.drawPath(bezierPath, bezierPaint);
        }
    }

    private int tmpIconTop;

    private void drawIcon(Canvas canvas) {
        if (iconDrawable == null) {
            return;
        }

        int drawableSize = 45;
        int left, top, right, bottom;
        left = containerWidth / 2 - drawableSize / 2;
        right = left + drawableSize;
        if (containerHeight - contentView.getBottom() <= DEFAULT_BEZIER_DRAG_THRESHOLD * 0.9f) {
            top = contentView.getBottom() + (containerHeight - contentView.getBottom()) / 2;
            bottom = top + drawableSize;
            tmpIconTop = top;
            iconDrawable.setBounds(left, top, right, bottom);
            iconDrawable.draw(canvas);
        } else {
            top = tmpIconTop;
            bottom = top + drawableSize;
            canvas.save();
            iconDrawable.setBounds(left, top, right, bottom);
            canvas.rotate(180, left + drawableSize / 2, top + drawableSize / 2);
            iconDrawable.draw(canvas);
            canvas.restore();
        }
    }

    private void drawTexts(Canvas canvas) {
        float x = contentView.getWidth() / 2;
        float y;
        y = tmpIconTop + 45;

        String text;
        if (containerHeight - contentView.getBottom() <= DEFAULT_BEZIER_DRAG_THRESHOLD * 0.9f) {
            text = "查看更多";
        } else {
            text = "释放查看";
        }
        canvas.drawText(text, x, y - textPaint.getFontMetrics().ascent, textPaint);
    }

    public void setIconDrawable(Drawable drawable) {
        iconDrawable = drawable;
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

    private void resetContentView() {
        if (!shouldResetContentView) {
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000);

        final int left = contentView.getLeft();
        final int right = contentView.getRight();
        final int top = contentView.getTop();
        final int bottom = contentView.getBottom();
        float totalDx = 0;
        float totalDy = 0;
        switch (orientation) {
            case LEFT:
                totalDx = containerWidth - contentView.getLeft();
                break;
            case RIGHT:
                totalDx = containerWidth - contentView.getRight();
                break;
            case TOP:
                totalDy = containerHeight - contentView.getTop();
                break;
            case BOTTOM:
                totalDy = containerHeight - contentView.getBottom();
                break;
        }

        final float totalDxFinal = totalDx;
        final float totalDyFinal = totalDy;
        final boolean isVertical = (orientation == TOP || orientation == BOTTOM);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
        animator.start();
    }

    private void checkChildren() {
        int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("DragContainer must hold only one child, check how many child you put in DragContainer");
        }
    }
}
