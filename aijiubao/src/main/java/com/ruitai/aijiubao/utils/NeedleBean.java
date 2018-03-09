package com.ruitai.aijiubao.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by static on 2017/9/26.
 */

public class NeedleBean {
    public boolean connStatus;
    public boolean enable;

    public BluetoothGatt bluetoothGatt;

    public int id;
    public String deviceAddress;


    public boolean isWorking;
    public int time;
    public int temperature;
    public int hopeTemperature;
    public int interval;
    public int deviceId;
}
