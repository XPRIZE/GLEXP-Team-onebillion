package com.maq.xprize.onecourse.mainui.oc_typing;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 12/06/2018.
 */

public class OC_Twrd extends OC_SectionController implements OC_TypewriterReceiver
{
    public OC_TypewriterManager typewriterManager;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        List<String> sfxList = getAudioForScene("sfx","click");
        if(eventAttributes.containsKey("scenes"))
            events = Arrays.asList(eventAttributes.get("scenes").split(","));
        typewriterManager = new OC_TypewriterManager((String)parameters.get("keyboard"), objectDict.get("keyboard_rect"),
                (OBPath)objectDict.get("button_normal"), (OBPath)objectDict.get("button_disabled"),
                sfxList != null ? sfxList.get(0) : null, this);
        boolean capitalMode = OBUtils.getBooleanValue(parameters.get("capitalmode"));
        typewriterManager.setCapitalMode(capitalMode);
        if(parameters.containsKey("locked"))
            typewriterManager.lockKeysForString(parameters.get("locked"));
        OBControl bg = objectDict.get("keyboard_bg");
        typewriterManager.fitBackground(bg);
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
    }

    @Override
    public void doMainXX() throws Exception
    {

    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        typewriterManager.touchDownAtPoint(pt);
    }

    @Override
    public void touchUpAtPoint(PointF pt,View v)
    {
        typewriterManager.touchUpAtPoint(pt);
    }

    @Override
    public void touchUpKey(OBGroup key)
    {
        typewriterManager.touchUpKey(key);
    }

    @Override
    public void touchDownKey(OBGroup key,boolean sound)
    {
        typewriterManager.touchDownKey(key,sound);
    }

}
