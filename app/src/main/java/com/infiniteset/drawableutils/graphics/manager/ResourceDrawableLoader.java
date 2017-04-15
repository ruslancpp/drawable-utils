package com.infiniteset.drawableutils.graphics.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;

import com.infiniteset.drawableutils.graphics.core.DrawableRequest;

/**
 * Implementation of {@link DrawableLoader} that can load {@link Drawable} from resources.
 */
public class ResourceDrawableLoader implements DrawableLoader {
    @Override
    public Drawable load(DrawableRequest request) {
        Context context = request.getContext();
        if (context == null) return null;

        return AppCompatResources.getDrawable(context, request.getDrawableId());
    }

    @Override
    public boolean canLoad(DrawableRequest request) {
        return request.getDrawableId() != 0;
    }
}