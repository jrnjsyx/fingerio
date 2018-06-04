package com.example.jrnjsyx.fingerio.physical;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.jrnjsyx.fingerio.utils.FlagVar;

import java.util.Arrays;


public class PlayThread extends Thread {

    /*
    This thread is used to play audio samples in PCM format
     */

    private boolean isRunning = true;
    private boolean isBufferReady = false;
    private boolean oneLoopStart = false;
    private int minBufferSize = 0;
    private int realBufferSize;
    private short[] buffer;

    private final String TAG = "PlayThread";

    public PlayThread(){
        buffer = new short[FlagVar.bufferSize];
        minBufferSize = AudioTrack.getMinBufferSize(
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        realBufferSize = 1;
        while(realBufferSize < minBufferSize){
            realBufferSize *= 2;
        }
    }

    public void fillBuffer(short[] data){
        isBufferReady = false;
        while(oneLoopStart){
        }
        Arrays.fill(buffer, (short)(0));
        System.arraycopy(data, 0, buffer, 0, data.length);
    }

    /**
     * run process, always stay alive until the end of the program
     */
    public void run(){

        AudioTrack audiotrack;
        // get the minimum buffer size

        // initialize the audiotrack
        audiotrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                realBufferSize,
                AudioTrack.MODE_STREAM);

        audiotrack.play();
        while (isRunning){
            if(isBufferReady){
                oneLoopStart = true;
                audiotrack.write(buffer, 0, realBufferSize);
                oneLoopStart = false;
            }
        }

        try{
            audiotrack.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isPlaying(){
        return isBufferReady;
    }

    public void pause(){
        isBufferReady = false;
    }

    public void play(){
        isBufferReady = true;
    }

    /*
    shut down the thread
     */
    public void close(){
        isRunning = false;
    }

}
