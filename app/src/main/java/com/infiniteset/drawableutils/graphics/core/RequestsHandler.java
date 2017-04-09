package com.infiniteset.drawableutils.graphics.core;

/**
 * Common interface for handling {@link DrawableRequest}.
 */
public interface RequestsHandler {

    /**
     * Posts a request for regionned drawable.
     *
     * @param request  Requested drawable with regions.
     * @param callback Callbacks of the request progress.
     */
    void post(DrawableRequest request, DrawableHandlerCallbacks callback);

    /**
     * Drops previously posted request.
     *
     * @param request Request to be dropped.
     * @return True if request successfully scheduled to be dropped otherwise false.
     */
    boolean drop(DrawableRequest request);
}
