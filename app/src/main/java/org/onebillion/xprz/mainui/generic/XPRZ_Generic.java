package org.onebillion.xprz.mainui.generic;

import android.graphics.PointF;
import android.os.SystemClock;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class XPRZ_Generic
{
    public static enum Anchor
    {
        ANCHOR_MIDDLE,
        ANCHOR_LEFT,
        ANCHOR_RIGHT,
        ANCHOR_TOP,
        ANCHOR_BOTTOM;

        public final int anchor;

        Anchor()
        {
            this.anchor = 1 << this.ordinal();
        }
    }


    public static void pointer_moveToObjectByName(String controlName, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait, XPRZ_SectionController sc)
    {
        OBControl control = sc.objectDict.get(controlName);
        pointer_moveToObject(control, angle, secs, anchorFlags, wait, sc);
    }


    public static void pointer_moveToObject(OBControl control, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait, XPRZ_SectionController sc)
    {
        PointF position = copyPoint(control.position());
        //
        if (anchorFlags.contains(Anchor.ANCHOR_LEFT)) position.x -= control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_RIGHT)) position.x += control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_TOP)) position.y -= control.height() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_BOTTOM)) position.y += control.height() / 2;
        //
        sc.movePointerToPoint(position, angle, secs, wait);
    }


    public static void pointer_moveToPointWithObject(OBControl control, PointF destination, float rotation, float secs, Boolean wait, XPRZ_SectionController sc)
    {
        OBAnim anim = OBAnim.moveAnim(destination, control);
        OBAnimationGroup.runAnims(Arrays.asList(anim), secs, false, OBAnim.ANIM_EASE_IN_EASE_OUT, sc);
        sc.movePointerToPoint(destination, rotation, secs, true);
    }



    public static float getNextZPosition(XPRZ_SectionController sc)
    {
        float maxZPosition = 0.0f;
        for (OBControl control : sc.objectDict.values())
        {
            maxZPosition = Math.max(maxZPosition, control.zPosition());
        }
        return maxZPosition + 0.001f;
    }

    public static void sendObjectToTop(OBControl control, XPRZ_SectionController sc)
    {
        control.setZPosition(getNextZPosition(sc));
    }

    public static PointF copyPoint(PointF original)
    {
        return new PointF(original.x, original.y);
    }




    public static double currentTime()
    {
        return (SystemClock.uptimeMillis() / (double) 1000);
    }

}
