package org.onebillion.onecourse.mainui.oc_practicebignum;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_countwhatyousee.OC_Cwys_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBUtils.*;

/**
 * Created by michal on 06/06/2018.
 */

public class OC_Pbn extends OC_SectionController
{
    boolean feedbackMode, playOnlyWrongDemo;
    List<Map<String,Object>> eventData;
    List<OBControl> targetLabels;
    OBControl unit;
    int highlightColour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventData = new ArrayList<>();
        events = new ArrayList<>();
        feedbackMode= OBUtils.getBooleanValue(parameters.get("feedback"));
        highlightColour = colorFromRGBString(eventAttributes.get("highlight_colour"));
        int index = 1;
        if(parameters.containsKey("phase1"))
        {
            for(String nums : parameters.get("phase1").split(";"))
            {
                eventData.add(eventDataForNumbers(nums.split(","),false));
                events.add(eventNameForPrefix("p1_",index));
                index++;
            }
        }
        index = 1;
        if(parameters.containsKey("phase2"))
        {
            for(String nums : parameters.get("phase2").split(";"))
            {
                eventData.add(eventDataForNumbers(nums.split(","),true));
                events.add(eventNameForPrefix("p2_",index));
                index++;
            }
        }
        int maxNum = 1;
        for(Map<String,Object> dict : eventData)
        {
            for(int num :(List<Integer>)dict.get("targets"))
                if(maxNum < num)
                    maxNum = num;
        }
        if(maxNum <= 25)
            unit = objectDict.get("unit_1");
        else if(maxNum <= 100)
            unit = objectDict.get("unit_10");
        else
            unit = objectDict.get("unit_100");
        setSceneXX(currentEvent());
        for(OBControl con : targetLabels)
            con.show();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        if(targetLabels != null)
        {
            for(OBControl con : targetLabels)
            {
                detachControl(con);
                if(con.propertyValue("block_group") != null)
                    detachControl((OBControl) con.propertyValue("block_group"));

            }

        }
        targetLabels = new ArrayList<>();
        Map<String,Object> data = eventData.get(eventIndex);
        playOnlyWrongDemo = (boolean)data.get("wrong_only_demo");
        List<Integer> nums = (List<Integer>)data.get("targets");
        int correct = -1;
        for(int num : nums)
        {
            if(correct < num)
            correct = num;
        }

        for(int i=0; i<2; i++)
        {
            int currentMode = nums.get(i).intValue();
            loadNum(currentMode,String.format("num_%d",i+1),feedbackMode,correct==currentMode);
        }

    }

    public void doMainXX() throws Exception
    {
        slideNumbersIn();
        startScene(true);
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,targetLabels,pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        checkLabel((OBLabel) targ);
                    }
                } );
            }
        }
    }

    public void checkLabel(OBLabel label) throws Exception
    {
        label.setColour(Color.RED);
        boolean correct = (boolean)label.propertyValue("correct");
        if(correct)
        {
            gotItRightBigTick(true);

        }
        else
        {
            gotItWrongWithSfx();

        }
        waitForSecs(0.3f);
        label.setColour(Color.BLACK);
        playAudioQueuedScene(correct ? "CORRECT" : "INCORRECT",0.3f,true);
        waitForSecs(0.3f);
        if(!playOnlyWrongDemo || !correct)
            demoCount();
        waitForSecs(0.3f);
        clearScene();
        waitForSecs(0.1f);
        nextScene();
    }

    public void startScene(boolean withDemoAudio) throws Exception
    {
        if(withDemoAudio)
        {
            playAudioQueuedScene("DEMO",0.3f,true);
            waitForSecs(0.3f);

        }
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK), this);
    }

    public Map<String,Object> eventDataForNumbers(String[] nums, boolean wrongOnlyDemo)
    {
        Map<String,Object> dict = new ArrayMap();
        List<Integer> intNums = new ArrayList<>();
        for(String val : nums)
            intNums.add(Integer.valueOf(val));
        dict.put("targets",intNums);
        dict.put("wrong_only_demo",wrongOnlyDemo);
        return dict;
    }

    public String eventNameForPrefix(String prefix,int num)
    {
        String eventName = null;
        if(audioScenes.get(eventName) == null)
        {
            eventName = String.format("%s%d",prefix,num);

        }
        if(audioScenes.get(eventName) != null)
        {
            return eventName;

        }
        return String.format("%sdefault",prefix);
    }

    public void loadNum(int num,String box,boolean feedback,boolean correct)
    {
        OBControl boxControl = objectDict.get(box);
        OBLabel label = new OBLabel(String.format("%d",num),OBUtils.StandardReadingFontOfSize(150));
        label.setColour(Color.BLACK);
        label.setProperty("num_val",num);
        label.setProperty("correct",correct);
        attachControl(label);
        if(feedback)
        {
            label.setPosition(boxControl.position());
            OBGroup unitBlocksGroup = OC_Cwys_Additions.loadUnitsBlockForNumber(num,unit,unit.width()*0.5f,boxControl.position().x,objectDict.get("work_rect").bottom(), this);
            label.setProperty("block_group",unitBlocksGroup);
        }
        else
        {
            PointF loc = OBMisc.copyPoint(boxControl.position());
            loc.y = OB_Maths.locationForRect(0.5f,0.5f,this.bounds()).y;
            label.setPosition(loc);
        }
        label.hide();
        targetLabels.add(label);
    }

    public void clearScene() throws Exception
    {
        if(currentEvent() == events.get(events.size()-1))
            return;
        lockScreen();
        for(OBControl con : targetLabels)
        {
            if(con.propertyValue("block_group") != null)
            {
                OBGroup blockGroup = (OBGroup)con.propertyValue("block_group");
                blockGroup.hide();

            }
            con.hide();

        }
        playSfxAudio("alloff",false);

        unlockScreen();
        waitSFX();
    }

    public void slideNumbersIn() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl label : targetLabels)
        {
            anims.add(OBAnim.moveAnim(label.position(),label));
            label.setLeft(label.left() + this.bounds().width());
            label.show();
        }
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_OUT,this);
        waitSFX();
    }

