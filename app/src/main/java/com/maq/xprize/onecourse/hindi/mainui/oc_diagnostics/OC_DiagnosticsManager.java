package com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics;


import android.database.Cursor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OBMainViewController;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.DBSQL;
import com.maq.xprize.onecourse.hindi.utils.OBConfigManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBXMLManager;
import com.maq.xprize.onecourse.hindi.utils.OBXMLNode;
import com.maq.xprize.onecourse.hindi.utils.OCM_FatController;
import com.maq.xprize.onecourse.hindi.utils.OCM_MlUnit;
import com.maq.xprize.onecourse.hindi.utils.OCM_MlUnitInstance;
import com.maq.xprize.onecourse.hindi.utils.OCM_User;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.OBViewController.MainViewController;
import static com.maq.xprize.onecourse.hindi.utils.OBUtils.LoadWordComponentsXML;

/**
 * Created by pedroloureiro on 03/05/2018.
 */

public class OC_DiagnosticsManager
{
    private static OC_DiagnosticsManager sharedDiagnosticsManager;
    //
    static String kTarget = "target";                                       // Class that will create the exercise
    static String kAudioEvent = "audio_event";                              // event in the ScriptEase related to the question
    static String kBackground = "background";                               // type of background for the exercise, use kBackgroundLiteracy or kBackgroundNumeracy
    static String kBackgroundLiteracy = "literacy";                         //
    static String kBackgroundNumeracy = "numeracy";                         //
    static String kLayout = "layout";                                       // event in ASCDraw that is loaded for the exercise
    static String kTotalQuestions = "total_questions";                      // number of questions asked to the user
    static String kTotalAvailableOptions = "total_options";                 // for each question asked, the number of options for the user to answer
    static String kClassFilter = "class_filter";                            // Remedial Units - targets that contain the necessary parameters
    static String kParameterFilter = "parameter_filter";                    // Remedial Units - parameters in the units that contain the necessary data
    static String kParameterPrefix = "parameter_prefix";                    // Remedial Units - types of word components for the exercise
    static String kParameterMaxWordLength = "parameter_max_word_length";    // To further filter words that wont fit on the label boxes
    static String kAudioOffset = "audio_offset";                            // Audio offset for the exercise in general
    static String kAudioOffsetParameter1 = "audio_offset_parameter_1";      // Audio offset for parameter 1
    static String kAudioOffsetParameter2 = "audio_offset_parameter_2";      // Audio offset for parameter 2
    static String kOptionsNumberRange = "number_range";                     // Range : the form of X-Y for options in the exercise
    static String kDistractorsNumberRange = "distractors_number_range";     // Range : the form of X-Y for distractors in the exercise
    static String kAnswerNumberRange = "answer_number_range";               // Range : the form of X-Y for answers in the exercise (the answer must fall within the range)
    static String kParameter1NumberRange = "number_range_parameter_1";       // Range : the form of X-Y for parameter 1 in the exercise;
    static String kParameter2NumberRange = "number_range_parameter_2";       // Range : the form of X-Y for parameter 1 in the exercise
    static String kNumberComparison = "number_comparison";                  // For number comparison units, use kNumberComparisonLarger or kNumberComparisonSmaller
    static String kNumberComparisonLarger = "larger";                       //
    static String kNumberComparisonSmaller = "smaller";                     //
    static String kNumberOperator = "number_operator";                      // For equation related exercises, use kNumberOperationAddition or kNumberOperationSubtraction
    static String kNumberOperatorAddition = "+";                            //
    static String kNumberOperatorSubtraction = "-";                         //
    static String kScenarios = "scenarios";                                 // For shape identification scenarios that need to be custom build based on script ease documents
    static String kSequenceLength = "sequence_length";                      // For missing numbers exercises, to indicate the length of the sequence that contains a box
    static String kLetterCase = "letter_case";                              // For letter identification exercises, to indicate the case for the letters shown : the labels, use kUppercase or kLowercase
    static String kUppercase = "uppercase";                                 //
    static String kLowercase = "lowercase";                                 //
    static String kRemedialUnits = "remedial_units";                        // Array containing the required filters to retrive the units that are related to each exercise

