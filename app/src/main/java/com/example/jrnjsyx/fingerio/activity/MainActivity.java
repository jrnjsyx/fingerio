package com.example.jrnjsyx.fingerio.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jrnjsyx.fingerio.R;
import com.example.jrnjsyx.fingerio.physical.AudioRecorder;
import com.example.jrnjsyx.fingerio.physical.PlayThread;
import com.example.jrnjsyx.fingerio.processing.DecodThread;
import com.example.jrnjsyx.fingerio.processing.Decoder;
import com.example.jrnjsyx.fingerio.utils.FlagVar;

public class MainActivity extends AppCompatActivity implements AudioRecorder.RecordingCallback{

    private DecodThread decodThread;
    private AudioRecorder audioRecorder;
    private PlayThread playThread;
    private Button omitButton;
    private Button recvButton;
    private TextView roughTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initParams();
    }

    public void initParams(){
        omitButton = (Button)findViewById(R.id.omit_sound);
        recvButton = (Button)findViewById(R.id.recv_sound);
        roughTextView = (TextView) findViewById(R.id.rough_distance);
        audioRecorder = new AudioRecorder();
        audioRecorder.recordingCallback(this);
        decodThread = new DecodThread(myHandler);
        decodThread.setProcessBufferSize(audioRecorder.getBufferSize()/2);
        playThread = new PlayThread();
        playThread.start();
        playThread.fillBuffer(Decoder.soundSig);
        Thread thread = new Thread(decodThread);
        thread.start();

        omitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playThread.isPlaying()){
                    playThread.pause();
                }else{
                    playThread.play();
                }

            }
        });
        recvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioRecorder.isRecording()) {
                    decodThread.decodeStart();
                    audioRecorder.startRecord();
                } else {
                    audioRecorder.finishRecord();
                }
            }
        });
    }

    @Override
    public void onDataReady(short[] data, int len) {
        //microphone down on the phone
        short[] data1 = new short[len];
        //microphone top on the phone
        short[] data2 = new short[len];
        for (int i = 0; i < len; i++) {
            data1[i] = data[2 * i];
            data2[i] = data[2 * i + 1];
        }
        if(decodThread.samplesList.size()<300) {
            decodThread.fillSamples(data1);
        }
    }


    public Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FlagVar.MESSAGE_ROUGH_ESTIMATE: {
                    StringBuilder sb = new StringBuilder();
                    sb.append("rough distance:"+msg.arg1);
                    roughTextView.setText(sb.toString());
                    break;
                }

            }
        }

    };
}
