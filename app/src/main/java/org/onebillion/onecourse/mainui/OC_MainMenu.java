package org.onebillion.onecourse.mainui;

import android.app.Activity;
import android.content.res.AssetManager;

import org.onebillion.onecourse.utils.OBUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by alan on 05/07/16.
 */
public class OC_MainMenu extends OBSectionController
{
    public OC_MainMenu (Activity a)
    {
        super(a);
        requiresOpenGL = false;
    }

    public List<String>readingList()
    {
        AssetManager am = MainActivity.mainActivity.getAssets();
        String dir = "oc-reading";
        dir = OBUtils.stringByAppendingPathComponent(dir,"books");
        try
        {
            if (OBUtils.assetsDirectoryExists(dir))
            {
                String files[] = am.list(dir);
                return Arrays.asList(files);
            }
        }
        catch (IOException e)
        {

        }
        return Collections.emptyList();
    }

    public List<String>mathsList()
    {
        AssetManager am = MainActivity.mainActivity.getAssets();
        String dir = "";
        try
        {
            if (OBUtils.assetsDirectoryExists(dir))
            {
                String files[] = am.list(dir);
                List<String> filenames = new ArrayList<>();
                for (String f : files)
                {
                    if (f.startsWith("oc-") && !f.startsWith("oc-reading") && !f.startsWith("oc-gen"))
                        filenames.add(f);
                }
                return filenames;
            }
        }
        catch (IOException e)
        {

        }
        return Collections.emptyList();
    }

}
