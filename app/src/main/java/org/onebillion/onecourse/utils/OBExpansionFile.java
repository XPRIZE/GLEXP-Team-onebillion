package org.onebillion.onecourse.utils;

import java.io.File;

/**
 * Created by pedroloureiro on 06/07/16.
 */
public class OBExpansionFile
{

    public String id, bundle, destination;
    public long version;
    public File folder;

    public OBExpansionFile(String id, String bundle, String destination, long version, File folder)
    {
        super();
        this.id = id;
        this.bundle = bundle;
        this.destination = destination;
        this.version = version;
        this.folder = folder;
    }
}
