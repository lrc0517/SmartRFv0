package com.AmoSmartRF.bluetooth.le;


import java.text.DecimalFormat;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ruitai.qab.bl.R;

public class StartActivity extends Activity implements OnClickListener {

    private final static String TAG = "StartActivity"; // StartActivity.class.getSimpleName();
    private final String ACTION_NAME_RSSI = "xwliu_RSSI"; // 其他文件广播的定义必须一致
    private final String ACTION_CONNECT = "xwliu_CONNECT";    // 其他文件广播的定义必须一致
    private final String APP_VER = "xwliuSmartRF蓝牙APP v1.2 20161203";    // 其他文件广播的定义必须一致
    final static int DATA_LEN = 12;
    // 根据rssi 值计算距离， 只是参考作用， 不准确---amomcu
    static final int rssibufferSize = 10;
    int[] rssibuffer = new int[rssibufferSize];
    int rssibufferIndex = 0;
    boolean rssiUsedFalg = false;


    static Handler mHandler = new Handler();



    private final static int SystemStateOff = 0;
    private final static int SystemStateDisconnect = 1;
    private final static int SystemStateWait = 2;
    private final static int SystemStateWork = 3;

    // 设备名称
    static boolean DeviceNameFlag = false;
    static String DeviceName = null;

    //通讯协议
    static byte[] ReceiveBuf = new byte[DATA_LEN];//byte 最大0xFFFFFFFF
    static byte[] SendBuf = new byte[DATA_LEN];

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


    //SystemStateOff = 0,//关闭
    //SystemStateDisconnect = 1,//断开连接
    //SystemStateWait = 2,//不工作
    //SystemStateWork = 3,//工作


    static EditText EditTextDeviceName = null;
    static CheckBox CheckBoxLed0State = null;
    static CheckBox CheckBoxLed1State = null;
    static CheckBox CheckBoxMotorState = null;
    static CheckBox CheckBoxHotState = null;

    static EditText EditTextMaxTemperatureH = null;
    static TextView TextViewMaxTemperatureH = null;
    static TextView TextViewCurTemperature = null;
    static TextView TextViewPower = null;
    static TextView TextViewPowerIcon = null;
    static Button ButtonSystemStateOff = null;
    static Button ButtonSystemStateDisconnect = null;

    // 退出线程标记
    boolean bExitThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        getActionBar().setTitle(APP_VER);

        EditTextDeviceName = (EditText) findViewById(R.id.idEditTextDeviceName);
        CheckBoxLed0State = (CheckBox)findViewById(R.id.idCheckBoxLed0);
        CheckBoxLed1State = (CheckBox)findViewById(R.id.idCheckBoxLed1);
        CheckBoxMotorState = (CheckBox)findViewById(R.id.idCheckBoxMotor);
        CheckBoxHotState = (CheckBox)findViewById(R.id.idCheckBoxHot);

        EditTextMaxTemperatureH =  (EditText) findViewById(R.id.idEditTextMaxTemperatureH);
        TextViewMaxTemperatureH = (TextView) findViewById(R.id.idTextViewMaxTemperatureH);
        TextViewCurTemperature = (TextView) findViewById(R.id.idTextViewCurTemperature);//当前温度
        TextViewPower = (TextView) findViewById(R.id.idTextViewPower);
        TextViewPowerIcon = (TextView)findViewById(R.id.idTextViewPowerIcon);

        ButtonSystemStateOff = (Button)findViewById(R.id.idButtonSystemStateOff);
        ButtonSystemStateDisconnect = (Button)findViewById(R.id.idButtonSystemStateDisconnect);


        //ButtonRead = (Button) findViewById(R.id.idButtonRead);
        //ButtonWrite = (Button) findViewById(R.id.idButtonWrite);


        findViewById(R.id.idButtonRename).setOnClickListener(this);
        findViewById(R.id.idButtonMaxTemperature).setOnClickListener(this);
        //ButtonSystemStateOff.setOnClickListener(this);
        //ButtonSystemStateDisconnect.setOnClickListener(this);
        //ButtonSystemStateWait.setOnClickListener(this);
        //ButtonSystemStateWork.setOnClickListener(this);

        SendBuf[0] = (byte) 0xA5;
        ReceiveBuf[0] = (byte) 0x5A;

        registerBoradcastReceiver();

