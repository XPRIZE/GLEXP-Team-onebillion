package org.onebillion.xprz.mainui.x_shapes;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 22/06/16.
 */
public class X_Shapes_S2 extends XPRZ_Generic_SelectCorrectObject
{
    String eventType;
    List<OBControl> demosDone;
    List<OBControl> shownShapes;
    int correctAnswerCount;


    public String action_getObjectPrefix()
    {
        return "obj";
    }


    public void action_prepareScene(String scene, Boolean redraw)
    {
        demosDone = new ArrayList<OBControl>();
        eventType = eventAttributes.get("type");
        correctAnswerCount = 0;
        //
        if (eventType.equals("choose"))
        {
            for (OBControl control : filterControls("obj.*"))
            {
                control.enable();
                control.setOpacity(1);
            }
        }
        else if (eventType.equals("showFill"))
        {
            if (shownShapes == null) shownShapes = new ArrayList<OBControl>();
            //
            for (OBControl control: filterControls("obj.*"))
            {
                OBPath path = (OBPath) control;
                //
                if (!shownShapes.contains(path))
                {
                    if (control.propertyValue("colour") == null)
                    {
                        int fillColour = path.fillColor();
                        path.setProperty("colour", fillColour);
                        path.setFillColor(Color.WHITE);
                    }
                }
            }
        }
        else if (eventType.equals("reveal"))
        {
            if (shownShapes != null)
            {
                for (OBControl control : shownShapes)
                {
                    detachControl(control);
                }
            }
            shownShapes = new ArrayList<OBControl>();
            //
            for (OBControl control : filterControls("obj.*"))
            {
                control.enable();
            }
        }
    }


    public void action_toggleShape(OBPath shape, Boolean value)
    {
        if (value)
        {
            OBPath clone = (OBPath) shape.copy();
            clone.setLineWidth(applyGraphicScale(10));
            clone.setFillColor(OBUtils.colorFromRGBString("251,9,250"));
            shape.setProperty("clone", clone);
            clone.disable();
            //
            XPRZ_Generic.sendObjectToTop(clone, this);
            attachControl(clone);
            clone.show();
            //
            shownShapes.add(clone);
        }
        else
        {
            OBPath clone = (OBPath) shape.propertyValue("clone");
            if (clone != null)
            {
                clone.hide();
                detachControl(clone);
                //
                shownShapes.remove(clone);
            }
        }

    }


    public void demo2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, false); // Touch each shape, to hear its name.
        setReplayAudioScene(currentEvent(), "REPEAT");
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demo2l() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(true); // Now look at this picture.
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // The sun is like a circle.
        waitForSecs(0.3);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void demo_circle() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO2", 0, false); // A circle. It is round.
        //
        OBPath circle = (OBPath) objectDict.get("obj_circle");
        OBPath clone = (OBPath) circle.copy();
        OBPath path = (OBPath) circle.copy();
        path.setLineWidth(applyGraphicScale(24));
        //
        OBGroup group = new OBGroup((List<OBControl>) (Object) Arrays.asList(path,clone));
        group.maskControl = clone;
        //
        XPRZ_Generic.sendObjectToTop(group, this);
        attachControl(group);
        group.show();
        //
        waitAudio();
        //
        group.hide();
        detachControl(group);
        //
        waitForSecs(0.3);
    }



    public void demo_oval() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO3", 0, false); // An oval. It is like a stretched circle.
        //
        OBPath oval = (OBPath) objectDict.get("obj_oval");
        OBPath clone = (OBPath) oval.copy();
        OBPath path = (OBPath) oval.copy();
        path.setLineWidth(applyGraphicScale(24));
        //
        OBGroup group = new OBGroup((List<OBControl>) (Object) Arrays.asList(path, clone));
        group.maskControl = clone;
        //
        XPRZ_Generic.sendObjectToTop(group, this);
        attachControl(group);
        group.show();
        //
        waitAudio();
        //
        group.hide();
        detachControl(group);
        //
        waitForSecs(0.3);
    }


