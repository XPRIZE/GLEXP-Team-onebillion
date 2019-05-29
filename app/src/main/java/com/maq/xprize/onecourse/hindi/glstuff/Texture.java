package com.maq.xprize.onecourse.hindi.glstuff;

import android.graphics.Bitmap;

/**
 * Created by alan on 01/05/16.
 */
public class Texture
{
    public float scale;
    TextureAtlas textureAtlas;
    float uvl=0,uvt=0,uvr=1,uvb=1;

    public Texture(Bitmap b,float sc)
    {
        textureAtlas = new TextureAtlas(b);
        scale = sc;
    }
    public Bitmap bitmap()
    {
        if(textureAtlas == null)
            return null;

        return textureAtlas.bitmap;
    }

    public void cleanUp()
    {
        if(textureAtlas != null && textureAtlas.bitmap != null)
        {
            textureAtlas.bitmap.recycle();
            textureAtlas.bitmap = null;
            textureAtlas = null;
        }
    }

}
