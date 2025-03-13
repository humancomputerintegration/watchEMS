package com.example.mockmaps;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private CustomMapView customMapView;

    private Bluetooth recievedBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set full-screen flags
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Singleton mySingleton = Singleton.getInstance(this);
        recievedBluetooth = mySingleton.getMyObject();

        setContentView(R.layout.activity_main);

        customMapView = findViewById(R.id.customMapView);
    }

    public void onMoveButtonClick(View view) {
        if (customMapView.isMoving()) {
            customMapView.stopMoving();
        } else {
            customMapView.startMoving();
        }
    }
}
