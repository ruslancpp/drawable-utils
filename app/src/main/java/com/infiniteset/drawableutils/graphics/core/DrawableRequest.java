package com.infiniteset.drawableutils.graphics.core;

import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * Data container for requested drawable.
 */
public class DrawableRequest {

    @DrawableRes
    private int mDrawableId;
    private RectF mRegion;
    private Rect mBounds;

    public DrawableRequest(@DrawableRes int drawableId, RectF region, Rect bounds) {
        mDrawableId = drawableId;
        mRegion = region;
        mBounds = bounds;
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