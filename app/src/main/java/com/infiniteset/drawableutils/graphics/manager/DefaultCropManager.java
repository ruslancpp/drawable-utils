package com.infiniteset.drawableutils.graphics.manager;

import android.graphics.Bitmap;
import android.graphics.RectF;

import static com.infiniteset.drawableutils.graphics.util.DrawableUtils.cropBitmap;

/**
 * Default implementation of {@link CropManager}.
 */
public class DefaultCropManager implements CropManager {

    @Override
    public Bitmap crop(Bitmap sourceBitmap, RectF boundsRatio) {
        return cropBitmap(sourceBitmap, boundsRatio);
    }
}