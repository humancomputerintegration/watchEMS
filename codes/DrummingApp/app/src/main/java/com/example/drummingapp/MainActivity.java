package com.example.drummingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button startDemo;

    private static final int REQUEST_CODE_SECOND_ACTIVITY = 1001;

    Button createButton;

    boolean[] pattern ;


    Bluetooth bluetoothEMS;

    TextView connectedText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Singleton mySingleton = Singleton.getInstance(this);
        bluetoothEMS = mySingleton.getMyObject();

        pattern = new boolean[8];
        for (int i = 0;i<8;i++){
            pattern[i] = true;
        }

        connectedText = findViewById(R.id.connectText);
        connectedText.setText("Not Connected");
        Button rescanEMS = findViewById(R.id.connect);
        rescanEMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothEMS.isConnected()){
                    bluetoothEMS.setmBluetoothAdapter();
                    connectedText.setText("Not Connected");

                }else{
                    bluetoothEMS.startAdd("connected with beacon");
                    connectedText.setText("Connected");
                }
            }
        });



        bluetoothEMS.setBluetoothGattCallback(
                "19b10000-e9f3-537e-4f6c-d104768a1214",
                "19b10004-e9f3-537e-4f6c-d104768a1214"
        );

        IntentFilter filterEMS = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(bluetoothEMS.createReciever("E7:96:1E:84:FD:75"), filterEMS);
        registerReceiver(bluetoothEMS.createReciever("1C:E7:15:DB:F3:E0"), filterEMS);
        bluetoothEMS.runnableFunc();

        // Check for Bluetooth-related permissions and request if not granted
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_ADMIN
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.BLUETOOTH_CONNECT,
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_ADMIN,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );
        }

        if (!bluetoothEMS.isConnected()){
            bluetoothEMS.setmBluetoothAdapter();
            connectedText.setText("Not Connected");

        }else{
            bluetoothEMS.startAdd("connected with beacon");
            connectedText.setText("Connected");
        }


        createButton = findViewById(R.id.createPattern);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SECOND_ACTIVITY);
            }
        });
        startDemo = findViewById(R.id.runDemo);

        startDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothEMS.isConnected()){
                    Intent intent = new Intent(MainActivity.this, demoActivity.class);
                    intent.putExtra("booleanArray", pattern);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Not connected. Try reconnecting.", Toast.LENGTH_SHORT).show();
                    if (!bluetoothEMS.isConnected()){
                        bluetoothEMS.setmBluetoothAdapter();
                        connectedText.setText("Not Connected");

                    }else{
                        bluetoothEMS.startAdd("connected with beacon");
                        connectedText.setText("Connected");
                    }
                }

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SECOND_ACTIVITY && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra("booleanArray")) {
                pattern = data.getBooleanArrayExtra("booleanArray");
            }
        }
    }

}