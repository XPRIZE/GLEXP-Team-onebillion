package org.onebillion.xprz.utils;

import java.util.Map;

/**
 * Created by michal on 10/08/16.
 */
public interface XPRZ_FatReceiver
{
    void receiveCommand(Map<String,Object> dict);
}
