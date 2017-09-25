package org.onebillion.onecourse.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.Arrays;
import java.util.Map;


/**
 * Created by michal on 18/01/2017.
 */

public class OC_User extends DBObject
{
    public int masterlistid, userid;
    public String name;

    private static final String[] stringFields = {"name"};
    private static final String[] intFields = {"userid", "masterlistid"};

    public OC_User()
    {
        super();
    }

    public static OC_User UserFromDBForID(DBSQL db, int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));

        OC_User user = null;

        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_USERS, allFieldNames(stringFields,intFields,null,null),whereMap);

        if(cursor.moveToFirst())
        {
            user = new OC_User();
            user.cursorToObject(cursor,stringFields,intFields,null,null);
        }
        cursor.close();

        return user;
    }


    public static OC_User initAndSaveUserInDB(DBSQL db, String name)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        long rowId = db.doInsertOnTable(DBSQL.TABLE_USERS,contentValues);
        if(rowId > -1)
        {
            OC_User user = new OC_User();
            user.name = name;
            user.userid = (int)rowId;
            user.masterlistid = 1;
            return user;
        }
        else
        {
            return null;
        }
    }

    public int lastUnitIndexFromDB(DBSQL db, int masterlistid)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT MAX(unitIndex) AS unitIndex FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid WHERE UI.userid = ? AND U.masterlistid = ?",
                DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES), Arrays.asList(String.valueOf(this.userid),String.valueOf(masterlistid)));
        int returnId = 0;
        int columnIndex = cursor.getColumnIndex("unitIndex");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
            returnId = cursor.getInt(columnIndex);

        cursor.close();

        return returnId;
    }


}
