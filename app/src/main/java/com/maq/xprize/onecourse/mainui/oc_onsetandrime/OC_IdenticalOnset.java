package com.maq.xprize.onecourse.mainui.oc_onsetandrime;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.controls.*;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBReadingWord;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller.boundingBoxForText;
import static com.maq.xprize.onecourse.utils.OBUtils.StandardReadingFontOfSize;

/**
 * Created by alan on 15/03/2018.
 */

public class OC_IdenticalOnset extends OC_Onset
{
    PointF midLeft0;
    List<String>suffixes;
    OBLabel onsetLabel;
    String onset;

    public void miscSetUp()
    {
        super.miscSetUp();
        onset = parameters.get("onset");
    }

    public List<String> suffixes()
    {
        List parray = new ArrayList<>();
        int rct =(int) onset.length();
        for(String k : wordIDs)
        {
            OBPhoneme rw = wordDict.get(k);
            String pref = rw.text.substring(rct,rw.text.length());
            parray.add(pref);
        }
        return parray;
    }

    public OBLabel createCompleteLabel(String tx)
    {
        OBLabel lab = createLabelText(tx,font,Color.BLACK);
        lab.setPosition(midLeft0);
        lab.setLeft(midLeft0.x);
        lab.setZPosition(onsetLabel.zPosition() + 1);
        attachControl(lab);
        return lab;
    }

    public void setUpFirstScreen()
    {
        loadEvent("a");

        setupImageBack();
        OBControl r = objectDict.get("rect0");
        midLeft0 = new PointF(r.left() ,r.frame().centerY());
        y0 = objectDict.get("rect1").frame().centerY();
        yinc = objectDict.get("rect2").frame().centerY() - y0;
        onsetLabel = createLabelText(onset,font,Color.BLACK);
        onsetLabel.setPosition(midLeft0);
        onsetLabel.setLeft(midLeft0.x);
        attachControl(onsetLabel);

        suffixes = suffixes();
        OBControl d = objectDict.get("dash");
        dash = new OBControl();
        dash.setFrame(d.frame());
        dash.setWidth(mDashWidth());
        dash.setLeft(onsetLabel.right());
        dash.setFillColor(d.fillColor());
        dash.setZPosition(d.zPosition());

        RectF bb = boundingBoxForText(onset, font);
        dash.setTop(onsetLabel.bottom() + bb.top);

        attachControl(dash);

        createBottomLabels(suffixes);
        positionBottomLabels();

        labelList = new ArrayList<>();
        setUpScene();
    }

    public void setScenefinale()
    {
        loadEvent("finale");
        fontSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize"),"180"));
        font = StandardReadingFontOfSize(fontSize);

        OBControl r = objectDict.get("rect0");
        rimeLabel = createLabelText(onset,font,Color.BLACK);
        rimeLabel.setLeft(r.left());
        rimeLabel.setTop(r.top());
        rimeLabel.setZPosition(20);

        attachControl(rimeLabel);

        OBControl d = objectDict.get("dash");
        dash = new OBControl();
        dash.setFrame(d.frame());
        dash.setWidth(WidthOfText("M", lineAttributes(font) , spaceExtra));
        dash.setLeft(rimeLabel.right());
        dash.setFillColor(d.fillColor());
        dash.setZPosition(d.zPosition());
        attachControl(dash);

        RectF bb = boundingBoxForText(onset, font);
        dash.setTop(rimeLabel.bottom() + bb.top);

        float w1 = dash.left();
        float w2 = bounds() .width() - rimeLabel.right();
        float offset =(w1 + w2) / 2 - w1;
        dash.setLeft(dash.left() + offset);
        rimeLabel.setLeft(rimeLabel.left() + offset);
    }

    public OBLabel staticLabel()
    {
        return onsetLabel;
    }

