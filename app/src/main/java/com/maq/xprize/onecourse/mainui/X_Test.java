package com.maq.xprize.onecourse.mainui;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.R;
import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBEmitter;
import com.maq.xprize.onecourse.controls.OBShaderControl;
import com.maq.xprize.onecourse.glstuff.OBRenderer;
import com.maq.xprize.onecourse.glstuff.PixelShaderProgram;

import java.util.Collections;

/**
 * Created by alan on 20/12/15.
 */
public class X_Test extends OC_SectionController
{
    OBEmitter emitter;
    Bitmap tempBitmap;
    public PixelShaderProgram shaderProgram;
    OBShaderControl shc;

    void creShc()
    {
        if (shc != null)
            detachControl(shc);
        shc = new OBShaderControl();
        shc.setFrame(100,100,1024,1024);
        attachControl(shc);
    }
    public X_Test()
    {
        super();
        creShc();
     }

    public void prepare()
    {
        super.prepare();
         loadFingers();
        loadEvent("mastera");
        //hideControls("group.*");
        //hideControls("Path.*");
        OBControl c = objectDict.get("circle");
        //c.setShouldTexturise(false);
        //c.rotation = (float) Math.toRadians(45);
         doVisual(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void render (OBRenderer renderer)
    {
        if (shaderProgram == null)
        {
            shaderProgram = new PixelShaderProgram(R.raw.threegradientsfragmentshader,shc.width(),shc.height(),false);
        }
        shc.shaderProgram = shaderProgram;
        super.render(renderer);
    }
    public void touchDownAtPoint(PointF pt, View v)
    {
        invalidateView(0,0,bounds().right,bounds().bottom);
    }

    @Override
    public void onResume()
    {
        if (shc != null)
        {
            shaderProgram = null;
            //creShc();
        }
    }
}
