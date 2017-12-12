package org.onebillion.onecourse.mainui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.view.*;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.glstuff.OBGLView;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.TextureShaderProgram;
import org.onebillion.onecourse.utils.OBBrightnessManager;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OBUtils;

public class OBMainViewController extends OBViewController
{
    public static final int SHOW_TOP_LEFT_BUTTON = 1,
            SHOW_TOP_RIGHT_BUTTON = 2,
            SHOW_BOTTOM_LEFT_BUTTON = 4,
            SHOW_BOTTOM_RIGHT_BUTTON = 8;
    public List<OBSectionController> viewControllers;
    public OBControl topLeftButton, topRightButton, bottomLeftButton, bottomRightButton;
    public OBLabel topLabel;
    public boolean navigating;
    protected Rect _buttonBoxRect = null;
    OBControl downButton;
    public long lastTouchActivity = 0;
    private Integer currentTouchID;

    public OBMainViewController (Activity a)
    {
        super(a);
        viewControllers = new ArrayList<OBSectionController>();
        glView().controller = this;
        //
        enterGLMode();
        navigating = false;
    }


    public void addButtons ()
    {
        if (topLeftButton != null)
            return;

        Rect bounds = bounds();
        topLeftButton = OBUtils.buttonFromSVGName("back");
        topLeftButton.setLeft(0);
        topLeftButton.setTop(0);
        bottomLeftButton = OBUtils.buttonFromSVGName("prev");
        bottomLeftButton.setLeft(0);
        bottomLeftButton.setBottom(bounds.height());
        topRightButton = OBUtils.buttonFromSVGName("repeataudio");
        topRightButton.setRight(bounds.width());
        topRightButton.setTop(0);
        setBottomRightButton("std");

        float amt = applyGraphicScale(2f);
        for (OBControl c : Arrays.asList(topLeftButton,topRightButton,bottomLeftButton,bottomRightButton))
        {
            c.setShadow(0,0.3f,amt,amt,Color.BLACK);
        }

        Typeface tf = OBUtils.standardTypeFace();
        topLabel = new OBLabel("ALL WORK AND NO PLAY MAKES JACK A DULL BOY. ALL WORK AND NO PLAY MAKES JACK A DULL BOY. ", tf, applyGraphicScale(15));
        topLabel.setColour(Color.BLACK);
        topLabel.controller = this;
        topLabel.sizeToBoundingBox();
        topLabel.setPosition(bounds().centerX(), bounds().centerY());
        topLabel.setJustification(OBTextLayer.JUST_CENTRE);
        topLabel.setTop(0);
        OBSystemsManager.sharedManager.setStatusLabel(topLabel);
    }

    public void setBottomRightButton(String itype)
    {
        String k = (itype.equals("std"))?"next":"next_star";
        if (bottomRightButton != null && k == bottomRightButton.textureKey)
            return;
        bottomRightButton = OBUtils.buttonFromSVGName(k);
        bottomRightButton.setRight(bounds().width());
        bottomRightButton.setBottom(bounds().height());
        float amt = applyGraphicScale(2f);
        bottomRightButton.setShadow(0,0.3f,amt,amt,Color.BLACK);
    }
    public void buttonHit (PointF pt)
    {
        OBSectionController cont = viewControllers.get(viewControllers.size() - 1);
        if (topLeftButton.frame().contains(pt.x, pt.y))
            cont.exitEvent();
        else if (topRightButton.frame().contains(pt.x, pt.y))
            cont.replayAudio();
        else if (bottomLeftButton.frame().contains(pt.x, pt.y))
            cont.prevPage();
        else if (bottomRightButton.frame().contains(pt.x, pt.y))
            cont.nextPage();
    }

    public void showButtons (int flags)
    {
        if (topLeftButton == null)
            addButtons();
        if ((flags & SHOW_TOP_LEFT_BUTTON) == 0)
            topLeftButton.setOpacity(0.0f);
        else
            topLeftButton.setOpacity(1.0f);
        if ((flags & SHOW_TOP_RIGHT_BUTTON) == 0)
            topRightButton.setOpacity(0.0f);
        else
            topRightButton.setOpacity(1.0f);
        if ((flags & SHOW_BOTTOM_LEFT_BUTTON) == 0)
            bottomLeftButton.setOpacity(0.0f);
        else
            bottomLeftButton.setOpacity(1.0f);
        if ((flags & SHOW_BOTTOM_RIGHT_BUTTON) == 0)
            bottomRightButton.setOpacity(0.0f);
        else
            bottomRightButton.setOpacity(1.0f);

        if (OBConfigManager.sharedManager.isDebugEnabled())
            topLabel.setOpacity(1.0f);
        else
            topLabel.setOpacity(0.0f);

    }

