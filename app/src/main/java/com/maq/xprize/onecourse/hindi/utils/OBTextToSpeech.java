package com.maq.xprize.onecourse.hindi.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.maq.xprize.onecourse.hindi.mainui.MainActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OBTextToSpeech {

    private TextToSpeech tts;
    private AudioManager am;
    private boolean isDone = false, isPreparing = false;
    private Lock audioLock = new ReentrantLock();
    private Condition condition;
    private int state;
    public static final int OBAP_IDLE = 0,
            OBAP_PREPARING = 1,
            OBAP_PLAYING = 2,
            OBAP_SEEKING = 3,
            OBAP_FINISHED = 4,
            OBAP_PAUSED = 5;

    OBTextToSpeech(final Context context) {
        am = (AudioManager) MainActivity.mainActivity.getSystemService(context.AUDIO_SERVICE);
        condition = audioLock.newCondition();
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = tts.setLanguage(Locale.forLanguageTag("hin"));
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.e("TTS", "The language is not supported");
                    else
                        Log.i("TTS", "Initialization success!");
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Log.i("TTS", utteranceId);
                            setState(OBAP_PREPARING);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.i("TTS", utteranceId);
                            setState(OBAP_FINISHED);
                            audioLock.lock();
                            condition.signalAll();
                            audioLock.unlock();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.e("TTS", utteranceId);
                        }
                    });
                }
                else
                    Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setState(int st) {
        state = st;
    }

    private int getState() {
        return state;
    }

    public boolean playAudio(AssetFileDescriptor fd) {
        try {
            FileInputStream f = fd.createInputStream();
            InputStreamReader i = new InputStreamReader(f, StandardCharsets.UTF_16LE);
            BufferedReader b = new BufferedReader(i);
            String data = b.readLine();
            setState(OBAP_PLAYING);
            int speechStatus = tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, null);
            return (speechStatus != TextToSpeech.ERROR);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPlaying() {
        return am.isMusicActive();
    }

    public boolean isDone() {
        return (getState() == OBAP_FINISHED);
    }

    public void stopAudio() {
        if (isPlaying())
            tts.stop();
        audioLock.lock();
        condition.signalAll();
        audioLock.unlock();
    }

    public void waitAudio() {
        if (!isPlaying())
            return;
        audioLock.lock();
        while (tts.isSpeaking() || isPlaying()) {
            try {
                condition.await();
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        audioLock.unlock();
    }
}
