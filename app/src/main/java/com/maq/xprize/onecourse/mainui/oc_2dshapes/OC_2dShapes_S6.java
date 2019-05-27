package com.maq.xprize.onecourse.mainui.oc_2dshapes;

import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;
import com.maq.xprize.onecourse.utils.OB_MutInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 07/03/2018.
 */

public class OC_2dShapes_S6 extends OC_SectionController
{
    Map<String,OBControl> dropObjs,dragObjs;
    List<List<OBControl>> gridObjs;
    int currentRow;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        gridObjs = new ArrayList<>();
        dropObjs = new ArrayMap<>();
        dragObjs = new ArrayMap<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo6a();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        boolean realign = false;
        if(OBUtils.getBooleanValue(eventAttributes.get("delete")) || scene == events.get(0))
        {
            realign = true;
            deleteControls("obj_.*");
            deleteControls("frame_.*");
            deleteControls("drag_.*");
            for(OBControl con : dragObjs.values())
                detachControl(con);
            gridObjs.clear();
            dropObjs.clear();
            dragObjs.clear();
        }
        super.setSceneXX(scene);
        if(gridObjs.size() == 0)
        {
            int i = 1, j = 1;
            while(true)
            {
                OBControl cont = objectDict.get(String.format("obj_%d_%d", i,j));
                if(cont instanceof OBPath)
                    ((OBPath)cont).sizeToBoundingBoxIncludingStroke();
                if(cont == null)
                {
                    if(j == 1)
                    {
                        break;
                    }
                    else
                    {
                        i++;
                        j = 1;
                    }
                }
                else
                {
                    List<OBControl> arr = null;
                    if(gridObjs.size() < i)
                    {
                        arr = new ArrayList<>();
                        gridObjs.add(arr);
                    }
                    else
                    {
                        arr = gridObjs.get(i-1);
                    }
                    arr.add(cont);
                    cont.setProperty("row",i);
                    cont.setProperty("column",j);
                    j++;
                }
            }
        }

        if(realign || eventAttributes.get("drag") != null)
        {
            if(realign)
            {
                for(OBControl control : filterControls("drag_.*"))
                {
                    OBControl con = control.copy();
                    attachControl(con);
                    con.hide();
                    dragObjs.put((String)control.attributes().get("tag"),con);
                    con.setProperty("tag",control.attributes().get("tag"));
                    con.setProperty("org",control.attributes().get("id"));
                }
            }

            for(OBControl con : filterControls("drag_.*"))
                if(con instanceof OBPath)
                    ((OBPath)con).sizeToBoundingBoxIncludingStroke();

            for(OBControl con : filterControls("frame_.*"))
                if(con instanceof OBPath)
                    ((OBPath)con).sizeToBoundingBoxIncludingStroke();


            hideControls("drag_.*");
            if((realign && eventAttributes.get("drag") == null) || eventAttributes.get("drag").equals("all"))
            {
                setDragPosition("drag_1",0.25f);
                setDragPosition("drag_2",0.75f);
                showControls("drag_.*");
            }
            else
            {
                hideControls("drag_.*");
                String drag= String.format("drag_%s", eventAttributes.get("drag"));
                setDragPosition(drag,0.25f);
                objectDict.get(drag).show();
            }
        }

