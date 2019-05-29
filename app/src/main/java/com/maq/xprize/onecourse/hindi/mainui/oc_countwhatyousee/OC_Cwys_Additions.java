package com.maq.xprize.onecourse.hindi.mainui.oc_countwhatyousee;

import android.content.Loader;
import android.graphics.PointF;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 06/06/2018.
 */

public class OC_Cwys_Additions {

    static public OBGroup loadUnitsBlockForNumber(int number, OBControl unit, float blockDist, float centre, float bottom, OC_SectionController controller)
    {
        List<OBControl> blocksArray = new ArrayList<>();
        int blockCount = (int)Math.ceil(number/100.0);

        Map<String,OBControl> dict = new ArrayMap<>();
        OBGroup lastBlock = null;
        for(int i=0; i<blockCount; i++)
        {
            boolean isLast = i==(blockCount-1) && number%100 != 0;
            OBGroup unitBlock = createBlockForUnit((OBPath)unit, isLast?(number%100):100, bottom ,isLast);
            if(lastBlock != null)
            {
                unitBlock.setLeft(lastBlock.right() + blockDist);
            }
            else
            {
                unitBlock.setLeft(0);
            }
            lastBlock = unitBlock;
            blocksArray.add(unitBlock);
            dict.put(String.format("block_%d",i+1), unitBlock);
        }

        OBGroup blocksGroup = new OBGroup(blocksArray);
        PointF loc = OBMisc.copyPoint(blocksGroup.position());
        loc.x = centre;
        blocksGroup.setPosition(loc);
        controller.attachControl(blocksGroup);
        blocksGroup.objectDict = dict;
        blocksGroup.setProperty("num_val",number);
        return blocksGroup;
    }

    static public OBGroup createBlockForUnit(OBPath unit, int val, float bottom, boolean spread)
    {
        float columnDist = unit.width()*0.5f;
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
        return completeBlock;
    }

    static public void showUnitsForColumn(OBGroup column)
    {
        for(OBControl con : column.filterMembers("unit_.*"))
            if(con.isEnabled())
                con.show();
    }

    static public void showUnitsForBlock(OBGroup block)
    {
        for(OBControl column : block.filterMembers("column_.*"))
            showUnitsForColumn((OBGroup)column);
    }

    static public void highlightUnit(OBControl con,boolean on)
    {
        con.setOpacity(on ? 0.6f : 1.0f);
    }

    static public void highlightColumn(OBGroup column,boolean on)
    {
        for(OBControl con : column.filterMembers("unit_.*"))
            if(con.isEnabled())
                highlightUnit(con,on);
    }

    static public void highlightBlock(OBGroup block,boolean on)
    {
        for(OBControl column : block.filterMembers("column_.*"))
            highlightColumn((OBGroup)column,on);
    }

    static public List<OBGroup> getHundredBlocksFromBlockGroup(OBGroup blockGroup)
    {
        List<OBGroup> hundredBlocks = new ArrayList<>();
        int currentNum = (int)blockGroup.propertyValue("num_val");
        int count = blockGroup.objectDict.size();
        long blockCount = currentNum%100 == 0 ? count : count -1;
        for(int i=0; i<blockCount; i++)
        {
            hundredBlocks.add((OBGroup)blockGroup.objectDict.get(String.format("block_%d",i+1)));
        }
        return hundredBlocks;

    }

    static public List<OBGroup> getTenColumnsFromBlockGroup(OBGroup blockGroup)
    {
        List<OBGroup> tenColumns = new ArrayList<>();
        OBGroup lastBlock =(OBGroup)blockGroup.objectDict.get(String.format("block_%d",blockGroup.objectDict.size()));
        int currentNum = (int)blockGroup.propertyValue("num_val");
        if(currentNum%100 == 0)
            return tenColumns;
        long columnCount = currentNum%10 == 0 ? lastBlock.objectDict.size() : lastBlock.objectDict.size()-1;
        for(int i=0;
            i<columnCount;
            i++)
        {
            OBGroup column =(OBGroup)lastBlock.objectDict.get(String.format("column_%d",i+1));
            tenColumns.add(column);

        }
        return tenColumns;
    }

    static public List<OBControl> getUnitsFromBlockGroup(OBGroup blockGroup)
    {
        List<OBControl> units = new ArrayList<>();
        int currentNum = (int)blockGroup.propertyValue("num_val");
        if(currentNum%10 == 0)        return units;
        OBGroup lastBlock =(OBGroup)blockGroup.objectDict.get(String.format("block_%d",blockGroup.objectDict.size()));
        OBGroup lastColumn =(OBGroup)lastBlock.objectDict.get(String.format("column_%d",lastBlock.objectDict.size()));
        for(int i=0; i<10; i++)
        {
            OBControl unit = lastColumn.objectDict.get(String.format("unit_%d",i+1));
            if(unit.isEnabled())
            {
                units.add(unit);

            }
            else
            {
                break;

            }

        }
        return units;
    }


}
