package com.infiniteset.drawableutils.graphics.core;

import android.graphics.Bitmap;
import android.graphics.RectF;

/**
 * Result with drawable with requested region to be drawn.
 */
public class DrawableResponse {

    private Bitmap mBitmap;
    private RectF mRegion;
    private long mProcessingTime;

    public DrawableResponse(Bitmap bitmap, RectF region, long processingTime) {
        mBitmap = bitmap;
        mRegion = region;
        mProcessingTime = processingTime;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public RectF getRegion() {
        return mRegion;
    }

    public void setRegion(RectF region) {
        mRegion = region;
    }

    public long getProcessingTime() {
        return mProcessingTime;
    }

    public void setProcessingTime(long time) {
        mProcessingTime = time;
    }
}
