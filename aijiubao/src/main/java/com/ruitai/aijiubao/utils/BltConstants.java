package com.ruitai.aijiubao.utils;

import java.util.UUID;

/**
 * Created by static on 2017/9/29.
 */

public interface BltConstants {

    UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");


    String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";

    String UUID_CHAR1 = "0000fff1-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR2 = "0000fff2-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR3 = "0000fff3-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR4 = "0000fff4-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR5 = "0000fff5-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR7 = "0000fff7-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR8 = "0000fff8-0000-1000-8000-00805f9b34fb";
    String UUID_CHAR9 = "0000fff9-0000-1000-8000-00805f9b34fb";
    String UUID_CHARA = "0000fffa-0000-1000-8000-00805f9b34fb";


    String ACTION_BROADCAST_NAME_RSSI = "action_broadcast_name_rssi";
    String ACTION_BROADCAST_CONNECT = "action_broadcast_connect";
    String APP_VER = "SmartRF  v1.2 20161203";

    String KEY_INTENT_BROADCAST_EXTRA = "key_intent_extra";

    String SMART_BLT_NAME = "SmartRF-RT";

    int MSG_HANDLER_ON_BLT_READ = 10;
    int MSG_HANDLER_ON_BLT_STOP_SCAN = 20;
}
