package org.onebillion.xprz.mainui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.*;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.glstuff.OBGLView;
import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.glstuff.TextureShaderProgram;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_utils;

public class OBMainViewController extends OBViewController
{
    public static final int SHOW_TOP_LEFT_BUTTON = 1,
            SHOW_TOP_RIGHT_BUTTON = 2,
            SHOW_BOTTOM_LEFT_BUTTON = 4,
            SHOW_BOTTOM_RIGHT_BUTTON = 8;
    public List<OBSectionController> viewControllers;
    public OBControl topLeftButton,topRightButton,bottomLeftButton,bottomRightButton;
    protected Rect _buttonBoxRect = null;
    boolean navigating;

    public OBMainViewController(Activity a)
    {
        super(a);
        viewControllers = new ArrayList<OBSectionController>();
        /*view = new OBView(a,this);
        ViewGroup rootView = (ViewGroup) a.findViewById(android.R.id.content);
        rootView.addView(view,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));*/
        navigating = false;
    }

    public void doButton(OBControl button, String source, boolean abutLeft, boolean abutTop)
    {
        Bitmap im = OBImageManager.sharedImageManager().bitmapForName(source);
        ((OBImage)button).setContents(im);
        float graphicScale = applyGraphicScale(1);
        button.setScale(graphicScale);
        if (abutLeft)
            button.setLeft(0);
        else
            button.setRight(bounds().width());
        if (abutTop)
            button.setTop(0);
        else
            button.setBottom(bounds().height());
    }

    public void addButtons()
    {
        if (topLeftButton != null)
            return;

        Rect bounds = bounds();
        topLeftButton = OB_utils.buttonFromSVGName("back");
        topLeftButton.setLeft(0);
        topLeftButton.setTop(0);
        bottomLeftButton = OB_utils.buttonFromSVGName("prev");
        bottomLeftButton.setLeft(0);
        bottomLeftButton.setBottom(bounds.height());
        topRightButton = OB_utils.buttonFromSVGName("repeataudio");
        topRightButton.setRight(bounds.width());
        topRightButton.setTop(0);
        bottomRightButton = OB_utils.buttonFromSVGName("next");
        bottomRightButton.setRight(bounds.width());
        bottomRightButton.setBottom(bounds.height());
    }

    public void buttonHit(PointF pt)
    {
        OBSectionController cont = viewControllers.get(viewControllers.size()-1);
        if (topLeftButton.frame().contains(pt.x,pt.y))
            cont.exitEvent();
        else if (topRightButton.frame().contains(pt.x,pt.y))
            cont.replayAudio();
        else if (bottomLeftButton.frame().contains(pt.x,pt.y))
            cont.prevPage();
        else if (bottomRightButton.frame().contains(pt.x,pt.y))
            cont.nextPage();
    }

    public void showButtons(int flags)
    {
        if (topLeftButton == null)
            addButtons();
        if ((flags & SHOW_TOP_LEFT_BUTTON)== 0)
            topLeftButton.setOpacity(0.0f);
        else
            topLeftButton.setOpacity(1.0f);
        if ((flags & SHOW_TOP_RIGHT_BUTTON)== 0)
            topRightButton.setOpacity(0.0f);
        else
            topRightButton.setOpacity(1.0f);
        if ((flags & SHOW_BOTTOM_LEFT_BUTTON)== 0)
            bottomLeftButton.setOpacity(0.0f);
        else
            bottomLeftButton.setOpacity(1.0f);
        if ((flags & SHOW_BOTTOM_RIGHT_BUTTON)== 0)
            bottomRightButton.setOpacity(0.0f);
        else
            bottomRightButton.setOpacity(1.0f);
    }

