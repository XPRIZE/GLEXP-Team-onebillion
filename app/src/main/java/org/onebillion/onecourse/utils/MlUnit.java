package org.onebillion.onecourse.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michal on 09/08/16.
 */
public class MlUnit extends DBObject
{
    public String key, icon, params, config, target, lang;
    public float passThreshold;
    public int masterlistid, catAudio, level, unitid, unitIndex, awardStar, startAudio, targetDuration;

    private static final String[] stringFields = {"key","icon","target","params","config","lang"};
    private static final String[] intFields = {"masterlistid","unitid","unitIndex","level","catAudio","targetDuration","awardStar","startAudio"};
    private static final String[] floatFields = {"passThreshold"};
    private static final Map<String,String> dbToXmlConvert;
    static
    {
        dbToXmlConvert = new HashMap<String, String>();
        dbToXmlConvert.put("key","id");
    }

    private static MlUnit mlUnitFromCursor(Cursor cursor)
    {
        MlUnit unit = new MlUnit();
        unit.cursorToObject(cursor,stringFields,intFields,null,floatFields);
        return unit;
    }

    public static MlUnit mlUnitFromXMLNode(OBXMLNode node)
    {
        MlUnit unit = new MlUnit();
        unit.xmlNodeToObject(node,stringFields,intFields,null,floatFields);
        return unit;
    }

    public static boolean insertUnitFromXMLNodeintoDB(DBSQL db, OBXMLNode node, int masterlistid, int unitIndex, int level, int startAudio)
    {
        ContentValues contentValues = contentValuesForNode(node,stringFields,intFields,null,floatFields,dbToXmlConvert);
        contentValues.put("masterlistid", masterlistid);
        contentValues.put("unitIndex", unitIndex);
        contentValues.put("level", level);
        contentValues.put("startAudio", startAudio);
        boolean result = db.doInsertOnTable(DBSQL.TABLE_UNITS,contentValues) > -1;
        return result;
    }
/*
    public static ContentValues XMLNodeToContentValues(OBXMLNode node, long unitid, int level)
    {
        ContentValues contentValues = contentValuesForNode(node, stringFields, intFields, null, floatFields, dbToXmlConvert);
        contentValues.put("unitid", unitid);
        contentValues.put("level", level);
        return contentValues;
    }*/

    public static MlUnit mlUnitforUnitID(long unitid)
    {

        MlUnit unit = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            unit = mlUnitforUnitIDFromDB(db,unitid);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }

        return unit;
    }

    public static MlUnit mlUnitforMasterlistID(int masterlistid, int unitIndex)
    {

        MlUnit unit = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            unit = mlUnitforMasterlistIDFromDB(db,masterlistid, unitIndex);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }

        return unit;
    }

    public static  MlUnit mlUnitforUnitIDFromDB(DBSQL db, long unitid)
    {
        MlUnit unit = null;
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("unitid",String.valueOf(unitid));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS,allFieldNames(stringFields,intFields,null,floatFields),whereMap);
        if(cursor.moveToFirst())
        {
            unit = mlUnitFromCursor(cursor);
        }
        cursor.close();
        return unit;
    }

    public static  MlUnit mlUnitforMasterlistIDFromDB(DBSQL db, int masterlistid, int unitIndex)
    {
        MlUnit unit = null;
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("masterlistid",String.valueOf(masterlistid));
        whereMap.put("unitIndex",String.valueOf(unitIndex));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS,allFieldNames(stringFields,intFields,null,floatFields),whereMap);
        if(cursor.moveToFirst())
        {
            unit = mlUnitFromCursor(cursor);
        }
        cursor.close();
        return unit;
    }

}
