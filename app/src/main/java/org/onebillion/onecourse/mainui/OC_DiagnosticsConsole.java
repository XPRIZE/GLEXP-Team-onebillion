package org.onebillion.onecourse.mainui;

import android.text.format.DateUtils;
import android.text.style.QuoteSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by pedroloureiro on 2018.06.18
 */

public class OC_DiagnosticsConsole extends OBSectionController
{
    private ListView remedialUnits_list, question_list, unitIndex_list;
    private SeekBar week_slider, wrongAnswers_slider, totalRuns_slider;
    private TextView week_value, wrongAnswers_value, totalRuns_value, runTests_remaining;
    private Button runDiagnostics, runTests;
    private ProgressBar runTests_progress;
    //
    private String selectedQuestionUUID;
    //
    private final ArrayList<String> question_array = new ArrayList(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p"));
    private Map<String,Map> remedialUnitsTestResults;


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
        runTests_progress = (ProgressBar) MainActivity.mainActivity.findViewById(R.id.runTests_progress);
        runTests_progress.setVisibility(INVISIBLE);
        //
        runTests_remaining = (TextView) MainActivity.mainActivity.findViewById(R.id.runTests_remaining);
        runTests_remaining.setVisibility(INVISIBLE);
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
                question_list.setVisibility(INVISIBLE);
                remedialUnits_list.setVisibility(INVISIBLE);
                //
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
        week_slider.setMax(60);
        week_slider.setProgress(60);
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
        wrongAnswers_slider.setMax(3);
        wrongAnswers_slider.setProgress(1);
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
        totalRuns_slider.setMax(1000);
        totalRuns_slider.setProgress(10);
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
                selectedQuestionUUID = (String) parent.getItemAtPosition(position);
                //
                List<Integer> entriesForQuestion = new ArrayList(remedialUnitsTestResults.get(selectedQuestionUUID).keySet());
                Collections.sort(entriesForQuestion);
                final List<Integer> resultsForQuestion = entriesForQuestion;
                //
                if (resultsForQuestion == null) return;
                //
                ArrayAdapter unitIndex_arrayAdapter = new ArrayAdapter<Integer>(MainActivity.mainActivity, android.R.layout.simple_list_item_1, resultsForQuestion)
                {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent)
                    {
                        String questionUUID = "  " + getItem(position) + "  ";
                        //
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        view.setTextSize(28);
                        ViewGroup.LayoutParams params = view.getLayoutParams();
                        params.height = 150;
                        view.setText(questionUUID);
                        //
                        return view;
                    }
                };
                unitIndex_list.setAdapter(unitIndex_arrayAdapter);
                unitIndex_list.setVisibility(VISIBLE);
                //
                remedialUnits_list.setVisibility(INVISIBLE);
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
                view.setTextSize(28);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 150;
                view.setText(questionUUID);
                //
                return view;
            }
        };
        question_list.setAdapter(question_arrayAdapter);
        question_list.setVisibility(INVISIBLE);
        //
        unitIndex_list = (ListView) MainActivity.mainActivity.findViewById(R.id.unitIndex_list);
        unitIndex_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        unitIndex_list.setSelector(android.R.color.darker_gray);
        unitIndex_list.setVisibility(INVISIBLE);
        unitIndex_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Integer entry = (Integer) parent.getItemAtPosition(position);
                //
                Map<String, List> entriesForQuestionUUID = remedialUnitsTestResults.get(selectedQuestionUUID);
                List entriesForEntry = entriesForQuestionUUID.get(entry);
                //
                final List<String> resultsForQuestion = entriesForEntry;
                if (resultsForQuestion == null) return;
                //
                ArrayAdapter remedialUnit_arrayAdapter = new ArrayAdapter<String>(MainActivity.mainActivity, android.R.layout.simple_list_item_1, resultsForQuestion)
                {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent)
                    {
                        String entry = "  " + getItem(position) + "  ";
                        //
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        view.setTextSize(18);
                        ViewGroup.LayoutParams params = view.getLayoutParams();
                        params.height = 150;
                        view.setText(entry);
                        //
                        return view;
                    }
                };
                remedialUnits_list.setAdapter(remedialUnit_arrayAdapter);
                remedialUnits_list.setVisibility(VISIBLE);
            }
        });
        //
        remedialUnits_list = (ListView) MainActivity.mainActivity.findViewById(R.id.remedialUnits_list);
        remedialUnits_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        remedialUnits_list.setSelector(android.R.color.darker_gray);
        remedialUnits_list.setVisibility(INVISIBLE);
        //
    }


    private void runTests()
    {
        int week = (week_slider.getProgress() > 0) ? week_slider.getProgress() : 1;
        final int totalRuns = (totalRuns_slider.getProgress() > 0) ? totalRuns_slider.getProgress() : 1;
        //
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                question_list.setVisibility(INVISIBLE);
                remedialUnits_list.setVisibility(INVISIBLE);
                unitIndex_list.setVisibility(INVISIBLE);
                //
                runTests_progress.setVisibility(VISIBLE);
                runTests_progress.setProgress(0);
                runTests_progress.setMax(totalRuns * question_array.size());
                //
                runTests_remaining.setVisibility(VISIBLE);
                runTests_remaining.setText("");
                //
                totalRuns_slider.setEnabled(false);
                week_slider.setEnabled(false);
                wrongAnswers_slider.setEnabled(false);
                //
                runDiagnostics.setEnabled(false);
                runTests.setEnabled(false);
            }
        });
        //
        remedialUnitsTestResults = new HashMap<>();
        Map<String, Map> statistics = new HashMap<>();
        Map<String, Double> loadTimes = new HashMap<>();
        //
        OC_DiagnosticsManager.sharedManager().resetDiagnostics(10, week, "", question_array, false);
        int progressCounter = 0;
        double totalTime = 0.0;
        double lastTimeRemainingTimeWasRefreshed = OC_Generic.currentTime();
        //
        for (String questionUUID : question_array)
        {
            Map<Integer,Map> entries = new HashMap<>();
            Double loadTime = 0.0;
            //
            for (int i = 0; i < totalRuns; i++)
            {
                runTests_progress.setProgress(progressCounter);
                progressCounter++;
                //
                double start = OC_Generic.currentTime();
                List<String> remedialUnits = OC_DiagnosticsManager.sharedManager().retrieveRemedialUnitsForEvent(questionUUID, null);
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
                Double totalTimeForQuestion = loadTimes.get(questionUUID);
                if (totalTimeForQuestion == null)
                {
                    totalTimeForQuestion = new Double(0);
                }
                loadTimes.put(questionUUID, totalTimeForQuestion + elapsed);
                //
                totalTime += elapsed;
                double average = totalTime / progressCounter;
                final double remaining = ((totalRuns * question_array.size()) - progressCounter) * average;
                //
                double elapsedRefresh = OC_Generic.currentTime() - lastTimeRemainingTimeWasRefreshed;
                if (elapsedRefresh > 1.0)
                {
                    lastTimeRemainingTimeWasRefreshed = OC_Generic.currentTime();
                    //
                    OBUtils.runOnMainThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            runTests_remaining.setText(DateUtils.formatElapsedTime((long) remaining));
                        }
                    });
                }
            }
            //
            statistics.put(questionUUID, entries);
        }
        //
        List<String> questions = new ArrayList<>(statistics.keySet());
        Collections.sort(questions, String.CASE_INSENSITIVE_ORDER);
        //
        remedialUnitsTestResults = new HashMap<>();
        //
        for (String questionUUID : questions)
        {
            Map<Integer, List> testResultsForQuestion = new HashMap<>();
            //
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
                int totalUsages = 0;
                //
                for (String unit : sortedUnits)
                {
                    totalUsages += units.get(unit);
                }
                //
                List<String> testResultsForEntry = new ArrayList<>();
                //
                for (String unit : sortedUnits)
                {
                    Integer usages = units.get(unit);
                    float percentage = (100.0f * usages) / (float) totalUsages;
                    //
                    Map<String,Object> unitAttributes = OC_DiagnosticsManager.sharedManager().getUnitAttributes(unit);
                    if (unitAttributes == null)
                    {
                        MainActivity.log("ERROR --> found empty unit for " + questionUUID + " " + entry);
                        continue;
                    }
                    //
                    String parameters = (String) unitAttributes.get("params");
                    String output = String.format("%02.2f%% (%03d) - %s - %s", percentage, usages, unit, parameters);
                    //
                    testResultsForEntry.add(output);
                    //MainActivity.log(questionUUID + " " + output);
                }
                //
                testResultsForQuestion.put(entry, testResultsForEntry);
            }
            //
            remedialUnitsTestResults.put(questionUUID, testResultsForQuestion);
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
                runTests_progress.setVisibility(INVISIBLE);
                //
                runTests_remaining.setVisibility(INVISIBLE);
                //
                question_list.setVisibility(VISIBLE);
                //
                totalRuns_slider.setEnabled(true);
                week_slider.setEnabled(true);
                wrongAnswers_slider.setEnabled(true);
                //
                runDiagnostics.setEnabled(true);
                runTests.setEnabled(true);
            }
        });
    }

}
