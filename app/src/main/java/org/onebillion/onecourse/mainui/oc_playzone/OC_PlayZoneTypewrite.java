package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBScrollingText;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.TextureShaderProgram;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBFatController;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OCM_FatController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset.ASSET_TEXT;

/**
 * Created by alan on 07/07/2017.
 */

public class OC_PlayZoneTypewrite extends OC_SectionController
{
    static final String MODE_NORMAL= "normal",MODE_SPECIAL = "special", MODE_HIGHLIGHT = "highlight";
    static final String KEY_ENTER= "s_enter", KEY_SPACE = "s_space", KEY_SHIFT = "s_shift", KEY_NUM = "s_num", KEY_BACK = "s_back";
    static final String line_prefix = "\t\t\t\t";
    static final float TICK_VALUE2 = 0.0025f;

    OBGroup lastKey;
    boolean keyboardLocked;
    String currentTheme;
    int highlightColour;
    Map<String,OBGroup> keyboardKeys;
    List<List<OBGroup>> keyboardLayout;
    List<String> themeNames;
    Map<String,Map> themesData = new HashMap<>();
    OBScrollingText textBox;
    SpannableStringBuilder currentText;
    DynamicLayout layout;
    Typeface currentTypeface;
    float currentTypeSize;
    Map displayStringAttributes;
    OBGroup textBoxGroup;
    float currentTextBoxHeight;
    float textBoxTopLimit, textBoxBottomLimit;
    PointF lastTextBoxLoc;
    boolean dragMode, capitalMode,cursorShouldShow;
    int currentTapIndex;
    float dragStartTouchY,dragStartTextOffset;
    float lastAmt;
    double lastAmtTime,dragSpeed;
    boolean readOnly = false;

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    void setUpKeyboard()
    {
        List<List<String>> keyboardLetters = new ArrayList<>();
        highlightColour = objectDict.get("button_highlight").fillColor();

        String[] rowa = parameters.get("keyboard").split(";");
        List<String> rows = Arrays.asList(rowa);
        for(String row : rows)
        {
            List<String> rowLetters = new ArrayList<>();
            for(String letter : row.split(","))
            {
                rowLetters.add(letter);
            }
            keyboardLetters.add(rowLetters);
        }
        loadKeyboard(keyboardLetters);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        String p = parameters.get("readonly");
        if (p != null)
            readOnly = p.equals("true");
        loadEvent(readOnly?"master_ro":"master");
        events = new ArrayList<>();

        if (!readOnly)
        {
            setUpKeyboard();
        }


        String[] themea = ((String)eventAttributes.get("themes")).split(",");;
        themeNames = Arrays.asList(themea);

        textBox = new OBScrollingText(objectDict.get("text_box").frame());
        textBox.setZPosition(5);
        textBox.setFillColor(Color.WHITE);
        textBox.setZPosition(50);
        attachControl(textBox);

        p = parameters.get("text");
        textBox.setString(p == null?"":p);

        int themeIdx = 1;
        p = parameters.get("theme");
        if (p != null)
        {
            int idx = themeNames.indexOf(p);
            if (idx >= 0)
                themeIdx = idx;
        }

        loadTheme(themeNames.get(themeIdx));


        OBControl gradientTop = objectDict.get("text_box_gradient_top");
        OBControl gradientBottom = objectDict.get("text_box_gradient_bottom");
        gradientTop.setWidth(textBox.width() * 1.02f);
        gradientBottom.setWidth(textBox.width() * 1.02f);
        gradientTop.setPosition(textBox.position());
        gradientBottom.setPosition(textBox.position());
        gradientTop.setZPosition(51);
        gradientBottom.setZPosition(51);
        gradientTop.setTop(textBox.top() - applyGraphicScale(1));
        gradientBottom.setBottom(textBox.bottom() + applyGraphicScale(1));

        List<OBControl>gControls = new ArrayList<>();
        gControls.add(textBox);
        OBPath cursor = (OBPath) objectDict.get("cursor");
        if (cursor != null)
        {
            float lw = cursor.shapeLayer().stroke.lineWidth;
            int col = cursor.shapeLayer().stroke.colour;
            OBControl newc = new OBControl();
            newc.setBounds(0,0,lw,textBox.capHeight());
            newc.setBackgroundColor(col);
            newc.setPosition(textBox.position());
            newc.setZPosition(textBox.zPosition() + 1);

            detachControl(cursor);
            attachControl(newc);
            objectDict.put("cursor",newc);
            gControls.add(newc);
        }

        textBoxGroup = new OBGroup(gControls,textBox.frame());
        textBoxGroup.setZPosition(50);
        OBControl tbb = objectDict.get("text_box_bg");
        tbb.setZPosition(49);
        textBoxGroup.setPosition(tbb.position());
        attachControl(textBoxGroup);

        if (!readOnly)
        {
            refreshCursorAndTextBox();
            cursorShouldShow = true;

            objectDict.get("button").setZPosition(48);
            objectDict.get("button_save").setZPosition(48);
            objectDict.get("text_box_bg").setZPosition(49);
        }

        capitalMode = false;
        currentTapIndex = 0;
    }

