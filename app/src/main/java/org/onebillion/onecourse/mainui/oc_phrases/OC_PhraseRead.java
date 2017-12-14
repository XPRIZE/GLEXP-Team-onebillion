package org.onebillion.onecourse.mainui.oc_phrases;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by alan on 13/12/17.
 */

public class OC_PhraseRead extends OC_PhraseSentenceRead
{
    public void miscSetUp()
    {
        loadEvent("mastera");
        showIntro = OBUtils.coalesce(parameters.get("intro"),"false").equals("true");
        lrDemoRequired = OBUtils.coalesce(parameters.get("lrdemo"),"false").equals("true");

        componentDict = loadComponent("phrase",getLocalPath("phrases.xml"));
        OBControl tb = objectDict.get("textbox");
        textBoxOriginalPos = new PointF();
        textBoxOriginalPos.set(tb.position());
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        textBox.setShouldTexturise(false);
        attachControl(textBox);

        currNo = -1;
        componentList = Arrays.asList(parameters.get("phrases").split(","));
        events = new ArrayList(Arrays.asList("b,b2,c,e,f".split(",")));
        while(events.size()  - 2 > componentList.size() )
            events.remove(events.size()-1);
        while(events.size()  - 2 < componentList.size() )
            events.add(events.get(events.size() - 1));
        if(showIntro)
            events.add(0,"a");
        setUpWordStuff();
    }


    public boolean newPicRequiredForScene(String scene)
    {
        return(Arrays.asList("b","e","f","g").contains(scene));
    }

    public void setSceneXX(String scene)
    {
        if(newPicRequiredForScene(scene))
        {
            currNo++;
            setUpScene();
        }
    }

    public void setScenea()
    {
        setSceneXX(currentEvent());
        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF rp = new PointF();
        rp.set(presenter.control.position());
        presenter.control.setProperty("restpos",rp);
        presenter.control.setRight(0);
        presenter.control.show();
    }



    public void doMainc() throws Exception
    {
        waitForSecs(0.5f);
        lockScreen();
        wordback.hide();
        wordback2.hide();
        unlockScreen();
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",true);
        waitForSecs(0.3f);
        nextWord();
    }


    public void demoa()throws Exception
    {
        presenterDemo();
    }

    public void demob()throws Exception
    {
        waitForSecs(0.3f);
        showPic();
        waitForSecs(0.3f);

        PointF destpoint = OB_Maths.locationForRect(0.75f, 0.75f,bounds());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);

        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.85f, 0.85f,bounds()),-1,true);
        waitForSecs(0.3f);
        showWords();
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);

        OBReadingWord firstWord = firstWord();
        RectF f = firstWord.frame;

        PointF wordPoint = OB_Maths.locationForRect(0.5f, 1.2f, f);
        movePointerToPoint(wordPoint,-1,true);
        waitForSecs(0.3f);

        lockScreen();
        wordback.show();
        wordback2.show();
        unlockScreen();
        waitForSecs(0.3f);
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        movePointerForwards(applyGraphicScale(-40),-1);
        waitForSecs(0.3f);
        playAudioScene("DEMO2",1,true);

        waitForSecs(0.4f);
        thePointer.hide();
        //waitForSecs(0.4f);
        nextScene();
    }

    public void demod()throws Exception
    {
        lrDemo("d");
    }

    public void nextWord()throws Exception
    {
        if(currentEvent().equals("b2"))
        {
            nextScene();
            return;
        }
        super.nextWord();
    }


    public void considerLrDemo(boolean primary)throws Exception
    {
        if(primary && currentEvent().equals("c") && lrDemoRequired)
        {
            demod();
        }
    }

}
