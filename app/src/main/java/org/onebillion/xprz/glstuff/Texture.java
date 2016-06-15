package org.onebillion.xprz.glstuff;

import android.graphics.Bitmap;

/**
 * Created by alan on 01/05/16.
 */
public class Texture
{
    TextureAtlas textureAtlas;
    float uvl=0,uvt=0,uvr=1,uvb=1;
    public float scale;

    public Texture(Bitmap b,float sc)
    {
        textureAtlas = new TextureAtlas(b);
        scale = sc;
    }
    public Bitmap bitmap()
    {
        return textureAtlas.bitmap;
    }
}
