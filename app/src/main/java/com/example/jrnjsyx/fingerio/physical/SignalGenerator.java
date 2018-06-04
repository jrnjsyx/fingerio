package com.example.jrnjsyx.fingerio.physical;


import com.example.jrnjsyx.fingerio.utils.FlagVar;
import com.example.jrnjsyx.fingerio.utils.JniUtils;

public class SignalGenerator  {

    /**
     * generate up chirp signal
     * @param fs - sampling rate
     * @param t - douration of the chirp signal
     * @param b - bandwidth
     * @param f - fmin
     * @return audio samples in short format
     */
    public static short[] upChirpGenerator(int fs, double t, int b, int f){
        double[] x = chirpGenerator(fs, t, b, f, 0);
        waveformReshaping(x);
        short[] samples = new short[x.length];
        for(int i = 0; i < x.length; i++){
            samples[i] = (short)(32767 * x[i]);
        }
        return samples;
    }

    /**
     * generate down chirp signal
     * @param fs - sampliong rate
     * @param t - duration
     * @param b - bandwidth
     * @param f - fmax
     * @return audio samples in short format
     */
    public static short[] downChirpGenerator(int fs, double t, int b, int f){
        double[] x = chirpGenerator(fs, t, b, f, 1);
        waveformReshaping(x);
        short[] samples = new short[x.length];
        for(int i = 0; i < x.length; i++){
            samples[i] = (short)(32767 * x[i]);
        }
        return samples;
    }

    /**
     *  generate the chirp signal in short format
     * @param fs - sampling rate
     * @param t - duration of the chirp signal
     * @param b - bandwidth of the chirp signal
     * @param f - fmin for up chirp signal and fmax for the down chirp signal
     * @param type - 0 for up chirp signal and 1 for down chirp signal
     * @return chirp samples in double format
     */
    public static double[] chirpGenerator(int fs, double t, int b, int f, int type){
        int n = (int)(fs * t);
        double[] samples = new double[n];
        if( type == 0 ) {
            for (int i = 0; i < n; i++) {
                samples[i] = (double) Math.cos(2 * Math.PI * f * i / fs + Math.PI * b * i * i / t / fs / fs);
            }
        }else{
            for (int i = 0; i < n; i++) {
                samples[i] = (double) Math.cos(2 * Math.PI * f * i / fs - Math.PI * b * i * i / t / fs / fs);
            }
        }
        return samples;
    }

    /**
     * waveform reshaping to mitigate the audible noise
     * slowly ramping up the amplitude of the 100 samples and reversely perform it on the last 100 samples
     * @param samples
     */
    public static void waveformReshaping(double samples[]){
        int k = 100;
        double coefficients = 1.0f / k;

        for(int i = 0; i < k; i++){
            samples[i] = samples[i] * (i + 1) / k;
            samples[samples.length - 1 - i] = samples[samples.length - 1 - i] * (i + 1) / k;
        }
    }



    public static short[] fingerioSymbolGenerator(int length, int floorF, int ceilF){
        short[] samples = new short[length];
        double[] sig = new double[length];
        int floor = (int)(1.0*floorF*length/FlagVar.Fs);
        int ceil = (int)(1.0*ceilF*length/FlagVar.Fs)+1;

        for(int i=0;i<sig.length;i++){
            if(i >= floor && i <= ceil){
                sig[i] = (double)((i%2)*2-1);
            }
            else{
                sig[i] = 0;
            }
        }
        double[] ifft = JniUtils.ifft(sig,length);
        for(int i=0;i<length;i++){
            sig[i] = ifft[2*i];
        }
        double max = 0;
        for(int i=0;i<sig.length;i++){
            double abs = Math.abs(sig[i]);
            max = max<abs?abs:max;
        }
        for(int i=0;i<samples.length;i++){
            samples[i] = (short)(32767*sig[i]/max);
        }
        return samples;
    }

    public static short[] soundSigGenerator(short[] sig, int size, int oneLoopTimes){
        short[] samples = new short[size];
        if(size%(sig.length*oneLoopTimes) != 0){
            throw new RuntimeException("size should be several times of the sig length");
        }
        for(int i=0;i<size;i++){
            if(i%(sig.length*oneLoopTimes)<sig.length){
                samples[i] = sig[i%(sig.length*oneLoopTimes)];
            }
        }
        return samples;
    }
}
