package com.infiniteset.drawableutils.graphics.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.infiniteset.drawableutils.graphics.manager.CacheManager;
import com.infiniteset.drawableutils.graphics.manager.CropManager;
import com.infiniteset.drawableutils.graphics.manager.DefaultDrawableScaleManager;
import com.infiniteset.drawableutils.graphics.manager.DrawableLoader;
import com.infiniteset.drawableutils.graphics.manager.DrawableScaleManager;
import com.infiniteset.drawableutils.graphics.manager.ResourceDrawableLoader;
import com.infiniteset.drawableutils.graphics.manager.TasksExecutor;
import com.infiniteset.drawableutils.graphics.util.FormatUtils;

import java.lang.annotation.Retention;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.os.Looper.getMainLooper;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Default implementation of {@link RequestsHandler}.
 */
public class DefaultRequestsHandler implements RequestsHandler {

    private static final String CACHE_KEY_FORMAT = "res_s:%s_b:%s_r:%s";

    private static final int REQUEST = 0;

    private static final int REQUEST_LOAD = 0;
    private static final int REQUEST_SCALE = 1;
    private static final int REQUEST_CROP = 2;
    private static final int REQUEST_ON_CROPPED = 3;
    private static final int REQUEST_ON_COMPLETED = 4;

    private static TasksExecutor executor = new TasksExecutor();

    private HandlerThread dispatcher;

    private final ArrayList<DrawableLoader> mLoaders = new ArrayList<>();
    private DrawableScaleManager mScaleManager = new DefaultDrawableScaleManager();
    private CropManager mCropManager;
    private CacheManager mCacheManager;

    private final CopyOnWriteArrayList<Action> mActions = new CopyOnWriteArrayList<>();

    public DefaultRequestsHandler(Context context) {
        mLoaders.add(new ResourceDrawableLoader(context));
        dispatcher = new HandlerThread("DefaultRequestsHandler");
        dispatcher.start();
    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        mCacheManager = cacheManager;
    }

    @Override
    public void setCropManager(@NonNull CropManager cropManager) {
        mCropManager = cropManager;
    }

    @Override
    public void post(@NonNull DrawableRequest request, DrawableHandlerCallbacks callback) {
        Action action = new Action();
        action.mNextState = REQUEST_LOAD;
        action.mCanceled = false;
        action.mRequest = request;
        action.mCallbackRef = new WeakReference<>(callback);
        mActions.add(action);

        Bitmap cache = mCacheManager.getMemoryCache(getKey(request));
        if (cache != null) {
            action.mNextState = REQUEST_ON_COMPLETED;
            action.mStateResult = cache;
            onFinished(action);
            return;
        }

        send(action);
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

    private void send(Action action) {
        RequestHandler handler = new RequestHandler();
        Message message = handler.obtainMessage(REQUEST, action);
        handler.sendMessage(message);
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

    private void onCancelled(final Action action) {
        final DrawableHandlerCallbacks callback = action.mCallbackRef.get();
        if (callback != null) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onCancelled(action.mRequest);
                }
            });
        }
    }

    private void onFinished(final Action action) {
        mActions.remove(action);
        final DrawableHandlerCallbacks callback = action.mCallbackRef.get();
        if (callback != null) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    DrawableResponse response = new DrawableResponse((Bitmap) action.mStateResult);
                    callback.onFinished(action.mRequest, response);
                }
            });
        }
    }

    private void onIntrinsicDimensionsDefined(Action action, final int width, final int height) {
        final DrawableHandlerCallbacks callback = action.mCallbackRef.get();
        if (callback != null) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onIntrinsicDimensionsDefined(width, height);
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

    private abstract class Task implements Runnable {

        protected Action mAction;

        Task(Action action) {
            mAction = action;
        }
    }

    private class LoadTask extends Task {

        LoadTask(Action action) {
            super(action);
        }

        @Override
        public void run() {
            DrawableRequest request = mAction.mRequest;
            String key = getKey(request);
            Bitmap cache = mCacheManager.getMemoryCache(key);
            if (cache == null) {
                cache = mCacheManager.getDiskCache(key);
                if (cache != null) {
                    mCacheManager.setMemoryCache(key, cache);
                }
            }

            int width;
            int height;

            if (cache != null) {
                mAction.mStateResult = cache;
                mAction.mNextState = REQUEST_ON_COMPLETED;
                RectF region = mAction.mRequest.getRegion();
                width = (int) (cache.getWidth() / (region.right - region.left));
                height = (int) (cache.getHeight() / (region.bottom - region.top));
            } else {
                Drawable drawable = loadDrawable(request);
                mAction.mStateResult = drawable;
                width = drawable.getIntrinsicWidth();
                height = drawable.getIntrinsicHeight();
                mAction.mNextState = REQUEST_SCALE;
            }

            onIntrinsicDimensionsDefined(mAction, width, height);
            send(mAction);
        }
    }

    private class ScaleTask extends Task {

        ScaleTask(Action action) {
            super(action);
        }

        @Override
        public void run() {
            Rect bounds = mAction.mRequest.getBounds();
            mAction.mStateResult = mScaleManager.scale((Drawable) mAction.mStateResult, bounds.width(), bounds.height());
            mAction.mNextState = REQUEST_CROP;
            send(mAction);
        }
    }

    private class SaveCacheTask extends Task {

        SaveCacheTask(Action action) {
            super(action);
        }

        @Override
        public void run() {
            DrawableRequest request = mAction.mRequest;
            String key = getKey(request);
            Bitmap bitmap = (Bitmap) mAction.mStateResult;

            mCacheManager.setMemoryCache(key, bitmap);
            mCacheManager.setDiskCache(key, bitmap);
            mAction.mNextState = REQUEST_ON_COMPLETED;
            send(mAction);
        }
    }

    private class CropTask extends Task {

        CropTask(Action action) {
            super(action);
        }

        @Override
        public void run() {
            RectF region = mAction.mRequest.getRegion();
            mAction.mStateResult = mCropManager.crop((Bitmap) mAction.mStateResult, region);
            mAction.mNextState = REQUEST_ON_CROPPED;
            send(mAction);
        }
    }

    private class RequestHandler extends Handler {

        RequestHandler() {
            super(dispatcher.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            Action action = (Action) msg.obj;

            if (action.mCanceled) {
                onCancelled(action);
                return;
            }

            Task task;
            switch (action.mNextState) {
                case REQUEST_LOAD: {
                    task = new LoadTask(action);
                    break;
                }
                case REQUEST_SCALE: {
                    task = new ScaleTask(action);
                    break;
                }
                case REQUEST_CROP: {
                    task = new CropTask(action);
                    break;
                }
                case REQUEST_ON_CROPPED: {
                    task = new SaveCacheTask(action);
                    break;
                }
                case REQUEST_ON_COMPLETED: {
                    onFinished(action);
                    return;
                }
                default: {
                    throw new IllegalArgumentException("Bad request");
                }
            }

            executor.execute(task);
        }
    }

    private class Action {

        private DrawableRequest mRequest;
        private Object mStateResult;
        private boolean mCanceled;
        @RequestState
        private int mNextState;
        WeakReference<DrawableHandlerCallbacks> mCallbackRef;
    }

    @Retention(SOURCE)
    @IntDef({REQUEST_LOAD, REQUEST_SCALE, REQUEST_CROP, REQUEST_ON_CROPPED, REQUEST_ON_COMPLETED})
    @interface RequestState {
    }
}