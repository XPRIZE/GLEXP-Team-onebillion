package org.onebillion.onecourse.mainui.oc_moreaddsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 16/03/2017.
 */

public class OC_MoreAddSubtract_S4 extends OC_SectionController
{
    Map<String,Integer> eventColour;
    boolean subtractMode, freeMode;
    int startNum, correctNum, currentIndex, currentPhase;

    public float graphicScale()
    {
        return this.bounds().width()/1024.0f;
    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        String startEvent = parameters.get("start");
        loadEvent(String.format("master%s",startEvent));
        eventColour = new ArrayMap<>();
        eventColour = OBMisc.loadEventColours(this);
        eventColour.put("hdiv", OBUtils.highlightedColour(eventColour.get("div")));
        eventColour.put("hmain",OBUtils.highlightedColour(eventColour.get("main")));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        OBGroup arrow = (OBGroup)objectDict.get("arrowhead");
        arrow.objectDict.get("background").setFillColor(eventColour.get("equation"));
        freeMode = OBUtils.getBooleanValue(eventAttributes.get("freemode"));
        subtractMode = OBUtils.getBooleanValue(eventAttributes.get("subtractmode"));
        eventColour.put("equation",OBUtils.colorFromRGBString(eventAttributes.get("colour_equation")));
        setSceneXX(currentEvent());

        if(!freeMode)
        {
            hideControls("divline_.*");
            objectDict.get("mainline").hide();
        }
        OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation")).show();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                performSel("demo", currentEvent());
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        String[] eqParts = eventAttributes.get("equ").split(",");
        startNum = Integer.valueOf(eqParts[0]);
        correctNum = Integer.valueOf(eqParts[2]);

        if(!freeMode)
            eqParts[1] = "88";

        String equation = String.format(subtractMode ? "%s – %s = %s" : "%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
        detachControl(objectDict.get("equation"));
        OC_Numberlines_Additions.loadEquation(equation, "equation",objectDict.get("eqbox"), eventColour.get("equation"),false,0,1,this);
        OBGroup equa = (OBGroup)objectDict.get("equation");
        for(int i=1; i<6; i++)
            OC_Numberlines_Additions.getLabelForEquation(i,equa).hide();

        loadNumberLine();
        OBGroup button = (OBGroup)objectDict.get("button");
        button.hide();
        button.objectDict.get("highlight").hide();
        currentIndex = 0;
        currentPhase = 1;
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(currentPhase == 1)
            {
                List<OBControl> targets = new ArrayList<>();
                targets.add(objectDict.get("mainline"));
                targets.addAll(filterControls("divline_.*"));
                if(finger(0,2,targets,pt)!= null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkNumberLine();
                                }
                            }
                    );
                }
            }
            else if((currentPhase == 2 || (freeMode && currentPhase != 1)) &&
                    finger(0,1,Arrays.asList(objectDict.get("button")),pt) != null )
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(
                        new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkButton();
                            }
                        }
                );

            }
            else if(currentPhase == 3 || (freeMode && currentPhase != 1))
            {
                OBControl cont = finger(0,1,filterControls("num_.*"),pt);

                if (freeMode && cont != null)
                {
                    if(((int)cont.propertyValue("num_value")  != startNum + currentIndex && !subtractMode)
                            ||((int)cont.propertyValue("num_value")  != startNum - currentIndex && subtractMode))
                        cont = null;
                }
                if(cont != null)
                {
                    setStatus(STATUS_BUSY);
                    final OBLabel cont2 = (OBLabel)cont;
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkTarget(cont2);
                                }
                            }
                    );


                }
                else if(freeMode && objectDict.get("cover").frame().contains(pt.x, pt.y))
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkCover();
                                }
                            }
                    );
                }
            }

        }
    }

    public void checkNumberLine() throws Exception
    {
        numberLineTouch();
        waitForSecs(0.3f);
        showNumbers();
        startButtonPhase();
    }

    public void checkButton() throws Exception
    {
        OBGroup button = (OBGroup)objectDict.get("button");
        button.objectDict.get("highlight").show();
        if(freeMode && ((currentIndex+startNum == 30 && !subtractMode) || (startNum == currentIndex && subtractMode)))
        {
            gotItWrongWithSfx();
            waitSFX();
            resetScene();
            button.objectDict.get("highlight").hide();
            setStatus(STATUS_AWAITING_CLICK);

        }
        else
        {
            makeCurrentJump();
            if(indexIsCorrect() && !freeMode)
            {
                waitForSecs(0.3f);
                startNumberPhase();

            }
            else
            {
                button.objectDict.get("highlight").hide();
                if(currentIndex >= 8 && (currentIndex+1)%3 == 0)
                {
                    playAudioQueuedScene("FEEDBACK",300,true);
                    slideNumberLine();

                }
                long time = setStatus(STATUS_AWAITING_CLICK);
                if(freeMode && currentIndex == correctNum && getAudioForScene(currentEvent(),"REMINDER") != null)
                {
                    reprompt(time, OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMINDER"), 300), 4);
                }

            }
        }
    }

    public void checkTarget(OBLabel cont) throws Exception
    {
        cont.setColour(Color.RED);
        if((int)cont.propertyValue("num_value")  == correctNum)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            showEquationPart(5);
            cont.setColour(eventColour.get("num"));
            waitForSecs(0.3f);
            int count = getAudioForScene(currentEvent(),"FINAL").size();
            if(count > 1)
            {
                playAudioScene("FINAL",0,true);
                waitForSecs(0.3f);

            }
            OBGroup equ = (OBGroup)objectDict.get("equation");
            OC_Numberlines_Additions.colourEquation(equ,1,5,Color.RED,this);
            playAudioScene("FINAL",count > 1 ? 1 : 0,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation(equ,1,5,eventColour.get("equation"),this);
            waitForSecs(1f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            resetScene();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            cont.setColour ( eventColour.get("num"));
            if(time== statusTime)
            {
                playAudioQueuedScene("INCORRECT",300,false);

            }
        }
    }

    public void checkCover() throws Exception
    {
        OBPath cover = (OBPath)objectDict.get("cover");
        cover.setStrokeColor(Color.RED);
        playSfxAudio("equcomplete",true);
        cover.setStrokeColor ( eventColour.get("equation"));
        resetScene();
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void startScene() throws Exception
    {
        waitForSecs(0.3f);
        if(freeMode)
            showEquationWithCover();
        else
            showEquationPart(1);
        playAudioQueuedScene("DEMO",300,true);
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }


    public void startButtonPhase() throws Exception
    {
        if(!performSel("demoButton",currentEvent()))
        {
            waitForSecs(0.3f);
            playSfxAudio("button",false);
            objectDict.get("button").show();
            waitSFX();
            waitForSecs(0.3f);
            currentPhase = 2;
            OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),"2",this);
        }
    }

    public void startNumberPhase() throws Exception
    {
        objectDict.get("button").hide();
        showEquationPart(4);
        waitForSecs(0.3f);
        performSel("demoEquation",currentEvent());
        currentPhase = 3;
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),"3",this);

    }

    public void showEquationPart(int num) throws Exception
    {
        lockScreen();
        if(freeMode && num == 5)
            objectDict.get("cover").hide();
        OC_Numberlines_Additions.getLabelForEquation(num,(OBGroup)objectDict.get("equation")).show();
        playSfxAudio(num == 1  ? "numpopon" : "equcomplete",false);
        unlockScreen();
        waitSFX();
    }

    public void loadNumberLine()
    {
        lockScreen();
        if(objectDict.get("mainline") != null)
        {
            deleteControls("mainline");
            deleteControls("divline_.*");
            deleteControls("num_.*");
            deleteControls("curveline_.*");
            objectDict.get("arrowhead").hide();

        }
        OC_Numberlines_Additions.drawNumberLine(objectDict.get("numberline"),28, subtractMode ? startNum-27 : startNum,
                eventColour.get("main"),eventColour.get("div"),eventColour.get("num"), this);
        hideControls("num_.*");
        unlockScreen();
    }

    public void slideNumberLine() throws Exception
    {
        float dist;
        if(freeMode)
        {
            if(subtractMode)
                dist = ((startNum - currentIndex) >=3 ? 3.0f : (startNum - currentIndex));
            else
                dist = ((startNum + currentIndex) <= 27 ? 3.0f : (30 - (startNum + currentIndex)));

        }
        else
        {
            if(subtractMode)
                dist = ((((startNum - currentIndex) - correctNum)>=2) ? 3.0f : 2.0f);
            else
                dist = (((correctNum - (startNum + currentIndex))>=2) ? 3.0f : 2.0f);

        }
        slideNumberLineBy(dist * objectDict.get("numberline").width()/27.0f);
    }

    public void slideNumberLineBy(float dist) throws Exception
    {
        List<OBControl> controls = new ArrayList<>();
        controls.addAll(filterControls("divline_.*"));
        controls.addAll(filterControls("num_.*"));
        controls.addAll(filterControls("curveline_.*"));
        controls.add(objectDict.get("arrowhead"));

        OBControl mainline = objectDict.get("mainline");
        PointF loc = OBMisc.copyPoint(mainline.position());
        if(subtractMode)
            loc.x += dist;
        else
            loc.x -= dist;

        OBMisc.moveControlWithAttached(mainline,controls,loc,Math.abs(dist/(objectDict.get("numberline").width()/27.0f))*0.12f, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }


    public void resetScene() throws Exception
    {
        OC_Numberlines_Additions.resetNumberLine(false, false, this);
        if(currentIndex >= 8)
        {
            PointF firstLoc = objectDict.get(String.format("divline_%d", startNum)).position();
            float dest;
            if(subtractMode)
                dest = objectDict.get("numberline").right() - firstLoc.x;
            else
                dest = firstLoc.x - objectDict.get("numberline").left();
            slideNumberLineBy(dest);
        }
        currentIndex = 0;
    }

    public boolean indexIsCorrect()
    {
        return ( startNum + currentIndex == correctNum && !subtractMode)||    ( startNum - currentIndex == correctNum && subtractMode);
    }

    public void showEquationWithCover() throws Exception
    {
        lockScreen();
        for(int i=1; i<5; i++)
            OC_Numberlines_Additions.getLabelForEquation(i,(OBGroup)objectDict.get("equation")).show();

        OBControl cover = objectDict.get("cover");
        cover.setPosition(OC_Numberlines_Additions.getLabelForEquation(5,(OBGroup)objectDict.get("equation")).getWorldPosition());
        cover.setBorderColor(eventColour.get("equation"));
        cover.show();
        playSfxAudio("equcomplete",false);
        unlockScreen();
        waitSFX();
    }

    public void makeCurrentJump() throws Exception
    {
        playSfxAudio("buttonpress",false);
        OC_Numberlines_Additions.animateCurve(subtractMode? startNum-currentIndex : startNum+currentIndex,
                !subtractMode, 0.4f, false, false, eventColour.get("curve"), this);
        currentIndex++;
        if(!freeMode)
        {
            lockScreen();
            if(currentIndex == 1)
            {
                OC_Numberlines_Additions.getLabelForEquation(2,(OBGroup)objectDict.get("equation")).show();
                OC_Numberlines_Additions.getLabelForEquation(3,(OBGroup)objectDict.get("equation")).show();
            }
            OBLabel label = OC_Numberlines_Additions.getLabelForEquation(3,(OBGroup)objectDict.get("equation"));
            label.setString(String.format("%d", currentIndex));
            unlockScreen();
        }
        waitSFX();
    }

    public void numberLineTouch() throws Exception
    {
        List<OBControl> divs = filterControls("divline_.*");
        OBPath main = (OBPath)objectDict.get("mainline");
        lockScreen();
        main.setStrokeColor(eventColour.get("hmain"));
        for(OBControl div : divs)
            ((OBPath)div).setStrokeColor(eventColour.get("hdiv"));
        playSfxAudio("linetouch",false);

        unlockScreen();
        waitForSecs(0.3f);
        waitSFX();
        lockScreen();
        main.setStrokeColor ( eventColour.get("main"));
        for(OBControl div : divs)
            ((OBPath)div).setStrokeColor(eventColour.get("div"));
        unlockScreen();
    }

    public void showNumbers() throws Exception
    {
        if(subtractMode)
        {
            for(int i = startNum; i > startNum-9; i--)
            {
                playSfxAudio("objectson",false);
                objectDict.get(String.format("num_%d",i)).show();
                waitSFX();
                waitForSecs(0.1f);
            }
        }
        else
        {
            for(int i = startNum; i < startNum+9; i++)
            {
                playSfxAudio("objectson",false);
                objectDict.get(String.format("num_%d",i)).show();
                waitSFX();
                waitForSecs(0.1f);
            }
        }
        showControls("num_.*");
    }

    public void pointerPointEquation(OBGroup equation, int at, String audio, float duration) throws Exception
    {
        PointF point = OB_Maths.locationForRect(0.6f,1f,equation.objectDict.get(String.format("part%d",at)).getWorldFrame());
        point.y = equation.bottom() + equation.height()*0.2f;
        movePointerToPoint(point,-15,duration,true);
        OC_Numberlines_Additions.colourEquation(equation,at,at,Color.RED,this);
        playAudio(audio);
        waitAudio();
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(equation,at,at,eventColour.get("equation"),this);
    }

    public void demo4a2() throws Exception
    {
        waitForSecs(0.5f);
        playAudioScene("DEMO",0,true);
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.05f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation")).getWorldFrame()),-15,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-35,0.5f,"DEMO",2,0.3f);
        lockScreen();
        showControls("divline_.*");
        objectDict.get("mainline").show();
        playSfxAudio("objectson",false);
        unlockScreen();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,this.bounds()),-35,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void demoButton4a2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        playSfxAudio("button",false);
        objectDict.get("button").show();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,objectDict.get("num_12").frame()),-25,0.5f,"DEMO2",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.75f,this.bounds()),-15,0.5f,"DEMO2",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,1.2f,objectDict.get("button").frame()),-35,0.5f,"DEMO2",2,0.3f);
        OBGroup button = (OBGroup)objectDict.get("button");
        for(int i= 0; i < 4; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button").frame()),-25,0.2f,true);
            button.objectDict.get("highlight").show();
            makeCurrentJump();
            button.objectDict.get("highlight").hide();
            movePointerToPoint(OB_Maths.locationForRect(1.2f,1.2f,objectDict.get("button").frame()),-25,0.2f,true);
            playAudioScene("DEMO2",3+i,true);
            waitForSecs(0.3f);
        }
        moveScenePointer(OB_Maths.locationForRect(2f,1.2f,objectDict.get("button").frame()),-35,0.5f,"DEMO2",7,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,objectDict.get("num_16").frame()),-30,0.5f,"DEMO2",8,0.3f);
        OBLabel label = (OBLabel)objectDict.get("num_16");
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,label.frame()),-30,0.2f,true);
        lockScreen();
        label.setColour(Color.RED);
        OC_Numberlines_Additions.getLabelForEquation(4,(OBGroup)objectDict.get("equation")).show();
        OC_Numberlines_Additions.getLabelForEquation(5,(OBGroup)objectDict.get("equation")).show();
        playSfxAudio("equcomplete",false);

        unlockScreen();
        waitSFX();
        label.setColour ( eventColour.get("num"));
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,label.frame()),-30,0.2f,true);
        waitForSecs(0.4f);
        for(int i=0; i<5; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"), i+1,getAudioForSceneIndex(currentEvent(),"DEMO2",9+i),i==0 ? 0.5f : 0.3f);
        }
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
        nextScene();
    }

    public void demo4b() throws Exception
    {
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showEquationPart(1);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void demoEquation4b() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.5f,0.3f,objectDict.get("num_11").frame()),-15,0.5f,"DEMO2",0,0.3f);
        for(int i=0; i<3; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"),i+1,getAudioForSceneIndex(currentEvent(),"DEMO2",1+i),i==0 ? 0.5f : 0.3f);
        }
        waitForSecs(0.3f);
        thePointer.hide();
    }

    public void demo4l() throws Exception
    {
        ((OBPath)objectDict.get("cover")).sizeToBoundingBoxIncludingStroke();

        waitForSecs(0.5f);
        playAudioScene("DEMO",0,true);

        loadPointer(POINTER_LEFT);

        showEquationWithCover();
        waitForSecs(0.3);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.8f,this.bounds()),-25,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.3f,OC_Numberlines_Additions.getLabelForEquation(3,(OBGroup)objectDict.get("equation")).getWorldFrame()),-15,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("cover").frame()),-15,0.35f,"DEMO",3,0.3f);
        PointF loc = OB_Maths.locationForRect(1.4f,-0.1f,objectDict.get("num_14").frame());
        PointF loc2 = OBMisc.copyPoint(loc);
        loc2.y = objectDict.get("mainline").position().y;
        moveScenePointer(loc,-25,0.5f,"DEMO",4,0.3f);
        movePointerToPoint(loc2,-25,0.2f,true);
        numberLineTouch();
        movePointerToPoint(OB_Maths.locationForRect(0.75f,0.8f,this.bounds()),-25,0.5f,true);
        waitForSecs(0.3f);
        showNumbers();
        waitForSecs(0.3f);
        playSfxAudio("button",false);
        objectDict.get("button").show();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(1.2f,1.2f,objectDict.get("button").frame()),-25,0.5f,"DEMO",5,0.3f);
        OBGroup button = (OBGroup)objectDict.get("button");
        for(int i= 0; i < 5; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button").frame()),-25,0.2f,true);
            button.objectDict.get("highlight").show();
            makeCurrentJump();
            button.objectDict.get("highlight").hide();
            movePointerToPoint(OB_Maths.locationForRect(1.2f,1.2f,objectDict.get("button").frame()),-25,0.2f,true);
            playAudioScene("DEMO",6+i,true);
            waitForSecs(0.3f);
        }
        OBLabel label = (OBLabel)objectDict.get("num_15");
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,label.frame()),-25,0.5f,"DEMO",11,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,label.frame()),-25,0.2f,true);
        lockScreen();
        label.setColour(Color.RED);
        OC_Numberlines_Additions.getLabelForEquation(5,(OBGroup)objectDict.get("equation")).show();
        objectDict.get("cover").hide();
        playSfxAudio("equcomplete",false);

        unlockScreen();
        waitSFX();
        label.setColour ( eventColour.get("num"));
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,label.frame()),-25,0.2f,true);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation"),1,5,Color.RED,this);
        playAudioScene("DEMO",12,true);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation") ,1,5,eventColour.get("equation"),this);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
        nextScene();
    }


    public void demo4m() throws Exception
    {
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showEquationWithCover();
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }

    public void demoButton4m() throws Exception
    {
        waitForSecs(0.3f);
        playSfxAudio("button",false);
        objectDict.get("button").show();
        waitSFX();
        waitForSecs(0.3f);
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        showEquationWithCover();
        playAudioScene("DEMO2",1,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("cover").frame()),-35,0.5f,"DEMO2",2,0.3f);
        thePointer.hide();
        currentPhase = 2;
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),"2",this);
    }

    public void demo5a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.05f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation")).getWorldFrame()),-15,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-35,0.5f,true);
        waitForSecs(0.3f);
        lockScreen();
        showControls("divline_.*");
        objectDict.get("mainline").show();
        playSfxAudio("objectson",false);
        unlockScreen();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,this.bounds()),-35,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void demoButton5a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        playSfxAudio("button",false);
        objectDict.get("button").show();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,objectDict.get("num_10").frame()),-25,0.5f,"DEMO2",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.75f,this.bounds()),-15,0.5f,"DEMO2",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,1.2f,objectDict.get("button").frame()),-35,0.5f,"DEMO2",2,0.3f);
        OBGroup button = (OBGroup)objectDict.get("button");
        for(int i= 0; i < 3; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button").frame()),-25,0.2f,true);
            button.objectDict.get("highlight").show();
            makeCurrentJump();
            button.objectDict.get("highlight").hide();
            movePointerToPoint(OB_Maths.locationForRect(1.2f,1.2f,objectDict.get("button").frame()),-25,0.2f,true);
            playAudioScene("DEMO2",3+i,true);
            waitForSecs(0.3f);
        }
        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.3f);
        startNumberPhase();
    }

    public void demo5b() throws Exception
    {
        waitForSecs(0.3f);
        showEquationPart(1);
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.05f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation")).getWorldFrame()),-15,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(2f,1.05f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation")).getWorldFrame()),-15,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,this.bounds()),-35,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }
    public void demo5j() throws Exception
    {
        ((OBPath)objectDict.get("cover")).sizeToBoundingBoxIncludingStroke();
        loadPointer(POINTER_LEFT);
        showEquationWithCover();
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.3f,OC_Numberlines_Additions.getLabelForEquation(3,(OBGroup)objectDict.get("equation")).getWorldFrame()),-15,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("cover").frame()),-15,0.35f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,this.bounds()),-35,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }

    public void demoButton5j() throws Exception
    {
        waitForSecs(0.3f);
        playSfxAudio("button",false);
        objectDict.get("button").show();
        waitSFX();
        waitForSecs(0.3f);
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("cover").frame()),-35,0.5f,"DEMO2",1,0.3f);
        thePointer.hide();
        currentPhase = 2;
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),"2",this);
    }

}
