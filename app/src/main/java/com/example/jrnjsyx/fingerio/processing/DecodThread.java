package com.example.jrnjsyx.fingerio.processing;

import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.fingerio.utils.FlagVar;
import com.example.jrnjsyx.fingerio.utils.JniUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class DecodThread extends Decoder implements Runnable {

    private static final String TAG = "DecodThread";
    private boolean isThreadRunning = true;
    private Integer mLoopCounter = 0;
    private Handler mHandler;
    private Integer corrMaxPos = -1;
    public List<short[]> samplesList;
    public List<double[]> echoProfiles;



    public DecodThread(Handler mHandler){
        samplesList = new LinkedList<short[]>();
        echoProfiles = new LinkedList<double[]>();
        this.mHandler = mHandler;
    }

    public void fillSamples(short[] s){

        synchronized (samplesList) {
            samplesList.add(s);
        }

    }

    @Override
    public void run() {
        if(processBufferSize < 0){
            throw new RuntimeException("processBufferSize < 0");
        }
        try {
            while (isThreadRunning) {
                if (samplesList.size() >= 2) {
                    short [] bufferedSamples = new short[processBufferSize+ FlagVar.detectedLen];
                    synchronized (samplesList) {
                        System.arraycopy(samplesList.get(0),processBufferSize-FlagVar.detectedLen,bufferedSamples,0,FlagVar.detectedLen);
                        System.arraycopy(samplesList.get(1),0,bufferedSamples,FlagVar.detectedLen,processBufferSize);
                        samplesList.remove(0);
                    }
                    IndexMaxVarInfo info = getIndexMaxVarInfoFromSigs(normalization(bufferedSamples),normalization(fingerioSymbol),false);
                    if(corrMaxPos == -1 ){
                        if(!isSignalRepeatedDetected(info.index)) {
                            corrMaxPos = info.index;
                        }else{
                            continue;
                        }
                    }else{
                        if(Math.abs(corrMaxPos-info.index)%(FlagVar.oneLoopTimes*FlagVar.fingerioSymbolLen) > 20 && !isSignalRepeatedDetected(info.index)){
                            corrMaxPos = info.index;
                        }
                    }
                    if(info.isReferenceSignalExist){
                        short[] samples = new short[FlagVar.detectedLen];
                        System.arraycopy(bufferedSamples,corrMaxPos,samples,0,FlagVar.detectedLen);
                        int len = samples.length+ fingerioSymbol.length;
                        double[] fft1 = JniUtils.fft(normalization(samples),len);
                        double[] fft2 = JniUtils.fft(normalization(fingerioSymbol),len);
                        double[] corr = JniUtils.xcorr(fft1,fft2);
                        corr = normalization(corr);
                        echoProfiles.add(corr);
                    }
                    if(echoProfiles.size() >= 2){
                        distanceEstimate();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void distanceEstimate(){
        int index = roughEstimate();
    }

    public int roughEstimate(){
        double[] corrDiff = new double[echoProfiles.get(0).length];
        synchronized (echoProfiles) {
            for (int i = 0; i < corrDiff.length; i++) {
                corrDiff[i] = Math.abs(echoProfiles.get(1)[i] - echoProfiles.get(0)[i]);
            }
            echoProfiles.remove(0);
        }
        int index = -1;
        for(int i=0;i<corrDiff.length;i++){
            if(corrDiff[i] > FlagVar.echoChangeDetectionThreshold){
                index = i;
                break;
            }
        }
        System.out.println(Arrays.toString(echoProfiles.get(0)));
        if(index > 0) {
            System.out.println("index:"+index+"  corrDiff:"+corrDiff[index]);
            Message msg = new Message();
            msg.what = FlagVar.MESSAGE_ROUGH_ESTIMATE;
            msg.arg1 = index;
            mHandler.sendMessage(msg);
        }
        return index;
    }
    public void decodeStart(){
        synchronized (corrMaxPos){
            corrMaxPos = -1;
        }
        synchronized (samplesList){
            samplesList.clear();
        }
        synchronized (echoProfiles){
            echoProfiles.clear();
        }

    }



    public void close() {
        synchronized (this){
            isThreadRunning = false;
        }
    }


}
