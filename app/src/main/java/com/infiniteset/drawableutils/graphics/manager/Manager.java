package com.infiniteset.drawableutils.graphics.manager;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Base class for managers.
 */
public abstract class Manager {

    private WeakReference<Context> mContextRef;

    protected Manager() {
    }

    protected Manager(Context context) {
        setContext(context);
    }

    public Context getContext() {
        return (mContextRef != null) ? mContextRef.get() : null;
    }

    public void setContext(Context context) {
        mContextRef = new WeakReference<>(context);
    }
}