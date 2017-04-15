package com.infiniteset.drawableutils.graphics.core;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.IntDef;

import com.infiniteset.drawableutils.graphics.manager.DrawableLoader;
import com.infiniteset.drawableutils.graphics.manager.ResourceDrawableLoader;

import java.lang.annotation.Retention;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.infiniteset.drawableutils.graphics.DrawableUtils.convertDrawableTpBitmap;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Default implementation of {@link RequestsHandler}.
 */
public class DefaultRequestsHandler implements RequestsHandler {

    private static final int REQUEST = 0;

    private static final int REQUEST_LOAD = 0;
    private static final int REQUEST_SCALE = 1;
    private static final int REQUEST_CROP = 2;
    private static final int REQUEST_ON_COMPLETED = 3;
    private static final int REQUEST_ON_CANCELLED = 4;

    private static HandlerThread REQUEST_DISPATCHER = new HandlerThread("DefaultRequestsHandler");

    private HandlerThread dispatcher = REQUEST_DISPATCHER;

    public DefaultRequestsHandler() {
        mLoaders.add(new ResourceDrawableLoader());
    }

    private ArrayList<DrawableLoader> mLoaders = new ArrayList<>();

    private CopyOnWriteArrayList<Action> mActions = new CopyOnWriteArrayList<>();

    @Override
    public void post(DrawableRequest request, DrawableHandlerCallbacks callback) {
        Action action = new Action();
        action.mCanceled = false;
        action.mRequest = request;
        mActions.add(action);
        send(REQUEST_LOAD, action);
    }

    @Override
    public boolean drop(DrawableRequest request) {
        for (Action action : mActions) {
            if (action.mRequest == request) {
                action.mCanceled = true;
                mActions.remove(action);
                return true;
            }
        }
        return false;
    }

    private void send(@RequestState int nextState, Action action) {
        action.mNextState = nextState;
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

    private Bitmap scaleDrawable(DrawableRequest request, Drawable drawable) {
        Drawable.ConstantState state = drawable.getConstantState();
        if (state != null) {
            drawable = state.newDrawable().mutate();
        }

        Rect bounds = request.getBounds();
        return convertDrawableTpBitmap(drawable, bounds.width(), bounds.height(), null);
    }

    private void onCancelled(final Action action) {
        final DrawableHandlerCallbacks callback = action.mCallbackRef.get();
        if (callback != null) {
            new Handler().post(new Runnable() {
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
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    DrawableResponse response = new DrawableResponse((Drawable) action.mStateResult);
                    callback.onFinished(action.mRequest, response);
                }
            });
        }
    }

    private class LoadTask extends AsyncTask<Action, Void, Action> {

        @Override
        protected Action doInBackground(Action... params) {
            Action action = params[0];
            action.mStateResult = loadDrawable(action.mRequest);
            return action;
        }

        @Override
        protected void onPostExecute(Action action) {
            send(REQUEST_SCALE, action);
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

            switch (action.mNextState) {
                case REQUEST_LOAD: {
                    new LoadTask().execute(action);
                    break;
                }
                case REQUEST_SCALE: {
                    break;
                }
                case REQUEST_CROP: {
                    break;
                }
                case REQUEST_ON_CANCELLED: {
                    break;
                }
                case REQUEST_ON_COMPLETED: {
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Bad request");
                }
            }
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
    @IntDef({REQUEST_LOAD, REQUEST_SCALE, REQUEST_CROP,
            REQUEST_ON_COMPLETED, REQUEST_ON_CANCELLED})
    @interface RequestState {
    }
}