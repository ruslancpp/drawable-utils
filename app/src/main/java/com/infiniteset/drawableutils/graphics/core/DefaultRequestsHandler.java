package com.infiniteset.drawableutils.graphics.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.infiniteset.drawableutils.graphics.manager.CacheManager;
import com.infiniteset.drawableutils.graphics.manager.CropManager;
import com.infiniteset.drawableutils.graphics.manager.DefaultDrawableScaleManager;
import com.infiniteset.drawableutils.graphics.manager.DrawableLoader;
import com.infiniteset.drawableutils.graphics.manager.DrawableScaleManager;
import com.infiniteset.drawableutils.graphics.manager.ResourceDrawableLoader;
import com.infiniteset.drawableutils.graphics.manager.TasksExecutor;
import com.infiniteset.drawableutils.graphics.util.FormatUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.os.Looper.getMainLooper;

/**
 * Default implementation of {@link RequestsHandler}.
 */
public class DefaultRequestsHandler implements RequestsHandler {

    private static final String TAG = DefaultRequestsHandler.class.getName();

    private static final String CACHE_KEY_FORMAT = "res_s:%s_b:%s_r:%s";

    private static TasksExecutor executor = new TasksExecutor();

    private final ArrayList<DrawableLoader> mLoaders = new ArrayList<>();
    private DrawableScaleManager mScaleManager = new DefaultDrawableScaleManager();
    private CropManager mCropManager;
    private CacheManager mCacheManager;

    private final CopyOnWriteArrayList<Action> mActions = new CopyOnWriteArrayList<>();

    public DefaultRequestsHandler(Context context) {
        mLoaders.add(new ResourceDrawableLoader(context));
    }

    @Override
    public void setCacheManager(@NonNull CacheManager cacheManager) {
        mCacheManager = cacheManager;
    }

    @Override
    public void setCropManager(@NonNull CropManager cropManager) {
        mCropManager = cropManager;
    }

    @Override
    public void post(@NonNull DrawableRequest request, DrawableHandlerCallbacks callback) {
        Action action = new Action();
        action.mCanceled = false;
        action.mRequest = request;
        action.mStartTime = SystemClock.uptimeMillis();
        action.mCallbackRef = new WeakReference<>(callback);
        mActions.add(action);

        Bitmap cache = mCacheManager.getMemoryCache(getKey(request));
        if (cache != null) {
            DrawableResponse response = new DrawableResponse(
                    cache,
                    request.getRegion(),
                    SystemClock.uptimeMillis() - action.mStartTime);
            onFinished(action, response);
            return;
        }

        executor.execute(new Task(action));
    }

    @Override
    public boolean drop(@NonNull DrawableRequest request) {
        for (Action action : mActions) {
            if (action.mRequest == request) {
                action.mCanceled = true;
                mActions.remove(action);
                return true;
            }
        }
        return false;
    }

    private DrawableLoader getDrawableLoader(DrawableRequest request) {
        for (int i = 0; i < mLoaders.size(); i++) {
            DrawableLoader loader = mLoaders.get(i);
            if (loader.canLoad(request)) {
                return loader;
            }
        }

        throw new UnsupportedOperationException("Requested drawable can not be loaded.");
    }

    private Drawable loadDrawable(DrawableRequest request) {
        return getDrawableLoader(request).load(request);
    }

    private void onFinished(final Action action, final DrawableResponse response) {
        Log.d(TAG, "Processing time: " + response.getProcessingTime() + "\n" + response.getRegion());

        mActions.remove(action);
        final DrawableHandlerCallbacks callback = action.mCallbackRef.get();
        if (callback != null && !action.mCanceled) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onFinished(action.mRequest, response);
                }
            });
        }
    }

    private String getKey(DrawableRequest request) {
        int resId = request.getDrawableId();
        Rect bounds = request.getBounds();
        RectF region = request.getRegion();

        if (request.getDrawableId() != 0) {
            return FormatUtils.hashKeyForDisk(String.format(Locale.US, CACHE_KEY_FORMAT, resId, bounds, region));
        }

        throw new IllegalArgumentException("Key generation is not supported for this type of DrawableRequest");
    }

    private class Task implements Runnable {

        private Action mAction;

        Task(Action action) {
            mAction = action;
        }

        @Override
        public void run() {
            //region Attempting to load bitmap from cache.
            DrawableRequest request = mAction.mRequest;
            String key = getKey(request);
            Bitmap cache = mCacheManager.getMemoryCache(key);
            if (cache == null) {
                cache = mCacheManager.getDiskCache(key);
                if (cache != null) {
                    mCacheManager.setMemoryCache(key, cache);
                }
            }

            if (cache != null) {
                DrawableResponse response = new DrawableResponse(
                        cache,
                        request.getRegion(),
                        SystemClock.uptimeMillis() - mAction.mStartTime);
                onFinished(mAction, response);
                return;
            }
            //endregion

            RectF region = mAction.mRequest.getRegion();
            Rect bounds = mAction.mRequest.getBounds();

            if (mAction.mCanceled) return;
            Drawable drawable = loadDrawable(request);

            if (mAction.mCanceled) return;
            Bitmap scaledBitmap = mScaleManager.scale(drawable, bounds.width(), bounds.height());

            if (mAction.mCanceled) return;
            Bitmap croppedBitmap = mCropManager.crop(scaledBitmap, region);

            mCacheManager.setMemoryCache(key, croppedBitmap);

            if (mAction.mCanceled) return;
            mCacheManager.setDiskCache(key, croppedBitmap);

            DrawableResponse response =
                    new DrawableResponse(
                            croppedBitmap,
                            request.getRegion(),
                            SystemClock.uptimeMillis() - mAction.mStartTime);

            onFinished(mAction, response);
        }
    }

    private class Action {

        private DrawableRequest mRequest;
        private boolean mCanceled;
        private WeakReference<DrawableHandlerCallbacks> mCallbackRef;
        /**
         * Used for debugging.
         */
        private long mStartTime;
    }
}