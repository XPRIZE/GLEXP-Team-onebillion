package org.onebillion.onecourse.mainui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TimePicker;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.DBSQL;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OCM_MlUnit;
import org.onebillion.onecourse.utils.OBBrightnessManager;
import org.onebillion.onecourse.utils.OBConnectionManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPreferenceManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Date;
import java.util.Random;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OCM_FatController;
import org.onebillion.onecourse.utils.OCM_User;


/**
 * Created by pedroloureiro on 27/07/2017.
 */

public class OBSetupMenu extends OC_SectionController implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener
{
    enum ScreenType
    {
        HOME_SCREEN, SET_DATE_SCREEN, SET_TRIAL_DATE_SCREEN, CONFIRMATION_SCREEN, VIDEO_SCREEN, BATTERY_SCREEN, FINAL_SCREEN;
    }
    //
    private static Date trialStartDate = new GregorianCalendar(2017,11,15).getTime(); // 0 based months, yay
    private static float clockRefreshInterval = 5.0f;
    private static String videoFilename = "tablet_care_sw.mp4";
    //
    private String saveConfig;
    private OBLabel dateTimeField, serverOKField, serverNotFoundField, setDateTimeButtonLabel;
    private OBLabel currentDateField, startOfTrialDateField;
    //
    private Date userSetDate, trialDate, serverDate;
    //
    private ScreenType screenType;
    //
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    //
    private List homeScreenControls, setDateScreenControls, setTrialStartDateScreenControls, confirmationScreenControls, videoScreenControls, finalScreenControls;
    //
    OBVideoPlayer videoPlayer;



    public void prepare ()
    {
        super.prepare();
        //
        MainActivity.log("OBSetupMenu:prepare");
        //
        // check all permissions at startup
        boolean permission1 = MainActivity.mainActivity.isAllPermissionGranted();
        //
        loadFingers();
        //
        saveConfig = OBConfigManager.sharedManager.getCurrentActivityFolder();
        //
        loadHomeScreen();
        //
        OBBrightnessManager.sharedManager.onSuspend();
    }

    public int buttonFlags ()
    {
        return 0;
    }

    public void setSceneXX ()
    {

    }

    @Override
    public void viewWillAppear (Boolean animated)
    {
        super.viewWillAppear(animated);
        for (OBControl c : filterControls("button.*"))
        {
            c.lowlight();
        }
        if (saveConfig != null)
        {
            OBConfigManager.sharedManager.updateConfigPaths(saveConfig, false);
        }
    }


