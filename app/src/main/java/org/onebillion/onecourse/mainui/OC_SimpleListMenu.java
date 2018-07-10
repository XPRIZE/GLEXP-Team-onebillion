package org.onebillion.onecourse.mainui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 11/12/17.
 */

public class OC_SimpleListMenu extends OBSectionController
{
    private ListView listView;
    List<MlUnit> masterList;
    List<MlUnit> filteredList;
    ArrayAdapter<MlUnit> arrayAdapter;
    private MasterlistFilter masterlistFilter = new MasterlistFilter();
    List<String> checkedList;
    String currentBuild;


    public OC_SimpleListMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void initScreen()
    {
        checkedList = new ArrayList<>();
        MainActivity.mainActivity.setContentView(R.layout.simple_list_menu);
        listView = (ListView)MainActivity.mainActivity.findViewById(R.id.OB_simplelistview);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(android.R.color.darker_gray);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                hideKeyboard();

                MlUnit m = (MlUnit)parent.getItemAtPosition(position);
                setStatus(STATUS_BUSY);
                String configName = m.config;
                //
                String unitUUID = "????";
                String keys = m.key;
                if (keys != null)
                {
                    String array[] = keys.split("\\.");
                    if (array.length > 1)
                    {
                        unitUUID = array[0];
                    }
                }
                //
                OBSystemsManager.sharedManager.setCurrentUnit(unitUUID);
                //
                if (configName == null)
                {
                    String appDir = OBConfigManager.sharedManager.getCurrentActivityFolder();
                    String[] comps = appDir.split("/");
                    configName = comps[0];
                }
                else
                {
                    OBConfigManager.sharedManager.updateConfigPaths(configName, false,m.lang);
                }

                try
                {
                    checkedList.add(m.key);
                    arrayAdapter.notifyDataSetChanged();
                    if (MainActivity.mainViewController.pushViewControllerWithNameConfig(m.target, configName, true, true, m.params))

                    {

                    } else
                    {
                        Toast.makeText(MainActivity.mainActivity, m.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(MainActivity.mainActivity, m.target + " error opening!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                setStatus(STATUS_IDLE);

            }
        });

        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();

                MlUnit m = (MlUnit) parent.getItemAtPosition(position);
                setStatus(STATUS_BUSY);

                askForUnitReport(m);
                setStatus(STATUS_IDLE);
                return true;
            }
        });


        listView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                hideKeyboard();
                return false;
            }
        });

        filteredList = new ArrayList<>(masterList);
        //
        TextView buildNumber = (TextView) MainActivity.mainActivity.findViewById(R.id.buildNumber);
        if (buildNumber != null)
        {
            String buildNumberValue = OBConfigManager.sharedManager.getBuildNumber();
            if (buildNumberValue == null)
            {
                buildNumberValue = "Missing BuildNo";
            }
            buildNumber.setText(buildNumberValue);
            currentBuild = buildNumberValue;

        }
        //
        LinearLayout topBar = (LinearLayout) MainActivity.mainActivity.findViewById(R.id.topBar);
        topBar.setBackgroundColor(Color.rgb(255,165,0));
        //
        arrayAdapter = new ArrayAdapter<MlUnit>(MainActivity.mainActivity,android.R.layout.simple_list_item_1,filteredList)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                MlUnit ml = getItem(position);
                String s =  String.format("%s/%s/%s/%s",ml.key,ml.target,ml.params,ml.config);
                TextView view = (TextView)super.getView(position,convertView,parent);
                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();
                // Set the height of the Item View
                params.height = 175;
                view.setText(s);
                if(checkedList.contains(ml.key))
                    view.setTextColor(Color.RED);
                else
                    view.setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public Filter getFilter() {
                return masterlistFilter;
            }
        };
        listView.setAdapter(arrayAdapter);


        final EditText filterText = (EditText) MainActivity.mainActivity.findViewById(R.id.filterText);
        if(filterText != null)
        {
            filterText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {

                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    String text = filterText.getText().toString().toLowerCase();
                    masterlistFilter.filter(text);
                }
            });

            final EditText positionText = (EditText) MainActivity.mainActivity.findViewById(R.id.positionText);
            if(positionText != null) {
                positionText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = positionText.getText().toString().toLowerCase();
                        int position = -1;
                        for (int i=0;i<arrayAdapter.getCount();i++){
                            MlUnit ml = arrayAdapter.getItem(i);
                            if (ml.key.toLowerCase().contains(text.toString().toLowerCase()))
                            {
                                position = i;
                                break;
                            }
                        }
                        if(position > -1)
                        {
                            listView.setSelectionFromTop(position, 0);
                        }
                    }
                });
            }
        }
    }

    List<MlUnit> loadUnitXML(String xmlPath)
    {
        List<MlUnit> arr = new ArrayList<>();
        if(xmlPath != null)
        {
            OBXMLManager xmlManager = new OBXMLManager();
            OBXMLNode xmlNode = null;
            try
            {
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                xmlNode = xl.get(0);
                for(OBXMLNode xmlLevel :  xmlNode.childrenOfType("level"))
                {
                    List<OBXMLNode> xmlunits = xmlLevel.childrenOfType("unit");
                    for(OBXMLNode n : xmlunits)
                    {
                        MlUnit m = MlUnit.mlUnitFromXMLNode(n);
                        m.key = n.attributeStringValue("id");
                        arr.add(m);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return arr;
    }

    void loadUnits()
    {
        String mlname = OBConfigManager.sharedManager.getMasterlist();
        masterList = loadUnitXML(String.format("masterlists/%s/units.xml", mlname));
    }

    public void prepare()
    {
        setStatus(STATUS_IDLE);
        loadUnits();
        initScreen();
    }

    public void askForUnitReport(final MlUnit unit)
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.mainActivity);
        alert.setTitle("Unit Report");
        alert.setMessage("Do you want to report unit "+unit.key+" ?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                prepareBugReportForUnit(unit);
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = alert.show();
        OBUtils.runOnOtherThreadDelayed(10, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(dialog != null && dialog.isShowing())
                {
                    dialog.cancel();
                }
            }
        });
    }

    public void prepareBugReportForUnit(MlUnit unit)
    {
        try {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://github.com/onebillionchildren/XPRIZE-ob-android/issues/new?");
            stringBuilder.append("title="+URLEncoder.encode("Build "+currentBuild+" - "+unit.key+" - TYPE ISSUE HERE", "UTF-8"));
            stringBuilder.append("&body=%23%23%23%23%23%20Component%0D%0Akey%20%3D%20"+URLEncoder.encode(unit.key, "UTF-8")+"%0D%0Atarget%20%3D%20"+URLEncoder.encode(unit.target, "UTF-8")
                    +"%0D%0Aconfig%20%3D%20"+URLEncoder.encode(unit.config, "UTF-8")+"%0D%0Aparams%20%3D%20"+URLEncoder.encode(unit.params, "UTF-8")
                    +"%0D%0A%23%23%23%23%23%20Description%0D%0ADescribe%20the%20issue%20clearly%2C%20with%20steps%20to%20reproduce.%0D%0AAttach%20a%20screenshot%20if%20applicable.%0D%0A%23%23%23%23%23%20Severity%0D%0A-%20%5B%20%5D%20Low%0D%0A-%20%5B%20%5D%20Medium%0D%0A-%20%5B%20%5D%20High%0D%0A");
            stringBuilder.append("&assignee=KaMpErTuGa");
            stringBuilder.append("&milestone=onecourse+-+upload+3");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuilder.toString()));
            MainActivity.mainActivity.startActivity(browserIntent);
        }
        catch (Exception e)
        {

        }
    }

    public void hideKeyboard()
    {
        View view = MainActivity.mainActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class MasterlistFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {


            final ArrayList<MlUnit> resultList = new ArrayList<>(masterList.size());


            for (int i = 0; i < masterList.size(); i++) {
                if (masterList.get(i).key.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                        masterList.get(i).params.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                        masterList.get(i).target.toLowerCase().contains(constraint.toString().toLowerCase())
                        ) {
                    resultList.add(masterList.get(i));
                }
            }
            FilterResults results = new FilterResults();
            results.values = resultList;
            results.count = resultList.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<MlUnit>) results.values;

            arrayAdapter.clear();
            for(int i = 0, l = filteredList.size(); i < l; i++)
                arrayAdapter.add(filteredList.get(i));
            listView.clearChoices();
            arrayAdapter.notifyDataSetChanged();
        }

    }

    }
