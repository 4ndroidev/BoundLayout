package com.androidev.boundlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BoundLayout extends ViewGroup {

    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final int MAX_CHILDREN_COUNT = 3;
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_POSITIVE = 1;
    private static final int DIRECTION_NEGATIVE = -1;
    private static final long ANIMATION_DURATION = 250;
    private static final float DRAGGING_RESISTANCE = 2.1f;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private View mHeader;
    private View mFooter;
    private View mContent;
    private int mDirection;
    private int mTouchSlop;
    private int mTotalOffset;
    private int mHeaderOffset;
    private int mFooterOffset;
    private int mContentOffset;
    private int mOrientation;
    private int mActivePointerId;
    private float mLastMotionValue;
    private boolean isBeingDragged;
    private Animation mBoundAnimation;

    public BoundLayout(Context context) {
        this(context, null);
    }

    public BoundLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BoundLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void bound(float interpolatedTime) {
        int targetValue = (mTotalOffset + (int) (-mTotalOffset * interpolatedTime));
        int lastValue = mOrientation == HORIZONTAL ? mContent.getLeft() : mContent.getTop();
        offset(targetValue - lastValue);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mDirection = DIRECTION_NONE;
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mBoundAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                bound(interpolatedTime);
            }
        };
        mBoundAnimation.setInterpolator(new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR));
        mBoundAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDirection = DIRECTION_NONE;
                mContentOffset = 0;
                mHeaderOffset = 0;
                mFooterOffset = 0;
                requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BoundLayout, defStyleAttr, defStyleRes);
        mOrientation = array.getInt(R.styleable.BoundLayout_orientation, HORIZONTAL);
        array.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount > MAX_CHILDREN_COUNT) {
            throw new IllegalStateException("HorizontalBoundView can host at most three children");
        }
        if (childCount == 1) {
            mContent = getChildAt(0);
        } else if (childCount == 2) {
            mHeader = getChildAt(0);
            mContent = getChildAt(1);
        } else {
            mHeader = getChildAt(0);
            mContent = getChildAt(1);
            mFooter = getChildAt(2);
        }
        if (mContent != null)
            mContent.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mOrientation == HORIZONTAL) {
            layoutHorizontal();
        } else {
            layoutVertical();
        }
    }

    private void layoutHorizontal() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (mHeader != null) {
            int displayMode = ((LayoutParams) mHeader.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_FIXED)
                mHeaderOffset = mHeader.getMeasuredWidth();
            mHeader.layout(
                    mHeaderOffset + paddingLeft - mHeader.getMeasuredWidth(),
                    paddingTop,
                    mHeaderOffset + paddingLeft,
                    paddingTop + mHeader.getMeasuredHeight()
            );
        }
        if (mFooter != null) {
            int displayMode = ((LayoutParams) mFooter.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_FIXED)
                mFooterOffset = mFooter.getMeasuredWidth();
            mFooter.layout(
                    mFooterOffset + paddingLeft + getMeasuredWidth(),
                    paddingTop,
                    mFooterOffset + paddingLeft + getMeasuredWidth() + mFooter.getMeasuredWidth(),
                    paddingTop + mFooter.getMeasuredHeight()
            );
        }
        if (mContent != null) {
            mContent.layout(
                    mContentOffset + paddingLeft,
                    paddingTop,
                    mContentOffset + paddingLeft + mContent.getMeasuredWidth(),
                    paddingTop + mContent.getMeasuredHeight());
        }
    }

    private void layoutVertical() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (mHeader != null) {
            int displayMode = ((LayoutParams) mHeader.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_FIXED)
                mHeaderOffset = mHeader.getMeasuredHeight();
            mHeader.layout(
                    paddingLeft,
                    mHeaderOffset + paddingTop - mHeader.getMeasuredHeight(),
                    paddingLeft + mHeader.getMeasuredWidth(),
                    mHeaderOffset + paddingTop
            );
        }
        if (mFooter != null) {
            int displayMode = ((LayoutParams) mFooter.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_FIXED)
                mFooterOffset = mFooter.getMeasuredHeight();
            mFooter.layout(
                    paddingLeft,
                    mFooterOffset + paddingTop + getMeasuredHeight(),
                    paddingLeft + mFooter.getMeasuredWidth(),
                    mFooterOffset + paddingTop + getMeasuredHeight() + mFooter.getMeasuredHeight()
            );
        }
        if (mContent != null) {
            mContent.layout(
                    paddingLeft,
                    mContentOffset + paddingTop,
                    paddingLeft + mContent.getMeasuredWidth(),
                    mContentOffset + paddingTop + mContent.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) return false;
        int action = ev.getActionMasked();
        int pointerIndex;
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                isBeingDragged = false;
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mLastMotionValue = mOrientation == HORIZONTAL ? ev.getX(pointerIndex) : ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == MotionEvent.INVALID_POINTER_ID) {
                    return false;
                }
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                startDragging(ev.getX(pointerIndex), ev.getY(pointerIndex));
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
        }
        return isBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) return false;
        int action = ev.getActionMasked();
        int pointerIndex;
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                isBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                startDragging(ev.getX(pointerIndex), ev.getY(pointerIndex));
                float value = mOrientation == HORIZONTAL ? ev.getX(pointerIndex) : ev.getY(pointerIndex);
                if (isBeingDragged) {
                    int offset = (int) ((value - mLastMotionValue) / DRAGGING_RESISTANCE);
                    if (mDirection == DIRECTION_POSITIVE && mContentOffset + offset < 0 ||
                            mDirection == DIRECTION_NEGATIVE && mContentOffset + offset > 0) {
                        offset = -mContentOffset;
                    }
                    offset(offset);
                    mLastMotionValue = value;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isBeingDragged) {
                    isBeingDragged = false;
                    animateOffsetToZero();
                }
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                return false;
        }

        return true;
    }

    private boolean canScroll(View view, float x, float y, int direction) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int scrollX = viewGroup.getScrollX();
            int scrollY = viewGroup.getScrollY();
            int count = viewGroup.getChildCount();
            for (int i = count - 1; i >= 0; --i) {
                View child = viewGroup.getChildAt(i);
                if (x + scrollX >= child.getLeft() &&
                        x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() &&
                        y + scrollY < child.getBottom() &&
                        canScroll(child, x + scrollX - child.getLeft(), y + scrollY - child.getTop(), direction)) {
                    return true;
                }
            }
        }
        return mOrientation == HORIZONTAL ? view.canScrollHorizontally(direction) : view.canScrollVertically(direction);
    }

    private void startDragging(float x, float y) {
        if (isBeingDragged) return;
        float diff = (mOrientation == HORIZONTAL ? x : y) - mLastMotionValue;
        if (diff > mTouchSlop && !canScroll(mContent, x, y, DIRECTION_NEGATIVE) ||
                diff < -mTouchSlop && !canScroll(mContent, x, y, DIRECTION_POSITIVE)) {
            mLastMotionValue = mLastMotionValue + (diff > 0 ? mTouchSlop : -mTouchSlop);
            mDirection = diff > 0 ? DIRECTION_POSITIVE : DIRECTION_NEGATIVE;
            isBeingDragged = true;
        }
    }

    private void offset(int offset) {
        if (offset == 0) return;
        if (mOrientation == HORIZONTAL) {
            offsetHorizontal(offset);
        } else {
            offsetVertical(offset);
        }
    }

    private void offsetHorizontal(int offset) {
        if (mHeader != null) {
            int displayMode = ((LayoutParams) mHeader.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_EDGE && mHeader.getLeft() <= 0) {
                if (mHeader.getLeft() + offset <= 0) {
                    mHeaderOffset += offset;
                    ViewCompat.offsetLeftAndRight(mHeader, offset);
                } else {
                    mHeaderOffset = 0;
                    ViewCompat.offsetLeftAndRight(mHeader, 0 - mHeader.getLeft());
                }
            } else if (displayMode == LayoutParams.DISPLAY_MODE_SCROLL) {
                mHeaderOffset += offset;
                ViewCompat.offsetLeftAndRight(mHeader, offset);
            }
        }
        if (mContent != null) {
            mContentOffset += offset;
            ViewCompat.offsetLeftAndRight(mContent, offset);
        }
        if (mFooter != null) {
            int displayMode = ((LayoutParams) mFooter.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_EDGE && mFooter.getRight() >= getMeasuredWidth()) {
                if (mFooter.getRight() + offset >= getMeasuredWidth()) {
                    mFooterOffset += offset;
                    ViewCompat.offsetLeftAndRight(mFooter, offset);
                } else {
                    mFooterOffset = 0;
                    ViewCompat.offsetLeftAndRight(mFooter, getMeasuredWidth() - mFooter.getRight());
                }
            } else if (displayMode == LayoutParams.DISPLAY_MODE_SCROLL) {
                mFooterOffset += offset;
                ViewCompat.offsetLeftAndRight(mFooter, offset);
            }
        }
    }

    private void offsetVertical(int offset) {
        if (mHeader != null) {
            int displayMode = ((LayoutParams) mHeader.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_EDGE && mHeader.getTop() <= 0) {
                if (mHeader.getTop() + offset <= 0) {
                    mHeaderOffset += offset;
                    ViewCompat.offsetTopAndBottom(mHeader, offset);
                } else {
                    mHeaderOffset = 0;
                    ViewCompat.offsetTopAndBottom(mHeader, 0 - mHeader.getTop());
                }
            } else if (displayMode == LayoutParams.DISPLAY_MODE_SCROLL) {
                mHeaderOffset += offset;
                ViewCompat.offsetTopAndBottom(mHeader, offset);
            }
        }
        if (mContent != null) {
            mContentOffset += offset;
            ViewCompat.offsetTopAndBottom(mContent, offset);
        }
        if (mFooter != null) {
            int displayMode = ((LayoutParams) mFooter.getLayoutParams()).getDisplayMode();
            if (displayMode == LayoutParams.DISPLAY_MODE_EDGE && mFooter.getBottom() >= getMeasuredHeight()) {
                if (mFooter.getBottom() + offset >= getMeasuredHeight()) {
                    mFooterOffset += offset;
                    ViewCompat.offsetTopAndBottom(mFooter, offset);
                } else {
                    mFooterOffset = 0;
                    ViewCompat.offsetTopAndBottom(mFooter, getMeasuredHeight() - mFooter.getBottom());
                }
            } else if (displayMode == LayoutParams.DISPLAY_MODE_SCROLL) {
                mFooterOffset += offset;
                ViewCompat.offsetTopAndBottom(mFooter, offset);
            }
        }
    }

    private void setAnimationDuration() {
        float pivotDistance = mOrientation == HORIZONTAL ?
                mTotalOffset > 0 ?
                        mHeader != null ? mHeader.getMeasuredWidth() : getMeasuredWidth() / 5 :
                        mFooter != null ? mFooter.getMeasuredWidth() : getMeasuredWidth() / 5
                :
                mTotalOffset > 0 ?
                        mHeader != null ? mHeader.getMeasuredHeight() : getMeasuredHeight() / 5 :
                        mFooter != null ? mFooter.getMeasuredHeight() : getMeasuredHeight() / 5;
        long duration = (long) (ANIMATION_DURATION * Math.abs(mTotalOffset) / pivotDistance);
        mBoundAnimation.setDuration(duration);
    }

    private void animateOffsetToZero() {
        mTotalOffset = mContentOffset;
        setAnimationDuration();
        clearAnimation();
        startAnimation(mBoundAnimation);
    }

    public void setHeaderView(View header) {
        mHeader = header;
        addView(header, 0);
    }

    public void setFooterView(View footer) {
        mFooter = footer;
        addView(footer, getChildCount());
    }

    public void setContentView(View content) {
        mContent = content;
        addView(content, mHeader == null ? 0 : 1);
    }

    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() > MAX_CHILDREN_COUNT) {
            throw new IllegalStateException("HorizontalBoundView can host at most three children");
        }
        super.addView(child, index, params);
    }

    public void setOrientation(@OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        return new LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        @IntDef({DISPLAY_MODE_FIXED, DISPLAY_MODE_SCROLL, DISPLAY_MODE_EDGE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DisplayMode {
        }

        public static final int DISPLAY_MODE_FIXED = 0;
        public static final int DISPLAY_MODE_SCROLL = 1;
        public static final int DISPLAY_MODE_EDGE = 2;

        int displayMode = DISPLAY_MODE_EDGE;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BoundLayout);
            displayMode = array.getInt(R.styleable.BoundLayout_displayMode, DISPLAY_MODE_EDGE);
            array.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            displayMode = source.displayMode;
        }

        public void setDisplayMode(@DisplayMode int displayMode) {
            this.displayMode = displayMode;
        }

        public int getDisplayMode() {
            return displayMode;
        }
    }
}