    Map<String, Map> allEvents;     // Collection of all available events
    List<String> remedialUnits_day1;        // Units that are added to day 1 whenever the user fails to correctly answer a question
    List<String> remedialUnits_day2;        // Units that are added to day 2 whenever the user fails to correctly answer a question
    List<Boolean> progress;         // True / False array that shows the progress of the user throughout the diagnostics
    List<Map> unitsFromMasterlist;  // Filtered units that go up to the week to extract parameters from
    Map wordComponents;             // Word Dabase for all phonemes, syllables and words (NOTE: no alphabet)
    List questionEvents;            // Sequence of events with the questions for the diagnostics
    String startingParameters;      // Parameters that were received from the masterlist that will generated all of the exercises
    List fixedEvents;               // Parameter that indicates the events shown : the exercise have been picked specifically
    int thresholdWeek;              // current week, dictating to as far as we go to retrieve parameters and remedial units
    int idleTimeout;                // time in seconds for the question to the considered wrong if there is no user interaction
    int currentQuestionIndex;       // current question to keep track of progress
    int maxWrongAnswers;            // maximum number of wrong answers a user can get before getting removed from the unit
    boolean debugEnabled;           // flag to either show the final feedback from Presenter or show the debug menu with the remedial units
    int lastLoadedWeek;             // last loaded week for performance issues (if loadMasterlist is called with the same lastWeek, it's skipped)
    Map<String, List> unitsByTarget; // units sorted by Target to make it easier to access the units
    Map<String, Map> unitsByUUID;    // units sorted by UUID to make it easier to access directly the unit

    public static OC_DiagnosticsManager sharedManager()
    {
        if (sharedDiagnosticsManager == null)
        {
            sharedDiagnosticsManager = new OC_DiagnosticsManager();
        }
        return sharedDiagnosticsManager;
    }


    public OC_DiagnosticsManager()
    {
        wordComponents = LoadWordComponentsXML(true);
        try
        {
            populateEvents();
        }
        catch (Exception e)
        {
            MainActivity.log("OC_DiagnosticsManager. Exception caught " + e.toString());
            e.printStackTrace();
        }
    }


    public void populateEvents() throws Exception
    {
        allEvents = new ArrayMap();
        OBXMLManager xmlManager = new OBXMLManager();
        String path = OBUtils.getConfigFile("exercises.xml");
        InputStream is = OBUtils.getInputStreamForPath(path);
        List<OBXMLNode> nodeList = xmlManager.parseFile(is);
        OBXMLNode rootNode = nodeList.get(0);
        for (OBXMLNode exerciseNode : rootNode.children)
        {
            Map<String, Object> attributes = new ArrayMap<>();
            attributes.putAll(exerciseNode.attributes);

            String exerciseUUID = (String) attributes.get("id");
            String classFilterString = (String) attributes.get(kClassFilter);
            if (classFilterString != null)
            {
                attributes.put(kClassFilter, classFilterString.split(","));
            }
            String parameterFilterString = (String) attributes.get(kParameterFilter);
            if (parameterFilterString != null)
            {
                attributes.put(kParameterFilter, parameterFilterString.split(","));
            }
            List<List<Object>> remedialUnits = new ArrayList<>();
            OBXMLNode remedialUnitsNode = exerciseNode.childrenOfType("remedialUnits").get(0);
            for (OBXMLNode remedialUnitNode : remedialUnitsNode.children)
            {
                String target = remedialUnitNode.attributeStringValue("target");
                String parameterString = remedialUnitNode.attributeStringValue("parameters");
                List<String> parameters = new ArrayList<>();
                if (parameterString.length() > 0)
                {
                    parameters.addAll(Arrays.asList(parameterString.split(",")));
                }
                String prefix = remedialUnitNode.attributeStringValue("prefix");
                String filterString = remedialUnitNode.attributeStringValue("filter");
                List<String> filter = new ArrayList<>();
                if (filterString.length() > 0)
                {
                    filter.addAll(Arrays.asList(filterString.split("/")));
                }
                List<Object> remedialUnitFilter = new ArrayList<>();
                remedialUnitFilter.add(target);
                remedialUnitFilter.add(parameters);
                remedialUnitFilter.add(prefix);
                remedialUnitFilter.add(filter);
                remedialUnits.add(remedialUnitFilter);
            }
            attributes.put(kRemedialUnits, remedialUnits);
            List scenariosNodeArray = exerciseNode.childrenOfType("scenarios");
            if (scenariosNodeArray != null && scenariosNodeArray.size() > 0)
            {
                Map<String, List<String>> scenarios = new ArrayMap<>();
                OBXMLNode scenariosNode = (OBXMLNode) scenariosNodeArray.get(0);
                for (OBXMLNode scenarioNode : scenariosNode.children)
                {
                    String scenarioUUID = scenarioNode.attributeStringValue("id");
                    String answerString = scenarioNode.attributeStringValue("answer");
                    scenarios.put(scenarioUUID, Arrays.asList(answerString.split(",")));
                }
                attributes.put(kScenarios, scenarios);
            }
            allEvents.put(exerciseUUID, attributes);
        }
    }


