package com.tunjid.projects.avantphotouploader.picassoutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

/**
 * Blurs an image using RenderScript and ScriptInstrinsic blur.
 */
public class BlurredTransformation implements com.squareup.picasso.Transformation {
    private final float radius;
    Context context;

    /**
     * Blurs an image using RenderScript and ScriptInstrinsic blur.
     * @param radius The radius of the CardView
     * @param context The Context the blur should run in.

     */

    public BlurredTransformation(final float radius, Context context) {
        this.radius = radius;
        this.context = context;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        final Bitmap blurredBitmap  = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        final RenderScript rs = RenderScript.create(context);
        final Allocation input = Allocation.createFromBitmap(rs, source, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(blurredBitmap);

        if (source != blurredBitmap) {
            source.recycle();
        }
        return blurredBitmap;
    }

    @Override
    public String key() {
        return "blurred(radius=" + radius + ")";
    }
}

