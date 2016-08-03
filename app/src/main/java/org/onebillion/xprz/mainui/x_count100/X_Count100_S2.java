package org.onebillion.xprz.mainui.x_count100;

import android.graphics.Color;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by michal on 02/08/16.
 */
public class X_Count100_S2 extends XPRZ_SectionController
{

    OBGroup testGroup;
    @Override
    public void prepare()
    {
        super.prepare();
        loadEvent("master");

        OBLabel label = new OBLabel("1", OBUtils.standardTypeFace(), 500);
        label.setColour(Color.BLUE);
        label.setPosition(OB_Maths.locationForRect(0.5f,0.5f,bounds()));
        attachControl(label);




        OBGroup group = new OBGroup(Collections.singletonList((OBControl) label));
        attachControl(group);

        OBGroup group2 = (OBGroup)group.copy();
attachControl(group2);

        group2.setBorderWidth(10);
        group2.setBorderColor(Color.BLUE);

        label.setColour(Color.BLACK);
        group.sizeToTightBoundingBox();
        group.setBorderWidth(10);
        group.setBorderColor(Color.RED);
        group2.setZPosition(5);
        group.setZPosition(10);
    }


    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                waitForSecs(0.5f);


            }
        });
    }
}
