package com.example.myactualsocket3_11;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ConnectActivity extends AppCompatActivity{
    Bluetooth bluetoothEMS;

    TextView connectedText;
    Button disconnectEMS;
    Button refresh;
    LinearLayout buttonContainer;

    Bluetooth b1Ble;

    Bluetooth b2Ble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_connect);


        disconnectEMS = findViewById(R.id.disconnectButton);
        refresh = findViewById(R.id.refreshButton);

        disconnectEMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothEMS.disconnect();
                disconnectEMS.setVisibility(View.GONE);
                bluetoothEMS.removeViewsbuttons();
                connectedText.setVisibility(View.GONE);
                buttonContainer.removeAllViews();
                unregisterReceiver(bluetoothEMS.mReceiver);
                refresh.setVisibility(View.VISIBLE);
            }
        });

        Singleton mySingleton = Singleton.getInstanceBLE(this);
        bluetoothEMS = mySingleton.getMyBLEObject();
        b1Ble = new Bluetooth(this);
        b2Ble = new Bluetooth(this);

        buttonContainer = findViewById(R.id.buttonContainer);

        Log.d("123",""+buttonContainer.getChildCount());

        refresh.setOnClickListener(view -> {
            Log.d("123",""+buttonContainer.getChildCount());

            bluetoothEMS.setmBluetoothAdapter();
            b1Ble.setmBluetoothAdapter();
            b2Ble.setmBluetoothAdapter();

//            if (bluetoothEMS.mReceiver != null) {
//                unregisterReceiver(bluetoothEMS.mReceiver);
//            }
//            bluetoothEMS.setBluetoothGattCallback(
//                    "19b10000-e9f3-537e-4f6c-d104768a1214",
//                    "19b10005-e9f3-537e-4f6c-d104768a1214"
//            );

//            buttonContainer.removeAllViews();
//            IntentFilter filtersEMS = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            registerReceiver(bluetoothEMS.createReciever(this, buttonContainer, disconnectEMS, refresh, connectedText), filtersEMS);
        });

        connectedText = findViewById(R.id.connectedTo);
        connectedText.setText("Not Connected");



        bluetoothEMS.setNewBluetoothGattCallback("ble");
        b1Ble.setNewBluetoothGattCallback("b1");
        b2Ble.setNewBluetoothGattCallback("b2");


        IntentFilter filterEMS = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothEMS.createReciever(this, buttonContainer, disconnectEMS, refresh,connectedText, "none"), filterEMS);
        bluetoothEMS.runnableFunc();

//        IntentFilter filterEMSB1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(b1Ble.createReciever(this, new LinearLayout(this), new Button(this), new Button(this),new TextView(this), "b1"), filterEMSB1);
//        b1Ble.runnableFunc();

        IntentFilter filterEMSB2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(b1Ble.createReciever(this, new LinearLayout(this), new Button(this), new Button(this),new TextView(this), "b2"), filterEMSB2);
        b2Ble.runnableFunc();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );
        }

        if (!bluetoothEMS.isConnected()) {
            bluetoothEMS.setmBluetoothAdapter();
            connectedText.setText("Not Connected");

        } else {
            bluetoothEMS.startAdd("stim1000");
            connectedText.setText("Connected");

            buttonContainer.setVisibility(View.GONE);
            disconnectEMS.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.GONE);
            connectedText.setVisibility(View.VISIBLE);

        }
    }

}
