package org.onebillion.onecourse.mainui.oc_playzone;

import org.onebillion.onecourse.controls.OBGroup;

public class OC_PlayZoneTrace1 extends OC_PlayZoneTrace
{
    public void demoa() throws Exception
    {
        waitForSecs(0.5f);
        highlightAndPlay(currNo);
        waitForSecs(0.4f);
        nextScene();
    }

    public void setScenea()
    {
        loadLetters();
        hideControls("letterrect.*");
        for(int i = 1;i < groupList.size();i++)
        {
            setLetterDashed(i,true);
        }
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            goToCard(OC_PlayZoneTrace2.class,(String)params,false);
        }
        catch(Exception e)
        {
        }
    }

    public void setSceneb()
    {

    }

    public void doMainb()
    {
        try
        {
            nextLetter();
        }
        catch(Exception e)
        {
            
        }
    }

    public void setSceneremaining()
    {

    }

    public void demoremaining() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //playAudioQueuedScene("DEMO",true);
        nextScene();
    }

    public void replayAudio()
    {
        if(busyStatuses.contains(status()))
            return;
        setStatus(status());
        playLetterSound(letter);

    }


    public void loadLetters()
    {
        for(int i = 0;i < letter.length();i++)
        {
            for(String n : filterControlsIDs("Path.*|xbox"))
                objectDict.remove(n);
            String character = letter.substring(i, i + 1);
            String l = "_" + (character);
            for(int ii = 0;ii < 8;ii++)
            {
                OBGroup g = letterGroup(l,String.format("letterrect%d",ii));
                groupList.add(g);
                attachControl(g);
                hideControls("xbox.*");
            }
        }
        if(groupList.get(1).left() - groupList.get(0).right() < spacerwidth)
        {
            repositionLetters();
        }
    }

    public void repositionLetters()
    {
        int objsPerRow = 4;
        float w = groupList.get(0).width();
        float marginw = (bounds() .width() - (objsPerRow * w +(objsPerRow - 1) * spacerwidth)) / 2;
        float l = marginw;
        for(int i = 0;i < 4;i++)
        {
            groupList.get(i).setLeft(l);
            groupList.get(i + 4).setLeft(l);
            l +=(groupList.get(i).width() + spacerwidth);
        }
    }

}