    public void start()
    {
        if (!readOnly)
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    setStatus(STATUS_AWAITING_CLICK);
                    startCursorFlash();
                }
            });
    }

    public void startCursorFlash()
    {
        OBControl cursor = objectDict.get("cursor");
        try
        {
            while(!_aborting)
            {
                lockScreen();
                if(cursor.hidden() )
                {
                    if (cursorShouldShow)
                        cursor.show();
                }
                else
                {
                    cursor.hide();
                }
                unlockScreen();
                if(!_aborting)
                    waitForSecs(0.5f);
            }
        }
        catch(Exception e)
        {
        }
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
    }

    public void doMainXX()
    {
    }


    public void resetKeyboardFlash()
    {
        for(List<OBGroup> row : keyboardLayout)
        for(OBGroup key : row)
            if(key.isEnabled() )
                key.lowlight();

    }

    public void keyboardHighlight()
    {
        for(List<OBGroup> row : keyboardLayout)
        {
            for(OBGroup key : row)
            {
                if(key.isEnabled() )
                    key.highlight();
            }
        }

    }

    public long unlockKeyboard()
    {
        keyboardLocked = false;
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void lockKeyboard()
    {
        keyboardLocked = true;
    }

    public void loadTheme(String name)
    {
        if (readOnly)
            name = name + "_ro";
        lockScreen();
        Map themeData = themesData.get(name);
        if(themeData == null)
        {
            Map dictData = new HashMap();
            List<OBControl> themeFiles = loadEvent(name);
            for(OBControl con : themeFiles)
            {
              //  con.setRasterScale(0.75f);
                //con.setShouldRasterize(true);
            }
            /*OBGroup group = new OBGroup(themeFiles);

            RectF frame = group.frame();
            RectF targetFrame = this.boundsf();
            group.setBounds(frame.left, frame.top, targetFrame.width()-frame.left, targetFrame.height() - frame.top);
            group.setMasksToBounds(true);
            group.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.boundsf()));
            group.setRasterScale(0.9f);
            attachControl(group);

            group.setZPosition(1);
            dictData.put("controls",Arrays.asList(group));*/

            dictData.put("controls",themeFiles);
            dictData.put("font",eventAttributes.get("fontfile"));
            themesData.put(name,dictData);
            themeData = dictData;
        }

        if(currentTheme != null)
        {
            Map<String,OBControl> prevThemeData = themesData.get(currentTheme);
            for(OBControl con : (List<OBControl>)prevThemeData.get("controls"))
            {
                con.hide();
            }
        }
        for(OBControl con : (List<OBControl>)themeData.get("controls"))
        {
            con.show();
        }

        currentTheme = name;
        String fontPath = String.format("fonts/%s",(String)themeData.get("font"));
        currentTypeface = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), fontPath);
        currentTypeSize = applyGraphicScale(60f);

        textBox.setTypeFace(currentTypeface);
        textBox.setFontSize(currentTypeSize);
        textBox.setColour(Color.BLACK);
        textBox.layout();
        currentText = textBox.textBuffer();
        if (currentText.length() == 0)
            textBox.setString(line_prefix);
        //currentText.setSpan(new LeadingMarginSpan.Standard(200, 0),0,currentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        /*
        NSMutableParagraphStyle *parStyle = [NSMutableParagraphStyle.alloc() init];

        parStyle.setLineBreakMode(NSLineBreakByWordWrapping);

        currentFont = UIFont.fontWithName(themeData"font".()  size:applyGraphicScale(60));*/
        OBControl cursor = objectDict.get("cursor");
        if (cursor != null)
            cursor.setHeight(currentTypeSize * 1.2f);

        unlockScreen();
    }

    public void loadKeyboard(List<List<String>> letters)
    {
        Map allKeys = new HashMap();
        List<List<OBGroup>> layout = new ArrayList<>();
        OBControl keyboardRect = objectDict.get("keyboard_rect");
        OBPath keyNormal = (OBPath)objectDict.get("button_normal");
        keyboardRect.setZPosition(51);
        float buttonDistance = keyNormal.width()*0.12f;
        float minTop = keyboardRect.top() + buttonDistance;
        float maxBottom = minTop;
        float top = minTop;
        float height = keyNormal.height();
        float minLeft = keyboardRect.left() + buttonDistance;
        float maxRight = minLeft;
        Typeface typeface = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "fonts/onebillionreader-Regular.otf");
        Typeface specialtypeface = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "fonts/onebillionreader-Regular.otf");
        float fontSize = 40f * height / 60f;
        float specialfontSize = 30f * height / 60f;
         for(List<String> lettersRow : letters)
        {
            float left = minLeft;
            List rowLayout = new ArrayList<>();
            for(String code : lettersRow)
            {
                String displayLetter = code;
                String value = code;
                List arr = new ArrayList<>();

                OBPath key  = (OBPath)keyNormal.copy();
                boolean specialKey = code.startsWith("s_");
                if(specialKey)
                {
                    if(code.equals(KEY_SHIFT))
                    {
                        displayLetter = "ABC";
                        Path p = new Path();
                        p.addRoundRect(0,0,key.width()*2+ buttonDistance, key.height(),6.71f,6.71f,Path.Direction.CCW);
                        key.setPath(p);
                        key.sizeToBoundingBox();
                    }
                    else if(code.equals(KEY_NUM))
                    {
                        displayLetter = "123";
                    }
                    else if(code.equals(KEY_ENTER))
                    {
                        displayLetter = "#8629";
                        Path p = new Path();
                        p.addRoundRect(0,0,key.width()*1.5f, key.height(),6.71f,6.71f,Path.Direction.CCW);
                        key.setPath(p);
                        key.sizeToBoundingBox();
                    }
                    else if(code.equals(KEY_BACK))
                    {
                        displayLetter = "#8592";
                    }
                    else if(code.equals(KEY_SPACE))
                    {
                        displayLetter = "";
                        value = " ";
                        Path p = new Path();
                        p.addRoundRect(0,0,key.width()*5f + buttonDistance * 4, key.height(),6.71f,6.71f,Path.Direction.CCW);
                        key.setPath(p);
                        key.sizeToBoundingBox();
                    }
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

                if(key.right() > maxRight)
                    maxRight = key.right();

                if(key.bottom() > maxBottom)
                    maxBottom = key.bottom();
                arr.add(key);

                OBLabel lowerCaseLabel = null;
                OBLabel upperCaseLabel = null;
                if(displayLetter != null && !displayLetter.equals(""))
                {
                    Typeface tf = typeface;
                    float fs = fontSize;
                    if (specialKey)
                    {
                        tf = specialtypeface;
                        //fs = specialfontSize;
                    }
                    lowerCaseLabel = labelForKey(key,tf,fs,displayLetter,!specialKey);
                    arr.add(lowerCaseLabel);
                    if(!specialKey && !unicodeChar)
                    {
                        upperCaseLabel = labelForKey(key,typeface,fontSize,displayLetter.toUpperCase(),false);
                        upperCaseLabel.hide();
                        arr.add(upperCaseLabel);
                    }
                }

                OBGroup buttonGroup = new OBGroup(arr);
                attachControl(buttonGroup);
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

                allKeys.put(code,buttonGroup);


                rowLayout.add(buttonGroup);
                left += key.width() + buttonDistance;
            }
            top += height + buttonDistance;
            layout.add(rowLayout);
        }

        float keyboardWidth = maxRight - minLeft;
        float keyboardRight = keyboardRect.right() -(keyboardRect.width() - keyboardWidth)/2.0f;

        float keyboardHeight = maxBottom - minTop;
        float keyboardTop = keyboardRect.top() +(keyboardRect.height() - keyboardHeight)/2.0f;
        float topNudge = keyboardTop - minTop;
        for(List<OBGroup> keyArray : layout)
        {
            float rightNudge = keyboardRight- keyArray.get(keyArray.size()-1).right();
            for(OBControl key : keyArray)
            {
                key.setTop(key.top() + topNudge);
                key.setRight(key.right() + rightNudge);
                key.setProperty("start_top",(key.top()));
            }
        }

        keyboardLayout = layout;
        keyboardKeys = allKeys;
    }

    public void setKeyboardCapitalMode(boolean capital)
    {
        lockScreen();
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
                        label.hide();
                        labelUpper.show();
                    }
                    else
                    {
                        label.show();
                        labelUpper.hide();
                    }
                }
            }
        }
        unlockScreen();
    }

    public OBLabel labelForKey(OBControl key, Typeface tf,float fontSize, String letter,boolean align)
    {
        OBLabel label = new OBLabel(letter,tf,fontSize);
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

    public void touchDownKeyWithSound(OBGroup key,boolean sound)
    {

        if(sound)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    String channel = String.format("special_%d",currentTapIndex);
                    OBAudioManager.audioManager.startPlaying("keyboard_click",channel);
                    /*OBAudioManager.audioManager.pausePlayingOnChannel(channel);
                    OBAudioManager.audioManager.setCurrentTime(0.15 onChannel:channel);
                    OBAudioManager.audioManager.playOnChannel(channel);*/
                    currentTapIndex =(currentTapIndex +1)%3;
                }
            });
        }
        highlightKey(key);
        triggerKeyValue(key);


    }

    public void triggerKeyValue(OBControl key)
    {
        String value = (String) key.propertyValue("value");
        String valueUpper = (String) key.propertyValue("value_upper");
        lockScreen();
        if(value.equals(KEY_BACK) )
        {
            int len = textBox.charLength();
            int preflen = line_prefix.length();
            if(len > preflen)
            {
                String eoln = String.format("\n%s",line_prefix);
                String ss = currentText.subSequence(len-(preflen+1),len).toString();
                if(len >= preflen + 1 && ss.equals(eoln))
                    textBox.deleteCharacters(len-(preflen+1),len);
                else
                    textBox.deleteCharacters(len-1,len);
            }

        }
        else if(value.equals(KEY_ENTER))
        {
            String eoln = String.format("\n%s",line_prefix);
            textBox.appendString(eoln);
        }
        else if(value.equals(KEY_SHIFT))
        {
            capitalMode = !capitalMode;
            setKeyboardCapitalMode(capitalMode);
            if(capitalMode)
                lastKey = null;
        }
        else
        {
            textBox.appendString((capitalMode && valueUpper != null) ? valueUpper : value);
            if(capitalMode)
            {
                capitalMode = false;
                setKeyboardCapitalMode(false);
                lowlightKey(keyboardKeys.get(KEY_SHIFT));
            }
        }

        refreshCursorAndTextBox();
        unlockScreen();
        textBox.scrollCursorToVisible();

        refreshCursorAndTextBox();

    }


    public void refreshCursorAndTextBox()
    {
        OBControl cursor = objectDict.get("cursor");
        if (cursor != null)
        {
            float x = textBox.xOfLastLine();
            float y = textBox.midYOfLastLine();
            PointF pt = textBoxGroup.convertPointFromControl(new PointF(x,y),textBox);
            cursor.setPosition(pt);
        }
    }
    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK  && !keyboardLocked)
        {
            if(textBoxGroup.frame().contains(pt.x, pt.y))
            {
                setStatus(STATUS_DOING_DEMO);
                dragMode = true;
                dragStartTouchY = pt.y;
                dragStartTextOffset = textBox.yOffset();
                lastAmt = dragStartTextOffset;
                lastAmtTime = System.currentTimeMillis() / 1000f;
                dragSpeed = 0;
                objectDict.get("cursor").hide();
                cursorShouldShow = false;
                setStatus(STATUS_DRAGGING);
                return;
            }

            if (!readOnly)
            {
                List<OBGroup>keys = new ArrayList<>(keyboardKeys.values());
                final OBGroup key = (OBGroup) finger(-1,-1, (List<OBControl>)(Object) keys,pt);
                if(key != null && key.isEnabled() )
                {
                    lockKeyboard();
                    lastKey = key;
                    dragMode = false;
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            touchDownKeyWithSound(key,true);
                            final long time = unlockKeyboard();
                            OBUtils.runOnOtherThreadDelayed(1,new OBUtils.RunLambda()
                            {
                                public void run() throws Exception
                                {
                                    try
                                    {
                                        if(time == statusTime  && !keyboardLocked && lastKey != null
                                                && !key.propertyValue("value").equals(KEY_SHIFT))
                                        {
                                            while(time == statusTime  && lastKey != null)
                                            {
                                                triggerKeyValue(key);
                                                waitForSecs(0.02f);
                                            }
                                        }
                                    }
                                    catch(Exception exception)
                                    {

                                    }
                                }
                            });
                        }
                    });

                }
                else if(finger(-1,-1,Arrays.asList(objectDict.get("button")),pt)  != null)
                {
                    final OBGroup themeButton = (OBGroup) objectDict.get("button");
                    setStatus(STATUS_DOING_DEMO);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            themeButton.highlight();
                            int index = themeNames.indexOf(currentTheme);
                            loadTheme(themeNames.get((index+1)%themeNames.size()));
                            refreshCursorAndTextBox();
                            themeButton.lowlight();
                            setStatus(STATUS_AWAITING_CLICK);
                        }
                    });
                }
                else if(finger(-1,-1,Arrays.asList(objectDict.get("button_save")),pt)  != null && textBox.charLength() > 4)
                {
                    final OBGroup saveButton = (OBGroup) objectDict.get("button_save");
                    setStatus(STATUS_DOING_DEMO);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                             {
                                                 @Override
                                                 public void run() throws Exception
                                                 {
                                                     saveButton.highlight();
                                                     saveCurrentText();
                                                 }
                                             }
                    );

                }
            }
        }
    }

    public Boolean scrollBox(float amt)
    {
        if (amt > 0)
            amt = 0;
        else
        {
            float topoff = textBox.topOffsetOfLastLine();
            if (-amt > topoff)
                amt = -topoff;
        }
        if (amt == textBox.yOffset())
            return false;
        textBox.setYOffset(amt);
        return true;
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
        {
            if(textBoxGroup.frame().contains(pt.x, pt.y))
            {
                lastAmt = textBox.yOffset();
                double thisTime = System.currentTimeMillis() / 1000.0;
                lockScreen();
                float yMoved = pt.y - dragStartTouchY;
                float amt = dragStartTextOffset + yMoved;
                scrollBox(amt);
                dragSpeed = (amt - lastAmt) / (thisTime - lastAmtTime);
                lastAmt = amt;
                lastAmtTime = thisTime;
                unlockScreen();
            }
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if(!dragMode && lastKey != null)
        {
            OBGroup temp = lastKey;
            lastKey = null;
            touchUpKey(temp);
            if(!keyboardLocked)
                unlockKeyboard();
        }
        else if(dragMode && status() == STATUS_DRAGGING )
        {
            final long time = setStatus(STATUS_AWAITING_CLICK);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    double speed = dragSpeed;
                    boolean finished = Math.abs(speed) < 0.0001;
                    while(!finished && time == statusTime  && !_aborting)
                    {

                        double amt = speed * 0.01f;
                        speed *= 0.97;
                        Boolean moved = scrollBox((float)(amt + textBox.yOffset()));
                        finished = !moved || Math.abs(speed) < 10;
                        if (!finished)
                            waitForSecs(0.01f);
                    }
                    refreshCursorAndTextBox();
                    cursorShouldShow = true;
                }
            });
        }
    }

    public void saveCurrentText()
    {
        Map<String,String>params = new HashMap<>();
        params.put("theme",currentTheme);
        params.put("font", (String) (themesData.get(currentTheme)).get("font"));
        params.put("text",textBox.textBuffer().toString());
        OBFatController fatController = (MainActivity.mainActivity.fatController);
        if (OCM_FatController.class.isInstance(fatController))
            ((OCM_FatController)fatController).savePlayZoneAssetForCurrentUserType(ASSET_TEXT,null,params);

        try
        {
            hideControls(".*gradient.*");
            OBControl bg = objectDict.get("text_box_bg");

            List<OBAnim> anims = new ArrayList<>();
            anims.add(OBAnim.scaleAnim(0.4f,textBoxGroup));
            anims.add(OBAnim.scaleAnim(0.4f,bg));
            OBAnimationGroup.runAnims(anims,0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
            waitForSecs(0.2f);
            anims.clear();
            anims.add(OBAnim.rotationAnim((float) Math.toRadians(-360f),textBoxGroup));
            anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(-0.2f,0.5f,boundsf()),textBoxGroup));
            anims.add(OBAnim.rotationAnim((float) Math.toRadians(-360f),bg));
            anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(-0.2f,0.5f,boundsf()),bg));

            OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN,null);
        }
        catch(Exception e)
        {

        }
        if(!_aborting)
            exitEvent();

    }

    public void touchUpKey(OBGroup key)
    {
        lowlightKey(key);
    }


    public void highlightKey(OBGroup key)
    {
        setKeyColour(key,highlightColour);
        key.setTop(key.top() + key.height()*0.015f);
    }

    public void setKeyColour(OBGroup key,int colour)
    {
        OBPath path =  (OBPath)key.objectDict.get("button");
        path.setFillColor(colour);
    }

    public void setKeyMode(OBGroup key,String mode)
    {
        OBPath targetButton = (OBPath) objectDict.get(String.format("button_%s",mode));

        OBPath button  = (OBPath)key.objectDict.get("button");
        button.setFillColor(targetButton.fillColor());
        button.setStrokeColor(targetButton.strokeColor());

        OBLabel label = (OBLabel)key.objectDict.get("label");
        String attr = (String)targetButton.attributes().get("font_colour");
        if(label != null && attr  != null)
        {
            label.setColour(OBUtils.colorFromRGBString((String) targetButton.attributes().get("font_colour")));
        }

    }

    public void lowlightKey(OBGroup key)
    {
        lockScreen();
        setKeyColour(key,Color.WHITE);
        key.setTop((Float)key.propertyValue("start_top"));
        unlockScreen();
    }

    public void disableKey(OBGroup key)
    {
        key.setTop((Float)key.propertyValue("start_top"));
        setKeyMode(key,"disabled");
        setKeyColour(key,Color.WHITE);
        key.disable();
    }

    public void enableKey(OBGroup key)
    {
        key.setTop((Float)key.propertyValue("start_top"));
        setKeyMode(key,(String)key.propertyValue("mode"));
        setKeyColour(key,Color.WHITE);
        key.enable();
    }


    public void skipTouchUp()
    {
        lastKey = null;
    }

/*    public void render (OBRenderer renderer)
    {
        renderLock.lock();
        renderBackground(renderer);
        TextureShaderProgram textureShader = (TextureShaderProgram) renderer.textureProgram;
        textureShader.useProgram();
        populateSortedAttachedControls();

        List<OBControl> clist = null;
        synchronized(sortedAttachedControls)
        {
            clist = sortedAttachedControls;
            for (OBControl control : clist)
            {
                if (control == textBox)
                {
                    Log.d("aa", "render: ");
                }
                if (!control.hidden())
                    control.render(renderer, this, renderer.projectionMatrix);
            }
        }

        renderLock.unlock();


    }*/
}
