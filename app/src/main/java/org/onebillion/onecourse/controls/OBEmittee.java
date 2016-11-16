package org.onebillion.onecourse.controls;

/**
 * Created by alan on 19/02/16.
 */
public class OBEmittee
{
    long startTime,endTime,lastTime;
    float xVelocity,yVelocity;
    float alpha,alphaSpeed;
    float spin,scale,scaleSpeed,angle;
    float posX,posY;

    public OBEmittee()
    {
        super();
        angle = 0;
        alpha = 1;
    }
    public boolean updateValues(long systemTime)
    {
        if (systemTime > endTime)
            return false;
        float secsGap = (systemTime - lastTime) / 1000f;
        posX += (xVelocity * secsGap);
        posY += (yVelocity * secsGap);
        scale += (scaleSpeed * secsGap);
        angle += spin * secsGap;
        lastTime = systemTime;
        alpha += (alphaSpeed * secsGap);
        if (alpha < 0)
            alpha = 0;
        return true;
    }
}
