package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGradientLayer;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.controls.OBRadialGradientLayer;
import org.onebillion.onecourse.controls.OBRadialGradientPath;
import org.onebillion.onecourse.controls.OBShaderControl;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.PixelShaderProgram;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBFatController;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OCM_FatController;
import org.onebillion.onecourse.utils.OC_FatController;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset.ASSET_DOODLE;
import static org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset.ASSET_TEXT;
import static org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset.assetsNamesForNewFile;
import static org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset.pathToAsset;

/**
 * Created by alan on 02/03/2017.
 */

public class OC_Doodle extends OC_SectionController
{
    class DoodleGradient
    {
        float[] HSV = new float[3];
        float[] HSV2 = new float[3];
        float hInc = 2;
        float x,y;
        float vectorx,vectory;
        OBRadialGradientPath gradient;
        float slowFactor = 0.05f;
        public DoodleGradient(int col,float px,float py,float vx,float vy,OBPath template)
        {
            Path p = template.path();
            p = new Path();
            p.addRect(template.bounds(), Path.Direction.CCW);
            x = px;
            y = py;
            vectorx = vx * slowFactor;
            vectory = vy * slowFactor;
            Color.colorToHSV(col,HSV);
            gradient = new OBRadialGradientPath(p,x,y,1500, Color.HSVToColor(HSV),Color.HSVToColor(0,HSV));
            gradient.setZPosition(template.zPosition() + 0.02f);
            gradient.sizeToBoundingBox();

            OBGradientLayer l = (OBGradientLayer)gradient.gradientLayer;
            l.locations = new float[3];
            l.locations[0] = 0;
            l.locations[1] = 0.5f;
            l.locations[2] = 1;
            l.colours = new int[3];
            l.colours[0] = Color.HSVToColor(HSV);
            l.colours[1] = Color.HSVToColor(HSV);
            l.colours[2] = Color.HSVToColor(0,HSV);
        }
        void doStep()
        {
            x += vectorx;
            y += vectory;
            if (x < -0.2f)
                vectorx = Math.abs(vectorx);
            else if (x > 1.2f)
                vectorx = -Math.abs(vectorx);
            if (y < -0.2f)
                vectory = Math.abs(vectory);
            else if (y > 1.2f)
                vectory = -Math.abs(vectory);
            OBRadialGradientLayer l = (OBRadialGradientLayer) gradient.gradientLayer;
            l.cx = x;
            l.cy = y;
            HSV[0] += hInc;
            if (HSV[0] > 360.0f)
                HSV[0] -= 360.0f;
            int rgb = Color.HSVToColor(HSV);
            l.colours[0] = rgb;
            HSV2[0] = HSV[0] + 180;
            if (HSV2[0] > 360.0f)
                HSV2[0] -= 360.0f;
            HSV2[1] = HSV[1];
            HSV2[2] = HSV[2];
            rgb = Color.HSVToColor(HSV2);
            l.colours[1] = rgb;
            l.colours[2] = Color.argb(0,Color.red(rgb),Color.green(rgb),Color.blue(rgb));
            //gradient.setNeedsRetexture();
        }
    }
    List<DoodleGradient> gradients = new ArrayList();
    public PixelShaderProgram shaderProgram;
    OBShaderControl shc;
    boolean readOnly;

    private Runnable messageCheckRunnable;
    private Handler messageCheckHandler = new Handler();
    float[] HSV = new float[3];
    float hInc = 5;

    OBPath blackborder;
    protected OBControl board,eraser, eraser2, boardMask;
    int boardMaskColour = Color.WHITE;
    List<PointF> pointQueue = new ArrayList<>();
    long lastTimeDrawn = 0;
    protected OBPresenter presenter;

    PointF startPoint;
    boolean eraserMode, arrowButtonHidden;
    Canvas canvas;
    OBImage drawOn;
    Paint drawingPaint, erasingPaint;
    Bitmap drawBitmap, eraserBitmap;

    OBImage backgroundImage;
    Bitmap gradBitmap;
    Canvas gradCanvas;
    OBImage eraserShadow;
    Boolean gradMode = true;


    public void setUpGradients()
    {
        OBPath blackboard = (OBPath) objectDict.get("blackboard");
        DoodleGradient dg = new DoodleGradient(Color.RED,0.25f,0.8f,0.01f,0.03f,blackboard);
        gradients.add(dg);
        dg = new DoodleGradient(Color.BLUE,0.85f,0.2f,0.01f,0.03f,blackboard);
        gradients.add(dg);
        dg = new DoodleGradient(Color.YELLOW,0.5f,0.7f,0.01f,0.03f,blackboard);
        gradients.add(dg);
        backgroundImage = new OBImage();
        backgroundImage.setFrame(board.frame());
        backgroundImage.setZPosition(board.zPosition()+0.02f);
        attachControl(backgroundImage);
        gradBitmap = Bitmap.createBitmap((int)board.width(), (int)board.height(), Bitmap.Config.ARGB_8888);
        gradCanvas = new Canvas(gradBitmap);
        backgroundImage.setScreenMaskControl(boardMask);
    }

