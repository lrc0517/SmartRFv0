package com.AmoSmartRF.bluetooth.le;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ruitai.qab.bl.R;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static Context mContext;

    private final String ACTION_NAME_RSSI = "xwliu_RSSI"; // 其他文件广播的定义必须一致
    private final String ACTION_CONNECT = "xwliu_CONNECT";    // 其他文件广播的定义必须一致
    private final String APP_VER = "xwliuSmartRF蓝牙APP v1.2 20161203";    // 其他文件广播的定义必须一致

    private ToggleButton mBtnMada;
    private ToggleButton mBtnLed;
    private ToggleButton mBtnHot;
    private CenterDialog mDialog;
    private WirteNumberDialog mWirteDialog;
    private int mId;

    private final static int SystemStateOff = 0;
    private final static int SystemStateDisconnect = 1;
    private final static int SystemStateWait = 2;
    private final static int SystemStateWork = 3;
    final static int DATA_LEN = 12;

    static byte[] ReceiveBuf = new byte[DATA_LEN];//byte 最大0xFFFFFFFF
    static byte[] SendBuf = new byte[DATA_LEN];
    static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10010:
                    setWriteSuccess(true);
                    break;
            }
        }


    };
    private static Button mBtnJin;
    private static Button mBtnMu;
    private static Button mBtnShui;
    private static Button mBtnHuo;
    private static Button mBtnTu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplication();
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "---> onStop");
        SetTemperatureNotifyUpdate(false);
        bExitThread = true;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onStop();
    }


    private void initListener() {
        mBtnMada.setOnCheckedChangeListener(mOnCheckChangedListener);
        mBtnLed.setOnCheckedChangeListener(mOnCheckChangedListener);
        mBtnHot.setOnCheckedChangeListener(mOnCheckChangedListener);

        mBtnJin.setOnClickListener(mOnClickListener);
        mBtnMu.setOnClickListener(mOnClickListener);
        mBtnShui.setOnClickListener(mOnClickListener);
        mBtnHuo.setOnClickListener(mOnClickListener);
        mBtnTu.setOnClickListener(mOnClickListener);

        new SendMessageThread().start();
    }

    private void initView() {
        mDialog = new CenterDialog(this, R.style.dialog_custom);
        mDialog.setOnCenterDialogSelected(mOnCenterDialogSelected);
        mWirteDialog = new WirteNumberDialog(this, R.style.dialog_custom);
        mWirteDialog.setOnWirteNumberDialogClick(mOnWirteNumberDialogClick);

        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.width = 500; // 宽度
        lp.height = 350; // 高度
        dialogWindow.setAttributes(lp);

        mBtnMada = (ToggleButton) findViewById(R.id.btn_mada);
        mBtnLed = (ToggleButton) findViewById(R.id.btn_led);
        mBtnHot = (ToggleButton) findViewById(R.id.btn_hot);

        mBtnJin = (Button) findViewById(R.id.btn_jin);
        mBtnMu = (Button) findViewById(R.id.btn_mu);
        mBtnShui = (Button) findViewById(R.id.btn_shui);
        mBtnHuo = (Button) findViewById(R.id.btn_huo);
        mBtnTu = (Button) findViewById(R.id.btn_tu);
    }

    private void initData() {
        SendBuf[0] = (byte) 0xA5;
        ReceiveBuf[0] = (byte) 0x5A;
        registerBoradcastReceiver();
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME_RSSI);
        myIntentFilter.addAction(ACTION_CONNECT);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }




    private void showAlertDialog(int itemId, boolean isChecked) {
        mId = itemId;
        switch (itemId) {
            case R.id.btn_mada:
                motor_state = isChecked;
                break;
            case R.id.btn_led:
                led1_state = isChecked? 1 :0 ;
                break;
            case R.id.btn_hot:
                hot_state = isChecked;
                break;
            default:
                break;
        }
        SendData();
        mDialog.show();

    }

    private void dismissAlertDialog(boolean isOk) {
        mDialog.dismiss();
        switch (mId) {
            case R.id.btn_mada:
                mBtnMada.setBackgroundResource(R.drawable.bg_btn_section_pressed_selector);
                mBtnMada.setSelected(isOk);
                break;
            case R.id.btn_led:
                mBtnLed.setBackgroundResource(R.drawable.bg_btn_section_pressed_selector);
                mBtnLed.setSelected(isOk);
                break;
            case R.id.btn_hot:
                mBtnHot.setBackgroundResource(R.drawable.bg_btn_section_pressed_selector);
                mBtnHot.setSelected(isOk);
                break;
            default:
                break;
        }
    }

