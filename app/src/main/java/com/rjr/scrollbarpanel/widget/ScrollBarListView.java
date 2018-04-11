package com.rjr.scrollbarpanel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;

import com.rjr.scrollbarpanel.R;

/**
 * Created by rjr on 2018/4/10.
 * 带滑动指示器的ListView
 */

public class ScrollBarListView extends ListView implements AbsListView.OnScrollListener {

    private View mScrollBarPanel; // 气泡布局
    private int mWidthMeasureSpec;
    private int mHeightMeasureSpec;
    private int mScrollBarPanelPosition; // 定义滑动条y坐标位置
    private int thumbOffset; // 指示器在ListView中y轴的高度
    private Animation mInAnimation; // 气泡控件显示时的动画
    private Animation mOutAnimation; // 气泡控件隐藏时的动画

    public ScrollBarListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnScrollListener(this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollBarListView);
        int layoutId = ta.getResourceId(R.styleable.ScrollBarListView_scrollBarPanel, -1);
        int inAnimId = ta.getResourceId(R.styleable.ScrollBarListView_scrollBarPanelInAnim, -1);
        int outAnimId = ta.getResourceId(R.styleable.ScrollBarListView_scrollBarPanelOutAnim, -1);
        ta.recycle();
        setScrollBarPanel(layoutId);
        initAnimations(inAnimId, outAnimId);
    }

    private void initAnimations(int inAnimId, int outAnimId) {
        mInAnimation = AnimationUtils.loadAnimation(getContext(), inAnimId);
        mOutAnimation = AnimationUtils.loadAnimation(getContext(), outAnimId);
        int scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
        mOutAnimation.setDuration(scrollBarFadeDuration);
        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mScrollBarPanel != null) {
                    mScrollBarPanel.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mScrollBarPanel != null && getAdapter() != null) {
            mWidthMeasureSpec = widthMeasureSpec;
            mHeightMeasureSpec = heightMeasureSpec;
            measureChild(mScrollBarPanel, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mScrollBarPanel != null && getAdapter() != null) {
            // 需测量气泡控件的宽高
            int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
            // 摆放气泡控件
            mScrollBarPanel.layout(
                    left,
                    mScrollBarPanelPosition,
                    left + mScrollBarPanel.getMeasuredWidth(),
                    mScrollBarPanelPosition + mScrollBarPanel.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // 在ViewGroup绘制的时候，在上面追加一个自己绘制的气泡布局
        if (mScrollBarPanel != null && mScrollBarPanel.getVisibility() == VISIBLE) {
            // 绘制前要确定mScrollBarPanel摆放的位置
            drawChild(canvas, mScrollBarPanel, getDrawingTime());
        }
    }

    /**
     * 渲染自己的气泡布局
     * @param layoutId
     */
    private void setScrollBarPanel(int layoutId) {
        mScrollBarPanel = LayoutInflater.from(getContext()).inflate(layoutId, this, false);
        mScrollBarPanel.setVisibility(View.GONE);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mScrollBarPanel == null) {
            return;
        }
        // 不断计算mScrollBarPanelPosition的值
        /*
         * computeVerticalScrollExtent() 滑动条在纵向滑动范围内经过放大后自身的高度
         * computeVerticalScrollOffset() 滑动条纵向幅度的位置 中间--5000
         * computeVerticalScrollRange() 滑动的范围：0--10000
         */
        // 1.计算系统滑块的高度
        // 思路：滑块高度 / ListView高度 = computeVerticalScrollExtent() / computeVerticalScrollRange()
        int height = Math.round(computeVerticalScrollExtent() * getMeasuredHeight() * 1.0f / computeVerticalScrollRange());
        // 2.得到系统滑块中间的y坐标
        // 思路：height / computeVerticalScrollExtent() = thumbOffset / computeVerticalScrollOffset()
        thumbOffset = Math.round(height * computeVerticalScrollOffset() * 1.0f / computeVerticalScrollExtent());
        thumbOffset += height / 2;
        // 得到气泡控件顶部的y轴坐标
        mScrollBarPanelPosition = thumbOffset - mScrollBarPanel.getMeasuredHeight() / 2;
        // 不断地控制气泡View的重新摆放
        int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
        mScrollBarPanel.layout(
                left,
                mScrollBarPanelPosition,
                left + mScrollBarPanel.getMeasuredWidth(),
                mScrollBarPanelPosition + mScrollBarPanel.getMeasuredHeight());
    }

    /**
     * 唤醒scrollBar回调
     * @param startDelay
     * @param invalidate
     * @return
     */
    @Override
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        boolean awaken = super.awakenScrollBars(startDelay, invalidate);
        if (mScrollBarPanel == null) {
            return awaken;
        }
        if (awaken) {
            if (mScrollBarPanel.getVisibility() == GONE) {
                mScrollBarPanel.setVisibility(VISIBLE);
                mScrollBarPanel.startAnimation(mInAnimation);
            }
            handler.removeCallbacks(r);
            handler.postDelayed(r, 100);
        }
        return awaken;
    }

    Handler handler = new Handler();

    Runnable r = new Runnable() {
        @Override
        public void run() {
            mScrollBarPanel.startAnimation(mOutAnimation);
        }
    };
}