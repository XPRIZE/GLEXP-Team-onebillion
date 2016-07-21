package org.onebillion.xprz.mainui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBEmitter;
import org.onebillion.xprz.controls.OBEmitterCell;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.UPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by alan on 20/12/15.
 */
public class X_Test extends XPRZ_SectionController
{
    OBEmitter emitter;
    Bitmap tempBitmap;
    public X_Test()
    {
        super();
    }

    public void prepare()
    {
        super.prepare();
         loadFingers();
        loadEvent("mastera");
        events = Collections.singletonList("1a");

         doVisual(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }


    /*public void drawControls(Canvas canvas)
    {
        super.drawControls(canvas);
        if (tempBitmap != null)
        {
            Paint p = new Paint();
            p.setStrokeWidth(1);
            p.setColor(Color.BLACK);
            canvas.drawRect(1100,500,1100 + tempBitmap.getWidth(),500 + tempBitmap.getHeight(),p);
            canvas.drawBitmap(tempBitmap, 1100, 500, null);
        }
    }*/
}
