package com.example.myactualsocket3_11;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Bluetooth implements Serializable {
    BluetoothAdapter mBluetoothAdapter;

    BluetoothGatt mGatt;
    BluetoothGatt b1Gatt;
    BluetoothGattCallback b1CallBack;

    BluetoothGattCallback b2CallBack;
    BluetoothGatt b2Gatt;


    BluetoothGattService service;

    boolean connected = false;

    double RSSI = 0;


    Activity myContext;
    BluetoothGattCharacteristic intensityCharacteristic;
    BluetoothGattCharacteristic pulseWidthCharacteristic;
    BluetoothGattCharacteristic frequencyCharacteristic;
    BluetoothGattCharacteristic startCharacteristic;
    BluetoothDevice device;

    BluetoothDevice beacon1;

    BluetoothDevice beacon2;

    LinkedList<String> startQ = new LinkedList<>();
    BluetoothGattCallback bluetoothGattCallback;

    BroadcastReceiver mReceiver;
    Handler queueHandler;
    Runnable queueRunnable;

    @SuppressLint("MissingPermission")
    public Bluetooth(Activity context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        myContext = context;


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.startDiscovery();
        Log.d("Discovering devices ...", "Discovering devices ...");


    }



    public BluetoothDevice getDevice() {
        return device;
    }


    public void startAdd(String data) {
        myContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startQ.add(data);
                if (isConnected()){
                    Log.d("sending...",startCharacteristic.getUuid().toString());

                }else{
                    Toast.makeText(myContext.getApplication(), "You not connected!", Toast.LENGTH_SHORT).show();
                    Log.d("sending...",startCharacteristic.getUuid().toString());

                }
                runnableFunc();
            }
        });
    }
    ArrayList<Button> buttonName  = new ArrayList<>();;
    ArrayList<String> buttonAddress  = new ArrayList<>();;

    //Put your bluetooth device address here.
    public BroadcastReceiver createReciever(Context actCont, LinearLayout buttonCont, Button disconnect, Button refresh, TextView tv, String adds) {
        buttonCont.removeAllViews();
        buttonCont.setVisibility(View.VISIBLE);
        for (Button b:
             buttonName) {
            ViewGroup parent = (ViewGroup) b.getParent();
            if (parent != null) {
                parent.removeView(b);
            }
            buttonCont.addView(b);
        }

        if (mReceiver == null){
            mReceiver = new BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Log.d("address", foundDevice.getAddress() + ":" + foundDevice.getName());

                        Singleton mySingleton = Singleton.getInstanceBLE((Activity) context);
                        if (adds.equals("b1")){
                            Log.d("here",foundDevice.getAddress()+" "+mySingleton.getBeacon1());

                            if (foundDevice.getAddress().equals(mySingleton.getBeacon1())){
    //                            sendMessage("1:"+intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0), mySingleton.getSock());
                                Log.d("rssi 1", "1:"+intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0));
                                mBluetoothAdapter.cancelDiscovery();
                                Log.d("Connecting...", "Connecting...");
                                device = foundDevice;
                                mGatt = device.connectGatt(context.getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
                            }
                        }
                        if (adds.equals("b2")) {
                            if (foundDevice.getAddress().equals(mySingleton.getBeacon2())) {
                                Log.d("found here","2");
//                            sendMessage("1:"+intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0), mySingleton.getSock());
                                Log.d("rssi 2", "2:" + intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0));
                                mBluetoothAdapter.cancelDiscovery();
                                Log.d("Connecting...", "Connecting...");
                                device = foundDevice;
                                mGatt = device.connectGatt(context.getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
                            }
                        }else{
                            Button button = new Button(actCont);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            );
                            button.setLayoutParams(params);
                            if (foundDevice.getName() != null && !buttonAddress.contains(foundDevice.getAddress())){
                                buttonAddress.add(foundDevice.getAddress());
                                button.setText(foundDevice.getName());
                                Log.d("names",foundDevice.getName()+" --3");
                                button.setOnClickListener(v -> {
                                    mBluetoothAdapter.cancelDiscovery();
                                    Log.d("Connecting...", "Connecting...");
                                    device = foundDevice;
                                    mGatt = device.connectGatt(context.getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
                                    buttonCont.setVisibility(View.GONE);
                                    disconnect.setVisibility(View.VISIBLE);
                                    refresh.setVisibility(View.GONE);
                                    tv.setVisibility(View.VISIBLE);
                                    if (device.getName()==null){
                                        tv.setText("Connected to UNKNOWN");
                                    }else{
                                        tv.setText("Connected to "+device.getName());
                                    }
                                });
                                buttonName.add(button);
                                myContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buttonCont.addView(button); // Add button to layout
                                        buttonCont.setVisibility(View.VISIBLE);
                                        buttonCont.invalidate();

                                    }
                                });
                            }

                        }
                    }
                }
            };

        }
        return mReceiver;
    }

    public void setNewBluetoothGattCallback(String type){
        bluetoothGattCallback = setBluetoothGattCallback("19b10000-e9f3-537e-4f6c-d104768a1214","19b10005-e9f3-537e-4f6c-d104768a1214");
    }
    private void sendMessage(final String message, Socket socket) {
        // Send messages on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter out = null;
                try {
                    out = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (out != null) {
                    String messageWithHeader = String.format("%-10s", message.length()) + message;
                    out.print(messageWithHeader);
                    out.flush();
                }
            }
        }).start();
    }

    public double getDistanceFromRSSI() {
        return RSSI;
    }

    @SuppressLint("MissingPermission")
    public void startRssiRead() {
        if (mGatt != null) {
            mGatt.readRemoteRssi();
        }
    }

    @SuppressLint("MissingPermission")
    public void startRssiB1() {
        if (mGatt != null) {

            Log.d("rssi here", "reading rssi b1"+mGatt.readRemoteRssi());


        }
    }

    @SuppressLint("MissingPermission")
    public void startRssiB2() {
        if (mGatt != null) {
            Log.d("rssi here", "reading rssi b1"+mGatt.readRemoteRssi());


        }
    }


    public void runnableFunc() {
        queueHandler = new Handler();
        queueRunnable = new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                if (mGatt == null || startCharacteristic == null) {
                    queueHandler.postDelayed(this, 10);
                } else if (!startQ.isEmpty()) {
                    String start = startQ.pop();
                    startQ.clear();
                    Log.d("test", "popped: " + start + " at " + System.currentTimeMillis());
                    startCharacteristic.setValue(start);
                    mGatt.writeCharacteristic(startCharacteristic);
                    queueHandler.postDelayed(this, 10);
                } else {
                    queueHandler.postDelayed(this, 10);
                }
            }
        };
        queueRunnable.run();
    }

    @SuppressLint("MissingPermission")
    public void manualWrite(String start) {
        myContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startCharacteristic.setValue(start);
                mGatt.writeCharacteristic(startCharacteristic);
            }
        });

    }
    public boolean isConnected() {
        return connected;
    }

    @SuppressLint("MissingPermission")
    public void setmBluetoothAdapter(){
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.startDiscovery();
        Log.d("Discovering devices ...", "Discovering devices ...");
        ;

    }
    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (mGatt != null) {
            connected = false;   // Updates the connection status.
            manualWrite("disconnect");
            mGatt.disconnect();  // Disconnects the device.
            mGatt.close();       // Closes the GATT connection to clean up resources.
            mGatt = null;        // Sets the GATT object to null after closing.
            Log.d("Bluetooth", "Device disconnected and resources released.");
        }
    }

    public void removeViewsbuttons(){
        buttonName.clear();
        buttonAddress.clear();
    }
    public BluetoothGattCallback setBluetoothGattCallback(String devUuid, String uuidChar1) {
        return ( new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();

                    if (device.getAddress().equals(Singleton.getInstanceBLE(myContext).getBeacon1())){
                        Log.d("rssi here","B1 You are here, connected.");
                        startRssiB1();
                    }
                    if (device.getAddress().equals(Singleton.getInstanceBLE(myContext).getBeacon2())){
                        Log.d("rssi here","B2 You are here, connected.");
                        startRssiB2();
                    }
//                    mGatt.readRemoteRssi();
                    connected = true;
                    startQ.clear();
                    Singleton mySingleton = Singleton.getInstanceBLE(myContext);
//                    sendMessage("Bluetooth connected!", mySingleton.getSock());
                    myContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Connected", "connected to "+gatt.getDevice().getName());
                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Singleton mySingleton = Singleton.getInstanceBLE(myContext);
//                    sendMessage("Bluetooth DISconnected!", mySingleton.getSock());
                    if(connected){
                        mGatt = device.connectGatt(myContext.getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
                    }else{
                        connected = false;

                        myContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(" not Connected", "not connected");
                            }
                        });
                    }

                }
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                String value = characteristic.getStringValue(0);
                Log.d("test", "Characteristic Written: " + value + " at " + System.currentTimeMillis());
            }
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                Log.d("RSSI Debug", gatt.getDevice().getAddress());
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (device.getAddress().equals(Singleton.getInstanceBLE(myContext).getBeacon1())) {
                        Log.d("RSSI B1", "RSSI Value: " + rssi);
                    }if (device.getAddress().equals(Singleton.getInstanceBLE(myContext).getBeacon2())){
                        Log.d("RSSI B2", "RSSI Value: " + rssi);
                    }
                    // Handle the RSSI value as needed
                } else {
                    Log.e("RSSI", "Error reading RSSI: " + status);
                }

                // Schedule the next RSSI read after a delay (adjust as needed)
                queueHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startRssiRead();
                        RSSI = rssi;
                    }
                }, 100); // 1000 milliseconds delay for the next RSSI read
            }
            @SuppressLint("MissingPermission")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                final List<BluetoothGattService> services = gatt.getServices();
                Log.d("debugginging", "1");

                for (BluetoothGattService s : services) {
                    String uuid = s.getUuid().toString();
                    Log.d("debugginging", "2");
                    if (uuid.equals(devUuid)) {
                        Log.d("debugginging", "3");
                        service = s;
                        for (BluetoothGattCharacteristic mCharacteristic : s.getCharacteristics()) {
                            Log.d("debugginging", "4");

                            if (mCharacteristic.getUuid().toString().equals(uuidChar1)) {
                                startCharacteristic = mCharacteristic;
                                Log.d("debugginging", uuidChar1);

                                Log.d("Start ", "Start characteristic established");
                            }else{
                                Log.d("Start ", mCharacteristic.getUuid().toString());

                            }
                        }
                    }
                }
            }
        });
    }
}
