package com.maq.xprize.onecourse.hindi.mainui.oc_typing;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBFont;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.OCM_MlUnitInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Created by michal on 12/06/2018.
 */

public class OC_TypewriterManager
{

    public static String MODE_NORMAL= "normal", MODE_DISABLED = "disabled";
    public static String  KEY_SPACE = "s_space", KEY_SHIFT = "s_shift", KEY_DOT = "s_dot";

    public Map<String,OBGroup> keyboardKeys;
    public List<List<OBGroup>> keyboardLayout;

    private List<OBControl> allKeyboardKeys;
    private OBGroup lastKey;
    private boolean keyboardLocked;
    private boolean keyboardCapitalMode;
    private OC_TypewriterReceiver typewriterReceiver;
    private OC_SectionController sectionController;
    private Map<String,OBPath> keyTemplates;
    private String tapAudio;

    private Handler touchUpHandler;
    private Runnable touchUpRunnable;

    public OC_TypewriterManager(String layoutString, OBControl rectControl , OBPath keyNormal,
                                OBPath keyDisabled, String audio, OC_SectionController controller)
    {

        sectionController = controller;
        typewriterReceiver = (OC_TypewriterReceiver)controller;
        keyTemplates = new ArrayMap<>();
        keyTemplates.put(MODE_NORMAL,keyNormal);
        keyTemplates.put(MODE_DISABLED,keyDisabled);
        List<List<String>> keyboardLetters = new ArrayList<>();
        String[] rows = layoutString.split(";");
        for(String row : rows)
        {
            keyboardLetters.add(Arrays.asList(row.split(",")));
        }

        tapAudio = audio;
        keyboardCapitalMode = false;
        keyboardLocked = false;
        touchUpHandler = new Handler();
        loadKeyboardInRect(rectControl,keyboardLetters);
    }

    public void setTypeAudio(String audio)
    {
        tapAudio = audio;
    }
    public void setSpecialAudio(String audio,String keyCode)
    {
        if(keyboardKeys.containsKey(keyCode)) {
            OBControl key = keyboardKeys.get(keyCode);
            key.setProperty("special_audio", audio);
        }
    }

