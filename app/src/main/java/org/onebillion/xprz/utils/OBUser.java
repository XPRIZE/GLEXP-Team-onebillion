package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.util.ArrayMap;

import java.util.*;

public class OBUser extends MlObject
{
    public String name;
    int colour, userid, currentsessionid;
    Map<String,Object>settings;

    private static final String[] stringFields = {"name"};
    private static final String[] intFields = {"userid"};

    public OBUser()
    {
        settings = new HashMap<String, Object>();
    }

    public boolean saveCertificateInDBwithFile(String fileName, int level)
    {
        ContentValues data = new ContentValues();
        data.put("userid",userid);
        data.put("file",fileName);
        data.put("level",level);
        DBSQL db = new DBSQL(true);
        boolean result =  db.doInsertOnTable("certificates",data) > 0;
        db.close();
        return result;
    }

    public String certificateFromDBforLevel(int level)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("level",String.valueOf(level));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.prepareSelectOnTable("certificates", Collections.singletonList("file"),whereMap);
        String file = null;
        if(cursor.moveToFirst())
        {
            file =  cursor.getString(cursor.getColumnIndex("file"));
        }
        cursor.close();
        db.close();
        return file;
    }

    public int lastCertificateLevelFromDB()
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.prepareSelectOnTable("certificates", Collections.singletonList("MAX(level) as level"),whereMap);
        int level = -1;
        if(cursor.moveToFirst())
        {
            level = cursor.getInt(cursor.getColumnIndex("level"));
        }
        cursor.close();
        db.close();
        return level;
    }

    public static OBUser OBUseruserFromDB(int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.prepareSelectOnTable("users", allFieldNames(stringFields,intFields,null,null),whereMap);
        OBUser user = null;
        if(cursor.moveToFirst())
        {
            user = new OBUser();
            user.cursorToObject(cursor,stringFields,intFields,null,null);
        }
        cursor.close();
        db.close();
        return user;
    }

    public static OBUser initAndSaveUserInDB(String name)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        DBSQL db = new DBSQL(true);
        long rowId = db.doInsertOnTable("users",contentValues);
        db.close();
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

    public static OBUser lastUserFromDB()
    {
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.prepareSelectOnTable("users",allFieldNames(stringFields,intFields,null,null),null);
        OBUser user = null;
        if(cursor.moveToFirst())
        {
            user =new OBUser();
            user.cursorToObject(cursor,stringFields,intFields,null,null);
        }

        cursor.close();
        db.close();
        return user;

    }

    public void startNewSession()
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        int sessionid = 1;

        DBSQL db = new DBSQL(true);
        Cursor cursor = db.prepareSelectOnTable("sessions", Collections.singletonList("MAX(sessionid) as sessionid"), whereMap);
        if (cursor.moveToFirst())
            sessionid = cursor.getInt(cursor.getColumnIndex("sessionid")) + 1;
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put("userid", userid);
        contentValues.put("sessionid", sessionid);
        contentValues.put("starttime", System.currentTimeMillis()/1000);
        db.doInsertOnTable("sessions",contentValues);
        db.close();
        currentsessionid = sessionid;
    }

    public int highestIncompleteUnitIDfromDB()
    {
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.prepareRawQuery("SELECT MAX(unitid) as unitid FROM unitinstances WHERE userid = ? AND endtime > 0",Collections.singletonList(String.valueOf(userid)));
        int returnId = 0;
        if(cursor.moveToFirst())
            returnId = cursor.getInt(cursor.getColumnIndex("unitid")) +1;

        cursor.close();
        db.close();
        return returnId;
    }

}
