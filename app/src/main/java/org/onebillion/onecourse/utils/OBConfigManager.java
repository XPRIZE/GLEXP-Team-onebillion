package org.onebillion.onecourse.utils;

import android.renderscript.ScriptGroup;

import org.onebillion.onecourse.BuildConfig;
import org.onebillion.onecourse.R;
import org.onebillion.onecourse.mainui.MainActivity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by pedroloureiro on 02/11/2017.
 */

public class OBConfigManager
{
    public static String MULTIPLE_APP_DIR_SEPARATOR = ",";
    //
    private static String EXTENSIONS_IMAGE = "extensions_image";
    private static String EXTENSIONS_AUDIO = "extensions_audio";
    private static String EXTENSIONS_VIDEO = "extensions_video";
    private static String EXTENSIONS_VECTOR = "extensions_vector";
    //
    private static String SKIN_COLOURS = "skin_colours";
    //
    private static String SFX_STAR = "sfx_star";
    //
    private static String LOCALISATION_SKIN_COLOUR = "localisation_skin_colour_index";
    private static String LOCALISATION_CLOTH_COLOUR = "localisation_cloth_colour";
    private static String LOCALISATION_LANGUAGE_ID = "localisation_language_id";
    private static String LOCALISATION_FALLBACK_LANGUAGE_ID = "localisation_fallback_language_id";
    //
    private static String SETUP_MENU_ENABLED = "setup_menu_enabled";
    private static String SETUP_MENU_FOLDER = "setup_menu_folder";
    private static String SETUP_MENU_CLASS = "setup_menu_class";
    //
    private static String BRIGHTNESS_MANAGER_ENABLED = "brightness_manager_enabled";
    private static String BRIGHTNESS_CHECK_INTERVAL_SECONDS = "brightness_check_interval_seconds";
    private static String BRIGHTNESS_TURN_OFF_VALUE = "brightness_turn_off_value";
    private static String BRIGHTNESS_MAX_SCREEN_TIMEOUT_SECONDS = "brightness_max_screen_timeout_seconds";
    //
    private static String NTP_ENABLED = "ntp_enabled";
    private static String NTP_WIFI_SSID = "ntp_wifi_ssid";
    private static String NTP_WIFI_PASSWORD = "ntp_wifi_password";
    private static String NTP_URL = "ntp_url";
    //
    private static String BACKUP_ENABLED_WHEN_CHARGING = "backup_enabled_when_charging";
    private static String BACKUP_WIFI_SSID = "backup_wifi_ssid";
    private static String BACKUP_WIFI_PASSWORD = "backup_wifi_password";
    private static String BACKUP_URL = "backup_url";
    private static String BACKUP_INTERVAL_MINUTES = "backup_interval_minutes";
    private static String BACKUP_WORKING_DIRECTORY = "backup_working_directory";
    //
    private static String STARTUP_ENABLE_ADMINISTRATOR_SERVICES = "startup_enable_administrator_services";
    private static String STARTUP_REQUEST_DEVICE_OWNER = "startup_request_device_owner";
    private static String STARTUP_PIN_APPLICATION = "startup_pin_application";
    //
    private static String AUDIO_MIN_VOLUME = "audio_min_volume";
    private static String AUDIO_DEFAULT_VOLUME = "audio_default_volume";
    //
    private static String RECOVERY_RESTART_AFTER_CRASH = "recovery_restart_after_crash";
    //
    private static String BATTERY_MANAGER_ENABLED = "battery_manager_enabled";
    private static String BATTERY_LEVELS = "battery_levels";
    public static String BATTERY_LEVEL_NORMAL = "battery_level_normal";
    public static String BATTERY_LEVEL_LOW = "battery_level_low";
    public static String BATTERY_LEVEL_CRITICAL = "battery_level_critical";
    private static String BATTERY_MAX_VALUE = "battery_max_value";
    private static String BATTERY_MIN_VALUE = "battery_min_value";
    private static String BATTERY_MAX_BRIGHTNESS = "battery_max_brightness";
    private static String BATTERY_MAX_SCREEN_TIMEOUT_SECONDS = "battery_max_screen_timeout_seconds";
    private static String BATTERY_CHARGE_REMINDER_ENABLED = "battery_charge_reminder_enabled";
    private static String BATTERY_CHARGE_REMINDER_INTERNAL_MINUTES = "battery_charge_reminder_internal_minutes";
    private static String BATTERY_LOCK_SCREEN_ENABLED = "battery_lock_screen_enabled";
    //
    private static String FAT_CONTROLLER_SESSION_TIMEOUT_ENABLED = "fat_controller_session_timeout_enabled";
    private static String FAT_CONTROLLER_SESSION_TIMEOUT_SECONDS = "fat_controller_session_timeout_seconds";
    private static String FAT_CONTROLLER_MAX_UNIT_ATTEMPTS = "fat_controller_max_unit_attempts";
    private static String FAT_CONTROLLER_NIGHT_MODE_HOURS = "fat_controller_night_mode_hours";
    private static String FAT_CONTROLLER_PLAYZONE_ACTIVE_HOUR = "fat_controller_playzone_active_hour";
    private static String FAT_CONTROLLER_SHOW_USER_NAME = "fat_controller_show_user_name";
    private static String FAT_CONTROLLER_LOCK_TIMEOUT = "fat_controller_playzone_lock_timeout";
    //
    private static String ASSETS_EXTERNAL_PATH = "assets_external_path";
    private static String ASSETS_LOOK_FOR_ZIPPED_FILES = "assets_look_for_zipped_files";
    private static String ASSETS_PRIORITY_FOLDERS = "assets_priority_folders";
    private static String ASSETS_READY_TO_BE_USED = "assets_ready_to_be_used";
    //
    private static String DEBUG_ENABLED = "debug_enabled";
    private static String DEBUG_SHOW_TEST_MENU = "debug_show_test_menu";
    private static String DEBUG_SHOW_BACK_ARROW = "debug_show_back_arrow";
    private static String DEBUG_SHOW_UNIT_ID = "debug_show_unit_id";
    private static String DEBUG_BUILD_NUMBER = "debug_build_number";
    private static String DEBUG_JUMP_TO_SETUP_PASSWORD = "debug_jump_to_setup_password";
    private static String DEBUG_CHANGE_DATE_PASSWORD = "debug_change_date_password";
    private static String DEBUG_COMMUNITY_MODE_OVERRIDE = "debug_community_mode_override_enabled";
    private static String DEBUG_ACTIVATE_COMMUNITY_MODE_OVERRIDE_PASSWORD = "debug_activate_community_mode_override_password";
    private static String DEBUG_REVERT_COMMUNITY_MODE_PASSWORD = "debug_revert_community_mode_password";
    //
    private static String APP_GEN_FOLDER = "app_gen_folder";
    private static String APP_MAIN_FOLDER = "app_main_folder";
    private static String APP_MENU_CLASS = "app_menu_class";
    private static String APP_FAT_CONTROLLER_CLASS = "app_fat_controller_class";
    private static String APP_MASTERLIST = "app_masterlist";
    private static String APP_MASTERLIST_PLAYZONE = "app_masterlist_playzone";
    private static String APP_MASTERLIST_LIBRARY = "app_masterlist_library";
    //
    public static String APP_LANGUAGES = "app_languages";
    public static String APP_LANGUAGE_NAMES = "app_language_names";
    //
    private static String ANALYTICS_ENABLED = "analytics_enabled";
    private static String ANALYTICS_CLASS = "analytics_class";
    private static String ANALYTICS_DEVICE_STATUS_REFRESH_INTERVAL_MINUTES = "analytics_device_status_refresh_interval_minutes";
    //
    private static String LOCATION_ENABLED = "location_enabled";
    //
    private static String JUDGES_MENU_SHOW_POPUP = "showMenuPopup";
    //
    private Map<String, Object> internalConfig;
    //
    private float sfxMasterVolume = 1.0f;
    private Map<String, Float> internalSfxVolumes = new HashMap<>();
    //
    private List<String> internalAudioSearchPaths;
    private List<String> internalImageSearchPaths;
    private List<String> internalConfigSearchPaths;
    private List<String> internalVideoSearchPaths;
    private List<String> internalVectorSearchPaths;
    private List<File> internalAssetsSearchPaths;
    //
    private String currentLanguage;
    private String currentActivityFolder;
    //
    private float internalGraphicScale;
    //
    public static OBConfigManager sharedManager;