    public void setUpShader()
    {
        shc = new OBShaderControl();
        shc.setFrame(board.frame());
        shc.setZPosition(board.zPosition()+0.02f);
        /*if (shaderProgram == null)
        {
            shaderProgram = new PixelShaderProgram(R.raw.threegradientsfragmentshader,shc.width(),shc.height());
            shc.shaderProgram = shaderProgram;
        }*/
        attachControl(shc);
    }
    public void miscSetUp()
    {
        OBPath blackboard = (OBPath) objectDict.get("blackboard");
        blackborder = (OBPath) blackboard.copy();
        blackborder.setFillColor(0);
        blackborder.setLineWidth(applyGraphicScale(6));
        blackborder.setZPosition(blackboard.zPosition()+ 0.3f);
        blackborder.outdent(blackborder.lineWidth());
        attachControl(blackborder);

        board = blackboard;
        blackboard.setLineWidth(0);
        boardMask = blackboard.copy();
        boardMask.setFillColor(Color.BLACK);

        if (gradMode)
            setUpGradients();
        else
        {
            setUpShader();
        }

        if (readOnly)
        {
            String dood = parameters.get("doodle");
            if (dood != null)
            {
                String filepath = pathToAsset(dood);
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inScaled = false;
                drawBitmap = BitmapFactory.decodeStream(OBUtils.getInputStreamForPath(filepath), null, opt);
                drawOn = new OBImage();
                drawOn.setFrame(blackboard.frame());
                drawOn.setZPosition(blackboard.zPosition() + 0.1f);
                drawOn.setContents(drawBitmap);
            }
            if (drawOn == null)
            {
                drawOn = (OBImage) OBConfigManager.sharedManager.getValue("oc_doodle");
            }
        }
        if (drawOn == null)
        {
            drawOn = new OBImage();
            drawOn.setFrame(blackboard.frame());
            drawOn.setZPosition(blackboard.zPosition() + 0.1f);
        }
        attachControl(drawOn);
        Color.colorToHSV(Color.RED,HSV);

        eraser = objectDict.get("eraser");
        if (eraser != null)
        {
            OBControl eraser2old = objectDict.get("eraser_2");

            eraser2old.show();
            OBControl copy = eraser2old.copy();
            attachControl(copy);
            eraser2 = new OBGroup(Collections.singletonList(copy));
            attachControl(eraser2);

            eraser2old.hide();
            eraser2.setZPosition(20);
            eraser2.hide();
            eraserShadow = (OBImage) objectDict.get("eraser_shadow");
            eraserShadow.show();
            detachControl(eraserShadow);
            arrowButtonHidden = true;

            eraser.show();
        }

        boardMask = blackboard.copy();
        boardMask.setFillColor(Color.BLACK);
        drawOn.setScreenMaskControl(boardMask);

        if (shc != null)
            shc.setScreenMaskControl(boardMask);
        /*boardMask = new OBControl();
        boardMask.setFrame(board.frame());
        boardMask.setBackgroundColor(Color.BLACK);
        boardMask.setAnchorPoint(new PointF(1, 0.5f));
        boardMask.setProperty("startWidth", boardMask.width());
        boardMask.setOpacity(1.0f);*/

        setupCanvas();


        preparePaintForDrawing();
        if (!readOnly)
            preparePaintForErasing();
        refreshGradient();
        refreshDrawingBoard();
    }

    public void render (OBRenderer renderer)
    {
        if (!gradMode && shaderProgram == null)
        {
            shaderProgram = new PixelShaderProgram(R.raw.threegradientsfragmentshadermask,shc.width(),shc.height(),true);
            shc.shaderProgram = shaderProgram;
        }
        super.render(renderer);
    }

    @Override
    public void onResume()
    {
        if (shc != null)
        {
            //shaderProgram = null;
            shc.stopAnimation();
            //creShc();
        }
    }

    void refreshGradient()
    {
        if (!gradMode)
            return;
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        int rgb = Color.HSVToColor(HSV);
        p.setColor(rgb);
        gradCanvas.drawRect(0,0,drawBitmap.getWidth(),drawBitmap.getHeight(),p);
        for (DoodleGradient g : gradients)
            g.gradient.draw(gradCanvas);
        backgroundImage.setContents(gradBitmap);
        backgroundImage.invalidate();
        backgroundImage.setNeedsRetexture();
    }

