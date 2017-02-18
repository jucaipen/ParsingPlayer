package com.hustunique.parsingplayer.player;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hustunique.parsingplayer.LogUtil;
import com.hustunique.parsingplayer.R;
import com.orhanobut.logger.Logger;

/**
 * Created by CoXier on 17-2-18.
 */

public class VerticalProgressBar extends View {
    private static final String TAG = "VerticalProgressBar";
    private Paint mProgressPaint;
    private Paint mBackgroundPaint;

    private int mProgress;
    private int mMaxProgress;

    private int mProgressColor;
    private int mBackgroundColor;


    public VerticalProgressBar(Context context) {
        super(context, null);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        initView();
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.VerticalProgressBarTheme);
            mMaxProgress = a.getInt(R.styleable.VerticalProgressBarTheme_max, 100);
            mProgressColor = a.getColor(R.styleable.VerticalProgressBarTheme_progressColor, Color.parseColor("#fb7299"));
            mBackgroundColor = a.getColor(R.styleable.VerticalProgressBarTheme_backgroundColor, Color.parseColor("#fffdff"));
        } finally {
            a.recycle();
        }
    }

    private void initView() {
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);
        mBackgroundPaint.setColor(mBackgroundColor);
        mProgressPaint.setStrokeWidth(10);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setStrokeWidth(10);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);
        // draw progress
        mProgress = 50;
        float dy = (mProgress + 0f) / mMaxProgress * getHeight();
        canvas.drawRect(0, getHeight() - dy, getWidth(), getHeight(), mProgressPaint);
    }

    public void setMax(int max) {
        mMaxProgress = max;
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    public void setProgress(int progress) {
        mProgress = progress > mMaxProgress ? mMaxProgress : progress;
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

}