        CheckBoxLed0State.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                Log.i(TAG, "led0_switch isChecked = " + isChecked);
                if (isChecked) {
                    led0_state = 2;
                } else {
                    led0_state = 0;
                }
                SendData();
            }
        });

        CheckBoxLed1State.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                // TODO Auto-generated method stub
                Log.i(TAG, "led1_switch isChecked = " + isChecked);
                if (isChecked) {
                    led1_state = 2;
                } else {
                    led1_state = 0;
                }
                SendData();
            }
        });

        CheckBoxMotorState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                // TODO Auto-generated method stub
                Log.i(TAG, "led2_switch isChecked = " + isChecked);
                if (isChecked) {
                    motor_state = true;
                } else {
                    motor_state = false;
                }
                SendData();
            }
        });

        CheckBoxHotState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                // TODO Auto-generated method stub
                Log.i(TAG, "led2_switch isChecked = " + isChecked);
                if (isChecked) {
                    hot_state = true;
                } else {
                    hot_state = false;
                }
                SendData();
            }
        });

        ButtonSystemStateOff.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                system_state =SystemStateOff;
                SendData();
            }
        });

        ButtonSystemStateDisconnect.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                system_state =SystemStateDisconnect;
                SendData();
            }
        });


        new MyThread().start();
    }

    // 接收 rssi 的广播
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "mBroadcastReceiver action = " + action);
            if (action.equals(ACTION_NAME_RSSI)) {
                int rssi = intent.getIntExtra("RSSI", 0);

                // 以下这些参数我 amomcu 自己设置的， 不太具有参考意义，
                //实际上我的本意就是根据rssi的信号前度计算以下距离，
                //以便达到定位目的， 但这个方法并不准  ---amomcu---------20150411

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
//					getActionBar().setTitle("已断开连接，请返回然后重新连接");
                    //connect_state.setText("已断开连接，请退出本界面后重新连接");
                    getActionBar().setTitle("已断开连接，请退出本界面后重新连接");
//					Toast toast = Toast.makeText(getApplicationContext(), "已断开连接",2000);
//					toast.setGravity(Gravity.CENTER, 0, 0);
//					toast.show();
//					finish();
                } else {
                    //connect_state.setText("已连接设备, 当前设备居有低功耗功能，电流仅为100uA左右");
//					getActionBar().setTitle("已连接设备");
                }
            }
        }
    };

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME_RSSI);
        myIntentFilter.addAction(ACTION_CONNECT);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private void SendData() {
        SendBuf[0]=(byte)0xA5;
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
            Log.i(TAG, "char6 SendBuf[" + j + "]=0x" + Integer.toHexString((SendBuf[j] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase());
        }
        DeviceScanActivity.WriteCharX(DeviceScanActivity.gattCharacteristic_char6, SendBuf);
    }

    // 按键事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.idButtonRename: {
                //TextView start_edit_SetDeviceName = (TextView) this.findViewById(R.id.start_edit_SetDeviceName);
                if (EditTextDeviceName.length() > 0) {
                    String str = EditTextDeviceName.getText().toString();
                    DeviceScanActivity.WriteCharX(DeviceScanActivity.gattCharacteristic_char7,str.getBytes());
                } else {
                    Toast.makeText(this, "请输入设备名称 ：BLE4.0-Device", Toast.LENGTH_SHORT).show();
                }
                SendData();
                break;
            }
            case R.id.idButtonMaxTemperature: {
                //TextView start_edit_SetDeviceName = (TextView) this.findViewById(R.id.start_edit_SetDeviceName);
                if (EditTextMaxTemperatureH.length() > 0) {
                    String str = EditTextMaxTemperatureH.getText().toString();
                    max_temp_h_state = Integer.parseInt(str);
                    //Toast.makeText(this, "您输入的是:"+Integer.parseInt(str), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请输入温度最大值:38", Toast.LENGTH_SHORT).show();
                }
                SendData();
                break;
            }
            /*case R.id.idButtonWrite: {//发送写通讯协议命令-写通讯数据
                SendData();
                Toast.makeText(this, "手动写入数据", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.idButtonRead: {
                DeviceScanActivity.ReadCharX(DeviceScanActivity.gattCharacteristic_char6);//发送读通讯协议命令
                Toast.makeText(this, "手动读取数据", Toast.LENGTH_SHORT).show();
                break;
            }*/
        }
    }

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
                battery_state = (ReceiveBuf[3]&0xFF);
                cur_temp_h_state = (ReceiveBuf[4]&0xFF);
                cur_temp_l_state = (ReceiveBuf[5]&0xFF);
                max_temp_h_state = (ReceiveBuf[6]&0xFF);
                max_temp_l_state = (ReceiveBuf[7]&0xFF);
                system_state = (ReceiveBuf[8]&0xFF);

                work_time = (ReceiveBuf[9]&0xFF);
                massage_time = (ReceiveBuf[10]&0xFF);
                device_id = (ReceiveBuf[11]&0xFF);
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

                Log.i(TAG, " DeviceName:"+DeviceName+" led0_state:"+led0_state+" led1_state:"+led1_state+" hot_state:"+hot_state);
                // 更新设备名称
                if (DeviceNameFlag == true) {
                    DeviceNameFlag = false;
                    EditTextDeviceName.setText(DeviceName);
                }

                if(led0_state == 0){
                    CheckBoxLed0State.setBackgroundResource(R.drawable.switch__led_off);
                    CheckBoxLed0State.setText("0");
                }
                else if(led0_state == 1){
                    CheckBoxLed0State.setBackgroundResource(R.drawable.switch_led_red_on);
                    CheckBoxLed0State.setText("1");
                }
                else if(led0_state == 2){
                    CheckBoxLed0State.setBackgroundResource(R.drawable.switch_led_green_on);
                    CheckBoxLed0State.setText("2");
                }
                else if(led0_state == 3){
                    CheckBoxLed0State.setBackgroundResource(R.drawable.switch_led_blue_on);
                    CheckBoxLed0State.setText("3");
                }

                if(led1_state == 0){
                    CheckBoxLed1State.setBackgroundResource(R.drawable.switch__led_off);
                    CheckBoxLed1State.setText("0");
                }
                else if(led1_state == 1){
                    CheckBoxLed1State.setBackgroundResource(R.drawable.switch_led_red_on);
                    CheckBoxLed1State.setText("1");
                }
                else if(led1_state == 2){
                    CheckBoxLed1State.setBackgroundResource(R.drawable.switch_led_green_on);
                    CheckBoxLed1State.setText("2");
                }
                else if(led1_state == 3){
                    CheckBoxLed1State.setBackgroundResource(R.drawable.switch_led_blue_on);
                    CheckBoxLed1State.setText("3");
                }

                if (hot_state){
                    CheckBoxHotState.setBackgroundResource(R.drawable.switch_led_red_on);
                }
                else {
                    CheckBoxHotState.setBackgroundResource(R.drawable.switch__led_off);
                }

                // 更新当前温湿度
                String current_temperature = "温度：" + cur_temp_h_state + "." + cur_temp_l_state + "℃";
                TextViewCurTemperature.setText(current_temperature);

                // 更新最高温湿度
                String max_temperature = "最大温度：" + max_temp_h_state  + "℃";
                TextViewMaxTemperatureH.setText(max_temperature);

                //更新电量图标
                //String string_power = "" + battery_state*25;
                //TextViewPower.setText(string_power);
                Log.i(TAG, " TextViewPowerIcon battery_state:"+battery_state);
                if (battery_state == 0){
                    TextViewPowerIcon.setBackgroundResource(R.drawable.battery0);
                }
                else if (battery_state == 1){
                    TextViewPowerIcon.setBackgroundResource(R.drawable.battery1);
                }
                else if (battery_state == 2){
                    TextViewPowerIcon.setBackgroundResource(R.drawable.battery2);
                }
                else if (battery_state == 3){
                    TextViewPowerIcon.setBackgroundResource(R.drawable.battery3);
                }
                else if (battery_state == 4){
                    TextViewPowerIcon.setBackgroundResource(R.drawable.battery4);
                }
                else if (battery_state == 0xFF){
                    TextViewPowerIcon.setBackgroundResource(R.drawable.battery_charging);
                    Log.i(TAG, " TextViewPowerIcon battery_charging");
                }
                Log.i(TAG, " current_temperature:"+current_temperature+" max_temperature:"+max_temperature+" battery_state:"+battery_state);
            }
        });
    }

    // 线程， 发送消息
    public class MyThread extends Thread {
        public void run() {
            int count = 0;
            int count_start_read = 10;


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 读取设备名称
            UpdateDeviceName();

            while (!Thread.currentThread().isInterrupted()) {
//            	Message msg = null;
//        		msg.what = REFRESH;  
//              mHandler.sendMessage(msg);

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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.i(TAG, "MyThread out...");
        }
    }


    @SuppressWarnings("unused")
    private void UpdateDeviceName() {
        DeviceScanActivity
                .ReadCharX(DeviceScanActivity.gattCharacteristic_char7);
    }

    private void SetTemperatureNotifyUpdate(boolean enable) {
        DeviceScanActivity.setCharacteristicNotification(
                DeviceScanActivity.gattCharacteristic_char6, enable);
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

}
