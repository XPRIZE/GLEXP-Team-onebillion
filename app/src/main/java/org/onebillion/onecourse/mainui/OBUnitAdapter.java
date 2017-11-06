package org.onebillion.onecourse.mainui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBXMLNode;

/**
 * Created by pedroloureiro on 05/08/16.
 */
public class OBUnitAdapter extends ArrayAdapter<OBXMLNode>
{

    OBSectionController sectionController;

    public OBUnitAdapter(Context context, OBXMLNode[] units, OBSectionController sc)
    {
        super(context, 0, units);
        this.sectionController = sc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        OBXMLNode xmlNode = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simplerow, parent, false);
        }
        // Lookup view for data population
        TextView tvRow = (TextView) convertView.findViewById(R.id.rowTextView);
        // Populate the data into the template view using the data object
        tvRow.setText(xmlNode.attributes.get("id"));
        tvRow.setTag(xmlNode);
        tvRow.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    OBXMLNode xmlNode = (OBXMLNode) v.getTag();
                    String target = xmlNode.attributeStringValue("target");
                    String parameters = xmlNode.attributeStringValue("params");
                    String config = xmlNode.attributeStringValue("config");
                    OBConfigManager.sharedManager.updateConfigPaths(config, false);
                    String className = config.replace("-", "_") + "." + target;
                    sectionController.MainViewController().pushViewControllerWithName(className, true, false, parameters);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}
