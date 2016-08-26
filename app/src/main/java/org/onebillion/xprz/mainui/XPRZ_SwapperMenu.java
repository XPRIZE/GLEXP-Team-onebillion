package org.onebillion.xprz.mainui;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.utils.OBUtils;

/**
 * Created by pedroloureiro on 25/08/16.
 */
public class XPRZ_SwapperMenu extends OBSectionController
{
    private OBImage background;
    private String className;
    private Object params;
    private Boolean goingBack;

    public XPRZ_SwapperMenu ()
    {
        super(MainActivity.mainActivity);
        goingBack = false;
    }

    public XPRZ_SwapperMenu (Bitmap image, String nm, Object params)
    {
        this();
        background = new OBImage(image);
        className = nm;
        this.params = params;
    }

    public void prepare ()
    {
        attachControl(background);
        background.setPosition(bounds().width() / 2, bounds().height() / 2);
        background.show();
    }

    public void start ()
    {
        GLSurfaceView glv = glView();
        if (!MainViewController().glMode())
        {
            ViewGroup rootView = (ViewGroup) MainActivity.mainActivity.findViewById(android.R.id.content);
            View currentView = rootView.getChildAt(0);
            rootView.addView(glv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            currentView.bringToFront();
        }
        //
        MainViewController().enterGLMode();
        //
        MainViewController().showButtons(buttonFlags());
        MainViewController().showHideButtons(buttonFlags());
        //
        final float delay = 0.175f;
        OBUtils.runOnOtherThreadDelayed(delay, new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                OBUtils.runOnMainThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        try
                        {
                            GLSurfaceView glv = glView();
                            glv.requestRender();
                            glv.bringToFront();
                            //
                            OBUtils.runOnOtherThreadDelayed(0.05f, new OBUtils.RunLambda()
                            {
                                @Override
                                public void run () throws Exception
                                {
                                    OBUtils.runOnMainThread(new OBUtils.RunLambda()
                                    {
                                        @Override
                                        public void run () throws Exception
                                        {
                                            try
                                            {
                                                if (goingBack)
                                                {
                                                    OBSectionController nextvc = MainViewController().viewControllers.get(MainViewController().viewControllers.size() - 2);
                                                    nextvc.viewWillAppear(false);
                                                    MainViewController().viewControllers.remove(MainViewController().viewControllers.size() - 1);
                                                    nextvc.start();
                                                }
                                                else
                                                {
                                                    goingBack = true;
                                                    MainViewController().pushViewControllerWithName(className, true, true, params);
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int buttonFlags ()
    {
        return 0;
    }


}
