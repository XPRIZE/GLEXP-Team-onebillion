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
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.UPath;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by alan on 20/12/15.
 */
public class X_Test extends XPRZ_SectionController
{
    OBEmitter emitter;
    public X_Test()
    {
        super();
    }
    Bitmap tempBitmap;
    public void prepare()
    {
        super.prepare();
         loadFingers();
        loadEvent("mastera");
        OBControl c = objectDict.get("path1");
        UPath p = deconstructedPath("mastera","path1");
        targets = new ArrayList<>();
        targets.add(c);
        events = Collections.singletonList("1a");

        OBControl bob = new OBControl();
        bob.setFrame(100,100,500,400);
        bob.setBackgroundColor(Color.WHITE);
        bob.setBorderColor(Color.BLACK);
        bob.setBorderWidth(4);
        attachControl(bob);
        doVisual(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkTarget()
    {
        if (emitter != null && emitter.running())
        {
            emitter.stop();
            return;
        }
        emitter = new OBEmitter();
        emitter.setFrame(0,0,bounds().width(),bounds().height());
        OBEmitterCell cell = new OBEmitterCell();
        cell.birthRate = 5;
        cell.velocity = 240;
        cell.velocityRange = 30;
        cell.lifeTime = 5;
        cell.emissionRange = (float)(2 * Math.PI);
        cell.scale = 1f;
        cell.scaleSpeed = -0.1f;
        //cell.spin = (float)(Math.PI / 12);
        cell.spinRange = (float)(2 * Math.PI / 2);
        cell.posX = bounds().width()/2;
        cell.posY = bounds().height()/2;
        cell.alphaSpeed = -0.2f;
        //OBControl c = objectDict.get("Path1");
        OBPath star = StarWithScale(bounds().height() * 0.1f,true);
        star.enCache();
        cell.contents = star.cache;
        emitter.cells.add(cell);
        attachControl(emitter);
        emitter.run();
    }
    OBControl findTarget(PointF pt)
    {
        OBControl a =  finger(0,0,targets,pt);
        //tempBitmap = fingers.get(0).intersectsWithx(targets.get(0));
        //invalidateView(0, 0, view.getRight(), view.getBottom());
        return a;
    }

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            //final OBControl c = findTarget(pt);
            //if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        //playAudio("correct");
                        //checkTarget();
                        try
                        {
                            displayAward();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();

            }
        }

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