    public void start ()
    {
        super.start();
        setStatus(STATUS_AWAITING_CLICK);
        //
        OBBrightnessManager.sharedManager.setScreenSleepTimeToMax();
        //
        MainActivity.log("OBSetupMenu.start.updating config paths to setup menu folder");
        OBConfigManager.sharedManager.updateConfigPaths(OBConfigManager.sharedManager.getSetupMenuFolder(), true);
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                getServerTime();
            }
        });
    }


    public void getServerTime ()
    {
        MainActivity.log("OBSetupMenu:getServerTime");
        //
        final String wifiSSID = OBConfigManager.sharedManager.getTimeServerWifiSSID();
        final String wifiPassword = OBConfigManager.sharedManager.getTimeServerWifiPassword();
        final String timeServerURL = OBConfigManager.sharedManager.getTimeServerURL();
        //
        String currentWifiSSID = OBConnectionManager.sharedManager.getCurrentWifiSSID();
        if (currentWifiSSID == null || !wifiSSID.equalsIgnoreCase(currentWifiSSID))
        {
            MainActivity.log("OBSetupMenu:getServerTime:connected to the wrong wifi [" + currentWifiSSID + "]");
            MainActivity.log("OBSetupMenu:getServerTime:attempting to connect to time server wifi [" + wifiSSID + "]");
            //
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
             {
                 @Override
                 public void run () throws Exception
                 {
                     OBConnectionManager.sharedManager.connectToNetwork_connectToWifi(wifiSSID, wifiPassword, new OBUtils.RunLambdaWithSuccess()
                     {
                         @Override
                         public void run (boolean success) throws Exception
                         {
                             getServerTime();
                         }
                     });
                 }
             });
            //
            // dont continue any further until we have connected to the correct wifi
            return;
        }
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                NTPUDPClient client = new NTPUDPClient();
                client.setDefaultTimeout(1000);
                //
                try
                {
                    client.open();
                    InetAddress hostAddr = InetAddress.getByName(timeServerURL);
                    //
                    TimeInfo info = client.getTime(hostAddr);
                    info.computeDetails();
                    client.close();
                    //
                    serverDate = new Date(info.getMessage().getReceiveTimeStamp().getTime());
                    //
                    homeScreen_refreshDate();
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                    //
                    MainActivity.log("OBSetupMenu:unable to reach wifi. Attempting to connect to time server wifi in settings.plist");
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            OBConnectionManager.sharedManager.connectToNetwork_connectToWifi(wifiSSID, wifiPassword, new OBUtils.RunLambdaWithSuccess()
                            {
                                @Override
                                public void run (boolean success) throws Exception
                                {
                                    getServerTime();
                                }
                            });
                        }
                    });
                    //
                    // dont continue any further until we have connected to the correct wifi
                    return;
                }
                catch (SocketTimeoutException e)
                {
                    MainActivity.log("OBSetupMenu:getServerTime: time out occurred");
                    serverDate = null;
                    homeScreen_refreshDate();
                }
                catch (Exception e)
                {
                    MainActivity.log("OBSetupMenu:getServerTime:exception caught");
                    e.printStackTrace();
                    //
                    serverDate = null;
                    homeScreen_refreshDate();
                }
                //
                if (!_aborting)
                {
                    OBUtils.runOnOtherThreadDelayed(clockRefreshInterval, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            getServerTime();
                        }
                    });
                }
            }
        });
    }


    public void homeScreen_refreshDate ()
    {
        if (dateTimeField == null)
        {
            MainActivity.log("OBSetupMenu:homeScreen_showDate:dateTimeField not setup. skipping");
            return;
        }
        //
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm   dd MMMM yyyy");
        Date date = userSetDate;
        Boolean fromServerTime = false;
        //
        if (date == null)
        {
            date = serverDate;
            fromServerTime = true;
        }
        if (date == null)
        {
            date = new Date(System.currentTimeMillis());
            fromServerTime = false;
        }
        final Boolean finalFromServerTime = fromServerTime;
        final Date finalDate = date;
        //

        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                OBPath boxForLabel = (OBPath) dateTimeField.propertyValue("box");
                dateTimeField.setString(sdf.format(finalDate));
                dateTimeField.sizeToBoundingBox();
                dateTimeField.setPosition(OC_Generic.copyPoint(boxForLabel.position()));
                dateTimeField.setLeft(boxForLabel.left());
                //
                OBPath button = (OBPath) setDateTimeButtonLabel.propertyValue("box");
                if (userSetDate != null && serverDate != null)
                {
                    setDateTimeButtonLabel.setString("Use Time Server");
                }
                else
                {
                    setDateTimeButtonLabel.setString((String) button.attributes().get("text"));
                }
                setDateTimeButtonLabel.sizeToBoundingBox();
                setDateTimeButtonLabel.setPosition(OC_Generic.copyPoint(button.position()));
                //
                if (screenType == ScreenType.HOME_SCREEN)
                {
                    dateTimeField.show();
                    //
                    if (userSetDate != null && serverDate != null)
                    {
                        // user has set the time, but the time server is working fine
                        serverOKField.show();
                        boxForLabel = (OBPath) serverOKField.propertyValue("box");
                        serverOKField.setString("User set date and time");
                        serverOKField.sizeToBoundingBox();
                        serverOKField.setPosition(OC_Generic.copyPoint(boxForLabel.position()));
                        serverOKField.setLeft(boxForLabel.left());
                        //
                        serverNotFoundField.hide();
                    }
                    else if (finalFromServerTime)
                    {
                        // receiving time from server and user hasn't set the date
                        serverOKField.show();
                        boxForLabel = (OBPath) serverOKField.propertyValue("box");
                        serverOKField.setString((String) boxForLabel.attributes().get("text"));
                        serverOKField.sizeToBoundingBox();
                        serverOKField.setPosition(OC_Generic.copyPoint(boxForLabel.position()));
                        serverOKField.setLeft(boxForLabel.left());
                        //
                        serverNotFoundField.hide();
                    }
                    else
                    {
                        // no date from server and user hasn't set the date
                        serverOKField.hide();
                        serverNotFoundField.show();
                    }
                }
                else
                {
                    dateTimeField.hide();
                    serverNotFoundField.hide();
                    serverOKField.hide();
                }
                //
                unlockScreen();
            }
        });
    }


    public void loadHomeScreen ()
    {
        MainActivity.log("OBSetupMenu:loadHomeScreen");
        screenType = ScreenType.HOME_SCREEN;
        //
        final OBSetupMenu finalSelf = this;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                //
                if (homeScreenControls == null)
                {
                    MainActivity.log("OBSetupMenu:loadHomeScreen:loadingScreen");
                    //
                    homeScreenControls = loadEvent("master_home");
                    //
                    Typeface defaultFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Regular.otf");
                    Typeface boldFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Bold.otf");
                    //
                    List<OBPath> lines = (List<OBPath>) (Object) filterControls("home_line_.*");
                    for (OBPath line : lines)
                    {
                        line.sizeToBoundingBoxIncludingStroke();
                    }
                    //
                    // step numbers: set label, hide placeholder box, set text color to stroke colour of placeholder
                    setupLabelsForScreen("home_number_.*", true, 0.8f, defaultFont, "centre", true, homeScreenControls, finalSelf);
                    //
                    // top labels: set label, hide placeholder, set boldFont colour to stroke colour of placeholder, centre align
                    setupLabelsForScreen("home_title.*", true, 0.8f, boldFont, "centre", false, homeScreenControls, finalSelf);
                    //
                    // labels for steps: set label, hide placeholder, set text colour to stroke colour of placeholder, left align
                    setupLabelsForScreen("home_label_step_.*", true, 0.8f, defaultFont, "left", true, homeScreenControls, finalSelf);
                    serverOKField = (OBLabel) (objectDict.get("home_label_step_2_server_ok")).propertyValue("label");
                    serverNotFoundField = (OBLabel) (objectDict.get("home_label_step_2_no_server")).propertyValue("label");
                    //
                    // buttons for steps: set label, keep placeholder, set text colour to stroke colour of placeholder, remove stroke from placeholder, bold font
                    setupLabelsForScreen("home_button_step_.*", false, 0.7f, boldFont, "centre", true, homeScreenControls, finalSelf);
                    setDateTimeButtonLabel = (OBLabel) objectDict.get("home_button_step_set_date_time").propertyValue("label");
                    //
                    // Date and Time Field, same size as the button font
                    setupLabelsForScreen("home_field_step_2_date", true, 0.7f, boldFont, "left", false, homeScreenControls, finalSelf);
                    dateTimeField = (OBLabel) (objectDict.get("home_field_step_2_date").propertyValue("label"));
                    //
                    // Activate onecourse button (font is slightly larger than all the others)
                    setupLabelsForScreen("home_button_activate_onecourse", false, 0.7f, boldFont, "centre", false, homeScreenControls, finalSelf);
                }
                //
                for (OBControl control : attachedControls)
                {
                    control.setHidden(!homeScreenControls.contains(control));
                }
                //
                // special cases
                serverOKField.hide();
                serverNotFoundField.hide();
                //
                homeScreen_refreshDate();
                //
                unlockScreen();
            }
        });
    }


    public void loadSetDateTimeScreen ()
    {
        screenType = ScreenType.SET_DATE_SCREEN;
        //
        final OBSetupMenu finalSelf = this;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                //
                if (setDateScreenControls == null)
                {
                    setDateScreenControls = loadEvent("master_set_date");
                    //
                    Typeface defaultFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Regular.otf");
                    //
                    // Title, hide box, text colour as stroke colour on box, centred
                    setupLabelsForScreen("date_title", true, 0.7f, defaultFont, "centre", true, setDateScreenControls, finalSelf);
                }
                //
                for (OBControl control : attachedControls)
                {
                    control.setHidden(!setDateScreenControls.contains(control));
                }
                //
                showPickDateDialog(finalSelf, null);
                //
                unlockScreen();
            }
        });
    }


    public void loadSetTrialStartDateScreen ()
    {
        screenType = ScreenType.SET_TRIAL_DATE_SCREEN;
        //
        final OBSetupMenu finalSelf = this;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                //
                if (setTrialStartDateScreenControls == null)
                {
                    setTrialStartDateScreenControls = loadEvent("master_set_trial");
                    //
                    Typeface defaultFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Regular.otf");
                    //
                    // Title, hide box, text colour as stroke colour on box, centred
                    setupLabelsForScreen("trial_title", true, 0.7f, defaultFont, "centre", true, setTrialStartDateScreenControls, finalSelf);
                }
                //
                for (OBControl control : attachedControls)
                {
                    control.setHidden(!setTrialStartDateScreenControls.contains(control));
                }
                //
                showPickDateDialog(finalSelf, trialStartDate);
                //
                unlockScreen();
            }
        });
    }


    public void loadVideoScreen ()
    {
        screenType = ScreenType.VIDEO_SCREEN;
        //
        final OBSetupMenu finalSelf = this;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                //
                if (videoScreenControls == null)
                {
                    videoScreenControls = loadEvent("master_video");
                    //
                    if (videoPlayer == null)
                    {
                        RectF r = new RectF();
                        r.set(boundsf());
                        videoPlayer = new OBVideoPlayer(r, finalSelf, false, true);
                        videoPlayer.stopOnCompletion = false;
                        videoPlayer.setZPosition(190);
                        videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FIT);
                        attachControl(videoPlayer);
                        //
                        videoScreenControls.add(videoPlayer);
                    }
                    //
                    videoPlayer.playAfterPrepare = true;
                }
                //
                for (OBControl control : attachedControls)
                {
                    control.setHidden(!videoScreenControls.contains(control));
                }
                //
                unlockScreen();
                //
                String moviesFolder = getConfigPath(sectionName() + ".xml");
                moviesFolder = OBUtils.stringByDeletingLastPathComponent(moviesFolder);
                moviesFolder = OBUtils.stringByDeletingLastPathComponent(moviesFolder);
                moviesFolder = OBUtils.stringByAppendingPathComponent(moviesFolder, "movies");
                String movieFilePath = OBUtils.stringByAppendingPathComponent(moviesFolder, videoFilename);
                //
                MainActivity.log("OBSetupMenu:loadVideoScreen:playing video at path: " + movieFilePath);
                AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(movieFilePath);
                //
                videoPlayer.startPlayingAtTime(afd, 0, new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        MainActivity.log("OBSetupMenu:loadVideoScreen:reached end of video. Moving back to home screen");
                        loadHomeScreen();
                    }
                });
            }
        });
    }



    public void loadConfirmationScreen ()
    {
        screenType = ScreenType.CONFIRMATION_SCREEN;
        //
        final OBSetupMenu finalSelf = this;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                //
                if (confirmationScreenControls == null)
                {
                    confirmationScreenControls = loadEvent("master_confirm");
                    //
                    // Some weird issue with these two logos. the confirmation logo is not the same size as the home logo
                    // this is a quick fix to solve the problem
                    // Alan might know what is going on here....
                    OBGroup homeLogo = (OBGroup) objectDict.get("home_onebillion_logo");
                    OBGroup confirmationLogo = (OBGroup) objectDict.get("confirmation_onebillion_logo");
                    confirmationLogo.setScale(homeLogo.scale());
                    confirmationLogo.setBounds(homeLogo.bounds());
                    //
                    Typeface defaultFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Regular.otf");
                    Typeface boldFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Bold.otf");
                    //
                    // top labels: set label, hide placeholder, set boldFont colour to stroke colour of placeholder, centre align
                    setupLabelsForScreen("confirmation_title.*", true, 0.8f, boldFont, "centre", false, confirmationScreenControls, finalSelf);
                    //
                    // labels for steps: set label, hide placeholder, set text colour to stroke colour of placeholder, left align
                    setupLabelsForScreen("confirmation_label_.*", true, 0.8f, defaultFont, "left", true, confirmationScreenControls, finalSelf);
                    //
                    // buttons for steps: set label, keep placeholder, set text colour to stroke colour of placeholder, remove stroke from placeholder, bold font
                    setupLabelsForScreen("confirmation_button_.*", false, 0.7f, boldFont, "centre", true, confirmationScreenControls, finalSelf);
                    //
                    setupLabelsForScreen("confirmation_field_.*", true, 0.7f, boldFont, "left", false, confirmationScreenControls, finalSelf);
                    currentDateField = (OBLabel) (objectDict.get("confirmation_field_date").propertyValue("label"));
                    startOfTrialDateField = (OBLabel) (objectDict.get("confirmation_field_trial").propertyValue("label"));
                }
                //
                for (OBControl control : attachedControls)
                {
                    control.setHidden(!confirmationScreenControls.contains(control));
                }
                //
                Date currentDate = serverDate;
                if (currentDate == null) currentDate = userSetDate;
                if (currentDate == null) currentDate = new Date(System.currentTimeMillis());
                //
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("HH:mm   dd MMMM yyyy");
                OBPath boxForLabel = (OBPath) currentDateField.propertyValue("box");
                currentDateField.setString(currentDateFormat.format(currentDate));
                currentDateField.sizeToBoundingBox();
                currentDateField.setPosition(OC_Generic.copyPoint(boxForLabel.position()));
                currentDateField.setLeft(boxForLabel.left());
                //
                SimpleDateFormat trialDateFormat = new SimpleDateFormat("dd MMMM yyyy");
                boxForLabel = (OBPath) startOfTrialDateField.propertyValue("box");
                startOfTrialDateField.setString(trialDateFormat.format(trialDate));
                startOfTrialDateField.sizeToBoundingBox();
                startOfTrialDateField.setPosition(OC_Generic.copyPoint(boxForLabel.position()));
                startOfTrialDateField.setLeft(boxForLabel.left());
                //
                unlockScreen();
            }
        });
    }




    public void loadFinalScreen ()
    {
        screenType = ScreenType.FINAL_SCREEN;
        //
        final OBSetupMenu finalSelf = this;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                //
                if (finalScreenControls == null)
                {
                    finalScreenControls = loadEvent("master_final");
                    //
                    Typeface defaultFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Regular.otf");
                    Typeface boldFont = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Bold.otf");
                    //
                    // top labels: set label, hide placeholder, set boldFont colour to stroke colour of placeholder, centre align
                    setupLabelsForScreen("final_title.*", true, 0.8f, boldFont, "centre", false, finalScreenControls, finalSelf);
                    //
                    // label, hide box, text colour as stroke colour on box, centred
                    setupLabelsForScreen("final_label.*", true, 0.7f, defaultFont, "centre", true, finalScreenControls, finalSelf);
                }
                //
                for (OBControl control : attachedControls)
                {
                    control.setHidden(!finalScreenControls.contains(control));
                }
                //
                unlockScreen();
                //
                OBUtils.runOnOtherThreadDelayed(30, new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        MainActivity.log("OBSetupMenu.delayedThread:SHUTTING DOWN");
                        //
                        try
                        {
                            Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                            i.putExtra("android.intent.extra.KEY_CONFIRM", false);
                            MainActivity.mainActivity.startActivity(i);
                        }
                        catch (Exception e)
                        {
                            MainActivity.log("OBSetupMenu:loadFinalScreen:exception caught while trying to shutdown device. Exiting App");
                            System.exit(0);
                        }
                    }
                });
            }
        });
    }



    public void loadRandomUnit()
    {
        DBSQL db = null;
        //
        try
        {
            OCM_FatController fatController = (OCM_FatController)MainActivity.mainActivity.fatController;
            //
            db = new DBSQL(false);
            //
            int unitCount = OCM_MlUnit.unitCountForMasterlist(db, 1);
            int randomUnitIndex = OB_Maths.randomInt(0, unitCount);
            //
            MainActivity.log("OBSetupMenu:loadRandomUnit:unit count [%d]", unitCount);
            MainActivity.log("OBSetupMenu:loadRandomUnit:random unit [%d]", randomUnitIndex);
            //
            OCM_MlUnit randomUnit = OCM_MlUnit.mlUnitforMasterlistIDFromDB(db, 1, randomUnitIndex);
            //
//            MainActivity.mainActivity.addToPreferences(MainActivity.PREFERENCES_RUNNING_EXAMPLE_UNIT, "true");
            fatController.startSectionByUnitNoUser(randomUnit);
        }
        catch(Exception e)
        {
            MainActivity.log("OBSetupMenu:loadRandomUnit:exception caught");
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
            {
                db.close();
            }
        }
    }


    void showPickDateDialog (final DatePickerDialog.OnDateSetListener listener, Date startDate)
    {
        Calendar currentCalendar = Calendar.getInstance();
        if (startDate != null)
        {
            currentCalendar.setTime(startDate);
        }
        final Calendar calendar = currentCalendar;
        DatePickerDialog d = new DatePickerDialog(MainActivity.mainActivity, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        //
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        //
        LinearLayout linearLayout = new LinearLayout(MainActivity.mainActivity.getApplicationContext());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow().clearFlags(Window.FEATURE_ACTION_BAR);
        d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        d.setCustomTitle(linearLayout);
        //
        d.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog, int which)
            {
                MainActivity.log("OBSetupMenu:showPickDateDialog:cancelled!");
                loadHomeScreen();
            }
        });
        //
        DatePicker datePicker = d.getDatePicker();
        calendar.clear();
        calendar.set(2017, Calendar.JANUARY, 1);
        datePicker.setMinDate(calendar.getTimeInMillis());
        calendar.clear();
        calendar.set(2025, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(calendar.getTimeInMillis());
        //
        d.show();
    }


    void showPickTimeDialog (final TimePickerDialog.OnTimeSetListener listener)
    {
        final DatePickerDialog.OnDateSetListener dateListener = (DatePickerDialog.OnDateSetListener) listener;
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog d = new TimePickerDialog(MainActivity.mainActivity, listener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(MainActivity.mainActivity));
        //
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        //
        d.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Back", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog, int which)
            {
                if (screenType == ScreenType.SET_DATE_SCREEN)
                {
                    showPickDateDialog(dateListener, null);
                }
                else
                {
                    MainActivity.log("OBSetupMenu:showPickTimeDialog:cancelled!");
                }
            }
        });
        //
        LinearLayout linearLayout = new LinearLayout(MainActivity.mainActivity.getApplicationContext());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCustomTitle(linearLayout);
        //
        d.show();
    }


    public OBControl findButton (PointF pt)
    {
        return finger(0, 0, filterControls(".*_button_.*"), pt, true);
    }


    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl button = findButton(pt);
            //
            if (button != null)
            {
                if (button.attributes().get("id").equals("home_button_step_1_video"))
                {
                    MainActivity.log("OBSetupMenu:touchDownAtPoint:video button pressed");
                    loadVideoScreen();
                }
                else if (button.attributes().get("id").equals("home_button_step_1_example"))
                {
                    MainActivity.log("OBSetupMenu:touchDownAtPoint:example button pressed");
                    loadRandomUnit();
                }
                else if (button.attributes().get("id").equals("home_button_step_set_date_time"))
                {
                    MainActivity.log("OBSetupMenu:touchDownAtPoint:set date time button pressed");
                    if (userSetDate != null)
                    {
                        userSetDate = null;
                        homeScreen_refreshDate();
                    }
                    else
                    {
                        loadSetDateTimeScreen();
                    }
                }
                else if (button.attributes().get("id").equals("home_button_activate_onecourse"))
                {
                    MainActivity.log("OBSetupMenu:touchDownAtPoint:activate onecourse button pressed");
                    loadSetTrialStartDateScreen();
                }
                else if (button.attributes().get("id").equals("confirmation_button_cancel"))
                {
                    MainActivity.log("OBSetupMenu:touchDownAtPoint:cancel onecourse button pressed");
                    loadHomeScreen();
                }
                else if (button.attributes().get("id").equals("confirmation_button_start"))
                {
                    MainActivity.log("OBSetupMenu:touchDownAtPoint:start onecourse button pressed");
                    completeSetup();
                }
            }
            else if (videoPlayer != null && !videoPlayer.hidden() && videoPlayer.frame().contains(pt.x, pt.y))
            {
                MainActivity.log("Video Player has been touched");
                videoPlayer.stop();
                loadHomeScreen();
            }
        }
    }


    @Override
    public void onDateSet (DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        selectedYear = year;
        selectedMonth = monthOfYear;
        selectedDay = dayOfMonth;
        //
        MainActivity.log("OBSetupMenu:onDateSet:" + year + " " + monthOfYear + " " + dayOfMonth);
        //
        if (screenType == ScreenType.SET_DATE_SCREEN)
        {
            showPickTimeDialog(this);
        }
        else if (screenType == ScreenType.SET_TRIAL_DATE_SCREEN)
        {
            MainActivity.log("OBSetupMenu:onDateSet:Trial Date has been set to: " + selectedYear + "." + selectedMonth + "." + selectedDay);
            //
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, selectedYear);
            c.set(Calendar.MONTH, selectedMonth);
            c.set(Calendar.DAY_OF_MONTH, selectedDay);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            //
            trialDate = c.getTime();
            //
            loadConfirmationScreen();
        }
    }

    @Override
    public void onTimeSet (TimePicker view, int hourOfDay, int minute)
    {
        selectedHour = hourOfDay;
        selectedMinute = minute;
        //
        MainActivity.log("OBSetupMenu:onTimeSet:" + hourOfDay + " " + minute);
        //
        if (screenType == ScreenType.SET_DATE_SCREEN)
        {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, selectedYear);
            c.set(Calendar.MONTH, selectedMonth);
            c.set(Calendar.DAY_OF_MONTH, selectedDay);
            c.set(Calendar.HOUR_OF_DAY, selectedHour);
            c.set(Calendar.MINUTE, selectedMinute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            //
            long when = c.getTimeInMillis();
            if (when / 1000 < Integer.MAX_VALUE)
            {
                try
                {
                    ((AlarmManager) MainActivity.mainActivity.getSystemService(Context.ALARM_SERVICE)).setTime(when);
                }
                catch (Exception e)
                {
                    MainActivity.log("OBSetupMenu:onTimeSet:Exception caught while trying to set the Date");
                    e.printStackTrace();
                }
            }
            //
            userSetDate = c.getTime();
            //
            loadHomeScreen();
        }
    }


    void setupLabelsForScreen (String pattern, Boolean hideContainer, float fontResizeFactor, Typeface font, String justification, Boolean normaliseFontSize, List screenContainer, final OBSetupMenu finalSelf)
    {
        List<OBLabel> normalisedLabels = new ArrayList<>();
        float minFontSize = 10000;
        //
        List<OBPath> stepLabels = (List<OBPath>) (Object) filterControls(pattern);
        //
        for (OBPath labelBox : stepLabels)
        {
            OBLabel label = OC_Generic.action_createLabelForControl(labelBox, fontResizeFactor, false, finalSelf, font);
            label.setColour(labelBox.strokeColor());
            //
            label.sizeToBoundingBox();
            label.setLeft(labelBox.left());
            label.setProperty("box", labelBox);
            label.setProperty("justification", justification);
            //
            labelBox.setProperty("label", label);
            if (hideContainer)
            {
                // this is a normal label
                // the container is to be hidden from the scene
                labelBox.hide();
                screenContainer.remove(labelBox);
            }
            else
            {
                // this is a button, so the border needs to be removed
                // border is only used to carry the text colour to the scene
                labelBox.setLineWidth(0);
                labelBox.sizeToBoundingBoxIncludingStroke();
            }
            //
            label.setPosition(OC_Generic.copyPoint(labelBox.position()));
            //
            if (justification.equals("left"))
            {
                label.setLeft(labelBox.left());
            }
            //
            minFontSize = Math.min(label.fontSize(), minFontSize);
            normalisedLabels.add(label);
            //
            screenContainer.add(label);
        }
        //
        if (normaliseFontSize)
        {
            for (OBLabel label : normalisedLabels)
            {
                OBPath boxForLabel = (OBPath) label.propertyValue("box");
                label.setFontSize(minFontSize);
                label.sizeToBoundingBox();
                label.setPosition(OC_Generic.copyPoint(boxForLabel.position()));
                //
                if (justification.equals("left"))
                {
                    label.setLeft(boxForLabel.left());
                }
            }
        }
    }


    void completeSetup()
    {
        Date currentDate = serverDate;
        if (currentDate == null) currentDate = userSetDate;
        if (currentDate == null) currentDate = new Date(System.currentTimeMillis());
        //
        try
        {
            Calendar c = Calendar.getInstance();
            c.setTime(currentDate);
            //
            long timeInMillis = c.getTimeInMillis();
            //
            ((AlarmManager) MainActivity.mainActivity.getSystemService(Context.ALARM_SERVICE)).setTime(timeInMillis);
        }
        catch (Exception e)
        {
            MainActivity.log("OBSetupMenu:completeSetup:Exception caught while trying to set the Date");
            e.printStackTrace();
        }
        //
        OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCES_SETUP_START_TIMESTAMP, Long.toString(OBUtils.timestampForDateOnly(trialDate.getTime())));
        OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCES_SETUP_COMPLETE, "true");
        //
        loadFinalScreen();
        //
        //OBBrightnessManager.sharedManager.onContinue();
        //MainActivity.mainActivity.fatController.startUp();
    }

}
