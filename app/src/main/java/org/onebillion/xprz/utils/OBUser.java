package org.onebillion.xprz.utils;

import java.util.*;

public class OBUser
{
    public int key;
    public String name;
    int colour;
    List<Object> results;
    Map<String,Object>settings;

    public OBUser(int k)
    {
        key = k;
        results = new ArrayList<Object>();
        settings = new HashMap<String, Object>();
    }

}
