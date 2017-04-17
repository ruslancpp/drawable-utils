package com.infiniteset.drawableutils.graphics.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import static com.infiniteset.drawableutils.graphics.util.DrawableUtils.cropBitmap;

/**
 * Default implementation of {@link CropManager}.
 */
public class DefaultCropManager extends Manager implements CropManager {

    public DefaultCropManager(Context context) {
        super(context);
    }

    @Override
    public Bitmap crop(Bitmap sourceBitmap, RectF region) {
        return cropBitmap(sourceBitmap, region);
    }
}