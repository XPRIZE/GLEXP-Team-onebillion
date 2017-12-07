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
    void deviceTurnedOn();
    void deviceTurnedOff();
    void deviceHeadphonesPluggedIn();
    void deviceHeadphonesUnplugged();
    void deviceMobileSignalStrength(float value);
    void deviceStorageUse(long used, long total);

    // Touches
    void touchMadeInUnit(String unitID, PointF startLoc, long started, PointF endLoc, long finished);

    // Battery
    void batteryState(float batteryValue, Boolean pluggedIn, String chargerType);

    // Study Zone
    void studyZoneStartedNewDay();
    void studyZoneUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses);

    // Community Mode
    void communityModeEntered();
    void communityModeUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses);

    // Play Zone
    void playZoneEntered();
    void playZoneVideoWatched(String videoID);
    void playZoneUnitCompleted(String activityID, long started, long finished, float score, int replayAudioPresses);
    void playZoneCreationsVideoAdded();
    void playZoneCreationsDoodleAdded();
    void playZoneCreationsTextAdded();

    // Night mode
    void nightModeEntered();

    void onStart();
    void onStop();

    /*
     * Unknown: screen capture  --> possible waste of mobile data for little to no information
     *          usage "sessions --> what does it mean?
     *          SIM status      --> what type of information to be collected?
     */
}
