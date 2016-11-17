package com.fangxu.library;

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

    public static final int DRAG_OUT = 10;
    public static final int DRAG_IN = 11;
    public static final int RELEASE = 12;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DRAG_OUT, DRAG_IN, RELEASE})
    public  @interface DragState {

    }

    private int dragState;

    private View contentView;
    private DragListener dragListener;
    private IDragChecker dragChecker;
    private int containerWidth, containerHeight;

    private static final int DEFAULT_BACKGROUND_COLOR = 0xffffffff;

    private static final int DEFAULT_FOOTER_HEIGHT = 30;
    private static final int DEFAULT_FOOTER_COLOR = 0xffcdcdcd;
    private static final int DEFAULT_BEZIER_DRAG_THRESHOLD = 120;
    private static final int DEFAULT_ICON_SIZE = 15;
    private static final int DEFAULT_TEXT_ICON_GAP = 4;
    private static final int DEFAULT_RESET_DURATION = 700;
    private static final int DEFAULT_TEXT_SIZE = 10;
    private static final int DEFAULT_TEXT_COLOR = 0xff222222;
    private static final float DEFAULT_DRAG_DAMP = 0.5f;

    //user define params in xml
    private Drawable iconDrawable;
    private int iconSize;
    private int textIconGap;
    private int textColor;
    private int textSize;
    private String normalString;
    private String eventString;
    private int footerHeight;
    private int footerColor;
    private int resetDuration;
    private float dragDamp;
    private int bezierDragThreshold;

    private boolean shouldResetContentView;
    private ValueAnimator resetAnimator;
//    private ValueAnimator iconRotateAnimator;
//    private int rotateThreshold;
//    private float iconRotateDegree = 0;
//    private boolean everRotatedIcon = false;
//    private boolean dragOutRotateAnimatorExecuted = false;
//    private boolean dragInRotateAnimatorExecuted = false;

    private float downX, downY;
    private float dragDx;
    private float lastMoveX;

//    private Paint rectPaint;
//    private Paint bezierPaint;
//    private Paint textPaint;
//    private Path bezierPath;
//    private RectF dragRect;
//    private int tmpIconPos;
//
//    private float[] bezierParams;
//    private int[] iconParams;
//    //store text array,if orientation is TOP or BOTTOM,the length of textRows is 1,otherwise is the max length of normalString and eventString
//    private String[] textRows;


    private IFooterDrawer footerDrawer;

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
        iconDrawable = ta.getDrawable(R.styleable.DragContainer_dc_icon_drawable);
        iconSize = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_icon_size, dp2px(context, DEFAULT_ICON_SIZE));
        normalString = ta.getString(R.styleable.DragContainer_dc_text_normal);
        eventString = ta.getString(R.styleable.DragContainer_dc_text_event);
        textColor = ta.getColor(R.styleable.DragContainer_dc_text_color, DEFAULT_TEXT_COLOR);
        textIconGap = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_text_icon_gap, dp2px(context, DEFAULT_TEXT_ICON_GAP));
        footerHeight = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_footer_height, dp2px(context, DEFAULT_FOOTER_HEIGHT));
        resetDuration = ta.getInteger(R.styleable.DragContainer_dc_reset_animator_duration, DEFAULT_RESET_DURATION);
        footerColor = ta.getColor(R.styleable.DragContainer_dc_footer_color, DEFAULT_FOOTER_COLOR);
        textSize = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_text_size, sp2px(context, DEFAULT_TEXT_SIZE));
        dragDamp = ta.getFloat(R.styleable.DragContainer_dc_drag_damp, DEFAULT_DRAG_DAMP);
        bezierDragThreshold = ta.getDimensionPixelSize(R.styleable.DragContainer_dc_bezier_threshold, dp2px(context, DEFAULT_BEZIER_DRAG_THRESHOLD));
        ta.recycle();

        setFooterDrawer(defaultFooterDrawer());

//        rotateThreshold = (int) (bezierDragThreshold * 0.9f);

//        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        rectPaint.setColor(footerColor);
//        rectPaint.setStyle(Paint.Style.FILL);

//        dragRect = new RectF();
//
//        bezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        bezierPaint.setColor(footerColor);
//        bezierPaint.setStyle(Paint.Style.FILL);
//
//        bezierPath = new Path();
//
//        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        textPaint.setColor(textColor);
//        textPaint.setTextAlign(Paint.Align.CENTER);
//        textPaint.setTextSize(textSize);

        setDragState(RELEASE);

