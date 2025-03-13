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

public class pulseActivity extends Activity {

    private int value_min = 150; //us
    private int value_max = 500; //us
    private int value = value_min;

    private CircularSeekBar circle;
    private TextView percentageDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);
        Intent intent = getIntent();
        /*try {
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("KEY_CUR_VAL"));
            String cur_val = jsonObject.getString("p_width");
            Log.d("cur val/p_width", jsonObject + ",  val: " + cur_val);
            value = Integer.parseInt(cur_val);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }*/

        percentageDisplay = findViewById(R.id.textView);
        percentageDisplay.setText(value + " us");
        circle = findViewById(R.id.circularSeekBar);
        circle.setProgress(100 * (value - value_min) / (value_max - value_min));
        circle.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                value = (value_max - value_min) * (int)circle.getProgress() / 100 + value_min;
                // Log the progress value whenever it changes
                Log.d("CircularSeekBar", "Progress: " + progress + "%, " + value + " us");
                percentageDisplay.setText((int)value + " us");
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
                String dataToSend = "wid " + value;

                Intent intent = new Intent(pulseActivity.this, MainActivity.class);
                intent.putExtra("key", dataToSend);
                setResult(RESULT_OK, intent);
                finish();

            }
        });

    }

}