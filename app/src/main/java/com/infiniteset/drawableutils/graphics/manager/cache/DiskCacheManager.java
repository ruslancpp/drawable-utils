package com.infiniteset.drawableutils.graphics.manager.cache;

import android.graphics.Bitmap;

/**
 * Common interface for disk cache manager.
 */
public interface DiskCacheManager {

    /**
     * Returns bitmap from disk cache.
     *
     * @param key Cache key.
     * @return Bitmap if it is on disk otherwise null.
     */
    Bitmap getDiskCache(String key);

    /**
     * Puts bitmap on disk cache under provided key.
     *
     * @param key    Cache key.
     * @param bitmap Bitmap to be cached.
     */
    void setDiskCache(String key, Bitmap bitmap);
}