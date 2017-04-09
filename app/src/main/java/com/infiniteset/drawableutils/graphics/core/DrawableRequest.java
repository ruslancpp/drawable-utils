package com.infiniteset.drawableutils.graphics.core;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Data container for requested drawable.
 */
public class DrawableRequest {

    @DrawableRes
    private int mDrawableId;
    private RectF mRegion;
    private Rect mBounds;
    private WeakReference<Context> mContextRef;

    public DrawableRequest(@DrawableRes int drawableId, RectF region, Rect bounds, Context context) {
        mContextRef = new WeakReference<>(context);
        mDrawableId = drawableId;
        mRegion = region;
        mBounds = bounds;
    }

    @Nullable
    public Context getContext() {
        return mContextRef.get();
    }

    @DrawableRes
    public int getDrawableId() {
        return mDrawableId;
    }

    public RectF getRegion() {
        return mRegion;
    }

    @NonNull
    public Rect getBounds() {
        return mBounds;
    }
}