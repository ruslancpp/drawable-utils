package com.infiniteset.drawableutils.graphics.core;

import android.support.annotation.UiThread;

/**
 * Callbacks of {@link RequestsHandler} events.
 */
public interface DrawableHandlerCallbacks {

    @UiThread
    void onFinished(DrawableRequest request, DrawableResponse response);

    @UiThread
    void onCancelled(DrawableRequest request);
}