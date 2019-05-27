package com.maq.xprize.onecourse.utils;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OBXMLManager
{
    private ArrayList<OBXMLNode>nodeStack;

    public OBXMLManager()
    {
        nodeStack = new ArrayList<OBXMLNode>();
    }

    public List<OBXMLNode> parseFile(InputStream is) throws XmlPullParserException, IOException
    {
        ArrayList<OBXMLNode>nodeList = new ArrayList<OBXMLNode>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(is, null);
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_TAG)
            {
                OBXMLNode node = new OBXMLNode();
                node.nodeName = parser.getName();
                int act = parser.getAttributeCount();
                HashMap<String, String> attrs = new HashMap<String, String>();
                for (int i = 0;i < act;i++)
                {
                    String aname = parser.getAttributeName(i);
                    String val = parser.getAttributeValue(i);
                    attrs.put(aname, val);
                }
                node.attributes = attrs;
                nodeStack.add(node);
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                OBXMLNode node = nodeStack.get(nodeStack.size()-1);
                nodeStack.remove(nodeStack.size()-1);
                if (nodeStack.size() > 0)
                {
                    OBXMLNode parentNode = nodeStack.get(nodeStack.size()-1);
                    parentNode.children.add(node);
                }
                else
                    nodeList.add(node);
            }
            else if (eventType == XmlPullParser.TEXT)
            {
                OBXMLNode node = nodeStack.get(nodeStack.size()-1);
                node.contents = parser.getText();
            }
            eventType = parser.next();
        }
        return nodeList;
    }

    private static Map<String, Object>processDictNode(OBXMLNode node) throws Exception
    {
        Map<String, Object> dict = new HashMap<String, Object>();
        List<OBXMLNode> kids = node.children;
        for (int i = 0;i < kids.size() - 1;i+=2)
        {
            OBXMLNode keyNode = kids.get(i);
            if (!keyNode.nodeName.equals("key"))
                throw new Exception("Missing key in plist dictionary");
            OBXMLNode valueNode = kids.get(i+1);
            dict.put(keyNode.contents, processNode(valueNode));
        }
        return dict;
    }

    private static List<Object>processArrayNode(OBXMLNode node) throws Exception
    {
        List<Object> array = new ArrayList<Object>();
        List<OBXMLNode> kids = node.children;
        for (int i = 0;i < kids.size();i++)
        {
            OBXMLNode anode = kids.get(i);
            array.add(processNode(anode));
        }
        return array;
    }

    private static Object processNode(OBXMLNode node) throws Exception
    {
        String nodeType = node.nodeName;
        if (nodeType.equals("string"))
            return node.contents;
        if (nodeType.equals("integer"))
            return Integer.valueOf(node.contents);
        if (nodeType.equals("real"))
            return Double.valueOf(node.contents);
        if (nodeType.equals("dict"))
            return processDictNode(node);
        if (nodeType.equals("array") || nodeType.equals("plist"))
            return processArrayNode(node);
        throw new Exception("Unknown plist type");
    }

    public Object parsePlist(InputStream is) throws Exception
    {
        List<OBXMLNode> nodeList = parseFile(is);
        if (nodeList.size() == 1)
        {
            OBXMLNode plistnode = nodeList.get(0);
            if (plistnode.nodeName.equals("plist"))
            {
                @SuppressWarnings("unchecked")
                List<Object> plist = (List<Object>) processNode(plistnode);
                return plist.get(0);
            }
        }
        return null;
    }
}
