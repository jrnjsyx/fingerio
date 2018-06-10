package com.example.jrnjsyx.fingerio.processing;

import com.example.jrnjsyx.fingerio.physical.SignalGenerator;
import com.example.jrnjsyx.fingerio.utils.FlagVar;
import com.example.jrnjsyx.fingerio.utils.JniUtils;


public class Decoder {


    // create variables to store the samples in case frequent new and return

    protected static int processBufferSize = -1;

    public static short[] fingerioSymbol = SignalGenerator.fingerioSymbolGenerator(FlagVar.fingerioSymbolLen,FlagVar.floor,FlagVar.ceil);

    public static short[] soundSig;

    public static void setProcessBufferSize(int size){
        processBufferSize = size;
        soundSig = SignalGenerator.soundSigGenerator(fingerioSymbol,size,FlagVar.oneLoopLen);
    }


    /**
     * normalize the short data to double array
     * @param s : data stream of short samples
     * @return normalized data in double format
     */
    public double[] normalization(short s[]){
        double[] normalized = new double[s.length];
        for (int i = 0; i < s.length; i++) {
            normalized[i] = (double) (s[i]) / 32768;
        }
        return normalized;
    }

    public double[] normalization(short s[], int low, int high){
        double[] normalized = new double[high-low+1];
        for(int i=low;i<=high;i++){
            normalized[i-low] = (double)(s[i])/32768;
        }
        return normalized;
    }

    public double[] normalization(double[] d){
        double max = 0;
        for(int i=0;i<d.length;i++){
            double abs = Math.abs(d[i]);
            max = max<abs?abs:max;
        }
        for(int i=0;i<d.length;i++){
            d[i] = d[i]/max;
        }
        return d;

    }


    /**
     * correlation results, return both the max value and its index
     * @param data1: audio samples
     * @param data2: reference signal
     * @return: return the max value and its index
     */

    public IndexMaxVarInfo getIndexMaxVarInfoFromSigs(double[] data1,double[] data2, boolean isFdomain){
        IndexMaxVarInfo indexMaxVarInfo = new IndexMaxVarInfo();
        double[] corr = xcorr(data1,data2,isFdomain);
        int index = getMaxPosFromCorrdouble(corr);
        indexMaxVarInfo.index = index;
        indexMaxVarInfo.maxVar = corr[(index+corr.length)%corr.length];
        indexMaxVarInfo.corr = corr;
        IndexMaxVarInfo resultInfo = preambleDetection(corr,indexMaxVarInfo);
        return resultInfo;
    }



    public double[] xcorr(double []data1, double[] data2, boolean isFDomain){
        double [] fft1;
        double [] fft2;
        if(isFDomain){
            fft1 = data1;
            fft2 = data2;
        }else{
            int len = data1.length+data2.length;
            fft1 = JniUtils.fft(data1,len);
            fft2 = JniUtils.fft(data2,len);
        }
        double[] corr = JniUtils.xcorr(fft1,fft2);
        return corr;
    }





    /** corr is the correlation array, chirpLength is the chirp signal's length. return the postion of the max correlation.
     * @auther Ruinan Jin
     * @param corr
     * @return
     */
    public int getMaxPosFromCorrdouble(double [] corr){
        double max = 0;
        int end = 0;
        int index = 0;
//        double[] fitVals = getFitPosFromCorrdouble(corr,200);
        for(int i=0;i<corr.length;i++){
            if(corr[i]>max){
                max = corr[i];
                end = i;
                index = i;
            }
        }
        int start = end-400>0?end-400:0;
        for(int i=start;i<=end;i++){
            if(corr[i] >= 0.9*max){
                index = i;
                break;
            }
        }
        return end;
    }

    public double[] getFitPosFromCorrDouble(double [] corr, int halfBlockLength){
        double[] fitVals = new double[corr.length];
        double val = 0;
        for(int i=0;i<halfBlockLength*2+1;i++){
            val += corr[i];
        }
        for(int i=halfBlockLength;i<corr.length-halfBlockLength-1;i++){
            fitVals[i] = val;
            val -= corr[i-halfBlockLength];
            val += corr[i+halfBlockLength+1];
        }
        fitVals[corr.length-halfBlockLength-1] = val;
        for(int i=0;i<halfBlockLength;i++){
            fitVals[i] = fitVals[halfBlockLength];
        }
        for(int i=corr.length-halfBlockLength;i<corr.length;i++){
            fitVals[i] = fitVals[corr.length-halfBlockLength-1];
        }
        double blockLengh = 2*halfBlockLength+1;
        for(int i=0;i<corr.length;i++){
            fitVals[i] = corr[i]*blockLengh/fitVals[i];
        }
        return fitVals;
    }

    public IndexMaxVarInfo preambleDetection(double[] corr, IndexMaxVarInfo indexMaxVarInfo){
        indexMaxVarInfo.isReferenceSignalExist = false;
        if(indexMaxVarInfo.maxVar > FlagVar.preambleDetectionThreshold) {
            indexMaxVarInfo.isReferenceSignalExist = true;
        }
//        System.out.println("index:"+indexMaxVarInfo.index+"   maxCorr:"+indexMaxVarInfo.maxVar);
        return indexMaxVarInfo;
    }


    public boolean isSignalRepeatedDetected(int pos){
        int index = pos;
        if(index >= processBufferSize){
            return true;
        }else{
            return false;
        }
    }





}
