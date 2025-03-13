package com.example.myactualsocket3_11;

import android.app.Activity;

import java.net.Socket;

public class Singleton {

    private static Singleton instance;
    private Bluetooth ble;

    private Socket sock;
    String Beacon1;
    String Beacon2;
    String address = "IDK";
    String port ="NOT SURE";


    private int num;

    private String what = "ble";

    private Singleton(Activity act, String what) {
        if (what.equals("ble")){
            ble = new Bluetooth(act); // Initialize your non-serializable object here
        }
        this.what  = what;
    }
    public void setSock(Socket socket) {
        this.sock = socket;
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBeacons(String b1, String b2) {
        this.Beacon1 = b1;
        this.Beacon2 = b2;
    }

    public void setBeacon1(String beacon1) {
        Beacon1 = beacon1;
    }

    public void setBeacon2(String beacon2) {
        Beacon2 = beacon2;
    }

    public String getBeacon1() {
        return Beacon1;
    }

    public String getBeacon2() {
        return Beacon2;
    }

    public static Singleton getInstanceBLE(Activity act) {
        if (instance == null) {
            instance = new Singleton(act, "ble");
        }
        return instance;
    }


    public Socket getSock() {
        return sock;
    }

    public Bluetooth getMyBLEObject() {
        return ble;
    }



}
