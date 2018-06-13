package org.onebillion.onecourse.mainui.oc_typing;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 12/06/2018.
 */

public class OC_Twrd1 extends OC_Twrd
{
    List<OBLabel> labels;
    OBLabel currentLabel;
    List<OBGroup> targetKeys;

    private Handler reminderHandler;
    private Runnable reminderRunnable;

    @Override
    public String sectionAudioName() {
        return "twrd1";
    }

    @Override
    public void prepare()
    {
        super.prepare();
        loadEvent("twrd1");
        events = Arrays.asList("1");
        OBControl box = objectDict.get("word_box");

        OBFont font = new OBFont(OBUtils.standardTypeFace(), 100.0f * box.height()/350.0f);
        reminderHandler = new Handler();
        labels = new ArrayList<>();
        labels.add(setupLabelforLine((OBPath)objectDict.get("line_1"),font));
        labels.add(setupLabelforLine((OBPath)objectDict.get("line_2"),font));
        currentLabel = labels.get(0);
        targetKeys = new ArrayList<>();
        for(OBGroup key : typewriterManager.keyboardKeys.values())
        {
            if(!(boolean)key.propertyValue("locked"))
                targetKeys.add(key);
        }
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                demo();
            }
        });
    }

    public OBLabel setupLabelforLine(OBPath line,OBFont font)
    {
        line.sizeToBoundingBoxIncludingStroke();
        OBLabel label = new OBLabel("0",font);
        label.setAlignment(OBLabel.OBLABEL_ALIGN_LEFT);
        label.setColour(Color.BLACK);
        PointF loc = OBMisc.copyPoint(line.getWorldPosition());
        loc.y -= OBUtils.getFontCapHeight(font.typeFace, font.size)/2.0f + line.lineWidth();
        label.setPosition(loc);
        label.setLeft(line.left());
        label.setString("");
        label.setProperty("start_top",label.top());
        label.setProperty("start_left",label.left());
        label.setProperty("line",line);
        attachControl(label);
        label.setZPosition(10);
        return label;
    }

    @Override
    public void touchDownKey(final OBGroup key, boolean sound)
    {
        key.disable();
        lockScreen();
        super.touchDownKey(key,true);
        String letter = (String)key.propertyValue("letter");
        boolean lastLabel = currentLabel == labels.get(labels.size()-1);
        if(appendLabel(currentLabel, letter,lastLabel))
        {
            currentLabel = labels.get(labels.indexOf(currentLabel)+1);
            appendLabel(currentLabel,letter,lastLabel);
        }
        unlockScreen();
        MainActivity.log("before");
        targetKeys.remove(key);
        if(targetKeys.size() == 0)
        {
            typewriterManager.lock();
            //typewriterManager.skipTouchUp();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception {
                    MainActivity.log("after");
                    waitForSecs(0.2f);
                    lockScreen();
                    typewriterManager.disableKey(key);
                    unlockScreen();
                    waitForSecs(0.5f);
                    fin();
                }
            });
        }
    }

    @Override
    public void touchUpKey(OBGroup key)
    {
        lockScreen();
        typewriterManager.disableKey(key);
        unlockScreen();
        long time = typewriterManager.unlock();
        prepareReminder(time);
    }

    public boolean appendLabel(OBLabel label, String letter, boolean force)
    {
        String prevString = label.text();
        boolean labelFull = false;
        label.setString(String.format("%s%s",label.text(),letter));
        label.sizeToBoundingBox();
        OBPath line = (OBPath)label.propertyValue("line");
        if(!force && label.width() > line.width())
        {
            label.setString(prevString);
            label.sizeToBoundingBox();
            labelFull = true;
        }
        label.setTop((float)label.propertyValue("start_top"));
        label.setLeft((float)label.propertyValue("start_left"));

       return labelFull;
    }

    public void playReminder(long time) throws Exception
    {
        if(time == statusTime() && !_aborting)
        {
            playAudioQueuedScene("REMIND",300,false);
            typewriterManager.animateFlash(time);
        }
    }

    public void startScene() throws Exception
    {
        setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene("1","PROMPT.REPEAT") ,300));
        long time =  typewriterManager.unlock();
        playAudioQueuedScene("PROMPT",300,true);
        prepareReminder(time);
    }

    public void prepareReminder(final long time)
    {
        cancelReminder();
        reminderRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    playReminder(time);
                }
                catch (Exception e)
                {

                }
            }
        };
       reminderHandler.postDelayed(reminderRunnable, 7 * 1000);
    }

    public void cancelReminder()
    {
        if(reminderRunnable != null)
            reminderHandler.removeCallbacks(reminderRunnable);

        reminderRunnable = null;
    }


    public void demo() throws Exception
    {
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.95f,this.bounds()),-35,0.5f,"DEMO",1,0.3f);
        List<OBGroup> row1 = typewriterManager.keyboardLayout.get(0);
        List<OBGroup> row3 = typewriterManager.keyboardLayout.get(typewriterManager.keyboardLayout.size()-1);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.05f,row1.get(0).frame()),-50,0.7f,true);
        playAudioScene("DEMO",2,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.05f,row3.get(row3.size()-1).frame()),-20,3,true);
        waitForAudio();
        waitForSecs(0.3f);
        OBGroup key = row1.get(4);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.05f,key.frame()),-35,0.5f,"DEMO",3,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.65f,key.frame()),-35,0.2f,true);
        touchDownKey(key,true);
        waitForSecs(0.3f);
        lockScreen();
        typewriterManager.disableKey(key);
        unlockScreen();
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.05f,key.frame()),-35,0.2f,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

}
