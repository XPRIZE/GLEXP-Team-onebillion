package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michal on 09/08/16.
 */
public class MlUnit extends MlObject
{
    public String key, icon, params, config, target, lang;
    public float targetDuration, passThreshold;
    public int catAudio, level, unitid;


    private static final String[] stringFields = {"key","icon","target","params","config","lang"};
    private static final String[] intFields = {"unitid","level","catAudio","targetDuration"};
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

    public static boolean insertUnitFromXMLNodeintoDB(DBSQL db, OBXMLNode node, long unitid, int level)
    {
        ContentValues contentValues = contentValuesForNode(node,stringFields,intFields,null,floatFields,dbToXmlConvert);
        contentValues.put("unitid", unitid);
        contentValues.put("level", level);
        boolean result = db.doInsertOnTable(DBSQL.TABLE_UNITS,contentValues) > -1;
        return result;
    }

    public static ContentValues XMLNodeToContentValues(OBXMLNode node, long unitid, int level)
    {
        ContentValues contentValues = contentValuesForNode(node, stringFields, intFields, null, floatFields, dbToXmlConvert);
        contentValues.put("unitid", unitid);
        contentValues.put("level", level);
        return contentValues;
    }

    public static MlUnit mlUnitFromDBforUnitID(long unitid)
    {
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("unitid",String.valueOf(unitid));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS,allFieldNames(stringFields,intFields,null,floatFields),whereMap);
        if(cursor.moveToFirst())
        {
            MlUnit unit = mlUnitFromCursor(cursor);
            cursor.close();
            db.close();
            return unit;
        }
        else
        {
            cursor.close();
            db.close();
            return null;
        }
    }

}
