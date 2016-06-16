package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 16/06/16.
 */
public class X_Count100_S1  extends XPRZ_SectionController{

    int curTarget;
    int maxTarget;
    OBControl line;
    OBLabel counter;

    public void prepare()
    {
        lockScreen();
        super.prepare();
        loadFingers();
        loadEvent("master1");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);

        line = objectDict.get("line");
        Typeface tf = OB_utils.standardTypeFace();
        float textSize = MainActivity.mainActivity.applyGraphicScale(80);
        counter = new OBLabel("000",tf,textSize);
        counter.setColour(Color.RED);
        counter.setPosition(objectDict.get("numbox").position());
        counter.setZPosition((float)1.5);

        counter.setString("0");
        attachControl(counter);
        setSceneXX(currentEvent());

        unlockScreen();
    }

    public void start()
    {

    }

    public void setSceneXX(String scene)
    {
        deleteControls("sea");
        super.setSceneXX(scene);

        curTarget = Integer.valueOf(eventAttributes.get("start"));
        maxTarget = Integer.valueOf(eventAttributes.get("end"));
        if(eventAttributes.get("redraw") != null)
        {
            deleteControls("obj.*");
            //colourObjectFromAttributes:self.objectDict[@"image"]];

            OBGroup loadImage = (OBGroup) objectDict.get("image");
            loadImage.show();
            /*NSMutableArray *arr = [NSMutableArray array];
            NSArray *frames = [loadImage filterMembers:@"frame.*"];
            OBGroup *loadImage2;
            if([frames count] >0)
            {
                for(OBControl *con in frames)
                {
                    [con hide];
                }

                for(OBControl *con in frames)
                {
                    [con show];
                    OBControl *frame = [loadImage renderedImageControl];
                    frame.frame = loadImage.frame;
                    [frame setProperty:@"name" value:con.settings[@"name"]];
                    [frame setParent:self.objectDict[@"workrect"]];
                    [arr addObject:frame];
                    [con hide];
                }

                loadImage2 = [[OBGroup alloc] initWithMembers:arr];
                for(OBControl *con in loadImage2.members)
                {
                    [loadImage2.objectDict setObject:con forKey:con.settings[@"name"]];
                    [con hide];
                }

                [loadImage2.objectDict[@"frame1"] show];

            }
            else
            {
                loadImage2 = [[OBGroup alloc] initWithMembers:@[[loadImage renderedImageControl]]];
                loadImage2.frame = loadImage.frame;
            }
*/

            loadImage.show();
            String locs[] =  ((String)eventAttributes.get("loc")).split(",");
            float x = Float.valueOf(locs[0]);
            float y = Float.valueOf(locs[1]);
            float d1 =(1-2*x)/9.0f;
            float d2=Float.valueOf(locs[2]);
            int redraw = Integer.valueOf(eventAttributes.get("redraw"));
            OBControl workrect = objectDict.get("workrect");
            for(int i=1; i<=redraw; i++)
            {
                OBControl cont = loadImage.copy();
                cont.setPosition(OB_Maths.locationForRect(x+((i-1)%10)*d1,(float)(y+(Math.ceil(i/10)-1)*d2), workrect.frame));
                objectDict.put("obj"+i,cont);
                attachControl(cont);

                if(i >= curTarget)
                    cont.hide();
            }

            counter.setString(String.valueOf(curTarget-1));

        }

    }

    public void doMainXX() throws Exception
    {

    }



    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {

    }
}
