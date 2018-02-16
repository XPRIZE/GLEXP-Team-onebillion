package org.onebillion.onecourse.utils;

/**
 * Created by pedroloureiro on 29/11/2017.
 */

public class OBAnalytics
{
    public static class Event
    {
        public static final String DEVICE = "device";
        public static final String DEVICE_STATUS = "device_status";
        public static final String TOUCH = "touch";
        public static final String BATTERY = "battery";
        public static final String PLAY_ZONE = "play_zone";
        public static final String APP = "app";
        public static final String UNITS = "units";
    }


    public static class Params
    {
        public static final String DEVICE_UUID = "device_uuid";
        public static final String DEVICE_GPS_LATITUDE = "gps_latitude";
        public static final String DEVICE_GPS_LONGITUDE = "gps_longitude";
        public static final String DEVICE_GPS_ALTITUDE = "gps_altitude";
        public static final String DEVICE_GPS_BEARING = "gps_bearing";
        public static final String DEVICE_VOLUME = "volume";
        public static final String DEVICE_STATE = "device_state";
        public static final String DEVICE_STATE_ON = "on";
        public static final String DEVICE_STATE_OFF = "off";
        public static final String DEVICE_SCREEN_STATE = "screen_state";
        public static final String DEVICE_SCREEN_STATE_ON = "on";
        public static final String DEVICE_SCREEN_STATE_OFF = "off";
        public static final String DEVICE_SIGNAL_STRENGTH = "signal_strength";
        public static final String DEVICE_USED_STORAGE = "used_storage";
        public static final String DEVICE_TOTAL_STORAGE = "total_storage";
        public static final String DEVICE_HEADPHONES_STATE = "headphones_state";
        public static final String DEVICE_HEADPHONES_STATE_PLUGGED = "plugged";
        public static final String DEVICE_HEADPHONES_STATE_UNPLUGGED = "unplugged";
        //
        public static final String BATTERY_LEVEL = "battery_level";
        public static final String BATTERY_CHARGER_STATE = "charger_state";
        public static final String BATTERY_CHARGER_STATE_PLUGGED_AC = "plugged_ac";
        public static final String BATTERY_CHARGER_STATE_PLUGGED_USB = "plugged_usb";
        public static final String BATTERY_CHARGER_STATE_UNPLUGGED = "unplugged";
        //
        public static final String UNIT_MODE = "unit_mode";
        public static final String UNIT_MODE_STUDY_ZONE = "study_zone";
        public static final String UNIT_MODE_COMMUNITY_MODE = "community_mode";
        public static final String UNIT_MODE_PLAY_ZONE = "play_zone";
        public static final String UNIT_ID = "unit_id";
        public static final String UNIT_SCORE = "unit_score";
        public static final String UNIT_START_TIME = "unit_start_time";
        public static final String UNIT_END_TIME = "unit_end_time";
        public static final String UNIT_REPLAY_AUDIO_COUNT = "unit_replay_audio_count";
        //
        public static final String PLAY_ZONE_VIDEO_ID = "video_id";
        //
        public static final String APP_MODE_CHANGE = "mode_change";
        public static final String APP_STUDY_ZONE = "study_zone";
        public static final String APP_COMMUNITY_MODE = "community_mode";
        public static final String APP_PLAY_ZONE = "play_zone";
        public static final String APP_NIGHT_MODE = "night_mode";
        //
        public static final String CREATION_TYPE = "play_zone_creation_type";
        public static final String CREATION_TYPE_VIDEO = "video";
        public static final String CREATION_TYPE_DOODLE = "doodle";
        public static final String CREATION_TYPE_TEXT = "text";

        public static final String TOUCH_UNIT_ID = "unit_id";
        public static final String TOUCH_START_LOCATION = "start_location";
        public static final String TOUCH_START_TIME = "start_time";
        public static final String TOUCH_END_LOCATION = "end_location";
        public static final String TOUCH_END_TIME = "end_time";
    }


    protected OBAnalytics ()
    {
    }
}