//        //store bezier params, params[0] = sx,params[1] = sy, params[2] = cx,params[3] = cy, params[4] = ex,params[5] = ey
//        //sx:bezier start point x
//        //sy:bezier start point y
//        //cx:bezier control point x
//        //cy:bezier control point y
//        //ex:bezier end point x
//        //ey:bezier end point y
//        bezierParams = new float[6];
//
//        //store icon position: left, top, right, bottom
//        iconParams = new int[4];

//        initTextRows();
    }

    private IFooterDrawer defaultFooterDrawer() {
        BezierFooterDrawer.DrawParams drawParams = new BezierFooterDrawer.DrawParams();
        drawParams.iconSize = iconSize;
        drawParams.iconDrawable = iconDrawable;
        drawParams.eventString = eventString;
        drawParams.normalString = normalString;
        drawParams.footerColor = footerColor;
        drawParams.textColor = textColor;
        drawParams.textIconGap = textIconGap;
        drawParams.textSize = textSize;
        IFooterDrawer footerDrawer = new BezierFooterDrawer(drawParams);
        ((BezierFooterDrawer)footerDrawer).setParams(footerHeight, bezierDragThreshold);
        return footerDrawer;
    }

    public void setFooterDrawer(IFooterDrawer footerDrawer) {
        this.footerDrawer = footerDrawer;
    }

    private void setBackgroundColor() {
        Drawable drawable = getBackground();
        if (drawable == null) {
            setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        }
    }

//    private void initTextRows() {
//        if (normalString == null || normalString.isEmpty()) {
//            return;
//        }
//
//        int normalLength = normalString.length();
//        int eventLength = normalLength;
//        if (eventString != null && !eventString.isEmpty()) {
//            eventLength = eventString.length();
//        }
//        int rowsLength = normalLength > eventLength ? normalLength : eventLength;
//        textRows = new String[rowsLength];
//    }

    @DragState
    private int getDragState() {
        return dragState;
    }

    private void setDragState(@DragState int dragState) {
        this.dragState = dragState;
        footerDrawer.updateDragState(dragState);
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
//        drawRect(canvas);
//        drawBezier(canvas);
//        drawIcon(canvas);
//        drawText(canvas);
    }