//    public void getSubPaths(Path path)
//    {
//        List<Path> segmentPath(Path path, float segmentLength, float scale, float dx, float dy)
//        {
//        PathMeasure pm = new PathMeasure(path, false);
//        float length = pm.getLength();
//
//        float start = 0;
//        float delta = segmentLength;
//
//        List<Path> segments = new ArrayList<Path>();
//        while (start <= length) {
//            float end = start + delta;
//            if (end > length) {
//                end = length;
//            }
//
//            Path segment = new Path();
//            pm.getSegment(start, end, segment, true);
//
//            segments.add(segment);
//            start += delta;
//        }
//
//        return segments;
//    }
//    }


    public void demo_triangle() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO4", 0, true); // A triangle. It has THREE sides.
        waitForSecs(0.3f);
        //
        OBPath triangle = (OBPath) objectDict.get("obj_triangle");
        PathMeasure measure = new PathMeasure(triangle.path(), false);
        int pathCount = 0;
        do
        {
            Path temp = new Path();
            measure.getSegment(0,measure.getLength(), temp, true);
            pathCount++;
            PathMeasure pm = new PathMeasure(temp, false);
            float measure_length = measure.getLength();
            float pm_length = pm.getLength();
            Log.i("   length of Path", temp.toString());
        } while (measure.nextContour());
        //

