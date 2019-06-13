package com.maq.xprize.onecourse.hindi.mainui;

import android.Manifest;
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
import android.graphics.PointF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.maq.xprize.onecourse.hindi.R;
import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.glstuff.OBGLView;
import com.maq.xprize.onecourse.hindi.glstuff.OBRenderer;
import com.maq.xprize.onecourse.hindi.utils.OBAnalyticsManager;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBConfigManager;
import com.maq.xprize.onecourse.hindi.utils.OBFatController;
import com.maq.xprize.onecourse.hindi.utils.OBImageManager;
import com.maq.xprize.onecourse.hindi.utils.OBLocationManager;
import com.maq.xprize.onecourse.hindi.utils.OBPreferenceManager;
import com.maq.xprize.onecourse.hindi.utils.OBSystemsManager;
import com.maq.xprize.onecourse.hindi.utils.OBUser;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import static android.R.attr.targetSdkVersion;
import static com.maq.xprize.onecourse.hindi.mainui.DownloadExpansionFile.xAPKS;

/**
 * MainActivity
 * Core of the Application.
 * Initialises OPENGL, Settings, wakes up receivers
 * Attaches OBMainViewController to the views.
 *
 * @see OBMainViewController
 */

public class MainActivity extends Activity {
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
    public static String TAG = "onecourse";
    //
    public static OBConfigManager configManager;
    public static OBSystemsManager systemsManager;
    public static OBAnalyticsManager analyticsManager;
    public static OBLocationManager locationManager;
    public static MainActivity mainActivity;
    public static OBMainViewController mainViewController;
    public static Typeface standardTypeFace, writingTypeFace;

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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    public List<OBUser> users;
    public OBFatController fatController;
    public OBGLView glSurfaceView;
    public OBRenderer renderer;
    public ReentrantLock suspendLock = new ReentrantLock();
    float sfxMasterVolume = 1.0f;
    Map<String, Float> sfxVolumes = new HashMap<>();
    private int b;

    public static OBGroup armPointer() {
        OBGroup arm = OBImageManager.sharedImageManager().vectorForName("arm_sleeve");
        OBControl anchor = arm.objectDict.get("anchor");
        if (anchor != null) {
            PointF pt = arm.convertPointFromControl(anchor.position(), anchor.parent);
            PointF rpt = OB_Maths.relativePointInRectForLocation(pt, arm.bounds());
            arm.anchorPoint = rpt;
        } else
            arm.anchorPoint = new PointF(0.64f, 0);
        //
        int skincol = OBConfigManager.sharedManager.getSkinColour(0);
        arm.substituteFillForAllMembers("skin.*", skincol);
        arm.setRasterScale(OBConfigManager.sharedManager.getGraphicScale());
        //arm.borderColour = 0xff000000;
        //arm.borderWidth = 1;
        return arm;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getSharedPreferences("ExpansionFile", MODE_PRIVATE);
        int defaultFileVersion = 0;

        String flagFilePath = "/storage/emulated/0/Android/data/com.maq.xprize.onecourse.hindi/files/.success.txt";
        File flagFile = new File(flagFilePath);
        if (!flagFile.exists()) {
            // Set main and patch file version to 0, if the extractions takes place for the first time
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.mainFileVersion), 0);
            editor.putInt(getString(R.string.patchFileVersion), 0);
            editor.commit();
            Intent intent = new Intent(MainActivity.this, SplashScreenActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Retrieve the stored values of main and patch file version
            int mainFileVersion = sharedPref.getInt(getString(R.string.mainFileVersion), defaultFileVersion);
            int patchFileVersion = sharedPref.getInt(getString(R.string.patchFileVersion), defaultFileVersion);
            boolean isExtractionRequired = isExpansionExtractionRequired(mainFileVersion, patchFileVersion);
            // If main or patch file is updated, the extraction process needs to be performed again
            if (isExtractionRequired) {
                Intent intent = new Intent(MainActivity.this, SplashScreenActivity.class);
                startActivity(intent);
                finish();
            }
        }

