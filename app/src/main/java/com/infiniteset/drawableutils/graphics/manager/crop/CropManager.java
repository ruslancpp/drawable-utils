package com.infiniteset.drawableutils.graphics.manager.crop;

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
     * @param region       Bounds region.
     * @return {@link CropResult}.
     */
    CropResult crop(Bitmap sourceBitmap, RectF region);

    /**
     * Resolves region of bitmap.
     *
     * @param bitmap Source bitmap.
     * @return Region
     */
    RectF resolveRegion(Bitmap bitmap);

    /**
     * Container for result of crop operation that contains a cropped bitmap and applied region to the
     * source bitmap.
     */
    class CropResult {

        private final Bitmap mBitmap;
        private final RectF mRegion;

        public CropResult(Bitmap bitmap, RectF region) {
            mBitmap = bitmap;
            mRegion = region;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public RectF getRegion() {
            return mRegion;
        }
    }
}