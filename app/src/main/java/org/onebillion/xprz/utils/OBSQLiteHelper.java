package org.onebillion.xprz.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.format.DateFormat;
import android.widget.Toast;

import org.onebillion.xprz.R;
import org.onebillion.xprz.mainui.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by michal on 11/08/16.
 */
public class OBSQLiteHelper extends SQLiteOpenHelper
{
    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "unitDB";
    private Context cont;
    public OBSQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        cont = context;
    }

    private static OBSQLiteHelper sqlHelper;

    public static OBSQLiteHelper getSqlHelper()
    {
        if(sqlHelper == null)
            sqlHelper = new OBSQLiteHelper(MainActivity.mainActivity);

        return sqlHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        InputStream inputStream = cont.getResources().openRawResource(R.raw.tables);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        StringBuilder builder = new StringBuilder();
        try
        {
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }

            String rawSql = builder.toString();

            String[] sqls = rawSql.split(";");
            for (String sql : sqls)
                if (!sql.isEmpty())
                    database.execSQL(sql);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        // onCreate(db);
    }

    public void runMaintenance()
    {
        if (runConsistencyChecks())
        {
            backupDatabase();
        }
    }

    public boolean runConsistencyChecks()
    {
        MainActivity.log("OBSQLiteHelper running consistency checks");
        //
        try
        {
            Cursor checkResult = runVacuum();
            if (checkResult != null)
            {
                MainActivity.log("OBSQLiteHelper Vacuum FAILED: " + DatabaseUtils.dumpCursorToString(checkResult));
                return false;
            }
            else
            {
                MainActivity.log("OBSQLiteHelper Vacuum PASSED");
            }
            //
            checkResult = integrityCheck();
            if (checkResult != null)
            {
                MainActivity.log("OBSQLiteHelper Integrity check FAILED: " + DatabaseUtils.dumpCursorToString(checkResult));
                return false;
            }
            else
            {
                MainActivity.log("OBSQLiteHelper Integrity check PASSED");
            }
            //
            checkResult = quickCheck();
            if (checkResult != null)
            {
                MainActivity.log("OBSQLiteHelper Quick check FAILED: " + DatabaseUtils.dumpCursorToString(checkResult));
                return false;
            }
            else
            {
                MainActivity.log("OBSQLiteHelper Quick check PASSED");
            }
            //
//            checkResult = foreignKeyCheck();
//            if (checkResult != null)
//            {
//                MainActivity.log("OBSQLiteHelper Foreign key check FAILED: " + DatabaseUtils.dumpCursorToString(checkResult));
//                return false;
//            }
            //
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Cursor runVacuum()
    {
        Cursor cursor = integrityCheck(getWritableDatabase().rawQuery("VACUUM", null));
        if (!cursor.moveToNext())
        {
            cursor.close();
            cursor = null;
        }
        return cursor;
    }

    public Cursor foreignKeyCheck()
    {
        Cursor cursor = getWritableDatabase().rawQuery("PRAGMA foreign_key_check", null);
        if (!cursor.moveToNext())
        {
            cursor.close();
            cursor = null;
        }
        return cursor;
    }


    public Cursor integrityCheck()
    {
        return integrityCheck(getWritableDatabase().rawQuery("PRAGMA integrity_check", null));
    }


    public Cursor quickCheck()
    {
        return integrityCheck(getWritableDatabase().rawQuery("PRAGMA quick_check", null));
    }


    private Cursor integrityCheck(Cursor cursor)
    {
        if (cursor.moveToNext())
        {
            String value = cursor.getString(0);
            if (value.equals("ok"))
            {
                cursor.close();
                cursor = null;
            }
        }
        return cursor;
    }


    public void emergencyRestore()
    {
        if (MainActivity.mainActivity.isDebugMode())
        {
            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.mainActivity).create();
            alertDialog.setTitle("Database Restore");
            alertDialog.setMessage("Debug Mode is ON." + System.getProperty("line.separator") + "Do you wish to restore the database to its last known good state?");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener()
            {
                public void onClick (DialogInterface dialog, int which)
                {
                    alertDialog.cancel();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener()
            {
                public void onClick (DialogInterface dialog, int which)
                {
                    alertDialog.cancel();
                    restoreDatabase();
                }
            });
            alertDialog.show();
        }
        else
        {
            restoreDatabase();
        }
    }

    private void backupDatabase() {
        try
        {
            File sd = new File(Environment.getExternalStorageDirectory(), "//onebillion//databases//");
            sd.mkdirs();
            //
            File data = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                DateFormat df = new android.text.format.DateFormat();
                String date = df.format("yyyy.MM.dd.hh.mm.ss", new java.util.Date()).toString();
                //
                String currentDBPath = "//data//" + MainActivity.mainActivity.getApplicationContext().getPackageName() + "//databases//" + DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, String.format("%s.db", date));
                //
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                //
                MainActivity.log("Database backup successful!. New database backup " + backupDB.getName());
//                Toast toast = Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Database backup successful!", Toast.LENGTH_SHORT);
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            MainActivity.log("Database backup failed!");
//            Toast toast = Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Database backup failed!", Toast.LENGTH_SHORT);
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.show();
        }
    }



    private void restoreDatabase()
    {
        try
        {
            File sd = new File (Environment.getExternalStorageDirectory(), "//onebillion//databases//");
            sd.mkdirs();
            //
            File[] backupFiles = sd.listFiles();
            Arrays.sort(backupFiles);
            Collections.reverse(Arrays.asList(backupFiles));
            //
            File data = Environment.getDataDirectory();
            //
            if (sd.canWrite())
            {
                for (File backupDB : backupFiles)
                {
                    String currentDBPath = "//data//" + MainActivity.mainActivity.getApplicationContext().getPackageName() + "//databases//" + DATABASE_NAME;
                    File currentDB = new File(data, currentDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    //
                    if (runConsistencyChecks())
                    {
                        MainActivity.log("Database restored successfully using backup " + backupDB.getName());
                        //
//                        Toast toast = Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Database restored successfully!", Toast.LENGTH_SHORT);
//                        toast.setDuration(Toast.LENGTH_SHORT);
//                        toast.show();
                        break;
                    }
                    else
                    {
                        MainActivity.log("Backup " + backupDB.getName() + " failed consistency checks, grabbing older version of database");
                    }
                }
            }
        }
        catch (Exception e)
        {
            MainActivity.log("Database restoration failed!");
//            Toast toast = Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Database restoration failed!", Toast.LENGTH_SHORT);
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.show();
        }
    }

}