    public void showStuff()
    {
        if(onsetLabel != null && onsetLabel.hidden())
        {
            float bot = Math.min(dash.bottom() , onsetLabel.bottom());
            PointF dashpos = new PointF();
            dashpos.set(dash.position());
            PointF onsetpos = new PointF();
            onsetpos.set(onsetLabel.position());
            lockScreen();
            dash.show();
            onsetLabel.show();
            dash.setBottom(dash.bottom() - bot);
            onsetLabel.setBottom(onsetLabel.bottom() - bot);
            unlockScreen();
            List anims = Arrays.asList(OBAnim.moveAnim(dashpos,dash),OBAnim.moveAnim(onsetpos,onsetLabel));
            OBAnimationGroup.runAnims(anims,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        }
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            playSfxAudio("bigonset",false);
            lockScreen();
            setScenefinale();
            unlockScreen();
            waitForSecs(0.3f);
            Map fnl = (Map) audioScenes.get("finale");
            playAudioQueued((List)fnl.get("DEMO"),true);
            waitForSecs(0.1f);
            for (int i = 0;i < 3;i++)
            {
                lockScreen();
                rimeLabel.setColour(Color.RED);
                unlockScreen();
                waitForSecs(0.5f);
                lockScreen();
                rimeLabel.setColour(Color.BLACK);
                unlockScreen();
                waitForSecs(0.25f);
            }
            waitForSecs(0.8f);
            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch(Exception exception)
        {
        }
    }

    public void demoa() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoa();

        nextScene();
    }

    public void demob() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        PointF ppos = new PointF();
        ppos.set(thePointer.position());
        movePointerForwards(applyGraphicScale(250),-1);
        waitForSecs(0.1f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);
        playAudioQueued(Arrays.asList((Object)wordIDs.get(currNo)),true);
        waitForSecs(0.4f);

        playAudioQueuedScene("DEMO2",true);
        PointF destpt = OB_Maths.locationForRect(new PointF(0.5f, 0.6f) , bottomLabels.get(0).frame());
        movePointerToPoint(destpt,-35,-1,true);
        waitForSecs(0.4f);
        OBLabel dragLabel = bottomLabels.get(0);
        destpt.x = (completeLabel.right() -(dragLabel.width() / 2));
        destpt.y = (completeLabel.position() .y);

        List anims = AnimsForMoveToPoint(Arrays.asList(dragLabel, thePointer), destpt);
        OBAnim turnAnim = OBAnim.rotationAnim((float) Math.toRadians(-10), thePointer);
        anims.add(turnAnim);
        OBAnimationGroup.runAnims(anims, 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, null);


        playSfxAudio("ending",true);
        lockScreen();
        dash.hide();
        unlockScreen();

        movePointerForwards(applyGraphicScale(-300),-1);
        waitForSecs(0.4f);

        lockScreen();
        completeLabel.setColour(Color.RED);
        completeLabel.show();
        dragLabel.hide();
        onsetLabel.hide();
        dash.hide();
        unlockScreen();

        playAudioQueued(Arrays.asList((Object)wordIDs.get(currNo)),true);
        waitForSecs(0.8f);


        lockScreen();
        completeLabel.hide();
        dragLabel.show();
        onsetLabel.show();
        unlockScreen();

        movePointerToPoint(ppos,-1,false);
        destpt =(PointF)dragLabel.propertyValue("botpos");
        moveObjects(Arrays.asList((OBControl) dragLabel), destpt, -1.5f, OBAnim.ANIM_EASE_IN_EASE_OUT);
        lockScreen();
        dash.show();
        unlockScreen();
        waitForSecs(0.3f);

        playAudioQueuedScene("DEMO3",true);

        nextScene();
    }

    public void democ() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoc();

        nextScene();
    }

    public void demof() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemof();

        nextScene();
    }

    public void  demog()throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemog(onset,true);

        nextScene();
    }

    public void demoh() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoh();

        nextScene();
    }

    public void demoi() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoi();

        nextScene();
    }

    public PointF destPointForDraggedLabel(OBLabel targ)
    {
        return new PointF(completeLabel.right() -(targ.width() / 2) ,completeLabel.position() .y);
    }

    public String dragInSFX()
    {
        return "ending";
    }

}
