package com.infiniteset.drawableutils.graphics.core;

import android.support.annotation.NonNull;

import com.infiniteset.drawableutils.graphics.manager.CacheManager;
import com.infiniteset.drawableutils.graphics.manager.CropManager;

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
    void post(@NonNull DrawableRequest request, DrawableHandlerCallbacks callback);

    /**
     * Drops previously posted request.
     *
     * @param request Request to be dropped.
     * @return True if request successfully scheduled to be dropped otherwise false.
     */
    boolean drop(@NonNull DrawableRequest request);

    /**
     * Sets cache manager.
     *
     * @param cacheManager Cache manager.
     */
    void setCacheManager(@NonNull CacheManager cacheManager);

    /**
     * Sets crop manager.
     *
     * @param cropManager Crop manager.
     */
    void setCropManager(@NonNull CropManager cropManager);
}
