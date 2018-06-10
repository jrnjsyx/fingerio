package com.example.jrnjsyx.fingerio.processing;

import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.fingerio.utils.FileUtils;
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
    private short [] bufferedSamples = new short[FlagVar.oneLoopLen];



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
                if (samplesList.size() >= 1) {

                    synchronized (samplesList) {
                        System.arraycopy(samplesList.get(0),0,bufferedSamples,0,FlagVar.oneLoopLen);
                    }
                    IndexMaxVarInfo info = getIndexMaxVarInfoFromSigs(normalization(bufferedSamples),normalization(fingerioSymbol),false);
                    corrMaxPos = info.index;
                    if(info.isReferenceSignalExist){
                        short[] samples = new short[FlagVar.detectedLen];
                        System.arraycopy(samplesList.get(0),corrMaxPos,samples,0,FlagVar.detectedLen);
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
                    synchronized (samplesList){
                        samplesList.remove(0);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void distanceEstimate(){
        int index = roughEstimate();
        if(index != -1) {
            preciseEstimate(index);
        }
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
            if(corrDiff[i] > FlagVar.downEchoChangeDetectionThreshold){
                index = i;
                break;
            }
        }
//        System.out.println(Arrays.toString(echoProfiles.get(0)));
        if(index > 5 && index < FlagVar.detectedLen) {
            System.out.println("index:"+index+"   corrMaxPos:"+corrMaxPos+"  corrDiff:"+corrDiff[index]);
            Message msg = new Message();
            msg.what = FlagVar.MESSAGE_ROUGH_ESTIMATE;
            msg.arg1 = index;
            mHandler.sendMessage(msg);
            return index;
        }else{
            return -1;
        }


    }

    public int preciseEstimate(int index){

        double[] data = new double[FlagVar.fingerioSymbolLen];
        System.arraycopy(normalization(samplesList.get(0)),corrMaxPos+index,data,0,FlagVar.fingerioSymbolLen);
        double[] fft = JniUtils.fft(data,FlagVar.fingerioSymbolLen);
        int start = FlagVar.floor;
        int end = FlagVar.ceil;
        double[] angles = new double[end-start+1];
        for(int i=start;i<=end;i++){
            double angle = Math.atan(fft[2*i+1]/fft[2*i])/Math.PI*180;
            if(fft[2*i] < 0){
                angle += 180;
            }
            angles[i-start] = angle;
        }
        for(int i=1;i<angles.length;i++){
            while (angles[i]<angles[i-1]){
                angles[i] += 360;
            }
        }
        FileUtils.saveBytes(samplesList.get(0),"data");
        System.out.println("fft:"+Arrays.toString(fft));
        System.out.println("samples:"+Arrays.toString(samplesList.get(0)));
        System.out.println("data:"+Arrays.toString(data));
        System.out.println("angles:"+Arrays.toString(angles));
        System.out.println("start:"+start+"  end:"+end);
        int shift = (int)((angles[angles.length-1]-angles[0])/(angles.length-1)*FlagVar.fingerioSymbolLen/360);
        System.out.println("shift:"+shift);
        Message msg = new Message();
        msg.what = FlagVar.MESSAGE_PRECISE_ESTIMATE;
        msg.arg1 = shift;
        mHandler.sendMessage(msg);
        return shift;
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
