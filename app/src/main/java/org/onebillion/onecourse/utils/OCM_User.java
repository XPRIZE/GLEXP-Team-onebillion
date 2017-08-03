package org.onebillion.onecourse.utils;

/**
 * Created by michal on 02/08/2017.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by michal on 18/01/2017.
 */

public class OCM_User extends DBObject
{
    public int masterlistid, userid;
    public String name;

    private static final String[] stringFields = {"name"};
    private static final String[] intFields = {"userid", "masterlistid"};

    public OCM_User()
    {
        super();
    }

    public static OCM_User UserFromDBForID(DBSQL db, int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));

        OCM_User user = null;

        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_USERS, allFieldNames(stringFields,intFields,null,null),whereMap);

        if(cursor.moveToFirst())
        {
            user = new OCM_User();
            user.cursorToObject(cursor,stringFields,intFields,null,null);
        }
        cursor.close();

        return user;
    }


    public static OCM_User initAndSaveUserInDB(DBSQL db, String name)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        long rowId = db.doInsertOnTable(DBSQL.TABLE_USERS,contentValues);
        if(rowId > -1)
        {
            OCM_User user = new OCM_User();
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




}
