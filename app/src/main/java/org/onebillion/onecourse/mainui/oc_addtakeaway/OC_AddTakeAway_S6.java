package org.onebillion.onecourse.mainui.oc_addtakeaway;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Tracing;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.UPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 04/05/2017.
 */

public class OC_AddTakeAway_S6 extends OC_Generic_Tracing
{
    List<OBLabel> equation;

    public OC_AddTakeAway_S6 ()
    {
        super(false);
    }


    public void fin ()
    {
        goToCard(OC_AddTakeAway_S6f.class, "event6", false);
    }


    public void prepare ()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master1");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        doVisual(currentEvent());
    }

    public void start ()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                setStatus(STATUS_WAITING_FOR_TRACE);
                final long startStatusTime = statusTime;
                try
                {
                    if (!performSel("demo", currentEvent()))
                    {
                        playAudioQueuedScene("DEMO", 0.3f, true);
                        doAudio(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                OBUtils.runOnOtherThreadDelayed(3, new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        if (!statusChanged(startStatusTime))
                        {
                            playAudioQueuedScene("REMIND", 0.3f, false);
                        }
                    }
                });
            }
        });
    }

    public void doAudio (String scene) throws Exception
    {
        List audio = (List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT");
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT"));
        playAudioQueued(audio, false);
    }

    public void setSceneXX (String scene)
    {
        List<OBControl> oldControls = filterControls(".*");
        super.setSceneXX(currentEvent());
        if (eventAttributes.get("redraw").equals("true"))
        {
            for (OBControl control : oldControls)
            {
                detachControl(control);
                String controlID = (String) control.attributes().get("id");
                if (objectDict.get(controlID) != null && objectDict.get(controlID).equals(control))
                {
                    objectDict.remove(controlID);
                }
            }
            List<OBControl> controls = sortedFilteredControls("label.*");
            equation = new ArrayList<>();
            for (OBControl control : controls)
            {
                String text = (String) control.attributes().get("text");
                if (text != null && text.length() != 0)
                {
                    OBLabel label = action_createLabelForControl(control);
                    String colour = (String) control.attributes().get("colour");
                    label.setColour(OBUtils.colorFromRGBString(colour));
                    label.setProperty("colour", colour);
                    equation.add(label);
                    objectDict.put(String.format("%s_label", control.attributes().get("id")), label);
                    if (control.parent != null)
                    {
                        OBGroup parent = control.parent;
                        parent.insertMember(label, 0, String.format("%s_label", control.attributes().get("id")));
                        label.setPosition(OC_Generic.copyPoint(control.position()));
                    }
                    label.hide();
                }
                control.hide();
            }
        }
        OC_Generic.colourObjectsWithScheme(this);
        //
        currentTry = 1;
        //
        tracing_reset();
        tracing_setup();
    }


    public void doMainXX () throws Exception
    {
        playAudioScene("DEMO", 0, true);
        doAudio(currentEvent());
        tracing_position_arrow();
        setStatus(STATUS_WAITING_FOR_TRACE);
    }

    public void setScene6a ()
    {
        setSceneXX(currentEvent());
        //
        OBGroup group1 = new OBGroup(Arrays.asList(objectDict.get("obj_1"), objectDict.get("obj_2"), objectDict.get("obj_5")));
        objectDict.put("group_1", group1);
        attachControl(group1);
        //
        OBGroup group2 = new OBGroup(Arrays.asList(objectDict.get("obj_3"), objectDict.get("obj_4")));
        objectDict.put("group_2", group2);
        attachControl(group2);
        //
        hideControls("group_.*");
    }

    public void demo6a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look.;
        OC_Generic.pointer_moveToObjectByName("group_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playSfxAudio("add_object", false);
        //
        lockScreen();
        showControls("group_1");
        equation.get(0).show();
        unlockScreen();
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, true); // Three counters.;
        waitForSecs(0.5f);
        //
        playAudioScene("DEMO", 2, false); // I add two counters.;
        OC_Generic.pointer_moveToObjectByName("group_2", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        playSfxAudio("add_object", false);
        //
        lockScreen();
        showControls("group_2");
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        waitAudio();
        waitForSecs(0.5f);
        //
        playAudioScene("DEMO", 3, false); // Three add two equals five.;
        OC_Generic.pointer_moveToObject(equation.get(0), -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        playSfxAudio("add_object", false);
        //
        lockScreen();
        equation.get(3).show();
        equation.get(4).show();
        unlockScreen();
        OC_Generic.pointer_moveToObject(equation.get(4), 0, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.5f);
        //
        playAudioScene("DEMO", 4, false); // This means ADD.;
        OC_Generic.pointer_moveToObject(equation.get(1), -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.5f);
        //
        playAudioScene("DEMO", 5, false); // This means EQUALS.;
        OC_Generic.pointer_moveToObject(equation.get(3), -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.5f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void setScene6b ()
    {
        setSceneXX(currentEvent());
        for (OBControl control : equation)
        {
            control.show();
        }

    }


    public void demo6b () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(true);  // Look how we write ADD
        //
        pointer_demoTrace(true); // Add
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        lockScreen();
        tracing_reset();
        tracing_setup();
        tracing_position_arrow();
        unlockScreen();
        //
        doAudio(currentEvent());
        setStatus(STATUS_WAITING_FOR_TRACE);
    }


    public void setScene6c ()
    {
        setSceneXX(currentEvent());
        for (OBControl control : equation)
        {
            control.show();
        }
    }

    public void demo6c () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(true);  // Look how we write Equals
        //
        pointer_demoTrace(true); // Equals
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        lockScreen();
        tracing_reset();
        tracing_setup();
        tracing_position_arrow();
        unlockScreen();
        //
        doAudio(currentEvent());
        setStatus(STATUS_WAITING_FOR_TRACE);
    }


    public void setScene6d ()
    {
        setSceneXX(currentEvent());
        hideControls("obj.*");
    }

    public void demo6d () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // This time there are FOUR counters.;
        OC_Generic.pointer_moveToObjectByName("obj_2", -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), false, this);
        playSfxAudio("add_object", false);
        //
        lockScreen();
        showControls("obj.*");
        equation.get(0).show();
        unlockScreen();
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // I take away three.;
        OBControl obj = objectDict.get("obj_1");
        OC_Generic.pointer_moveToObject(obj, -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("remove_object", false);
        obj.hide();
        waitForSecs(0.3);
        //
        obj = objectDict.get("obj_3");
        OC_Generic.pointer_moveToObject(obj, -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("remove_object", false);
        obj.hide();
        waitForSecs(0.3);
        //
        obj = objectDict.get("obj_4");
        OC_Generic.pointer_moveToObject(obj, -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("remove_object", false);
        obj.hide();
        waitSFX();
        waitForSecs(0.3f);
        //
        playSfxAudio("add_object", false);
        //
        lockScreen();
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(equation.get(0), -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        playAudioScene("DEMO", 2, false);
        playSfxAudio("add_object", false);
        //
        lockScreen();
        equation.get(3).show();
        equation.get(4).show();
        unlockScreen();
        //
        OC_Generic.pointer_moveToObject(equation.get(4), 0, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false); // This means TAKE AWAY.;
        OC_Generic.pointer_moveToObject(equation.get(1), -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void setScene6e ()
    {
        setSceneXX(currentEvent());
        for (OBControl control : equation)
        {
            control.show();
        }
    }

    public void demo6e () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(true);  // Look how we write take away.
        //
        pointer_demoTrace(true); // Take away
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        lockScreen();
        tracing_reset();
        tracing_setup();
        tracing_position_arrow();
        unlockScreen();
        //
        doAudio(currentEvent());
        setStatus(STATUS_WAITING_FOR_TRACE);
    }



    public void action_cleanupTracing ()
    {
        lockScreen();
        tracing_reset();
        tracing_setup();
        tracing_position_arrow();
        unlockScreen();
        //
        setStatus(STATUS_WAITING_FOR_TRACE);
    }



    @Override
    public void tracing_setup ()
    {
        pathColour = Color.BLACK;
        path1 = (OBGroup) objectDict.get("trace_p1");
        path2 = (OBGroup) objectDict.get("trace_p2");
        //
        dash1 = (OBImage) objectDict.get("dash_p1");
        //
        uPaths = new ArrayList<>();
        uPaths.addAll(tracing_processDigit(path1));
        uPaths.addAll(tracing_processDigit(path2));
        //
        List<OBGroup> spArray = new ArrayList<>();
        spArray.addAll(tracing_subpathControlsFromPath("trace_p1"));
        spArray.addAll(tracing_subpathControlsFromPath("trace_p2"));
        //
        List<OBPath> paths = new ArrayList<>();
        if (path1 != null)
        {
            paths.addAll((List<OBPath>) (Object) path1.filterMembers("p.*"));
            path1.hide();
        }
        if (path2 != null)
        {
            paths.addAll((List<OBPath>) (Object) path2.filterMembers("p.*"));
            path2.hide();
        }
        for (OBPath path : paths)
        {
            path.setStrokeEnd(0.0f);
        }
        //
        subPaths = spArray;
        for (OBControl c : subPaths)
        {
            c.hide();
        }
    }

}
