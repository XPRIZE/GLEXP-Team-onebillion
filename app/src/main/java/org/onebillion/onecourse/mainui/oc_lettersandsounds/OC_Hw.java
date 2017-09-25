package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 21/07/16.
 */
public class OC_Hw extends OC_SectionController
{
    protected Map<String,OBPhoneme> componentDict;
    protected OBControl drawRect,board,eraser, eraser2,arrowButton, boardMask;
    protected OBPath  lineTop,lineBottom;
    protected OBPresenter presenter;

    PointF startPoint;
    boolean eraserMode, arrowButtonHidden;
    Canvas canvas;
    OBImage drawOn;
    Paint drawingPaint, erasingPaint;
    Bitmap drawBitmap, eraserBitmap;


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();

        loadFingers();
        loadEvent("master");

        board = objectDict.get("board");
        Rect frame = new Rect();
        board.frame().round(frame);
        board.setFrame(new RectF(frame));
        board.setPosition(new PointF((int)board.position().x, (int)board.position().y));
        lineTop = (OBPath)objectDict.get("line_top");
        lineBottom = (OBPath)objectDict.get("line_bottom");
        arrowButton = objectDict.get("arrow_next");

        board.setZPosition(1);
        objectDict.get("frame").setZPosition(30);
        lineTop.setZPosition(2);
        lineTop.sizeToBoundingBoxIncludingStroke();
        lineBottom.setZPosition(2);
        lineBottom.sizeToBoundingBoxIncludingStroke();

        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("startloc", OC_Generic.copyPoint(presenter.control.position()));
        presenter.control.setRight(0);

        Map<String,Object> ed = loadXML(getConfigPath("tracingletters.xml"));
        eventsDict.putAll(ed);

        drawOn = new OBImage();
        drawOn.setFrame(board.frame());
        drawOn.setPosition(board.position());
        drawOn.setZPosition(10);
        attachControl(drawOn);

        componentDict = OBUtils.LoadWordComponentsXML(true);

        arrowButton.show();
        arrowButton.highlight();
        arrowButton.lowlight();
        arrowButton.hide();

        drawRect = new OBControl();
        RectF rect = new RectF(board.frame());
        rect.inset(-applyGraphicScale(20), -applyGraphicScale(20));
        drawRect.setFrame(rect);
        drawRect.setZPosition(2);
        drawRect.hide();
        attachControl(drawRect);

        eraser = objectDict.get("eraser");
        OBControl eraser2old = objectDict.get("eraser_2");

        eraser2old.show();
        OBControl copy = eraser2old.copy();
        attachControl(copy);
        eraser2 = new OBGroup(Collections.singletonList(copy));
        // eraserUIImage = .get(eraser2 renderedImage);
        attachControl(eraser2);

        eraser2old.hide();
        eraser2.setZPosition(20);
        eraser2.hide();

        arrowButtonHidden = true;

        eraser.show();

        boardMask = new OBControl();
        boardMask.setFrame(board.frame());
        boardMask.setBackgroundColor(Color.BLACK);
        boardMask.setAnchorPoint(new PointF(1, 0.5f));
        boardMask.setProperty("startWidth", boardMask.width());
        boardMask.setOpacity(1.0f);

