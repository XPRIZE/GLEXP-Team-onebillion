package org.onebillion.xprz.mainui;

import java.io.File;

/**
 * Created by pedroloureiro on 06/07/16.
 */
public class OBExpansionFile
{

    public String id, bundle, destination;
    public int version;
    public File folder;

    public OBExpansionFile(String id, String bundle, String destination, int version, File folder)
    {
        super();
        this.id = id;
        this.bundle = bundle;
        this.destination = destination;
        this.version = version;
        this.folder = folder;
    }
}
