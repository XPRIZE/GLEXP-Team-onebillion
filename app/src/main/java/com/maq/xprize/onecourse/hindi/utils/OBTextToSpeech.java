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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/* TEXT TO SPEECH IMPLEMENTATION
/* This class defines a text-to-speech object which is used to play audio from
 * the Hindi transcripts, thus removing the dependency on audio files. */

public class OBTextToSpeech {

    private TextToSpeech textToSpeech;
    private AudioManager audioManager;
    private static int state;
    public static OBTextToSpeech obTextToSpeech;
    private static final int OBAP_IDLE = 0,
            OBAP_PREPARING = 1,
            OBAP_PLAYING = 2,
            OBAP_FINISHED = 3;

    OBTextToSpeech(final Context context) {
        setState(OBAP_IDLE);
        // initializes AudioManager object which is used to detect whether any sound is being played from the device
        audioManager = (AudioManager) MainActivity.mainActivity.getSystemService(context.AUDIO_SERVICE);
        // initializes the TextToSpeech object which generates the audio
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // sets output language as Hindi
                    int ttsLang = textToSpeech.setLanguage(Locale.forLanguageTag("hin"));
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.e("TTS", "The language is not supported");
                    else
                        Log.i("TTS", "Initialization success!");
                    // creates UtteranceProgressListener which checks for any utterance when the audio is being synthesized
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Log.i(utteranceId, "Audio started playing ...");
                            setState(OBAP_PREPARING);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.i(utteranceId, "Audio finished playing");
                            setState(OBAP_FINISHED);
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.e(utteranceId, "Error in playing audio");
                        }
                    });
                }
                else
                    Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
        obTextToSpeech = this;
    }

    private void setState(int st) {
        state = st;
    }

    public int getState() {
        return state;
    }

    public boolean playAudio(AssetFileDescriptor fd) {
        synchronized (this) {
            try {
                FileInputStream f = fd.createInputStream();
                InputStreamReader i = new InputStreamReader(f, StandardCharsets.UTF_16LE);
                BufferedReader b = new BufferedReader(i);
                String data = b.readLine();
                // generates audio
                setState(OBAP_PLAYING);
                int speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, "TTS");
                // this loop ensures that the audio has completed playing to prevent sound overlapping
                while (textToSpeech.isSpeaking());
                return (speechStatus != TextToSpeech.ERROR);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean isPreparing() {
        return getState() == OBAP_PREPARING;
    }

    public boolean isPlaying() {
        return audioManager.isMusicActive() || (getState() == OBAP_PLAYING && textToSpeech.isSpeaking());
    }

    public boolean isDone() {
        return getState() == OBAP_FINISHED;
    }

    public void stopAudio() {
        if (isPlaying())
            textToSpeech.stop();
    }

}
