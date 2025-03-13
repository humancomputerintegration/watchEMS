package com.example.drummingapp;

import android.app.Activity;
import android.content.Context;

public class Singleton {

    private static Singleton instance;
    private Bluetooth ble;

    private Singleton(Activity act) {
        ble = new Bluetooth(act); // Initialize your non-serializable object here
    }

    public static Singleton getInstance(Activity act) {
        if (instance == null) {
            instance = new Singleton(act);
        }
        return instance;
    }

    public Bluetooth getMyObject() {
        return ble;
    }

}
