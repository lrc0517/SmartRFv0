package com.aijiubao.factorytests.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;


import com.aijiubao.factorytests.utils.BltConstants;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by static on 2017/10/9.
 */

public class BltConn {

    private static final String TAG = "BltConn";

    private final Context mContext;
    private final Timer mConnTimer;
    private final Timer mWorkTimer;
    private boolean mScanning;
    private BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;


    private ArrayMap<String, Integer> mBltAddressContainer;
    private ArrayMap<Integer, BltNeedle> mNeedleContainer;

    public BltConn(Context context, Handler handler, BluetoothAdapter bluetoothAdapter, ArrayMap<String, Integer> bltAddressContainer, ArrayMap<Integer, BltNeedle> needleContainer) {
        this.mContext = context;
        this.mBluetoothAdapter = bluetoothAdapter;
        this.mBltAddressContainer = bltAddressContainer;
        this.mNeedleContainer = needleContainer;
        this.mHandler = handler;

        scanLeDevice(true);
        mConnTimer = new Timer();
        mWorkTimer = new Timer();
        /*mConnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                scanLeDevice(false);
                doConnect();
            }
        }, 5000);*/
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void doConnect() {
        for (String key : mBltAddressContainer.keySet()) {
            Log.e(TAG, "doConnect key = " + key);
            BltNeedle bltNeedle = new BltNeedle(mContext, mHandler, mBluetoothAdapter, key);
            mNeedleContainer.put(mBltAddressContainer.get(key), bltNeedle);
        }

        //START_WORK
        mWorkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                doWork();
            }
        }, 3000,3000);
    }

    private void doDisConnect() {

        for (Integer key : mNeedleContainer.keySet()) {
            BltNeedle bltNeedle = mNeedleContainer.get(key);
            if (bltNeedle != null && bltNeedle.mNeedleBean != null) {
                Log.e(TAG, "doDisConnect");
                bltNeedle.turnOffDivices();
                bltNeedle.disconnect();
            }
        }
    }

    private void doTurnOff() {

        for (Integer key : mNeedleContainer.keySet()) {
            BltNeedle bltNeedle = mNeedleContainer.get(key);
            if (bltNeedle != null && bltNeedle.mNeedleBean != null) {
                Log.e(TAG, "doTurnOff");
                bltNeedle.turnOffDivices();
                //bltNeedle.disconnect();
            }
        }
    }

    public void doWork() {

        for (Integer key : mNeedleContainer.keySet()) {
            BltNeedle bltNeedle = mNeedleContainer.get(key);
            if (bltNeedle != null && bltNeedle.mNeedleBean != null) {
                Log.e(TAG, "startWork");
                bltNeedle.startWork();
            }
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (mBltAddressContainer.size() >= 10) {
                scanLeDevice(false);
                doConnect();
            }

            String bluetoothAddress = device.getAddress();
            String name = device.getName();
            Log.e(TAG, "name = " + name);

            if (BltConstants.SMART_BLT_NAME.equals(name) && !mBltAddressContainer.containsKey(bluetoothAddress)) {
                mBltAddressContainer.put(bluetoothAddress, mBltAddressContainer.size() + 1);
            }
        }
    };


    //+==============================================+//


    public void clearData() {
        doDisConnect();
        mBltAddressContainer.clear();
        mNeedleContainer.clear();
    }

    public void turnOff() {
        doTurnOff();
       // mBltAddressContainer.clear();
       // mNeedleContainer.clear();
    }


    public void reflash() {
        /*try {
            mWorkTimer.cancel();
        }catch (Exception e){
            Log.e(TAG,"Work Timer cancel Error -->"+e);
        }*/

        Log.e(TAG, "reflash");
        scanLeDevice(true);
        mConnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = BltConstants.MSG_HANDLER_ON_BLT_STOP_SCAN;
                mHandler.sendMessage(msg);
                scanLeDevice(false);
                doConnect();
            }
        }, 6000);
    }
}
