package org.onebillion.onecourse.mainui.oc_playzone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.ArrayMap;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.DBObject;
import org.onebillion.onecourse.utils.DBSQL;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 03/07/2017.
 */

public class OC_PlayZoneAsset extends DBObject
{
    public static String TABLE_PLAY_ZONE_ASSETS = "playzoneassets";
    public static String ASSET_FOLDER = "pzassets";

    public static int ASSET_VIDEO = 1, ASSET_TEXT = 2, ASSET_DOODLE = 3;

    public int assetid, typeid, userid, sessionid;
    public String params, thumbnail;
    public long createTime, unitid;

    private static final String[] stringFields = {"params", "thumbnail"};
    private static final String[] intFields = {"assetid","typeid","userid","sessionid","deleted"};
    private static final String[] longFields = {"createTime","unitid"};

    public static List<String> assetsNamesForNewFile(int type)
    {
        File dir = MainActivity.mainActivity.getDir(ASSET_FOLDER, Context.MODE_PRIVATE);

        String dateString = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());

        String prefix = "thumb";
        String extension1 = "jpg";
        String extension2 = null;
        if(type == ASSET_VIDEO)
        {
            prefix = "video";
            extension2 = "mp4";
        }
        else if(type == ASSET_DOODLE)
        {
            prefix = "doodle";
            extension2 = "png";
        }
        List<String> paths = new ArrayList<>();
        String currentFileName  = String.format("%s_%s", prefix, dateString);
        paths.add(String.format("thumb_%s.%s", currentFileName, extension1));
        if(extension2 != null)
            paths.add(String.format("%s.%s", currentFileName, extension2));
        return paths;
    }

    public static String pathToAsset(String fileName)
    {
        File dir = MainActivity.mainActivity.getDir(ASSET_FOLDER, Context.MODE_PRIVATE);
        return dir.getAbsolutePath() + "/" +fileName;
    }

    public static long saveAssetInDBForUserId(DBSQL db, int userid, int type, String thumbnail, Map<String, String> params)
    {
        StringBuffer mutableString = new StringBuffer();
        for(String key : params.keySet())
        {
            if(mutableString.length() > 0)
                mutableString.append(";");
            mutableString.append(String.format("%s=%s",key, params.get(key)));
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("typeid",type);
        contentValues.put("params",mutableString.toString());
        if(thumbnail != null)
            contentValues.put("thumbnail",thumbnail);
        contentValues.put("userid",userid);
        contentValues.put("deleted",0);
        contentValues.put("createTime", (long)(System.currentTimeMillis()/1000));

        return db.doInsertOnTable(DBSQL.TABLE_PLAYZONE_ASSETS,contentValues);
    }

    public static OC_PlayZoneAsset assetFromCursor(Cursor cursor)
    {
        OC_PlayZoneAsset playZoneAsset = new OC_PlayZoneAsset();
        playZoneAsset.cursorToObject(cursor,stringFields,intFields,longFields,null);
        return playZoneAsset;
    }


    public static List<OC_PlayZoneAsset> assetsFromDBForUserId(int userid)
    {
        List<OC_PlayZoneAsset> assetList = new ArrayList<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Map<String,String> whereMap = new ArrayMap<>();
            whereMap.put("userid",String.valueOf(userid));
            whereMap.put("deleted","0");
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_PLAYZONE_ASSETS,allFieldNames(stringFields,intFields,longFields,null),whereMap, "createTime DESC");

            if(cursor.moveToFirst())
            {
                while (cursor.isAfterLast() == false)
                {
                    assetList.add(assetFromCursor(cursor));
                    cursor.moveToNext();
                }
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
        return assetList;
    }

    public boolean isLatestAsset()
    {
        int result = 0;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);

            Cursor cursor = db.prepareRawQuery(String.format("SELECT COUNT(*) as count FROM %s WHERE userid = ? AND createTime > ? AND deleted = 0", TABLE_PLAY_ZONE_ASSETS),
                    Arrays.asList(String.valueOf(userid),String.valueOf(createTime)));

            int columnIndex = cursor.getColumnIndex("count");
            if(cursor.moveToFirst()  && !cursor.isNull(columnIndex))
            {
                result = cursor.getInt(columnIndex);
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
        return result == 0;
    }

    public Map<String,String> paramsDictionary()
    {
        if(params == null)
            return null;
        Map<String,String> dictionary = new ArrayMap<>();
        for(String stringPart : params.split(";"))
        {
           String[] parts = stringPart.split("=");
            if(parts.length > 1)
                dictionary.put(parts[0], parts[1]);
        }
        return dictionary;
    }

    public void deleteAssetData()
    {
        if (thumbnail != null)
        {
            File file = new File(pathToAsset(thumbnail));
            file.delete();
        }

        Map<String, String> parDict = paramsDictionary();
        if (typeid == ASSET_VIDEO)
        {
            File file = new File(pathToAsset(parDict.get("video")));
            file.delete();
        } else if (typeid == ASSET_DOODLE)
        {
            File file = new File(pathToAsset(parDict.get("doodle")));
            file.delete();
        }

        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            Map<String, String> whereMap = new ArrayMap<>();
            whereMap.put("assetid", String.valueOf(assetid));
            ContentValues contentValues = new ContentValues();
            contentValues.put("deleted",1);
            db.doUpdateOnTable(DBSQL.TABLE_PLAYZONE_ASSETS, whereMap, contentValues);
        } catch (Exception e)
        {

        } finally
        {
            if (db != null)
                db.close();
        }
    }

    public static int[] thumbnailSize()
    {
        GLSurfaceView gls = MainActivity.mainViewController.glView();
        return new int[]{(int)(gls.getRight()*0.15),(int)(gls.getBottom()*0.15)};
    }


}
