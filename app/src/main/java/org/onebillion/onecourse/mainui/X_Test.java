package org.onebillion.onecourse.mainui;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBEmitter;

import java.util.Collections;

/**
 * Created by alan on 20/12/15.
 */
public class X_Test extends OC_SectionController
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
    public void touchDownAtPoint(PointF pt, View v)
    {
        invalidateView(0,0,bounds().right,bounds().bottom);
    }
}