    public void animateFlash(final long time)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                while (time == sectionController.statusTime()) {
                    for (int i = 0; i < 2; i++) {
                        if (sectionController.statusChanged(time))
                            break;
                        sectionController.lockScreen();
                        resetFlash();
                        highlight();
                        sectionController.unlockScreen();

                        if (sectionController.statusChanged(time))
                            break;
                        sectionController.waitForSecs(0.5);
                        sectionController.lockScreen();
                        resetFlash();
                        sectionController.unlockScreen();

                        if (sectionController.statusChanged(time))
                            break;
                        sectionController.waitForSecs(0.5);

                    }
                    if (!sectionController.statusChanged(time))
                        sectionController.waitForSecs(5);

                }
                sectionController.lockScreen();
                resetFlash();
                sectionController.unlockScreen();
            }
        } );

    }

    public void fitBackground(OBControl bg)
    {
        bg.setZPosition(1);
        bg.setWidth(sectionController.bounds().width());
        List<OBGroup> lastRow = keyboardLayout.get(keyboardLayout.size()-1);
        float bottomDist = sectionController.bounds().height() - lastRow.get(lastRow.size()-1).bottom();
        float keyTop = keyboardLayout.get(0).get(0).top();
        bg.setHeight (sectionController.bounds().height() -(keyTop - bottomDist));
        bg.setPosition(OB_Maths.locationForRect(0.5f,0.5f, sectionController.boundsf()));
        bg.setTop(keyTop - bottomDist);
    }

    public void resetFlash()
    {
        for(List<OBGroup> row : keyboardLayout)
            for(OBGroup key : row)
                if(key.isEnabled())
                    key.lowlight();
    }

    public void highlight()
    {
        for(List<OBGroup> row : keyboardLayout)
        {
            for(OBGroup key : row)
            {
                if(key.isEnabled())
                    key.highlight();
            }
        }
    }

    public void lock()
    {
        keyboardLocked = true;
    }


    public void loadKeyboardInRect(OBControl keyboardRect,List<List<String>> letters)
    {
        Map<String,OBGroup> allKeys = new ArrayMap();
        List<List<OBGroup>> layout = new ArrayList<>();
        long rowCount = letters.size();
        float buttonDistanceRatio = 0.12f;
        float buttonSize= keyboardRect.height()/(rowCount + rowCount*buttonDistanceRatio + buttonDistanceRatio);
        float buttonDistance = buttonSize*buttonDistanceRatio;
        float top = keyboardRect.top() + buttonDistance;
        float minTop = keyboardRect.top() + buttonDistance;
        float maxBottom = minTop;

        OBFont font = new OBFont(OBUtils.standardTypeFace(),60.0f * buttonSize/87.0f);
        for(int i=0; i<letters.size(); i++)
        {
            List<String> lettersRow = letters.get(i);
            float left = 0;
            List<OBGroup> rowLayout = new ArrayList<>();
            for(String code : lettersRow)
            {
                List<OBControl> arr = new ArrayList<>();
                String displayLetter = code;
                String value = code;
                OBPath key  = null;
                boolean specialKey = code.startsWith("s_");
                if(specialKey)
                {
                    if(code.equals(KEY_SHIFT))
                    {
                        displayLetter = "ABC";
                        key = keyboardButtonForWidth(buttonSize *(2+buttonDistanceRatio),buttonSize);
                    }
                    else if(code.equals(KEY_SPACE))
                    {
                        displayLetter = "";
                        value = " ";
                        key = keyboardButtonForWidth(buttonSize *(5 + 4 * buttonDistanceRatio),buttonSize);
                    }
                    else if(code.equals(KEY_DOT))
                    {
                        displayLetter = ".";
                        value = ".";
                        key = keyboardButtonForWidth(buttonSize,buttonSize);
                    }
                }
                else
                {
                    key = keyboardButtonForWidth(buttonSize,buttonSize);
                }
                boolean unicodeChar = displayLetter.startsWith("#");
                if(unicodeChar)
                {
                    int codevalue = (new Integer(displayLetter.substring(1))).intValue();

                    displayLetter =  Character.toString((char)codevalue);
                    if(!specialKey)
                        value = displayLetter;
                }
                key.setTop(top);
                key.setLeft(left);
                key.setZPosition(1);
                key.sizeToBoundingBoxIncludingStroke();
                key.show();
                if(key.bottom() > maxBottom)
                    maxBottom =  key.bottom();
                arr.add(key);
                OBLabel lowerCaseLabel = null;
                OBLabel upperCaseLabel = null;
                if(displayLetter != null && !displayLetter.equals(""))
                {
                    lowerCaseLabel = labelForKey(key, font, displayLetter, !specialKey);
                    arr.add(lowerCaseLabel);
                    if(!specialKey && !unicodeChar)
                    {
                        upperCaseLabel = labelForKey(key,font,displayLetter.toUpperCase(),false);
                        upperCaseLabel.hide();
                        arr.add(upperCaseLabel);
                    }
                }
                OBGroup buttonGroup = new OBGroup(arr);
                sectionController.attachControl(buttonGroup);
                buttonGroup.setZPosition(52);
                buttonGroup.objectDict.put("button",key);
                if(lowerCaseLabel != null)
                    buttonGroup.objectDict.put("label",lowerCaseLabel);
                if(upperCaseLabel != null)
                {
                    buttonGroup.objectDict.put("label_upper",upperCaseLabel);
                    buttonGroup.setProperty("value_upper",value.toUpperCase());
                }
                buttonGroup.setProperty("value",value);
                buttonGroup.enable();
                buttonGroup.setProperty("mode",MODE_NORMAL);
                buttonGroup.setProperty("locked",false);

                if(specialKey)
                    buttonGroup.setProperty("letter",code);
                allKeys.put(code,buttonGroup);
                rowLayout.add(buttonGroup);
                left += key.width() + buttonDistance;

            }
            top += buttonSize + buttonDistance;
            layout.add(rowLayout);
        }
        keyboardLayout = layout;
        keyboardKeys = allKeys;
        for(List<OBGroup> keyArray : layout)
        {
            float maxRight = keyArray.get(keyArray.size()-1).right();
            float minLeft =  keyArray.get(0).left();
            float keyboardWidth = maxRight - minLeft;
            float startLeft = keyboardRect.position().x -(keyboardWidth)/2.0f;
            float rightNudge = startLeft - minLeft;
            for(OBControl key : keyArray)
            {
                key.setRight(key.right() + rightNudge);
                key.setProperty("start_top",key.top());
            }
        }
        setCapitalMode(keyboardCapitalMode);

        allKeyboardKeys = (List<OBControl>)(Object)new ArrayList<>(keyboardKeys.values());
    }

    public void lockKeysForString(String lockString)
    {
        String[] arr = lockString.split(",");
        for(String code : arr)
        {
            OBGroup key = keyboardKeys.get(code);
            disableKey(key);
            key.setProperty("locked",true);
        }
    }

    public void setCapitalMode(boolean capital)
    {
        keyboardCapitalMode = capital;
        sectionController.lockScreen();
        for(OBGroup key : keyboardKeys.values())
        {
            if(key.objectDict.get("label") != null)
            {
                OBControl label = key.objectDict.get("label");
                if(key.objectDict.get("label_upper") != null)
                {
                    OBControl labelUpper = key.objectDict.get("label_upper");
                    if(capital)
                    {
                        key.setProperty("letter",key.propertyValue("value_upper"));
                        label.hide();
                        labelUpper.show();
                    }
                    else
                    {
                        key.setProperty("letter",key.propertyValue("value"));
                        label.show();
                        labelUpper.hide();
                    }
                }
            }
        }
        sectionController.unlockScreen();
    }

    public OBPath keyboardButtonForWidth(float width, float height)
    {
        OBPath keyNormal = (OBPath)keyTemplates.get(MODE_NORMAL).copy();
        Path p = new Path();
        float cornerRadius = height * 0.1f;
        p.addRoundRect(0,0,width, height,cornerRadius ,cornerRadius,Path.Direction.CCW);
        keyNormal.setPath(p);
        keyNormal.sizeToBoundingBox();
        return keyNormal;
    }

    public OBLabel labelForKey(OBControl key, OBFont font, String letter, boolean align)
    {
        OBLabel label = new OBLabel(letter,font);
        label.setZPosition(2);
        label.setColour(Color.BLACK);
        PointF loc = key.position();
        if(align)
        {
            // loc.y-= font.capHeight*0.5- font.xHeight*0.5;

        }
        label.setPosition(loc);
        return label;
    }

    public void setKey(OBGroup key,String mode)
    {
        OBPath targetButton = keyTemplates.get(mode);
        OBPath button =(OBPath ) key.objectDict.get("button");
        button.setFillColor(targetButton.fillColor());
        button.setStrokeColor(targetButton.strokeColor());
        OBLabel label =(OBLabel ) key.objectDict.get("label");
        if(label != null && targetButton.attributes().containsKey("font_colour"))
        {
            label.setColour(OBUtils.colorFromRGBString((String)targetButton.attributes().get("font_colour")));

        }
        OBLabel labelUpper =(OBLabel)key.objectDict.get("label_upper");
        if(labelUpper != null && targetButton.attributes().containsKey("font_colour"))
        {
            labelUpper.setColour(OBUtils.colorFromRGBString((String)targetButton.attributes().get("font_colour")));
        }
    }

    public void touchDownKey(OBGroup key,boolean sound)
    {
        highlightKey(key,true,sound);
    }

    public void highlightKey(OBGroup key,boolean on,boolean sound)
    {
        if(sound)
        {
            if(key.propertyValue("special_audio") != null)
            {
                sectionController.playSFX((String)key.propertyValue("special_audio"));
            }
            else
            {
                sectionController.playSFX(tapAudio);
            }
        }
        if(on)
        {
            highlightKey(key);
        }
        else
        {
            lowlightKey(key);
        }
    }

    public void touchUpKey(OBGroup key)
    {
        lowlightKey(key);
    }

    public long unlock()
    {
        keyboardLocked = false;
        return sectionController.setStatus(OBSectionController.STATUS_AWAITING_CLICK);
    }

    public void highlightKey(OBGroup key)
    {
        key.highlight();
        key.setTop(key.top() + key.height()*0.015f);
    }

    public void lowlightKey(OBGroup key)
    {
        sectionController.lockScreen();
        key.lowlight();
        key.setTop((float)key.propertyValue("start_top"));
        sectionController.unlockScreen();
    }

    public void disableKey(OBGroup key)
    {
        if((boolean)key.propertyValue("locked"))
            return;
        key.setTop((float)key.propertyValue("start_top"));
        setKey(key,MODE_DISABLED);
        key.lowlight();
        key.disable();
    }

    public void enableKey(OBGroup key)
    {
        if((boolean)key.propertyValue("locked"))
            return;
        key.setTop((float)key.propertyValue("start_top"));
        setKey(key,(String)key.propertyValue("mode"));
        key.lowlight();
        key.enable();
    }

    public void skipTouchUp()
    {
        lastKey = null;
    }

    public void touchDownAtPoint(PointF pt)
    {
        if(sectionController.status() == OBSectionController.STATUS_AWAITING_CLICK && !keyboardLocked)
        {
            final OBControl targ = sectionController.finger(-1,-1, allKeyboardKeys,pt);
            if(targ != null && targ.isEnabled())
            {
                final OBGroup key = (OBGroup)targ;
                final long time = sectionController.setStatus(OBSectionController.STATUS_BUSY);
                lastKey = key;
                String value = (String)key.propertyValue("value");
                if(value.equals(KEY_SHIFT))
                {
                    keyboardCapitalMode = !keyboardCapitalMode;
                    highlightKey(key,keyboardCapitalMode,true);
                    setCapitalMode(keyboardCapitalMode);
                    lastKey = null;
                    unlock();
                }
                else
                {
                    cancelTouchUpRunnable();
                    typewriterReceiver.touchDownKey(key,true);
                    touchUpRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(time == sectionController.statusTime() && !keyboardLocked)
                            {
                                lastKey = null;
                                typewriterReceiver.touchUpKey(key);
                            }
                        }
                    };
                    touchUpHandler.postDelayed(touchUpRunnable,2*1000);
                }
            }
        }
    }

    public void cancelTouchUpRunnable()
    {
        if(touchUpRunnable != null)
            touchUpHandler.removeCallbacks(touchUpRunnable);

        touchUpRunnable = null;
    }

    public void touchUpAtPoint(PointF pt)
    {
        if(sectionController.status() == OBSectionController.STATUS_BUSY && lastKey != null && !keyboardLocked)
        {
            OBGroup temp = lastKey;
            lastKey = null;
            typewriterReceiver.touchUpKey(temp);
        }
    }

    public boolean capitalMode()
    {
        return keyboardCapitalMode;
    }

    public void disableRowsSkip(OBGroup skipKey)
    {
        sectionController.lockScreen();
        for(List<OBGroup> row : keyboardLayout)
        {
            if(!row.contains(skipKey))
            {
                for(OBGroup key : row)
                {
                    disableKey(key);
                }
            }
        }
        sectionController.unlockScreen();
    }

    public void disableAllSkip(OBGroup skipKey)
    {
        sectionController.lockScreen();
        for(List<OBGroup> row : keyboardLayout)
        {
            for(OBGroup key : row)
            {
                if(key != skipKey)
                    disableKey(key);
            }
        }
        sectionController.unlockScreen();
    }

}
