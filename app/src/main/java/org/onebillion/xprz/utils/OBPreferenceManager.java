package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by michal on 24/08/16.
 */
public class OBPreferenceManager
{
    public final static String PREFERENCE_ML_TOKEN = "mltoken";

    private final static String COLUMN_NAME = "name";
    private final static String COLUMN_VAL = "val";


    public static boolean setPreference(String name, String val, DBSQL db)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_VAL, val);
        boolean result = db.doReplaceOnTable(DBSQL.TABLE_PREFERENCES,contentValues) > -1;
        return result;
    }


    public static boolean setPreference(String name, String val)
    {
        DBSQL db = null;
        boolean result = false;
        try
        {
            db = new DBSQL(true);
            result = setPreference(name,val,db);
        }
        catch (Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }

        return result;
    }

    public static boolean setPreference(String name, int val)
    {
        return setPreference(name, String.valueOf(val));
    }

    public static boolean setPreference(String name, float val)
    {
        return setPreference(name, String.valueOf(val));
    }

    public static boolean setPreference(String name, boolean val)
    {
        return setPreference(name, String.valueOf(val));
    }

    public static String getStringPreference(String name, DBSQL db)
    {
        Map<String,String> map = new ArrayMap<>();
        map.put(COLUMN_NAME,name);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_PREFERENCES, Arrays.asList(COLUMN_NAME, COLUMN_VAL),map);
        String result = null;
        if(cursor.moveToFirst())
        {
            result = cursor.getString(cursor.getColumnIndex(COLUMN_VAL));
        }
        cursor.close();
        return result;
    }

    public static String getStringPreference(String name)
    {
        String result = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            result = getStringPreference(name, db);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
        return result;
    }

    public static int getIntPreference(String name)
    {
        String result = getStringPreference(name);
        if(result == null)
            return -1;

        try
        {
            int val = Integer.valueOf(result);
            return val;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    public static float getFloatPreference(String name)
    {
        String result = getStringPreference(name);
        if(result == null)
            return -1;

        try
        {
            float val = Float.valueOf(result);
            return val;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    public static boolean getBooleanPreference(String name)
    {
        String result = getStringPreference(name);
        if(result == null)
            return false;

        try
        {
            boolean val = Boolean.valueOf(result);
            return val;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}
