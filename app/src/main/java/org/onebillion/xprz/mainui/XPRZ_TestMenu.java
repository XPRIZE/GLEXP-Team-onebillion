package org.onebillion.xprz.mainui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.onebillion.xprz.R;
import org.onebillion.xprz.utils.DBSQL;
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
        db = new DBSQL(false);
        controller = (XPRZ_FatController)MainActivity.mainActivity.fatController;
        currentUnitId = controller.lastPlayedUnitIndex(db);
        controller.firstUnstartedIndex = currentUnitId;
        MainActivity.mainActivity.setContentView(R.layout.list_menu);
        listView = (ListView)MainActivity.mainActivity.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentUnitId = id;
                controller.firstUnstartedIndex = id;
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
                //cursorAdapter.swapCursor(null);
                controller.refreshUnitsList();
                db = new DBSQL(false);
                Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS, Arrays.asList("key", "unitid as _id"),null,"unitid ASC");
                if(cursor.moveToFirst())
                {
                    cursorAdapter.swapCursor(cursor);
                }
                cursorAdapter.notifyDataSetChanged();
            }
        });

        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS, Arrays.asList("key", "unitid as _id"),null,"unitid ASC");
        if(cursor.moveToFirst())
        {
            cursorAdapter = new OBCursorAdapter(MainActivity.mainActivity, cursor);
            listView.setAdapter(cursorAdapter);
        }

        listView.setSelection((int)currentUnitId);
        listView.setItemChecked((int)currentUnitId,true);
    }


    public void loadUnit (long unitId)
    {
        cursorAdapter.swapCursor(null);
        db.close();
        controller.startSectionByIndex(unitId);
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
            String unitKey = cursor.getString(cursor.getColumnIndex("key"));
            unitName.setText(unitKey);
        }

    }
}
