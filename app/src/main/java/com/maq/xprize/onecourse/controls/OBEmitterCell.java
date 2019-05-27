package com.maq.xprize.onecourse.controls;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;

import com.maq.xprize.onecourse.glstuff.OBRenderer;
import com.maq.xprize.onecourse.glstuff.Texture;
import com.maq.xprize.onecourse.glstuff.TextureRect;
import com.maq.xprize.onecourse.glstuff.TextureShaderProgram;
import com.maq.xprize.onecourse.mainui.OBViewController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 19/02/16.
 */
public class OBEmitterCell
{
    public float birthRate,lifeTime,lifeTimeRange,velocity,velocityRange,emissionAngle,emissionRange,red,green,blue,alpha,
            blueSpeed,redSpeed,greenSpeed,
            alphaSpeed,spin,spinRange,scale,scaleRange,scaleSpeed;
    public PointF position;
    public Bitmap contents;
    public Texture texture;
    public String name;
    public float[] modelMatrix = new float[16];
    public float[] finalMatrix = new float[16];
    boolean running;
    long startTime,lastBirthTime,birthCount;
    List<OBEmittee>emittees;
    Matrix tempMatrix;
    OBEmitter emitter;

    public OBEmitterCell()
    {
        super();
        birthRate = lifeTime = lifeTimeRange = 0;
        velocity = velocityRange = 0;
        emissionAngle = emissionRange = spin = spinRange = 0;
        scale = 1;
        scaleRange = scaleSpeed = 0;
        red = green = blue = alpha = 1.0f;
        blueSpeed = redSpeed = greenSpeed = alphaSpeed = 0;
        running = false;
        emittees = new ArrayList<>();
        tempMatrix = new Matrix();
    }

    static float rValue(float centralValue,float range)
    {
        return (centralValue - range / 2) + range * (float)Math.random();
    }

    public OBEmittee createEmittee()
    {
        OBEmittee ee = new OBEmittee();
        ee.scale = rValue(scale,scaleRange);
        ee.scaleSpeed = scaleSpeed;
        ee.spin = rValue(spin,spinRange);
        float vel = rValue(velocity,velocityRange);
        float ang = rValue(emissionAngle,emissionRange);
        float x = (float)Math.cos(ang);
        float y = (float)Math.sin(ang);
        ee.xVelocity = x * vel;
        ee.yVelocity = y * vel;
        PointF loc = emitter.convertPointToControl(position,null);
        ee.posX = loc.x;
        ee.posY = loc.y;
        float life = rValue(lifeTime,lifeTimeRange);
        ee.startTime = ee.lastTime = SystemClock.uptimeMillis();
        ee.endTime = ee.startTime + (long)(life * 1000f);
        ee.alpha = alpha;
        ee.alphaSpeed = alphaSpeed;
        return ee;
    }

    public void start(OBEmitter parent)
    {
        texture = new Texture(contents,1);
        emitter = parent;
        startTime = lastBirthTime = SystemClock.uptimeMillis();
        birthCount = 0;
    }
    public void updateEmittees()
    {
        long tm = SystemClock.uptimeMillis();
        synchronized (emittees)
        {
            for (int i = emittees.size()-1;i >= 0;i--)
            {
                OBEmittee ee = emittees.get(i);
                boolean res = ee.updateValues(tm);
                if (!res)
                    emittees.remove(i);
            }
        }
    }

    public boolean doCycle(boolean withBirth)
    {
        if (emittees.size() == 0 && (!withBirth || birthRate == 0))
            return false;
        updateEmittees();
        long tm = SystemClock.uptimeMillis();
        float secs = (tm - lastBirthTime) / 1000f;
        int birthCountNeeded = (int)((birthRate * secs));
        if(withBirth)
        {
            synchronized (emittees)
            {
                for (int i = 0; i < birthCountNeeded; i++)
                {
                    emittees.add(createEmittee());
                    birthCount++;
                    lastBirthTime = SystemClock.uptimeMillis();
                }
            }
        }
        return true;
    }

    public void draw(Canvas canvas)
    {
        float ax = contents.getWidth() / 2;
        float ay = contents.getHeight() / 2;
        Paint p = new Paint();
        for (OBEmittee ee : emittees)
        {
            canvas.save();
            tempMatrix.reset();
            tempMatrix.preTranslate(ee.posX,ee.posY);
            float f;
            if ((f = ee.angle) != 0)
            {
                f = (float)Math.toDegrees(f);
                tempMatrix.preRotate(f);
            }
            if ((f = ee.scale) != 0)
                tempMatrix.preScale(f,f);
            tempMatrix.preTranslate(-ax, -ay);
            canvas.concat(tempMatrix);
            p.setColor(Color.RED);
            boolean needAnotherRestore = ee.alpha < 1.0f;
            if (needAnotherRestore)
                canvas.saveLayerAlpha(0,0,contents.getWidth(),contents.getHeight(),(int)(ee.alpha*255),Canvas.ALL_SAVE_FLAG);
            canvas.drawBitmap(contents, 0, 0, p);
            if (needAnotherRestore)
                canvas.restore();
            canvas.restore();
        }
    }

    public float[] matrix3dForDraw(float x,float y,float w2,float h2,float rotation,float scale)
    {

        android.opengl.Matrix.setIdentityM(modelMatrix,0);
        android.opengl.Matrix.translateM(modelMatrix,0,x,y,0);
        if (rotation != 0)
            android.opengl.Matrix.rotateM(modelMatrix,0,(float) Math.toDegrees(rotation),0,0,1);
        if (scale != 1)
            android.opengl.Matrix.scaleM(modelMatrix,0,scale, scale,1);
        android.opengl.Matrix.translateM(modelMatrix,0,-w2,-h2,0);
        return modelMatrix;

    }

    public void render(OBRenderer renderer, OBViewController vc, float[] modelViewMatrix)
    {
        float w2 = contents.getWidth()/2;
        float h2 = contents.getHeight()/2;
        float blendColour[] = {1,1,1,1};
        TextureShaderProgram textureShader = (TextureShaderProgram) renderer.textureProgram;
        textureShader.useProgram();
        TextureRect tr = renderer.textureRect;
        synchronized (emittees)
        {
            for (OBEmittee ee : emittees)
            {
                matrix3dForDraw(ee.posX,ee.posY,w2,h2,ee.angle,ee.scale);
                android.opengl.Matrix.multiplyMM(finalMatrix,0,modelViewMatrix,0,modelMatrix,0);
                blendColour[3] = ee.alpha;
                textureShader.setUniforms(finalMatrix,renderer.textureObjectId(0),blendColour, 1);
                tr.draw(renderer,0,0,w2*2,h2*2,texture.bitmap());
            }
        }
    }

}
