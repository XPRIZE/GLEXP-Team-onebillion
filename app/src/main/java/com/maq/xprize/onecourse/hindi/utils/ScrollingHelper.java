package com.maq.xprize.onecourse.hindi.utils;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.MutableBoolean;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;

import java.util.Map;

/**
 * Created by michal on 03/07/2017.
 */

public class ScrollingHelper
{
    public static void measureScrollSpeed(OBControl con)
    {
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
            return;
        Map<String,Object> scrollingData = (ArrayMap<String,Object>)data;

        long currentTime = SystemClock.uptimeMillis();
        long lastTime = (long)scrollingData.get("last_action");
        if(currentTime == lastTime)
            return;
        float lastSpeedX = (float)scrollingData.get("speed_x");
        float lastSpeedY = (float)scrollingData.get("speed_y");
        float tick = (float)scrollingData.get("tick") ;
        PointF lastLoc = (PointF)scrollingData.get("last_loc");
        PointF currentLoc = OBMisc.copyPoint(con.position());
        float ticks =  (currentTime - lastTime)*1.0f/(1000.0f*tick);
        float speedX = 0.8f * ((currentLoc.x - lastLoc.x) / ticks) + 0.2f * lastSpeedX;
        float speedY = 0.8f * ((currentLoc.y - lastLoc.y) / ticks) + 0.2f * lastSpeedY;
        scrollingData.put("speed_x",speedX);
        scrollingData.put("speed_y", speedY);
        scrollingData.put("last_loc",currentLoc);
        scrollingData.put("last_action",currentTime);
    }

    public static void prepareForScrollMeasureTickValue(OBControl con, float tickValue,double decay,float minSpeedX, float minSpeedY)
    {
        Object data = con.propertyValue("scrolling_data");
        Map<String,Object> scrollingData;
        if(data == null)
            scrollingData = new ArrayMap<String,Object>();
        else
            scrollingData = (ArrayMap<String,Object>)data;

        scrollingData.put("last_action",SystemClock.uptimeMillis());
        scrollingData.put("last_loc",OBMisc.copyPoint(con.position()));
        scrollingData.put("speed_x",0.0f);
        scrollingData.put("speed_y",0.0f);
        scrollingData.put("min_speed_x",minSpeedX);
        scrollingData.put("min_speed_y",minSpeedY);
        scrollingData.put("decay",decay);
        scrollingData.put("tick",tickValue);
        con.setProperty("scrolling_data", scrollingData);
    }

    public static void prepareForScrollMeasure(OBControl con)
    {
        float startSpeed = OBConfigManager.sharedManager.applyGraphicScale(0.1f);
        prepareForScrollMeasureTickValue(con, 0.0025f, 0.99f, startSpeed, startSpeed);
    }

    public static float speedX(OBControl con)
    {
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
            return 0;
        Map<String,Object> scrollingData = (Map<String,Object>)data;
        return (float)scrollingData.get("speed_x") ;

    }

    public static float speedY(OBControl con)
    {
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
            return 0;
        Map<String,Object> scrollingData = (Map<String,Object>)data;
        return (float)scrollingData.get("speed_y");
    }

    public static void setSpeedX(OBControl con, float speedX)
    {
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
        {
            Map<String, Object> scrollingData = (Map<String, Object>) data;
            scrollingData.put("speed_x", speedX);
        }
    }

    public static void setSpeedY(OBControl con, float speedY)
    {
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
        {
            Map<String, Object> scrollingData = (Map<String, Object>) data;
            scrollingData.put("speed_y", speedY);
        }
    }

    public static boolean scrollControlByFrac(OBControl con, float frac)
    {
        MutableBoolean finished = new MutableBoolean(true);
        con.setPosition(nextScrollingLocationByFrac(con, frac, finished));
        return finished.value;
    }

    public static boolean scrollControl(OBControl con)
    {
        MutableBoolean finished = new MutableBoolean(true);
        con.setPosition(nextScrollingLocation(con, finished));
        return finished.value;
    }

    public static PointF nextScrollingLocation(OBControl con, MutableBoolean finished)
    {
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
        {
            finished.value = true;
            return OBMisc.copyPoint(con.position());
        }
        Map<String,Object> scrollingData = (ArrayMap<String,Object>)data;

        long currentTime = SystemClock.uptimeMillis();
        long lastTime = (long)scrollingData.get("last_action");
        float tick = (float)scrollingData.get("tick") ;
        scrollingData.put("last_action",currentTime);
        return nextScrollingLocationByFrac(con, (currentTime-lastTime)*1.0f/(1000.0f*tick), finished);
    }

    public static PointF nextScrollingLocationByFrac(OBControl con, float frac, MutableBoolean finished)
    {
        PointF loc = OBMisc.copyPoint(con.position());
        Object data = con.propertyValue("scrolling_data");
        if(data == null)
        {
            finished.value = true;
            return loc;
        }
        Map<String,Object> scrollingData = (ArrayMap<String,Object>)data;
        float speedX = (float)scrollingData.get("speed_x");
        float speedY = (float)scrollingData.get("speed_y");
        float minSpeedX = (float)scrollingData.get("min_speed_x");
        float minSpeedY = (float)scrollingData.get("min_speed_y");
        if(speedX == 0 && speedY == 0)
        {
            finished.value = true;
            return loc;
        }
        loc.x += speedX * frac;
        loc.y += speedY * frac;
        double decay = (double)scrollingData.get("decay");
        double decayFrac = Math.pow(decay,frac);
        speedX *= decayFrac;
        speedY *= decayFrac;
        if(minSpeedX >= Math.abs(speedX))
            speedX = 0;
        if(minSpeedY >= Math.abs(speedY))
            speedY = 0;
        scrollingData.put("speed_x", speedX);
        scrollingData.put("speed_y", speedY);
        finished.value = speedX == 0 && speedY == 0;
        return loc;
    }

}
