package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.Collections;
import java.util.Map;

/**
 * Created by michal on 09/08/16.
 */
public class MlUnitInstance extends MlObject
{
    public long unitid;
    public int userid, sessionid, seqno, elapsedtime;
    public float score;
    public long starttime, endtime;

    private static final String[] intFields = {"userid","sessionid","seqno","elapsedtime"};
    private static final String[] floatFields = {"score"};
    private static final String[] longFields = {"starttime","endtime","unitid"};

    public MlUnitInstance()
    {
        score = 0;
        elapsedtime = 0;
        starttime = 0;
        endtime = 0;
    }


    public static MlUnitInstance initMlUnitDBWith(int userid, long unitid, int sessionid, long starttime)
    {
        MlUnitInstance mlui = new MlUnitInstance();
        mlui.userid = userid;
        mlui.unitid = unitid;
        mlui.sessionid = sessionid;

        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("unitid",String.valueOf(unitid));
        DBSQL db = new DBSQL(true);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES, Collections.singletonList("MAX(seqno) as seqno"),whereMap);
        if(cursor.moveToFirst())
        {
            mlui.seqno = cursor.getInt(cursor.getColumnIndex("seqno")) + 1;
        }
        else
        {
            mlui.seqno = 0;
        }
        mlui.starttime = starttime;
        if(mlui.saveToDB(db))
        {
            cursor.close();
            db.close();
            return mlui;
        }
        else
        {

            cursor.close();
            db.close();
            return null;
        }
    }

    public static MlUnitInstance mlUnitInstanceFromDBFor(int userid, long unitid, int seqno)
    {
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("unitid",String.valueOf(unitid));
        whereMap.put("seqno",String.valueOf(seqno));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES,allFieldNames(null,intFields,longFields,floatFields),whereMap);

        MlUnitInstance mlui = null;
        if(cursor.moveToFirst())
        {
            mlui = new MlUnitInstance();
            mlui.cursorToObject(cursor,null,floatFields,intFields,longFields);
        }
        cursor.close();
        db.close();
        return mlui;
    }

    public boolean updateDataInDB()
    {
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("unitid",String.valueOf(unitid));
        whereMap.put("seqno",String.valueOf(seqno));

        ContentValues contentValues = new ContentValues();
        contentValues.put("endtime",endtime);
        contentValues.put("score",score);
        contentValues.put("elapsedtime",elapsedtime);
        DBSQL db = new DBSQL(true);
        boolean result = db.doUpdateOnTable(DBSQL.TABLE_UNIT_INSTANCES,whereMap,contentValues) > -1;
        db.close();
        return result;
    }

    public Boolean saveToDB(DBSQL db)
    {
        ContentValues contentValues = getContentValues(null,intFields,longFields,floatFields);
        boolean result = db.doInsertOnTable(DBSQL.TABLE_UNIT_INSTANCES,contentValues) > -1;
        return result;
    }

    public static long lastPlayedUnitIndexForUserIDInDB(DBSQL db, int userid)
    {
        Cursor cursor = db.prepareRawQuery("SELECT unitid, MAX(starttime) FROM unitinstances WHERE userid = ? GROUP BY userid", Collections.singletonList(String.valueOf(userid)));
        int maxId = 0;
        if(cursor.moveToFirst())
        {
            maxId = cursor.getInt(cursor.getColumnIndex("unitid"));
        }
        cursor.close();

        return maxId;
    }


}
