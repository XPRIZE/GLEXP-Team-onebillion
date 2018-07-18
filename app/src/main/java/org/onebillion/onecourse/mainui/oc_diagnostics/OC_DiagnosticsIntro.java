package org.onebillion.onecourse.mainui.oc_diagnostics;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OCM_FatController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 03/05/2018.
 */

public class OC_DiagnosticsIntro extends OC_Diagnostics
{
    OBPresenter presenter;
    boolean endSequence;
    List fixedEvents;


    public void prepare()
    {
        super.prepare();
        String endValue = parameters.get("end");
        endSequence = endValue != null && endValue.equals("true");
        if (parameters.get("events") != null)
        {
            fixedEvents = Arrays.asList(parameters.get("events").split(","));
        }
        String debugValue = parameters.get("debug");
        boolean debugFlag = (debugValue != null) && debugValue.equals("true");
        if (!endSequence)
        {
            int totalQuestions = 10;
            String totalQuestionsParameter = parameters.get("questions");
            if (totalQuestionsParameter != null) totalQuestions = Integer.parseInt(totalQuestionsParameter);
            //
            OCM_FatController fatController = (OCM_FatController) MainActivity.mainActivity.fatController;
            int thresholdWeek = fatController.getCurrentWeek();
            String thresholdWeekParameter = parameters.get("week");
            if (thresholdWeekParameter != null) thresholdWeek = Integer.parseInt(thresholdWeekParameter);
            //
            OC_DiagnosticsManager.sharedManager().resetDiagnostics(totalQuestions, thresholdWeek, (String) params, fixedEvents, debugFlag);
        }
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restpos", OC_Generic.copyPoint(presenter.control.position()));
            presenter.control.setRight(0);
            presenter.control.show();
        }
        //
        hideControls("background.*");
        events = new ArrayList<>();
        if (endSequence)
        {
            events.add("exit");
        }
        else
        {
            events.add("intro");
        }
        doVisual(currentEvent());
    }


    public void setSceneXX(String scene)
    {
        MainActivity.log("Intro Scene %s", scene);
        showQuestionProgress(!endSequence);
    }

    public void finishEvent()
    {
        if(MainActivity.mainActivity.fatController.getClass() == OCM_FatController.class)
        {
            OCM_FatController fatController = (OCM_FatController)MainActivity.mainActivity.fatController;
            fatController.completeEventWithStar(this,false);
        }
        else
        {
            fin();
        }
    }

    public void start()
    {
        setStatus(STATUS_BUSY);
        if (fixedEvents != null)
        {
            if (endSequence)
            {
                MainActivity.log("OC_DiagnosticsIntro --> fixed events detected --> exit event");
                finishEvent();
            }
            else
            {
                MainActivity.log("OC_DiagnosticsIntro --> fixed events detected --> loading first question");
                OC_DiagnosticsManager.sharedManager().loadCurrentQuestion();
            }
        }
        else
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        if (!performSel("demo", currentEvent()))
                        {
                            doBody(currentEvent());
                        }
                    }
                    catch (Exception e)
                    {
                        MainActivity.log("OC_DiagnosticsIntro --> Exception caught %s", e.toString());
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    public void demointro() throws Exception
    {
        setStatus(STATUS_BUSY);
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        List audioFiles = getAudioForScene(currentEvent(), "DEMO");
        presenter.speak(audioFiles, 0.3f, this);
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        OBControl front = presenter.control.objectDict.get("front");
        PointF destPos = new PointF(bounds().width() + 2.5f * front.width(), currPos.y);
        presenter.walk(destPos);
        //
        OC_DiagnosticsManager.sharedManager().loadCurrentQuestion();
    }


    public void demoexit() throws Exception
    {
        setStatus(STATUS_BUSY);
        int wrongAnswers = OC_DiagnosticsManager.sharedManager().WrongAnswers();
        String audioScene = wrongAnswers == 0 ? "DEMO" : wrongAnswers == 1 ? "DEMO2" : wrongAnswers == 2 ? "DEMO3" : "DEMO4";
        List audioFiles = getAudioForScene(currentEvent(), audioScene);
        //
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        presenter.speak(audioFiles, 0.3f, this);
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        OBControl front = presenter.control.objectDict.get("front");
        PointF destPos = new PointF(0 - 2.5f * front.width(), currPos.y);
        presenter.walk(destPos);
        finishEvent();
    }




}

/*





 */