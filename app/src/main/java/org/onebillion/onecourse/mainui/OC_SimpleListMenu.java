package org.onebillion.onecourse.mainui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

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

    public OC_SimpleListMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void initScreen()
    {
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
                }
                setStatus(STATUS_IDLE);

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

        }

        //
        arrayAdapter = new ArrayAdapter<MlUnit>(MainActivity.mainActivity,android.R.layout.simple_list_item_1,filteredList)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                MlUnit ml = getItem(position);
                String s =  String.format("%s/%s/%s",ml.key,ml.target,ml.params);
                TextView view = (TextView)super.getView(position,convertView,parent);
                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();
                // Set the height of the Item View
                params.height = 175;
                view.setText(s);
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
                        masterList.get(i).params.toLowerCase().contains(constraint.toString().toLowerCase())) {
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
