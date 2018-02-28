package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_WordsEvent;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.UPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 30/06/16.
 */
public class OC_Alpha extends OC_Generic_WordsEvent
{
    float yinc;
    int boxesPerRow;
    List<String> letters, targetLetters, templateColours_fill, templateColours_stroke;
    OBPath ropePath;
    List<OBPath> boxes, strokes;
    List<OBLabel> labels;
    List<OBGroup> templateBacks, backs;
    int boxHighColour, boxLowColour;


    public OC_Alpha ()
    {
        super();
    }


    public void layOutBoxes ()
    {
        templateBacks = (List<OBGroup>) (Object) OBUtils.randomlySortedArray(filterControls("back_.*"));
        templateColours_fill = new ArrayList<String>(Arrays.asList(eventAttributes.get("fill").split(";")));
        templateColours_stroke = new ArrayList<String>(Arrays.asList(eventAttributes.get("stroke").split(";")));
        int templateIdx = 0;
        int colourIdx = 0;
        //
        boxLowColour = ((OBPath) objectDict.get("_box2")).fillColor();
        boxHighColour = ((OBPath) objectDict.get("boxhiswatch")).fillColor();
        //
        OBPath c = (OBPath) objectDict.get("_box1");
        PointF topLeft = c.position();
        //
        c = (OBPath) objectDict.get("_box2");
        PointF bottomRight = c.position();
        //
        int i = 0, row = 0, col = 0;
        float y = topLeft.y;
        yinc = bottomRight.y - y;
        //
        boxes = new ArrayList<OBPath>();
        backs = new ArrayList<OBGroup>();
        strokes = new ArrayList<OBPath>();
        //
        while (i < letters.size())
        {
            OBPath box = (OBPath) c.copy();
            float x = OB_Maths.interpolateVal(topLeft.x, bottomRight.x, col * 1.0f / (float) (boxesPerRow - 1));
            box.setPosition(x, y);
            attachControl(box);
            boxes.add(box);
            //
            OBPath stroke = (OBPath) box.copy();
            stroke.setZPosition(box.zPosition() - 0.001f);
            stroke.setLineWidth(applyGraphicScale(8));
            stroke.setStrokeColor(boxHighColour);
            attachControl(stroke);
            stroke.sizeToBoundingBoxIncludingStroke();
            stroke.hide();
            strokes.add(stroke);
            //
            i++;
            col++;
            if (col >= boxesPerRow)
            {
                row++;
                col = 0;
                y += yinc;
            }
            box.show();
            //
            OBGroup backTemplate = templateBacks.get(templateIdx);
            if (++templateIdx == templateBacks.size())
            {
                templateIdx = 0;
                templateBacks = OBUtils.randomlySortedArray(templateBacks);
            }
            OBGroup back = (OBGroup) backTemplate.copy();
            back.setPosition(box.position());
            //
            box.setFrame(back.frame());
            box.sizeToBoundingBoxIncludingStroke();
            //
            OBPath fill = (OBPath) back.objectDict.get("colour");
            fill.setFillColor(OBUtils.colorFromRGBString(templateColours_fill.get(colourIdx)));
            stroke = (OBPath) back.objectDict.get("frame");
            stroke.setFillColor(OBUtils.colorFromRGBString(templateColours_stroke.get(colourIdx)));
            stroke.setProperty("stroke", stroke.fillColor());
            //
            if (++colourIdx == templateColours_fill.size()) colourIdx = 0;
            //
            backTemplate.hide();
            attachControl(back);
            back.show();
            backs.add(back);
        }
    }


