package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 07/07/2017.
 */

public class OC_PlayZoneTypewrite extends OC_SectionController
{
    static final String MODE_NORMAL= "normal",MODE_SPECIAL = "special", MODE_HIGHLIGHT = "highlight";
    static final String KEY_ENTER= "s_enter", KEY_SPACE = "s_space", KEY_SHIFT = "s_shift", KEY_NUM = "s_num", KEY_BACK = "s_back";
    static final float TICK_VALUE2 = 0.0025f;

    OBGroup lastKey;
    boolean keyboardLocked;
    String currentTheme;
    int highlightColour;
    Map<String,OBGroup> keyboardKeys;
    List<List<OBGroup>> keyboardLayout;
    List<String> themeNames;
    Map<String,Map> themesData;
    OBLabel textBox;
    String currentText;
    //TextFace currentFont;
    Map displayStringAttributes;
    OBGroup textBoxGroup;
    float currentTextBoxHeight;
    float textBoxTopLimit, textBoxBottomLimit;
    PointF lastTextBoxLoc;
    boolean dragMode, capitalMode;
    int currentTapIndex;

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("master");
        themesData = new HashMap();
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);

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

        String[] themea = ((String)eventAttributes.get("themes")).split(",");;
        themeNames = Arrays.asList(themea);
        loadTheme(themeNames.get(1));


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

        OBControl cursor = objectDict.get("cursor");
        cursor.setPosition(textBox.position());

        objectDict.get("button").setZPosition(48);
        objectDict.get("button_save").setZPosition(48);
        objectDict.get("text_box_bg").setZPosition(49);

       /* textBoxGroup = OBGroup.alloc().initWithMembers(@textBox,.cursor() );
        textBoxGroup.setPosition((OBControl*)objectDict.get("text_box_bg").position());
        textBoxGroup.setBounds(RectFMake(0, 0, textBoxGroup.bounds.width() + applyGraphicScale(6), textBoxGroup.bounds.height()));
        textBoxGroup.width += applyGraphicScale(6);
        attachControl(textBoxGroup);
        textBoxGroup.setZPosition(50);
        textBoxGroup.setMasksToBounds(true);
        textBoxTopLimit = gradientTop.height();
        textBoxBottomLimit =textBox.bottom() - gradientBottom.height();
        textBox.setTop(textBoxTopLimit);
        refreshCursorAndTextBox();

        textBox.setString((String*)currentText);
        cursor.setProperty("start_loc",new PointF(cursor.position().x,cursor.position().y));

        currentTextBoxHeight = textBox.height();*/
        capitalMode = false;
        setSceneXX(currentEvent());
        currentTapIndex = 0;
    }

    public void start()
    {
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
        lockScreen();
        Map themeData = themesData.get(name);
        if(themeData == null)
        {
            Map dictData = new HashMap();
            List<OBControl> themeFiles = loadEvent(name);
            for(OBControl con : themeFiles)
            {
                con.setRasterScale(con.scale());
                //con.setShouldRasterize(true);
            }
            dictData.put("controls",themeFiles);
            dictData.put("font",eventAttributes.get("font"));
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
        /*
        NSMutableParagraphStyle *parStyle = [NSMutableParagraphStyle.alloc() init];

        parStyle.setLineBreakMode(NSLineBreakByWordWrapping);

        currentFont = UIFont.fontWithName(themeData"font".()  size:applyGraphicScale(60));
        OBControl cursor = objectDict.get("cursor");
        cursor.setHeight(currentFont.capHeight*1.2);

        displayStringAttributes = @{NSFontAttributeName:currentFont,
            NSForegroundColorAttributeName:(Object)[int.blackColor() CGColor],
        NSParagraphStyleAttributeName:parStyle
    };

        currentText = NSMutableAttributedString.alloc().initWithString(currentText == null ? "\t\t" : currentText.mutableString attributes:displayStringAttributes);
        textBox.setString((String*)currentText);
        textBox.setTop(textBoxTopLimit);
        refreshCursorAndTextBox();
        */
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
                    displayLetter =  (new Integer(displayLetter.substring(1))).toString();
                    if(!specialKey)
                        value = displayLetter;
                }

                key.setTop(top);
                key.setLeft(left);
                key.setZPosition(1);
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
                        fs = specialfontSize;
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

}
