package org.onebillion.onecourse.utils;

import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;

import org.onebillion.onecourse.controls.OBLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 02/06/16.
 */
public class OBReadingWord
{
    public static int WORD_SPEAKABLE=1,
    WORD_CAN_BREAK=2;
    public String text,audio;
    public List<String> syllables,sounds;
    public int flags,index;
    public double timeStart,timeEnd,slowTimeStart,slowTimeEnd;
    public OBLabel label;
    public PointF homePosition;
    public RectF frame;
    public int paraNo;
    public String filePath;
    public String imageName;
    public Map<String,Object> settings = new HashMap<>();

    public static String TextByRemovingSlashes(String s)
    {
        String[] l = s.split("/");
        return TextUtils.join("",l);
    }

    public static OBReadingWord wordFromString(String str,int st,int en,int fl,int pno)
    {
        OBReadingWord w = new OBReadingWord();
        String s = str.substring(st,en);
        if (s.contains("/"))
        {
            w.syllables = Arrays.asList(s.split("/"));
            w.text = TextByRemovingSlashes(s);
        }
        else
        {
            w.text = s;
        }
        w.flags = fl;
        w.index = st;
        w.paraNo = pno;
        return w;
    }

    static boolean IsApostrophe(char ch)
    {
        return (ch == '\'' || ch == 0x2019);
    }

    static boolean IsSpace(char ch)
    {
        return ch <= ' ';
    }

    public static boolean isWordCharacter(char ch)
    {
        if (Character.isLetter(ch))
            return true;
        if (Character.isDigit(ch))
            return true;
        if ("- /_".indexOf(ch) >= 0)
            return true;
        return false;
    }
    static OBReadingWord GetWordFromString(String str,int[] ia,boolean betweenQuotes,int pno)
    {
        int startidx = ia[0];
        int i = startidx;
        while (i < str.length())
        {
            while (i < str.length() && isWordCharacter(str.charAt(i)))
                i++;
            if (i == str.length())
            {
                ia[0] = i;
                return OBReadingWord.wordFromString(str, startidx, i, WORD_SPEAKABLE, pno);
            }
            if (IsApostrophe(str.charAt(i)))
            {
                if (i + 1 < str.length() && isWordCharacter(str.charAt(i + 1)))
                {
                    i++;
                    continue;
                }
            }
            ia[0] = i;
            return OBReadingWord.wordFromString(str, startidx, i, WORD_SPEAKABLE, pno);
        }
        ia[0] = i;
        return null;
    }
    static List<OBReadingWord> WordsFromString(String str,int pno)
    {
        List<OBReadingWord> words = new ArrayList<>();
        int startidx = 0,i = 0;
        boolean betweenQuotes = false;
        while (i < str.length())
        {
            while (i < str.length() && IsSpace(str.charAt(i)))
                i++;
            if (i > startidx)
            {
                words.add(OBReadingWord.wordFromString(str,startidx,i,WORD_CAN_BREAK,pno));
                startidx = i;
            }
            if (i < str.length() && isWordCharacter(str.charAt(i)))
            {
                int[] ia = {0};
                ia[0] = i;
                words.add(GetWordFromString(str, ia, betweenQuotes,pno));
                i = ia[0];
                startidx = i;
            }
            else
            {

                while (i < str.length() && !isWordCharacter(str.charAt(i)) && !IsSpace(str.charAt(i)))
                    i++;
                if (i > startidx)
                {
                    int flag = WORD_CAN_BREAK;
                    String substr = str.substring(startidx,i);
                    String sarr[] = {"“","‘"};
                    for (String ch : sarr)
                        if (substr.contains(ch))
                        {
                            flag = 0;
                            break;
                        }
                    words.add(OBReadingWord.wordFromString(str,startidx,i,flag,pno));
                    startidx = i;
                }
            }
        }

        return words;
    }

}