    public OBConfigManager ()
    {
        sharedManager = this;
        //
        try
        {
            loadConfigPLIST();
        }
        catch (Exception e)
        {
            MainActivity.log("Exception caught while trying to load the config PLIST");
            e.printStackTrace();
        }
    }


    void loadSFXVolumesPLIST () throws Exception
    {
        InputStream pis = MainActivity.mainActivity.getAssets().open("sfxvols.plist");
        OBXMLManager xmlManager = new OBXMLManager();
        Map<String, String> smap = (Map<String, String>) xmlManager.parsePlist(pis);
        for (String k : smap.keySet())
        {
            internalSfxVolumes.put(k, Float.parseFloat(smap.get(k)));
        }
    }

    public void loadConfigPLIST () throws Exception
    {
        String configPath = BuildConfig.SETTINGS_FILE;
        InputStream fileInputStream = MainActivity.mainActivity.getAssets().open(configPath);
        OBXMLManager xmlManager = new OBXMLManager();
        //
        internalConfig = (Map<String, Object>) xmlManager.parsePlist(fileInputStream);
        loadSFXVolumesPLIST();
        //
        // Convert string colours into integer colours
        List<String> cols = (List<String>) internalConfig.get(SKIN_COLOURS);
        List<Integer> newSkinColours = new ArrayList<Integer>();
        for (String s : cols)
        {
            int col = OBUtils.colorFromRGBString(s);
            newSkinColours.add(Integer.valueOf(col));
        }
        internalConfig.put(SKIN_COLOURS, newSkinColours);
        //
        // Convert cloth colour string into colour
        String clothColourString = (String) internalConfig.get(LOCALISATION_CLOTH_COLOUR);
        if (clothColourString != null && clothColourString instanceof String)
        {
            int clothColour = OBUtils.colorFromRGBString(clothColourString);
            internalConfig.put(LOCALISATION_CLOTH_COLOUR, Integer.valueOf(clothColour));
        }
        //
        // Store current Activity and Language
        currentActivityFolder = getMainFolder();
        currentLanguage = getLanguageID();
    }

