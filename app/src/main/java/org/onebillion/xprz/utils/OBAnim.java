package org.onebillion.xprz.utils;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.graphics.PointF;

import org.onebillion.xprz.controls.OBGroup;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by alan on 19/10/15.
 */
public class OBAnim {
    public static int ANIM_TYPE_FLOAT = 0,
            ANIM_TYPE_POINT = 1,
            ANIM_TYPE_COLOUR = 2,
            ANIM_TYPE_SEQUENCE = 3,
            ANIM_TYPE_INT = 4,
    ANIM_TYPE_BLOCK = 5;
    public static int ANIM_EASE_IN_EASE_OUT = 0,
            ANIM_LINEAR = 1,
            ANIM_EASE_IN = 2,
            ANIM_EASE_OUT = 3;

    public Object object, value, initialValue, tempValue;
    public int typeType;
    public String key;
    public Method getter, setter;
    TypeEvaluator evaluator;

    public OBAnim(Object obj, String ky, int ty)
    {
        object = obj;
        key = ky;
        typeType = ty;
        if (ky == null || obj == null)
            return;
        String u1 = ky.substring(0, 1).toUpperCase() + ky.substring(1);
        try
        {
            getter = obj.getClass().getMethod("get" + u1);
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                getter = obj.getClass().getMethod(ky);
            } catch (NoSuchMethodException nsm) {
            }
        }
        try {
            Class x = null;
            if (typeType == ANIM_TYPE_FLOAT)
            {
                x = float.class;
                evaluator = new FloatEvaluator();
            }
            else if (typeType == ANIM_TYPE_POINT)
            {
                x = PointF.class;
                //evaluator = new PointFEvaluator(new PointF());
                evaluator = null;
            }
            else if (typeType == ANIM_TYPE_COLOUR)
            {
                evaluator = new ArgbEvaluator();
                x = int.class;
            }
            else if (typeType == ANIM_TYPE_INT)
            {
                evaluator = new IntEvaluator();
                x = int.class;
            }
            else if (typeType == ANIM_TYPE_SEQUENCE)
            {
                evaluator = new IntEvaluator();
                x = int.class;
            }
            if (x != null)
                setter = obj.getClass().getMethod("set" + u1, x);
        } catch (NoSuchMethodException e) {
        }
    }

    public static OBAnim opacityAnim(float val, Object obj) {
        OBAnim anim = new OBAnim(obj, "opacity", ANIM_TYPE_FLOAT);
        anim.value = val;
        return anim;
    }

    public static OBAnim scaleAnim(float val, Object obj) {
        OBAnim anim = new OBAnim(obj, "scale", ANIM_TYPE_FLOAT);
        anim.value = val;
        return anim;
    }

    public static OBAnim rotationAnim(float val, Object obj) {
        OBAnim anim = new OBAnim(obj, "rotation", ANIM_TYPE_FLOAT);
        anim.value = val;
        return anim;
    }

    public static OBAnim propertyAnim(String property,float val, Object obj)
    {
        OBAnim anim = new OBAnim(obj, property, ANIM_TYPE_FLOAT);
        anim.value = val;
        return anim;
    }

    public static OBAnim propertyIntAnim(String property,int val, Object obj)
    {
        OBAnim anim = new OBAnim(obj, property, ANIM_TYPE_INT);
        anim.value = val;
        return anim;
    }

    public static OBAnim moveAnim(PointF val, Object obj) {
        OBAnim anim = new OBAnim(obj, "position", ANIM_TYPE_POINT);
        anim.value = new PointF(val.x, val.y);
        anim.tempValue = new PointF();
        return anim;
    }

    public static OBAnim pathMoveAnim(Object obj, Path p,boolean changle,float offsetradians)
    {
        return new OBAnimPath(obj,p,changle,offsetradians);
    }

    public static OBAnim sequenceAnim(OBGroup obj, List<String> frames, float delay, boolean repeat)
    {
        return new OBAnimSequence(obj,frames,delay,repeat);
    }

    public void retrieveInitialValue()
    {
        if (object != null && key != null)
        {
            try
            {
                initialValue = getter.invoke(object);
                if (initialValue instanceof PointF)
                {
                    PointF tpt = (PointF) initialValue;
                    initialValue = new PointF(tpt.x,tpt.y);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public Object valueForT(float t)
    {
        if (typeType == ANIM_TYPE_FLOAT || typeType == ANIM_TYPE_INT)
        {
            return evaluator.evaluate(t, initialValue, value);
            //return OB_Maths.interpolateVal(((Number)initialValue).floatValue(),((Number)value).floatValue(),t);
        }
        if (typeType == ANIM_TYPE_POINT)
        {
            //return evaluator.evaluate(t, initialValue, value);
            return OB_Maths.tPointAlongLine(t,(PointF)initialValue,(PointF)value);
        }
        if (typeType == ANIM_TYPE_COLOUR)
        {
            return (ArgbEvaluator) evaluator.evaluate(t, initialValue, value);
        }
        return null;
    }
}