        setupCanvas();
    }

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON|OBMainViewController.SHOW_TOP_RIGHT_BUTTON|0|0;
    }

    public void setupCanvas()
    {
        drawBitmap = Bitmap.createBitmap((int)board.width(), (int)board.height(), Bitmap.Config.ARGB_8888);
        drawOn.setContents(drawBitmap);
        canvas = new Canvas(drawBitmap);
    }

    public void initScene()
    {
        arrowButtonHidden = true;
    }


    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            if(drawRect.frame().contains(pt.x, pt.y))
            {
                eraserMode = false;
                startPoint = pt;
                if(arrowButtonHidden && arrowButton.hidden)
                {
                    arrowButtonHidden = false;
                    OBUtils.runOnOtherThreadDelayed(arrowButtonTimeout(),
                            new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            try {
                                arrowButton.show();
                            }
                            catch (Exception exception) {

                            }
                        }
                    });

                }
                drawPoint(pt);
                setStatus(STATUS_DRAGGING);

            }
            else if(finger(0,1,Collections.singletonList(objectDict.get("eraser")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                lockScreen();
                eraser2.setPosition(pt);
                eraser.hide();
                eraser2.show();
                this.target = eraser2;
                eraser2.setZPosition(eraser2.zPosition() + 10);
                dragOffset = OB_Maths.DiffPoints(eraser2.position(), pt);
                unlockScreen();
                eraserMode = true;
                startPoint = pt;
                setStatus(STATUS_DRAGGING);
            }
            else if(finger(0,1,Collections.singletonList(arrowButton),pt) != null)
            {
                setStatus(STATUS_BUSY);
                arrowButton.highlight();
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        arrowButtonClick();
                    }
                });

            }
        }

    }



    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_BUSY);
            if(eraserMode)
            {
                lockScreen();
                target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
                if(board.frame.contains(pt.x,pt.y))
                    eraseAtEraserLoc();

                setStatus(STATUS_DRAGGING);
                unlockScreen();

            }
            else
            {
                if(!drawRect.frame.contains(pt.x,pt.y))
                {
                   PointF loc = new PointF(pt.x, pt.y);
                    if(loc.y > drawRect.bottom())
                        loc.y =  drawRect.bottom();
                    else if(loc.y < drawRect.top())
                        loc.y = drawRect.top();

                    if(loc.x > drawRect.right())
                        loc.x = drawRect.right();
                    else if(pt.x < drawRect.left())
                        loc.x = drawRect.left();

                }

                drawPath(startPoint,pt);

                startPoint = pt;
                setStatus(STATUS_DRAGGING);
            }

        }

    }


    public void touchUpAtPoint(PointF pt,View v)
    {

        if(status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkTouchUp();
                }
            });


        }

    }

    public void checkTouchUp()
    {
        if(eraserMode)
        {
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(eraser.position(),eraser2)),0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            lockScreen();
            eraser2.hide();
            eraser.show();
            unlockScreen();
            target = null;
            eraserMode = false;
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
        else
        {
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public void arrowButtonClick() throws Exception
    {

    }

    public void hideArrowButton()
    {
        arrowButton.lowlight();
        arrowButton.hide();
    }

    public float arrowButtonTimeout()
    {
        return 1.5f;
    }



    public void setupLinesFor(OBGroup group)
    {
        List<OBControl> xboxes = group.filterMembers("xbox.*",true);
        OBControl xbox = xboxes.get(0);

        RectF rect = this.convertRectFromControl(xbox.bounds(),xbox);
        PointF loc = OC_Generic.copyPoint(lineTop.position());
        loc.y = rect.top;
        lineTop.setPosition(lineTop.position().x, rect.top);
        lineBottom.setPosition(lineTop.position().x, rect.top+rect.bottom-rect.top);

        lineTop.setStrokeEnd(0);
        lineBottom.setStrokeEnd(0);
        lineTop.show();
        lineBottom.show();
    }

    public void highlightPathsForGroup(OBGroup group,boolean on)
    {
        lockScreen();
        for(OBControl p : group.filterMembers("Path.*",true))
            ((OBPath)p).setStrokeColor( on ? Color.RED : Color.WHITE);

        unlockScreen();

    }
    public void startScene() throws Exception
    {
        setReplayAudioScene(currentEvent(),"PROMPT.REPEAT");
        playAudioQueuedScene("PROMPT",true);
        reprompt(setStatus(STATUS_WAITING_FOR_DRAG), OBUtils.insertAudioInterval(currentAudio("REMIND"),300),4);
    }

    public void drawPoint(PointF point)
    {
        PointF pt1 = this.convertPointToControl(point, board);
        canvas.drawPoint(pt1.x,pt1.y,drawingPaint);
        refreshDrawingBoard();
    }

    public void drawPath(PointF fromPoint, PointF toPoint)
    {
        PointF pt1 = this.convertPointToControl(fromPoint, board);
        PointF pt2 = this.convertPointToControl(toPoint, board);

        canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, drawingPaint);

        refreshDrawingBoard();
    }

    private void refreshDrawingBoard()
    {
        drawOn.setContents(drawBitmap);
        drawOn.setNeedsRetexture();
        drawOn.invalidate();
    }


    public void eraseAtEraserLoc()
    {
        RectF frame = this.convertRectToControl(eraser2.frame(), board);

        canvas.drawBitmap(eraserBitmap,(int)frame.left, (int)frame.top,erasingPaint);
        refreshDrawingBoard();
    }

    // call at the end of prepare!!
    public void preparePaintForDrawing()
    {
        drawingPaint = new Paint();
        drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setStrokeWidth(drawPathWidth());
        drawingPaint.setColor(Color.WHITE);
        drawingPaint.setStrokeJoin(Paint.Join.ROUND);
        drawingPaint.setStrokeCap(Paint.Cap.ROUND);
        drawingPaint.setAntiAlias(true);
        drawingPaint.setDither(true);
    }

    // call at the end of prepare!!
    public void preparePaintForErasing()
    {
        erasingPaint = new Paint();
        erasingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        eraserBitmap = eraser2.drawn();
    }

    public float drawPathWidth()
    {
        return applyGraphicScale(20);
    }

    public void animatePathDraw(OBPath path)
    {
        float len = path.length();
        double duration = len * 2 / theMoveSpeed;
        OBAnim anim = OBAnim.propertyAnim("strokeEnd",1,path);
        OBAnimationGroup.runAnims(Collections.singletonList(anim),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }


    public void stampImage(OBPath path)
    {

        drawCanvasOnLayer();
    }


    public void eraseAtPoint(PointF point)
    {


        drawCanvasOnLayer();

    }


    public void drawCanvasOnLayer()
    {

    }


    public OBGroup loadPaths(String letter,int colour, float size, boolean prepare)
    {
        List<List<OBPath>> letterPaths = new ArrayList<>();
        List<OBControl> xboxes = new ArrayList<>();
        for (int i = 0;i < letter.length();i++)
        {
            for (String n : filterControlsIDs("Path.*|xbox"))
                objectDict.remove(n);

            String character = letter.substring(i,i+1);
            String l = String.format("_%s", character);
            loadEvent(l);
            List<OBControl> lpaths = sortedFilteredControls("Path.*");
            letterPaths.add((List<OBPath>)(Object)lpaths);
            OBControl xbox = objectDict.get("xbox");
            xboxes.add(xbox);
            xbox.hide();
        }
        if (letter.length() > 1)
        {
            float xboxeswidth = 0,maxy = 0,miny = bounds().height();
            for (int i = 0;i < xboxes.size();i++)
            {
                OBControl xb = xboxes.get(i);
                RectF f = xb.frame();
                xboxeswidth += f.width();
                List<OBPath> arr = letterPaths.get(i);
                RectF fp = OBUtils.PathsUnionRect(arr);
                fp.top -= f.top;
                if (fp.top < miny)
                    miny = fp.top;
                float thismaxy = fp.top + fp.height();
                if (thismaxy > maxy)
                    maxy = thismaxy;
            }
            float pathsheight = maxy - miny;
            float diff = (this.bounds().height() - pathsheight) / 2.0f;
            float xboxtop = diff - miny;
            float left = (this.bounds().width() - xboxeswidth) / 2.0f;
            for (int i = 0;i < xboxes.size();i++)
            {
                OBControl xb = xboxes.get(i);
                float xdiff = left - xb.left();
                float ydiff = xboxtop - xb.top();
                xb.setPosition(OB_Maths.OffsetPoint(xb.position(), xdiff, ydiff));
                for (OBPath p : letterPaths.get(i))
                    p.setPosition(OB_Maths.OffsetPoint(p.position(), xdiff, ydiff));
                left += xb.width();
            }

        }
        for (List<OBPath> arr : letterPaths)
        {
            for(OBPath path : arr)
            {
                path.setStrokeColor(colour);
                path.setLineWidth(size);
                path.setStrokeEnd(prepare ? 0 : 1);
            }

        }

        List<OBControl> allObjects = new ArrayList<>();
        for(List<OBPath> arr : letterPaths)
            allObjects.addAll(arr);

        allObjects.addAll(xboxes);
        OBGroup letterGrp = new OBGroup(allObjects);
        attachControl(letterGrp);
        letterGrp.recalculateFrameForPath(allObjects);
        int index = 1;
        for(int i=0; i<letterPaths.size(); i++)
        {
            List<OBPath> paths = letterPaths.get(i);
            for(int j=0; j<paths.size(); j++)
            {
                OBPath p = paths.get(j);

                Map<String, Object> dict = new HashMap<String,Object>(p.attributes());
                dict.put("id", String.format("Path%d_%d",i+1,j+1));
                p.setProperty("attrs",dict);
                p.setProperty("name",p.attributes().get("id"));
                letterGrp.objectDict.put((String)p.attributes().get("id"),p);

            }
        }

        for(int i=0; i<xboxes.size(); i++)
        {
            OBControl x = xboxes.get(i);
            Map<String, Object> dict = new HashMap<String,Object>(x.attributes());
            dict.put("id",String.format("xbox%d",i+1));
            x.setProperty("attrs",dict);
            x.setProperty("name",x.attributes().get("id"));
            letterGrp.objectDict.put((String)x.attributes().get("id"),x);
            index++;
        }
        return letterGrp;
    }

/*
    public void createDrawRectForGroup(OBGroup drawGroup)
    {
        drawRect = new OBControl();
        RectF rect1 = new RectF(drawGroup.frame());
        rect1.inset(-applyGraphicScale(60), -applyGraphicScale(70));
        RectF rect2 = new RectF(drawGroup.frame());
        rect2.inset( applyGraphicScale(20), applyGraphicScale(20));
        rect1.intersect(rect2);
        drawRect.setFrame(rect1);
        drawRect.setBackgroundColor(Color.RED);
        drawRect.setZPosition(2);
        drawRect.setOpacity(0);
        attachControl(drawRect);

    }
*/
    public void setGroupPaths(OBGroup group,float size)
    {
        for(OBControl p : group.filterMembers("Path.*",true))
            ((OBPath)p).setLineWidth(size*group.scale());
    }

    public void cleanUpDrawing()
    {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawOn.setContents(drawBitmap);
        drawOn.setNeedsRetexture();
        drawOn.invalidate();
    }

    public void animateLinesOn()
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("strokeEnd",1,lineTop),OBAnim.propertyAnim("strokeEnd",1,lineBottom)),0.5f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void hideLines()
    {
        //lineTop.setStrokeStart(0);
       // lineBottom.setStrokeStart(0);
        lineTop.setStrokeEnd(0);
        lineBottom.setStrokeEnd(0);
    }


    public void presenterOpening() throws Exception
    {
        if(parameters.get("presenter").equalsIgnoreCase("true"))
        {
            presenter.control.show();
            PointF point = (PointF)presenter.control.settings.get("startloc");
            presenter.walk(point);
            presenter.faceFront();
            List<String>  audio = getAudioForScene(currentEvent(), "DEMO");
            presenter.speak((List<Object>)(Object)Collections.singletonList(audio.get(0)),this);
            waitForSecs(0.3f);
            PointF point2 = OC_Generic.copyPoint(point);
            point2.x = 0.8f* this.bounds().width();
            presenter.walk(point2);
            presenter.faceFront();
            presenter.speak((List<Object>)(Object)Collections.singletonList(audio.get(1)),this);
            waitForSecs(0.3f);
            PointF point3 = OC_Generic.copyPoint(point);
            point3.x = 1.3f*this.bounds().width();
            presenter.walk(point3);
            presenter.control.hide();
        }
    }

    public void showAllStrokes(OBGroup group)
    {
        lockScreen();
        for(OBControl p : group.filterMembers("Path.*",true))
        {
            OBPath path = (OBPath)p;
           // path.setStrokeStart(0);
            path.setStrokeEnd(1);
        }
        unlockScreen();

    }

    public void resetGuideMask()
    {
        boardMask.setWidth((float) boardMask.propertyValue("startWidth"));
    }

}
