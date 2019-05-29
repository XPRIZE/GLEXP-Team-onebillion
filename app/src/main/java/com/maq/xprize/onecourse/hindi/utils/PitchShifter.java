package com.maq.xprize.onecourse.hindi.utils;

/****************************************************************************
 *
 * NAME: PitchShifter.java
 * VERSION: 1.2
 * HOME URL: http://www.dspdimension.com
 * KNOWN BUGS: none
 *
 * SYNOPSIS: Routine for doing pitch shifting while maintaining
 * duration using the Short Time Fourier Transform.
 *
 * DESCRIPTION: The routine takes a pitchShift factor value which is between 0.5
 * (one octave down) and 2. (one octave up). A value of exactly 1 does not change
 * the pitch. numSampsToProcess tells the routine how many samples in indata[0...
 * numSampsToProcess-1] should be pitch shifted and moved to outdata[0 ...
 * numSampsToProcess-1]. The two buffers can be identical (ie. it can process the
 * data in-place). fftFrameSize defines the FFT frame size used for the
 * processing. Typical values are 1024, 2048 and 4096. It may be any value <=
 * MAX_FRAME_LENGTH but it MUST be a power of 2. osamp is the STFT
 * oversampling factor which also determines the overlap between adjacent STFT
 * frames. It should at least be 4 for moderate scaling ratios. A value of 32 is
 * recommended for best quality. sampleRate takes the sample rate for the signal
 * in unit Hz, ie. 44100 for 44.1 kHz audio. The data passed to the routine in
 * indata[] should be in the range [-1.0, 1.0), which is also the output range
 * for the data, make sure you scale the data accordingly (for 16bit signed integers
 * you would have to divide (and multiply) by 32768).
 *
 * COPYRIGHT 1999-2006 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
 *
 * 						The Wide Open License (WOL)
 *
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice and this license appear in all source copies.
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY OF
 * ANY KIND. See http://www.dspguru.com/wol.htm for more information.
 *
 *****************************************************************************/

public class PitchShifter {

    private static int MAX_FRAME_LENGTH = 8192;
    private  float[] gInFIFO = new float[MAX_FRAME_LENGTH];
    private  float[] gOutFIFO = new float[MAX_FRAME_LENGTH];
    private  float[] gFFTworksp = new float[2 * MAX_FRAME_LENGTH];
    private  float[] gLastPhase = new float[MAX_FRAME_LENGTH / 2 + 1];
    private  float[] gSumPhase = new float[MAX_FRAME_LENGTH / 2 + 1];
    private  float[] gOutputAccum = new float[2 * MAX_FRAME_LENGTH];
    private  float[] gAnaFreq = new float[MAX_FRAME_LENGTH];
    private  float[] gAnaMagn = new float[MAX_FRAME_LENGTH];
    private  float[] gSynFreq = new float[MAX_FRAME_LENGTH];
    private  float[] gSynMagn = new float[MAX_FRAME_LENGTH];
    private  int gRover, gInit;