    public Boolean isJumpToSetupPasswordCorrect(String password)
    {
        String correctValue = getStringValue(DEBUG_JUMP_TO_SETUP_PASSWORD);
        return correctValue != null && correctValue.equals(password);
    }

    public Boolean isChangeDatePasswordCorrect(String password)
    {
        String correctValue = getStringValue(DEBUG_CHANGE_DATE_PASSWORD);
        return correctValue != null && correctValue.equals(password);
    }

    public Boolean isCommunityModeOverrideEnabled()
    {
        return getBooleanValue(DEBUG_COMMUNITY_MODE_OVERRIDE);
    }

    public Boolean isActivateCommunityModeOverridePasswordCorrect(String password)
    {
        String correctValue = getStringValue(DEBUG_ACTIVATE_COMMUNITY_MODE_OVERRIDE_PASSWORD);
        return correctValue != null && correctValue.equals(password);
    }

    public Boolean isRevertCommunityModePasswordCorrect(String password)
    {
        String correctValue = getStringValue(DEBUG_REVERT_COMMUNITY_MODE_PASSWORD);
        return correctValue != null && correctValue.equals(password);
    }

    public String getAwardSoundEffect()
    {
        return getStringValue(SFX_STAR);
    }

    public List<String> getAudioSearchPaths()
    {
        return internalAudioSearchPaths;
    }

