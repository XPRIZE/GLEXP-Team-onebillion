package org.onebillion.onecourse.mainui;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBConfigManager;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 2018.06.18
 */

public class OC_DiagnosticsConsole extends OBSectionController
{
    private ListView listView;
    private SeekBar week_slider, wrongAnswers_slider, totalRuns_slider;
    private TextView week_value, wrongAnswers_value, totalRuns_value;
    private Button runDiagnostics, runTests;


    public OC_DiagnosticsConsole()
    {
        super(MainActivity.mainActivity, false);
    }

    public void prepare()
    {
        setStatus(STATUS_IDLE);
        initScreen();
    }

    public void initScreen()
    {
        MainActivity.mainActivity.setContentView(R.layout.diagnostics_console);
        //
        runDiagnostics = (Button) MainActivity.mainActivity.findViewById(R.id.diagnostics_button);
        runDiagnostics.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity.log("Running Diagnostics");
                //
                OBConfigManager.sharedManager.updateConfigPaths("oc-diagnostics", true);
                //
                String params = String.format("diagnostics/debug=true/questions=10/week=%d", week_slider.getProgress());
                MainActivity.mainViewController.pushViewControllerWithNameConfig("OC_DiagnosticsIntro", "oc-diagnostics,oc-literacy-gen,oc-numeracy-gen", true, true, params);
            }
        });
        //
        runTests = (Button) MainActivity.mainActivity.findViewById(R.id.runTests_button);
        runTests.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity.log("Running Tests");
            }
        });
        //
        week_slider = (SeekBar) MainActivity.mainActivity.findViewById(R.id.week_slider);
        week_slider.setProgress(24);
        week_slider.setMax(36);
        //
        week_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                week_value.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        //
        week_value = (TextView) MainActivity.mainActivity.findViewById(R.id.week_value);
        week_value.setText(String.valueOf(week_slider.getProgress()));
        //
        wrongAnswers_slider = (SeekBar) MainActivity.mainActivity.findViewById(R.id.wrongAnwers_slider);
        wrongAnswers_slider.setProgress(1);
        wrongAnswers_slider.setMax(3);
        //
        wrongAnswers_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                wrongAnswers_value.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        //
        wrongAnswers_value = (TextView) MainActivity.mainActivity.findViewById(R.id.wrongAnwers_value);
        wrongAnswers_value.setText(String.valueOf(wrongAnswers_slider.getProgress()));
        //
        totalRuns_slider = (SeekBar) MainActivity.mainActivity.findViewById(R.id.totalRuns_slider);
        totalRuns_slider.setProgress(1);
        totalRuns_slider.setMax(1000);
        //
        totalRuns_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                totalRuns_value.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        //
        totalRuns_value = (TextView) MainActivity.mainActivity.findViewById(R.id.totalRuns_value);
        totalRuns_value.setText(String.valueOf(totalRuns_slider.getProgress()));
    }




}
