package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 07/07/16.
 */
public class OC_PresenterTest extends OC_Generic_Event
{
    OBPresenter presenter1, presenter2;

    public OC_PresenterTest ()
    {
        super();
    }

    public void miscSetup ()
    {
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            PointF worldLocation = OC_Generic.copyPoint(presenterControl.getWorldPosition());
            worldLocation.x = (float) (presenterControl.width() / 2.0);
            //
            presenter1 = OBPresenter.characterWithGroup(presenterControl, false);
            presenter1.control.setZPosition(200);
            presenter1.control.setProperty("restPos", worldLocation);
            presenter1.control.show();
            presenter1.control.setRight(0);
            //
            OBGroup presenterClone = (OBGroup) presenterControl.copy();
            attachControl(presenterClone);
            worldLocation = OC_Generic.copyPoint(presenterClone.getWorldPosition());
            worldLocation.x = (float) (bounds().width() - presenterClone.width() / 2.0);
            //
            presenter2 = OBPresenter.characterWithGroup(presenterClone, true);
            presenter2.control.setZPosition(200);
            presenter2.control.setProperty("restPos", worldLocation);
            presenter2.control.show();
            presenter2.control.setLeft(bounds().width());
        }
    }


    @Override
    public void prepare ()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetup();
        events = new ArrayList(Arrays.asList("a", "b"));
        doVisual(currentEvent());
    }

    @Override
    public void doMainXX () throws Exception
    {
        setStatus(STATUS_AWAITING_CLICK);
    }

    @Override
    public void doAudio (String scene) throws Exception
    {
        // do nothing
    }


    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
        hideControls("rope.*");
        hideControls(".*box.*");
        hideControls("back.*");
    }


    public void demoa() throws Exception
    {
        demoa1();
    }


    public void demoa2() throws Exception
    {
        PointF presenterLoc = (PointF)presenter1.control.propertyValue("restPos");
        PointF loc = OC_Generic.copyPoint(presenterLoc);
        loc.x = 0.5f*bounds().width();
        presenter1.walk(loc);
        presenter1.faceFrontReflected();
        waitForSecs(0.3f);
        presenter1.moveHandfromIndex(0,2,0.2f);
        waitForSecs(0.3f);
        presenter1.moveHandfromIndex(2,0,0.2f);
        waitForSecs(0.3f);
        presenter1.faceFront();
        presenter1.moveHandToEarController(this);
        waitForSecs(0.3f);
        waitForSecs(0.3f);
        presenter1.moveHandFromEarController(this);
        waitForSecs(0.3f);
        presenter1.walk(presenterLoc);
        presenter1.faceFrontReflected();
        presenter1.moveHandfromIndex(0,2,0.2f);
        presenter1.moveHandfromIndex(2,0,0.2f);
        //
    }


    public void demoa1 () throws Exception
    {
        //
        for (OBControl c : presenter1.control.members)
        {
            c.setBorderWidth(2.0f);
            c.setBorderColor(Color.BLACK);
        }
        //
        //
        presenter2.control.setBorderWidth(2.0f);
        presenter2.control.setBorderColor(Color.RED);
        //
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.3);
        //
//        gotItRightBigTick(true);
//        waitForSecs(0.3);
//        //
//        gotItRightBigTick(true);
//        waitForSecs(0.3);
//        //
//        gotItRightBigTick(true);
//        waitForSecs(0.3);
        //
        //
        List aud = currentAudio("DEMO");
        PointF position = OC_Generic.copyPoint((PointF) presenter1.control.propertyValue("restPos"));
        presenter1.walk(position);
//        presenter1.faceFront();
        presenter1.faceFrontReflected();
        waitForSecs(0.3);
        //
        position = OC_Generic.copyPoint((PointF) presenter2.control.propertyValue("restPos"));
        presenter2.walk(position);
