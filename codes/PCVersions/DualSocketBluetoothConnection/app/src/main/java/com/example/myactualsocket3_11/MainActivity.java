package com.example.myactualsocket3_11;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button connectBLE;
    private Button connectSocket;
    private Socket socket;
    private  String SERVER_IP = "10.0.0.178"; // Update with your server IP
    private  String SERVER_PORT = "1234";

    private PrintWriter out;
    private BufferedReader in;
    private boolean running = false;
    Bluetooth bluetoothEMS;

    private final Handler handler = new Handler(Looper.getMainLooper()); // UI update handler
    private Thread receiveThread; // Thread that gets messages
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::WakeLockTag");
        wakeLock.acquire();

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

        Singleton mySingleton = Singleton.getInstanceBLE(this);
        bluetoothEMS = mySingleton.getMyBLEObject();

        //Get xml elements
        connectBLE = findViewById(R.id.connectBLE);
        connectSocket = findViewById(R.id.socket);



        connectSocket.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this, ConnectSocketActivity.class);
            startActivityForResult(i, 1000);
        });


        connectBLE.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this, ConnectActivity.class);
            startActivity(i);
        });
    }

    //establish the connection
    private void startTcpConnection(String serverIP, String serverPort) {
        try {
            socket = new Socket(serverIP, Integer.parseInt(serverPort));
            Singleton mySingleton = Singleton.getInstanceBLE(this);
            mySingleton.setSock(socket);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = true;

            // Start the receiving thread
            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    listenForMessages();
                }
            });
            receiveThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        String response;
        try {
            // Continuously listen for messages from the server
            while (running && (response = in.readLine()) != null) {
                String finalResponse = response;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothEMS.startAdd(finalResponse.substring(10));
                    }
                });
            }
            if (!running){
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Clean up resources
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            if (data != null) {
                Boolean connectionCont = true;
                if (data.hasExtra("address")){
                    SERVER_IP = data.getStringExtra("address");
                    Singleton.getInstanceBLE(this).setAddress(SERVER_IP);

                    Log.d("arc", SERVER_IP);
                }else{
                    connectionCont = false;
                }
                if (data.hasExtra("port")){
                    SERVER_PORT = data.getStringExtra("port");
                    Singleton.getInstanceBLE(this).setPort(SERVER_PORT);

                    Log.d("arc", SERVER_PORT);
                }else{
                    connectionCont = false;
                }

                if (data.hasExtra("b1")){
                    Singleton.getInstanceBLE(this).setBeacon1(data.getStringExtra("b1"));
                    Log.d("arc", "b1 found");
                }
                if (data.hasExtra("b2")){
                    Singleton.getInstanceBLE(this).setBeacon2(data.getStringExtra("b2"));
                    Log.d("arc", "b2 found");
                }


                if (data.hasExtra("port")){
                    SERVER_PORT = data.getStringExtra("port");
                    Log.d("arc", SERVER_PORT);
                }
                if (connectionCont){
                    // Start the TCP connection in a new thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startTcpConnection(SERVER_IP, SERVER_PORT);
                        }
                    }).start();

                }


            }
        }

    }

    private void sendMessage(final String message) {
        // Send messages on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (out != null) {
                    String messageWithHeader = String.format("%-10s", message.length()) + message;
                    out.print(messageWithHeader);
                    out.flush();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false; // Stop the listening thread
        // Stop the receiving thread
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (receiveThread != null) {
            try {
                receiveThread.join(); // Wait for the receiving thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clearAppCache();
        super.onDestroy();
        if (bluetoothEMS.isConnected()){
            bluetoothEMS.disconnect();
        }

    }

    public void clearAppCache() {
        try {
            File cacheDir = getCacheDir();
            deleteDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
