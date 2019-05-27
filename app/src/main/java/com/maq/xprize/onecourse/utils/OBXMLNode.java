package com.maq.xprize.onecourse.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OBXMLNode
{
    public String nodeName,contents;
    public List<OBXMLNode> children;
    public Map<String,String> attributes;

    public OBXMLNode()
    {
        nodeName = "";
        contents = "";
        children = new ArrayList<OBXMLNode>();
        attributes = new HashMap<String,String>();
    }

    public List<OBXMLNode> childrenOfType(String typeName)
    {
        List<OBXMLNode> chlist = new ArrayList<OBXMLNode>();
        for (OBXMLNode xch : children)
            if (xch.nodeName.equals(typeName))
                chlist.add(xch);
        return chlist;
    }

    public String attributeStringValue(String attrName)
    {
        return attributes.get(attrName);
    }

    public double attributeFloatPrefixValue(String attrName)
    {
        String val = attributeStringValue(attrName);
        double dval = 0.0;
        if (val != null)
        {
            Pattern pattern = Pattern.compile("(^[0-9]*\\.*[0-9]*)(.*)");
            Matcher matcher = pattern.matcher(val);
            matcher.find();
            if (matcher.matches())
            {
                String res = matcher.group(1);
                try
                {
                    dval = Double.parseDouble(res);
                }
                catch(NumberFormatException e)
                {
                }
            }
        }
        return dval;
    }
    public double attributeFloatValue(String attrName)
    {
        String val = attributeStringValue(attrName);
        double dval = 0.0;
        if (val != null)
        {
            try
            {
                dval = Double.parseDouble(val);
            }
            catch(NumberFormatException e)
            {
            }
        }
        return dval;
    }

    public int attributeIntValue(String attrName)
    {
        String val = attributeStringValue(attrName);
        int ival = 0;
        if (val != null)
        {
            try
            {
                ival = Integer.parseInt(val);
            }
            catch(NumberFormatException e)
            {
            }
        }
        return ival;
    }


    public long attributeLongValue(String attrName)
    {
        String val = attributeStringValue(attrName);
        long ival = 0;
        if (val != null)
        {
            try
            {
                ival = Long.parseLong(val);
            }
            catch(NumberFormatException e)
            {
            }
        }
        return ival;
    }

    public boolean attributeBoolValue(String attrName)
    {
        String val = attributeStringValue(attrName);
        boolean bval = false;
        if (val != null)
        {
            val = val.toLowerCase();
            bval = (val == "yes" || val == "true" || val == "y");
        }
        return bval;
    }
}