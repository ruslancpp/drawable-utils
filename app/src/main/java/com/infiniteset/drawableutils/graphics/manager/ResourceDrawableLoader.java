package com.infiniteset.drawableutils.graphics.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;

import com.infiniteset.drawableutils.graphics.core.DrawableRequest;

/**
 * Implementation of {@link DrawableLoader} that can load {@link Drawable} from resources.
 */
public class ResourceDrawableLoader extends Manager implements DrawableLoader {

    public ResourceDrawableLoader(Context context) {
        super(context);
    }

    @Override
    public Drawable load(DrawableRequest request) {
        Context context = getContext();
        if (context == null) return null;

        return AppCompatResources.getDrawable(context, request.getDrawableId());
    }

    @Override
    public boolean canLoad(DrawableRequest request) {
        return request.getDrawableId() != 0;
    }
}