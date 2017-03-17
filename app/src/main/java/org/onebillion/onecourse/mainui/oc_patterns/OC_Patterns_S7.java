package org.onebillion.onecourse.mainui.oc_patterns;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Patterns_S7 extends OC_Generic_Event
{
    Map items, types;
    Map <String, String> sounds;
    List<OBControl> createdObjects;
    OBGroup selectedItem;
    int colourPhase;

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        items = new HashMap();
        sounds = new HashMap();
        types = new HashMap();
        colourPhase = 1;
        //
        if (createdObjects != null)
        {
            for (OBControl control : createdObjects)
            {
                detachControl(control);
            }
        }
        //
        createdObjects = new ArrayList<>();
        for (OBGroup control : (List<OBGroup>) (Object) filterControls("obj.*"))
        {
            control.objectDict.get("hidden").hide();
        }
        //
        for (OBGroup control : (List<OBGroup>) (Object) filterControls("button.*"))
        {
            control.objectDict.get("hidden").hide();
            control.objectDict.get("selector").hide();
        }
        //
        for (int i = 1; i <= filterControls("obj.*").size(); i++)
        {
            String key = String.format("button_%d", i);
            OBGroup button = (OBGroup) objectDict.get(key);
            OBGroup control = (OBGroup) objectDict.get(String.format("obj_%d", i));
            control.setPosition(OC_Generic.copyPoint(button.position()));
            control.setScale(0.9f);
            //
            button.insertMember(control, 1, "object");
            items.put(key, control);
            sounds.put(key, (String) control.attributes().get("sound"));
        }
        //
        hideControls("playButton");
        selectedItem = null;
    }

    public void action_selectButton (OBGroup control) throws Exception
    {
        selectedItem = control;
        String soundEffect = sounds.get(selectedItem.attributes().get("id"));
        playSfxAudio(soundEffect, false);
        //
        lockScreen();
        for (OBGroup button : (List<OBGroup>) (Object) filterControls("button_.*"))
        {
            if (button.equals(control))
            {
                button.objectDict.get("selector").show();
            }
            else
            {
                button.objectDict.get("selector").hide();
            }
        }
        unlockScreen();
    }

    public boolean action_updateTypeInformation (OBControl box) throws Exception
    {
        String soundEffect = sounds.get(selectedItem.attributes().get("id"));
        String selectedType = (String) selectedItem.attributes().get("type");
        String boxType = (String) box.attributes().get("type");
        if (currentEvent().equals("7b") && !boxType.equals("1") && !action_areAllBoxesFilledForType("1"))
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("REPEAT", false);
            //
            return false;
        }
        else if (types.get(boxType) == null)
        {
            for (Object type : types.values())
            {
                if (type.equals(selectedType))
                {
                    if (currentEvent().equals("7b"))
                    {
                        gotItWrongWithSfx();
                        waitSFX();
                        waitForSecs(0.3f);
                        //
                        playAudioQueuedScene("INCORRECT", false);
                    }
                    return false;
                }
            }
            types.put(boxType, selectedType);
            box.setProperty("sound", soundEffect);
            return true;
        }
        else if (types.get(boxType).equals(selectedType))
        {
            box.setProperty("sound", soundEffect);
            return true;
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("REPEAT", false);
            return false;
        }
    }


    public void action_checkBox (OBControl target) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        if (selectedItem != null)
        {
            OBGroup item = (OBGroup) items.get(selectedItem.attributes().get("id"));
            OBGroup clone = (OBGroup) item.copy();
            //
            createdObjects.add(clone);
            //
            clone.setScale(item.scale() * selectedItem.scale());
            //
            if (action_updateTypeInformation(target))
            {
                String soundEffect = sounds.get(selectedItem.attributes().get("id"));
                playSfxAudio(soundEffect, false);
                //
                lockScreen();
                clone.setPosition(OC_Generic.copyPoint(target.getWorldPosition()));
                OC_Generic.sendObjectToTop(clone, this);
                attachControl(clone);
                clone.show();
                target.setProperty("object", clone);
                unlockScreen();
                //
                target.disable();
                //
                if (action_isFirstPhaseOver() && !currentEvent().equals("7a"))
                {
                    waitForSecs(0.3f);
                    //
                    showControls("playButton");
                    final long stat = setStatus(STATUS_AWAITING_CLICK);
                    if (currentEvent().equals("7b"))
                    {
                        OBUtils.runOnOtherThreadDelayed(7, new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                if (!statusChanged(stat))
                                {
                                    playAudioQueuedScene("REMINDER", false);
                                }
                            }
                        });
                        List audio = (List) ((Map) audioScenes.get(currentEvent())).get("PROMPT3");
                        setReplayAudio((List) ((Map) audioScenes.get(currentEvent())).get("REPEAT3"));
                        playAudioQueued(audio, false);
                    }
                }
                else
                {
                    revertStatusAndReplayAudio();
                }
            }
            else
            {
                revertStatusAndReplayAudio();
            }
        }
        else
        {
            revertStatusAndReplayAudio();
        }
    }


    public boolean action_areAllBoxesFilledForType (String type)
    {
        for (OBControl box : filterControls("box.*"))
        {
            if (box.attributes().get("type").equals(type) && box.isEnabled()) return false;

        }
        return true;

    }

    public boolean action_isFirstPhaseOver ()
    {
        for (OBControl box : filterControls("box.*"))
        {
            if (box.isEnabled()) return false;

        }
        return true;

    }

    public void action_playSequence () throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        for (OBControl control : sortedFilteredControls("box.*"))
        {
            String soundEffect = (String) control.propertyValue("sound");
            playSfxAudio(soundEffect, false);
            //
            OBControl object = (OBControl) control.propertyValue("object");
            float originalControlScale = control.scale();
            float originalObjectScale = object.scale();
            //
            lockScreen();
            control.setScale(originalControlScale * 1.1f);
            object.setScale(originalObjectScale * 1.1f);
            unlockScreen();
            //
            waitSFX();
            //
            lockScreen();
            control.setScale(originalControlScale);
            object.setScale(originalObjectScale);
            unlockScreen();
        }
        gotItRightBigTick(true);
        waitForSecs(0.3f);
        //
        playAudioQueuedScene("CORRECT", true);
        playAudioQueuedScene("FINAL", true);
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void demo7a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        action_playNextDemoSentence(false);  // Look at the boxes.
        OC_Generic.pointer_moveToObjectByName("box_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        OC_Generic.pointer_moveToObjectByName("box_8", -5, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // I will put a raindrop in each green box.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.6f, -10, 0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // Like this.
        OC_Generic.pointer_moveToObjectByName("button_1", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_selectButton((OBGroup) objectDict.get("button_1"));
        waitForSecs(0.3f);
        //
        List<String> boxes = Arrays.asList("box_1", "box_3", "box_5", "box_7");
        int count = 0;
        for (String boxName : boxes)
        {
            OBControl box = objectDict.get(boxName);
            OC_Generic.pointer_moveToObject(box, -25 + 5 * count, (count == 0 ? 0.6f : 0.3f), EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            action_checkBox(box);
            count++;
            //
            waitForSecs(0.1f);
        }
        //
        OC_Generic.pointer_moveToObjectByName("box_7", -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.6f);
        action_playNextDemoSentence(false); // Next, a drum for the blue boxes.
        OC_Generic.pointer_moveToObjectByName("button_2", -15, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_selectButton((OBGroup) objectDict.get("button_3"));
        waitForSecs(0.3f);
        //
        boxes = Arrays.asList("box_2", "box_4", "box_6", "box_8");
        count = 0;
        for (String boxName : boxes)
        {
            OBControl box = objectDict.get(boxName);
            OC_Generic.pointer_moveToObject(box, -25 + 5 * count, (count == 0 ? 0.6f : 0.3f), EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            action_checkBox(box);
            count++;
            //
            waitForSecs(0.1f);
        }
        OC_Generic.pointer_moveToObjectByName("box_8", -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.6f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public Object findButton (PointF pt)
    {
        return finger(0, 2, filterControls("button.*"), pt, true);

    }

    public Object findBox (PointF pt)
    {
        return finger(0, 2, filterControls("box.*"), pt, true);

    }

    public Object findPlayButton (PointF pt)
    {
        return finger(0, 2, filterControls("playButton"), pt);

    }

    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBGroup obj = (OBGroup) findButton(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        action_selectButton(obj);
                    }
                });
            }
            else
            {
                final OBControl obj1 = (OBControl) findBox(pt);
                if (obj1 != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            action_checkBox(obj1);
                        }
                    });
                }
                else
                {
                    final OBGroup obj2 = (OBGroup) findPlayButton(pt);
                    if (obj2 != null)
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                action_playSequence();
                            }
                        });
                    }
                }
            }
        }
    }



}