        MainActivity.log("MainActivity.onCreate");
        //
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        systemsManager = new OBSystemsManager(this);
        //
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                MainActivity.log("Details of unhandled exception:");
                //
                paramThrowable.printStackTrace();
                //
                if (OBConfigManager.sharedManager.shouldAppRestartAfterCrash()) {
                    MainActivity.log("Caught unhandled exception. Restarting App");
                    // TODO: restart?
                } else {
                    Toast.makeText(MainActivity.mainActivity, "Application has crashed due to uncaught exception", Toast.LENGTH_LONG).show();
                }
                OBSystemsManager.sharedManager.shutdownProcedures();
                System.exit(0);
            }
        });
        //
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //
        // Hide Status Bar
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //
        mainActivity = this;
        //
        configManager = new OBConfigManager();
        //
        analyticsManager = new OBAnalyticsManager(this);
        locationManager = new OBLocationManager(this);
        //
        // this flag disables screenshots
        // Commented code to enable capturing of screenshots
        /*if (!OBConfigManager.sharedManager.isDebugEnabled()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }*/
        //
        /*if (OBConfigManager.sharedManager.shouldPinApplication()) {
            OBSystemsManager.disableStatusBar();
        }*/
        //
        OBSystemsManager.printBuildVersion();
        //
        doGLStuff();
        //
        setupWindowVisibilityFlags();
        //
        users = new ArrayList<OBUser>();
        setContentView(R.layout.activity_main);
        //ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        //rootView.addView(glSurfaceView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //        setContentView(glSurfaceView);
        try {
            new OBAudioManager(mainActivity);
            //
            setUpConfig();
            checkForFirstSetupAndRun();
            //
            ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).setCorePoolSize(20);
            log("onCreate ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isExpansionExtractionRequired(int mainFileVersion, int patchFileVersion) {
        for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
            // If main or patch file is updated set isExtractionRequired to true
            if (xf.mIsMain && xf.mFileVersion != mainFileVersion || !xf.mIsMain && xf.mFileVersion != patchFileVersion) {
                return true;
            }
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FIRST_SETUP_DATE_TIME) {
            OBPreferenceManager.setPreference("dateTimeSetupComplete", true);
            checkForFirstSetupAndRun();
        } else if (requestCode == REQUEST_FIRST_SETUP_PERMISSIONS) {
            checkForFirstSetupAndRun();
        } else if (requestCode == REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES) {
            if (resultCode == Activity.RESULT_OK) {
                checkForFirstSetupAndRun();
            } else {
                MainActivity.log("Requesting Administrator privileges cancelled or failed");
                OBSystemsManager.sharedManager.killAllServices();
                OBSystemsManager.sharedManager.shutdownProcedures();
                finish();
            }
        } else if (requestCode == REQUEST_FIRST_SETUP_PROVISION_MANAGED_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                checkForFirstSetupAndRun();
            } else {
                MainActivity.log("Requesting Provision Manager profile cancelled or failed");
                OBSystemsManager.sharedManager.killAllServices();
                OBSystemsManager.sharedManager.shutdownProcedures();
                finish();
            }
        } else if (requestCode == REQUEST_FIRST_SETUP_WIFI_BT_SCANNING) {
            if (resultCode == Activity.RESULT_OK) {
                checkForFirstSetupAndRun();
            } else {
                MainActivity.log("Requesting Wifi and Bluetooth scanning to be disabled cancelled or failed");
                OBSystemsManager.sharedManager.killAllServices();
                OBSystemsManager.sharedManager.shutdownProcedures();
                finish();
            }
        }
    }


    public void setupWindowVisibilityFlags() {
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
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
    }


    public void checkForFirstSetupAndRun() {
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                boolean permissionsGranted = isAllPermissionGranted();
                if (!permissionsGranted) {
                    return;
                }
                //
                boolean writeSettingsPermission = OBSystemsManager.sharedManager.hasWriteSettingsPermission();
                if (OBConfigManager.sharedManager.isBrightnessManagerEnabled() && !writeSettingsPermission) {
                    Toast.makeText(MainActivity.mainActivity, "Please allow this app to write settings before going back.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_FIRST_SETUP_PERMISSIONS);
                    return;
                }
                //
                MainActivity.log("MainActivity.checkForFirstSetupAndRun. will NOT show date and time settings");
                OBPreferenceManager.setPreference("dateTimeSetupComplete", true);
                //
                if (OBConfigManager.sharedManager.isEnablingAdministratorServicesRequired()) {
                    boolean hasAdministratorPrivileges = OBSystemsManager.sharedManager.hasAdministratorPrivileges();
                    if (!hasAdministratorPrivileges) {
                        MainActivity.log("MainActivity.App does not have administrator privileges. Requesting");
                        //
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        final PackageManager packageManager = MainActivity.mainActivity.getPackageManager();
                        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
                        if (resolveInfos != null && !resolveInfos.isEmpty()) {
                            try {
                                final ResolveInfo resolveInfo = resolveInfos.get(0);
                                intent = new Intent();
                                intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, OBSystemsManager.sharedManager.AdministratorReceiver());
                                startActivityForResult(intent, MainActivity.REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES);
                                return;
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return;
                    }
                }
                if (OBConfigManager.sharedManager.isRequestingDeviceOwnerRequired()) {
                    //
                    boolean isDeviceOwner = OBSystemsManager.sharedManager.isDeviceOwner();
                    if (!isDeviceOwner) {
                        OBSystemsManager.sharedManager.requestDeviceOwner();
                    }
                }
                //
                log("First Setup complete. Loading Main View Controller");
                //
                OBPreferenceManager.setPreference("firstSetupComplete", true);
                //
                OBSystemsManager.sharedManager.unzipAssetsIfFound(new OBUtils.RunLambda() {
                    @Override
                    public void run() throws Exception {
                        OBConfigManager.sharedManager.updateConfigPaths(OBConfigManager.sharedManager.getMainFolder(), true);
                        //
                        runChecksAndLoadMainViewController();
                    }
                });
            }
        });
    }


    public void runChecksAndLoadMainViewController() {
        MainActivity.log("MainActivity.startup block. memory dump");
        OBSystemsManager.sharedManager.printMemoryStatus("Before mainViewController");
        //
        MainActivity.log("MainActivity.startup block. creating mainViewControlller");
        mainViewController = new OBMainViewController(MainActivity.mainActivity);
    }


    // This bypasses the power button (long press), preventing shutdown
    public void onWindowFocusChanged(boolean hasFocus) {
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


    public void onBackPressed() {
        // do nothing
    }


    public void doGLStuff() {
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

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.
            glSurfaceView.setRenderer(renderer = new OBRenderer());
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        //
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    OBSectionController topController() {
        List l = mainViewController.viewControllers;
        return (OBSectionController) l.get(l.size() - 1);
    }

    public void setUpConfig() throws Exception {
        OBSystemsManager.sharedManager.printMemoryStatus("setupconfig");
        //
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int w = metrics.widthPixels;
        int h = metrics.heightPixels;
        OBConfigManager.sharedManager.updateGraphicScale(w, h);
        //
        OBConfigManager.sharedManager.updateConfigPaths(OBConfigManager.sharedManager.getMainFolder(), true);
        //
        String fatControllerClassName = OBConfigManager.sharedManager.getFatControllerClassName();
        Class aClass = Class.forName("com.maq.xprize.onecourse.hindi.utils." + fatControllerClassName);
        Constructor<?> cons = aClass.getConstructor();
        fatController = (OBFatController) cons.newInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mainViewController != null) {
            mainViewController.onPause();
        }
        if (renderer != null) {
            glSurfaceView.onPause();
        }
        //
        OBAnalyticsManager.sharedManager.deviceScreenTurnedOff();
        //
        suspendLock.lock();
        OBSystemsManager.sharedManager.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        OBSystemsManager.sharedManager.onDestroy();
    }

    @Override
    protected void onResume() {
        OBSystemsManager.sharedManager.onResume();
        super.onResume();
        //
        if (mainViewController != null)
            mainViewController.onResume();
        if (renderer != null) {
            glSurfaceView.onResume();
        }
        //
        setupWindowVisibilityFlags();
        //
        OBAnalyticsManager.sharedManager.deviceScreenTurnedOn();
        //
        try {
            suspendLock.unlock();
        } catch (Exception e) {

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        analyticsManager.onStart();
        //
        OBSystemsManager.sharedManager.onStart();
        //
        OBSystemsManager.sharedManager.runChecks();
    }


    @Override
    protected void onStop() {
        systemsManager.sharedManager.onStop();
        analyticsManager.onStop();
        super.onStop();
    }

    public void onAlarmReceived(Intent intent) {
        if (mainViewController != null)
            mainViewController.onAlarmReceived(intent);
    }

    public void onBatteryStatusReceived(float level, boolean charging) {
        if (mainViewController != null)
            mainViewController.onBatteryStatusReceived(level, charging);
    }


    public boolean isStoragePermissionGranted() {
        Boolean writePermission = selfPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean readPermission = selfPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        //
        Boolean result = writePermission && readPermission;
        if (!result)
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        //
        return result;
    }


    public boolean isMicrophonePermissionGranted() {
        Boolean micPermission = selfPermissionGranted(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        //
        if (!micPermission)
            ActivityCompat.requestPermissions(this, PERMISSIONS_MICROPHONE, REQUEST_MICROPHONE);
        //
        return micPermission;
    }

    public boolean isCameraPermissionGranted() {
        Boolean micPermission = selfPermissionGranted(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        //
        if (!micPermission)
            ActivityCompat.requestPermissions(this, PERMISSIONS_CAMERA, REQUEST_CAMERA);
        //
        return micPermission;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            log("received permission to access external storage. attempting to download again");
            runChecksAndLoadMainViewController();
        } else if (requestCode == REQUEST_ALL) {
            checkForFirstSetupAndRun();
        }
    }

    public boolean isAllPermissionGranted() {
        return isAllPermissionGranted(true);
    }

    public boolean isAllPermissionGranted(boolean requestIfNotGranted) {
        Boolean allPermissionsOK = true;
        for (String permission : PERMISSION_ALL) {
            boolean permissionGranted = selfPermissionGranted(permission) == PackageManager.PERMISSION_GRANTED;
            MainActivity.log("MainActivity.Permission " + (permissionGranted ? "" : "NOT ") + "granted: " + permission);
            allPermissionsOK = allPermissionsOK && permissionGranted;
        }
        if (!allPermissionsOK && requestIfNotGranted)
            ActivityCompat.requestPermissions(this, PERMISSION_ALL, REQUEST_ALL);
        //
        return allPermissionsOK;
    }


    public void addToPreferences(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.apply();
        //
        log("Preferences SET [" + key + "] --> " + value);
    }


    public String getPreferences(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        String result = sharedPreferences.getString(key, null);
        //
        log("Preferences GET [" + key + "] --> " + result);
        //
        return result;
    }

    public static void log(String message) {
        if (message == null) return;
        Log.v(TAG, message);
    }

    public static void log(String format, Object... args) {
        try {
            log(String.format(format, args));
        } catch (Exception e) {
            MainActivity.log("Exception caught in log with format: %s", format);
            e.printStackTrace();
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (!OBSystemsManager.sharedManager.settingsContentObserver.allowsLowerVolume())
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public int selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        int result = PackageManager.PERMISSION_GRANTED;
        ;
        //
        if (isSDKCompatible()) {
            return ActivityCompat.checkSelfPermission(MainActivity.mainActivity.getApplicationContext(), permission);
        }
        return result;
    }


    public static boolean isSDKCompatible() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && targetSdkVersion >= Build.VERSION_CODES.M;
    }

    public void restartApplication() {
        OBSystemsManager.sharedManager.unpinApplication();
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(i);
            }
        });

    }

}

