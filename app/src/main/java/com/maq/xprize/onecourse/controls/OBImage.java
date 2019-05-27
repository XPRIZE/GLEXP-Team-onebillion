package com.maq.xprize.onecourse.controls;

import android.graphics.Bitmap;

public class OBImage extends OBControl
{
    float intrinsicScale = 1;
    public OBImage()
    {
        super();
    }
    public OBImage(Bitmap d)
    {
        super();
        setContents(d);
    }

    public boolean needsTexture()
    {
        return true;
    }

    public void setContents(Bitmap d)
    {
        layer.setContents(d);
        layer.setBounds(0,0,d.getWidth(),d.getHeight());
    }

    public void setIntrinsicScale(float f)
    {
        intrinsicScale = f;
    }

    public float intrinsicScale()
    {
        return intrinsicScale;
    }
}