    public void showHideButtons (int flags)
    {
        if (topLeftButton == null)
            addButtons();
        topLeftButton.setHidden((flags & SHOW_TOP_LEFT_BUTTON) == 0);
        topRightButton.setHidden((flags & SHOW_TOP_RIGHT_BUTTON) == 0);
        bottomLeftButton.setHidden((flags & SHOW_BOTTOM_LEFT_BUTTON) == 0);
        bottomRightButton.setHidden((flags & SHOW_BOTTOM_RIGHT_BUTTON) == 0);
        invalidateView(0, 0, glView().getRight(), (int) topLeftButton.height());
    }

    public void viewWasLaidOut (boolean changed, int l, int t, int r, int b)
    {
        if (!inited)
        {
            prepare();
            inited = true;
        }
    }

    public void drawControls (Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
    }

    public void highlightButton (OBControl but)
    {
        but.highlight();
        glView().requestRender();
        downButton = but;
    }

    OBControl buttonForPoint (float x, float y)
    {
        for (OBControl but : Arrays.asList(topLeftButton, topRightButton, bottomLeftButton, bottomRightButton))
        {
            if ((!but.hidden) && but.frame().contains(x, y))
                return but;
        }
        return null;
    }

    public void setTouchTime()
    {
        lastTouchActivity = System.currentTimeMillis();
    }
    public void touchDownAtPoint (float x, float y, OBGLView v)
    {
        setTouchTime();
        OBControl but = buttonForPoint(x, y);
        if (but == null)
            topController().touchDownAtPoint(new PointF(x, y), v);
        else
        {
            highlightButton(but);

        }
    }

