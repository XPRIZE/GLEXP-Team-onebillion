package org.onebillion.xprz.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
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
        try
        {
            MainActivity.mainActivity.log("OB SQL Lite running maintenance");
            getWritableDatabase().execSQL("VACUUM");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

//    private void exportDB() {
//        try
//        {
//            File sd = Environment.getExternalStorageDirectory();
//            File data = Environment.getDataDirectory();
//
//            if (sd.canWrite())
//            {
//                String currentDBPath = "//data//" + MainActivity.mainActivity.getApplicationContext().getPackageName() + "//databases//" + "<db name>";
//                String backupDBPath = "<destination>";
//                File currentDB = new File(data, currentDBPath);
//                File backupDB = new File(sd, backupDBPath);
//
//                FileChannel src = new FileInputStream(currentDB).getChannel();
//                FileChannel dst = new FileOutputStream(backupDB).getChannel();
//                dst.transferFrom(src, 0, src.size());
//                src.close();
//                dst.close();
//                Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Backup Successful!", Toast.LENGTH_SHORT).show();
//            }
//        }
//        catch (Exception e)
//        {
//            Toast.makeText(MainActivity.mainActivity.getApplicationContext(), "Backup Failed!", Toast.LENGTH_SHORT).show();
//        }
//    }

}