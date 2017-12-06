package org.onebillion.onecourse.utils;

import android.graphics.PointF;

/**
 * Created by pedroloureiro on 29/11/2017.
 */


public interface OBAnalyticsProtocol
{
    // Device
    void deviceGpsLocation();
    void deviceVolumeChanged(float value);
    void deviceScreenTurnedOn();
    void deviceScreenTurnedOff();
    void deviceMobileSignalStrength(float value);
    void deviceStorageUse(long used, long total);

    // Touches
    void touchMade(PointF coordinates);

    // Battery
    void batteryState(float batteryValue, Boolean chargerPluggedIn);

    // Study Zone
    void studyZoneUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses);

    // Play Zone
    void playZoneVideoWatched(String videoID);
    void playZoneActivityCompleted(String activityID, long started, long finished);
    void playZoneCreationsVideoAdded();
    void playZoneCreationsDoodleAdded();
    void playZoneCreationsTextAdded();

    // Night mode
    void nightModeTriggered();

    void onStart();
    void onStop();

    /*
     * Unknown: screen capture  --> possible waste of mobile data for little to no information
     *          usage "sessions --> what does it mean?
     *          SIM status      --> what type of information to be collected?
     */
}
