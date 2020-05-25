/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.AmoSmartRF.bluetooth.le;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import com.AmoSmartRF.bluetooth.le.iBeaconClass;
import com.AmoSmartRF.bluetooth.le.BluetoothLeClass.OnDataAvailableListener;
import com.AmoSmartRF.bluetooth.le.BluetoothLeClass.OnServiceDiscoverListener;
import com.AmoSmartRF.bluetooth.le.iBeaconClass.iBeacon;
import com.ruitai.qab.bl.R;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
// public class DeviceScanActivity extends ListActivity implements
// View.OnClickListener
public class DeviceScanActivity extends ListActivity {
    private final static String TAG = "DeviceScanActivity";// DeviceScanActivity.class.getSimpleName();
    public static final int REFRESH = 0x000001;
    public static final int START = 0x000010;
    private final static int REQUEST_CODE = 1;

    public static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public static String UUID_CHAR1 = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR2 = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR3 = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR4 = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR5 = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR7 = "0000fff7-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR8 = "0000fff8-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR9 = "0000fff9-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHARA = "0000fffa-0000-1000-8000-00805f9b34fb";

    // public static String UUID_HERATRATE =
    // "00002a37-0000-1000-8000-00805f9b34fb";
    // public static String UUID_TEMPERATURE =
    // "00002a1c-0000-1000-8000-00805f9b34fb";

    static BluetoothGattCharacteristic gattCharacteristic_keydata = null;

    static BluetoothGattCharacteristic gattCharacteristic_char1 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char2 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char3 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char4 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char5 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char6 = null;//温度湿度
    static BluetoothGattCharacteristic gattCharacteristic_char7 = null;//设备名称
    static BluetoothGattCharacteristic gattCharacteristic_char8 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char9 = null;//ADC电压值
    static BluetoothGattCharacteristic gattCharacteristic_charA = null;//通讯协议数据

    // static BluetoothGattCharacteristic gattCharacteristic_heartrate = null;


