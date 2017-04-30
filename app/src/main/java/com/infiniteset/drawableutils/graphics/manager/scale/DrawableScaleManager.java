package com.infiniteset.drawableutils.graphics.manager.scale;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Drawable scale manager.
 */
public interface DrawableScaleManager {

    /**
     * Scales provided drawable to specified bounds.
     *
     * @param drawable Source drawable.
     * @param width    Width of resulting bitmap.
     * @param height   Height of resulting bitmap.
     * @return A bitmap obtained from provided drawable with provided width and height.
     */
    Bitmap scale(Drawable drawable, int width, int height);
}