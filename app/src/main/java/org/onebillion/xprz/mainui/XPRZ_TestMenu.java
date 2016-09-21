package org.onebillion.xprz.mainui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.onebillion.xprz.R;
import org.onebillion.xprz.glstuff.OBGLView;
import org.onebillion.xprz.utils.DBSQL;
import org.onebillion.xprz.utils.MlUnit;
import org.onebillion.xprz.utils.OBBrightnessManager;
import org.onebillion.xprz.utils.OBSystemsManager;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.XPRZ_FatController;


import java.util.Arrays;


/**
 * Created by michal on 10/08/16.
 */
public class XPRZ_TestMenu extends OBSectionController
{
    private ListView listView;
    private OBCursorAdapter cursorAdapter;
    private DBSQL db;
    private long currentUnitId;
    XPRZ_FatController controller;

    public XPRZ_TestMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void initScreen()
    {
        OBBrightnessManager.sharedManager.onSuspend();
        db = new DBSQL(false);
        controller = (XPRZ_FatController)MainActivity.mainActivity.fatController;
        currentUnitId = controller.lastPlayedUnitIndexFromDB(db);
        MainActivity.mainActivity.setContentView(R.layout.list_menu);
        listView = (ListView)MainActivity.mainActivity.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentUnitId = id;
                controller.setCurrentUnitId(id);
            }
        });

        Button nextButton = (Button)MainActivity.mainActivity.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String menuClassName = (String)Config().get(MainActivity.CONFIG_MENU_CLASS);
                String appCode = (String)Config().get(MainActivity.CONFIG_APP_CODE);
                if (menuClassName != null && appCode != null)
                {
                    OBBrightnessManager.sharedManager.onContinue();
                    db.close();
                    MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, false, null);
                }

            }
        });

        Button refreshButton = (Button)MainActivity.mainActivity.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.mainActivity).create();
                alertDialog.setTitle("Delete DB");
                alertDialog.setMessage("Do you want to delete all data in DB?");
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
                        db.close();
                        DBSQL.deleteDB();

                        controller.initDB();
                        db = new DBSQL(false);
                        Cursor cursor = getCursorForList(db);
                        if(cursor.moveToFirst())
                        {
                            cursorAdapter.swapCursor(cursor);
                        }
                        cursorAdapter.notifyDataSetChanged();
                        currentUnitId = 0;
                        selectCurrentUnit();
                    }
                });
                alertDialog.show();

            }
        });

        Button newDayButton = (Button)MainActivity.mainActivity.findViewById(R.id.newdDayButton);
        newDayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.mainActivity).create();
                alertDialog.setTitle("Start new day");
                alertDialog.setMessage("Do you want to start a new day?");
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
                        controller.startNewDay();
                    }
                });
                alertDialog.show();


            }
        });


        Button shutdownButton = (Button)MainActivity.mainActivity.findViewById(R.id.shutdownButton);
        shutdownButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity.mainActivity.finish();
            }
        });


        Button causeCrashButton = (Button)MainActivity.mainActivity.findViewById(R.id.crashButton);
        causeCrashButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int timeToCrash = 1000 / (1 - 1);
            }
        });


        Button killButton = (Button)MainActivity.mainActivity.findViewById(R.id.killButton);
        killButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                OBSystemsManager.sharedManager.killAllServices();
                Toast.makeText(MainActivity.mainActivity, "Services killed", Toast.LENGTH_LONG).show();
            }
        });

        Button disableAdministratorButton = (Button) MainActivity.mainActivity.findViewById(R.id.disableAdministratorButton);
        disableAdministratorButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                OBSystemsManager.sharedManager.disableAdministratorPrivileges();
            }
        });

        Button skipButton = (Button) MainActivity.mainActivity.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                String menuClassName = (String)Config().get(MainActivity.CONFIG_MENU_CLASS);
                String appCode = (String)Config().get(MainActivity.CONFIG_APP_CODE);
                if (menuClassName != null && appCode != null)
                {
                    OBBrightnessManager.sharedManager.onContinue();
                    db.close();
                    //controller.continueFromLastUnit();
                    MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, false, null);
                }
            }
        });

        Cursor cursor = getCursorForList(db);
        if(cursor.moveToFirst())
        {
            cursorAdapter = new OBCursorAdapter(MainActivity.mainActivity, cursor);
            listView.setAdapter(cursorAdapter);
        }
        selectCurrentUnit();
    }


    public Cursor getCursorForList(DBSQL db)
    {
        return db.doSelectOnTable(DBSQL.TABLE_UNITS, Arrays.asList("key", "unitid as _id", "level", "awardStar", "startAudio"),null,"unitid ASC");
    }

    public void selectCurrentUnit()
    {
        listView.setSelection((int)currentUnitId);
        listView.setItemChecked((int)currentUnitId,true);
    }


    public void loadUnit (MlUnit unit)
    {
        cursorAdapter.swapCursor(null);
        db.close();
        controller.startSectionByUnit(unit);
    }


    @Override
    public void prepare ()
    {
        try
        {

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void start ()
    {

        try
        {
           initScreen();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public int buttonFlags ()
    {
        return 0;
    }



    private class OBCursorAdapter extends CursorAdapter
    {
        public OBCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_activated_1, parent, false);
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView unitName = (TextView) view.findViewById(android.R.id.text1);
            int level = cursor.getInt(cursor.getColumnIndex("level"));
            String unitKey = cursor.getString(cursor.getColumnIndex("key"));

            int startAudio = cursor.getInt(cursor.getColumnIndex("startAudio"));

            if(startAudio > -1)
            {
                unitName.setText(String.format("%d %d - %s", level, startAudio, unitKey));
            }
            else
            {
                unitName.setText(String.format("%d   - %s", level, unitKey));
            }

            int awardStar = cursor.getInt(cursor.getColumnIndex("awardStar"));
            unitName.setTextColor(awardStar > 0 ? Color.MAGENTA: Color.BLACK);
        }

    }
}
