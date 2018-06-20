package org.onebillion.onecourse.mainui;

import android.text.style.QuoteSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.oc_diagnostics.OC_Diagnostics;
import org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;

/**
 * Created by pedroloureiro on 2018.06.18
 */

public class OC_DiagnosticsConsole extends OBSectionController
{
    private ListView remedialUnits_list, question_list;
    private SeekBar week_slider, wrongAnswers_slider, totalRuns_slider;
    private TextView week_value, wrongAnswers_value, totalRuns_value;
    private Button runDiagnostics, runTests;
    //
    private final ArrayList<String> question_array = new ArrayList(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p"));
    private Map<String,List<String>> remedialUnitsTestResults;


    public OC_DiagnosticsConsole()
    {
        super(MainActivity.mainActivity, false);
    }

    public void prepare()
    {
        OBConfigManager.sharedManager.updateConfigPaths("oc-diagnostics,oc-literacy-gen,oc-numeracy-gen", true);
        //
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
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        runTests();
                    }
                });
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
        //
        question_list = (ListView) MainActivity.mainActivity.findViewById(R.id.question_list);
        question_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        question_list.setSelector(android.R.color.darker_gray);
        question_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String question = (String) parent.getItemAtPosition(position);
                //
                MainActivity.log("Select Remedial units for question " + question);
                //
                final List<String> resultsForQuestion = remedialUnitsTestResults.get(question);
                ArrayAdapter remedialUnit_arrayAdapter = new ArrayAdapter<String>(MainActivity.mainActivity, android.R.layout.simple_list_item_1, resultsForQuestion)
                {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent)
                    {
                        String questionUUID = "  " + getItem(position) + "  ";
                        //
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        view.setTextSize(40);
                        ViewGroup.LayoutParams params = view.getLayoutParams();
                        params.height = 150;
                        view.setText(questionUUID);
                        //
                        return view;
                    }
                };
                remedialUnits_list.setAdapter(remedialUnit_arrayAdapter);
                remedialUnits_list.setVisibility(VISIBLE);
            }
        });
        question_list.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return false;
            }
        });
        //
        ArrayAdapter question_arrayAdapter = new ArrayAdapter<String>(MainActivity.mainActivity, android.R.layout.simple_list_item_1, question_array)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                String questionUUID = "  " + getItem(position) + "  ";
                //
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextSize(40);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 150;
                view.setText(questionUUID);
                //
                return view;
            }
        };
        question_list.setAdapter(question_arrayAdapter);
        question_list.setVisibility(View.INVISIBLE);
        //
        remedialUnits_list = (ListView) MainActivity.mainActivity.findViewById(R.id.remedialUnits_list);
        remedialUnits_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        remedialUnits_list.setSelector(android.R.color.darker_gray);
        remedialUnits_list.setVisibility(View.INVISIBLE);
    }


    private void runTests()
    {
        int week = week_slider.getProgress();
        int totalRuns = totalRuns_slider.getProgress();
        //
        remedialUnitsTestResults = new HashMap<>();
        Map<String, Map> statistics = new HashMap<>();
        Map<String, Double> loadTimes = new HashMap<>();
        //
        OC_DiagnosticsManager.sharedManager().resetDiagnostics(10, week, "", question_array, false);
        //
        for (String questionUUID : question_array)
        {
            Map<Integer,Map> entries = new HashMap<>();
            Double loadTime = 0.0;
            //
            for (int i = 0; i < totalRuns; i++)
            {
                MainActivity.log("Running test " + i + " for question " + questionUUID);
                //
                double start = OC_Generic.currentTime();
                List<String> remedialUnits = OC_DiagnosticsManager.sharedManager().retrieveRemedialUnitsForEvent(questionUUID, new ArrayList<String>());
                double elapsed = OC_Generic.currentTime() - start;
                //
                for (int index = 0; index < remedialUnits.size(); index++)
                {
                    String remedialUnit = remedialUnits.get(index);
                    //
                    Map<String, Integer> unitUsage = entries.get(index + 1);
                    if (unitUsage == null)
                    {
                        unitUsage = new HashMap();
                        entries.put(index + 1, unitUsage);
                    }
                    //
                    Integer usages = unitUsage.get(remedialUnit);
                    if (usages == null)
                    {
                        usages = new Integer(0);
                    }
                    unitUsage.put(remedialUnit, usages + 1);
                }
                //
                Double totalTime = loadTimes.get(questionUUID);
                if (totalTime == null)
                {
                    totalTime = new Double(0);
                }
                loadTimes.put(questionUUID, totalTime + elapsed);
            }
            //
            statistics.put(questionUUID, entries);
        }
        //
        List<String> questions = new ArrayList(statistics.keySet());
        Collections.sort(questions, String.CASE_INSENSITIVE_ORDER);
        //
        for (String questionUUID : questions)
        {
            Map<Integer, Map> entries = statistics.get(questionUUID);
            List<Integer> sortedEntries = new ArrayList(entries.keySet());
            Collections.sort(sortedEntries);
            //
            for (Integer entry : sortedEntries)
            {
                Map<String, Integer> units = entries.get(entry);
                List<String> sortedUnits = new ArrayList(units.keySet());
                Collections.sort(sortedUnits, String.CASE_INSENSITIVE_ORDER);
                //
                for (String unit : sortedUnits)
                {
                    Integer usages = units.get(unit);
                    //
                    MainActivity.log(questionUUID + " " + entry + " " + unit + " " + usages);
                }
            }
        }
        //
        for (String questionUUID : questions)
        {
            double totalLoadTime = loadTimes.get(questionUUID);
            double averageLoadTime = totalLoadTime / totalRuns;
            //
            int averageMS = Math.round((float)averageLoadTime * 1000.0f);
            //
            MainActivity.log(questionUUID + " " + averageMS + "ms");
        }
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                question_list.setVisibility(VISIBLE);
            }
        });
    }

}
