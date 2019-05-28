package com.maq.xprize.onecourse.hindi.mainui.oc_shapes;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.Collections;
import java.util.EnumSet;

/**
 * Created by pedroloureiro on 03/05/2017.
 */

public class OC_Shapes_S6 extends OC_Generic_Event
{
    OBPath selectedColour;
    int colouredObjects;


    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        selectedColour = null;
        colouredObjects = 0;
        hideControls("arrow");

    }

    public void action_selectPaint (OBControl paint) throws Exception
    {
        selectedColour = (OBPath) paint;
        OBControl arrow = objectDict.get("arrow");
        playSfxAudio("select", false);
        if (arrow.hidden())
        {
            lockScreen();
            arrow.setPosition(selectedColour.position());
            arrow.setRight(bounds().width());
            arrow.show();
            unlockScreen();
        }
        else
        {
            PointF destination = OC_Generic.copyPoint(selectedColour.position());
            destination.x = arrow.position().x;
            OBAnim moveAnim = OBAnim.moveAnim(destination, arrow);
            OBAnimationGroup.runAnims(Collections.singletonList(moveAnim), 0.2, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
    }

    public void action_colourObject (OBControl control) throws Exception
    {
        control.disable();
        playSfxAudio("select_object", false);
        control.setFillColor(selectedColour.fillColor());
    }

    public void demo6a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look.
        OC_Generic.pointer_moveToObjectByName("container", -25, 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // You can colour : all the white shapes, like this.
        OC_Generic.pointer_moveToObjectByName("container", -5, 1.8f, EnumSet.of(OC_Generic.Anchor.ANCHOR_TOP, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("paint_1", 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Green for the triangles.
        action_selectPaint(objectDict.get("paint_1"));
        OC_Generic.pointer_moveToObjectByName("obj_3", -10, 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_colourObject(objectDict.get("obj_3"));
        OC_Generic.pointer_moveToObjectByName("obj_6", -10, 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_colourObject(objectDict.get("obj_6"));
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("paint_2", 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playAudioScene("DEMO", 3, false); // Pink for the circles.
        action_selectPaint(objectDict.get("paint_2"));
        OC_Generic.pointer_moveToObjectByName("obj_9", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_colourObject(objectDict.get("obj_9"));
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("obj_6", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void checkPaint (OBControl targetControl) throws Exception
    {
        setStatus(STATUS_CHECKING);
        action_selectPaint(targetControl);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkObject (Object target) throws Exception
    {
        setStatus(STATUS_CHECKING);
        OBPath control = (OBPath) target;
        String type = (String) control.attributes().get("type");
        if (type == null)
        {
            // wrong type of object
            setStatus(STATUS_AWAITING_CLICK);
            return;
        }
        //
        if (selectedColour == null)
        {
            // no colour selected
            setStatus(STATUS_AWAITING_CLICK);
            return;
        }
        if (type.equals(selectedColour.attributes().get("type")))
        {
            action_colourObject(control);
            colouredObjects++;
            if (colouredObjects == filterControls("obj.*").size())
            {
                waitSFX();
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                performSel("finDemo", currentEvent());
                //
                playAudioQueuedScene("FINAL", 0.3f, true);
                nextScene();
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            gotItWrongWithSfx();
            //
            setStatus(STATUS_AWAITING_CLICK);
            //
            String paintID = (String) selectedColour.attributes().get("id");
            int index = Integer.parseInt(paintID.replaceAll("paint_", ""));
            playAudioScene("INCORRECT", index - 1, false);
        }
    }

    public OBControl findPaint (PointF pt)
    {
        return finger(0, 1, filterControls("paint.*"), pt, true);
    }

    public OBControl findObject (PointF pt)
    {
        return finger(0, 1, zPositionSortedFilteredControls("obj.*"), pt, true);
    }

    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl paint = findPaint(pt);
            if (paint != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkPaint(paint);

                    }
                });
            }
            else
            {
                final OBControl object = findObject(pt);
                if (object != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            checkObject(object);

                        }
                    });
                }
            }
        }
    }

}