public void higlightAndSayLabel(OBLabel label) throws Exception
{
    int currentNum = (int)label.propertyValue("num_val");
    if(currentNum == 0)        return;
    label.setColour(Color.RED);
    playAudio(String.format("n_%d",currentNum));
    waitAudio();
    waitForSecs(0.3f);
    label.setColour(Color.BLACK);
    waitForSecs(0.3f);

}
    public void countUnitsInGroups(OBLabel label) throws Exception
    {
        higlightAndSayLabel(label);
        OBGroup blockGroup =(OBGroup)label.propertyValue("block_group");
        if(feedbackMode)
        {
            demoCountUnitsInGroups(blockGroup,label);
        }
        else
        {
            String string = label.text();
            for(int i=0; i<label.text().length(); i++)
            {
                String num = string.substring(i, i+1);
                int val = Integer.valueOf(num);
                label.setHighRange(i,i+1,Color.RED);
                int place = (int)Math.pow(10,(label.text().length()-i-1));
                playAudio(String.format("plc%d_%d",place,val));
                waitAudio();
                waitForSecs(0.3f);
                label.setColour(Color.BLACK);
                waitForSecs(0.3f);
            }
        }
    }

    public void demoCount() throws Exception
    {
        List<String> audio = getAudioForScene(currentEvent(),"FINAL");
        if(audio != null && audio.size() > 1)
        {
            playAudio(audio.get(0));
            waitAudio();
        }
        waitForSecs(0.3f);
        OBLabel correctLabel = null;
        for(OBControl con : targetLabels)
        {
            OBLabel label = (OBLabel)con;
            countUnitsInGroups(label);
            if((boolean)label.propertyValue("correct"))
                correctLabel = label;
            waitForSecs(0.3f);

        }
        if(audio != null)
        {
            playAudio(audio.get(audio.size()-1));

        }
        for(int i=0; i<3; i++)
        {
            correctLabel.setColour(highlightColour);
            waitForSecs(0.3f);
            correctLabel.setColour(Color.BLACK);
            waitForSecs(0.3f);
        }
        correctLabel.setColour(highlightColour);
        waitAudio();
        waitForSecs(2f);
    }

    public void demo() throws Exception
    {
        if(OBUtils.getBooleanValue(parameters.get("presenter")))
        {
            OBGroup presenterGroup =(OBGroup ) objectDict.get("presenter");
            PointF startLoc = OBMisc.copyPoint(presenterGroup.position());
            OBPresenter presenter = OBPresenter.characterWithGroup(presenterGroup);
            presenter.control.setZPosition(200);
            presenter.control.setPosition(startLoc);
            presenter.control.setRight(0);
            PointF loc = OBMisc.copyPoint(presenter.control.position());
            presenter.control.show();
            loc.x = 0.5f*this.bounds().width();
            presenter.walk(loc);
            presenter.faceFront();
            waitForSecs(0.3f);
            presenter.speak(Arrays.asList((Object)getAudioForScene(currentEvent(),"DEMO").get(0)),this);
            waitForSecs(0.3f);
            loc.x = 0.87f*this.bounds().width();
            presenter.walk(loc);
            presenter.faceFront();
            presenter.speak(Arrays.asList((Object)getAudioForScene(currentEvent(),"DEMO").get(1)),this);
            waitForSecs(0.3f);
            loc.x = 1.2f*this.bounds().width();
            presenter.walk(loc);
            presenter.control.hide();
            waitForSecs(0.4f);
        }
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-30,0.5f,"DEMO2",0,0.3f);
        playAudioScene("DEMO2",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene(false);
    }

    public void demoCountUnitsInGroups(OBGroup blockGroup,OBLabel label) throws Exception
    {
        List<OBGroup> hundredBlocks = OC_Cwys_Additions.getHundredBlocksFromBlockGroup(blockGroup);
        List<OBGroup> tenColumns = OC_Cwys_Additions.getTenColumnsFromBlockGroup(blockGroup);
        List<OBControl> units = OC_Cwys_Additions.getUnitsFromBlockGroup(blockGroup);
        if(hundredBlocks.size() != 0)
        {
            lockScreen();

            unlockScreen();
            for(OBGroup block : hundredBlocks)
            {
                lockScreen();
                OC_Cwys_Additions.showUnitsForBlock(block);
                playSfxAudio("hundred",false);
                unlockScreen();
                waitSFX();
            }
            waitForSecs(0.3f);
            lockScreen();
            label.setHighRange(0,1,Color.RED);
            for(OBGroup block : hundredBlocks)
                OC_Cwys_Additions.highlightBlock(block,true);

            unlockScreen();
            playAudio(String.format("plc100_%d",hundredBlocks.size()));
            waitAudio();
            waitForSecs(0.3f);
            lockScreen();
            label.setColour(Color.BLACK);
            for(OBGroup block : hundredBlocks)
                OC_Cwys_Additions.highlightBlock(block,false);

            unlockScreen();
            waitForSecs(0.3f);
        }
        int hiIndex = label.text().length() > 2 ? 1 : 0;
        if(tenColumns.size() != 0)
        {
            for(OBGroup column : tenColumns)
            {
                lockScreen();
                OC_Cwys_Additions.showUnitsForColumn(column);
                playSfxAudio("ten",false);
                unlockScreen();
                waitSFX();
            }
            waitForSecs(0.3f);
            lockScreen();
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            for(OBGroup column : tenColumns)
                OC_Cwys_Additions.highlightColumn(column,true);

            unlockScreen();
            playAudio(String.format("plc10_%d",tenColumns.size()));
            waitAudio();
            waitForSecs(0.3f);
            lockScreen();
            label.setColour(Color.BLACK);
            for(OBGroup column : tenColumns)
                OC_Cwys_Additions.highlightColumn(column,false);

            unlockScreen();
            waitForSecs(0.3f);

        }
        else if(hundredBlocks.size() != 0)
        {
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            playAudio("plc10_0");
            waitAudio();
            waitForSecs(0.3f);
            label.setColour(Color.BLACK);
            waitForSecs(0.3f);

        }
        hiIndex = label.text().length()-1;
        if(units.size() != 0)
        {
            for(OBControl unit : units)
            {
                lockScreen();
                unit.show();
                playSfxAudio("unit",false);

                unlockScreen();
                waitSFX();
            }
            waitForSecs(0.3f);
            lockScreen();
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            for(OBControl unit : units)
                OC_Cwys_Additions.highlightUnit(unit,true);

            unlockScreen();
            playAudio(String.format("plc1_%d",units.size()));
            waitAudio();
            waitForSecs(0.3f);
            lockScreen();
            label.setColour(Color.BLACK);
            for(OBControl unit : units)
                OC_Cwys_Additions.highlightUnit(unit,false);

            unlockScreen();
            waitForSecs(0.3f);

        }
        else
        {
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            playAudio("plc1_0");
            waitAudio();
            waitForSecs(0.3f);
            label.setColour(Color.BLACK);
            waitForSecs(0.3f);

        }

    }


}
