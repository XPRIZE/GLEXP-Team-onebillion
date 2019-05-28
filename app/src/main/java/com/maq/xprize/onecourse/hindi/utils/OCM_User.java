package com.maq.xprize.onecourse.hindi.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by michal on 02/08/2017.
 * Class describes tablet user and what masterlists the users use.
 */

public class OCM_User extends DBObject
{
    public int studylistid, playzonelistid, librarylistid, userid;
    public String name;

    private static final String[] stringFields = {"name"};
    private static final String[] intFields = {"userid", "studylistid", "playzonelistid", "librarylistid"};

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


    public static OCM_User initAndSaveUserInDB(DBSQL db, String name, int studylist, int playzonelist, int librarylist)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("studylistid", studylist);
        contentValues.put("librarylistid", librarylist);
        contentValues.put("playzonelistid", playzonelist);
        long rowId = db.doInsertOnTable(DBSQL.TABLE_USERS,contentValues);
        if(rowId > -1)
        {
            OCM_User user = new OCM_User();
            user.name = name;
            user.userid = (int)rowId;
            user.studylistid = studylist;
            user.librarylistid = librarylist;
            user.playzonelistid = playzonelist;
            return user;
        }
        else
        {
            return null;
        }
    }




}