    public void layOutRope ()
    {
        OBControl mainRect = objectDict.get("mainrect");
        OBControl cornerRect = objectDict.get("cornerrect");
        float w = cornerRect.width();
        float r = mainRect.right();
        float l = mainRect.left();
        Boolean finished = false;
        int startBox = 0;
        Path p = new Path();
        PointF position = OC_Generic.copyPoint(boxes.get(0).position());
        p.moveTo(position.x, position.y);
        while (!finished)
        {
            int lastBox = startBox + boxesPerRow - 1;
            if (lastBox >= boxes.size())
            {
                lastBox = boxes.size() - 1;
                if (lastBox <= startBox)
                {
                    break;
                }
            }
            position = OC_Generic.copyPoint(boxes.get(lastBox).position());
            p.lineTo(position.x, position.y);
            if (lastBox == boxes.size() - 1) break;
            //
            float y = boxes.get(lastBox).position().y;
            float nexty = y + yinc * 0.5f;
            float nx = mainRect.right() - w;
            p.lineTo(nx, y);
            //
            float toy = y + w;
            p.cubicTo(nx + w * 0.5f, y, r, toy - w * 0.5f, r, toy);
            //
            toy = nexty - w;
            p.lineTo(r, toy);
            p.cubicTo(r, toy + w * 0.5f, r - w * 0.5f, nexty, r - w, nexty);
            p.lineTo(l + w, nexty);
            //
            toy = nexty + w;
            p.cubicTo(l + w * 0.5f, nexty, l, nexty + w * 0.5f, l, toy);
            //
            nexty = nexty + yinc * 0.5f;
            p.lineTo(l, nexty - w);
            p.cubicTo(l, nexty - w * 0.5f, l + w * 0.5f, nexty, l + w, nexty);
            //
            startBox = lastBox + 1;
        }
        OBPath rope = (OBPath) objectDict.get("ropestart");
        //
        if (ropePath != null)
        {
            detachControl(ropePath);
        }
        ropePath = new OBPath(p);
        ropePath.sizeToBox(new RectF(bounds()));
        ropePath.setLineWidth(rope.lineWidth());
        ropePath.setStrokeColor(rope.strokeColor());
        ropePath.setZPosition(rope.zPosition());
        ropePath.show();
        ropePath.setNeedsRetexture();
        attachControl(ropePath);
        objectDict.put("path", ropePath);
        //
        if (rope.propertyValue("finalPosition") != null)
        {
            rope.setPosition((PointF)rope.propertyValue("finalPosition"));
        }
        else
        {
            UPath deconPath = deconstructedPath("mastera", "ropestart");
            PointF firstPoint = deconPath.subPaths.get(0).elements.get(0).pt0;
            position = OC_Generic.copyPoint(boxes.get(0).position());
            PointF diff = OB_Maths.DiffPoints(position, firstPoint);
            PointF newRopePosition = OB_Maths.AddPoints(rope.position(), diff);
            rope.setPosition(newRopePosition);
            //
            rope.setProperty("originalPosition", rope.getWorldPosition());
            rope.setProperty("finalPosition", rope.getWorldPosition());
        }
        //
        rope = (OBPath) objectDict.get("ropeend");
        if (rope.propertyValue("finalPosition") != null)
        {
            rope.setPosition((PointF)rope.propertyValue("finalPosition"));
        }
        else
        {
            UPath deconPath = deconstructedPath("mastera", "ropeend");
            PointF firstPoint = deconPath.subPaths.get(0).elements.get(0).pt0;
            position = OC_Generic.copyPoint(boxes.get(boxes.size() - 1).position());
            PointF diff = OB_Maths.DiffPoints(position, firstPoint);
            PointF newRopePosition = OB_Maths.AddPoints(rope.position(), diff);
            rope.setPosition(newRopePosition);
            //
            rope.setProperty("originalPosition", rope.getWorldPosition());
            rope.setProperty("finalPosition", rope.getWorldPosition());
        }
    }


    public void layOutLabels ()
    {
        Typeface tf = OBUtils.standardTypeFace();
        if (labels != null && labels.size() > 0)
        {
            for (OBLabel label : labels)
            {
                detachControl(label);
            }
        }
        labels = new ArrayList<OBLabel>();
        //
        float fontSize = applyGraphicScale(textSize) * 0.85f;
        //
        for (int i = 0; i < letters.size(); i++)
        {
            String text = letters.get(i);
            //
            if (text.toUpperCase().equals(text))
            {
                text = OC_Generic.toTitleCase(text);
                fontSize = applyGraphicScale(textSize) * 0.75f;
            }
            //
            OBLabel label = new OBLabel(text, tf, fontSize);
            label.setColour(Color.BLACK);
            label.setPosition(boxes.get(i).position());
            label.setZPosition(5.0f);
            attachControl(label);
            labels.add(label);
            label.hide();
        }
    }


