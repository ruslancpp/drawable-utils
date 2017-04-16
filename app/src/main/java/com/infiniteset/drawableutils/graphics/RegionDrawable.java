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
import com.infiniteset.drawableutils.graphics.manager.CacheManager;
import com.infiniteset.drawableutils.graphics.manager.DefaultCacheManager;

import java.lang.ref.WeakReference;

import static com.infiniteset.drawableutils.graphics.util.DrawableUtils.getCroppedBounds;

/**
 * Drawable container that draws a particular region of provided drawable.
 */
final public class RegionDrawable extends Drawable implements DrawableHandlerCallbacks {

    private final static RequestsHandler REQUESTS_HANDLER = new DefaultRequestsHandler();

    private RequestsHandler mHandler = REQUESTS_HANDLER;

    @DrawableRes
    private int mDrawableRes;
    private RectF mRegion;
    private WeakReference<Context> mContextRef;
    private DrawableRequest mCurrentRequest;
    private int mIntrinsicWidth = -1;
    private int mIntrinsicHeight = -1;
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Rect mDirtyBounds = new Rect();

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (mCurrentRequest != null) {
            mHandler.drop(mCurrentRequest);
        }

        mCurrentRequest = new DrawableRequest(mDrawableRes, mRegion, new Rect(bounds), mContextRef.get());
        mHandler.post(mCurrentRequest, this);

        getCroppedBounds(bounds.width(), bounds.height(), mRegion, mDirtyBounds);
        mDirtyBounds.offset(bounds.left, bounds.top);
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
        return mIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @NonNull
    @Override
    public Rect getDirtyBounds() {
        return mDirtyBounds;
    }

    @Override
    public void onIntrinsicDimensionsDefined(int width, int height) {
        if (mIntrinsicHeight == height && mIntrinsicWidth == width) return;

        mIntrinsicWidth = width;
        mIntrinsicHeight = height;

        invalidateSelf();
    }

    @Override
    public void onFinished(DrawableRequest request, DrawableResponse response) {
        mBitmap = null;

        if (request != mCurrentRequest) return;

        mBitmap = response.getBitmap();
        invalidateSelf();
    }

    @Override
    public void onCancelled(DrawableRequest request) {
        //No-op
    }

    public static class Builder {

        @DrawableRes
        private int mDrawableRes;
        private RectF mRegion;
        private Context mContext;
        private RequestsHandler mHandler = REQUESTS_HANDLER;
        private CacheManager mCacheManager;

        public Builder(Context context) {
            mContext = context;
            mCacheManager = DefaultCacheManager.getInstance(context);
        }

        public Builder setDrawableRes(@DrawableRes int drawableRes) {
            mDrawableRes = drawableRes;
            return this;
        }

        public Builder setRegion(RectF region) {
            mRegion = region;
            return this;
        }

        public Builder setRequestHandler(RequestsHandler handler) {
            mHandler = handler;
            return this;
        }

        public Builder setCacheManager(CacheManager cacheManager) {
            mCacheManager = cacheManager;
            return this;
        }

        public RegionDrawable build() {
            RegionDrawable drawable = new RegionDrawable();
            drawable.mContextRef = new WeakReference<>(mContext);
            drawable.mDrawableRes = mDrawableRes;
            drawable.mRegion = mRegion;
            drawable.mHandler = mHandler;
            drawable.mHandler.setCacheManager(mCacheManager);
            return drawable;
        }
    }
}