    private LeDeviceListAdapter mLeDeviceListAdapter = null;
    // 搜索BLE终端
    private BluetoothAdapter mBluetoothAdapter;
    // 读写BLE终端
    static private BluetoothLeClass mBLE;
    public String bluetoothAddress;
    static private byte writeValue_char1 = 0;
    private boolean mScanning;
    //private Handler mHandler = null;
    private MyThread mythread = null;
    private ImageView start_bg;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device);
        start_bg = (ImageView) findViewById(R.id.start_bg);
        // getActionBar().setTitle(R.string.title_devices);

        getActionBar().setTitle("正在扫描设备 ....");
        // findViewById(R.id.button_new_encoder).setOnClickListener(this);

        // Use this check to determine whether BLE is supported on the device.
        // Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Log.i(TAG, "initialize Bluetooth, has BLE system");
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.i(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
        }

        //蓝牙
        mBluetoothAdapter.enable();
        Log.i(TAG, "mBluetoothAdapter.enable");


        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        Log.i(TAG, "mBLE = e" + mBLE);

        // 发现BLE终端的Service时回
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);

        // 收到BLE终端数据交互的事
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
        mHandler.sendEmptyMessageDelayed(START, 3000);
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == START) {
                start_bg.setVisibility(View.GONE);
            }
        }

        ;
    };

    static public void WriteCharX(
            BluetoothGattCharacteristic GattCharacteristic, byte[] writeValue) {
        Log.i(TAG, "writeCharX = " + GattCharacteristic);
        if (GattCharacteristic != null) {
            GattCharacteristic.setValue(writeValue);
            mBLE.writeCharacteristic(GattCharacteristic);
        }
    }

    static public void ReadCharX(BluetoothGattCharacteristic GattCharacteristic) {
//		Log.i(TAG, "GattCharacteristic = " + GattCharacteristic);
        if (GattCharacteristic != null) {
            mBLE.readCharacteristic(GattCharacteristic);
        }
    }

    ;

    static public void setCharacteristicNotification(
            BluetoothGattCharacteristic gattCharacteristic, boolean enabled) {
//		Log.i(TAG, "gattCharacteristic = " + gattCharacteristic);
        if (gattCharacteristic != null) {
            mBLE.setCharacteristicNotification(gattCharacteristic, enabled);
        }
    }

    ;

    public class MyThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                Message msg = new Message();
                msg.what = REFRESH;
                mHandler.sendMessage(msg);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void DisplayStart() {
        Log.i(TAG, "DisplayStart+++");

        if (mythread == null) {
            mythread = new MyThread();
            mythread.start();
            // mythread.setThread(true);
        } else {
            // mythread.setThread(true);
        }
    }

    public void DisplayStop() {
        if (mythread != null) {
            // mythread.setThread(false);
            // delay(3000);
        }
        Log.i(TAG, "DisplayStop---");
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "---> onResume");
        super.onResume();
        mBLE.close();
        // Initializes list view adapter.
        //mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        //setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "---> onPause");
        super.onPause();
        // scanLeDevice(false);
        // mLeDeviceListAdapter.clear();
        // mBLE.disconnect();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "---> onStop");
        super.onStop();
        DisplayStop();
        // mBLE.close();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "---> onDestroy");
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        Log.e(TAG, "start onDestroy~~~");
        scanLeDevice(false);
        mBLE.disconnect();
        mBLE.close();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //final iBeacon device = mLeDeviceListAdapter.getDevice(position);
        /*if (device == null)
			return;
		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}

		Log.i(TAG, "mBluetoothAdapter.enable");
		bluetoothAddress = device.bluetoothAddress;
		boolean bRet = mBLE.connect(device.bluetoothAddress);

		Log.i(TAG, "connect bRet = " + bRet);

		Toast toast = Toast.makeText(getApplicationContext(), "正在连接设备并获取服务中",
				1500);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();*/
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    /**
     * 搜索到BLE终端服务的事�?
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }

    };

    /**
     * 收到BLE终端数据交互的事�?
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener() {
        /**
         * BLE终端数据被读的事�?
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            // 执行 mBLE.readCharacteristic(gattCharacteristic); 后就会收到数�? if
            // (status == BluetoothGatt.GATT_SUCCESS)
            Log.e(TAG,
                    "onCharRead " + gatt.getDevice().getName() + " read "
                            + characteristic.getUuid().toString() + " -> 0x"
                            + Utils.bytesToHexString(characteristic.getValue()));

            //StartActivity.onCharacteristicRead(gatt, characteristic);
            MainActivity.onCharacteristicRead(gatt, characteristic);
        }

        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharWrite " + gatt.getDevice().getName() + " write "
                    + characteristic.getUuid().toString() + " -> "
                    + new String(characteristic.getValue()));

           // StartActivity.onCharacteristicRead(gatt, characteristic);
           //  MainActivity.onCharacteristicRead(gatt, characteristic);
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mLeDeviceListAdapter.addDevice(ibeacon);
                    //mLeDeviceListAdapter.notifyDataSetChanged();


                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }

                    Log.i(TAG, "mBluetoothAdapter.enable");
                    bluetoothAddress = device.getAddress();
                    boolean bRet = mBLE.connect(bluetoothAddress);

                    Log.i(TAG, "connect bRet = " + bRet);

                    Toast toast = Toast.makeText(getApplicationContext(), "正在连接设备并获取服务中", 1500);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });

            // rssi
            Log.i(TAG, "rssi = " + rssi);
            Log.i(TAG, "mac = " + bluetoothAddress);
            Log.i(TAG, "scanRecord.length = " + scanRecord.length);
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        for (BluetoothGattService gattService : gattServices) {
            // -----Service的字段信�?----//
            int type = gattService.getType();
            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:"
                    + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());

            // -----Characteristics的字段信�?----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG,
                        "---->char permission:"
                                + Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                Log.e(TAG,
                        "---->char property:"
                                + Utils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG, "---->char value:" + new String(data));
                }

                // if(gattCharacteristic.getUuid().toString().equals(UUID_CHAR6)){
                // // 把char1 保存起来�?以方便后面读写数据时使用
                // gattCharacteristic_char6 = gattCharacteristic;
                // mBLE.setCharacteristicNotification(gattCharacteristic, true);
                // Log.i(TAG, "+++++++++UUID_CHAR6");
                // }
                //
                // if(gattCharacteristic.getUuid().toString().equals(UUID_HERATRATE)){
                // // 把heartrate 保存起来�?以方便后面读写数据时使用
                // gattCharacteristic_heartrate = gattCharacteristic;
                // //接受Characteristic被写的�?�?收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                // mBLE.setCharacteristicNotification(gattCharacteristic, true);
                // Log.i(TAG, "+++++++++UUID_HERATRATE");
                // }
                //
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                    // 把heartrate 保存起来�?以方便后面读写数据时使用
                    gattCharacteristic_keydata = gattCharacteristic;
                    //接受Characteristic被写的�?�?收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    Log.i(TAG, "+++++++++UUID_KEY_DATA");
                }

                // if(gattCharacteristic.getUuid().toString().equals(UUID_TEMPERATURE)){
                // // 把heartrate 保存起来�?以方便后面读写数据时使用
                // //接受Characteristic被写的�?�?收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                // mBLE.setCharacteristicNotification(gattCharacteristic, true);
                // Log.i(TAG, "+++++++++UUID_TEMPERATURE");
                // }
                //
                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR1)) {
                    gattCharacteristic_char1 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR2)) {
                    gattCharacteristic_char2 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR3)) {
                    gattCharacteristic_char3 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR4)) {
                    gattCharacteristic_char4 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR5)) {
                    gattCharacteristic_char5 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR6)) {
                    gattCharacteristic_char6 = gattCharacteristic;
                    // mBLE.setCharacteristicNotification(gattCharacteristic,
                    // true);
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR7)) {
                    gattCharacteristic_char7 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR8)) {
                    gattCharacteristic_char8 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR9)) {
                    gattCharacteristic_char9 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHARA)) {
                    gattCharacteristic_charA = gattCharacteristic;
                    Log.e(TAG, "---->gattCharacteristic_charA---->gattCharacteristic_charA");
                }
                // -----Descriptors的字段信�?----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG,
                            "-------->desc permission:"
                                    + Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }
            }
        }//

        // 打开新窗体   StartActivity
        Intent intent = new Intent();
        //intent.setClass(DeviceScanActivity.this, StartActivity.class);
        intent.setClass(DeviceScanActivity.this, MainActivity.class);
        intent.putExtra("mac_addr", bluetoothAddress);
        startActivityForResult(intent, REQUEST_CODE);
    }
}