    public void resetDiagnostics(int totalQuestions, int week, String parameters, List events, boolean debugValue)
    {
        remedialUnits_day1 = new ArrayList<>();
        remedialUnits_day2 = new ArrayList<>();
        progress = new ArrayList<>();
        thresholdWeek = week;
        currentQuestionIndex = 0;
        startingParameters = parameters;
        debugEnabled = debugValue;
        idleTimeout = 18;
        maxWrongAnswers = 3;
        unitsFromMasterlist = loadMasterlistFromDB(thresholdWeek);
        fixedEvents = events;
        //
        if (fixedEvents != null)
        {
            questionEvents = fixedEvents;
        }
        else
        {
            questionEvents = generateRandomEvents(totalQuestions);
        }
    }



    public List loadMasterlistFromDB(int lastWeek)
    {
        if (lastLoadedWeek == lastWeek && unitsFromMasterlist != null)
        {
            return unitsFromMasterlist;
        }
        //
        lastLoadedWeek = lastWeek;
        //
        unitsByTarget = new HashMap<>();
        unitsByUUID = new HashMap<>();
        OCM_FatController fatController = (OCM_FatController) MainActivity.mainActivity.fatController;
        //
        List<Map> result = new ArrayList<>();
        //
        List<OCM_MlUnit> units = fatController.getUnitsForWeek(1, lastWeek);
        //
        for (OCM_MlUnit unit : units)
        {
            String unitUUID = String.valueOf(unit.unitid);
            String unitIndex = String.valueOf(unit.unitIndex);
            //
            if (unitUUID.contains(".repeat"))
            {
                continue; // skip the repeat units to prevent duplication;
            }
            //
            Map<String, Object> unitAttributes = new ArrayMap<>();
            String target = unit.target;
            //
            unitAttributes.put("id", unitUUID);
            unitAttributes.put("index", unitIndex);
            unitAttributes.put("target", target);
            unitAttributes.put("config", unit.config);
            unitAttributes.put("params", unit.params);
            //
            result.add(unitAttributes);
            //
            List unitsList = unitsByTarget.get(target);
            if (unitsList == null)
            {
                unitsList = new ArrayList();
            }
            unitsList.add(unitAttributes);
            unitsByTarget.put(target, unitsList);
            //
            unitsByUUID.put(unitUUID, unitAttributes);
        }
        //
        return result;
    }



