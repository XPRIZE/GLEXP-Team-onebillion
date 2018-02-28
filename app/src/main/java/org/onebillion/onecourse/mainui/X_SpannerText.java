package org.onebillion.onecourse.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.renderscript.Type;
import android.view.View;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.PixelShaderProgram;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 07/04/2017.
 */

public class X_SpannerText extends OC_SectionController
{
    List<OBLabel> labels;

    public X_SpannerText()
    {
        super();
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        doVisual(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
        //
        Typeface font1 = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "onebillionreader-Regular_old2.otf");
        Typeface font2 = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "onebillionreader-Regular.otf");
        Typeface font3 = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "onebillionreader-Regular_new.otf");
        //
        labels = new ArrayList<>();
        int spacingY = 400;
        //
        String text[] = {"abc", "a1c", "123", "12a", "a12"};
        float widthPerLabel = bounds().width() / text.length;
        int counter = 0;
        //
        for (String value : text)
        {
            float positionX = widthPerLabel * counter + widthPerLabel / 2;
            float positionY = bounds().centerY() - spacingY;
            //
            OBLabel label = new OBLabel(value, font1, applyGraphicScale(100));
            label.setPosition(new PointF(positionX, positionY));
            label.sizeToBoundingBox();
            labels.add(label);
            attachControl(label);
            //
            positionY = bounds().centerY();
            //
            label = new OBLabel(value, font2, applyGraphicScale(100));
            label.setPosition(new PointF(positionX, positionY));
            label.sizeToBoundingBox();
            labels.add(label);
            attachControl(label);
            //
            positionY = bounds().centerY() + spacingY;
            //
            label = new OBLabel(value, font3, applyGraphicScale(100));
            label.setPosition(new PointF(positionX, positionY));
            label.sizeToBoundingBox();
            labels.add(label);
            attachControl(label);
            //
            counter++;
        }
    }


    public void checkLabel(OBLabel label)
    {
        try
        {
            lockScreen();
            label.setHighRange(1, 2, Color.RED);
            unlockScreen();
            //
            waitForSecs(0.6);
            //
            lockScreen();
            label.setHighRange(-1, -1, 0);
            unlockScreen();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBLabel findTarget (PointF pt)
    {
        return (OBLabel) finger(-1, 2, (List<OBControl>) (Object) labels, pt, true);
    }


    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBLabel c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkLabel(c);
                    }
                });
            }
        }
    }
}
