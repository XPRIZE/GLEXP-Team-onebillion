package com.maq.xprize.onecourse.hindi.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class OBTextToSpeech extends Application {

    private TextToSpeech tts;
    private AudioManager am;
    private static boolean playBack;

    OBTextToSpeech(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang =tts.setLanguage(Locale.forLanguageTag("hin"));
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.e("TTS", "The language is not supported");
                    else
                        Log.i("TTS", "Initialization success!");
                }
                else
                    Toast.makeText(getApplicationContext(), "TTS initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected Boolean playAudio(AssetFileDescriptor fd) {
        try {
            FileInputStream f = fd.createInputStream();
            InputStreamReader i = new InputStreamReader(f, StandardCharsets.UTF_16LE);
            BufferedReader b = new BufferedReader(i);
            String data = b.readLine();
            int speechStatus = tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, null);
            return (speechStatus != TextToSpeech.ERROR);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
