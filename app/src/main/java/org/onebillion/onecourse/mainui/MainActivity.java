package org.onebillion.onecourse.mainui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import org.onebillion.onecourse.BuildConfig;
import org.onebillion.onecourse.R;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.glstuff.OBGLView;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBExpansionManager;
import org.onebillion.onecourse.utils.OBFatController;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPreferenceManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUser;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OBUtils;

import static android.R.attr.targetSdkVersion;

/**
 * MainActivity
 * Core of the Application.
 * Initialises OPENGL, Settings, wakes up receivers
 * Attaches OBMainViewController to the views.
 * @see OBMainViewController
 */

public class MainActivity extends Activity
{
    public static final int REQUEST_EXTERNAL_STORAGE = 1,
                    REQUEST_MICROPHONE = 2,
                    REQUEST_CAMERA = 3,
                    REQUEST_ALL = 4,
                    REQUEST_FIRST_SETUP_DATE_TIME = 5,
                    REQUEST_FIRST_SETUP_PERMISSIONS = 6,
                    REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES = 7,
                    REQUEST_FIRST_SETUP_PROVISION_MANAGED_PROFILE = 8,
                    REQUEST_FIRST_SETUP_WIFI_BT_SCANNING = 9;
    //
    public static String CONFIG_IMAGE_SUFFIX = "image_suffix",
            CONFIG_AUDIO_SUFFIX = "audio_suffix",
            CONFIG_VIDEO_SUFFIX = "video_suffix",
            CONFIG_AUDIO_SEARCH_PATH = "audioSearchPath",
            CONFIG_VIDEO_SEARCH_PATH = "videoSearchPath",
            CONFIG_IMAGE_SEARCH_PATH = "imageSearchPath",
            CONFIG_CONFIG_SEARCH_PATH = "configSearchPath",
            CONFIG_LEFT_BUTTON_POS = "lbuttonpos",
            CONFIG_RIGHT_BUTTON_POS = "rbuttonpos",
            CONFIG_GRAPHIC_SCALE = "graphicscale",
            CONFIG_POINTERS = "pointers",
            CONFIG_POINTERCOORDS = "pointercoords",
            CONFIG_POINTERSTARTPOINTS = "pointerstartpoints",
            CONFIG_COLOURS = "colours",
            CONFIG_SKINCOLOURS = "skincolours",
            CONFIG_SKINCOLOUR = "skincolour",
            CONFIG_MENUTABCOLOURS = "menutabcolours",
            CONFIG_LOCKED = "locked",
            CONFIG_LANGUAGE = "language",
            CONFIG_DEFAULT_LANGUAGE = "defaultlanguage",
            CONFIG_CLOTHCOLOUR = "clothcolour",
            CONFIG_VECTOR_SEARCH_PATH = "vectorsearchpath",
            CONFIG_AWARDAUDIO = "staraudio",
            CONFIG_APP_CODE = "app_code",
            CONFIG_USER = "user",
            CONFIG_EXPANSION_SEARCH_FOR_UPDATES = "expansionSearchForUpdates",
            CONFIG_EXPANSION_URL = "expansionURL",
            CONFIG_OBB_PASSWORD = "obbPassword",
            CONFIG_FAT_CONTROLLER = "fatcontrollerclass",
            CONFIG_MASTER_LIST = "masterlist",
            CONFIG_MASTER_LIST_PLAYZONE = "masterlist_playzone",
            CONFIG_MASTER_LIST_LIBRARY = "masterlist_library",
            CONFIG_DEBUG = "debug",
            CONFIG_DEFAULT_AUDIO_VOLUME = "defaultAudioVolume",
            CONFIG_MIN_AUDIO_VOLUME = "minimumAudioVolume",
            CONFIG_WIFI_CONNECT_ON_STARTUP = "connectToWifiOnStartup",
            CONFIG_WIFI_SSID = "wifiSSID",
            CONFIG_WIFI_PASSWORD = "wifiPassword",
            CONFIG_USES_BRIGHTNESS_ADJUSTMENT = "usesBrightnessAdjustment",
            CONFIG_DEFAULT_MAX_BRIGHTNESS = "defaultMaxBrightness",
            CONFIG_DEFAULT_MAX_SCREEN_TIMEOUT = "defaultScreenMaxTimeout",
            CONFIG_BRIGHTNESS_CHECK_INTERVAL = "brightnessCheckInterval",
            CONFIG_KEEP_WIFI_ON = "keepWifiOn",
            CONFIG_RESTART_AFTER_CRASH = "restartAfterCrash",
            CONFIG_PIN_APPLICATION = "pinApplication",
            CONFIG_HIDE_NAVIGATION_BAR = "hideNavigationBar",
            CONFIG_ALLOWS_TIMEOUT = "allowsTimeout",
            CONFIG_SHOW_USER_NAME = "showUserName",
            CONFIG_USE_ADMINISTRATOR_SERVICES = "enableAdministratorServices",
            CONFIG_REQUEST_DEVICE_OWNER = "requestDeviceOwner",
            CONFIG_MENU_CLASS = "menuclass",
            CONFIG_USES_SETUP_MENU = "usesSetupMenu",
            CONFIG_SETUP_CLASS = "setupClass",
            CONFIG_SETUP_FOLDER = "setupFolder",
            CONFIG_SESSION_TIMEOUT = "sessionTimeout",
            CONFIG_UNIT_TIMEOUT_COUNT = "unitAttemptsCount",
            CONFIG_SHOW_TEST_MENU = "showTestMenu",
            CONFIG_SHOW_BACK_BUTTON = "showBackButton",
            CONFIG_SHOW_DATE_TIME_SETTINGS = "showDateTimeSettings",
            CONFIG_SCREEN_MAX_TIMEOUT = "screenMaxTimeout",
            CONFIG_CHECKSUM_VERIFICATION = "checksumVerification",
            CONFIG_BACKUP_SEND_WHEN_CONNECTED = "backupSendWhenConnected",
            CONFIG_BACKUP_WIFI = "backupWifi",
            CONFIG_BACKUP_URL = "backupURL",
            CONFIG_BACKUP_WORKING_DIRECTORY = "backupWorkingDirectory",
            CONFIG_BACKUP_INTERVAL = "backupInterval",
            CONFIG_DISALLOW_HOURS = "disallowHours",
            CONFIG_BUNDLED_OBB_FILENAME = "bundledOBBFilename",
            CONFIG_EXTERNAL_ASSETS = "externalAssets",
            CONFIG_CHECK_FOR_DISABLED_LOCATION_SERVICES = "checkForDisabledLocationServices",
            CONFIG_CHECK_FOR_DISABLED_SCANNING = "checkForDisabledScanning",
            CONFIG_BUILD_NUMBER = "buildNumber",
            CONFIG_USES_BATTERY_MANAGEMENT = "usesBatteryManagement",
            CONFIG_BATTERY_LEVELS = "batteryLevels",
            CONFIG_BATTERY_LEVEL_NORMAL = "normal",
            CONFIG_BATTERY_LEVEL_LOW = "low",
            CONFIG_BATTERY_LEVEL_CRITICAL = "critical",
            CONFIG_MINIMUM_BATTERY_VALUE = "minBatteryValue",
            CONFIG_MAXIMUM_BATTERY_VALUE = "maxBatteryValue",
            CONFIG_MAX_BRIGHTNESS = "maxBrightness",
            CONFIG_MAX_SCREEN_TIMEOUT = "screenMaxTimeout",
            CONFIG_CHARGE_BATTERY_REMINDER = "chargeBatteryReminder",
            CONFIG_CHARGE_BATTERY_REMINDER_INTERVAL = "chargeBatteryReminderInterval",
            CONFIG_SHOW_BATTERY_LOCK_SCREEN = "showBatteryLockScreen",
            CONFIG_PLAYZONE_ACTIVE_HOUR = "playzoneActiveHour",
            CONFIG_SHOW_UNIT_ID = "showUnitID",
            CONFIG_TIME_SERVER_WIFI_SSID = "timeServerWifiSSID",
            CONFIG_TIME_SERVER_WIFI_PASSWORD = "timeServerWifiPassword",
            CONFIG_TIME_SERVER_URL = "timeServerURL"
    ;
    public static String TAG = "onecourse";
    //
    public static OBSystemsManager systemsManager;
    public static MainActivity mainActivity;
    public static OBMainViewController mainViewController;
    public static Typeface standardTypeFace;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static String[] PERMISSIONS_MICROPHONE = {
            Manifest.permission.RECORD_AUDIO
    };
    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    private static String[] PERMISSION_ALL = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
    };
    public Map<String, Object> config;
    public List<OBUser> users;
    public OBFatController fatController;
    public OBGLView glSurfaceView;
    public OBRenderer renderer;
    public ReentrantLock suspendLock = new ReentrantLock();
    float sfxMasterVolume = 1.0f;
    Map<String,Float> sfxVolumes = new HashMap<>();
    private int b;

    public static OBGroup armPointer ()
    {
        OBGroup arm = OBImageManager.sharedImageManager().vectorForName("arm_sleeve");
        OBControl anchor = arm.objectDict.get("anchor");
        if (anchor != null)
        {
            PointF pt = arm.convertPointFromControl(anchor.position(), anchor.parent);
            PointF rpt = OB_Maths.relativePointInRectForLocation(pt, arm.bounds());
            arm.anchorPoint = rpt;
        }
        else
            arm.anchorPoint = new PointF(0.64f, 0);


        int skincol = OBUtils.SkinColour(0);
        arm.substituteFillForAllMembers("skin.*", skincol);
        arm.setRasterScale((Float) Config().get(CONFIG_GRAPHIC_SCALE));
        //arm.borderColour = 0xff000000;
        //arm.borderWidth = 1;
        return arm;
    }


    public static Map<String, Object> Config ()
    {
        return mainActivity.config;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        MainActivity.log("MainActivity.onCreate");
        //
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        systemsManager = new OBSystemsManager(this);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable)
            {
                String restartAfterCrash = mainActivity.configStringForKey(CONFIG_RESTART_AFTER_CRASH);
                //
                paramThrowable.printStackTrace();
                //
                if (restartAfterCrash != null && restartAfterCrash.equals("true"))
                {
                    MainActivity.log("Caught unhandled exception. Restarting App");
                }
                else
                {
                    Toast.makeText(MainActivity.mainActivity, "Application has crashed due to uncaught exception", Toast.LENGTH_LONG).show();
                }
                OBSystemsManager.sharedManager.shutdownProcedures();
                System.exit(0);
            }
        });
        //
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        //
        mainActivity = this;
        doGLStuff();
        //
        setupWindowVisibilityFlags();
        //
        users = new ArrayList<OBUser>();
        setContentView(R.layout.activity_main);
        //ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        //rootView.addView(glSurfaceView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //        setContentView(glSurfaceView);
        try
        {
            new OBAudioManager();
            setUpConfig();
            checkForFirstSetupAndRun();
            //glSurfaceView.controller = mainViewController;

            ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).setCorePoolSize(20);
            log("onCreate ended");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_FIRST_SETUP_DATE_TIME)
        {
            OBPreferenceManager.setPreference("dateTimeSetupComplete", true);
//            addToPreferences("dateTimeSetupComplete", "true");
            checkForFirstSetupAndRun();
        }
        else if (requestCode == REQUEST_FIRST_SETUP_PERMISSIONS)
        {
            checkForFirstSetupAndRun();
        }
        else if (requestCode == REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                checkForFirstSetupAndRun();
            }
            else
            {
                MainActivity.log("Requesting Administrator privileges cancelled or failed");
                OBSystemsManager.sharedManager.killAllServices();
                OBSystemsManager.sharedManager.shutdownProcedures();
                finish();
            }
        }
        else if (requestCode == REQUEST_FIRST_SETUP_PROVISION_MANAGED_PROFILE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                checkForFirstSetupAndRun();
            }
            else
            {
                MainActivity.log("Requesting Provision Manager profile cancelled or failed");
                OBSystemsManager.sharedManager.killAllServices();
                OBSystemsManager.sharedManager.shutdownProcedures();
                finish();
            }
        }
        else if (requestCode == REQUEST_FIRST_SETUP_WIFI_BT_SCANNING)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                checkForFirstSetupAndRun();
            }
            else
            {
                MainActivity.log("Requesting Wifi and Bluetooth scanning to be disabled cancelled or failed");
                OBSystemsManager.sharedManager.killAllServices();
                OBSystemsManager.sharedManager.shutdownProcedures();
                finish();
            }
        }
    }


    public void setupWindowVisibilityFlags()
    {
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {

            @Override
            public void onSystemUiVisibilityChange (int visibility)
            {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
        //
        decorView.setSystemUiVisibility(flags);
        //
        // disable the lock screen when the app is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }


    public void checkForFirstSetupAndRun()
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                boolean permissionsGranted = isAllPermissionGranted();
                if (!permissionsGranted)
                {
                    return;
                }
                //
                String checkForDisabledLocationServices = mainActivity.configStringForKey(CONFIG_CHECK_FOR_DISABLED_LOCATION_SERVICES);
                if (checkForDisabledLocationServices != null && checkForDisabledLocationServices.compareTo("true") == 0)
                {
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    if (gps_enabled || network_enabled)
                    {
                        Toast.makeText(MainActivity.mainActivity, "Please disable the location services before going back.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivityForResult(intent, REQUEST_FIRST_SETUP_WIFI_BT_SCANNING);
                        return;
                    }
                }
                String checkForDisabledScanning = mainActivity.configStringForKey(CONFIG_CHECK_FOR_DISABLED_SCANNING);
                if (checkForDisabledScanning != null && checkForDisabledScanning.compareTo("true") == 0)
                {
                    boolean scanningDisabled = OBSystemsManager.sharedManager.connectionManager.isScanningDisabled();
                    //
                    if (!scanningDisabled)
                    {
                        Toast.makeText(MainActivity.mainActivity, "Please disable all Wifi and Bluetooth scanning before going back.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivityForResult(intent, REQUEST_FIRST_SETUP_WIFI_BT_SCANNING);
                        return;
                    }
                }
                //
                boolean writeSettingsPermission = OBSystemsManager.sharedManager.hasWriteSettingsPermission();
                if (!writeSettingsPermission)
                {
                    Toast.makeText(MainActivity.mainActivity, "Please allow this app to write settings before going back.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_FIRST_SETUP_PERMISSIONS);
                    return;
                }
                //
                if (OBSystemsManager.sharedManager.shouldShowDateTimeSettings())
                {
                    MainActivity.log("MainActivity.checkForFirstSetupAndRun.will show date and time settings");
                    boolean dateTimeSetupComplete = OBPreferenceManager.getBooleanPreference("dateTimeSetupComplete");
//                    boolean dateTimeSetupComplete = getPreferences("dateTimeSetupComplete") != null;
                    if (!dateTimeSetupComplete)
                    {
                        OBSystemsManager.sharedManager.setDateAndTimeDialog(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run () throws Exception
                            {
                                OBPreferenceManager.setPreference("dateTimeSetupComplete", true);
//                                addToPreferences("dateTimeSetupComplete", "true");
                                checkForFirstSetupAndRun();
                            }
                        });
                        return;
                    }
                }
                else
                {
                    MainActivity.log("MainActivity.checkForFirstSetupAndRun. will NOT show date and time settings");
                    OBPreferenceManager.setPreference("dateTimeSetupComplete", true);
//                    addToPreferences("dateTimeSetupComplete", "true");
                }
                //
                boolean administratorServices = OBSystemsManager.sharedManager.usesAdministratorServices();
                if (administratorServices)
                {
                    boolean hasAdministratorPrivileges = OBSystemsManager.sharedManager.hasAdministratorPrivileges();
                    if (!hasAdministratorPrivileges)
                    {
                        MainActivity.log("MainActivity.App does not have administrator privileges. Requesting");
                        //
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        final PackageManager packageManager = MainActivity.mainActivity.getPackageManager();
                        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
                        if (resolveInfos != null && !resolveInfos.isEmpty())
                        {
                            try
                            {
                                final ResolveInfo resolveInfo = resolveInfos.get(0);
                                intent = new Intent();
                                intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, OBSystemsManager.sharedManager.AdministratorReceiver());
                                startActivityForResult(intent, MainActivity.REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES);
                                return;
                            }
                            catch (final Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        return;
                    }
                }
                if (OBSystemsManager.sharedManager.shouldRequestDeviceOwner())
                {
                    //
                    boolean isDeviceOwner = OBSystemsManager.sharedManager.isDeviceOwner();
                    if (!isDeviceOwner)
                    {
                        OBSystemsManager.sharedManager.requestDeviceOwner();
                    }
                }
                //
                log("First Setup complete. Loading Main View Controller");
                //
                OBPreferenceManager.setPreference("firstSetupComplete", true);
//                addToPreferences("firstSetupComplete", "true");
                //
                checkForUpdatesAndLoadMainViewController();

            }
        });
    }


    public void checkForUpdatesAndLoadMainViewController()
    {
        MainActivity.log("MainActivity.checkForUpdatesAndLoadMainViewController");
        OBSystemsManager.sharedManager.checkForConnectivity(new OBUtils.RunLambdaWithSuccess()
        {
            @Override
            public void run (boolean success) throws Exception
            {
                if (!success) return;
                //
                MainActivity.log("MainActivity.checkForUpdatesAndLoadMainViewController.checking for updates in the ExpansionManager");
                OBExpansionManager.sharedManager.checkForUpdates(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        try
                        {
                            MainActivity.log("MainActivity. killing progress dialog (if it's still on screen)");
                            OBExpansionManager.sharedManager.stopProgressDialog();
                            //
                            MainActivity.log("MainActivity.startup block. runChecks");
                            OBSystemsManager.sharedManager.runChecks();
                            //
                            MainActivity.log("MainActivity.startup block. memory dump");
                            OBSystemsManager.sharedManager.printMemoryStatus("Before mainViewController");
                            //
                            MainActivity.log("MainActivity.startup block. creating mainViewControlller");
                            mainViewController = new OBMainViewController(MainActivity.mainActivity);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    // This bypasses the power button (long press), preventing shutdown
    public void onWindowFocusChanged(boolean hasFocus)
    {
//        if (!hasFocus)
//        {
//            // Close every kind of system dialog
//            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            sendBroadcast(closeDialog);
//        }
//        else
//        {
            super.onWindowFocusChanged(hasFocus);
//        }
    }



    public void onBackPressed()
    {
        // do nothing
    }


    void doGLStuff ()
    {
        glSurfaceView = new OBGLView(this);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        /*

        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
         */
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.
            glSurfaceView.setRenderer(renderer = new OBRenderer());
        }
        else
        {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        //
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public String languageCode ()
    {
        return configStringForKey(CONFIG_LANGUAGE);
//        return "en_gb";
    }


    public boolean isDebugMode()
    {
        String debugMode = configStringForKey(CONFIG_DEBUG);
        return debugMode != null && debugMode.equalsIgnoreCase("true");
    }

    OBSectionController topController ()
    {
        List l = mainViewController.viewControllers;
        return (OBSectionController) l.get(l.size() - 1);
    }

    public Object configValueForKey (String k)
    {
        return config.get(k);
    }

    public String configStringForKey (String k)
    {
        return (String) config.get(k);
    }

    public int configIntForKey (String k)
    {
        Integer i = (Integer) config.get(k);
        return i.intValue();
    }

    public float configFloatForKey (String k)
    {Float f = (Float) config.get(k);

        return f.floatValue();
    }

    public boolean configBooleanForKey (String k)
    {
        String boolString = (String) config.get(k);
        return  (boolString != null && boolString.equals("true"));
    }

    public List<String> audioSearchPath (String appDir, String genDir)
    {
        String wDir = null;
        if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"))
            wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
        String language = (String) config.get(CONFIG_LANGUAGE);
        String defaultLanguage = (String) config.get(CONFIG_DEFAULT_LANGUAGE);
        List audioSearchPath = new ArrayList(4);
        if (!language.equals(defaultLanguage))
        {
            if (appDir != null)
                audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(appDir, "local"), language));
            if (wDir != null)
                audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(wDir, "local"), language));
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(genDir, "local"), language));
        }
        if (appDir != null)
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(appDir, "local"), defaultLanguage));
        if (wDir != null)
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(wDir, "local"), defaultLanguage));
        audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(genDir, "local"), defaultLanguage));

        if (appDir != null)
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(appDir, "sfx"));
        audioSearchPath.add(OBUtils.stringByAppendingPathComponent(genDir, "sfx"));

        for (int i = audioSearchPath.size() - 1; i >= 0; i--)
            if (!OBUtils.assetsDirectoryExists((String) audioSearchPath.get(i)))
                audioSearchPath.remove(i);

        return audioSearchPath;
    }

    public List<String> imageSearchPath (String appDir, String genDir)
    {
        Boolean inBooks = (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"));
        List lowres = new ArrayList(4);
        if (appDir != null)
        {
            lowres.add(OBUtils.stringByAppendingPathComponent(appDir, "img/shared_3"));
            if (inBooks)
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                lowres.add(OBUtils.stringByAppendingPathComponent(wDir, "img/shared_3"));
            }
        }
        lowres.add(OBUtils.stringByAppendingPathComponent(genDir, "img/shared_3"));
        List highres = new ArrayList(4);
        if (appDir != null)
        {
            highres.add(OBUtils.stringByAppendingPathComponent(appDir, "img/shared_4"));
            if (inBooks)
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                highres.add(OBUtils.stringByAppendingPathComponent(wDir, "img/shared_4"));
            }
        }
        highres.add(OBUtils.stringByAppendingPathComponent(genDir, "img/shared_4"));
        //
        List imageSearchPath = new ArrayList(4);
        imageSearchPath.addAll(highres);
        imageSearchPath.addAll(lowres);
        //
        return imageSearchPath;
    }

    public List<String> videoSearchPath (String appDir, String genDir)
    {
        List<String> videoSearchPath = new ArrayList<>();
        //
        if(appDir != null)
        {
            videoSearchPath.add(appDir + "/img/movies");
        }
        //
        videoSearchPath.add(genDir+"/img/movies");
        //
        return videoSearchPath;
    }


    public List<String> configSearchPath (String appDir, String genDir)
    {
        List configSearchPath = new ArrayList(4);
        if (appDir != null)
        {
            configSearchPath.add(OBUtils.stringByAppendingPathComponent(appDir, "config"));
            if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"))
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                configSearchPath.add(OBUtils.stringByAppendingPathComponent(wDir, "config"));
            }
        }
        configSearchPath.add(OBUtils.stringByAppendingPathComponent(genDir, "config"));
        return configSearchPath;
    }

    public List<String> vectorSearchPath (String appDir, String genDir)
    {
        List configSearchPath = new ArrayList(4);
        if (appDir != null)
        {
            configSearchPath.add(OBUtils.stringByAppendingPathComponent(appDir, "img/vector"));
            if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"))
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                configSearchPath.add(OBUtils.stringByAppendingPathComponent(wDir, "img/vector"));
            }
        }
        configSearchPath.add(OBUtils.stringByAppendingPathComponent(genDir, "img/vector"));
        return configSearchPath;
    }

    public void updateConfigPaths (String newAppCode, Boolean force)
    {
        updateConfigPaths(newAppCode,force,null);
    }

    public void updateConfigPaths (String newAppCode, Boolean force, String language)
    {
        String lastAppCode = (String) config.get(CONFIG_APP_CODE);
        if (lastAppCode.equals(newAppCode) && !force)
            return;
        config.put(CONFIG_APP_CODE, newAppCode);
        String appDir = newAppCode;
        String genDir = (String) config.get("gen_code");
        String languageCode = language;
        if(languageCode == null)
            languageCode = languageCode();
        if (languageCode != null)
            config.put(CONFIG_LANGUAGE, languageCode);
        else
            config.put(CONFIG_LANGUAGE, config.get(CONFIG_DEFAULT_LANGUAGE));
        config.put(CONFIG_AUDIO_SEARCH_PATH, audioSearchPath(appDir, genDir));
        config.put(CONFIG_IMAGE_SEARCH_PATH, imageSearchPath(appDir, genDir));
        config.put(CONFIG_VIDEO_SEARCH_PATH, videoSearchPath(appDir, genDir));
        config.put(CONFIG_VECTOR_SEARCH_PATH, vectorSearchPath(appDir, genDir));
        config.put(CONFIG_CONFIG_SEARCH_PATH, configSearchPath(appDir, genDir));
        //
        OBImageManager.sharedImageManager().clearCaches();
        if (OBAudioManager.audioManager != null)
        {
            OBAudioManager.audioManager.clearCaches();
        }
    }

    void getSfxVolumes()
    {
        InputStream pis;
        try
        {
            pis = getAssets().open("config/sfxvols.plist");
            OBXMLManager xmlManager = new OBXMLManager();
            Map<String,String>smap = (Map<String, String>) xmlManager.parsePlist(pis);
            for (String k : smap.keySet())
                sfxVolumes.put(k,Float.parseFloat(smap.get(k)));
        }
        catch (Exception e)
        {

        }
    }
    public void setUpConfig () throws Exception
    {
        OBSystemsManager.sharedManager.printMemoryStatus("setupconfig");
        //
        InputStream pis;
//        pis = getAssets().open("config/settings.plist");
        pis = getAssets().open(BuildConfig.SETTINGS_FILE);
        OBXMLManager xmlManager = new OBXMLManager();
        config = (Map<String, Object>) xmlManager.parsePlist(pis);
        getSfxVolumes();
        //

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int w = metrics.widthPixels;
        int h = metrics.heightPixels;
        //float h = getResources().getDisplayMetrics().heightPixels;
      //  float w = getResources().getDisplayMetrics().widthPixels;
        updateGraphicScale(w, h);
        //
//        config.put(CONFIG_DEFAULT_LANGUAGE, configStringForKey(CONFIG_LANGUAGE)); // original
//        config.put(CONFIG_DEFAULT_LANGUAGE, configStringForKey(CONFIG_DEFAULT_LANGUAGE));
//        config.put(CONFIG_LANGUAGE, configStringForKey(CONFIG_LANGUAGE));
        //
        config.put(CONFIG_LEFT_BUTTON_POS, new PointF(0.0677f, 0.075f));
        config.put(CONFIG_RIGHT_BUTTON_POS, new PointF(0.9323f, 0.075f));
        List<String> cols = (List<String>) config.get(CONFIG_COLOURS);
        ArrayList<Integer> newcols = new ArrayList<Integer>();
        for (String s : cols)
        {
            int col = OBUtils.colorFromRGBString(s);
            newcols.add(Integer.valueOf(col));
        }
        config.put(CONFIG_COLOURS, newcols);
        cols = (List<String>) config.get(CONFIG_SKINCOLOURS);
        newcols = new ArrayList<Integer>();
        for (String s : cols)
        {
            int col = OBUtils.colorFromRGBString(s);
            newcols.add(Integer.valueOf(col));
        }
        config.put(CONFIG_SKINCOLOURS, newcols);
        Object skincolour = config.get(CONFIG_SKINCOLOUR);
        if (skincolour != null && skincolour instanceof String)
        {
            int col = OBUtils.colorFromRGBString((String) skincolour);
            config.put(CONFIG_SKINCOLOUR, Integer.valueOf(col));
        }
        updateConfigPaths((String) config.get(CONFIG_APP_CODE), true);
        String fcname = (String) config.get(CONFIG_FAT_CONTROLLER);
        if (fcname == null)
            fcname = "OBFatController";
        Class aClass = Class.forName("org.onebillion.onecourse.utils." + fcname);
        Constructor<?> cons = aClass.getConstructor();
        fatController = (OBFatController) cons.newInstance();
        //
        // Setting the default value for volume
//        String volume = configStringForKey(CONFIG_DEFAULT_AUDIO_VOLUME);
//        if (volume != null)
//        {
//            float volumePercentage = Float.parseFloat(volume) / (float) 100;
//            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercentage), 0);
//        }
    }

    public void updateGraphicScale(float newWidth, float newHeight)
    {
        log(String.format("updateGraphicScale called: %f %f",newWidth,newHeight));
        if (newHeight > newWidth)
        {
            float temp = newWidth;
            newWidth = newHeight;
            newHeight = temp;
        }
        float graphicScale = newHeight / 768;
        config.put(CONFIG_GRAPHIC_SCALE, graphicScale);
    }


    void retrieveUsers ()
    {
    }

    public float applyGraphicScale (float val)
    {
        return val * configFloatForKey(CONFIG_GRAPHIC_SCALE);
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        if (mainViewController != null)
        {
            mainViewController.onPause();
        }
        if (renderer != null)
        {
            glSurfaceView.onPause();
        }
        //
        suspendLock.lock();
        OBSystemsManager.sharedManager.onPause();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        OBSystemsManager.sharedManager.onDestroy();
    }

    @Override
    protected void onResume ()
    {
        OBSystemsManager.sharedManager.onResume();
        super.onResume();
        //
        if (mainViewController != null)
            mainViewController.onResume();
        if (renderer != null)
        {
            glSurfaceView.onResume();
        }
        //
        setupWindowVisibilityFlags();
        //
        try
        {
            suspendLock.unlock();
        }
        catch (Exception e)
        {

        }

    }


    @Override
    protected void onStop ()
    {
        systemsManager.sharedManager.onStop();
        super.onStop();
    }

    public void onAlarmReceived(Intent intent)
    {
        if (mainViewController != null)
            mainViewController.onAlarmReceived(intent);
    }

    public void onBatteryStatusReceived(float level, boolean charging)
    {
        if (mainViewController != null)
            mainViewController.onBatteryStatusReceived(level,charging);
    }


    public boolean isStoragePermissionGranted ()
    {
        Boolean writePermission = selfPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean readPermission = selfPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        //
        Boolean result = writePermission && readPermission;
        if (!result)
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        //
        return result;
    }


    public boolean isMicrophonePermissionGranted ()
    {
        Boolean micPermission = selfPermissionGranted(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        //
        if (!micPermission)
            ActivityCompat.requestPermissions(this, PERMISSIONS_MICROPHONE, REQUEST_MICROPHONE);
        //
        return micPermission;
    }

    public boolean isCameraPermissionGranted ()
    {
        Boolean micPermission = selfPermissionGranted(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        //
        if (!micPermission)
            ActivityCompat.requestPermissions(this, PERMISSIONS_CAMERA, REQUEST_CAMERA);
        //
        return micPermission;
    }


    public void onRequestPermissionsResult (int requestCode, String permissions[], int[] grantResults)
    {
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            log("received permission to access external storage. attempting to download again");
            checkForUpdatesAndLoadMainViewController();
        }
        else if (requestCode == REQUEST_ALL)
        {
            checkForFirstSetupAndRun();
        }
    }

    public boolean isAllPermissionGranted ()
    {
        return isAllPermissionGranted(true);
    }

    public boolean isAllPermissionGranted (boolean requestIfNotGranted)
    {
        Boolean allPermissionsOK = true;
        for (String permission : PERMISSION_ALL)
        {
            boolean permissionGranted = selfPermissionGranted(permission) == PackageManager.PERMISSION_GRANTED;
            MainActivity.log("MainActivity.Permission " + (permissionGranted ? "" : "NOT ") + "granted: " + permission);
            allPermissionsOK = allPermissionsOK && permissionGranted;
        }
        if (!allPermissionsOK && requestIfNotGranted)
            ActivityCompat.requestPermissions(this, PERMISSION_ALL, REQUEST_ALL);
        //
        return allPermissionsOK;
    }


    public void addToPreferences (String key, String value)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.apply();
        //
        log("Preferences SET [" + key + "] --> " + value);
    }


    public String getPreferences (String key)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        String result = sharedPreferences.getString(key, null);
        //
        log("Preferences GET [" + key + "] --> " + result);
        //
        return result;
    }

    public static void log (String message)
    {
        Log.v(TAG, message);
    }

    public static void log(String format, Object... args)
    {
        log(String.format(format, args));
    }


    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            if (!OBSystemsManager.sharedManager.settingsContentObserver.allowsLowerVolume()) return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    public int selfPermissionGranted(String permission)
    {
        // For Android < Android M, self permissions are always granted.
        int result = PackageManager.PERMISSION_GRANTED;;
        //
        if (isSDKCompatible())
        {
            return ActivityCompat.checkSelfPermission(MainActivity.mainActivity.getApplicationContext(), permission);
        }
        return result;
    }


    public static boolean isSDKCompatible()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && targetSdkVersion >= Build.VERSION_CODES.M;
    }

    public void restartApplication()
    {
        OBSystemsManager.sharedManager.unpinApplication();
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(i);
            }
        });

    }

}

