package com.infiniteset.drawableutils.graphics.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Default implementation of {@link CacheManager}.
 * This implementation based on LRU memory and disk caches.
 * Default disk cache size is 1 MB and memory cache is 1/8 of max available memory.
 */
public class DefaultCacheManager extends Manager implements CacheManager {

    private final static String TAG = DefaultCacheManager.class.getName();
    private final static long DISK_CACHE_SIZE = 1 << 20;
    private final static int DISK_CACHE_INDEX = 0;

    private final Object mDiskCacheLock = new Object();

    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private boolean mDiskCacheStarting = true;

    public DefaultCacheManager(Context context) {
        super(context);
        initMemoryCache();
        initDiskCache();
    }

    @Override
    public Bitmap getMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    public void setMemoryCache(String key, Bitmap bitmap) {
        if (getMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public Bitmap getDiskCache(String key) {
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            bitmap = BitmapFactory.decodeStream(inputStream);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "Failed to load bitmap from disk cache", e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close stream", e);
                    }
                }
            }
            return bitmap;
        }
    }

    @Override
    public void setDiskCache(String key, Bitmap bitmap) {
        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "Failed to add to LRU disk cache", e);
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error", e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close stream", e);
                    }
                }
            }
        }
    }

    private void initMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void initDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = getContext().getDir("regions-cache", MODE_PRIVATE);
                if (diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE) {
                        try {
                            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                        } catch (final IOException e) {
                            Log.e(TAG, "Failed to initialise LRU disk cache", e);
                        }
                    }
                }
            }

            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to clear cache", e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to flush cache", e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close disk cache", e);
                }
            }
        }
    }
}