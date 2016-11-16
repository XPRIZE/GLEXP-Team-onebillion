package org.onebillion.onecourse.controls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 07/06/16.
 */
public class OBCharacter
{
    public OBGroup control;
    public Map<String,Object> properties;
    public OBCharacter()
    {
        properties = new HashMap<>();
    }
    public static OBCharacter characterWithGroup(OBGroup g)
    {
        OBCharacter c = new OBCharacter();
        c.control = g;
        return c;
    }

    public void showAnatomy(String parent,String chp,int idx)
    {
        String targetId = String.format("%s%d",chp,idx);
        showOnly(targetId, (OBGroup) control.objectDict.get(parent));
        control.needsRetexture = true;
        control.invalidate();
    }

    public void showOnly(String nm,OBGroup parent)
    {
        control.lockScreen();
        OBGroup g;
        if (parent == null)
            g = control;
        else
            g = parent;
        if (g != null)
            for (OBControl m : g.members)
            {
                m.setHidden(!(m.propertyValue("name").equals(nm)));
            }
        control.unlockScreen();
    }

    public void showOnly(String nm)
    {
        OBControl c = control.objectDict.get(nm);
        showOnly(nm,c.parent);
    }

}