//        presenter2.faceFront();
        presenter2.faceFrontReflected();
        waitForSecs(0.3);
        //
        //
        //
        presenter1.speak(Arrays.asList(aud.get(0)), this); // You have a name. Letters have names too!
        waitForSecs(0.8);
        //
        presenter2.speak(Arrays.asList(aud.get(1)), this); // Let’s hear the names of the letters.
        waitForSecs(0.3);
        //
        //
        //
        PointF currPos = OC_Generic.copyPoint(presenter1.control.getWorldPosition());
        PointF destPos = new PointF(currPos.x - bounds().width() * 0.1f, currPos.y);
        presenter1.walk(destPos);
//        presenter1.faceFront();
        presenter1.faceFrontReflected();
        waitForSecs(0.1);
        //
        currPos = OC_Generic.copyPoint(presenter2.control.getWorldPosition());
        destPos = new PointF(currPos.x + bounds().width() * 0.1f, currPos.y);
        presenter2.walk(destPos);
//        presenter2.faceFront();
        presenter2.faceFrontReflected();
        waitForSecs(0.1);
        //
        //
        //
        currPos = OC_Generic.copyPoint(presenter1.control.getWorldPosition());
        destPos = new PointF(currPos.x + bounds().width() * 0.1f, currPos.y);
        presenter1.walk(destPos);
//        presenter1.faceFront();
        presenter1.faceFrontReflected();
        waitForSecs(0.1);
        //
        currPos = OC_Generic.copyPoint(presenter2.control.getWorldPosition());
        destPos = new PointF(currPos.x - bounds().width() * 0.1f, currPos.y);
        presenter2.walk(destPos);
//        presenter2.faceFront();
        presenter2.faceFrontReflected();
        waitForSecs(0.1);
        //
        //
        //
        presenter1.moveHandfromIndex(0,2,0.2f);
        presenter2.moveHandfromIndex(0,2,0.2f);
        //
        waitForSecs(1.0);
        //
        presenter1.moveHandfromIndex(2,0,0.2f);
        presenter2.moveHandfromIndex(2,0,0.2f);
        //
        //
        //
        currPos = OC_Generic.copyPoint(presenter1.control.getWorldPosition());
        destPos = new PointF(currPos.x - bounds().width() * 0.1f, currPos.y);
        presenter1.walk(destPos);
//        presenter1.faceFront();
        presenter1.faceFrontReflected();
        waitForSecs(0.1);
        //
        currPos = OC_Generic.copyPoint(presenter2.control.getWorldPosition());
        destPos = new PointF(currPos.x + bounds().width() * 0.1f, currPos.y);
        presenter2.walk(destPos);
//        presenter2.faceFront();
        presenter2.faceFrontReflected();
        waitForSecs(0.1);
        //
        //
        //
        presenter1.faceRight();
        presenter2.faceRight();
        waitForSecs(0.6);
        //
        presenter1.faceLeft();
        presenter2.faceLeft();
        waitForSecs(0.6);
        //
//        presenter1.faceFront();
        presenter1.faceFrontReflected();
//        presenter2.faceFront();
        presenter2.faceFrontReflected();
        waitForSecs(0.6);
        /*
        //
        presenter1.moveHandToEarController(this);
        presenter2.moveHandToEarController(this);
        waitForSecs(1.2);
        presenter1.moveHandFromEarController(this);
        presenter2.moveHandFromEarController(this);
        waitForSecs(0.6);
        //
        //
        presenter1.speak(Arrays.asList(aud.get(2)), this); // Are you ready?
        waitForSecs(0.6);
        //
        presenter2.speak(Arrays.asList(aud.get(2)), this); // Are you ready?
        waitForSecs(0.6);
        //
        //
        //
        currPos = OC_Generic.copyPoint(presenter1.control.getWorldPosition());
        OBControl side = presenter1.control.objectDict.get("faceright");
        destPos = new PointF(-side.width() * 1.2f, currPos.y);
        presenter1.walk(destPos);
        //
        currPos = OC_Generic.copyPoint(presenter2.control.getWorldPosition());
        side = presenter2.control.objectDict.get("faceright");
        destPos = new PointF(bounds().width() + side.width() * 1.2f, currPos.y);
        presenter2.walk(destPos);
        //
        //
        //
        */
    }


}
