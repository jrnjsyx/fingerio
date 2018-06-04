package com.example.jrnjsyx.fingerio.utils;

public class FlagVar {
    public static int Fs = 48000;
    public static int bufferSize = 20480;
    public static double preambleDetectionThreshold= 0.01f;
    public static int numberOfPreviousSamples = 100;
    public static double ratioThreshold = 2.5f;
    public static int floorF = 18000;
    public static int ceilF = 20000;
    public static int fingerioSymbolLen = 64;
    public static int detectedLen = fingerioSymbolLen *2;
    public static double echoChangeDetectionThreshold = 0.05;
    public static final int MESSAGE_ROUGH_ESTIMATE = 0;
    public static int oneLoopTimes = 4;



}
