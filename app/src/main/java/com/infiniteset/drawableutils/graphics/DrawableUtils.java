package com.infiniteset.drawableutils.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Drawable related utils.
 */
final public class DrawableUtils {

    private DrawableUtils() {
    }

    /**
     * Converts drawables to the bitmap with provided width and height.
     *
     * @param drawable     Source drawable.
     * @param targetWidth  Width of resulting bitmap.
     * @param targetHeight Height of resulting bitmap.
     * @param config       Configurations of resulting butmap.
     * @return Converted bitmap.
     */
    public static Bitmap convertDrawableTpBitmap(Drawable drawable, int targetWidth,
                                                 int targetHeight, Bitmap.Config config) {
        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }
}
