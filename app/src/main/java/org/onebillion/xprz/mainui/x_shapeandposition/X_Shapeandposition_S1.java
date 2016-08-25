package org.onebillion.xprz.mainui.x_shapeandposition;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;

/**
 * Created by alan on 25/08/16.
 */
public class X_Shapeandposition_S1 extends XPRZ_SectionController
{
    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public void setSceneXX(String scene)
    {
        deleteControls("obj.*");
        super.setSceneXX(scene);
        targets = filterControls("obj.");
    }

    public void fin()
    {
        goToCard(X_Shapeandposition_S1B.class,(String)params);
    }

    public void demo1a() throws Exception
    {
        demoButtons();
        waitForSecs(0.3f);
        OBGroup obj = (OBGroup) objectDict.get("obj");
        PointF pt1 = OB_Maths.locationForRect(0.8f, 0.8f, obj.frame);
        movePointerToPoint(pt1,-20,-1,true);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.2f);
        OBControl leftobj = obj.objectDict.get("side1");
        OBControl rightobj = obj.objectDict.get("side2");
        playSfxAudio("hiliteleft",false);
        leftobj.show();
        waitSFX();
        PointF midpt = OB_Maths.locationForRect(0.5f, 0.5f, obj.frame);
        movePointerToPoint(midpt,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO2",true);
        PointF sidept = OB_Maths.locationForRect(0.02f, 0.3f, obj.frame);
        movePointerToPoint(sidept,-0.25f,true);
        waitAudio();
        waitForSecs(0.2f);
        movePointerToPoint(midpt,-1,true);
        playSfxAudio("hiliteright",false);
        lockScreen();
        leftobj.hide();
        rightobj.show();
        unlockScreen();
        waitSFX();
        playAudioQueuedScene("DEMO3",true);
        sidept = OB_Maths.locationForRect(0.98f, 0.3f,obj.frame);
        movePointerToPoint(sidept,-0.25f,true);
        waitAudio();
        waitForSecs(0.2f);
        movePointerToPoint(pt1,-1,true);
        rightobj.hide();
        waitForSecs(0.3f);
        playAudioScene("DEMO4",0,false);
        lockScreen();
        leftobj.show();
        rightobj.hide();
        unlockScreen();
        waitAudio();
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(-60),-1);
        playAudioScene("DEMO4",1,false);
        lockScreen();
        leftobj.hide();
        rightobj.show();
        unlockScreen();
        waitAudio();
        waitForSecs(0.2f);
        rightobj.hide();
        movePointerForwards(applyGraphicScale(-60),-1);
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO5",true);
        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
    }

    public void endOfScene() throws Exception
    {
        playAudioQueuedScene("CORRECT",true);
        OBGroup obj = (OBGroup) objectDict.get("obj1");
        OBControl side1 = obj.objectDict.get("side1");
        OBControl side2 = obj.objectDict.get("side2");
        playSfxAudio("hiliteleft",false);
        lockScreen();
        side1.show();
        unlockScreen();
        waitForSecs(1f);
        playSfxAudio("hiliteright",false);
        lockScreen();
        side1.hide();
        side2.show();
        unlockScreen();
        waitForSecs(1f);
        lockScreen();
        side2.hide();
        unlockScreen();
    }
    public void checkTarget(final OBControl targ)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        try
        {
            targ.highlight();
            if(targ == objectDict.get("obj1"))
            {
                gotItRightBigTick(true);
                waitForSecs(0.2f);
                waitAudio();
                targ.lowlight();
                waitForSecs(0.4f);
                endOfScene();
                waitForSecs(1f);
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                waitSFX();
                playAudioQueuedScene("INCORRECT",false);
                OBUtils.runOnOtherThreadDelayed(0.2f,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        targ.lowlight();
                    }
                });
                setStatus(saveStatus);
            }
        }
        catch(Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        OBControl c =finger(-1,2,targets,pt);
        return c;
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(target);
                    }
                });
            }
        }
    }

}