        if(eventAttributes.get("row") != null)
        {
            currentRow = OBUtils.getIntValue(eventAttributes.get("row"));
        }
        else
        {
            currentRow = -1;
        }

    }


    public void doMainXX() throws Exception
    {
        startScene();
    }


    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl targ = finger(0,1,filterControls("drag_.*"),pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBControl con = dragObjs.get(targ.attributes().get("tag"));
                con.setPosition(targ.position());
                OBMisc.prepareForDragging(con,pt,this);
                con.show();
                setStatus(STATUS_DRAGGING);
            }
        }
    }


    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target != null)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void touchUpAtPoint(PointF pt,View v)
    {

        if(status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            final OBControl targ = target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop(targ);
                }
            });


        }
    }

    public void checkDrop(OBControl targ) throws Exception
    {
        Boolean detected = Boolean.FALSE;
        OBControl cont = nearestHiddenControl(targ,detected);
        if(cont != null && canBeShown(cont))
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(cont.position(),targ))
                    ,0.1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            playSfxAudio("tile_settle",false);
            lockScreen();
            cont.show();
            targ.hide();

            unlockScreen();
            gotItRight();
            if(checkSceneComplete())
            {
                waitAudio();
                waitForSecs(0.3f);
                displayTick();
                waitForSecs(0.5f);
                performSel("demoFin",currentEvent());
                nextScene();
            }
            else
            {
                setStatus(STATUS_WAITING_FOR_DRAG);
            }
        }
        else
        {
            if(detected)
                gotItWrongWithSfx();
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(objectDict.get(targ.propertyValue("org")).position()
                    ,targ)) ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            targ.hide();
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }


    public boolean canBeShown(OBControl obj)
    {
        if(eventAttributes.get("mode") != null)
        {
            int mode = OBUtils.getIntValue(eventAttributes.get("mode"));
            int row = (int)obj.propertyValue("row");
            int column = (int)obj.propertyValue("column");
            if(mode == 1)
            {
                if(eventAttributes.get("row") != null && OBUtils.getIntValue(eventAttributes.get("row")) != row)
                    return false;
                if(controlVisibleRow(row,column-1)
                        ||controlVisibleRow(row-1,column)
                        ||controlVisibleRow(row+1,column)
                        ||controlVisibleRow(row,column+1))
                    return true;
                return false;

            }
            else if(mode == 2)
            {
                if(eventAttributes.get("row") != null && OBUtils.getIntValue(eventAttributes.get("row")) != row)                    return false;
                boolean tag1 =(OBUtils.getIntValue((String)obj.attributes().get("tag"))  == 1);
                for(int i=-1; i<2; i++)
                {
                    int start = -2, stop = 2;
                    if(i == -1)
                    {
                        if(tag1)
                        {
                            start =-2;
                            stop=0;
                        }
                        else
                        {
                            start=-3;
                            stop=1;
                        }
                    }
                    else if(i == 1)
                    {
                        if(tag1)
                        {
                            start =-1;
                            stop=3;
                        }
                        else
                        {
                            start=0;
                            stop=2;
                        }
                    }
                    for(int j=start; j<=stop; j++)
                    {
                        if(controlVisibleRow(row+i,column+j))
                            return true;
                    }
                }
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true;
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(0,setStatus(STATUS_WAITING_FOR_DRAG), this);
    }

    public void setDragPosition(String name,float y)
    {
        OBControl obj = objectDict.get(name);
        PointF loc = new PointF();
        loc.y = OB_Maths.locationForRect(0f,y,objectDict.get("frame_1").frame()).y;
        loc.x = OB_Maths.locationForRect(0.5f,0f,objectDict.get("panel").frame()).x;
        obj.setPosition(loc);
    }

    public OBControl nearestHiddenControl(OBControl targ, Boolean objDetected)
    {
        float maxDist = targ.width()*0.75f;
        float dist = this.bounds().width();
        OBControl returnCon = null;
        objDetected = Boolean.FALSE;
        for(int i=0; i<gridObjs.size(); i++)
        {
            for(int j=0; j<gridObjs.get(i).size(); j++)
            {
                OBControl con = gridObjs.get(i) .get(j);
                float curDist = OB_Maths.PointDistance(con.position(), targ.position());
                if(curDist <= maxDist && curDist < dist)
                {
                    objDetected = Boolean.TRUE;
                    if(con.hidden && con.attributes().get("tag").equals(targ.propertyValue("tag")))
                    {
                        dist = curDist;
                        returnCon = con;
                    }
                }
            }
        }
        return returnCon;
    }

    public boolean controlVisibleRow(int row,int col)
    {
        row--;
        col--;
        if(gridObjs.size() <=row || row < 0)
            return false;
        if(gridObjs.get(row).size() <= col || col < 0)
            return false;
        OBControl cont = gridObjs.get(row).get(col);
        return !cont.hidden;
    }

    public boolean checkSceneComplete()
    {
        if(eventAttributes.get("row") != null)
        {
            int mode = OBUtils.getIntValue(eventAttributes.get("row"));
            for(int i=0; i<gridObjs.get(mode-1).size(); i++)
            {
                if(gridObjs.get(mode-1).get(i).hidden())
                    return false;
            }
            return true;
        }
        else
        {
            for(int i=0; i<gridObjs.size(); i++)
            {
                for(int j=0; j<gridObjs.get(i).size(); j++)
                {
                    if(gridObjs.get(i).get(j).hidden())
                        return false;
                }
            }
            return true;
        }
    }

    public void demoDragRow(int row,int column,float x,float y) throws Exception
    {
        OBControl targ = gridObjs.get(row) .get(column);
        OBControl drag = objectDict.get(String.format("drag_%s",targ.attributes().get("tag")));
        OBControl dra = dragObjs.get(targ.attributes().get("tag"));
        movePointerToPoint(OB_Maths.locationForRect(x,y,drag.frame()),-10,0.7f,true);
        dra.setPosition(drag.position());
        dra.show();
        OBAnim attAnim = OBMisc.attachedAnim(dra,Arrays.asList(thePointer));
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(targ.position(),dra) ,attAnim,OBAnim.rotationAnim((float)Math.toRadians(-30) ,thePointer)),
                0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("tile_settle",false);
        lockScreen();
        targ.show();
        dra.hide();
        unlockScreen();
    }

    public void demoDragRow(int row,int column) throws Exception
    {
        demoDragRow(row,column,0.5f,0.75f);
    }

    public void demo6a() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(-0.1f,1.2f,objectDict.get("drag_2") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.7f,objectDict.get("frame_1") .frame()),-20,0.5f,"DEMO",1,0.3f);
        demoDragRow(0,0);
        demoDragRow(0,1);
        demoDragRow(0,2);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.2f,gridObjs.get(0).get(2).frame()),-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demo6c() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.5f,gridObjs.get(0).get(4).frame()),-20,0.5f,"DEMO",0,0.3f);
        demoDragRow(1,0);
        demoDragRow(1,1);
        demoDragRow(1,2);
        movePointerToPoint(OB_Maths.locationForRect(0f,0.1f,gridObjs.get(1).get(0).frame()),-30,0.7f,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.1f,gridObjs.get(1).get(2).frame()),-20,2,true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoFin6c() throws Exception
    {
        playAudioScene("FINAL",0,true);
        loadPointer(POINTER_MIDDLE);
        thePointer.setPosition(OB_Maths.locationForRect(0.1f,1.1f,this.bounds()));
        movePointerToPoint(OB_Maths.locationForRect(0.3f,0.3f,gridObjs.get(0).get(0).frame()),-35,0.7f,true);
        playAudioScene("FINAL",1,false);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,0.7f,gridObjs.get(3).get(5).frame()),-10,2,true);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demo6e() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(-0.1f,1.4f,objectDict.get("drag_2") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(-0.2f,1.1f,objectDict.get("drag_2") .frame()),-10,0.5f,"DEMO",1,0.3f);
        demoDragRow(0,0,0.25f,0.75f);
        demoDragRow(0,1,0.75f,0.25f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.15f,gridObjs.get(0).get(0).frame()),-30,0.5f,"DEMO",2,0.3f);
        demoDragRow(0,2,0.25f,0.75f);
        demoDragRow(0,3,0.75f,0.25f);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demo6g() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.7f,objectDict.get("frame_1") .frame()),-10,0.5f,"DEMO",0,0.3f);
        demoDragRow(1,0,0.25f,0.75f);
        demoDragRow(1,1,0.75f,0.25f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,gridObjs.get(1).get(0).frame()),-30,0.4f,true);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoFin6g() throws Exception
    {
        playAudioScene("FINAL",0,true);
        loadPointer(POINTER_MIDDLE);
        thePointer.setPosition(OB_Maths.locationForRect(0.1f,1.1f,this.bounds()));
        movePointerToPoint(OB_Maths.locationForRect(0f,0f,gridObjs.get(1).get(0).frame()),-35,0.7f,true);
        playAudioScene("FINAL",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,gridObjs.get(3).get(8).frame()),-10,2,true);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demo6i() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.5f,objectDict.get("drag_1") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.7f,objectDict.get("frame_1") .frame()),-20,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(-0.1f,1.2f,objectDict.get("drag_1") .frame()),-10,0.5f,"DEMO",2,0.3f);
        demoDragRow(0,0);
        demoDragRow(0,1);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demo6k() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.5f,objectDict.get("drag_2") .frame()),-10,0.5f,"DEMO",0,0.3f);
        demoDragRow(1,0);
        demoDragRow(1,1);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoFin6l() throws Exception
    {
        playAudioScene("FINAL",0,true);
        loadPointer(POINTER_MIDDLE);
        thePointer.setPosition(OB_Maths.locationForRect(0f,1.1f,this.bounds()));
        movePointerToPoint(OB_Maths.locationForRect(0.3f,0.3f,gridObjs.get(1).get(0).frame()),-35,0.7f,true);
        playAudioScene("FINAL",1,false);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,gridObjs.get(3).get(8).frame()),-10,2,true);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demo6n() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.2f,1.2f,objectDict.get("drag_2") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.2f,1.1f,gridObjs.get(1).get(1).frame()),-10,0.5f,"DEMO",1,0.3f);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoFin6n() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        thePointer.setPosition(OB_Maths.locationForRect(0.1f,1.1f,this.bounds()));
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,gridObjs.get(0).get(1).frame()),-30,0.7f,true);
        playAudioScene("FINAL",0,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,gridObjs.get(0).get(3).frame()),-30,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,gridObjs.get(1).get(1).frame()),-30,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,gridObjs.get(2).get(4).frame()),-30,0.5f,true);
        waitForSecs(0.5f);
        waitAudio();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.05f,objectDict.get("frame_1") .frame()),-10,0.5f,"FINAL",1,0.5f);
        thePointer.hide();
        waitForSecs(1f);
    }



}
