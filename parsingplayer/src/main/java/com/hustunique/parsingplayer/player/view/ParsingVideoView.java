/*
 *
 * Copyright (c) 2017 UniqueStudio
 *
 * This file is part of ParsingPlayer.
 *
 * ParsingPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ParsingPlayer; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package com.hustunique.parsingplayer.player.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.hustunique.parsingplayer.R;
import com.hustunique.parsingplayer.player.android.ParsingIntegrator;
import com.hustunique.parsingplayer.player.media.ParsingMediaManager;
import com.hustunique.parsingplayer.player.media.StateListener;
import com.hustunique.parsingplayer.util.LogUtil;

/**
 * Created by JianGuo on 1/16/17.
 * VideoView using {@link tv.danmaku.ijk.media.player.IMediaPlayer} as media player
 */

public class ParsingVideoView extends RelativeLayout implements StateListener {
    private static final String TAG = "ParsingVideoView";
    private float mSlop;
    private static final float SET_PROGRESS_VERTICAL_SLIP = 10f;

    private Context mContext;

    private ParsingMediaManager mMedia;


    private ControllerView mControllerView;
    private int mCurrentState;
    private int mTargetState;
    private int mSeekWhenPrepared;
    private int mCurrentBufferPercentage;

    public ParsingVideoView(Context context) {
        this(context, null);
    }

    public ParsingVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParsingVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ParsingVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.parsing_video_view, this);
        TextureRenderView renderView = (TextureRenderView) findViewById(R.id.texture_view);
        mControllerView = (ControllerView) findViewById(R.id.controller_view);
        mMedia = ParsingMediaManager.getInstance(mContext);
        mMedia.configureRenderView(renderView);
        mControllerView.setMediaPlayer(mMedia);
        mMedia.setStateListener(this);
        mControllerView.setRestoreListener(mRestoreListener);
        renderView.setOnClickListener(mRenderViewClickListener);
    }

    public void setRestoreListener(OnClickListener restoreListener) {
        mControllerView.setRestoreListener(restoreListener);
    }

    private View.OnClickListener mRestoreListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showFullscreen();
        }
    };

    private View.OnClickListener mRenderViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleMediaControlsVisibility();
        }
    };


    private float mDownX, mDownY;
    private boolean mChangePos = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mChangePos = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final float dx = ev.getX() - mDownX;
                final float dy = ev.getY() - mDownY;
                float absDx = Math.abs(dx);
                float absDy = Math.abs(dy);
                if (checkValidSlide(absDx, absDy)) {
                    mChangePos = true;
                    updatePosition(dx, mMedia.getCurrentPosition());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mChangePos) {
                    mMedia.seekTo(mSeekWhenPrepared);
                }
                break;
        }
        return this.mChangePos || super.dispatchTouchEvent(ev);
    }

    private boolean checkValidSlide(float absDx, float absDy) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(mContext);
        mSlop = viewConfiguration.getScaledEdgeSlop();
        return Float.compare(absDx, mSlop) > 0 && Float.compare(absDy, SET_PROGRESS_VERTICAL_SLIP) < 0;
    }

    private void updatePosition(float dx, int currentPos) {
        int totalTimeDuration = mMedia.getDuration();
        mSeekWhenPrepared = Math.min((int) ((currentPos + dx * totalTimeDuration) / getWidth()), totalTimeDuration);

    }

    public void play(String url) {
        mMedia.play(url);
    }


    private void toggleMediaControlsVisibility() {
        if (mControllerView.isShowing()) {
            mControllerView.hide();
        } else {
            mControllerView.show();
        }
    }


    @Override
    // FIXME: 1/23/17 Can't maintain View status after configuration changes, as we can't maintain mediaplayer here
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        LogUtil.d(TAG, "onRestoreInstanceState " + ss.toString());
        mCurrentState = ss.currentState;
        mTargetState = ss.targetState;
        mCurrentBufferPercentage = ss.currentBufferPercentage;
        int currPos = ss.currentPos;

//        seekTo(currPos);

    }


    private void showFullscreen() {
        ParsingIntegrator parsingIntegrator = new ParsingIntegrator(mContext);
        parsingIntegrator.parsingToPlay();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        ss.currentState = mCurrentState;
        ss.targetState = mTargetState;
        ss.currentPos = mMedia == null ? 0 : (int) mMedia.getCurrentPosition();
        ss.currentBufferPercentage = mCurrentBufferPercentage;
        LogUtil.d(TAG, "onSaveInstanceState " + ss.toString());
        return ss;
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPrepared() {
        mControllerView.show();
    }

    @Override
    public void onError(String msg) {
        // TODO: 2/19/17 Error Handling
    }

    @Override
    public void onCompleted() {
        mControllerView.complete();
    }

    class SavedState extends BaseSavedState {
        int currentState;
        int targetState;
        int currentBufferPercentage;
        int currentPos;
        private ClassLoader mClassLoader;

        public SavedState(Parcel in) {
            super(in);
            mClassLoader = mContext.getClassLoader();
            currentState = in.readInt();
            targetState = in.readInt();
            currentPos = in.readInt();
            currentBufferPercentage = in.readInt();
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            mClassLoader = loader;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentState);
            out.writeInt(targetState);
            out.writeInt(currentPos);
            out.writeInt(currentBufferPercentage);
        }

        @Override
        public String toString() {
            return "SavedState{" +
                    "currentState=" + currentState +
                    ", targetState=" + targetState +
                    ", currentBufferPercentage=" + currentBufferPercentage +
                    ", currentPos=" + currentPos +
                    '}';
        }
    }


}