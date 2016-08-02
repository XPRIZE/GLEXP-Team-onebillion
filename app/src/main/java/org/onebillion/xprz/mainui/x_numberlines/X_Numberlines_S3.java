package org.onebillion.xprz.mainui.x_numberlines;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 29/07/16.
 */
public class X_Numberlines_S3 extends XPRZ_SectionController
{
    PointF frogPosition;
    int currentJump, targetNum, hiliteNum;
    int hiliteColour, numColour;
    boolean frogIsAnimated;


    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master3");

        events = Arrays.asList(eventAttributes.get("scenes").split(","));

        hiliteColour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        numColour = OBUtils.colorFromRGBString(eventAttributes.get("numcolour"));

        frogIsAnimated = false;
        objectDict.get("river").setZPosition(-0.1f);
        objectDict.get("river").setWidth(bounds().width());
        objectDict.get("river").setPosition(OB_Maths.locationForRect(0.5f,0.5f,bounds()));
        objectDict.get("river").setBottom(bounds().bottom);

        objectDict.get("frog_sit").setProperty("flipped",false);
        objectDict.get("frog_sit").hide();
        objectDict.get("frog_sit").setZPosition(2);
        OBGroup frogJump = (OBGroup)objectDict.get("frog_jump");
        frogJump.show();
        OBGroup groupFrog = new OBGroup(Collections.singletonList((OBControl)frogJump));
        attachControl(groupFrog);
        groupFrog.setZPosition ( 2);
        groupFrog.hide();
        groupFrog.objectDict.put("frog",frogJump);
        objectDict.put("frog_jump",groupFrog);
        frogPosition = OB_Maths.relativePointInRectForLocation(objectDict.get("frog_sit").position(), objectDict.get("rock1") .frame());
        currentJump =1;
        hiliteNum =0;
        OBMisc.loadNumbersFrom(1,8,Color.BLACK,"numrect","cnum_",this);

        PointF numPos = new PointF(objectDict.get("rock3").position().x,OB_Maths.locationForRect(0.5f,0.45f,this.bounds()).y);
        objectDict.get("numberline").setPosition(numPos);
        X_Numberlines_Additions.drawNumberLine(objectDict.get("numberline"),5,1,
                OBUtils.colorFromRGBString(eventAttributes.get("maincolour")),
                OBUtils.colorFromRGBString(eventAttributes.get("divcolour")),numColour,this);

        hideControls("num_.*");
        hideControls("divline_.*");
        objectDict.get("mainline").hide();

        for(OBControl con : filterControls("rock.*"))
        {
            con.setZPosition(1);
        }