    public void showHideButtons(int flags)
    {
        if (topLeftButton == null)
            addButtons();
        topLeftButton.setHidden((flags & SHOW_TOP_LEFT_BUTTON)== 0);
        topRightButton.setHidden((flags & SHOW_TOP_RIGHT_BUTTON)== 0);
        bottomLeftButton.setHidden((flags & SHOW_BOTTOM_LEFT_BUTTON)== 0);
        bottomRightButton.setHidden((flags & SHOW_BOTTOM_RIGHT_BUTTON)== 0);
        invalidateView(0,0,glView().getRight(),(int)topLeftButton.height());
    }

    public void viewWasLaidOut(boolean changed, int l, int t, int r, int b)
    {
        if (! inited)
        {
            prepare();
            inited = true;
        }
    }

    public void drawControls(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
    }

    public void touchDownAtPoint(float x,float y,OBGLView v)
    {
        if ((!topLeftButton.hidden) && topLeftButton.frame().contains(x,y))
            return;
        if ((!topRightButton.hidden) && topRightButton.frame().contains(x,y))
            return;
        if ((!bottomLeftButton.hidden) && bottomLeftButton.frame().contains(x,y))
            return;
        if ((!bottomRightButton.hidden) && bottomRightButton.frame().contains(x,y))
            return;
        topController().touchDownAtPoint(new PointF(x,y), v);
    }
    public void touchUpAtPoint(float x,float y,OBGLView v)
    {
        if (!topLeftButton.hidden && topLeftButton.frame().contains(x,y))
        {
            topController().goBack();
            return;
        }
        if (!topRightButton.hidden && topRightButton.frame().contains(x,y))
        {
            topController().replayAudio();
            return;
        }
        if (!bottomLeftButton.hidden && bottomLeftButton.frame().contains(x,y))
        {
            topController().prevPage();
            return;
        }
        if (!bottomRightButton.hidden && bottomRightButton.frame().contains(x,y))
        {
            topController().nextPage();
            return;
        }
        topController().touchUpAtPoint(new PointF(x,y), v);
    }
    @Override

