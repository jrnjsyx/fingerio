package com.example.jrnjsyx.fingerio.utils;

public class JniUtils {
    static {
        System.loadLibrary("JniUtils");

    }
    public static native String sayHello();

    public static native double[] fft(double[] data, int len);

    public static native double[] ifft(double[] data, int len);

    public static native double[] xcorr(double[] data1,double[] data2);

//    public static native void fft(double[] data);
}