    public void touchUpAtPoint (float x, float y, OBGLView v)
    {
        setTouchTime();
        final OBControl db = downButton;
        if (db != null)
            OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    db.lowlight();
                    glView().requestRender();
                }
            });
        else
        {
            topController().touchUpAtPoint(new PointF(x, y), v);
            return;
        }
        OBControl but = buttonForPoint(x, y);
        if (db != but)
            topController().touchUpAtPoint(new PointF(x, y), v);
        else
        {
            downButton = null;
            if (but == topLeftButton)
                topController().goBack();
            else if (but == topRightButton)
                topController().replayAudio();
            else if (but == bottomLeftButton)
                topController().prevPage();
            else if (but == bottomRightButton)
                topController().nextPage();
        }
    }

    @Override

    public void prepare ()
    {
        addButtons();
        glView().setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch (View v, MotionEvent event)
            {
                int pointerIndex = event.getActionIndex();
                int pointerID = event.getPointerId(pointerIndex);
                int action = event.getAction() & event.ACTION_MASK;
                //
                if (currentTouchID == null && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN))
                {
                    currentTouchID = pointerID;
                    touchDownAtPoint(event.getX(), event.getY(), (OBGLView) v);
                    OBBrightnessManager.sharedManager.registeredTouchOnScreen(false);
                }
                else if (action == MotionEvent.ACTION_MOVE && currentTouchID != null && currentTouchID == pointerID)
                {
                    OBGLView ov = (OBGLView) v;
                    topController().touchMovedToPoint(new PointF(event.getX(), event.getY()), ov);
                }
                else if (currentTouchID != null && currentTouchID == pointerID && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP))
                {
                    currentTouchID = null;
                    touchUpAtPoint(event.getX(), event.getY(), (OBGLView) v);
                }
                return true;
            }
        });
        MainActivity.mainActivity.fatController.startUp();
    }

    public OBGLView glView ()
    {
        return MainActivity.mainActivity.glSurfaceView;
    }

    public boolean glMode ()
    {
        return glView().getParent() != null;
    }

    public void enterGLMode ()
    {
        if (!glMode())
        {
            addView(glView());
        }
    }

    public void exitGLMode ()
    {
        if (glMode())
        {
            removeView(glView());
        }
    }

    public void addView (View v)
    {
        ViewGroup rootView = (ViewGroup) MainActivity.mainActivity.findViewById(android.R.id.content);
        rootView.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //rootView.addView(v, 0);
    }

    public void removeView (View v)
    {
        ViewGroup rootView = (ViewGroup) MainActivity.mainActivity.findViewById(android.R.id.content);
        rootView.removeView(v);
    }

    private Class controllerClass (String name, String configPath)
    {
        try
        {
            String config = "";
            if (configPath != null)
            {
                String[] sarr = configPath.split(",");
                configPath = sarr[0];
                String[] paths = configPath.split("/");
                config = paths[0];
                config = config.replace("-", "_");
                config += ".";
            }
            Class cnm = Class.forName("org.onebillion.onecourse.mainui." + config + name);
            return cnm;
        }
        catch (ClassNotFoundException e)
        {
            if (configPath != null)
                return controllerClass(name, null);
        }
        return null;
    }


    public boolean pushViewControllerWithNameConfig (String nm, String configPath, boolean animate, boolean fromRight, Object _params)
    {
        return pushViewControllerWithNameConfig(nm, configPath,animate,fromRight,_params,false);
    }


    public boolean pushViewControllerWithNameConfig (String nm, String configPath, boolean animate, boolean fromRight, Object _params, boolean pop)
    {
        Class cnm = controllerClass(nm, configPath);
        if (cnm == null)
            return false;

        pushViewController(cnm, animate, fromRight, _params, pop);
        return true;
    }

    public boolean pushViewControllerWithName (String nm, boolean animate, boolean fromRight, Object _params)
    {
        try
        {
            Class cnm = Class.forName("org.onebillion.onecourse.mainui." + nm);
            pushViewController(cnm, animate, fromRight, _params, false);
            return true;
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void pushViewController (Class<?> vcClass, Boolean animate, boolean fromRight, Object _params, boolean pop)
    {
        pushViewController(vcClass,animate,fromRight,_params,pop,false,null);
    }

    public void pushViewController (Class<?> vcClass, Boolean animate, boolean fromRight, Object _params, boolean pop, boolean zoom, RectF zoomRect)
    {
        Constructor<?> cons;
        OBSectionController controller;
        try
        {
            cons = vcClass.getConstructor();
            controller = (OBSectionController) cons.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        OBRenderer renderer = MainActivity.mainActivity.renderer;
        if (renderer != null)
        {
            controller.setViewPort(0,0,renderer.w,renderer.h);
        }

        controller.params = _params;
        if(zoom)
            controller.setPopAnimationZoom(zoomRect);
        controller.viewWillAppear(animate);
        controller.prepare();
        if (viewControllers.size() >= 1 && animate)
        {
            if (fromRight)
            {
                if (!zoom)
                    transition(topController(), controller, fromRight, 0.6);
                else
                    transitionZoom(topController(), controller, fromRight, zoomRect,0.4);
            }
            else
            {
                if (!zoom)
                    transition(controller, topController(), fromRight, 0.6);
                else
                    transitionZoom(controller, topController(), fromRight, zoomRect, 0.4);
            }
        }
        viewControllers.add(controller);
        if (pop && viewControllers.size() > 1)
        {
            viewControllers.remove(viewControllers.size() - 2);
        }
        if (controller.requiresOpenGL)
        {
            enterGLMode();
            showButtons(controller.buttonFlagsWithFatController());
            showHideButtons(controller.buttonFlagsWithFatController());
        }
        else
        {
            ViewGroup vg = (ViewGroup) glView().getParent();
            if (vg != null)
            {
                vg.removeView(glView());
            }
        }
        final OBSectionController vc = controller;
        new Handler().post(new Runnable()
        {
            @Override
            public void run ()
            {
                vc.start();
            }
        });
    }


    public void transition (OBSectionController l, OBSectionController r, boolean fromRight, double duration)
    {
        OBRenderer renderer = MainActivity.mainActivity.renderer;
        renderer.transitionFrac = 0;
        renderer.transitionScreenL = l;
        renderer.transitionScreenR = r;
        renderer.transitionType = OBRenderer.TRANSITION_SLIDE;
        GLSurfaceView glv = glView();

        long startms = SystemClock.uptimeMillis();
        double frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (currtime - startms) / (duration * 1000);
            float f = (float) OB_Maths.clamp01(frac);
            if (fromRight)
                f = 1 - f;
            renderer.transitionFrac = f;
            glv.requestRender();
            if (l == null)
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                }
            else
                l.waitForSecsNoThrow(0.01);
        }
        renderer.transitionScreenR.setViewPort(0,0,renderer.w,renderer.h);
        renderer.transitionScreenL.setViewPort(0,0,renderer.w,renderer.h);
        glv.requestRender();
        renderer.transitionScreenR = renderer.transitionScreenL = null;
        renderer.resetViewport();
    }

    public void transitionZoom (OBSectionController l, OBSectionController r, boolean fromRight, RectF zoomRect, double duration)
    {
        OBRenderer renderer = MainActivity.mainActivity.renderer;
        renderer.transitionFrac = 0;
        renderer.transitionScreenL = l;
        renderer.transitionScreenR = r;
        renderer.transitionType = OBRenderer.TRANSITION_ZOOM;
        renderer.zoomRect = zoomRect;
        GLSurfaceView glv = glView();

        long startms = SystemClock.uptimeMillis();
        double frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (currtime - startms) / (duration * 1000);
            float f = (float) OB_Maths.clamp01(frac);
            if (fromRight)
                f = 1 - f;
            renderer.transitionFrac = f;
            glv.requestRender();
            if (l == null)
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                }
            else
                l.waitForSecsNoThrow(0.01);
        }
        renderer.transitionScreenR.setViewPort(0,0,renderer.w,renderer.h);
        renderer.transitionScreenL.setViewPort(0,0,renderer.w,renderer.h);
        glv.requestRender();
        renderer.transitionScreenR = renderer.transitionScreenL = null;
        renderer.resetViewport();
    }

    public void popViewController ()
    {
        OBSectionController topvc = viewControllers.get(viewControllers.size() - 1);
        OBSectionController nextvc = viewControllers.get(viewControllers.size() - 2);
        nextvc.viewWillAppear(false);
        if (glMode() && !nextvc.requiresOpenGL)
        {
            exitGLMode();
        }
        else
            transition(nextvc, topvc, false, 0.5);
        viewControllers.remove(viewControllers.size() - 1);
        showButtons(nextvc.buttonFlagsWithFatController());
        showHideButtons(nextvc.buttonFlagsWithFatController());
        nextvc.start();
    }

    public void popViewControllerZoom (RectF zoomRect)
    {
        OBSectionController topvc = viewControllers.get(viewControllers.size() - 1);
        OBSectionController nextvc = viewControllers.get(viewControllers.size() - 2);
        nextvc.viewWillAppear(false);
        transitionZoom(nextvc, topvc, false, zoomRect, 0.4);
        viewControllers.remove(viewControllers.size() - 1);
        showButtons(nextvc.buttonFlagsWithFatController());
        showHideButtons(nextvc.buttonFlagsWithFatController());
        nextvc.start();
    }

    public void popViewControllerToBottom(boolean animate)
    {
        if(viewControllers.size() > 1)
        {
            OBSectionController topvc = viewControllers.get(viewControllers.size() - 1);
            OBSectionController bottomvc = viewControllers.get(0);
            bottomvc.viewWillAppear(false);
            transition(bottomvc, topvc, false, 0.5);
            List<OBSectionController> toRemove = new ArrayList<>();
            for(int i=1; i<viewControllers.size(); i++)
                toRemove.add(viewControllers.get(i));
            if(toRemove.size() > 0)
                viewControllers.removeAll(toRemove);
            showButtons(bottomvc.buttonFlagsWithFatController());
            showHideButtons(bottomvc.buttonFlagsWithFatController());
            bottomvc.start();
        }
    }

    public OBSectionController topController ()
    {
        if (viewControllers.size() > 0)
            return viewControllers.get(viewControllers.size() - 1);
        return null;

    }

    public void render (OBRenderer renderer)
    {
        TextureShaderProgram textureShader = (TextureShaderProgram) renderer.textureProgram;
        textureShader.useProgram();
        topLeftButton.render(renderer, this, renderer.projectionMatrix);
        topRightButton.render(renderer, this, renderer.projectionMatrix);
        bottomRightButton.render(renderer, this, renderer.projectionMatrix);
        bottomLeftButton.render(renderer, this, renderer.projectionMatrix);
        topLabel.render(renderer, this, renderer.projectionMatrix);
    }

    public void onResume()
    {
        if(viewControllers != null && viewControllers.size() > 0)
        {
            OBSectionController controller = viewControllers.get(viewControllers.size()-1);
            if(controller != null)
                controller.onResume();
        }

    }

    public void onPause()
    {
        if(viewControllers != null && viewControllers.size() > 0)
        {
            OBSectionController controller = viewControllers.get(viewControllers.size()-1);
            if(controller != null)
                controller.onPause();
        }
    }

    public void onAlarmReceived(Intent intent)
    {
        if(viewControllers != null && viewControllers.size() > 0)
        {
            OBSectionController controller = viewControllers.get(viewControllers.size()-1);
            if(controller != null)
                controller.onAlarmReceived(intent);
        }
    }

    public void onBatteryStatusReceived(float level, boolean charging)
    {
        if(viewControllers != null && viewControllers.size() > 0)
        {
            OBSectionController controller = viewControllers.get(viewControllers.size()-1);
            if(controller != null)
                controller.onBatteryStatusReceived(level,charging);
        }
    }
}
