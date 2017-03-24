package org.onebillion.onecourse.mainui;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBEmitter;
import org.onebillion.onecourse.controls.OBShaderControl;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.PixelShaderProgram;

import java.util.Collections;

/**
 * Created by alan on 20/12/15.
 */
public class X_Test extends OC_SectionController
{
    OBEmitter emitter;
    Bitmap tempBitmap;
    public PixelShaderProgram shaderProgram;
    OBShaderControl shc = new OBShaderControl();
    public X_Test()
    {
        super();
        shc.setFrame(100,100,1024,1024);
        attachControl(shc);
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
            shaderProgram = new PixelShaderProgram(R.raw.threegradientsfragmentshader,shc.width(),shc.height());
            shc.shaderProgram = shaderProgram;
        }
        super.render(renderer);
    }
    public void touchDownAtPoint(PointF pt, View v)
    {
        invalidateView(0,0,bounds().right,bounds().bottom);
    }
}
