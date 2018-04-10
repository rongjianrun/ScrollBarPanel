package com.rjr.scrollbarpanel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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

    public ScrollBarListView(Context context) {
        this(context, null);
    }

    public ScrollBarListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollBarListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollBarListView);
        int layoutId = ta.getResourceId(R.styleable.ScrollBarListView_scrollBarPanel, -1);
        int inAnimId = ta.getResourceId(R.styleable.ScrollBarListView_scrollBarPanelInAnim, -1);
        int outAnimId = ta.getResourceId(R.styleable.ScrollBarListView_scrollBarPanelOutAnim, -1);
        ta.recycle();
        setScrollBarPanel(layoutId);
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
        // 不断计算mScrollBarPanelPosition的值
        /*
         * computeHorizontalScrollExtent() 滑动条在纵向滑动范围内经过放大后自身的高度
         * computeHorizontalScrollOffset() 滑动条纵向幅度的位置 中间--5000
         * computeHorizontalScrollRange() 滑动的范围：0--10000
         */
        // 不断地控制气泡View的重新摆放
        int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
        mScrollBarPanel.layout(
                left,
                mScrollBarPanelPosition,
                left + mScrollBarPanel.getMeasuredWidth(),
                mScrollBarPanelPosition + mScrollBarPanel.getMeasuredHeight());
    }
}