//        measure.
//        Path upath = triangle.path();
//        upath.su
//        UPath *upath = DeconstructedPath(triangle.path);
//        USubPath *subPath = upath.subPaths()0.();a
////
//        for (int i = 0; i < 3; i++)
//        {
//            ULine *line = subPath.elementsi.();
//            UIBezierPath *bezier = [UIBezierPath.alloc() init];
//            bezier.moveToPoint(line.pt0);
//            bezier.addLineToPoint(line.pt1);
//            OBPath path = [OBPath.alloc() init];
//            path.path = bezier.CGPath();
//            path.frame = triangle.frame;
//            path.strokeColor = UIColor.blackColor();
//            path.lineWidth = applyGraphicScale(24);
//            //
//            OBPath clone = triangle.copy();
//            OBGroup group = [OBGroup.alloc() initWithMembers:@clone,.path()];
//            group.maskControl = clone;
//            //
//            playSceneAudio("DEMO4",false); // One. Two. Three
//            sendObjectToTop(group);
//            attachControl(group);
//            group.show();
//            waitAudio();
//            //
//            group.hide();
//            detachControl(group);
//            waitForSecs(0.3f);
//        }

    }


    public void checkTarget(OBControl targ)
    {
        final OBControl target = targ;
        //
        setStatus(STATUS_DOING_DEMO);
        //
        try
        {
            if (eventType.equals("demo"))
            {
                if (!demosDone.contains(target))
                {
                    demosDone.add(target);
                    List<OBAnim> anims = new ArrayList<OBAnim>();
                    for (OBControl control : filterControls("obj.*"))
                    {
                        anims.add(OBAnim.opacityAnim(0.3f, control));
                    }
                    OBAnimationGroup.runAnims(anims, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    //
                    String demo = ((String) target.attributes().get("id")).replace("obj", "demo");
                    performSel(demo, "");
                    //
                    anims.clear();
                    for (OBControl control : filterControls("obj.*"))
                    {
                        if (demosDone.contains(control))
                        {
                            anims.add(OBAnim.opacityAnim(0.3f, control));
                            control.disable();
                        }
                        else
                        {
                            anims.add(OBAnim.opacityAnim(1.0f, control));
                            control.enable();
                        }
                    }
                    OBAnimationGroup.runAnims(anims, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    //
                    List<OBControl> shapes = filterControls("obj.*");
                    if (shapes.size() == demosDone.size())
                    {
                        anims.clear();
                        for (OBControl control : filterControls("obj.*"))
                        {
                            anims.add(OBAnim.opacityAnim(1.0f, control));
                        }
                    }
                    OBAnimationGroup.runAnims(anims, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    //
                    nextScene();
                    return;
                }
            }
            else if (eventType.equals("choose"))
            {
                action_highlight(target);
                //
                if (target.equals(action_getCorrectAnswer()))
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.3);
                    //
                    playAudioQueuedScene(currentEvent(), "CORRECT", true);
                    waitForSecs(0.3);
                    //
                    playAudioQueuedScene(currentEvent(), "FINAL", true);
                    action_lowlight(targ);
                    //
                    nextScene();
                    return;
                }
                else
                {
                    gotItWrongWithSfx();
                    //
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            action_lowlight(target);
                            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                        }
                    });
                }
            }
            else if (eventType.equals("showFill"))
            {
                action_highlight(target);
                //
                if (target.equals(action_getCorrectAnswer()))
                {
                    OBPath path = (OBPath) target;
                    path.setFillColor((Integer) path.propertyValue("colour"));
                    shownShapes.add(path);
                    //
                    gotItRightBigTick(true);
                    waitForSecs(0.3);
                    //
                    playAudioQueuedScene(currentEvent(), "CORRECT", true);
                    waitForSecs(0.3);
                    //
                    playAudioQueuedScene(currentEvent(), "FINAL", true);
                    action_lowlight(target);
                    //
                    nextScene();
                    return;
                }
                else
                {
                    gotItWrongWithSfx();
                    //
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            action_lowlight(target);
                            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                        }
                    });
                }
            }
            else if (eventType.equals("reveal"))
            {
                final String wrongAudio = (currentEvent().equals("2l")) ? "INCORRECT2" : "INCORRECT";
                OBPath path = (OBPath) target;
                //
                if (currentEvent().equals("2l") && target.attributes().get("id").equals("obj_sun"))
                {
                    gotItWrongWithSfx();
                    //
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                        }
                    });
                }
                else if (target.attributes().get("type").equals(eventAttributes.get("correctAnswer")))
                {
                    action_toggleShape(path, true);
                    //
                    correctAnswerCount++;
                    int correctQuantity = Integer.parseInt(eventAttributes.get("correctQuantity"));
                    if (correctQuantity == 1)
                    {
                        gotItRightBigTick(true);
                        waitForSecs(0.3f);
                        //
                        if (currentEvent().equals("2n"))
                        {
                            String targetID = (String) target.attributes().get("id");
                            if (targetID.contains("door")) playAudioQueuedSceneIndex(currentEvent(), "CORRECT", 0, true);
                            else if (targetID.contains("house")) playAudioQueuedSceneIndex(currentEvent(), "CORRECT", 1, true);
                            else if (targetID.contains("trunk")) playAudioQueuedSceneIndex(currentEvent(), "CORRECT", 2, true);
                        }
                        else
                        {
                            playAudioQueuedScene(currentEvent(), "CORRECT", true);
                        }
                        waitForSecs(0.3f);
                        //
                        playAudioQueuedScene(currentEvent(), "FINAL", true);
                        //
                        nextScene();
                        return;
                    }
                    else
                    {
                        gotItRightBigTick(false);
                        waitSFX();
                        target.disable();
                        //
                        playAudioQueuedSceneIndex(currentEvent(), "CORRECT", correctAnswerCount-1, false);
                        //
                        if (correctAnswerCount == correctQuantity)
                        {
                            waitAudio();
                            waitForSecs(0.3f);
                            //
                            gotItRightBigTick(true);
                            waitForSecs(0.3f);
                            //
                            playAudioQueuedScene(currentEvent(), "CORRECT2", true);
                            waitForSecs(0.3f);
                            //
                            playAudioQueuedScene(currentEvent(), "FINAL", true);
                            //
                            nextScene();
                            return;
                        }
                    }
                }
                else
                {
                    gotItWrongWithSfx();
                    //
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            action_lowlight(target);
                            playAudioQueuedScene(currentEvent(), wrongAudio, false);
                        }
                    });
                }
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            Log.i("exception caught", e.toString());
            e.printStackTrace();
        }
    }


    public OBControl findTarget(PointF pt)
    {
        return finger(-1, 2, zPositionSortedFilteredControls("obj.*"), pt, true);
    }




    /*




-(void) demo_triangle
{
    [self playSceneAudio:@"DEMO4" atIndex:0 wait:YES]; // A triangle. It has THREE sides.
    [self waitForSecs:0.3];
    //
    OBPath *triangle = self.objectDict[@"obj_triangle"];
    UPath *upath = DeconstructedPath(triangle.path);
    USubPath *subPath = [upath subPaths][0];
    //
    for (int i = 0; i < 3; i++)
    {
        ULine *line = subPath.elements[i];
        UIBezierPath *bezier = [[UIBezierPath alloc] init];
        [bezier moveToPoint:line.pt0];
        [bezier addLineToPoint:line.pt1];
        OBPath *path = [[OBPath alloc] init];
        path.path = [bezier CGPath];
        path.frame = triangle.frame;
        path.strokeColor = [UIColor blackColor];
        path.lineWidth = applyGraphicScale(24);
        //
        OBPath *clone = [triangle copy];
        OBGroup *group = [[OBGroup alloc] initWithMembers:@[clone, path]];
        group.maskControl = clone;
        //
        [self playSceneAudio:@"DEMO4" atIndex:i+1 wait:NO]; // One. Two. Three
        [self sendObjectToTop:group];
        [self attachControl:group];
        [group show];
        [self waitAudio];
        //
        [group hide];
        [self detachControl:group];
        [self waitForSecs:0.3];
    }
}




-(void) demo_rectangle
{
    [self playSceneAudio:@"DEMO5" atIndex:0 wait:YES]; // A rectangle. It has FOUR sides.
    [self waitForSecs:0.3];
    //
    OBPath *rectangle = self.objectDict[@"obj_rectangle"];
    UPath *upath = DeconstructedPath(rectangle.path);
    USubPath *subPath = [upath subPaths][0];
    NSMutableArray *sides = [[NSMutableArray alloc] init];
    //
    for (int i = 0; i < 4; i++)
    {
        ULine *line = subPath.elements[i];
        UIBezierPath *bezier = [[UIBezierPath alloc] init];
        [bezier moveToPoint:line.pt0];
        [bezier addLineToPoint:line.pt1];
        //
        OBPath *path = [[OBPath alloc] init];
        path.path = [bezier CGPath];
        path.frame = rectangle.frame;
        path.strokeColor = [UIColor blackColor];
        path.lineWidth = applyGraphicScale(24);
        //
        OBPath *clone = [rectangle copy];
        OBGroup *group = [[OBGroup alloc] initWithMembers:@[clone, path]];
        group.maskControl = clone;
        //
        [self playSceneAudio:@"DEMO5" atIndex:i+1 wait:NO]; // One. Two. Three. Four
        [self sendObjectToTop:group];
        [self attachControl:group];
        [sides addObject:group];
        [group show];
        [self waitAudio];
        //
        [group hide];
        [self waitForSecs:0.3];
    }
    //
    [self playSceneAudio:@"DEMO5" atIndex:5 wait:NO]; // These two are the same length.
    DoBlockWithScreenLocked(^{
        [((OBGroup*)sides[0]) show];
        [((OBGroup*)sides[2]) show];
    });
    [self waitAudio];
    DoBlockWithScreenLocked(^{
        [((OBGroup*)sides[0]) hide];
        [((OBGroup*)sides[2]) hide];
    });
    [self waitForSecs:0.3];
    //
    [self playSceneAudio:@"DEMO5" atIndex:6 wait:NO]; // And these two are the same length
    DoBlockWithScreenLocked(^{
        [((OBGroup*)sides[1]) show];
        [((OBGroup*)sides[3]) show];
    });
    [self waitAudio];
    DoBlockWithScreenLocked(^{
        [((OBGroup*)sides[1]) hide];
        [((OBGroup*)sides[3]) hide];
    });
    [self waitForSecs:0.3];
    //
    for (OBGroup *side in sides) [self detachControl:side];
}



-(void) demo_square
{
    [self playSceneAudio:@"DEMO6" atIndex:0 wait:YES]; // A square. It has four sides.
    [self waitForSecs:0.3];
    //
    OBPath *square = self.objectDict[@"obj_square"];
    UPath *upath = DeconstructedPath(square.path);
    USubPath *subpath = [upath subPaths][0];
    NSMutableArray *sides = [[NSMutableArray alloc] init];
    //
    for (int i = 0; i < 4; i++)
    {
        ULine *line = subpath.elements[i];
        UIBezierPath *bezier = [[UIBezierPath alloc] init];
        [bezier moveToPoint:line.pt0];
        [bezier addLineToPoint:line.pt1];
        //
        OBPath *path = [[OBPath alloc] init];
        path.path = [bezier CGPath];
        path.frame = square.frame;
        path.strokeColor = [UIColor blackColor];
        path.lineWidth = applyGraphicScale(24);
        //
        OBPath *clone = [square copy];
        OBGroup *group = [[OBGroup alloc] initWithMembers:@[clone,path]];
        group.maskControl = clone;
        //
        [self playSceneAudio:@"DEMO6" atIndex:i+1 wait:NO]; // One. Two. Three. Four
        [self sendObjectToTop:group];
        [self attachControl:group];
        [sides addObject:group];
        [group show];
        //
        [self waitAudio];
        //
        [group hide];
        [self waitForSecs:0.3];
    }
    //
    [self playSceneAudio:@"DEMO6" atIndex:5 wait:NO]; // They are ALL the same length.
    DoBlockWithScreenLocked(^{
        for(OBGroup *side in sides) [side show];
    });
    //
    [self waitAudio];
    //
    DoBlockWithScreenLocked(^{
        for(OBGroup *side in sides)
        {
            [side hide];
            [self detachControl:side];
        }
    });
    [self waitForSecs:0.3];
}



     */

}