    public void prepare()
    {
        addButtons();
        glView().setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    touchDownAtPoint(event.getX(),event.getY(),(OBGLView)v);
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    OBGLView ov = (OBGLView)v;
                    topController().touchMovedToPoint(new PointF(event.getX(),event.getY()), ov);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    touchUpAtPoint(event.getX(),event.getY(),(OBGLView)v);
                }
                return true;
            }
        });


        MainActivity.mainActivity.fatController.startUp();
    }

    public OBGLView glView()
    {
        return MainActivity.mainActivity.glSurfaceView;
    }

    public void addView(View v)
    {
       /* if (topLeftButton != null)
        {
            int idx = view.indexOfChild(topLeftButton);
            view.addView(v, idx);
        }*/
    }

    public boolean pushViewControllerWithName(String nm,boolean animate,boolean fromRight,Object _params)
    {
        try
        {
            Class cnm = Class.forName("org.onebillion.xprz.mainui."+nm);
            pushViewController(cnm, animate,fromRight, _params);
            return true;
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void pushViewController(Class<?> vcClass,Boolean animate,boolean fromRight,Object _params)
    {
        Constructor<?> cons;
        OBSectionController controller;
        try
        {
            cons = vcClass.getConstructor();
            controller = (OBSectionController)cons.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        controller.params = _params;
        controller.prepare();
        if (viewControllers.size() >= 1 && animate)
            transition(topController(),controller,fromRight,0.6);
        viewControllers.add(controller);
        if (viewControllers.size() > 2)
            viewControllers.remove(viewControllers.size()-2);
        showButtons(controller.buttonFlags());
        showHideButtons(controller.buttonFlags());
        final OBSectionController vc = controller;
        new Handler().post(new Runnable()
        {
            @Override
            public void run() {
                vc.start();
            }
        });
    }

    public void transition(OBSectionController l,OBSectionController r,boolean fromRight,double duration)
    {
        OBRenderer renderer = MainActivity.mainActivity.renderer;
        renderer.transitionFrac = 0;
        renderer.transitionScreenL = l;
        renderer.transitionScreenR = r;
        GLSurfaceView glv = glView();

        long startms = SystemClock.uptimeMillis();
        double frac = 0;
        while (frac <= 1.0 )
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (currtime - startms) / (duration * 1000);
            float f = (float)OB_Maths.clamp01(frac);
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
        renderer.transitionScreenR = renderer.transitionScreenL = null;
        renderer.resetViewport();
    }
    public void pushViewControllerFromRight(Class<?> vcClass,Object _params,boolean fromRight,double duration)
    {
        if (!navigating)
        {
            Constructor<?> cons;
            final OBSectionController controller;
            try
            {
                cons = vcClass.getConstructor(Activity.class);
                controller = (OBSectionController)cons.newInstance(activity);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            viewControllers.add(controller);
            controller.params = _params;
            final OBView v = new OBView(activity, controller);
            final int vw = view.getRight();
            int vh = view.getBottom();
            int vl;
            if (fromRight)
                vl = vw;
            else
                vl = -vw;
            vl = 0;
            v.layout(vl, 0, vl+vw, vh);
            controller.view = v;
            addView(v);
            controller.prepare();
            showButtons(controller.buttonFlags());
            showHideButtons(controller.buttonFlags());


            if (viewControllers.size() > 2)
            {
                OBSectionController oldvc = viewControllers.get(viewControllers.size()-2);
                view.removeView(oldvc.view);
                viewControllers.remove(viewControllers.size()-2);
            }

            final OBSectionController vc = controller;
            navigating = false;
            new Handler().post(new Runnable()
            {
                @Override
                public void run() {
                    vc.start();
                }
            });

			/*new AsyncTask<Void, Void,Void>()
			{
				protected Void doInBackground(Void... params) {
					animTopView(-vw, 2.0);
					return null;
				}}.execute();
			 */
			/*new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
				    TranslateAnimation anim = new TranslateAnimation( 0, -vl , 0, 0 );
				    anim.setDuration(1000);
				    anim.setFillAfter( true );
				    anim.setAnimationListener(new Animation.AnimationListener() {


						@Override
						public void onAnimationEnd(Animation animation) {
							if (viewControllers.size() > 2)
							{
								OBSectionController oldvc = viewControllers.get(viewControllers.size()-2);
								view.removeView(oldvc.view);
								viewControllers.remove(viewControllers.size()-2);
							}

							final OBSectionController vc = controller;
							navigating = false;
							ReadingMainActivity.mainViewController.setPageNo(((RD_ReadingController)controller).pageNo);
							new Handler().post(new Runnable()
							{
								@Override
								public void run() {
									vc.start();
								}
							});

						}

						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}
					});
				    v.startAnimation(anim);
				}
			},20);

 		*/
        }
    }

    public void popViewController()
    {
        OBSectionController topvc = viewControllers.get(viewControllers.size()-1);
        OBSectionController nextvc = viewControllers.get(viewControllers.size()-2);
        transition(nextvc,topvc,false,0.5);
        viewControllers.remove(viewControllers.size()-1);
        showButtons(nextvc.buttonFlags());
        showHideButtons(nextvc.buttonFlags());
        nextvc.start();
    }

    public OBSectionController topController()
    {
        if (viewControllers.size() > 0)
            return viewControllers.get(viewControllers.size()-1);
        return null;

    }
    public void render(OBRenderer renderer)
    {
        TextureShaderProgram textureShader = (TextureShaderProgram) renderer.textureProgram;
        textureShader.useProgram();
        topLeftButton.render(renderer,this,renderer.projectionMatrix);
        topRightButton.render(renderer,this,renderer.projectionMatrix);
        bottomRightButton.render(renderer,this,renderer.projectionMatrix);
        bottomLeftButton.render(renderer,this,renderer.projectionMatrix);
    }

}