    public void miscSetup ()
    {
        letters = parameters.get("letters") == null ? new ArrayList() : new ArrayList(Arrays.asList(parameters.get("letters").split(",")));
        targetLetters = parameters.get("targetletters") == null ? new ArrayList() : new ArrayList(Arrays.asList(parameters.get("targetletters").split(",")));
        boxesPerRow = Integer.parseInt(eventAttributes.get("boxesperrow"));
        textSize = Float.parseFloat(eventAttributes.get("textsize"));
        needDemo = (parameters.get("demo") != null && parameters.get("demo").equals("true"));
        currNo = 0;
        layOutBoxes();
        layOutLabels();
    }

    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
        //
        targets = (List<OBControl>) (Object) boxes;
        //
        layOutRope();
        //
        hideControls("_box.*");
    }


    public void doAudio (String scene) throws Exception
    {
        String letterAudio = String.format("alph_%s", targetLetters.get(currNo));
        List reparr = new ArrayList();
        reparr.addAll(currentAudio("PROMPT.REPEAT"));
        reparr.add(300);
        reparr.add(letterAudio);
        setReplayAudio(reparr);
        playSceneAudio("PROMPT", true);
        waitForSecs(0.3);
        playAudioQueued(new ArrayList(Arrays.asList(letterAudio)), false);
    }


    public void doMainXX () throws Exception
    {
        doAudio(currentEvent());
    }


    public void endBody ()
    {
        final String letterAudio = String.format("alph_%s", targetLetters.get(currNo));
        //
        List reminderAudio = currentAudio("PROMPT.REMINDER");
        if (reminderAudio != null)
        {
            try
            {
                final long stTime = statusTime;
                waitForSecs(0.3);
                waitAudio();
                if (!statusChanged(stTime))
                {
                    reprompt(stTime, reminderAudio, 6.0f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            if (!statusChanged(stTime))
                            {
                                playAudioQueued(new ArrayList(Arrays.asList(letterAudio)), false);
                            }
                        }
                    });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public void showLetters () throws Exception
    {
        String fn = ((List<String>) ((Map<String, Object>) audioScenes.get("sfx")).get("fullaplhin")).get(0);

        playSfxAudio("fullalphin", false);
        double dur = OBAudioManager.audioManager.durationSFX();
        dur = (dur == 0.0) ? 0.3f : dur / (float) letters.size();
        //
        for (OBLabel l : labels)
        {
            l.show();
            waitForSecs(dur);
        }
    }


    public void highlightBox (OBPath box, Boolean high)
    {
        int col = high ? boxHighColour : boxLowColour;
        box.setFillColor(col);
    }


    public void nextObj ()
    {
        currNo++;
        nextScene();
    }


    public void checkTarget (OBPath targ, PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        highlightBox(targ, true);
        try
        {
            String letterAudio = String.format("alph_%s", targetLetters.get(currNo));
            int idx = boxes.indexOf(targ);
            if (letters.get(idx).equals(targetLetters.get(currNo)))
            {
                playSfxAudio("click", true);
                waitForSecs(0.4);
                playAudioQueued(new ArrayList(Arrays.asList(letterAudio)), true);
                waitForSecs(0.4);
                gotItRightBigTick(true);
                highlightBox(targ, false);
                nextObj();
            }
            else
            {
                gotItWrongWithSfx();
                waitSFX();
                setReplayAudio(saverep);
                highlightBox(targ, false);
                playAudioQueued(new ArrayList(Arrays.asList(letterAudio)), true);
                setStatus(saveStatus);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBPath findTarget (PointF pt)
    {
        return (OBPath) finger(-1, 2, targets, pt);
    }

    @Override
    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            target = findTarget(pt);
            if (target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkTarget((OBPath) target, pt);
                    }
                });
            }
        }
    }

}