//    private void drawRect(Canvas canvas) {
//        if (containerWidth - contentView.getRight() >= footerHeight) {
//            dragRect.set(containerWidth - footerHeight, 0, containerWidth, containerHeight);
//        } else {
//            dragRect.set(contentView.getRight(), 0, containerWidth, containerHeight);
//        }
//        canvas.drawRect(dragRect, rectPaint);
//    }
//
//    private void drawBezier(Canvas canvas) {
//        if (shouldDrawBezier()) {
//            setBezierParams();
//            bezierPath.reset();
//            bezierPath.moveTo(bezierParams[0], bezierParams[1]);
//            bezierPath.quadTo(bezierParams[2], bezierParams[3], bezierParams[4], bezierParams[5]);
//            canvas.drawPath(bezierPath, bezierPaint);
//        }
//    }
//
//    private void setBezierParams() {
//        float sx = containerWidth - footerHeight;
//        float sy = 0;
//        float cy = containerHeight / 2;
//        float cx;
//        if (containerWidth - contentView.getRight() >= bezierDragThreshold) {
//            cx = containerWidth - bezierDragThreshold;
//        } else {
//            cx = contentView.getRight();
//        }
//        float ex = containerWidth - footerHeight;
//        float ey = containerHeight;
//
//        bezierParams[0] = sx;
//        bezierParams[1] = sy;
//        bezierParams[2] = cx;
//        bezierParams[3] = cy;
//        bezierParams[4] = ex;
//        bezierParams[5] = ey;
//    }
//
//    private boolean shouldDrawBezier() {
//        return containerWidth - contentView.getRight() >= footerHeight;
//    }
//
//    private void drawIcon(final Canvas canvas) {
//        if (iconDrawable == null) {
//            return;
//        }
//
//        setIconPosParams();
//
//        if (getDragState() == RELEASE) {
//            if (everRotatedIcon && !dragInRotateAnimatorExecuted && !excessRotateThreshold()) {
//                startIconRotateAnimator();
//            }
//        } else if (getDragState() == DRAG_OUT) {
//            if (excessRotateThreshold() && !dragOutRotateAnimatorExecuted) {
//                startIconRotateAnimator();
//            }
//        } else {
//            if (!excessRotateThreshold() && !dragInRotateAnimatorExecuted) {
//                startIconRotateAnimator();
//            }
//        }
//
//        canvas.save();
//        canvas.rotate(iconRotateDegree, getIconRotateX(iconSize), getIconRotateY());
//
//        iconDrawable.setBounds(iconParams[0], iconParams[1], iconParams[2], iconParams[3]);
//        iconDrawable.draw(canvas);
//
//        canvas.restore();
//    }
//
//    private void startIconRotateAnimator() {
//        int duration = 100;
//        if (iconRotateAnimator != null && iconRotateAnimator.isRunning()) {
//            iconRotateAnimator.cancel();
//        }
//        if (getDragState() == DRAG_OUT) {
//            dragOutRotateAnimatorExecuted = true;
//            dragInRotateAnimatorExecuted = false;
//            iconRotateAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
//        } else {
//            dragInRotateAnimatorExecuted = true;
//            dragOutRotateAnimatorExecuted = false;
//            iconRotateAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
//        }
//        iconRotateAnimator.setDuration(duration);
//        iconRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                iconRotateDegree = (float) animation.getAnimatedValue() * 180;
//            }
//        });
//        iconRotateAnimator.start();
//    }
//
//    private float getIconRotateX(int drawableSize) {
//        int left = iconParams[0];
//        return left + drawableSize / 2;
//    }
//
//    private float getIconRotateY() {
//        return containerHeight / 2;
//    }
//
//    private boolean excessRotateThreshold() {
//        return containerWidth - contentView.getRight() > rotateThreshold;
//    }
//
//    private void setIconPosParams() {
//        int top = containerHeight / 2 - iconSize / 2;
//        int bottom = top + iconSize;
//        int left, right;
//        if (containerWidth - contentView.getRight() <= rotateThreshold) {
//            left = contentView.getRight() + (containerWidth - contentView.getRight()) / 2;
//            right = left + iconSize;
//            tmpIconPos = left;
//        } else {
//            left = tmpIconPos;
//            right = left + iconSize;
//        }
//
//        iconParams[0] = left;
//        iconParams[1] = top;
//        iconParams[2] = right;
//        iconParams[3] = bottom;
//    }
//
//    private void drawText(Canvas canvas) {
//        if (normalString == null || normalString.isEmpty()) {
//            return;
//        }
//
//        if (iconDrawable == null) {
//            setIconPosParams();
//        }
//
//        int iconTop = iconParams[1];
//        int iconRight = iconParams[2];
//
//        float x = iconRight + textPaint.getTextSize() / 2 + textIconGap;
//        float y = iconTop + iconSize / 2;
//
//        setTextRows();
//        drawTextInRows(textRows, canvas, x, y);
//    }
//
//    private void setTextRows() {
//        if (eventString == null || eventString.isEmpty()) {
//            eventString = normalString;
//        }
//
//        String tmp;
//        tmp = excessRotateThreshold() ? eventString : normalString;
//        for (int i = 0; i < tmp.length(); i++) {
//            textRows[i] = String.valueOf(tmp.charAt(i));
//        }
//    }
//
//    /**
//     * draw texts in rows
//     */
//    private void drawTextInRows(String[] strings, Canvas canvas, float x, float y) {
//        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//        float top = fontMetrics.top;
//        float bottom = fontMetrics.bottom;
//        int length = strings.length;
//        float total = (length - 1) * (-top + bottom) + (-fontMetrics.ascent + fontMetrics.descent);
//        float offset = total / 2 - bottom;
//        for (int i = 0; i < length; i++) {
//            float yAxis = -(length - i - 1) * (-top + bottom) + offset;
//            canvas.drawText(strings[i], x, y + yAxis, textPaint);
//        }
//    }

    private void setContentView(int left, int top, int right, int bottom) {
        shouldResetContentView = false;
        if (right > containerWidth) {
            return;
        }

        shouldResetContentView = true;
        contentView.setLeft(left);
        contentView.setTop(top);
        contentView.setRight(right);
        contentView.setBottom(bottom);
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (resetAnimator != null && resetAnimator.isRunning()) {
            return super.dispatchTouchEvent(event);
        }

        //dispatch event to child
        super.dispatchTouchEvent(event);

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
                    dragDx = event.getX() - downX;
                    float realDragDistance = dragDx * dragDamp;
                    setContentView((int) realDragDistance, 0, containerWidth + (int) realDragDistance, containerHeight);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                resetContentView();
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (getDragState() != RELEASE) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
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

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void resetContentView() {
//        dragOutRotateAnimatorExecuted = false;
//        dragInRotateAnimatorExecuted = false;
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
                setContentView(left + (int) currentDx, top, right + (int) currentDx, bottom);
            }
        });
        resetAnimator.start();

        if (dragListener != null && totalDx > bezierDragThreshold * 0.9f) {
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
