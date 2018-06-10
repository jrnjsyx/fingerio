package com.example.jrnjsyx.fingerio.utils;

public class FlagVar {
    public static int Fs = 48000;
    public static int bufferSize = 20480;
    public static double preambleDetectionThreshold= 0.01f;
    public static int numberOfPreviousSamples = 100;
    public static double ratioThreshold = 2.5f;
    public static int floorF = 13000;
    public static int ceilF = 15000;
    public static int fingerioSymbolLen = 64;
    public static int floor = (int)(1.0*floorF*fingerioSymbolLen/FlagVar.Fs);
    public static int ceil = (int)(1.0*ceilF*fingerioSymbolLen/FlagVar.Fs);
    public static int detectedLen = fingerioSymbolLen *2;
    public static double downEchoChangeDetectionThreshold = 0.03;
    public static double upEchoChangeDetectionThreshold = 0.12;
    public static final int MESSAGE_ROUGH_ESTIMATE = 0;
    public static final int MESSAGE_PRECISE_ESTIMATE = 1;
    public static int oneLoopLen = 4*fingerioSymbolLen;
    public static int trimLen = 20;



}
