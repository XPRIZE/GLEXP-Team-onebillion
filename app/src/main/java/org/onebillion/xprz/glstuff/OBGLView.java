package org.onebillion.xprz.glstuff;

import android.content.Context;
import android.opengl.GLSurfaceView;

import org.onebillion.xprz.mainui.OBViewController;

/**
 * Created by alan on 02/05/16.
 */
public class OBGLView extends GLSurfaceView
{
    public OBViewController controller;

    public OBGLView(Context context)
    {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        if (controller != null)
            controller.viewWasLaidOut(changed, l, t, r, b);
    }


}