    public List loadMasterlist(int lastWeek)
    {
        if (lastLoadedWeek == lastWeek && unitsFromMasterlist != null)
        {
            return unitsFromMasterlist;
        }
        //
        lastLoadedWeek = lastWeek;
        //
        unitsByTarget = new HashMap<>();
        unitsByUUID = new HashMap<>();
        //
        String masterListFolder = OBConfigManager.sharedManager.getMasterlist();
        //
        if (masterListFolder == null || masterListFolder.length() == 0)
        {
            return new ArrayList<>();
        }
        String masterList = String.format("masterlists/%s/units.xml", masterListFolder);
        InputStream xmlPath = OBUtils.getInputStreamForPath(masterList);
        //
        try
        {
            List<Map> result = new ArrayList<>();
            if (xmlPath != null)
            {
                OBXMLManager xmlManager = new OBXMLManager();
                OBXMLNode rootNode = xmlManager.parseFile(xmlPath).get(0);
                if (rootNode != null)
                {
                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        int level = levelNode.attributeIntValue("id");
                        //
                        if (lastWeek > 0 && level >= lastWeek) continue;
                        //
                        List<OBXMLNode> nodes = levelNode.childrenOfType("unit");
                        for (OBXMLNode unitNode : nodes)
                        {
                            String unitUUID = unitNode.attributeStringValue("id");
                            //
                            if (unitUUID.contains(".repeat"))
                            {
                                continue; // skip the repeat units to prevent duplication;
                            }
                            //
                            Map<String, Object> unitAttributes = new ArrayMap<>();
                            String target = unitNode.attributeStringValue("target");
                            //
                            unitAttributes.put("id", unitUUID);
                            unitAttributes.put("target", target);
                            unitAttributes.put("config", unitNode.attributeStringValue("config"));
                            unitAttributes.put("params", unitNode.attributeStringValue("params"));
                            //
                            result.add(unitAttributes);
                            //
                            List unitsList = unitsByTarget.get(target);
                            if (unitsList == null)
                            {
                                unitsList = new ArrayList();
                            }
                            unitsList.add(unitAttributes);
                            unitsByTarget.put(target, unitsList);
                            //
                            unitsByUUID.put(unitUUID, unitAttributes);
                        }
                    }
                }
                else
                {
                    MainActivity.log("OC_Diagnostics: loadMasterlist: [%s]  root node not found", masterListFolder);
                }
            }
            else
            {
                MainActivity.log("OC_Diagnostics: loadMasterlist: [%s] file not found", masterListFolder);
            }
            return result;
        }
        catch (Exception e)
        {
            MainActivity.log("OC_DiagnosticsManager. Exception caught " + e.toString());
            e.printStackTrace();
        }
        return null;
    }


    public List generateRandomEvents(int totalQuestions)
    {
        List firstBlock = OBUtils.randomlySortedArray(Arrays.asList("a", "b", "c", "d", "e"));
        List secondBlock = Arrays.asList("f");
        List thirdBlock = OBUtils.randomlySortedArray(Arrays.asList("g", "h", "i", "j", "k", "l", "m", "o", "p")).subList(0, 4);
        //
        List mergeBlock = new ArrayList();
        mergeBlock.addAll(secondBlock);
        mergeBlock.addAll(thirdBlock);
        mergeBlock = OBUtils.randomlySortedArray(mergeBlock);
        //
        List selectedEvents = new ArrayList();
        selectedEvents.addAll(firstBlock);
        selectedEvents.addAll(mergeBlock);
        //
        MainActivity.log("Selected events for Diagnostics: %s", selectedEvents.toString());
        return selectedEvents;
    }

    public List<String> getParameterPairsFromUnit(Map unit)
    {
        List<String> pairs = (List<String>) unit.get("parameterPairs");
        if (pairs == null)
        {
            String parameterString = (String) unit.get("params");
            pairs = Arrays.asList(parameterString.split("/"));
            unit.put("parameterPairs", pairs);
        }
        return pairs;
    }


    public Map<String, List> getParametersFromUnit(Map unit)
    {
        Map<String, List> parameters = (Map<String, List>) unit.get("parameters");
        if (parameters == null)
        {
            parameters = new HashMap<>();
            //
            List<String> pairs = getParameterPairsFromUnit(unit);
            for (String pair : pairs)
            {
                List<String> split = Arrays.asList(pair.split("="));
                if (split.size() == 1)
                {
                    parameters.put(split.get(0), new ArrayList());
                }
                else if (split.size() == 2)
                {
                    String key = split.get(0);
                    String valueString = split.get(1);
                    //
                    List<String> values = Arrays.asList(valueString.split(";|,| "));
                    parameters.put(key, values);
                }
            }
            //
            unit.put("parameters", parameters);
        }
        return parameters;
    }


    public List<String> extractValuesFromParameters(List<String> parameterNames, Map unit, String valuePrefix)
    {
        List result = new ArrayList<>();
        //
        Map<String, List> parameters = getParametersFromUnit(unit);
        //
        for (String parameterName : parameterNames)
        {
            List<String> values = parameters.get(parameterName);
            if (values == null) continue;
            //
            for (String value : values)
            {
                if (valuePrefix != null && valuePrefix.length() > 0 && !value.startsWith(valuePrefix))
                {
                    continue;
                }
                result.add(value);
            }
        }
        return new ArrayList<>(new HashSet<String>(result));
    }


    public void markQuestion(String eventUUID, boolean value, List parameters)
    {
        if (eventUUID == null) return;
        //
        MainActivity.log("OC_DiagnosticsManager --> markQuestionWithValueAndRelevantParameters --> [%s]", value ? "CORRECT" : "WRONG");
        progress.add(value);
        currentQuestionIndex++;
        if (!value)
        {
            List<String> units = retrieveRemedialUnitsForEvent(eventUUID, parameters);
            for (int i = 0; i < 5; i++)
            {
                String unit = units.get(i);
                if (unit.equals(""))
                {
                    MainActivity.log("ERROR --> unable find a remedial unit for " + eventUUID + " " + i);
                }
                else
                {
                    remedialUnits_day1.add(unit);
                }
            }
            for (int i = 5; i < 10; i++)
            {
                String unit = units.get(i);
                if (unit.equals(""))
                {
                    MainActivity.log("ERROR --> unable find a remedial unit for " + eventUUID + " " + i);
                }
                else
                {
                    remedialUnits_day2.add(unit);
                }
            }
        }
        loadCurrentQuestion();
    }


    public void loadCurrentQuestion()
    {
        final String appCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
        OBSectionController currentController = MainViewController().viewControllers.get(MainViewController().viewControllers.size() - 1);
        currentController._aborting = true;
        MainActivity.log("OC_DiagnosticsManager --> marking currentController as aborting");
        //
        if (questionEvents.size() <= currentQuestionIndex || WrongAnswers() >= maxWrongAnswers)
        {
            final String finalParameters = String.format("%s/end=true", startingParameters);
            if (fixedEvents != null)
            {
                finalParameters.concat(String.format("/events=%s", TextUtils.join(",", fixedEvents)));
            }
            MainActivity.log("OC_DiagnosticsManager --> loadCurrentQuestion --> Reached the end of the questions");
            //
            OCM_FatController fatController = (OCM_FatController) MainActivity.mainActivity.fatController;
            List<Integer> unitUUIDs = new ArrayList();
            for (String uuid : remedialUnits_day1)
            {
                unitUUIDs.add(Integer.parseInt(uuid));
            }
            for (String uuid : remedialUnits_day2)
            {
                unitUUIDs.add(Integer.parseInt(uuid));
            }
            fatController.saveExtraUnitsForCurrentUser(unitUUIDs);
            //
            OBUtils.runOnOtherThreadDelayed(0.1f, new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    OBUtils.runOnMainThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            if (debugEnabled)
                            {
                                if (WrongAnswers() > 0)
                                {
                                    MainViewController().pushViewControllerWithNameConfig("OC_DiagnosticsDebug", appCode, false, false, finalParameters, true);
                                }
                                else
                                {
                                    MainViewController().popViewController();
                                }
                            }
                            else
                            {
                                MainViewController().pushViewControllerWithNameConfig("OC_DiagnosticsIntro", appCode, false, false, finalParameters, true);
                            }
                        }
                    });
                }
            });
        }
        else
        {
            Map eventParameters = parametersForEvent(CurrentEvent());
            final String eventTarget = (String) eventParameters.get(kTarget);
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    MainViewController().pushViewControllerWithNameConfig(eventTarget, appCode, false, false, startingParameters, true);
                }
            });
        }
    }


    public List RemedialUnits()
    {
        List result = new ArrayList();
        result.add(remedialUnits_day1);
        result.add(remedialUnits_day2);
        //
        return result;
    }


    public List Progress()
    {
        return progress;
    }


    public List AvailableUnits()
    {
        return unitsFromMasterlist;
    }

    public Map<String, Object> getUnitAttributes(String unitUUID)
    {
        return unitsByUUID.get(unitUUID);
    }


    public Map WordComponents()
    {
        return wordComponents;
    }


    public int TotalQuestions()
    {
        return questionEvents.size();
    }


    public List QuestionEvents()
    {
        return questionEvents;
    }


    public int WrongAnswers()
    {
        int result = 0;
        for (Boolean value : progress)
        {
            if (!value)
            {
                result++;
            }
        }
        return result;
    }


    public int IdleTimeout()
    {
        return idleTimeout;
    }


    public int CurrentQuestion()
    {
        return currentQuestionIndex;
    }


    public String CurrentEvent()
    {
        if (questionEvents == null || questionEvents.size() <= currentQuestionIndex) return null;
        //
        String eventUUID = (String) questionEvents.get(currentQuestionIndex);
        return eventUUID;
    }


    public Map parametersForEvent(String eventUUID)
    {
        Map parameters = allEvents.get(eventUUID);
        return parameters;
    }


    public String backgroundForEvent(String eventUUID)
    {
        Map parameters = parametersForEvent(eventUUID);
        return (String) parameters.get(kBackground);
    }


    public String layoutForEvent(String eventUUID)
    {
        Map parameters = parametersForEvent(eventUUID);
        return (String) parameters.get(kLayout);
    }


    public List availableUnitsForEvent(String eventUUID)
    {
        Map parameters = parametersForEvent(eventUUID);
        List classFilter = Arrays.asList((String[]) parameters.get(kClassFilter));
        //
        if (classFilter == null)
            return null;               // no filter required, the parameters are generated by the unit.;
        //
        List filteredUnits = new ArrayList();
        for (Map unitAttributes : unitsFromMasterlist)
        {
            String target = ((String) unitAttributes.get(kTarget)).toLowerCase();
            if (classFilter.contains(target))
            {
                filteredUnits.add(unitAttributes);
            }
        }
        //
        return filteredUnits;
    }


    public Map unitsPerParameterForEvent(String eventUUID)
    {
        List<Map> availableUnits = availableUnitsForEvent(eventUUID);
        Map eventParameters = parametersForEvent(eventUUID);
        List unitParameters = Arrays.asList((String[]) eventParameters.get(kParameterFilter));
        String parameterPrefix = (String) eventParameters.get(kParameterPrefix);
        Map<String, List> unitsUsedForParameter = new ArrayMap<>();
        //
        for (Map unit : availableUnits)
        {
            List<String> values = extractValuesFromParameters(unitParameters, unit, parameterPrefix);
            if (values != null && values.size() > 0)
            {
                for (String value : values)
                {
                    List units = unitsUsedForParameter.get(value);
                    if (units == null)
                    {
                        units = new ArrayList<>();
                    }
                    //
                    if (units.contains(unit)) continue;
                    //
                    units.add(unit);
                    unitsUsedForParameter.put(value, units);
                }
            }
        }
        return unitsUsedForParameter;
    }


    public List unitsWithTarget(String target, List parameterNames, String parameterPrefix, List<String> parameterConditions)
    {
        List filteredUnits = new ArrayList();
        //
        for (Map unitAttributes : unitsFromMasterlist)
        {
            String unitTarget = ((String) unitAttributes.get(kTarget)).toLowerCase();
            if (!unitTarget.equalsIgnoreCase(target.toLowerCase()))
            {
                continue;
            }
            //
            if (parameterConditions.size() > 0)
            {
                int conditionsMet = 0;
                List<String> pairs = getParameterPairsFromUnit(unitAttributes);
                //
                for (String condition : parameterConditions)
                {
                    if (pairs.contains(condition))
                    {
                        conditionsMet++;
                    }
                }
                //
                if (conditionsMet != parameterConditions.size())
                {
                    continue;      // this unit didnt match the necessary conditions for the filter;
                }
            }
            //
            if (parameterNames.size() > 0)
            {
                List values = extractValuesFromParameters(parameterNames, unitAttributes, parameterPrefix);
                //
                if (values == null || values.size() == 0) continue;
            }
            //
            filteredUnits.add(unitAttributes);
        }
        //
        return filteredUnits;
    }


    public List retrieveRemedialUnitsForEvent(String eventUUID, List<String> relevantParameters)
    {
        //unitsFromMasterlist = loadMasterlistFromDB(-1);
        //
        Map exerciseData = parametersForEvent(eventUUID);
        List<List> remedialUnitsTemplate = (List) exerciseData.get(kRemedialUnits);
        List<List> remedialUnitsCollection = new ArrayList<>();
        //
        for (List remedialUnitsData : remedialUnitsTemplate)
        {
            String target = (String) remedialUnitsData.get(0);
            List parameters = (List) remedialUnitsData.get(1);
            String parameterFilter = (String) remedialUnitsData.get(2);
            List parameterConditions = (List) remedialUnitsData.get(3);
            List relevantUnits = new ArrayList<>();
            List possibleUnits = new ArrayList<>();
            //
            List<Map> result = unitsWithTarget(target, parameters, parameterFilter, parameterConditions);
            //
            for (Map unit : result)
            {
                if (relevantParameters != null)
                {
                    boolean unitWasRelevant = false;
                    //
                    List extractedValues = extractValuesFromParameters(parameters, unit, parameterFilter);
                    //
                    for (String value : relevantParameters)
                    {
                        List<String> possibleValues = Arrays.asList(value, String.format("is_%s", value), String.format("isyl_%s", value), String.format("fc_%s", value), String.format("nw_%s", value));
                        //
                        for (String possibleValue : possibleValues)
                        {
                            if (extractedValues.contains(possibleValue))
                            {
                                unitWasRelevant = true;
                                relevantUnits.add(unit.get("id"));
                                break;
                            }
                        }
                        if (unitWasRelevant) break;     // no more work to be done here;
                    }
                    if (!unitWasRelevant)
                    {
                        possibleUnits.add(unit.get("id"));
                    }
                }
                else
                {
                    possibleUnits.add(unit.get("id"));
                }
            }
            if (relevantUnits.size() == 0)
            {
                remedialUnitsCollection.add(OBUtils.randomlySortedArray(possibleUnits));
            }
            else
            {
                remedialUnitsCollection.add(OBUtils.randomlySortedArray(relevantUnits));
            }
        }
        //
        List<String> remedialUnits = new ArrayList<>();
        for (int i = 0; i < remedialUnitsTemplate.size(); i++)
        {
            remedialUnits.add("");
        }
        //
        List<List> sortedRemedialUnitCollection = new ArrayList();
        sortedRemedialUnitCollection.addAll(remedialUnitsCollection);
        //
        Arrays.sort(remedialUnitsCollection.toArray(), new Comparator<Object>()
        {
            @Override
            public int compare(Object lhs, Object rhs)
            {
                return ((List) lhs).size() - ((List) rhs).size();
            }
        });
        //
        List usedUnits = new ArrayList<>();
        for (List<String> unitCollection : sortedRemedialUnitCollection)
        {
            if (unitCollection.size() == 0)
            {
                //MainActivity.log("OC_DiagnosticsManager.retrieveRemedialUnitsForEvent:withRelevantParameters --> unit collection is empty!");
                continue;
            }
            //
            List<Integer> possibleIndexes = new ArrayList<>();
            for (int i = 0; i < remedialUnitsCollection.size(); i++)
            {
                List units = remedialUnitsCollection.get(i);
                if (units.equals(unitCollection))
                {
                    possibleIndexes.add(i);
                }
            }
            //
            for (int index : possibleIndexes)
            {
                for (String unit : unitCollection)
                {
                    if (!usedUnits.contains(unit) && remedialUnits.get(index).equals(""))
                    {
                        remedialUnits.set(index, unit);
                        usedUnits.add(unit);
                        break;
                    }
                }
            }
            //
            for (int index : possibleIndexes)
            {
                if (remedialUnits.get(index).equals(""))
                {
                    MainActivity.log("WARNING --> added remedial unit as a fail safe --> " + unitCollection.get(0));
                    remedialUnits.set(index, unitCollection.get(0));
                }
            }
        }
        //
//        MainActivity.log("Remedial Units calculated for event %s", eventUUID);
//        int index = 1;
//        for (String unit : remedialUnits)
//        {
//            MainActivity.log("%2d: %s", index, unit);
//            index++;
//        }
        return remedialUnits;
    }
}
