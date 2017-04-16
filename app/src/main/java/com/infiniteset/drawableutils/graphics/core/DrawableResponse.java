package com.infiniteset.drawableutils.graphics.core;

import android.graphics.Bitmap;

/**
 * Result with drawable with requested region to be drawn.
 */
public class DrawableResponse {

    private Bitmap mBitmap;

    public DrawableResponse(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
