package com.infiniteset.drawableutils.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.infiniteset.drawableutils.graphics.core.DefaultRequestsHandler;
import com.infiniteset.drawableutils.graphics.core.DrawableHandlerCallbacks;
import com.infiniteset.drawableutils.graphics.core.DrawableRequest;
import com.infiniteset.drawableutils.graphics.core.DrawableResponse;
import com.infiniteset.drawableutils.graphics.core.RequestsHandler;

import static com.infiniteset.drawableutils.graphics.util.DrawableUtils.getCroppedBounds;

/**
 * Drawable container that draws a particular region of provided drawable.
 */
final public class RegionDrawable extends Drawable implements DrawableHandlerCallbacks {

    private RequestsHandler mHandler;

    @DrawableRes
    private int mDrawableRes;
    private RectF mRegion;
    private DrawableRequest mCurrentRequest;
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Rect mDirtyBounds = new Rect();
    private Rect mPrevBounds = new Rect();

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (mPrevBounds == bounds) return;

        mPrevBounds.set(bounds);

        if (mCurrentRequest != null) {
            mHandler.drop(mCurrentRequest);
        }

        mCurrentRequest = new DrawableRequest(mDrawableRes, mRegion, new Rect(bounds));
        mHandler.post(mCurrentRequest, this);

        updateDirtyBounds();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, null, getDirtyBounds(), mPaint);
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }

    @NonNull
    @Override
    public Rect getDirtyBounds() {
        return mDirtyBounds;
    }

    @Override
    public void onFinished(DrawableRequest request, DrawableResponse response) {
        mBitmap = null;

        if (request != mCurrentRequest) return;

        mRegion = response.getRegion();
        mBitmap = response.getBitmap();

        updateDirtyBounds();

        invalidateSelf();
    }

    private void updateDirtyBounds() {
        getCroppedBounds(getBounds().width(), getBounds().height(), mRegion, mDirtyBounds);
        mDirtyBounds.offset(getBounds().left, getBounds().top);
    }

    public static class Factory {

        private RequestsHandler mHandler;

        public Factory(Context context) {
            mHandler = new DefaultRequestsHandler(context);
        }

        public void setHandler(RequestsHandler handler) {
            mHandler = handler;
        }

        public RegionDrawable getInstance(@DrawableRes int resId, RectF region) {
            RegionDrawable drawable = new RegionDrawable();
            drawable.mDrawableRes = resId;
            drawable.mRegion = region;
            drawable.mHandler = mHandler;
            return drawable;
        }
    }
}