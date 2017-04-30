package com.infiniteset.drawableutils.graphics.manager.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.infiniteset.drawableutils.graphics.manager.Manager;

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