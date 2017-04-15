package com.infiniteset.drawableutils.graphics.manager;

import android.graphics.Bitmap;
import android.graphics.RectF;

/**
 * Bitmap crop manager.
 */
public interface CropManager {

    /**
     * Crops bitmap with provided bounds ratios.
     *
     * @param sourceBitmap Source bitmap.
     * @param boundsRatio  Bounds ratios.
     * @return Cropped bitmap.
     */
    Bitmap crop(Bitmap sourceBitmap, RectF boundsRatio);
}