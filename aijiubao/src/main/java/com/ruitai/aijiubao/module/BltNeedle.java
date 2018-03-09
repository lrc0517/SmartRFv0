package com.ruitai.aijiubao.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ruitai.aijiubao.utils.BltConstants;
import com.ruitai.aijiubao.utils.NeedleBean;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by static on 2017/9/29.
 */

public class BltNeedle {

    private static final String TAG = "BltNeedle";
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;

    public NeedleBean mNeedleBean = new NeedleBean();

    public BltNeedle(Context context, Handler handler, BluetoothAdapter bluetoothAdapter, String address) {
        this.mContext = context;
        this.mBluetoothAdapter = bluetoothAdapter;
        this.mBluetoothDeviceAddress = address;
        this.mHandler = handler;

        connect(mBluetoothDeviceAddress);
    }


    //+=========================================================================+//

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG,
                    "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.e(TAG,"mBluetoothGatt = "+mBluetoothGatt);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection.
     * The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    //+=========================================================================+//
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.w(TAG, "BluetoothGattCallback onConnectionStateChange status = " + status + ", newState = " + newState);
            Intent mIntent = new Intent(BltConstants.ACTION_BROADCAST_CONNECT);
            rssiTimer = new Timer();

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                gatt.discoverServices();

                // rssiTimer.schedule(task, delay, period);
                mReadDataTask = new BltReadDataTimerTask(gatt);
                rssiTimer.schedule(mReadDataTask, 160, 1000);
                mIntent.putExtra("CONNECT_STATUC", 1);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mIntent.putExtra("CONNECT_STATUC", 0);
                rssiTimer.cancel();
            }
            mContext.sendBroadcast(mIntent);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.w(TAG, "BluetoothGattCallback onServicesDiscovered status = " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(gatt, gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.w(TAG, "BluetoothGattCallback onCharacteristicRead status = " + status);
            onBltCharacteristicRead(gatt,characteristic,status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.w(TAG, "BluetoothGattCallback onCharacteristicWrite status = " + status);
            onBltCharacteristicRead(gatt,characteristic,status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.w(TAG, "BluetoothGattCallback onCharacteristicChanged");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.w(TAG, "BluetoothGattCallback onReadRemoteRssi status = " + status);
        }
    };

    //+====Timer==========================================================================+//
    Timer rssiTimer;
    private BltReadDataTimerTask mReadDataTask;

    public class BltReadDataTimerTask extends TimerTask {

        private BluetoothGatt mGatt;

        public BltReadDataTimerTask(BluetoothGatt gatt) {
            mGatt = gatt;
        }

        @Override
        public void run() {
            if (mGatt != null) {
                if (gattCharacteristic_char6!=null)
                mGatt.readCharacteristic(gattCharacteristic_char6);
            }
        }
    }

    //+====Data==========================================================================+//
    public BluetoothGattCharacteristic gattCharacteristic_keydata;
    public BluetoothGattCharacteristic gattCharacteristic_char1;
    public BluetoothGattCharacteristic gattCharacteristic_char2;
    public BluetoothGattCharacteristic gattCharacteristic_char3;
    public BluetoothGattCharacteristic gattCharacteristic_char4;
    public BluetoothGattCharacteristic gattCharacteristic_char5;
    public BluetoothGattCharacteristic gattCharacteristic_char6;//温度湿度
    public BluetoothGattCharacteristic gattCharacteristic_char7;//设备名称
    public BluetoothGattCharacteristic gattCharacteristic_char8;
    public BluetoothGattCharacteristic gattCharacteristic_char9;//ADC电压值
    public BluetoothGattCharacteristic gattCharacteristic_charA;//通讯协议数据


    private void displayGattServices(BluetoothGatt gatt, List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        Log.e(TAG,"displayGattServices init protocol");

        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                byte[] data = gattCharacteristic.getValue();

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_KEY_DATA)) {
                    gattCharacteristic_keydata = gattCharacteristic;
                    setCharacteristicNotification(gatt, gattCharacteristic, true);
                }


                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR1)) {
                    gattCharacteristic_char1 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR2)) {
                    gattCharacteristic_char2 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR3)) {
                    gattCharacteristic_char3 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR4)) {
                    gattCharacteristic_char4 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR5)) {
                    gattCharacteristic_char5 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR6)) {
                    gattCharacteristic_char6 = gattCharacteristic;

                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR7)) {
                    gattCharacteristic_char7 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR8)) {
                    gattCharacteristic_char8 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHAR9)) {
                    gattCharacteristic_char9 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(BltConstants.UUID_CHARA)) {
                    gattCharacteristic_charA = gattCharacteristic;

                }
            }
        }
        if (gatt != null) {
            Log.e(TAG,"displayGattServices ReadData");
            ReadCharX(gatt,gattCharacteristic_char6);
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (enabled == true) {
            Log.i(TAG, "Enable Notification");
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(BltConstants.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        } else {
            Log.i(TAG, "Disable Notification");
            gatt.setCharacteristicNotification(characteristic, false);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BltConstants.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
        //gatt.setCharacteristicNotification(characteristic,enabled);
    }


    public void WriteCharX(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, byte[] writeValue) {
        Log.i(TAG, "writeCharX = " + characteristic);
        if (characteristic != null) {
            characteristic.setValue(writeValue);
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }





    private boolean ReadCharX(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "GattCharacteristic = " + characteristic);
        if (characteristic != null) {
           boolean isGotten =  bluetoothGatt.readCharacteristic(characteristic);
           return isGotten;
        }else {
            return false;
        }
    }

    private final static int SystemStateOff = 0;
    private final static int SystemStateDisconnect = 1;
    private final static int SystemStateWait = 2;
    private final static int SystemStateWork = 3;
    final static int DATA_LEN = 12; // 9
    byte[] ReceiveBuf = new byte[DATA_LEN];//byte 最大0xFFFFFFFF
    byte[] SendBuf = new byte[DATA_LEN];
    int led0_state = 0;    //data1 bit0-bit3 0xN0-OFF 0xN1-ON 0xN2-闪烁
    int led1_state = 0;    //data1 bit4-bit7 0x0N-OFF 0x1N-ON 0x2N-闪烁
    boolean hot_state = false;     //data2 bit0
    boolean motor_state = false;   //data2 bit1
    int battery_state = 2;    //data3 bit0-bit7
    int cur_temp_h_state = 11;    //data4 当前温度整数部分
    int cur_temp_l_state = 22;    //data5 当前温度小数部分
    int max_temp_h_state = 45;    //data6 最大温度整数部分
    int max_temp_l_state = 44;    //data7 最大温度小数部分
    int system_state = SystemStateOff;    //data8 当前系统状态

    int work_time = 30;    //data9 工作时间（时长）
    int massage_time = 5;  //data10 按摩间隔时间（时长）
    int device_id = 0;     //data9 设备ID


    public void startWork(){
        hot_state = !hot_state;
        sendData();
    }

    int mWorkTime = 30;
    int mMaxTemperature = 45;
    int mMassageTime = 5;

    public void setHope(int workTime,int maxTemperature,int massageTime){
        mMaxTemperature = maxTemperature;
        mWorkTime = workTime;
        mMassageTime = massageTime;
        Log.e(TAG,"workTime = "+ workTime +",maxTemperature = " +maxTemperature +",massageTime = " +massageTime);
        sendData();
    }

    public void sendData() {
        SendBuf[0] = (byte) 0xA5;
        SendBuf[1] = (byte) (((led1_state & 0x0F) << 4) | (led0_state & 0x0F));
        if (hot_state) {
            SendBuf[2] |= 0x01;
        } else {
            SendBuf[2] &= ~0x01;
        }
        if (motor_state) {
            SendBuf[2] |= 0x02;
        } else {
            SendBuf[2] &= ~0x02;
        }
        SendBuf[3] = (byte) 0;//电量值不能设置
        SendBuf[4] = (byte) 0;//当前温度不能设置
        SendBuf[5] = (byte) 0;//当前温度不能设置
        SendBuf[6] = (byte) (mMaxTemperature==0?max_temp_h_state:mMaxTemperature);
        SendBuf[7] = (byte) 0;//小数部分不让设置
        SendBuf[8] = (byte) system_state;
        SendBuf[9] = (byte) (mWorkTime==0?work_time:mWorkTime);
        SendBuf[10] = (byte) (mMassageTime==0?massage_time:mMassageTime);
        SendBuf[11] = (byte) device_id;
        int j = 0;
        for (j = 0; j < DATA_LEN; j++) {
            //Log.i(TAG, "char6 SendBuf[" + j + "]=0x" + Integer.toHexString(SendBuf[j]).toUpperCase());
            Log.i(TAG, "char6 SendBuf[" + j + "]=0x" + Integer.toHexString((SendBuf[j] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase());
        }
        if (mBluetoothGatt != null)
            WriteCharX(mBluetoothGatt, gattCharacteristic_char6, SendBuf);
    }


    public  void onBltCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int readStatus) {


        if (characteristic == null)return;

        if (gattCharacteristic_char6.equals(characteristic)) {
            int i = characteristic.getValue().length;
            Log.i(TAG, "gattCharacteristic_char6 len = " + i);
            ReceiveBuf = characteristic.getValue();

            int j = 0;
            for (j = 0; j < i; j++) {
                //Log.i(TAG, "char6 ReceiveBuf[" + j + "]=0x" + Integer.toHexString(ReceiveBuf[j]&0xFF).toUpperCase());
                Log.i(TAG, "char6 ReceiveBuf[" + j + "]=0x" + Integer.toHexString((ReceiveBuf[j] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase());
            }
            if (ReceiveBuf[0] == 0x5A) {
                led0_state = (ReceiveBuf[1] & 0x0F);
                led1_state = (ReceiveBuf[1] >> 4);
                if ((ReceiveBuf[2] & 0x01) != 0) {
                    hot_state = true;
                } else {
                    hot_state = false;
                }
                if ((ReceiveBuf[2] & 0x02) != 0) {
                    motor_state = true;
                } else {
                    motor_state = false;
                }
                battery_state = (ReceiveBuf[3] & 0xFF);
                cur_temp_h_state = (ReceiveBuf[4] & 0xFF);
                cur_temp_l_state = (ReceiveBuf[5] & 0xFF);
                max_temp_h_state = (ReceiveBuf[6] & 0xFF);
                max_temp_l_state = (ReceiveBuf[7] & 0xFF);
                system_state = (ReceiveBuf[8] & 0xFF);

                work_time = (ReceiveBuf[9]&0xFF);
                massage_time = (ReceiveBuf[10]&0xFF);
                device_id = (ReceiveBuf[11]&0xFF);
            }

        } else if (gattCharacteristic_char7.equals(characteristic)) {
            int i = characteristic.getValue().length;
            //DeviceName = Utils.bytesToString(characteristic.getValue());
        }else {

        }


        /**
         * LXQ add datas
         */
        String address = gatt.getDevice().getAddress();
        Log.e(TAG, "device_id s = " + device_id);
        Log.e(TAG, "max_temp_h_state s = " + max_temp_h_state);
        Log.e(TAG, "massage_time s = " + massage_time);
        Log.e(TAG, "work_time s = " + work_time);
        mNeedleBean.enable =true;
        mNeedleBean.isWorking = hot_state;
        mNeedleBean.time = work_time;
        mNeedleBean.interval = massage_time;
        mNeedleBean.deviceAddress = address;
        mNeedleBean.temperature = cur_temp_h_state - 5;//FIXME 2017.12.29 modify
        mNeedleBean.hopeTemperature = max_temp_h_state;
        mNeedleBean.deviceId = device_id;
        Message msg = Message.obtain();  //从全局池中返回一个message实例，避免多次创建message（如new Message）
        msg.what=BltConstants.MSG_HANDLER_ON_BLT_READ;   //标志消息的标志
        mHandler.sendMessage(msg);
    }

    //+====Data==========================================================================+//


}