    private List<String> generateSearchPathsForFolders(String appDir, String genDir)
    {
        List result = new ArrayList();
        //
        StringTokenizer mainFolderTokens = new StringTokenizer(appDir, MULTIPLE_APP_DIR_SEPARATOR);
        while (mainFolderTokens.hasMoreTokens())
        {
            String token = mainFolderTokens.nextToken();
            //
            if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(token)).equals("books"))
            {
                String extraFolder = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(token));
                //
                result.add(extraFolder);
            }
            //
            result.add(token);
        }
        //
        result.add(genDir);
        //
        return result;
    }

    public List<String> getAudioSearchPaths (String appDir, String genDir, String language)
    {
        List result = new ArrayList();
        List<String> searchPaths = generateSearchPathsForFolders(appDir, genDir);
        //
        List<String> languages = new ArrayList();
        if (language != null) languages.add(language);
        if (!languages.contains(getLanguageID())) languages.add(getLanguageID());
        if (!languages.contains(getFallbackLanguageID())) languages.add(getFallbackLanguageID());
        //
        for (String availableLanguage : languages)
        {
            for (String dir : searchPaths)
            {
                if (dir == null) continue;
                //
                String newPath = OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(dir, "local"), availableLanguage);
                if (OBUtils.assetsDirectoryExists(newPath))
                {
                    result.add(newPath);
                }
            }
        }
        //
        for (String dir : searchPaths)
        {
            if (dir == null) continue;
            //
            String newPath = OBUtils.stringByAppendingPathComponent(dir, "sfx");
            if (OBUtils.assetsDirectoryExists(newPath))
            {
                result.add(newPath);
            }
        }
        //
        return result;
    }

    public List<String> getImageSearchPaths()
    {
        return internalImageSearchPaths;
    }

    public List<String> getImageSearchPaths (String appDir, String genDir)
    {
        List result = new ArrayList();
        List<String> searchPaths = generateSearchPathsForFolders(appDir, genDir);
        //
        for (String resolution : Arrays.asList("img/shared_4", "img/shared_3"))
        {
            for (String dir : searchPaths)
            {
                if (dir == null) continue;
                //
                String newPath = OBUtils.stringByAppendingPathComponent(dir, resolution);
                if (OBUtils.assetsDirectoryExists(newPath))
                {
                    result.add(newPath);
                }
            }
        }
        return result;
    }

    public List<String> getConfigSearchPaths()
    {
        return internalConfigSearchPaths;
    }

    public List<String> getConfigSearchPaths (String appDir, String genDir)
    {
        List result = new ArrayList();
        List<String> searchPaths = generateSearchPathsForFolders(appDir, genDir);
        //
        for (String dir : searchPaths)
        {
            if (dir == null) continue;
            //
            String newPath = OBUtils.stringByAppendingPathComponent(dir, "config");
            if (OBUtils.assetsDirectoryExists(newPath))
            {
                result.add(newPath);
            }
        }
        //
        return result;
    }

    public List<String> getVectorSearchPaths()
    {
        return internalVectorSearchPaths;
    }

    public List<String> getVectorSearchPaths (String appDir, String genDir)
    {
        List result = new ArrayList();
        List<String> searchPaths = generateSearchPathsForFolders(appDir, genDir);
        //
        for (String dir : searchPaths)
        {
            if (dir == null) continue;
            //
            String newPath = OBUtils.stringByAppendingPathComponent(dir, "img/vector");
            if (OBUtils.assetsDirectoryExists(newPath))
            {
                result.add(newPath);
            }
        }
        //
        return result;
    }

    public List<String> getVideoSearchPaths()
    {
        return internalVideoSearchPaths;
    }

    public List<String> getVideoSearchPaths (String appDir, String genDir)
    {
        List result = new ArrayList();
        List<String> searchPaths = generateSearchPathsForFolders(appDir, genDir);
        //
        for (String dir : searchPaths)
        {
            if (dir == null) continue;
            //
            String newPath = OBUtils.stringByAppendingPathComponent(dir, "/img/movies");
            if (OBUtils.assetsDirectoryExists(newPath))
            {
                result.add(newPath);
            }
        }
        //
        return result;
    }

    public void updateGraphicScale(float newWidth, float newHeight)
    {
        MainActivity.log(String.format("updateGraphicScale called: %f %f",newWidth,newHeight));
        if (newHeight > newWidth)
        {
            float temp = newWidth;
            newWidth = newHeight;
            newHeight = temp;
        }
        internalGraphicScale = newHeight / 768;
    }

    public float applyGraphicScale (float val)
    {
        return val * internalGraphicScale;
    }

    public float getGraphicScale()
    {
        return internalGraphicScale;
    }

    public String getCurrentActivityFolder()
    {
        String value = currentActivityFolder;
        if (value == null)
        {
            value = getMainFolder();
        }
        return value;
    }

    public String getCurrentLanguage()
    {
        String value = currentLanguage;
        if (value == null)
        {
            value = getLanguageID();
        }
        if (value == null)
        {
            value = getFallbackLanguageID();
        }
        return value;
    }

    public void updateConfigPaths (String newActivityFolder, Boolean force)
    {
        updateConfigPaths(newActivityFolder, force, null);
    }

    public void updateConfigPaths (String newActivityFolder, Boolean force, String newLanguage)
    {
        if (currentActivityFolder != null && currentActivityFolder.equals(newActivityFolder) && currentLanguage != null && currentLanguage.equals(newLanguage) && !force)
        {
            MainActivity.log("OBConfigManager.updateConfigPath no need to update as new values are the current ones");
            return;
        }
        //
        currentActivityFolder = newActivityFolder;
        currentLanguage = newLanguage;
        //
        String genFolder = getGenFolder();
        //
        internalAudioSearchPaths = getAudioSearchPaths(currentActivityFolder, genFolder, currentLanguage);
        internalImageSearchPaths = getImageSearchPaths(currentActivityFolder, genFolder);
        internalConfigSearchPaths = getConfigSearchPaths(currentActivityFolder, genFolder);
        internalVectorSearchPaths = getVectorSearchPaths(currentActivityFolder, genFolder);
        internalVideoSearchPaths = getVideoSearchPaths(currentActivityFolder, genFolder);
        //
        OBImageManager.sharedImageManager().clearCaches();
        //
        if (OBAudioManager.audioManager != null)
        {
            OBAudioManager.audioManager.clearCaches();
        }
    }


    public void setValue(String key, Object value)
    {
        MainActivity.log(String.format("OBConfigManager:setValue [%s] for key [%s]", value, key));
        internalConfig.put(key, value);
    }

    public Object getValue (String key)
    {
        return internalConfig.get(key);
    }

    public Boolean getBooleanValue (String key)
    {
        String result = getStringValue(key);
        if (result != null)
        {
            return result.equalsIgnoreCase("true");
        }
        return false;
    }


    public List<String> getArrayValue (String key)
    {
        Object result = (Object) internalConfig.get(key);
        if (result != null)
        {
            if (result instanceof List)
            {
                return (List<String>) result;
            }
        }
        return null;
    }


    public String getStringValue (String key)
    {
        String result = (String) internalConfig.get(key);
        if (result != null)
        {
            if (result instanceof String)
            {
                return result;
            }
            else
            {
                return String.valueOf(result);
            }
        }
        return null;
    }


    public int getIntValue (String key)
    {
        Object result = internalConfig.get(key);
        //
        if (result == null) return -1;
        //
        if (result instanceof Integer) return ((Integer) result).intValue();
        //
        return Integer.parseInt(String.valueOf(result));
    }


    public float getFloatValue (String key)
    {
        Object result = internalConfig.get(key);
        //
        if (result == null) return -1;
        //
        if (result instanceof Double) return ((Double) result).floatValue();
        if (result instanceof Float) return ((Float) result).floatValue();
        //
        return Float.parseFloat(String.valueOf(result));
    }


    protected List<String> getExtensions (String key)
    {
        Object value = internalConfig.get(key);
        if (value instanceof String)
        {
            List<String> result = new ArrayList<>();
            result.add((String) value);
            //
            return result;
        }
        else if (value instanceof List)
        {
            return (List) value;
        }
        else
        {
            MainActivity.log("OBConfigManager.getExtensions.unknown value from internal config --> " + value.toString());
        }
        return null;
    }


    public List<String> getAudioExtensions ()
    {
        return getExtensions(EXTENSIONS_AUDIO);
    }

    public List<String> getImageExtensions ()
    {
        return getExtensions(EXTENSIONS_IMAGE);
    }

    public List<String> getVideoExtensions ()
    {
        return getExtensions(EXTENSIONS_VIDEO);
    }

    public List<String> getVectorExtensions()
    {
        return getExtensions(EXTENSIONS_VECTOR);
    }


    public int getPresenterColourIndex ()
    {
        int value = getIntValue(LOCALISATION_SKIN_COLOUR);
        if (value == -1)
        {
            MainActivity.log("ERROR --> [" + LOCALISATION_SKIN_COLOUR + "] not set in the CONFIG");
            return 0;
        }
        return value;
    }

    public int getSkinColour (int offset)
    {
        List<Integer> skinColours = (List<Integer>) internalConfig.get(SKIN_COLOURS);
        Integer index = (getPresenterColourIndex() + offset) % skinColours.size();
        return skinColours.get(index);
    }

    public int getClothColour ()
    {
        Integer result = (Integer) internalConfig.get(LOCALISATION_CLOTH_COLOUR);
        return result.intValue();
    }

    public String getLanguageID ()
    {
        String result = (String) internalConfig.get(LOCALISATION_LANGUAGE_ID);
        return result;
    }

    public String getFallbackLanguageID ()
    {
        String result = (String) internalConfig.get(LOCALISATION_FALLBACK_LANGUAGE_ID);
        return result;
    }


    public Boolean isSetupMenuEnabled ()
    {
        return getBooleanValue(SETUP_MENU_ENABLED);
    }

    public String getSetupMenuFolder ()
    {
        return getStringValue(SETUP_MENU_FOLDER);
    }

    public String getSetupMenuClassName ()
    {
        return getStringValue(SETUP_MENU_CLASS);
    }


    public Boolean isBrightnessManagerEnabled ()
    {
        return getBooleanValue(BRIGHTNESS_MANAGER_ENABLED);
    }

    public int getBrightnessCheckIntervalInSeconds ()
    {
        return getIntValue(BRIGHTNESS_CHECK_INTERVAL_SECONDS);
    }

    public float getBrightnessTurnOffThresholdValue ()
    {
        return getFloatValue(BRIGHTNESS_TURN_OFF_VALUE);
    }

    public int getBrightnessMaxScreenTimeInSeconds ()
    {
        return getIntValue(BRIGHTNESS_MAX_SCREEN_TIMEOUT_SECONDS);
    }


    public Boolean isTimeServerEnabled ()
    {
        return getBooleanValue(NTP_ENABLED);
    }

    public String getTimeServerWifiSSID ()
    {
        return getStringValue(NTP_WIFI_SSID);
    }

    public String getTimeServerWifiPassword ()
    {
        return getStringValue(NTP_WIFI_PASSWORD);
    }

    public String getTimeServerURL ()
    {
        return getStringValue(NTP_URL);
    }


    public Boolean isBackupWhenChargingEnabled ()
    {
        return getBooleanValue(BACKUP_ENABLED_WHEN_CHARGING);
    }

    public String getBackupWifiSSID ()
    {
        return getStringValue(BACKUP_WIFI_SSID);
    }

    public String getBackupWifiPassword ()
    {
        return getStringValue(BACKUP_WIFI_PASSWORD);
    }

    public String getBackupURL ()
    {
        return getStringValue(BACKUP_URL);
    }

    public String getBackupWorkingDirectory ()
    {
        return getStringValue(BACKUP_WORKING_DIRECTORY);
    }

    public int getBackupIntervalInMinutes ()
    {
        return getIntValue(BACKUP_INTERVAL_MINUTES);
    }


    public Boolean isEnablingAdministratorServicesRequired ()
    {
        return getBooleanValue(STARTUP_ENABLE_ADMINISTRATOR_SERVICES);
    }

    public Boolean isRequestingDeviceOwnerRequired ()
    {
        return getBooleanValue(STARTUP_REQUEST_DEVICE_OWNER);
    }

    public Boolean shouldPinApplication ()
    {
        return getBooleanValue(STARTUP_PIN_APPLICATION);
    }


    public int getMinimumAudioVolumePercentage ()
    {
        return getIntValue(AUDIO_MIN_VOLUME);
    }


    public int getDefaultAudioVolumePercentage()
    {
        return getIntValue(AUDIO_DEFAULT_VOLUME);
    }


    public Boolean shouldAppRestartAfterCrash ()
    {
        return getBooleanValue(RECOVERY_RESTART_AFTER_CRASH);
    }


    public Boolean isBatteryManagerEnabled ()
    {
        return getBooleanValue(BATTERY_MANAGER_ENABLED);
    }

    public Set<String> getBatteryLevelKeys ()
    {
        Object value = internalConfig.get(BATTERY_LEVELS);
        //
        if (value != null && value instanceof HashMap) return ((HashMap<String, Object>) value).keySet();
        //
        return null;
    }

    protected HashMap<String, Object> getBatteryLevels ()
    {
        Object value = internalConfig.get(BATTERY_LEVELS);
        //
        if (value != null && value instanceof HashMap) return (HashMap<String, Object>) value;
        //
        return null;
    }

    public HashMap<String, Object> getBatteryDataForLevel (String levelKey)
    {
        HashMap<String, Object> data = getBatteryLevels();
        Object value = data.get(levelKey);
        //
        if (value != null && value instanceof HashMap) return (HashMap<String, Object>) value;
        //
        return null;
    }

    protected Object getBatteryValueForLevel (String levelKey, String propertyKey)
    {
        HashMap<String, Object> data = getBatteryDataForLevel(levelKey);
        //
        if (data != null) return data.get(propertyKey);
        //
        return null;
    }

    public int getBatteryMaxValueForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_MAX_VALUE);
        if (value != null) return Integer.parseInt(String.valueOf(value));
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_MAX_VALUE, levelKey));
        return -1;
    }

    public int getBatteryMinValueForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_MIN_VALUE);
        if (value != null) return Integer.parseInt(String.valueOf(value));
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_MIN_VALUE, levelKey));
        return -1;
    }

    public float getBatteryMaxBrightnessForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_MAX_BRIGHTNESS);
        if (value != null) return Float.parseFloat(String.valueOf(value));
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_MAX_BRIGHTNESS, levelKey));
        return -1;
    }

    public int getBatteryMaxScreenTimeoutInSecondsForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_MAX_SCREEN_TIMEOUT_SECONDS);
        if (value != null) return Integer.parseInt(String.valueOf(value));
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_MAX_SCREEN_TIMEOUT_SECONDS, levelKey));
        return -1;
    }

    public int getBatteryChargeReminderIntervalInMinutesForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_CHARGE_REMINDER_INTERNAL_MINUTES);
        if (value != null) return Integer.parseInt(String.valueOf(value));
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_CHARGE_REMINDER_INTERNAL_MINUTES, levelKey));
        return -1;
    }

    public Boolean isBatteryChargeReminderEnabledForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_CHARGE_REMINDER_ENABLED);
        if (value != null) return String.valueOf(value).equalsIgnoreCase("true");
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_CHARGE_REMINDER_ENABLED, levelKey));
        return false;
    }

    public Boolean isBatteryLockScreenEnabledForLevel (String levelKey)
    {
        Object value = getBatteryValueForLevel(levelKey, BATTERY_LOCK_SCREEN_ENABLED);
        if (value != null) return String.valueOf(value).equalsIgnoreCase("true");
        //
        MainActivity.log(String.format("ERROR --> missing value in config for key [%s] in [%s]", BATTERY_LOCK_SCREEN_ENABLED, levelKey));
        return false;
    }


    public Boolean isFatControllerSessionTimeoutEnabled ()
    {
        return getBooleanValue(FAT_CONTROLLER_SESSION_TIMEOUT_ENABLED);
    }

    public int getFatControllerSessionTimeoutInSeconds ()
    {
        return getIntValue(FAT_CONTROLLER_SESSION_TIMEOUT_SECONDS);
    }

    public int getFatControllerMaxUnitAttempts ()
    {
        return getIntValue(FAT_CONTROLLER_MAX_UNIT_ATTEMPTS);
    }

    public String getFatControllerNightModeHours ()
    {
        return getStringValue(FAT_CONTROLLER_NIGHT_MODE_HOURS);
    }

    public int getFatControllerPlayzoneActiveHour ()
    {
        return getIntValue(FAT_CONTROLLER_PLAYZONE_ACTIVE_HOUR);
    }

    public Boolean shouldFatControllerShowUserName()
    {
        return getBooleanValue(FAT_CONTROLLER_SHOW_USER_NAME);
    }

    public int getFatControllerPlayzoneLockTimeout()
    {
        return getIntValue(FAT_CONTROLLER_LOCK_TIMEOUT);
    }



    public String getAssetsExternalPath ()
    {
        return getStringValue(ASSETS_EXTERNAL_PATH);
    }

    public List<File> getExternalAssetsSearchPaths()
    {
        if (internalAssetsSearchPaths == null)
        {
            internalAssetsSearchPaths = OBSystemsManager.sharedManager.getExternalAssetsFolders();
        }
        return internalAssetsSearchPaths;
    }

    public Boolean shouldLookForZippedAsssets()
    {
        return getBooleanValue(ASSETS_LOOK_FOR_ZIPPED_FILES);
    }

    public List<String> getZippedAssetsPriorityFolders()
    {
        String value = getStringValue(ASSETS_PRIORITY_FOLDERS);
        return Arrays.asList(value.split(","));
    }

    public Boolean areAssetsReadyToBeUsed()
    {
        return getBooleanValue(ASSETS_READY_TO_BE_USED);
    }

    public void setAssetsReadyToBeUsed(Boolean value)
    {
        internalConfig.put(ASSETS_READY_TO_BE_USED, (value ? "true" : "false"));
    }


    public Boolean isDebugEnabled ()
    {
        return getBooleanValue(DEBUG_ENABLED);
    }

    public Boolean shouldShowTestMenu ()
    {
        return getBooleanValue(DEBUG_SHOW_TEST_MENU);
    }

    public Boolean shouldShowBackArrow ()
    {
        return getBooleanValue(DEBUG_SHOW_BACK_ARROW);
    }

    public Boolean shouldShowUnitID ()
    {
        return getBooleanValue(DEBUG_SHOW_UNIT_ID);
    }

    public String getBuildNumber ()
    {
        return getStringValue(DEBUG_BUILD_NUMBER);
    }


    public String getGenFolder ()
    {
        return getStringValue(APP_GEN_FOLDER);
    }

    public String getMainFolder ()
    {
        return getStringValue(APP_MAIN_FOLDER);
    }

    public String getMenuClassName ()
    {
        return getStringValue(APP_MENU_CLASS);
    }

    public String getFatControllerClassName ()
    {
        String value = getStringValue(APP_FAT_CONTROLLER_CLASS);
        if (value == null || value.length() == 0)
        {
            return "OBFatController";
        }
        return value;
    }

    public String getMasterlist ()
    {
        return getStringValue(APP_MASTERLIST);
    }

    public String getMasterlistForPlayzone ()
    {
        return getStringValue(APP_MASTERLIST_PLAYZONE);
    }

    public String getMasterlistForLibrary ()
    {
        return getStringValue(APP_MASTERLIST_LIBRARY);
    }



    public Boolean isAnalyticsEnabled ()
    {
        return getBooleanValue(ANALYTICS_ENABLED);
    }

    public String getAnalyticsClassName ()
    {
        return getStringValue(ANALYTICS_CLASS);
    }

    public int getAnalyticsDeviceStatusRefreshIntervalMinutes()
    {
        return getIntValue(ANALYTICS_DEVICE_STATUS_REFRESH_INTERVAL_MINUTES);
    }

    public Map<String,Float> getSfxVolumes()
    {
        if(internalSfxVolumes != null)
            return internalSfxVolumes;
        else
            return new HashMap<>();
    }


    public Boolean getShowJudgesPopupMenu()
    {
        return getBooleanValue(JUDGES_MENU_SHOW_POPUP);
    }




    public Boolean isLocationEnabled()
    {
        return getBooleanValue(LOCATION_ENABLED);
    }

}