        for(int i=0; i<5; i++)
        {
            loadLine(i,true);
            loadLine(i,false);
            OBLabel numCont = (OBLabel)objectDict.get(String.format("num_%d", i+1));
            numCont.setProperty("startpos",XPRZ_Generic.copyPoint(numCont.position()));

            OBControl rock = objectDict.get(String.format("rock%d",i+1));
            numCont.setPosition ( OB_Maths.locationForRect(0.5f,0.7f,rock.frame()));
            rock.setProperty("num_value",i+1);
        }
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {

        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3a();
            }
        });

    }

    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(eventAttributes.get("target").equals("divline")
                && eventAttributes.get("startnum") != null)
        {
            int startNum = Integer.valueOf(eventAttributes.get("startnum"));

            if(startNum > 1)
            {
                for(int i=0; i<5; i++)
                {
                    OBLabel numCont = (OBLabel)objectDict.get(String.format("num_%d", i+1));
                    numCont.setString ( String.format("%d", startNum+i));
                }

            }
        }
        if(eventAttributes.get("hilitenum") != null)
        {
            hiliteNum = Integer.valueOf(eventAttributes.get("hilitenum"));
            currentJump = hiliteNum;
        }

        targetNum = Integer.valueOf(eventAttributes.get("num"));

    }

    @Override
    public void doMainXX() throws Exception
    {
        startScene();
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("frog"))
        {

            OBControl cont = finger(0,1,Collections.singletonList(objectDict.get("frog_sit")),pt);
            if(cont != null )
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkFrog();
                    }
                });

            }
        }else if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("cnum"))
        {
            final OBControl cont = finger(0,1,filterControls("cnum_.*"),pt);

            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                playAudio(null);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkCnum((OBLabel)cont);
                    }
                });
            }

        }else if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("rock"))
        {
            final OBControl cont = finger(0,1,filterControls("rock.*"),pt);

            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                playAudio(null);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkRockNum(cont);
                    }
                });
            }
        }else if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("divline"))
        {
            OBControl tempCont = finger(0,2,filterControls("divline_.*") ,pt);

            if(tempCont == null)
                tempCont = finger(0,1,filterControls("num_.*") ,pt);

            final OBControl cont = tempCont;


            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                playAudio(null);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDivLine(cont);
                    }
                });
            }
        } else if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("num"))
        {
            final OBControl cont = finger(0,1,filterControls("num_.*"),pt);

            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                playAudio(null);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkNum((OBLabel)cont);
                    }
                });
            }
        }


    }

    private void checkNum(OBLabel cont) throws Exception
    {
        cont.setColour(hiliteColour);
        if((int)cont.settings.get("num_value")  == targetNum)
        {
            gotItRightBigTick(true);
            if(!performSel("demoFin", currentEvent()))
                waitForSecs(1f);

            if(currentEvent() != events.get(events.size()-1))
                X_Numberlines_Additions.resetNumberLine(true, OBUtils.getBooleanValue(eventAttributes.get("reset")),this);

            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();

            if((int)cont.settings.get("num_value")  == hiliteNum)
                cont.setColour(Color.RED);
            else
                cont.setColour(numColour);

            playAudioQueuedScene("INCORRECT",300,false);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    private void checkDivLine(OBControl cont) throws Exception
    {
        if((int)cont.settings.get("num_value")  == currentJump)
        {
            if(currentJump == hiliteNum)
                ((OBLabel)objectDict.get(String.format("num_%d",hiliteNum))).setColour(Color.RED);

            X_Numberlines_Additions.animateCurve(currentJump,currentJump<targetNum,0.65f,true,false,this);

            if(currentJump<targetNum)
            {
                currentJump++;
            }
            else
            {
                currentJump--;
            }

            if(currentJump == targetNum)
            {
                waitForSecs(0.3f);
                gotItRightBigTick(true);
                if(!performSel("demoFin", currentEvent()))
                    waitForSecs(1f);
                X_Numberlines_Additions.resetNumberLine(true,OBUtils.getBooleanValue(eventAttributes.get("reset")),this);
                waitForSecs(0.5f);
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

            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if(time == statusTime)
            {
                if(currentJump > 1)
                {
                    String audioName = String.format("INCORRECT%d", currentJump);
                    List<String> audios = getAudioForScene(currentEvent(),audioName);
                    if(audios != null)
                    {
                        playAudioQueued(OBUtils.insertAudioInterval(audios,300),true);
                    }
                    else
                    {
                        playAudioQueuedScene("INCORRECT",300,false);
                    }
                }
                else
                {
                    playAudioQueuedScene("INCORRECT",300,false);
                }
            }
        }
    }

    private void checkRockNum(OBControl cont) throws Exception
    {
        OBLabel numControl = (OBLabel)objectDict.get(String.format("num_%d",cont.settings.get("num_value")));
        numControl.setColour(Color.RED);
        if((int)cont.settings.get("num_value")  == targetNum)
        {
            gotItRightBigTick(true);
            performSel("demoFin", currentEvent());
            resetScene(numControl);
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            numControl.setColour ( numColour);
            playAudioQueuedScene("INCORRECT",300,false);
            animateFrogIdle(setStatus(STATUS_AWAITING_CLICK));
        }
    }

    private void checkCnum(OBLabel cont) throws Exception
    {
        cont.setColour(Color.RED);
        if((int)cont.settings.get("num_value")  == targetNum)
        {
            playAudio(null);
            gotItRightBigTick(true);
            performSel("demoFin", currentEvent());
            resetScene(cont);
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            cont.setColour ( numColour);
            playAudioQueuedScene("INCORRECT",300,false);
            animateFrogIdle(setStatus(STATUS_AWAITING_CLICK));
        }
    }

    private void checkFrog() throws Exception
    {
        playAudio(null);
        animateJump(currentJump,currentJump<targetNum);
        if(currentJump<targetNum)
        {
            currentJump++;
        }
        else
        {
            currentJump--;
        }

        if(currentJump == targetNum)
        {
            //Kids can't get it wrong, so display tick doesn't indicate correct for OBFat
            if(targetNum == 1 || targetNum == 5)
                displayTick();
            else
                waitForSecs(0.5f);

            performSel("demoFin",currentEvent());
            if(OBUtils.getBooleanValue(eventAttributes.get("reset")))
            {
                resetScene(null);
                waitForSecs(0.5f);
            }
            nextScene();
        }
        else
        {
            List<String> audios = getAudioForScene(currentEvent(),"REPEAT2");

            if(audios != null)
                setReplayAudio(OBUtils.insertAudioInterval(audios, 300));

            animateFrogIdle(setStatus(STATUS_AWAITING_CLICK));
        }
    }


    public void startScene() throws Exception
    {
        long time = setStatus(STATUS_AWAITING_CLICK);
        OBMisc.doSceneAudio(4,time,this);

        if(eventAttributes.get("target").equals("frog") ||
                eventAttributes.get("target").equals("rock") ||
                eventAttributes.get("target").equals("cnum"))
        {
            animateFrogIdle(time);
        }
    }

    public void resetScene(OBLabel label)
    {
        lockScreen();
        hideControls("line_.*");
        hideControls("cnum_.*");
        for(OBControl con : filterControls("rock.*"))
        {
            con.lowlight();
        }
        if(label != null)
            label.setColour ( numColour);
        unlockScreen();
    }

    public void hiliteRock(OBControl rock)
    {
        rock.setHighlightColour(Color.argb(180,29,132,219));
    }

    public void unhiliteRock(OBGroup rock)
    {
        rock.lowlight();
    }


    public void loadLine(int num,boolean clockwise)
    {
        int rocknum = num;
        if(rocknum == 0)
            rocknum = 1;
        PointF position1 = OB_Maths.locationForRect(0.5f,0.01f,objectDict.get(String.format("rock%d", rocknum )).frame());
        PointF position2 = OB_Maths.locationForRect(0.5f,0.01f,objectDict.get(String.format("rock%d", rocknum+1 )).frame());

        OBPath curveLine = new OBPath();
        Path path = new Path();
        float radius = Math.abs(position1.x -position2.x)/2.0f;

        path.addArc(new RectF(position1.x,position1.y-radius,position2.x,position1.y+radius),clockwise ? 180 : 0, clockwise ? 180 : -180);
        curveLine.setPath(path);
        curveLine.sizeToBoundingBox();
        if(num == 0)
            curveLine.setRight ( curveLine.right()-curveLine.width());
        curveLine.setLineWidth((float)Math.ceil(applyGraphicScale(6)));
        curveLine.setStrokeColor( clockwise ? OBUtils.colorFromRGBString("255,252,0") : OBUtils.colorFromRGBString("255,83,0"));
        curveLine.setZPosition(0.8f);
        curveLine.hide();
        attachControl(curveLine);
        curveLine.sizeToBoundingBoxIncludingStroke();

        objectDict.put(String.format("line_%d_%d",clockwise ? 1 : 2, num),curveLine);
    }

    public void flipFrog(boolean flipped)
    {
        OBGroup frogJump = (OBGroup)objectDict.get("frog_jump");
        OBControl frogSit = objectDict.get("frog_sit");
        if(flipped != (boolean)frogSit.propertyValue("flipped"))
        {
            frogSit.setProperty("flipped",flipped);
            frogJump.objectDict.get("frog").flipHoriz();
            frogSit.flipHoriz();
        }
    }

    public void animateJump(int num,boolean clockwise) throws Exception
    {
        final OBPath curveLine =(OBPath)objectDict.get(String.format("line_%d_%d",clockwise ? 1 : 2, clockwise ? num : num-1));
        curveLine.setStrokeEnd(0);

        final OBControl frogJump = objectDict.get("frog_jump");
        OBControl frogSit = objectDict.get("frog_sit");

        flipFrog(!clockwise);

        List<OBAnim> arr = new ArrayList<>();

        frogJump.setPosition(curveLine.convertPointToControl(curveLine.firstPoint(), null));
        frogJump.setRotation(clockwise ? (float)Math.toRadians(30) : (float)Math.toRadians(-30));
        curveLine.setStrokeEnd(0);

        arr.add(new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                frogJump.setPosition(curveLine.convertPointToControl(curveLine.sAlongPath(frac,null),null));
            }
        });
        arr.add(OBAnim.rotationAnim((clockwise ? (float)Math.toRadians(120): (float)Math.toRadians(-120)),frogJump));

        if(!(num == 0 && clockwise) && !(num ==1 && !clockwise))
            arr.add(OBAnim.propertyAnim( "strokeEnd",1,curveLine));

        playSfxAudio("frog_croak",false);
        lockScreen();
        curveLine.show();
        frogSit.hide();
        frogJump.show();
        unlockScreen();
        OBAnimationGroup.runAnims(arr,0.35,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        lockScreen();
        if(!(num ==1 && !clockwise))
        {
            frogSit.setPosition(OB_Maths.locationForRect(frogPosition.x, frogPosition.y,objectDict.get(String.format("rock%d", clockwise ? num+1 : num-1)).frame()));
            frogSit.show();
        }

        frogJump.hide();
        unlockScreen();
    }

    public void animateFrogIdle(long time) throws Exception
    {
        if(time == statusTime && !frogIsAnimated)
        {
            frogIsAnimated = true;
            if(OB_Maths.randomInt(0, 1) == 0)
            {
                animateFrames(Arrays.asList("eyes1", "eyes2", "eyes1"), 0.1f,(OBGroup)objectDict.get("frog_sit"));
            }
            else
            {
                animateFrames(Arrays.asList("side", "front"),0.1f,(OBGroup)objectDict.get("frog_sit"));
                animateFrames(Arrays.asList("mouth2", "mouth1"),0.1f,(OBGroup)objectDict.get("frog_sit"));
                animateFrames(Arrays.asList("front", "side"),0.1f,(OBGroup)objectDict.get("frog_sit"));
            }
            frogIsAnimated = false;
            if(time == statusTime)
            {
                startFrogIdle(time);
            }
        } else if(time == statusTime && frogIsAnimated)
        {
            startFrogIdle(time);
        }
    }

    public void startFrogIdle(final long time)
    {
        OBUtils.runOnOtherThreadDelayed(OB_Maths.randomInt(1, 4), new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                animateFrogIdle(time);
            }
        });
    }

    public void resetFrog()
    {
        OBGroup frog = (OBGroup)objectDict.get("frog_sit");
        frog.objectDict.get("eyes2").hide();
        frog.objectDict.get("eyes1").show();
        frog.objectDict.get("front").hide();
        frog.objectDict.get("mouth1").show();
        frog.objectDict.get("mouth2").hide();
        frog.objectDict.get("side").show();
    }

    public void showNumbers() throws Exception
    {
        for(int i=1; i<=5; i++)
        {
            playSfxAudio(String.format("note%d",i),false);
            objectDict.get(String.format("num_%d",i)).show();
            waitForSecs(0.3f);
        }
        waitSFX();
    }


    public void demo3a() throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);

        moveScenePointer(OB_Maths.locationForRect(0.5f,0.7f,objectDict.get("river").frame()),-45,0.4f,"DEMO",0,0.3f);
        animateJump(0,true);
        waitForSecs(0.3f);

        moveScenePointer(OB_Maths.locationForRect(0.7f,0.9f,objectDict.get("frog_sit").frame()),-35,0.4f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo3c()  throws Exception
    {
        loadPointer(POINTER_LEFT);
        final OBPath curve = (OBPath)objectDict.get("line_1_1").copy();
        curve.setStrokeEnd(0.8f);
        movePointerToPoint(curve.convertPointToControl(curve.firstPoint(), null),-30,0.5f,true);
        playAudioScene("DEMO",0,false);

        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                thePointer.setPosition(curve.convertPointToControl(curve.sAlongPath(frac*0.85f,null), null));
            }
        };
        OBAnimationGroup.runAnims(Collections.singletonList(anim), 1.5f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, null, this);

        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }


    public void demo3e() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(objectDict.get("rock5").position(),-15,0.5f,true);
        hiliteRock(objectDict.get("rock5"));
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo3f() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(objectDict.get("rock1").position(),-40,0.5f,true);
        hiliteRock(objectDict.get("rock1"));
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        playAudioScene("DEMO",1,false);
        movePointerToPoint(objectDict.get("rock5").position(),-15,1f,true);
        waitAudio();
        waitForSecs(0.5f);

        lockScreen();
        showControls("cnum_.*");
        thePointer.hide();
        unlockScreen();

        startScene();
    }

    public void pointCurveAndHilite(int num,int audioIndex, float duration, boolean clockwise) throws Exception
    {
        OBPath curve = (OBPath)objectDict.get(String.format("line_%d_%d", clockwise ? 1:2, num));

        PointF point = curve.convertPointToControl(curve.sAlongPath(0.5f,null), null);
        point.y += objectDict.get("numberline").height()/10.0f;

        movePointerToPoint(point,-30+(num*5),duration,true);
        curve.setZPosition(0.81f);
        int oldCol = curve.strokeColor();
        curve.setStrokeColor(OBUtils.highlightedColour(curve.strokeColor()));
        playAudioScene("FINAL",audioIndex,true);
        waitForSecs(0.3f);
        curve.setStrokeColor(oldCol);
    }

    public void demoFin3f() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        playAudioScene("FINAL",1,true);
        waitForSecs(0.3f);

        for(int i=1; i<5; i++)
        {
            pointCurveAndHilite(i, i+1,i==1 ? 0.5f : 0.3f, true);
        }
        thePointer.hide();
        playAudioScene("FINAL",6,true);
        waitForSecs(1f);

    }

    public void demoFin3g() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
    }

    public void demo3h() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(objectDict.get("rock1").position(),-30,0.5f,true);
        hiliteRock(objectDict.get("rock1"));
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo3i() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(objectDict.get("rock4").position(),-20,0.5f,true);
        hiliteRock(objectDict.get("rock4"));
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        playAudioScene("DEMO",1,false);
        movePointerToPoint(objectDict.get("rock1").position(),-35,1f,true);
        waitAudio();
        waitForSecs(0.5f);

        lockScreen();
        showControls("cnum_.*");
        thePointer.hide();
        unlockScreen();

        startScene();
    }

    public void demoFin3i() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);

        for(int i=1; i<4; i++)
        {
            pointCurveAndHilite(4-i, i, i==1 ? 0.5f : 0.3f, false);
        }
        thePointer.hide();
        playAudioScene("FINAL",4,true);
        waitForSecs(1f);
    }


    public void demo3j() throws Exception
    {
        flipFrog(false);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showNumbers();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.9f,objectDict.get("frog_sit").frame()),-35,0.4f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3j() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        animateJump(1,true);
        waitForSecs(1f);
    }

    public void demo3k() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.9f,objectDict.get("frog_sit").frame()),-35,0.4f,"DEMO",0,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3k() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        for(int i=2; i<5; i++)
        {
            animateJump(i, true);
            waitForSecs(0.2f);
        }
        waitForSecs(1f);
    }

    public void demo3l() throws Exception
    {
        flipFrog(true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.9f,objectDict.get("rock5").frame()),-10,0.4f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3l() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        for(int i=5; i>1; i--)
        {
            animateJump(i,false);
            waitForSecs(0.2f);
        }
        waitForSecs(1f);
    }

    public void pointerTouchDiv(int num,boolean hilite, boolean clockwise, int audioIndex) throws Exception
    {
        PointF clickLoc = OB_Maths.locationForRect(0.5f,0.8f,objectDict.get(String.format("divline_%d",num)).frame());
        PointF prevLoc = OB_Maths.locationForRect(0.5f,1f,objectDict.get(String.format("divline_%d",num)).frame());

        prevLoc.x = prevLoc.x+objectDict.get("numberline").width()/18.0f;

        movePointerToPoint(prevLoc,-40+(num*5),0.5f,true);
        if(audioIndex>=0)
            playAudioScene("DEMO",audioIndex,true);

        waitForSecs(0.3f);
        movePointerToPoint(clickLoc,-40+(num*5),0.2f,true);

        if(hilite)
            ((OBLabel)objectDict.get(String.format("num_%d",num))).setColour(Color.RED);

        playSfxAudio("div_line",true);
        X_Numberlines_Additions.animateCurve(num,clockwise,0.65f,true,false,this);
        waitForSecs(0.1f);
    }

    public void demo3m() throws Exception
    {
        animateJump(1,false);
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);

        List<OBAnim> anim1 = new ArrayList<>();

        for(OBControl con : filterControls("num_.*"))
        {
            anim1.add(OBAnim.moveAnim((PointF)con.propertyValue("startpos") ,con));
        }
        anim1.add(OBAnim.propertyAnim("top",bounds().height(),objectDict.get("river")));
        anim1.add(OBMisc.attachedAnim(objectDict.get("river"),filterControls("rock.*")));
        playSfxAudio("cross_fade",false);
        OBAnimationGroup.runAnims(anim1,1.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.3f);
        X_Numberlines_Additions.animateNumberLineShow(1,this);
        waitForSecs(0.3f);

        loadPointer(POINTER_LEFT);

        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("numberline").frame()),-30,0.4f,"DEMO",1,0.3f);
        pointerTouchDiv(1,true,true,2);

        moveScenePointer(OB_Maths.locationForRect(0.1f,0.6f,objectDict.get("numberline").frame()),-40,0.4f,"DEMO",3,0.3f);
        pointerTouchDiv(2,false,true,4);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        X_Numberlines_Additions.resetNumberLine(false,false,this);
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo3o() throws Exception
    {
        showNumbers();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("numberline").frame()),-30,0.4f,"DEMO",0,0.5f);

        thePointer.hide();
        ((OBLabel)objectDict.get("num_1")).setColour(Color.RED);

        startScene();
    }

    public void demo3p() throws Exception
    {
        showNumbers();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("numberline").frame()),-30,0.4f,"DEMO",0,0.3f);
        pointerTouchDiv(5,true,false,1);
        pointerTouchDiv(4,false,false,-1);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        X_Numberlines_Additions.resetNumberLine(true,true,this);
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo3q() throws Exception
    {
        showNumbers();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("numberline").frame()),-30,0.5f,"DEMO",0,0.3f);

        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("num_5").frame()),-45,0.4f,true);
        ((OBLabel)objectDict.get("num_5")).setColour(Color.RED);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo3r() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("num_4").frame()),-40,0.4f,true);
        ((OBLabel)objectDict.get("num_4")).setColour(Color.RED);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3r() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        X_Numberlines_Additions.animateCurve(4,false,0.65f,true,false,this);
        waitForSecs(0.2f);
        X_Numberlines_Additions.animateCurve(3,false,0.65f,true,false,this);
        waitForSecs(1f);
    }

    public void demo3s() throws Exception
    {

        showNumbers();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("numberline").frame()),-30,0.5f,"DEMO",0,0.3f);

        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("num_5").frame()),-15,0.4f,true);
        ((OBLabel)objectDict.get("num_5")).setColour(Color.RED);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo3t() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("num_4").frame()),-20,0.4f,true);
        ((OBLabel)objectDict.get("num_4")).setColour(Color.RED);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3t() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        X_Numberlines_Additions.animateCurve(4,false,0.65f,true,false,this);
        waitForSecs(0.2f);
        X_Numberlines_Additions.animateCurve(3,false,0.65f,true,false,this);
        waitForSecs(0.2f);
        X_Numberlines_Additions.animateCurve(2,false,0.65f,true,false,this);
        waitForSecs(1f);
    }
}
