package com.example.drummingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class demoActivity extends AppCompatActivity {
    private CircularSeekBar circle;

    TextView bpmDisplay;

    long pastTime = System.currentTimeMillis();
    long currentTime = System.currentTimeMillis();


    int bpm = 60;

    Button playButton;
    Bluetooth recievedBluetooth;
    ImageView exit;

    Timer timer;
    boolean playing = true;

    boolean[] pattern;

    int ind = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        pattern = getIntent().getBooleanArrayExtra("booleanArray");

        Singleton mySingleton = Singleton.getInstance(this);
        recievedBluetooth = mySingleton.getMyObject();


        bpmDisplay = findViewById(R.id.bpmDisp);

        circle = findViewById(R.id.circularSeekBar);
        bpm=(int)(circle.getProgress()*1.50);
        bpmDisplay.setText(bpm+" bpm");

        playButton = findViewById(R.id.play);


        TimerTask task = new TimerTask() {
            public void run() {
                currentTime = System.currentTimeMillis();
                if ((currentTime - pastTime )>= ((60.0/bpm)*1000)/2){
                    if (!playing){
                        if (pattern[ind]){
                            recievedBluetooth.startAdd("drum");
                        }
                        ind++;
                        ind = ind%(pattern.length);
                    }
                    pastTime = System.currentTimeMillis();
                }
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 90);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing){
                    playing = false;
                    playButton.setText("Pause");
                }else{
                    playing = true;
                    playButton.setText("Play");
                }
            }
        });

        exit = findViewById(R.id.button_close);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                timer.cancel();
                finish();
            }
        });

        circle.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                // Log the progress value whenever it changes
                Log.d("CircularSeekBar", "Progress: " + progress);
                bpm=(int)(circle.getProgress()*1.50);
                bpmDisplay.setText(bpm+" bpm");

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                // Handle when the user starts dragging the thumb (optional)
            }
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop all timers when activity is destroyed
        timer.cancel();
    }

}