package com.example.watchapp.presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.watchapp.R;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class freqActivity extends Activity {

    private int value_min = 1; //Hz
    private int value_max = 400; //Hz
    private int value = 0;
    TextView percentageDisplay;
    private CircularSeekBar circle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freq);
        Intent intent = getIntent();
        /*try {
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("KEY_CUR_VAL"));
            String cur_val = jsonObject.getString("frequency");
            Log.d("cur val/frequency", jsonObject + ",  val: " + cur_val);
            value = Integer.parseInt(cur_val);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }*/

        percentageDisplay = findViewById(R.id.textView);
        percentageDisplay.setText(value + " Hz");
        circle = findViewById(R.id.circularSeekBar);
        circle.setProgress(100 * (value - value_min) / (value_max - value_min));
        circle.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                //Max value is 400 Hz, Min value is 1
                value = (value_max - value_min) * (int)circle.getProgress() / 100 + value_min;
                // Log the progress value whenever it changes
                Log.d("CircularSeekBar", "Progress: " + progress + "%, value: " + value + "Hz");
                percentageDisplay.setText(value + " Hz");
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                // Handle when the user stops dragging the thumb (optional)
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                // Handle when the user starts dragging the thumb (optional)
            }
        });


        Button send = findViewById(R.id.button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dataToSend = "freq " + value;

                Intent intent = new Intent(freqActivity.this, MainActivity.class);
                intent.putExtra("key", dataToSend);
                setResult(RESULT_OK, intent);
                finish();

            }
        });

    }

}