package org.onebillion.onecourse.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.onebillion.onecourse.mainui.MainActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/08/16.
 */
public class DBSQL
{
    public final static String TABLE_UNITS = "units";
    public final static String TABLE_PREFERENCES = "preferences";
    public final static String TABLE_UNIT_INSTANCES = "unitinstances";
    public final static String TABLE_SESSIONS = "sessions";
    public final static String TABLE_USERS = "users";
    public final static String TABLE_STARS = "stars";

    private SQLiteDatabase database;

    public DBSQL(boolean writable)
    {
        OBSQLiteHelper helper =  OBSQLiteHelper.getSqlHelper();
        database =  writable ? helper.getWritableDatabase() : helper.getReadableDatabase();
    }

    private static String mapToWhereStatement(Map<String,String> whereMap)
    {
        if(whereMap == null)
            return null;

        StringBuilder whereStringBuilder = new StringBuilder();
        for(String val : whereMap.keySet())
        {
            if(whereStringBuilder.length() == 0)
            {
                whereStringBuilder.append(String.format("%s = ?",val));
            }
            else
            {
                whereStringBuilder.append(String.format(" AND %s = ?",val));
            }
        }

        return whereStringBuilder.toString();
    }

    private static String[] mapToWhereValues(Map<String,String> whereMap)
    {
        if(whereMap == null)
            return new String[0];

        return whereMap.values().toArray(new String[whereMap.size()]);
    }

    public long doInsertOnTable(String table, ContentValues insertValues)
    {
        long rowId = database.insert(table,null,insertValues);
        return rowId;
    }

    public long doReplaceOnTable(String table, ContentValues insertValues)
    {
        long rowId = database.replace(table,null,insertValues);
        return rowId;
    }

    public long doUpdateOnTable(String table, Map<String,String> whereMap, ContentValues updateValues)
    {
        long result = database.update(table,updateValues,mapToWhereStatement(whereMap),mapToWhereValues(whereMap));
        return result;
    }

    public Cursor doSelectOnTable(String table, List<String> columns, Map<String,String> whereMap)
    {
        return database.query(table,columns.toArray(new String[columns.size()]),mapToWhereStatement(whereMap),mapToWhereValues(whereMap),null,null,null);
    }

    public Cursor doSelectOnTable(String table, List<String> columns, Map<String,String> whereMap, String orderBy)
    {
        return database.query(table,columns.toArray(new String[columns.size()]),mapToWhereStatement(whereMap),mapToWhereValues(whereMap),null,null,orderBy);
    }

    public Cursor doSelectOnTable(String table, List<String> columns, Map<String,String> whereMap, String groupBy , String orderBy)
    {
        return database.query(table,columns.toArray(new String[columns.size()]),mapToWhereStatement(whereMap),mapToWhereValues(whereMap),groupBy,null,orderBy);
    }

    public long doDeleteOnTable(String table, Map<String,String> whereMap)
    {
        long result = database.delete(table,mapToWhereStatement(whereMap),mapToWhereValues(whereMap));
        return result;
    }

    public Cursor prepareRawQuery(String query, List<String> args)
    {
        return database.rawQuery(query,args == null ? null :args.toArray(new String[args.size()]));
    }

    public static void deleteDB()
    {
        MainActivity.mainActivity.getApplicationContext().deleteDatabase(OBSQLiteHelper.DATABASE_NAME);
    }

    public void beginTransaction()
    {
        database.beginTransaction();
    }

    public void setTransactionSuccessful()
    {
        database.setTransactionSuccessful();
    }

    public void commitTransaction()
    {
        database.endTransaction();
    }

    public void close()
    {
        database.close();
    }
}
