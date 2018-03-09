package com.ruitai.aijiubao.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ruitai.aijiubao.R;

/**
 * Created by static on 2017/9/29.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkBLT();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.enable()) {
                Toast.makeText(this, R.string.do_open_bluetooth, Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        }
    }


    protected void checkBLT() {
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Log.i(TAG, "initialize Bluetooth, has BLE system");
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.i(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
        }



    }
}
