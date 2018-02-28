package org.onebillion.onecourse.utils;

import android.graphics.PointF;

import java.util.Map;

/**
 * Created by pedroloureiro on 29/11/2017.
 */


public interface OBAnalyticsProtocol
{
    // Device
    void deviceStatus();
    void deviceGpsLocation();
    void deviceVolumeChanged(float value);
    void deviceScreenTurnedOn();
    void deviceScreenTurnedOff();
    void deviceTurnedOn();
    void deviceTurnedOff();
    void deviceHeadphonesPluggedIn();
    void deviceHeadphonesUnplugged();
    void deviceMobileSignalStrength(int value);
    void deviceStorageUse();

    // Touches
    void touchMadeInUnit(String unitID, PointF startLoc, long started, PointF endLoc, long finished);

    // Battery
    void batteryState(float batteryValue, Boolean pluggedIn, String chargerType);

    // Entered Screen
    void enteredScreen(String screen);

    // Study Zone
    void studyZoneUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses);

    // Community Mode
    void communityModeUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses);

    // Play Zone
    void playZoneVideoWatched(String videoID);
    void playZoneUnitCompleted(String activityID, long started, long finished, float score, int replayAudioPresses);
    void playZoneAssetCreated(int assetType, Map<String, String> data);

    void onStart();
    void onStop();

    void uploadData();

    /*
     * Unknown: screen capture  --> possible waste of mobile data for little to no information
     *          usage "sessions --> what does it mean?
     *          SIM status      --> what type of information to be collected?
     */
}
