package com.infiniteset.drawableutils.graphics.manager.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;

import com.infiniteset.drawableutils.ScriptC_BoundsResolver;
import com.infiniteset.drawableutils.graphics.manager.Manager;

import static com.infiniteset.drawableutils.graphics.util.DrawableUtils.cropBitmap;

/**
 * Default implementation of {@link CropManager}.
 */
public class DefaultCropManager extends Manager implements CropManager {

    public DefaultCropManager(Context context) {
        super(context);
    }

    @Override
    public CropResult crop(Bitmap sourceBitmap, RectF region) {
        if (region == null) {
            region = resolveRegion(sourceBitmap);
        }
        return new CropResult(cropBitmap(sourceBitmap, region), region);
    }

    @Override
    public RectF resolveRegion(Bitmap bitmap) {
        RenderScript rs = RenderScript.create(getContext());
        Allocation bitmapMatrix = Allocation.createFromBitmap(rs, bitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);

        int[] bounds = {bitmap.getWidth(), bitmap.getHeight(), 0, 0};
        Allocation boundsVector = Allocation.createSized(rs, Element.I32(rs), bounds.length, Allocation.USAGE_SCRIPT);
        boundsVector.copy1DRangeFrom(0, bounds.length, bounds);

        ScriptC_BoundsResolver script = new ScriptC_BoundsResolver(rs);
        script.bind_bounds(boundsVector);
        script.forEach_resolveBounds(bitmapMatrix);
        boundsVector.copyTo(bounds);

        float width = (float) bitmap.getWidth();
        float height = (float) bitmap.getHeight();
        return new RectF(
                bounds[0] / width,
                bounds[1] / height,
                bounds[2] / width,
                bounds[3] / height);
    }
}