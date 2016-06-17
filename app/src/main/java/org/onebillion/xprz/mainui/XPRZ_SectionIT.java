package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by alan on 17/06/16.
 */
public class XPRZ_SectionIT extends XPRZ_SectionController
{
    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
        unlockScreen();
    }

    public long switchStatus(String scene)
    {
        if (Arrays.asList("it6","it7").contains(scene))
            return setStatus(STATUS_WAITING_FOR_DRAG);
        if (Arrays.asList("it9").contains(scene))
            return setStatus(STATUS_AWAITING_ARROW_CLICK);
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
            }});
    }

    public void setSceneXX(String scene)
    {
        if (Arrays.asList("it1","it6","it9").contains(scene))
            deleteControls(".*");
        setSceneXX(scene);
        targets = filterControls("obj.*");
    }

    public void setSceneit5()
    {
        deleteControls(".*");
        loadEvent("masterb");
        super.setSceneXX(currentEvent());
        setUpDragObjectsForEvent("it5-2");
        targets = filterControls("obj.*");
    }

    public void setSceneit7()
    {
        deleteControls(".*");
        loadEvent("masterb");
        super.setSceneXX(currentEvent());
        setUpDragObjectsForEvent("it7-2");
        targets = filterControls("obj.*");
    }

    public void setSceneit8()
    {
        super.setSceneXX(currentEvent());
    }

    public void setSceneit9()
    {
        targets = filterControls("confirm");
    }

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }

    public void setUpDragObjectsForEvent(String ev)
    {
        Map<String,Object> event = (Map<String, Object>) eventsDict.get(ev);
        Map<String, Map<String, Object>> objectsdict = (Map<String, Map<String, Object>>) event.get("objectsdict");

        for (String k : objectsdict.keySet())
        {
            OBControl c = objectDict.get(k);
            PointF pt = c.position();
            c.setProperty("origpos",new PointF(pt.x,pt.y));
            c.setProperty("destpos",pointFromObjectsDict(objectsdict,k));
            Map<String,Object>odict = objectsdict.get(k);
            Map<String,Object>oattrs = (Map<String, Object>) odict.get("attrs");
            String fstr = (String) oattrs.get("scalex");
            if (fstr != null)
            {
                c.setProperty("scale",c.scale());
                float f = Float.parseFloat(fstr);
                c.setProperty("destscale",f);
            }
            c.setProperty("destcontainer",oattrs.get("parent"));
        }
    }

    public void demoit0() throws Exception 
    {
        PointF restPt = OB_Maths.locationForRect(0.5f,0.8f,new RectF(bounds()));
        PointF startPt = pointForDestPoint(restPt,15);
        loadPointerStartPoint(startPt,restPt);

        movePointerToPoint(restPt,-1,true);
        waitForSecs(0.1f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.5f);

        PointF butPt = OB_Maths.locationForRect(0.5f,1.1f,MainViewController().topRightButton.frame);

        float angle = thePointer.rotation();
        float angle2 = angle - (float)Math.toRadians(5);
        movePointerToPoint(butPt,0,-1,true);

        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.5f);

        movePointerToPoint(restPt,(float)Math.toDegrees(angle2),-1,true);

        waitAudio();
        waitForSecs(0.5);

        OBControl obj = objectDict.get("obj");
        playSfxAudio("tock",false);
        obj.show();


        waitAudio();
        waitForSecs(0.3);

        playAudioQueuedScene(currentEvent(),"DEMO2",true);
        waitAudio();

        waitForSecs(0.2);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,obj.frame()),(float)Math.toDegrees(angle),-1,true);


        waitForSecs(0.01);
        obj.highlight();
        playSfxAudio("tap",false);
        waitAudio();
        waitForSecs(0.6);
        obj.lowlight();
        movePointerToPoint(restPt,0.3f, true);
        waitForSecs(0.4);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

}
