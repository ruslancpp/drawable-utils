package com.infiniteset.drawableutils.graphics.core;

import android.graphics.drawable.Drawable;

/**
 * Result with drawable with requested region to be drawn.
 */
public class DrawableResponse {

    private Drawable mDrawable;

    public DrawableResponse(Drawable drawable) {
        mDrawable = drawable;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }
}
