package org.onebillion.onecourse.mainui.oc_phrases;
import android.graphics.PointF;
import android.graphics.RectF;
import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by alan on 15/12/17.
 */

public class OC_PhraseMake extends OC_PhraseSentenceMake
{

    public void miscSetUp()
    {
        loadEvent("mastera");
        showIntro = OBUtils.coalesce(parameters.get("demo"),"false").equals("true");
        componentDict = loadComponent("phrase",getLocalPath("phrases.xml"));
        OBControl tb = objectDict.get("textbox");
        textBoxOriginalPos = new PointF();
        textBoxOriginalPos.set(tb.position());
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        textBox.setShouldTexturise(false);
        attachControl(textBox);

        OBPath ul = (OBPath)objectDict.get("underline");
        float lw = ul.lineWidth();
        ul.outdent(lw);

        String s = null;
        if((s = eventAttributes.get("textsize")) != null)
            fontSize = applyGraphicScale(Float.valueOf(s));
        letterSpacing = 0;
        currNo = -1;
        componentList = Arrays.asList(parameters.get("phrases").split(","));
        events = new ArrayList<>(Arrays.asList("b,b2,c,d".split(",")));
        if(showIntro)
            events.add(0,"a");
        while(events.size()  - 1 > componentList.size() )
            events.remove(events.size()-1);
        while(events.size()  - 1 < componentList.size() )
            events.add(events.get(events.size() - 1));
    }

    public boolean newPicRequiredForScene(String scene)
    {
        return(Arrays.asList("a","b","c","d","e").contains(scene));
    }

    public void demoa() throws Exception
    {
        doIntro();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);
        playObjectAudioWait(true);

        waitForSecs(0.2f);
        playAudioScene("DEMO2",0,true);


        PointF destpoint = OB_Maths.locationForRect(0.5f, 0.6f,words.get(wordIdx).label.frame());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);

        OBControl underline = objectDict.get("underline");
        for(OBReadingWord w : words)
        {
            positionUnderline();
            waitForSecs(0.2f);
            PointF p = OB_Maths.locationForRect(0.5f, 0.6f,w.label.frame());
            movePointerToPoint(p,-1,true);
            waitForSecs(0.2f);
            w.label.setZPosition(w.label.zPosition() + 30);
            moveObjects(Arrays.asList(w.label,thePointer),w.homePosition,-0.6f,OBAnim.ANIM_EASE_IN_EASE_OUT);
            underline.hide();
            waitForSecs(0.2f);
            movePointerForwards(applyGraphicScale(-100),-1);
            speakWordAsPartial(w,currComponentKey);
            w.label.setZPosition(w.label.zPosition() - 30);

            wordIdx++;
        }
        movePointerToPoint(OB_Maths.locationForRect(0.9f, 0.9f,boundsf()),-1,true);
        waitForSecs(0.2f);
        readPage();
        waitForSecs(1f);
        thePointer.hide();
        waitForSecs(1f);
        playAudioScene("DEMO2",1,true);
        waitForSecs(0.2f);
        nextScene();

    }

    public void demob() throws Exception
    {
        doIntro();
        waitForSecs(0.3f);

        PointF destpoint = OB_Maths.locationForRect(0.9f, -0.3f,objectDict.get("bottomrect") .frame());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);

        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);

        destpoint = OB_Maths.locationForRect(0.9f, 0.5f,objectDict.get("bottomrect").frame());
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO2",true);

        PointF butPt = OB_Maths.locationForRect(0.5f,1.1f, MainViewController().topRightButton.frame);

        movePointerToPoint(butPt,0,-1,true);

        playAudioQueuedScene("DEMO3",true);
        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

}