    void clearArrays()
    {
        for (int i = 0;i < gInFIFO.length;i++)
            gInFIFO[i] = 0f;
        for (int i = 0;i < gOutFIFO.length;i++)
            gOutFIFO[i] = 0f;
        for (int i = 0;i < gFFTworksp.length;i++)
            gFFTworksp[i] = 0f;
        for (int i = 0;i < gLastPhase.length;i++)
            gLastPhase[i] = 0f;
        for (int i = 0;i < gSumPhase.length;i++)
            gSumPhase[i] = 0f;
        for (int i = 0;i < gOutputAccum.length;i++)
            gOutputAccum[i] = 0f;
        for (int i = 0;i < gAnaFreq.length;i++)
            gAnaFreq[i] = 0f;
        for (int i = 0;i < gAnaMagn.length;i++)
            gAnaMagn[i] = 0f;
        for (int i = 0;i < gSynFreq.length;i++)
            gSynFreq[i] = 0f;
        for (int i = 0;i < gSynMagn.length;i++)
            gSynMagn[i] = 0f;
    }
    public  void PitchShift(float pitchShift, int numSampsToProcess, int fftFrameSize,
                                  int osamp, float sampleRate, float[] indata)
    {
        double magn, phase, tmp, window, real, imag;
        double freqPerBin, expct;
        int i, k, qpd, index, inFifoLatency, stepSize, fftFrameSize2;

        clearArrays();
        float[] outdata = indata;
        /* set up some handy variables */
        fftFrameSize2 = fftFrameSize / 2;
        stepSize = fftFrameSize / osamp;
        freqPerBin = sampleRate / (double)fftFrameSize;
        expct = 2.0 * Math.PI * (double)stepSize / (double)fftFrameSize;
        inFifoLatency = fftFrameSize - stepSize;
        if (gRover == 0) gRover = inFifoLatency;


        /* main processing loop */
        for (i = 0; i < numSampsToProcess; i++)
        {

            /* As long as we have not yet collected enough data just read in */
            gInFIFO[gRover] = indata[i];
            outdata[i] = gOutFIFO[gRover - inFifoLatency];
            gRover++;

            /* now we have enough data for processing */
            if (gRover >= fftFrameSize)
            {
                gRover = inFifoLatency;

                /* do windowing and re,im interleave */
                for (k = 0; k < fftFrameSize; k++)
                {
                    window = -.5 * Math.cos(2.0 * Math.PI * (double)k / (double)fftFrameSize) + .5;
                    gFFTworksp[2 * k] = (float)(gInFIFO[k] * window);
                    gFFTworksp[2 * k + 1] = 0.0F;
                }


                /* ***************** ANALYSIS ******************* */
                /* do transform */
                ShortTimeFourierTransform(gFFTworksp, fftFrameSize, -1);

                /* this is the analysis step */
                for (k = 0; k <= fftFrameSize2; k++)
                {

                    /* de-interlace FFT buffer */
                    real = gFFTworksp[2 * k];
                    imag = gFFTworksp[2 * k + 1];

                    /* compute magnitude and phase */
                    magn = 2.0 * Math.sqrt(real * real + imag * imag);
                    phase = smbAtan2(imag, real);

                    /* compute phase difference */
                    tmp = phase - gLastPhase[k];
                    gLastPhase[k] = (float)phase;

                    /* subtract expected phase difference */
                    tmp -= (double)k * expct;

                    /* map delta phase into +/- Pi interval */
                    qpd = (int)(tmp / Math.PI);
                    if (qpd >= 0) qpd += qpd & 1;
                    else qpd -= qpd & 1;
                    tmp -= Math.PI * (double)qpd;

                    /* get deviation from bin frequency from the +/- Pi interval */
                    tmp = osamp * tmp / (2.0 * Math.PI);

                    /* compute the k-th partials' true frequency */
                    tmp = (double)k * freqPerBin + tmp * freqPerBin;

                    /* store magnitude and true frequency in analysis arrays */
                    gAnaMagn[k] = (float)magn;
                    gAnaFreq[k] = (float)tmp;

                }

                /* ***************** PROCESSING ******************* */
                /* this does the actual pitch shifting */
                for (int zero = 0; zero < fftFrameSize; zero++)
                {
                    gSynMagn[zero] = 0;
                    gSynFreq[zero] = 0;
                }

                for (k = 0; k <= fftFrameSize2; k++)
                {
                    index = (int)(k * pitchShift);
                    if (index <= fftFrameSize2)
                    {
                        gSynMagn[index] += gAnaMagn[k];
                        gSynFreq[index] = gAnaFreq[k] * pitchShift;
                    }
                }

                /* ***************** SYNTHESIS ******************* */
                /* this is the synthesis step */
                for (k = 0; k <= fftFrameSize2; k++)
                {

                    /* get magnitude and true frequency from synthesis arrays */
                    magn = gSynMagn[k];
                    tmp = gSynFreq[k];

                    /* subtract bin mid frequency */
                    tmp -= (double)k * freqPerBin;

                    /* get bin deviation from freq deviation */
                    tmp /= freqPerBin;

                    /* take osamp into account */
                    tmp = 2.0 * Math.PI * tmp / osamp;

                    /* add the overlap phase advance back in */
                    tmp += (double)k * expct;

                    /* accumulate delta phase to get bin phase */
                    gSumPhase[k] += (float)tmp;
                    phase = gSumPhase[k];

                    /* get real and imag part and re-interleave */
                    gFFTworksp[2 * k] = (float)(magn * Math.cos(phase));
                    gFFTworksp[2 * k + 1] = (float)(magn * Math.sin(phase));
                }

                /* zero negative frequencies */
                for (k = fftFrameSize + 2; k < 2 * fftFrameSize; k++) gFFTworksp[k] = 0.0F;

                /* do inverse transform */
                ShortTimeFourierTransform(gFFTworksp, fftFrameSize, 1);

                /* do windowing and add to output accumulator */
                for (k = 0; k < fftFrameSize; k++)
                {
                    window = -.5 * Math.cos(2.0 * Math.PI * (double)k / (double)fftFrameSize) + .5;
                    gOutputAccum[k] += (float)(2.0 * window * gFFTworksp[2 * k] / (fftFrameSize2 * osamp));
                }
                for (k = 0; k < stepSize; k++) gOutFIFO[k] = gOutputAccum[k];

                /* shift accumulator */
                //memmove(gOutputAccum, gOutputAccum + stepSize, fftFrameSize * sizeof(float));
                for (k = 0; k < fftFrameSize; k++)
                {
                    gOutputAccum[k] = gOutputAccum[k + stepSize];
                }

                /* move input FIFO */
                for (k = 0; k < inFifoLatency; k++) gInFIFO[k] = gInFIFO[k + stepSize];
            }
        }
    }
    public static void ShortTimeFourierTransform(float[] fftBuffer, int fftFrameSize, int sign)
    {
        float wr, wi, arg, temp;
        float tr, ti, ur, ui;
        int i, bitm, j, le, le2, k;

        for (i = 2; i < 2 * fftFrameSize - 2; i += 2)
        {
            for (bitm = 2, j = 0; bitm < 2 * fftFrameSize; bitm <<= 1)
            {
                if ((i & bitm) != 0) j++;
                j <<= 1;
            }
            if (i < j)
            {
                temp = fftBuffer[i];
                fftBuffer[i] = fftBuffer[j];
                fftBuffer[j] = temp;
                temp = fftBuffer[i + 1];
                fftBuffer[i + 1] = fftBuffer[j + 1];
                fftBuffer[j + 1] = temp;
            }
        }
        long max = (long)(Math.log(fftFrameSize) / Math.log(2.0) + .5);
        for (k = 0, le = 2; k < max; k++)
        {
            le <<= 1;
            le2 = le >> 1;
            ur = 1.0F;
            ui = 0.0F;
            arg = (float)Math.PI / (le2 >> 1);
            wr = (float)Math.cos(arg);
            wi = (float)(sign * Math.sin(arg));
            for (j = 0; j < le2; j += 2)
            {
                int p1r = j;
                int p1i = p1r + 1;
                int p2r = p1r + le2;
                int p2i = p2r + 1;
                for (i = j; i < 2 * fftFrameSize; i += le)
                {
                    tr = fftBuffer[p2r] * ur - fftBuffer[p2i] * ui;
                    ti = fftBuffer[p2r] * ui + fftBuffer[p2i] * ur;
                    fftBuffer[p2r] = fftBuffer[p1r] - tr;
                    fftBuffer[p2i] = fftBuffer[p1i] - ti;
                    fftBuffer[p1r] += tr;
                    fftBuffer[p1i] += ti;
                    p1r += le;
                    p1i += le;
                    p2r += le;
                    p2i += le;
                }
                tr = ur * wr - ui * wi;
                ui = ur * wi + ui * wr;
                ur = tr;
            }
        }
    }

    private static double smbAtan2(double x, double y)
    {
        double signx;
        if (x > 0.) signx = 1.;
        else signx = -1.;

        if (x == 0.) return 0.;
        if (y == 0.) return signx * Math.PI / 2.;
        return Math.atan2(x, y);
    }
}
