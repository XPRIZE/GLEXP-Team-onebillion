package org.onebillion.onecourse.mainui.oc_countwhatyousee;

import android.util.ArrayMap;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 15/01/2018.
 */

public class OC_Cwys extends OC_SectionController
{
    public List<OBGroup> currentBlocks;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
    }

    public void loadUnitsForNumber(int number)
    {
        if(currentBlocks != null)
        {
            for(OBGroup unitGroup  : currentBlocks)
            {
                detachControl(unitGroup);

            }

        }
        currentBlocks = new ArrayList<>();
        OBControl unit = objectDict.get("unit");
        float blockDist = unit.width()*0.5f;
        int blockCount = (int)Math.ceil(number/100.0);
        OBGroup lastBlock = null;
        for(int i=0; i<blockCount; i++)
        {
            boolean isLast = i==(blockCount-1) && number%100 != 0;
            OBGroup unitBlock = createBlockForUnit((OBPath)unit, isLast?(number%100):100 ,isLast);
            if(lastBlock != null)
            {
                unitBlock.setLeft(lastBlock.right() + blockDist);
            }
            else
            {
                unitBlock.setLeft(0);
            }
            lastBlock = unitBlock;
            currentBlocks.add(unitBlock);
        }
        float lastRight = currentBlocks.get(currentBlocks.size()-1).right();
        float centre = OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("work_rect") .frame()).x;
        float targLeft = centre - (0.5f*lastRight);
        for(int i=0; i<currentBlocks.size(); i++)
        {
            OBGroup group = currentBlocks.get(i);
            group.setLeft(group.left() + targLeft);
        }
    }

    public OBGroup createBlockForUnit(OBPath unit, int val, boolean spread)
    {
        float columnDist = unit.width()*0.5f;
        float bottom = objectDict.get("work_rect").bottom();
        Map<String,OBControl> columnDict = new ArrayMap<>();
        OBGroup lastColumn = null;
        for(int i=0; i<Math.ceil(val/10.0); i++)
        {
            Map<String,OBControl> unitDict = new ArrayMap<>();
            OBControl lastUnit = null;
            for(int j=0; j<10; j++)
            {
                OBPath unitCopy = (OBPath)unit.copy();
                unitCopy.setZPosition(1+(i*10)+j);
                if(lastUnit != null)
                {
                    unitCopy.setBottom(lastUnit.top()+unit.lineWidth());
                }
                else
                {
                    unitCopy.setBottom(bottom);
                }
                lastUnit = unitCopy;
                if(((i*10) +j) <val)
                    unitCopy.enable();
                else
                    unitCopy.disable();
                unitCopy.hide();
                unitDict.put(String.format("unit_%d",j+1),unitCopy);
            }
            OBGroup column = new OBGroup(new ArrayList<>(unitDict.values()));
            column.objectDict = unitDict;
            if(lastColumn != null)
            {
                column.setLeft(lastColumn.right() +(spread ? columnDist : -unit.lineWidth()));
            }
            lastColumn = column;
            column.setZPosition(i+1);
            columnDict.put(String.format("column_%d",i+1),column);
        }
        OBGroup completeBlock = new OBGroup(new ArrayList<>(columnDict.values()));
        completeBlock.objectDict = columnDict;
        attachControl(completeBlock);
        return completeBlock;
    }

    public void showUnitsForColumn(OBGroup column)
    {
        for(OBControl con : column.filterMembers("unit_.*"))
            if(con.isEnabled())
                con.show();
    }

    public void showUnitsForBlock(OBGroup block)
    {
        for(OBControl column : block.filterMembers("column_.*"))
            showUnitsForColumn((OBGroup)column);
    }

    public void highlightUnit(OBControl con,boolean on)
    {
        con.setOpacity(on ? 0.6f : 1.0f);
    }

    public void highlightColumn(OBGroup column,boolean on)
    {
        for(OBControl con : column.filterMembers("unit_.*"))
            if(con.isEnabled())
                highlightUnit(con,on);
    }

    public void highlightBlock(OBGroup block,boolean on)
    {
        for(OBControl column : block.filterMembers("column_.*"))
            highlightColumn((OBGroup)column,on);
    }

    public String eventNameForPrefix(String prefix,int num,boolean last)
    {
        String eventName = null;
        if(last)
        {
            eventName = String.format("%slast",prefix);
        }
        if(!last || audioScenes.get(eventName) == null)
        {
            eventName = String.format("%s%d",prefix,num);
        }
        if(audioScenes.get(eventName) != null)
        {
            return eventName;
        }
        return String.format("%sdefault",prefix);
    }



}
