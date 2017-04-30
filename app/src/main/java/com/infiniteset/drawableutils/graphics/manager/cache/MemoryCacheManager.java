package com.infiniteset.drawableutils.graphics.manager.cache;

import android.graphics.Bitmap;

/**
 * Common interface for memory cache manager.
 */
public interface MemoryCacheManager {

    /**
     * Returns bitmap from memory cache.
     *
     * @param key Cache key.
     * @return Bitmap if it is in the memory otherwise null.
     */
    Bitmap getMemoryCache(String key);

    /**
     * Puts bitmap into memory cache under provided key.
     *
     * @param key    Cache key.
     * @param bitmap Bitmap to be cached.
     */
    void setMemoryCache(String key, Bitmap bitmap);
}