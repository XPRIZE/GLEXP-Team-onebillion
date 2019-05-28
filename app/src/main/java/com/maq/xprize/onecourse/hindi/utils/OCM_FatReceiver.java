package com.maq.xprize.onecourse.hindi.utils;

import java.util.Map;

/**
 * Created by michal on 03/08/2017.
 * Receiver required to be used by menu that uses Fat Controller
 */

public interface OCM_FatReceiver
{
    void receiveCommand(Map<String,Object> dict);
}
