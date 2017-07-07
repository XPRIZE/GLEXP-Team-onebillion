package org.onebillion.onecourse.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import org.onebillion.onecourse.mainui.OBSectionController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Created by michal on 09/08/16.
 */
public class MlUnitInstance extends DBObject
{
    public long unitid;
    public int userid, sessionid, seqNo, elapsedTime;
    public float score;
    public long startTime, endTime;

    public OBSectionController sectionController;
    public MlUnit mlUnit;

    private static final String[] intFields = {"userid","sessionid","seqNo","elapsedTime"};
    private static final String[] floatFields = {"score"};
    private static final String[] longFields = {"startTime","endTime","unitid"};

    public MlUnitInstance()
    {
        score = 0;
        elapsedTime = 0;
        startTime = 0;
        endTime = 0;
    }


    public static MlUnitInstance initWithMlUnit(MlUnit unit, int userid, int sessionid, long starttime)
    {
       return initInDBWithMlUnit(unit,userid,sessionid,starttime);
    }

    private static MlUnitInstance initInDBWithMlUnit(MlUnit unit, int userid, int sessionid, long starttime)
    {
        MlUnitInstance mlui = new MlUnitInstance();
        mlui.userid = userid;
        mlui.unitid = unit.unitid;
        mlui.sessionid = sessionid;
        mlui.mlUnit = unit;

        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("unitid",String.valueOf(unit.unitid));

        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES, Collections.singletonList("MAX(seqNo) as seqNo"),whereMap);

            int columnIndex = cursor.getColumnIndex("seqNo");
            if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
            {
                mlui.seqNo = cursor.getInt(columnIndex) + 1;
            }
            else
            {
                mlui.seqNo = 0;
            }
            mlui.startTime = starttime;
            if(!mlui.saveToDB(db))
                mlui = null;
            cursor.close();
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
        return mlui;
    }


    public boolean updateDataInDB(DBSQL db)
    {
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("unitid",String.valueOf(unitid));
        whereMap.put("seqNo",String.valueOf(seqNo));

        ContentValues contentValues = new ContentValues();
        contentValues.put("endTime",endTime);
        contentValues.put("score",score);
        contentValues.put("elapsedTime",elapsedTime);

        boolean result = db.doUpdateOnTable(DBSQL.TABLE_UNIT_INSTANCES,whereMap,contentValues) > 0;
        return result;
    }

    public Boolean saveToDB(DBSQL db)
    {
        ContentValues contentValues = getContentValues(null,intFields,longFields,floatFields);
        boolean result = db.doInsertOnTable(DBSQL.TABLE_UNIT_INSTANCES,contentValues) > 0;
        return result;
    }

    public static int lastPlayedUnitIndexForUserIDInDB(DBSQL db, int userid, int masterlist)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT unitIndex FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid WHERE UI.userid = ? AND U.masterlistid = ? ORDER BY UI.startTime DESC LIMIT 1",
                DBSQL.TABLE_UNITS, DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(userid), String.valueOf(masterlist)));
        int maxId = 0;
        int columnIndex = cursor.getColumnIndex("unitIndex");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
        {
            maxId = cursor.getInt(columnIndex);
        }
        cursor.close();

        return maxId;
    }


}
