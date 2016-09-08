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
        boolean result =  db.doInsertOnTable(DBSQL.TABLE_CERTIFICATES,data) > 0;
        db.close();
        return result;
    }

    public String certificateFromDBforLevel(int level)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("level",String.valueOf(level));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_CERTIFICATES, Collections.singletonList("file"),whereMap);
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
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_CERTIFICATES, Collections.singletonList("MAX(level) as level"),whereMap);
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
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_USERS, allFieldNames(stringFields,intFields,null,null),whereMap);
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
        long rowId = db.doInsertOnTable(DBSQL.TABLE_USERS,contentValues);
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
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_USERS,allFieldNames(stringFields,intFields,null,null),null);
        OBUser user = null;
        if(cursor.moveToFirst())
        {
            user =new OBUser();
            user.cursorToObject(cursor,stringFields,intFields,null,null);
        }
        cursor.close();

        if(user != null)
             user.currentsessionid = user.lastSessionIDFromDB(db);

        db.close();
        return user;

    }

    public void startNewSessionInDB(long starttime)
    {
        DBSQL db = new DBSQL(true);

        int sessionid = lastSessionIDFromDB(db);
        if(sessionid<0)
            sessionid = 1;
        else
            sessionid++;

        ContentValues contentValues = new ContentValues();
        contentValues.put("userid", userid);
        contentValues.put("sessionid", sessionid);
        contentValues.put("starttime",starttime);
        db.doInsertOnTable(DBSQL.TABLE_SESSIONS,contentValues);
        db.close();
        currentsessionid = sessionid;
    }

    private int lastSessionIDFromDB(DBSQL db)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        int sessionid = -1;

        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_SESSIONS, Collections.singletonList("MAX(sessionid) as sessionid"), whereMap);
        if (cursor.moveToFirst())
            sessionid = cursor.getInt(cursor.getColumnIndex("sessionid"));
        cursor.close();
        return sessionid;
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

    public boolean currentSessionHasProgress()
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("sessionid",String.valueOf(currentsessionid));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES,Collections.singletonList("COUNT(*) as count"),whereMap);
        boolean result = false;
        if(cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex("count")) > 0;

        cursor.close();
        db.close();
        return result;
    }

    public String starFromDBForLevel(int level,int starnum)
    {
        DBSQL db = new DBSQL(false);
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("level",String.valueOf(level));
        whereMap.put("starnum",String.valueOf(starnum));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS,Collections.singletonList("colour"),whereMap);
        String result = null;
        if(cursor.moveToFirst())
            result = cursor.getString(cursor.getColumnIndex("colour"));

        cursor.close();
        db.close();
        return result;

    }

    public Map<Integer,String> starsFromDBForLevel(int level)
    {
        DBSQL db = new DBSQL(false);
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("level",String.valueOf(level));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS,Arrays.asList("starnum","colour"),whereMap);

        Map<Integer,String> result = new ArrayMap<>();
        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                result.put(cursor.getInt(cursor.getColumnIndex("starnum")), cursor.getString(cursor.getColumnIndex("colour")));
                cursor.moveToNext();
            }

        }

        cursor.close();
        db.close();
        return result;

    }

    public String lastStarColourFromDBForLevel(int level)
    {
        DBSQL db = new DBSQL(false);
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("level",String.valueOf(level));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS,Arrays.asList("MAX(starnum) as starnum","colour"),whereMap);

        String result = null;
        if(cursor.moveToFirst())
            result = cursor.getString(cursor.getColumnIndex("colour"));

        cursor.close();
        db.close();
        return result;

    }

    public boolean saveStarInDBForLevel(int level,int starnum,String colour)
    {
        DBSQL db = new DBSQL(true);
        ContentValues contentValues = new ContentValues();
        contentValues.put("userid",userid);
        contentValues.put("level",level);
        contentValues.put("starnum",starnum);
        contentValues.put("colour",colour);

        boolean result = db.doReplaceOnTable(DBSQL.TABLE_STARS,contentValues) > 0;

        db.close();
        return result;

    }
}