    public void clearCanvas()
    {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(boardMaskColour);
        canvas.drawRect(0,0,drawBitmap.getWidth(),drawBitmap.getHeight(),p);
    }
    public void setupCanvas()
    {
        if (readOnly && drawOn != null)
            drawBitmap = drawOn.layer.getContents();
        else
        {
            drawBitmap = Bitmap.createBitmap((int)board.width(), (int)board.height(), Bitmap.Config.ARGB_8888);
            drawOn.setContents(drawBitmap);
            canvas = new Canvas(drawBitmap);
            clearCanvas();
        }
    }

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
        drawingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    // call at the end of prepare!!
    public void preparePaintForErasing()
    {
        erasingPaint = new Paint();
        erasingPaint.setColor(Color.BLACK);
        //erasingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        erasingPaint.setColorFilter(new PorterDuffColorFilter(boardMaskColour,PorterDuff.Mode.SRC_ATOP));
        eraserBitmap = eraserShadow.drawn();
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

    public void prepare()
    {
        super.prepare();
        loadFingers();
        String p = parameters.get("readonly");
        if (p != null)
            readOnly = p.equals("true");
        loadEvent(readOnly?"master_ro":"master");
        miscSetUp();

        List<String> themes = Arrays.asList("toys","food","animals","transport");
        p = parameters.get("theme");
        if (p != null)
        {
            int idx = themes.indexOf(p);
            if (idx >= 0)
                eventIndex = idx;
        }

        if (readOnly)
        {
            String suffix = "_ro";
            for (String theme : themes)
                events.add(theme + suffix);
        }
        else
            events = themes;
        doVisual(currentEvent());
    }

    public void start()
    {
        setStatus(0);
        if (gradMode)
            scheduleTimerEvent();
        else
            shc.startAnimation(this);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()) )
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(shc != null)
            shc.stopAnimation();
    }


    public long switchStatus (String scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public int buttonFlags()
    {
        int tlflag = readOnly? 0:OBMainViewController.SHOW_TOP_LEFT_BUTTON;
        return tlflag;
    }

    public void setSceneXX (String scene)
    {
        deleteControls("obj.*");
        deleteControls("swatch");
        loadEvent(scene);
        OBPath swatch = (OBPath) objectDict.get("swatch");
        int col = swatch.strokeColor();
        float lw = swatch.lineWidth();
        blackborder.setStrokeColor(col);
        blackborder.setLineWidth(lw);
        boardMaskColour = swatch.fillColor();
        //clearCanvas();
        refreshDrawingBoard();
    }

    void timerEvent()
    {
        if (theStatus != STATUS_DRAGGING && theStatus != STATUS_BUSY)
        {
            if (gradMode)
            {
                HSV[0] -= hInc;
                if (HSV[0] < 0f)
                    HSV[0] += 360.0f;
                for (DoodleGradient dg : gradients)
                {
                    dg.doStep();
                }
                refreshGradient();
            }
            else
            {
                shc.invalidate();
            }
        }
        scheduleTimerEvent();
    }
    void scheduleTimerEvent()
    {
        if (_aborting || theStatus == STATUS_EXITING)
            return;
        if (messageCheckRunnable == null)
        {
            messageCheckRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    timerEvent();
                }
            };
        }
        messageCheckHandler.removeCallbacks(messageCheckRunnable);
        messageCheckHandler.postDelayed(messageCheckRunnable,250);
    }

    public void drawPoint(PointF point)
    {
        PointF pt1 = this.convertPointToControl(point, board);
        canvas.drawPoint(pt1.x,pt1.y,drawingPaint);
        refreshDrawingBoard();
    }

    public void drawPath(List<PointF>points)
    {
        //MainActivity.log(String.format("drawPath %d",points.size()));
        if (points.size() < 2)
            return;
        if (points.size() == 2)
        {
            drawPath(points.get(0),points.get(1));
            PointF p = points.get(1);
            points.clear();
            points.add(p);
            refreshDrawingBoard();
        }
        else
        {
            Path path = new Path();
            PointF p0 = points.get(0);
            PointF pt0 = this.convertPointToControl(p0, board);
            path.moveTo(pt0.x,pt0.y);
            // MainActivity.log(String.format("drawPath moveto %g %g %g %g",p0.x, p0.y,pt0.x,pt0.y));
            for (int i = 1;i < points.size();i++)
            {
                PointF p = points.get(i);
                PointF pt1 = this.convertPointToControl(p, board);
                path.lineTo(pt1.x,pt1.y);
                //MainActivity.log(String.format("drawPath lineto %g %g",pt1.x, pt1.y));
            }
            canvas.drawPath(path,drawingPaint);
            PointF pl = points.get(points.size()-1);
            points.clear();
            points.add(pl);
            refreshDrawingBoard();
        }
    }
    public void drawPath(PointF fromPoint, PointF toPoint)
    {
        PointF pt1 = this.convertPointToControl(fromPoint, board);
        PointF pt2 = this.convertPointToControl(toPoint, board);

        canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, drawingPaint);
        //MainActivity.log(String.format("drawPath %g %g %g %g",pt1.x, pt1.y, pt2.x, pt2.y));

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

        //canvas.drawBitmap(eraserBitmap,(int)frame.left, (int)frame.top,erasingPaint);
        canvas.drawBitmap(eraserBitmap,(int)frame.left, (int)frame.top,erasingPaint);
        refreshDrawingBoard();
    }

    public void nextScene ()
    {
        eventIndex = (eventIndex + 1) % events.size();
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground (Void... params)
            {
                setScene(currentEvent());
                switchStatus(currentEvent());
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    void redButtonHit()
    {
        nextScene();
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
            if (pointQueue.size() > 0)
                if (pointQueue.size() == 1)
                    drawPoint(pointQueue.get(0));
                else
                    drawPath(pointQueue);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    Bitmap thumbnail(String assetname)
    {
        int[] wh = OC_PlayZoneAsset.thumbnailSize();
        OC_Doodle dood = new OC_Doodle();
        dood.params = "doodle/readonly=true/theme="+currentEvent()+"/doodle="+assetname;
        dood.prepare();
        Bitmap bm = dood.thumbnail(wh[0],wh[1],true);

        //frig because of masking problems.

        Bitmap b2 = Bitmap.createBitmap(wh[0],wh[1], Bitmap.Config.ARGB_8888);
        b2.eraseColor(Color.argb((int)(topColour[3]*255),(int)(topColour[0]*255),(int)(topColour[1]*255),(int)(topColour[2]*255)));
        Canvas canvas = new Canvas(b2);
        canvas.drawBitmap(bm,0,0,null);
        return b2;
    }
    void saveContentsToDisk()
    {
        Bitmap cont = drawOn.layer.getContents();
        List<String> names = assetsNamesForNewFile(ASSET_DOODLE);
        String name = names.get(1);
        String path = pathToAsset(name);
        String thumbname = names.get(0);
        String thumbpath = pathToAsset(thumbname);

        try
        {
            FileOutputStream out = new FileOutputStream(path);
            cont.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            out = new FileOutputStream(thumbpath);
            thumbnail(name).compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            OBFatController fc = MainActivity.mainActivity.fatController;
            if (fc instanceof OCM_FatController)
            {
                OCM_FatController ocf = (OCM_FatController)fc;
                Map<String,String> map = new ArrayMap<>();
                map.put("theme", currentEvent());
                map.put("doodle", name);
                ocf.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_DOODLE,thumbname,map);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void saveDoodle()
    {
        Map<String,String> params = new HashMap<>();
        params.put("theme",currentEvent());
        Bitmap bm = drawn(null,true);
        OBConfigManager.sharedManager.setValue("oc_doodle", drawOn);
        saveContentsToDisk();

        if(!_aborting)
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                     {
                                         @Override
                                         public void run() throws Exception
                                         {
                                             exitEvent();
                                         }
                                     }
            );


    }
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (readOnly)
        {
            exitEvent();
            return;
        }
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            if(finger(0,1,Collections.singletonList(objectDict.get("doodleredbutton")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                         {
                                             @Override
                                             public void run() throws Exception
                                             {
                                                 redButtonHit();
                                             }
                                         }

                );
            }
            else if(board.frame().contains(pt.x, pt.y))
            {
                eraserMode = false;
                startPoint = pt;
                //drawPoint(pt);
                lastTimeDrawn = SystemClock.uptimeMillis();
                pointQueue.clear();
                pointQueue.add(pt);
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
            else if(finger(-1,-1,Arrays.asList(objectDict.get("button_save")),pt)  != null)
            {
                final OBGroup saveButton = (OBGroup) objectDict.get("button_save");
                setStatus(STATUS_DOING_DEMO);

                saveButton.highlight();
                saveDoodle();

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
                pointQueue.add(pt);
                long currTime = SystemClock.uptimeMillis();
                if (currTime - lastTimeDrawn > 20)
                {
                    drawPath(pointQueue);
                    lastTimeDrawn = currTime;
                }
                /*drawPath(startPoint,pt);
                lastTimeDrawn = SystemClock.uptimeMillis();

                startPoint = pt;*/
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

}

