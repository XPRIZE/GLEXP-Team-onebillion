package org.onebillion.xprz.mainui;

/**
 * Created by alan on 11/10/15.
 */

        import android.app.Activity;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.PointF;
        import android.graphics.Rect;
        import android.graphics.RectF;
        import android.opengl.GLSurfaceView;
        import android.os.Handler;
        import android.os.Looper;
        import android.view.MotionEvent;
        import android.view.View;

        import org.onebillion.xprz.controls.OBControl;
        import org.onebillion.xprz.glstuff.GraphicState;
        import org.onebillion.xprz.glstuff.Texture;
        import org.onebillion.xprz.utils.OBRunnableSyncUI;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.concurrent.locks.ReentrantLock;


public class OBViewController
{
    public OBView view;
    public Activity activity;
    public boolean inited = false;
    public boolean requiresOpenGL = true;
    public int viewPortLeft,viewPortRight,viewPortTop,viewPortBottom;
    public int screenLock = 0; // public for now
    public ReentrantLock renderLock = new ReentrantLock();
    public float[] projectionMatrix;
    public float[] modelViewMatrix;
    protected Map<String,List<Texture>> textureDictionary = new HashMap();
    RectF lockedInvalidRect;
    List<GraphicState> graphicStateStack = new ArrayList<>();

    public OBViewController(Activity a)
    {
        activity = a;
    }

    public static OBMainViewController MainViewController()
    {
        return MainActivity.mainViewController;
    }

    public void viewWasLaidOut(boolean changed, int l, int t, int r, int b)
    {
    }

    public void prepare()
    {
    }

    public void start()
    {

    }

    public void drawControls(Canvas canvas)
    {

    }

    public void lockScreen()
    {
      //  if (screenLock == 0)
        renderLock.lock();
        screenLock++;
    }

    public void unlockScreen()
    {
        screenLock--;
        if (screenLock == 0)
        {
            if (lockedInvalidRect != null)
            {
                invalidateView((int)lockedInvalidRect.left,(int)lockedInvalidRect.top,(int)lockedInvalidRect.right,(int)lockedInvalidRect.bottom);
                lockedInvalidRect = null;
            }

        }
        renderLock.unlock();

    }

    public Rect bounds()
    {
        GLSurfaceView gls = MainActivity.mainViewController.glView();
        return new Rect(0,0,gls.getRight(),gls.getBottom());
    }

    public RectF boundsf()
    {
        GLSurfaceView gls = MainActivity.mainViewController.glView();
        return new RectF(0,0,gls.getRight(),gls.getBottom());
    }

    public void invalidateView(int left,int top,int right,int bottom)
    {
        if (screenLock > 0)
        {
            if (lockedInvalidRect == null)
                lockedInvalidRect = new RectF(left,top,right,bottom);
            else
                lockedInvalidRect.union(left,top,right,bottom);
            return;
        }
        GLSurfaceView glv = MainActivity.mainViewController.glView();
        if (glv != null)
            glv.requestRender();
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
    }

    public void touchMovedToPoint(PointF pt,View v)
    {

    }

    public void touchDownAtPoint(PointF pt, View v)
    {

    }

    public void saveGraphicState()
    {
        graphicStateStack.add(new GraphicState(projectionMatrix,modelViewMatrix));
    }

    public void restoreGraphicState()
    {
        GraphicState gs = graphicStateStack.remove(graphicStateStack.size()-1);
        projectionMatrix = gs.projectionMatrix;
        modelViewMatrix = gs.modelViewMatrix;
    }

    Texture sharedTexture(String src,float scale)
    {
        List<Texture> lt = textureDictionary.get(src);
        if (lt == null)
            return null;
        for (Texture t : lt)
            if (t.scale >= scale)
                return t;
        return null;
    }

    public Texture createTexture(OBControl c,String src,boolean shared)
    {
        if (shared)
        {
            Texture t = sharedTexture(src,c.scale());
            if (t != null)
                return t;
        }
        Bitmap b = null;
        if (c.texture != null)
            b = c.texture.bitmap();
        b = c.drawn(b);
        Texture t = new Texture(b,c.scale());
        if (shared)
        {
            List<Texture> lt = textureDictionary.get(src);
            if (lt == null)
            {
                lt = new ArrayList<>();
                textureDictionary.put(src,lt);
            }
            lt.add(t);
        }
        return t;
    }

    public float applyGraphicScale(float amt)
    {
        return MainActivity.mainActivity.applyGraphicScale(amt);
    }

    public void setViewPort(int l,int t,int r,int b)
    {
        viewPortLeft = l;
        viewPortRight = r;
        viewPortTop = t;
        viewPortBottom = b;
    }
}

