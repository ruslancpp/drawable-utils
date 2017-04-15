package com.infiniteset.drawableutils.graphics.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
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

    /**
     * Crops bitmap with provided bounds ratios.
     *
     * @param sourceBitmap Source bitmap.
     * @param boundsRatio  Bounds ratios.
     * @return Cropped bitmap.
     */
    public static Bitmap cropBitmap(Bitmap sourceBitmap, RectF boundsRatio) {
        float startLeftPixel = sourceBitmap.getWidth() * boundsRatio.left;
        float endRightPixel = sourceBitmap.getWidth() * boundsRatio.right;
        float startTopPixel = sourceBitmap.getHeight() * boundsRatio.top;
        float endBottomPixel = sourceBitmap.getHeight() * boundsRatio.bottom;

        return Bitmap.createBitmap(sourceBitmap,
                (int) Math.floor(startLeftPixel),
                (int) Math.floor(startTopPixel),
                Math.round(endRightPixel - startLeftPixel),
                Math.round(endBottomPixel - startTopPixel));
    }
}
