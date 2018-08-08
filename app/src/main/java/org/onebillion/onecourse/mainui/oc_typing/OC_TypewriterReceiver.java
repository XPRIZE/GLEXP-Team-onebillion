package org.onebillion.onecourse.mainui.oc_typing;

import org.onebillion.onecourse.controls.OBGroup;

/**
 * Created by michal on 12/06/2018.
 */

public interface OC_TypewriterReceiver
{
    void touchDownKey(OBGroup key, boolean sound);
    void touchUpKey(OBGroup key);
}
