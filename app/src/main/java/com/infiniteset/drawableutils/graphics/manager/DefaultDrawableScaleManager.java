package com.infiniteset.drawableutils.graphics.manager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import static com.infiniteset.drawableutils.graphics.util.DrawableUtils.convertDrawableTpBitmap;

/**
 * Default implementation of {@link DrawableScaleManager}.
 */
public class DefaultDrawableScaleManager implements DrawableScaleManager {

    @Override
    public Bitmap scale(Drawable drawable, int width, int height) {
        Drawable.ConstantState state = drawable.getConstantState();
        if (state != null) {
            drawable = state.newDrawable().mutate();
        }

        return convertDrawableTpBitmap(drawable, width, height, null);
    }
}