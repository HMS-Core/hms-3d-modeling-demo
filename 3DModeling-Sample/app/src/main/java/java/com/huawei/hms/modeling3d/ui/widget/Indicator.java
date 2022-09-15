package com.huawei.hms.modeling3d.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class Indicator extends View {

    private static final int DEFAULT_TOTAL_INDEX = 5;
    private static final int DEFAULT_CURRENT_INDEX = 0;
    private static final int DEFAULT_CIRCLE_DISTANCE = 40;
    private static final int DEFAULT_CIRCLE_RADIUS = 8;
    private static final int DEFAULT_CIRCLE_SELECTED_RADIUS = 11;

    private int selectedColor;
    private int unselectedColor;
    private int currentIndex;
    private int totalIndex;
    private Paint paint;
    private int startX;
    private int startSelectedY;
    private int startY;
    private int centreX;

    public Indicator(Context context) {
        this(context, null);
    }

    public Indicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Indicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        selectedColor = Color.BLACK ;
        unselectedColor = Color.LTGRAY;
        totalIndex = DEFAULT_TOTAL_INDEX;
        currentIndex = DEFAULT_CURRENT_INDEX;
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centreX = getWidth() / 2;
        startSelectedY = getHeight() / 2 - DEFAULT_CIRCLE_SELECTED_RADIUS;
        startY = getHeight() / 2 - DEFAULT_CIRCLE_RADIUS;
        if (totalIndex % 2 == 0) {
            startX = centreX - (int) (1.0 * (totalIndex - 1) / 2 * DEFAULT_CIRCLE_DISTANCE);
        } else {
            startX = centreX - totalIndex / 2 * DEFAULT_CIRCLE_DISTANCE;
        }
        paint.setAntiAlias(true);
        paint.setColor(unselectedColor);
        int tempX = startX;
        for (int i = 0; i < totalIndex; i++) {
            RectF rectF = new RectF(tempX - DEFAULT_CIRCLE_RADIUS, startY,
                    tempX + DEFAULT_CIRCLE_RADIUS, startY + 2 * DEFAULT_CIRCLE_RADIUS);
            if (i == currentIndex) {
                paint.setColor(selectedColor);
                rectF = new RectF(tempX - DEFAULT_CIRCLE_SELECTED_RADIUS, startSelectedY,
                        tempX + DEFAULT_CIRCLE_SELECTED_RADIUS, startSelectedY + 2 * DEFAULT_CIRCLE_SELECTED_RADIUS);
            }
            canvas.drawOval(rectF, paint);
            if (paint.getColor() == selectedColor)
                paint.setColor(unselectedColor);
            tempX += DEFAULT_CIRCLE_DISTANCE;
        }
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int desired = DEFAULT_CIRCLE_SELECTED_RADIUS * 2 + getPaddingBottom() + getPaddingTop();
        if (specMode == MeasureSpec.EXACTLY) {
            result = Math.max(desired, specSize);
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(desired, specSize);
            } else result = desired;
        }
        return result;
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int desired = (totalIndex - 1) * DEFAULT_CIRCLE_DISTANCE + DEFAULT_CIRCLE_SELECTED_RADIUS * 2 + getPaddingLeft() + getPaddingRight();
        if (specMode == MeasureSpec.EXACTLY) {
            result = Math.max(desired, specSize);
        } else {
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(desired, specSize);
            } else result = desired;
        }
        return result;
    }

    public void setTotalIndex(int totalIndex) {
        int oldTotalIndex = this.totalIndex;
        if (totalIndex < 1)
            return;
        if (totalIndex < oldTotalIndex) {
            if (currentIndex == totalIndex)
                currentIndex = totalIndex - 1;
        }
        this.totalIndex = totalIndex;
        invalidate();
    }
}