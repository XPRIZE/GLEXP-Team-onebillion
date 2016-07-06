package org.onebillion.xprz.mainui;

import java.io.File;

/**
 * Created by pedroloureiro on 06/07/16.
 */
public class OBExpansionFile
{

    public String id, type;
    public int version;
    public File folder;

    public OBExpansionFile(String id, String type, int version, File folder)
    {
        super();
        this.id = id;
        this.type = type;
        this.version = version;
        this.folder = folder;
    }
}
