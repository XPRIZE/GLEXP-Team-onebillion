package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.GestureUtils;
import android.util.ArrayMap;

import java.util.*;

public class OBUser extends MlObject
{
    public String name;
    int colour, userid;
    Map<String,Object>settings;

    private static final String[] stringFields = {"name"};
    private static final String[] intFields = {"userid"};

    public OBUser()
    {
        settings = new HashMap<String, Object>();
    }


    public static OBUser OBUserForID(int userid)
    {
        return OBUserFromDBForID(userid);
    }

    private static OBUser OBUserFromDBForID(int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));

        OBUser user = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_USERS, allFieldNames(stringFields,intFields,null,null),whereMap);

            if(cursor.moveToFirst())
            {
                user = new OBUser();
                user.cursorToObject(cursor,stringFields,intFields,null,null);
            }
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

        return user;
    }


    public static OBUser initAndSaveUserInDB(DBSQL db, String name)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        long rowId = db.doInsertOnTable(DBSQL.TABLE_USERS,contentValues);
        if(rowId > -1)
        {
            OBUser user = new OBUser();
            user.name = name;
            user.userid = (int)rowId;
            return user;
        }
        else
        {
            return null;
        }
    }

    public static OBUser lastUserFromDB(DBSQL db)
    {
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_USERS,allFieldNames(stringFields,intFields,null,null),null);
        OBUser user = null;
        if(cursor.moveToFirst())
        {
            user =new OBUser();
            user.cursorToObject(cursor,stringFields,intFields,null,null);
        }
        cursor.close();
        return user;

    }

    public int lastUnitIDFromDB(DBSQL db)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES,Collections.singletonList("MAX(unitid) as unitid"),whereMap);
        int returnId = 0;
        if(cursor.moveToFirst())
            returnId = cursor.getInt(cursor.getColumnIndex("unitid"));

        cursor.close();

        return returnId;
    }


}
