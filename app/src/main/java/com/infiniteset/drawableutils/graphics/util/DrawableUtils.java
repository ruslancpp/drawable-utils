package com.infiniteset.drawableutils.graphics.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
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
        Rect bounds = getCroppedBounds(sourceBitmap.getWidth(), sourceBitmap.getHeight(), boundsRatio, new Rect());
        return Bitmap.createBitmap(sourceBitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
    }

    /**
     * Returns cropped bounds of an area.
     *
     * @param width  Width of area.
     * @param height Height of area.
     * @param region Region of an area to crop.
     * @param cache  Cache of cropped bounds.
     * @return Cropped bounds.
     */
    public static Rect getCroppedBounds(int width, int height, RectF region, Rect cache) {
        int left = (int) Math.floor(width * region.left);
        int right = (int) Math.ceil(width * region.right);
        int top = (int) Math.floor(height * region.top);
        int bottom = (int) Math.ceil(height * region.bottom);
        cache.set(left, top, right, bottom);
        return cache;
    }
}
