package com.example.love_pet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private List<List<View>> lineViews = new ArrayList<>();
    private int lineHeight = 0;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        lineViews.clear();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        int lineWidth = 0;
        List<View> currentLine = new ArrayList<>();
        int paddingStart = getPaddingLeft();
        int paddingEnd = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int availableWidth = width - paddingStart - paddingEnd;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams margin = layoutParamsAsMargin(child);
            int childWidth = child.getMeasuredWidth() + margin.leftMargin + margin.rightMargin;
            int childHeight = child.getMeasuredHeight() + margin.topMargin + margin.bottomMargin;

            if (lineWidth + childWidth > availableWidth && !currentLine.isEmpty()) {
                lineViews.add(currentLine);
                height += lineHeight;
                lineWidth = 0;
                lineHeight = 0;
                currentLine = new ArrayList<>();
            }
            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
            currentLine.add(child);
        }
        if (!currentLine.isEmpty()) {
            lineViews.add(currentLine);
            height += lineHeight;
        }

        setMeasuredDimension(
            width,
            height + paddingTop + paddingBottom
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int x = getPaddingLeft();
        int y = getPaddingTop();

        for (List<View> line : lineViews) {
            for (View view : line) {
                MarginLayoutParams margin = layoutParamsAsMargin(view);
                int left = x + margin.leftMargin;
                int top = y + margin.topMargin;
                view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
                x += view.getMeasuredWidth() + margin.leftMargin + margin.rightMargin;
            }
            x = getPaddingLeft();
            y += lineHeight;
        }
    }

    public void addTag(String text, TagClickListener onClickListener) {
        TextView tagView = new TextView(getContext());
        tagView.setText(text);
        tagView.setTextSize(13f);
        tagView.setTextColor(0xFF666666);
        tagView.setBackgroundResource(R.drawable.bg_search_tag);
        tagView.setPadding(dpToPx(20), dpToPx(8), dpToPx(20), dpToPx(8));
        tagView.setSingleLine(true);
        if (onClickListener != null) {
            final String tagText = text;
            tagView.setOnClickListener(v -> onClickListener.onTagClick(tagText));
        }

        MarginLayoutParams lp = new MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.rightMargin = dpToPx(10);
        lp.bottomMargin = dpToPx(10);
        addView(tagView, lp);
    }

    private MarginLayoutParams layoutParamsAsMargin(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            return (MarginLayoutParams) lp;
        } else {
            return new MarginLayoutParams(lp);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // 点击回调接口
    public interface TagClickListener {
        void onTagClick(String text);
    }
}