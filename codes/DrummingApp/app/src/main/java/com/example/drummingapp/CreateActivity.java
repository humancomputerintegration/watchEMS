package com.example.drummingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CreateActivity extends AppCompatActivity {

    private boolean[] boxStates = new boolean[8]; // Array for 8 boxes
    private TextView[] boxes = new TextView[8]; // Array to hold TextViews for boxes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Initialize TextViews for boxes
        boxes[0] = findViewById(R.id.box1);
        boxes[1] = findViewById(R.id.box2);
        boxes[2] = findViewById(R.id.box3);
        boxes[3] = findViewById(R.id.box4);
        boxes[4] = findViewById(R.id.box5);
        boxes[5] = findViewById(R.id.box6);
        boxes[6] = findViewById(R.id.box7);
        boxes[7] = findViewById(R.id.box8);

        // Initialize box states
        for (int i = 0; i < 8; i++) {
            boxStates[i] = false; // Initially all boxes are grey
            toggleBoxState(i, boxes[i]); // Set initial state
        }

        // Set click listeners for each box
        for (int i = 0; i < 8; i++) {
            final int index = i;
            boxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleBoxState(index, boxes[index]);
                }
            });
        }

        // Button to send back the boolean array
        findViewById(R.id.buttonSendback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("booleanArray", boxStates);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void toggleBoxState(int index, TextView textView) {
        if (boxStates[index]) {
            textView.setBackgroundColor(getResources().getColor(R.color.grey));
            boxStates[index] = false;
        } else {
            textView.setBackgroundColor(getResources().getColor(R.color.purple));
            boxStates[index] = true;
        }
    }
}