//LXQ send number data
    private static int deviceID = 0;
    private void showWriteNumberDialog(int id) {
        mWirteDialog.show();
        Log.e(TAG, "showWriteNumberDialog: id = " +id );
        switch (id) {

            case R.id.btn_jin:
                deviceID = 1;
                break;
            case R.id.btn_mu:
                deviceID = 2;
                break;
            case R.id.btn_shui:
                deviceID = 3;
                break;
            case R.id.btn_huo:
                deviceID = 4;
                break;
            case R.id.btn_tu:
                deviceID = 5;
                break;
        }
        Log.e(TAG, "deviceID = " +deviceID);

    }

    private static void setWriteSuccess(boolean isSuccessed) {
        switch (deviceID) {
            case 0:
                mBtnJin.setSelected(false);
                mBtnMu.setSelected(false);
                mBtnShui.setSelected(false);
                mBtnHuo.setSelected(false);
                mBtnTu.setSelected(false);
                break;
            case 1:
                mBtnJin.setSelected(isSuccessed);
                mBtnMu.setSelected(false);
                mBtnShui.setSelected(false);
                mBtnHuo.setSelected(false);
                mBtnTu.setSelected(false);
                break;
            case 2:
                mBtnJin.setSelected(false);
                mBtnMu.setSelected(isSuccessed);
                mBtnShui.setSelected(false);
                mBtnHuo.setSelected(false);
                mBtnTu.setSelected(false);
                break;
            case 3:
                mBtnJin.setSelected(false);
                mBtnMu.setSelected(false);
                mBtnShui.setSelected(isSuccessed);
                mBtnHuo.setSelected(false);
                mBtnTu.setSelected(false);
                break;
            case 4:
                mBtnJin.setSelected(false);
                mBtnMu.setSelected(false);
                mBtnShui.setSelected(false);
                mBtnHuo.setSelected(isSuccessed);
                mBtnTu.setSelected(false);
                break;
            case 5:
                mBtnJin.setSelected(false);
                mBtnMu.setSelected(false);
                mBtnShui.setSelected(false);
                mBtnHuo.setSelected(false);
                mBtnTu.setSelected(isSuccessed);
                break;

        }
    }

    private void SendData() {

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
        SendBuf[6] = (byte) max_temp_h_state;
        SendBuf[7] = (byte) 0;//小数部分不让设置
        SendBuf[8] = (byte) system_state;

        SendBuf[9] = (byte) work_time;
        SendBuf[10] = (byte) massage_time;
        SendBuf[11] = (byte) device_id;
        int j = 0;
        for (j = 0; j < DATA_LEN; j++) {
            //Log.i(TAG, "char6 SendBuf[" + j + "]=0x" + Integer.toHexString(SendBuf[j]).toUpperCase());
            Log.e(TAG, "char6 SendBuf[" + j + "]=0x" + Integer.toHexString((SendBuf[j] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase());
        }
        DeviceScanActivity.WriteCharX(DeviceScanActivity.gattCharacteristic_char6, SendBuf);
    }

    private void SetTemperatureNotifyUpdate(boolean enable) {
        DeviceScanActivity.setCharacteristicNotification(
                DeviceScanActivity.gattCharacteristic_char6, enable);
    }

    /**
     * ==============================================================================================
     */

    static int led0_state = 0;    //data1 bit0-bit3 0xN0-OFF 0xN1-ON 0xN2-闪烁
    static int led1_state = 0;    //data1 bit4-bit7 0x0N-OFF 0x1N-ON 0x2N-闪烁
    static boolean hot_state = false;     //data2 bit0
    static boolean motor_state = false;   //data2 bit1
    static int battery_state = 2;    //data3 bit0-bit7
    static int cur_temp_h_state = 11;    //data4 当前温度整数部分
    static int cur_temp_l_state = 22;    //data5 当前温度小数部分
    static int max_temp_h_state = 45;    //data6 最大温度整数部分
    static int max_temp_l_state = 44;    //data7 最大温度小数部分
    static int system_state = SystemStateOff;    //data8 当前系统状态

    static int work_time = 30;    //data9 工作时间（时长）
    static int massage_time = 5;    //data10 按摩间隔时间（时长）
    static int device_id = 0;    //data9 设备ID

    // 设备名称
    static boolean DeviceNameFlag = false;
    static String DeviceName = null;


    public static synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // Log.i(TAG, "onCharacteristicRead str = " + str);

        if (DeviceScanActivity.gattCharacteristic_char6.equals(characteristic)) {
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

                if (device_id!=0 && device_id == deviceID){
                    Log.e(TAG,"device_id == deviceID");
                    Message msg = new Message();
                    msg.what = 10010;
                    mHandler.sendMessage(msg);
                }
            }

        } else if (DeviceScanActivity.gattCharacteristic_char7.equals(characteristic)) {
            int i = characteristic.getValue().length;
            DeviceName = Utils.bytesToString(characteristic.getValue());
            DeviceNameFlag = true;
            Log.i(TAG, "DeviceName = " + DeviceName);
        } else {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {

                Log.i(TAG, " DeviceName:" + DeviceName + " led0_state:" + led0_state + " led1_state:" + led1_state + " hot_state:" + hot_state);
                // 更新设备名称
                if (DeviceNameFlag == true) {
                    DeviceNameFlag = false;
                    //EditTextDeviceName.setText(DeviceName);
                }
            }
        });
    }


    /**
     * ===== rssi BroadcastReceiver=========================================================================================
     */

    static final int rssibufferSize = 10;
    int[] rssibuffer = new int[rssibufferSize];
    int rssibufferIndex = 0;
    boolean rssiUsedFalg = false;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ACTION_NAME_RSSI)) {
                int rssi = intent.getIntExtra("RSSI", 0);
                int rssi_avg = 0;
                int distance_cm_min = 10; // 距离cm -30dbm
                int distance_cm_max_near = 1500; // 距离cm -90dbm
                int distance_cm_max_middle = 5000; // 距离cm -90dbm
                int distance_cm_max_far = 10000; // 距离cm -90dbm
                int near = -72;
                int middle = -80;
                int far = -88;
                double distance = 0.0f;

                if (true) {
                    rssibuffer[rssibufferIndex] = rssi;
                    rssibufferIndex++;

                    if (rssibufferIndex == rssibufferSize)
                        rssiUsedFalg = true;

                    rssibufferIndex = rssibufferIndex % rssibufferSize;

                    if (rssiUsedFalg == true) {
                        int rssi_sum = 0;
                        for (int i = 0; i < rssibufferSize; i++) {
                            rssi_sum += rssibuffer[i];
                        }

                        rssi_avg = rssi_sum / rssibufferSize;

                        if (-rssi_avg < 35)
                            rssi_avg = -35;

                        if (-rssi_avg < -near) {
                            distance = distance_cm_min
                                    + ((-rssi_avg - 35) / (double) (-near - 35))
                                    * distance_cm_max_near;
                        } else if (-rssi_avg < -middle) {
                            distance = distance_cm_min
                                    + ((-rssi_avg - 35) / (double) (-middle - 35))
                                    * distance_cm_max_middle;
                        } else {
                            distance = distance_cm_min
                                    + ((-rssi_avg - 35) / (double) (-far - 35))
                                    * distance_cm_max_far;
                        }
                    }
                }

                getActionBar().setTitle("RSSI: " + rssi_avg + " dbm" + ", " + "距离: " + (int) distance + " cm");
            } else if (action.equals(ACTION_CONNECT)) {
                int status = intent.getIntExtra("CONNECT_STATUC", 0);

                Log.i(TAG, "ACTION_CONNECT status = " + status);

                if (status == 0) {
                    getActionBar().setTitle("已断开连接，请退出本界面后重新连接");
                } else {
                }
            }
        }
    };


    /**
     * ==============================================================================================
     */
    // 退出线程标记
    boolean bExitThread = false;

    public class SendMessageThread extends Thread {
        public void run() {
            int count = 0;
            int count_start_read = 10;


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UpdateDeviceName();
            while (!Thread.currentThread().isInterrupted()) {
                if (bExitThread) {
                    break;
                }
                if (count_start_read == 0) {   // 每隔一秒钟读一次（count=10）
                    if (count == 20) {//调试暂时改成2秒读一次（count=20）
                        count = 0;
                        Log.i(TAG, "MyThread-------------------DeviceScanActivity.ReadCharY");
                        DeviceScanActivity.ReadCharX(DeviceScanActivity.gattCharacteristic_char6);
                    }

                    count++;
                } else {
                    count_start_read--;
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    Log.e(TAG, "Thread interrupted error-->" + e);
                }
            }
            Log.i(TAG, "MyThread out...");
        }
    }

    private void UpdateDeviceName() {
        DeviceScanActivity
                .ReadCharX(DeviceScanActivity.gattCharacteristic_char7);
    }


    /**
     * ==============================================================================================
     */

    private CompoundButton.OnCheckedChangeListener mOnCheckChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            showAlertDialog(compoundButton.getId(), b);
        }
    };


    private CenterDialog.OnCenterDialogSelected mOnCenterDialogSelected = new CenterDialog.OnCenterDialogSelected() {

        @Override
        public void onSelected(boolean isOk) {
            dismissAlertDialog(isOk);
        }

    };

    private WirteNumberDialog.OnWirteNumberDialogClick mOnWirteNumberDialogClick = new WirteNumberDialog.OnWirteNumberDialogClick() {
        @Override
        public void onConfirm() {
            device_id = deviceID;
            SendData();
        }

        @Override
        public void onCancel() {
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showWriteNumberDialog(view.getId());
        }
    };

}
