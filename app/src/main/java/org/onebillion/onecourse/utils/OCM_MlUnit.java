package org.onebillion.onecourse.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import org.onebillion.onecourse.mainui.MainActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by michal on 09/08/16.
 */
public class OCM_MlUnit extends DBObject
{
    public String key, icon, params, config, target, lang;
    public float passThreshold;
    public int masterlistid, catAudio, week, unitid, unitIndex, targetDuration;

    private static final String[] stringFields = {"key","icon","target","params","config","lang"};
    private static final String[] intFields = {"masterlistid","unitid","week","targetDuration","unitIndex"};
    private static final String[] floatFields = {"passThreshold"};
    private static final Map<String,String> dbToXmlConvert;
    static
    {
        dbToXmlConvert = new HashMap<String, String>();
        dbToXmlConvert.put("key","id");
    }

    public static int unitCountForMasterlist(DBSQL db, int masterlistid)
    {
        int result = 0;
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("masterlistid",String.valueOf(masterlistid));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS, Arrays.asList("MAX(unitindex) as unitindex"),whereMap);
        if(cursor.moveToFirst())
        {
            result = cursor.getInt(cursor.getColumnIndex("unitindex"));
        }
        cursor.close();
        //
        return result;
    }

    public static OCM_MlUnit mlUnitFromCursor(Cursor cursor)
    {
        OCM_MlUnit unit = new OCM_MlUnit();
        unit.cursorToObject(cursor,stringFields,intFields,null,floatFields);
        return unit;
    }

    public static OCM_MlUnit mlUnitFromXMLNode(OBXMLNode node)
    {
        OCM_MlUnit unit = new OCM_MlUnit();
        unit.xmlNodeToObject(node,stringFields,intFields,null,floatFields);
        return unit;
    }

    public static boolean insertUnitFromXMLNodeintoDB(DBSQL db, OBXMLNode node, int masterlistid, int unitIndex, int week)
    {
        ContentValues contentValues = contentValuesForNode(node,stringFields,intFields,null,floatFields,dbToXmlConvert);
        contentValues.put("masterlistid", masterlistid);
        contentValues.put("unitIndex", unitIndex);
        contentValues.put("week", week);
        boolean result = db.doInsertOnTable(DBSQL.TABLE_UNITS,contentValues) > -1;
        return result;
    }

    public static OCM_MlUnit mlUnitforUnitID(long unitid)
    {

        OCM_MlUnit unit = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            unit = mlUnitforUnitIDFromDB(db,unitid);
        }
        catch(Exception e)
        {
            MainActivity.log("OCM_MlUnit: database access error: " + e.getMessage());
        }
        finally
        {
            if(db != null)
                db.close();
        }

        return unit;
    }

    public static OCM_MlUnit mlUnitforMasterlistID(int masterlistid, int unitIndex)
    {

        OCM_MlUnit unit = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            unit = mlUnitforMasterlistIDFromDB(db,masterlistid, unitIndex);
        }
        catch(Exception e)
        {
            MainActivity.log("OCM_MlUnit: database access error: " + e.getMessage());
        }
        finally
        {
            if(db != null)
                db.close();
        }

        return unit;
    }

    public static  OCM_MlUnit mlUnitforUnitIDFromDB(DBSQL db, long unitid)
    {
        OCM_MlUnit unit = null;
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

    public static  OCM_MlUnit mlUnitforMasterlistIDFromDB(DBSQL db, int masterlistid, int unitIndex)
    {
        OCM_MlUnit unit = null;
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

    public static OCM_MlUnit nextMlUnitFromDB(DBSQL db, int masterlistid, int unitIndex)
    {
        OCM_MlUnit unit = null;
        Cursor cursor = db.prepareRawQuery(String.format("SELECT * FROM %s "+
                        "WHERE masterlistid = ? AND unitIndex > ? " +
                        "ORDER BY unitIndex ASC LIMIT 1"
                ,DBSQL.TABLE_UNITS),Arrays.asList(String.valueOf(masterlistid),String.valueOf(unitIndex)));
        if(cursor.moveToFirst())
        {
            unit = mlUnitFromCursor(cursor);
        }
        cursor.close();
        return unit;
    }

    public String pathToIcon(boolean small)
    {
        String imgName = String.format(small ? "%s_small" : "%s", this.icon);
        String mlname = OBConfigManager.sharedManager.getMasterlist();
        String fullPath = String.format("masterlists/%s/icons/%s.%s", mlname, imgName, "png");
        if (OBUtils.fileExistsAtPath(fullPath))
        {
            return fullPath;
        }
        return null;
    }

}
