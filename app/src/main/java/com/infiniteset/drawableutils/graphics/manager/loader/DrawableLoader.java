package com.infiniteset.drawableutils.graphics.manager.loader;

import android.graphics.drawable.Drawable;

import com.infiniteset.drawableutils.graphics.core.DrawableRequest;

/**
 * Common interface for loading drawables from resources or URI.
 */
public interface DrawableLoader {

    Drawable load(DrawableRequest request);

    boolean canLoad(DrawableRequest request);
}