package org.onebillion.onecourse.utils;

/**
 * Created by pedroloureiro on 29/11/2017.
 */

public class OBAnalytics
{
    public static class Event
    {
        public static final String DEVICE = "device";
        public static final String TOUCH = "touch";
        public static final String BATTERY = "battery";
        public static final String STUDY_ZONE = "study_zone";
        public static final String PLAY_ZONE = "play_zone";
        public static final String NIGHT_MODE = "night_mode";
    }


    public static class Params
    {
        public static final String GPS_COORDINATES = "gps_coordinates";
        public static final String VOLUME = "volume";
        public static final String SCREEN_STATE = "screen_state";
        public static final String TIMESTAMP = "timestamp";
        public static final String SIGNAL_STRENGTH = "signal_strength";
        public static final String USED_STORAGE = "used_storage";
        public static final String TOTAL_STORAGE = "total_storage";
        public static final String TOUCH_COORDINATES = "touch_coordinates";
        public static final String BATTERY_LEVEL = "battery_level";
        public static final String CHARGER_STATE = "charger_state";
        public static final String STUDY_ZONE_UNIT_ID = "study_zone_unit_id";
        public static final String STUDY_ZONE_UNIT_SCORE = "study_zone_unit_score";
        public static final String STUDY_ZONE_UNIT_ELAPSED = "study_zone_elapsed";
        public static final String STUDY_ZONE_REPLAY_AUDIO = "study_zone_replay_audio";
        public static final String VIDEO_ID = "video_id";
        public static final String PLAY_ZONE_ACTIVITY_ID = "play_zone_activity_id";
        public static final String PLAY_ZONE_ELAPSED = "play_zone_elapsed";
        public static final String PLAY_ZONE_CREATION_TYPE = "play_zone_creation_type";
    }


    protected OBAnalytics ()
    {
    }
}
