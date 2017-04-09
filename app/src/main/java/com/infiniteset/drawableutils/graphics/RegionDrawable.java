package com.infiniteset.drawableutils.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
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

import java.lang.ref.WeakReference;

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
    private Drawable mDrawable;

    private int mAlpha = 1;
    private ColorFilter mColorFilter;

    private RegionDrawable() {
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (mCurrentRequest != null) {
            mHandler.drop(mCurrentRequest);
        }

        mCurrentRequest = new DrawableRequest(mDrawableRes, mRegion, new Rect(bounds), mContextRef.get());
        mHandler.post(mCurrentRequest, this);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mAlpha = alpha;
        if (mDrawable != null) {
            mDrawable.setAlpha(mAlpha);
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        if (mDrawable != null) {
            mDrawable.setColorFilter(mColorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void onFinished(DrawableRequest request, DrawableResponse response) {
        mDrawable = null;
        if (request == mCurrentRequest) return;

        mDrawable = response.getDrawable();
        setColorFilter(mColorFilter);
        setAlpha(mAlpha);
        invalidateSelf();
    }

    static class Builder {
        
        @DrawableRes
        private int mDrawableRes;
        private RectF mRegion;
        private Context mContext;
        private RequestsHandler mHandler = REQUESTS_HANDLER;

        public Builder(Context context) {
            mContext = context;
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

        public RegionDrawable build() {
            RegionDrawable drawable = new RegionDrawable();
            drawable.mContextRef = new WeakReference<>(mContext);
            drawable.mDrawableRes = mDrawableRes;
            drawable.mRegion = mRegion;
            drawable.mHandler = mHandler;
            return drawable;
        }
    }
}