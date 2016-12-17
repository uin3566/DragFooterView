package com.fangxu.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.fangxu.library.footer.BezierFooterDrawer;
import com.fangxu.library.footer.BaseFooterDrawer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 2016/10/31.
 */
public class DragContainer extends FrameLayout {

    private static final String TAG = "DragContainer";

    public static final int DRAG_OUT = 10;
    public static final int DRAG_IN = 11;
    public static final int RELEASE = 12;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DRAG_OUT, DRAG_IN, RELEASE})
    public @interface DragState {

    }

    private View contentView;
    private DragListener dragListener;
    private IDragChecker dragChecker;
    private int containerWidth, containerHeight;

    private static final int DEFAULT_BACKGROUND_COLOR = 0xffffffff;

    private static final int DEFAULT_RESET_DURATION = 700;
    private static final int DEFAULT_FOOTER_COLOR = 0xffcdcdcd;
    private static final float DEFAULT_DRAG_DAMP = 0.5f;

    //user define params in xml
    private int footerColor;
    private int resetDuration;
    private float dragDamp;

    private boolean shouldResetContentView;
    private ValueAnimator resetAnimator;

    private float downX, downY;
    private float dragDx;
    private float lastMoveX;

    private BaseFooterDrawer footerDrawer;

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

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }

    public void setIDragChecker(IDragChecker dragChecker) {
        this.dragChecker = dragChecker;
    }

    private void init(Context context, AttributeSet attrs) {
        setIDragChecker(new DefaultDragChecker());

        setBackgroundColor();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragContainer);
        resetDuration = ta.getInteger(R.styleable.DragContainer_dc_reset_animator_duration, DEFAULT_RESET_DURATION);
        footerColor = ta.getColor(R.styleable.DragContainer_dc_footer_color, DEFAULT_FOOTER_COLOR);
        dragDamp = ta.getFloat(R.styleable.DragContainer_dc_drag_damp, DEFAULT_DRAG_DAMP);
        ta.recycle();

        setFooterDrawer(defaultFooterDrawer());

        setDragState(RELEASE);
    }

    private BaseFooterDrawer defaultFooterDrawer() {
        return new BezierFooterDrawer.Builder(getContext(), footerColor).setIconDrawable(getContext().getResources().getDrawable(R.drawable.left)).build();
    }

    public void setFooterDrawer(BaseFooterDrawer footerDrawer) {
        this.footerDrawer = footerDrawer;
    }

    private void setBackgroundColor() {
        Drawable drawable = getBackground();
        if (drawable == null) {
            setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        }
    }

    private void setDragState(@DragState int dragState) {
        if (footerDrawer != null) {
            footerDrawer.updateDragState(dragState);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        checkChildren();
        contentView = getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = contentView.getMeasuredWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = contentView.getMeasuredHeight();
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
        contentView.layout(0, 0, containerWidth, containerHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (footerDrawer != null) {
            footerDrawer.drawFooter(canvas, contentView.getRight(), 0, containerWidth, containerHeight);
        }
    }

    private void setContentViewPosition(int left, int top, int right, int bottom) {
        shouldResetContentView = false;
        if (right > containerWidth) {
            return;
        }

        shouldResetContentView = true;
        contentView.layout(left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (resetAnimator != null && resetAnimator.isRunning()) {
            return super.dispatchTouchEvent(event);
        }

        //dispatch event to child
        super.dispatchTouchEvent(event);

        if (!dragChecker.canDrag(contentView)) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragDx = 0;
                downX = event.getX();
                downY = event.getY();
                lastMoveX = downX;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(event.getX() - downX);
                float dy = Math.abs(event.getY() - downY);
                if (dx >= dy) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }

                if (dragDx <= 0 && dragChecker.canDrag(contentView)) {
                    updateDragState(event);
                    if (dragDx != 0) {
                        //when is drag state, consume event self and send cancel event to child view
                        sendCancelEvent(event);
                    }
                    dragDx = event.getX() - downX;
                    float realDragDistance = dragDx * dragDamp;
                    setContentViewPosition((int) realDragDistance, 0, containerWidth + (int) realDragDistance, containerHeight);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                resetContentView();
                break;
        }
        return true;
    }

    private void sendCancelEvent(MotionEvent event) {
        MotionEvent cancel = MotionEvent.obtain(event.getDownTime(), event.getEventTime() + ViewConfiguration.getLongPressTimeout()
                , MotionEvent.ACTION_CANCEL, event.getX(), event.getY(), event.getMetaState());
        super.dispatchTouchEvent(cancel);
    }

    private void updateDragState(MotionEvent event) {
        if (event.getX() < lastMoveX) {
            setDragState(DRAG_OUT);
        }
        if (event.getX() > lastMoveX && contentView.getRight() < containerWidth) {
            setDragState(DRAG_IN);
        }
        lastMoveX = event.getX();
    }

    private void resetContentView() {
        setDragState(RELEASE);

        if (!shouldResetContentView) {
            return;
        }

        resetAnimator = ValueAnimator.ofFloat(0, 1);
        resetAnimator.setDuration(resetDuration);

        final int left = contentView.getLeft();
        final int right = contentView.getRight();
        final int top = contentView.getTop();
        final int bottom = contentView.getBottom();
        final float totalDx = containerWidth - right;

        resetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                float currentDx;
                currentDx = totalDx * progress;
                setContentViewPosition(left + (int) currentDx, top, right + (int) currentDx, bottom);
            }
        });
        resetAnimator.start();

        if (dragListener != null && footerDrawer != null && footerDrawer.shouldTriggerEvent(totalDx)) {
            dragListener.onDragEvent();
        }
    }

    private void checkChildren() {
        int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("DragContainer must hold only one child, check how many child you put in DragContainer");
        }
    }
}
