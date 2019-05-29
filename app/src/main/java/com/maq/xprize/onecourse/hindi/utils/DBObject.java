package com.maq.xprize.onecourse.hindi.utils;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 09/08/16.
 */
public class DBObject
{
    protected void cursorToObject(Cursor cursor, String[] stringFields, String[] intFields, String[] longFields, String[] floatFields)
    {
        if(stringFields != null)
        {
            for (String string : stringFields)
            {
                int index = cursor.getColumnIndex(string);
                if (index > -1)
                    setStringField(string, cursor.getString(index));
            }
        }

        if(floatFields != null)
        {
            for (String string : floatFields)
            {
                int index = cursor.getColumnIndex(string);
                if (index > -1)
                    setFloatField(string, cursor.getFloat(index));
            }
        }

        if(intFields != null)
        {
            for (String string : intFields)
            {
                int index = cursor.getColumnIndex(string);
                if (index > -1)
                    setIntField(string, cursor.getInt(index));
            }
        }

        if(longFields != null)
        {
            for (String string : longFields)
            {
                int index = cursor.getColumnIndex(string);
                if (index > -1)
                    setLongField(string, cursor.getLong(index));
            }
        }

    }

    protected void xmlNodeToObject(OBXMLNode node, String[] stringFields, String[] intFields, String[] longFields, String[] floatFields)
    {
        if(stringFields != null)
        {
            for (String string : stringFields)
            {
                if(node.attributeStringValue(string) != null)
                    setStringField(string, node.attributeStringValue(string));
            }
        }

        if(floatFields != null)
        {
            for (String string : floatFields)
            {
                setFloatField(string, (float)node.attributeFloatValue(string));
            }
        }

        if(intFields != null)
        {
            for (String string : intFields)
            {

                setIntField(string, node.attributeIntValue(string));
            }
        }

        if(longFields != null)
        {
            for (String string : longFields)
            {
                setLongField(string, node.attributeLongValue(string));
            }
        }

    }

    private void setStringField(String name, String value)
    {
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.set(this, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void setIntField(String name, int value)
    {
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.setInt(this, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void setLongField(String name, long value)
    {
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.setLong(this, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void setFloatField(String name, float value)
    {
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.setFloat(this, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String getStringValue(String name)
    {
        String result = "";
        try {
            Field field = this.getClass().getDeclaredField(name);
            result = (String)field.get(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }
        return result;
    }

    private int getIntValue(String name)
    {
        int result = 0;
        try {
            Field field = this.getClass().getDeclaredField(name);
            result = field.getInt(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }
        return result;
    }

    private float getFloatValue(String name)
    {
        float result = 0.0f;
        try {
            Field field = this.getClass().getDeclaredField(name);
            result = field.getFloat(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }
        return result;
    }

    private long getLongValue(String name)
    {
        long result = 0;
        try {
            Field field = this.getClass().getDeclaredField(name);
            result =  field.getLong(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }
        return result;
    }

    protected static ContentValues contentValuesForNode(OBXMLNode node, String[] stringFields, String[] intFields, String[] longFields, String[] floatFields, Map<String,String> dbToXML)
    {
        ContentValues contentValues = new ContentValues();

        if(stringFields != null)
        {
            for (String fieldName : stringFields)
            {
                String convertedName = dbFieldtoXMLfield(fieldName, dbToXML);
                if (node.attributeStringValue(convertedName) != null)
                    contentValues.put(fieldName, node.attributeStringValue(convertedName));
            }
        }

        if(floatFields != null)
        {
            for (String fieldName : floatFields)
            {
                String convertedName = dbFieldtoXMLfield(fieldName, dbToXML);
                if (node.attributeStringValue(convertedName) != null)
                    contentValues.put(fieldName, node.attributeFloatValue(convertedName));

            }

        }

        if(intFields != null)
        {
            for (String fieldName : intFields)
            {
                String convertedName = dbFieldtoXMLfield(fieldName, dbToXML);
                if (node.attributeStringValue(convertedName) != null)
                    contentValues.put(fieldName, node.attributeIntValue(convertedName));
            }
        }

        if(longFields != null)
        {
            for (String fieldName : longFields)
            {
                String convertedName = dbFieldtoXMLfield(fieldName, dbToXML);
                if (node.attributeStringValue(convertedName) != null)
                    contentValues.put(fieldName, node.attributeIntValue(convertedName));

            }

        }

        return contentValues;
    }

    protected ContentValues getContentValues(String[] stringFields, String[] intFields, String[] longFields, String[] floatFields)
    {
        ContentValues contentValues = new ContentValues();

        if(stringFields != null)
            for(String fieldName : stringFields)
                contentValues.put(fieldName, getStringValue(fieldName));

        if(floatFields != null)
            for(String fieldName : floatFields)
                contentValues.put(fieldName, getFloatValue(fieldName));

        if(intFields != null)
            for(String fieldName : intFields)
                contentValues.put(fieldName, getIntValue(fieldName));


        if(longFields != null)
            for(String fieldName : longFields)
                contentValues.put(fieldName, getLongValue(fieldName));

        return contentValues;
    }

    private static String dbFieldtoXMLfield(String name, Map<String,String> dbToXML)
    {
        if(dbToXML != null && dbToXML.get(name) != null)
            return dbToXML.get(name);
        else
            return name;
    }

    protected static List<String> allFieldNames(String[] stringFields, String[] intFields, String[] longFields, String[] floatFields)
    {
        List<String> list = new ArrayList<>();

        if(stringFields != null)
            list.addAll(Arrays.asList(stringFields));

        if(floatFields != null)
            list.addAll(Arrays.asList(floatFields));

        if(intFields != null)
            list.addAll(Arrays.asList(intFields));

        if(longFields != null)
            list.addAll(Arrays.asList(longFields));
        return list;
    }

}
