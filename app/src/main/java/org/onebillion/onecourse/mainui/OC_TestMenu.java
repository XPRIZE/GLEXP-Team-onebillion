package org.onebillion.onecourse.mainui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.DBSQL;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBBrightnessManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OCM_FatController;
import org.onebillion.onecourse.utils.OC_FatController;


import java.util.Arrays;


/**
 * Created by michal on 10/08/16.
 */
public class OC_TestMenu extends OBSectionController
{
    private ListView listView;
    private OBCursorAdapter cursorAdapter;
    private DBSQL db;
    private int currentUnitIndex;
    private String originalAppCode;
    OC_FatController controller;

    public OC_TestMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void initScreen()
    {
        if (originalAppCode == null)
        {
            originalAppCode = (String) Config().get(MainActivity.CONFIG_APP_CODE);
        }
        OBBrightnessManager.sharedManager.onSuspend();
        db = new DBSQL(false);
        controller = (OC_FatController) MainActivity.mainActivity.fatController;
        //
        currentUnitIndex = controller.lastPlayedUnitIndexFromDB(db);
        MainActivity.mainActivity.setContentView(R.layout.list_menu);
        listView = (ListView)MainActivity.mainActivity.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentUnitIndex = (int)id;
                controller.setCurrentUnitIndex(currentUnitIndex);
            }
        });

        Button nextButton = (Button)MainActivity.mainActivity.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OBUtils.runOnMainThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        String menuClassName = (String)Config().get(MainActivity.CONFIG_MENU_CLASS);
                        MainActivity.mainActivity.updateConfigPaths(originalAppCode, true, null);
                        //
                        if (menuClassName != null)
                        {
                            OBBrightnessManager.sharedManager.onContinue();
                            db.close();
                            MainViewController().pushViewControllerWithNameConfig(menuClassName, originalAppCode, false, false, null);
                        }
                    }
                });
            }
        });

        Button loopButton = (Button)MainActivity.mainActivity.findViewById(R.id.loopButton);
        if (loopButton != null)
        {
            loopButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick (View v)
                {
                    OBSystemsManager.sharedManager.onSuspend();
                    String menuClassName = "OC_LoopMenu";
                    String appCode = "oc-gen";
                    if (menuClassName != null && appCode != null)
                    {
                        OBBrightnessManager.sharedManager.onContinue();
                        db.close();
                        MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, false, null);
                    }

                }
            });
        }

        Button uploadBackupButton = (Button)MainActivity.mainActivity.findViewById(R.id.uploadBackup);
        uploadBackupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        MainActivity.log("Device UUID: " + OBSystemsManager.sharedManager.device_getUUID());
                        OBSystemsManager.sharedManager.connectToWifiAndSynchronizeTimeAndData();
                    }
                });
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
                        currentUnitIndex = 0;
                        selectCurrentUnit();
                    }
                });
                alertDialog.show();

            }
        });

        Button newDayButton = (Button)MainActivity.mainActivity.findViewById(R.id.newdDayButton);
        if (newDayButton != null)
        {
            newDayButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick (View v)
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
                            controller.prepareNewSession();
                        }
                    });
                    alertDialog.show();
                }
            });
        }


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
        if (causeCrashButton != null)
        {
            causeCrashButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick (View v)
                {
                    int timeToCrash = 1000 / (1 - 1);
                }
            });
        }


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
        if (skipButton != null)
        {
            skipButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick (View v)
                {
                    String menuClassName = (String) Config().get(MainActivity.CONFIG_MENU_CLASS);
                    String appCode = (String) Config().get(MainActivity.CONFIG_APP_CODE);
                    if (menuClassName != null && appCode != null)
                    {
                        OBBrightnessManager.sharedManager.onContinue();
                        db.close();
                        controller.continueFromLastUnit();
                        MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, false, null);
                    }
                }
            });
        }
        //
        Button playzoneButton = (Button) MainActivity.mainActivity.findViewById(R.id.playzone);
        if (playzoneButton != null)
        {
            playzoneButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick (View v)
                {
                    OBUtils.runOnMainThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            OBBrightnessManager.sharedManager.onContinue();
                            //
                            String lang = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_DEFAULT_LANGUAGE);
                            MainActivity.mainActivity.updateConfigPaths("oc-playzone", false, lang);
                            MainViewController().pushViewControllerWithNameConfig("OC_PlayZoneMenu","oc-playzone",false,true, null);
                        }
                    });
                }
            });
        }
        //
        TextView buildNumber = (TextView) MainActivity.mainActivity.findViewById(R.id.buildNumber);
        if (buildNumber != null)
        {
            String buildNumberValue = (String) MainActivity.Config().get(MainActivity.CONFIG_BUILD_NUMBER);
            if (buildNumberValue == null)
            {
                buildNumberValue = "Missing BuildNo";
            }
            buildNumber.setText(buildNumberValue);
        }
        //
        Cursor cursor = getCursorForList(db);
        if(cursor != null && cursor.moveToFirst())
        {
            cursorAdapter = new OBCursorAdapter(MainActivity.mainActivity, cursor);
            listView.setAdapter(cursorAdapter);
        }
        selectCurrentUnit();
    }


    public Cursor getCursorForList(DBSQL db)
    {
        Cursor currentUnitCursor;
        try
        {
            currentUnitCursor = db.doSelectOnTable(DBSQL.TABLE_UNITS, Arrays.asList("key", "unitIndex as _id", "level", "awardStar", "startAudio"),null,"unitid ASC");
        }
        catch (Exception e)
        {
            currentUnitCursor = null;
        }
        return currentUnitCursor;
    }

    public void selectCurrentUnit()
    {
        listView.setSelection((int)currentUnitIndex);
        listView.setItemChecked((int)currentUnitIndex,true);
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
                unitName.setText(String.format("%d 0 - %s", level, unitKey));
            }

            int awardStar = cursor.getInt(cursor.getColumnIndex("awardStar"));
            unitName.setTextColor(awardStar > 0 ? Color.MAGENTA: Color.BLACK);
        }

    }
}
